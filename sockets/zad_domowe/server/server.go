package main

import (
	"bufio"
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net"
	"os"
	"os/signal"
	"strings"
	"sync"
	"syscall"
)

const (
	NormalMessage     = "normal-message"
	ShutdownMessage   = "shutdown-message"
	JoinedLeftMessage = "joined-left-message"
)

type Server struct {
	mu    sync.Mutex
	rooms map[string]*ChatRoom
}

type User struct {
	conn     net.Conn
	username string
}

type ChatRoom struct {
	Users []User
}

func CreateServer() *Server {
	return &Server{
		rooms: make(map[string]*ChatRoom),
	}
}

func main() {
	server := CreateServer()
	fmt.Println("[Server] Listening...")
	listener, err := net.Listen("tcp", "127.0.0.1:8080")
	if err != nil {
		log.Fatalf("cannot create listener: %s", err)
	}

	defer listener.Close()

	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()
	server.handleServerShutdownConn(listener, cancel)

	for {
		select {
		case <-ctx.Done():
			fmt.Println("Received ctx done server in main()")
			return 
		default:
			conn, err := listener.Accept()
			if err != nil {
				fmt.Printf("cannot accept connection: %s\n", err)
				continue
			}
	
			fmt.Println("Accepted connection from: ", conn.RemoteAddr().String())
			go server.handleConnection(ctx, conn)
		}
	}
}

func (server *Server) handleConnection(ctx context.Context, conn net.Conn) {
	defer conn.Close()
	reader := bufio.NewReader(conn)

	chatID, err := reader.ReadString('\n')
	chatID = strings.TrimSuffix(chatID, "\n")

	if err != nil {
		fmt.Println("couldn't read chatID from data: ", err)
		return
	}

	username, err := reader.ReadString('\n')
	username = strings.TrimSuffix(username, "\n")

	if err != nil {
		fmt.Println("couldn't read username from data: ", err)
		return
	}

	server.mu.Lock()
	if _, ok := server.rooms[chatID]; !ok {
		server.rooms[chatID] = &ChatRoom{
			Users: []User{{conn: conn, username: username}},
		}

	} else {
		server.rooms[chatID].Users = append(server.rooms[chatID].Users, User{conn, username})
		server.sendHelloToOthers(username, chatID)
	}
	server.mu.Unlock()

	clientCtx, clientCancel := context.WithCancel(context.Background())
	defer clientCancel()

	for {
		select {
		case <-ctx.Done():
			fmt.Println("Received ctx done server in client")
			return 

		case <-clientCtx.Done():
			fmt.Println("Klient done()")
			return 

		default:
			server.handleConnReading(clientCancel, username, chatID, conn, reader)
		}

	}
}

func (server *Server) handleConnReading(
	cancel context.CancelFunc, 
	username string, 
	chatID string, 
	conn net.Conn, 
	reader *bufio.Reader) {

	messageData, err := reader.ReadBytes('\n')
	if err != nil {
		fmt.Println("cannot read message from data: ", err)
		return
	}

	var messageType MessageType
	err = json.Unmarshal(messageData, &messageType)
	if err != nil {
		fmt.Println("couldn't unmarshall message: ", err)
		return 
	}

	if messageType.Type == ShutdownMessage {
		server.removeUserFromChatRoom(messageData, conn)
		cancel()
		return
	}

	if messageType.Type == NormalMessage {
		server.sendMessageToOthers(username, chatID, messageData)
		return
	}
}

func (server *Server) sendHelloToOthers(newUsername string, chatID string) {
	message := Message{
		Username: newUsername,
		Message:  fmt.Sprintf("[%s] (%s) joined the chat!", chatID, newUsername),
		Type:     JoinedLeftMessage,
	}
	messageData, err := json.Marshal(message)
	if err != nil {
		fmt.Println("couldn't marshal message, during sending hello to others:", err)
		return
	}
	messageData = append(messageData, '\n')

	for _, user := range server.rooms[chatID].Users {
		if user.username == newUsername {
			continue
		}

		_, err = user.conn.Write(messageData)
		if err != nil {
			fmt.Println("couldn't send message to server: ", err)
		}
	}
}

func (server *Server) sendByeToOthers(username string, chatID string) {
	message := Message{
		Username: username,
		Message:  fmt.Sprintf("[%s] (%s) left the chat!", chatID, username),
		Type:     JoinedLeftMessage,
	}
	messageData, err := json.Marshal(message)
	if err != nil {
		fmt.Println("couldn't marshal message, during sending bye to others:", err)
		return
	}
	messageData = append(messageData, '\n')

	for _, user := range server.rooms[chatID].Users {
		_, err = user.conn.Write(messageData)
		if err != nil {
			fmt.Println("couldn't send message to server: ", err)
		}
	}
}

func (server *Server) removeUserFromChatRoom(messageData []byte, conn net.Conn) {
	defer conn.Close()

	var shutdownConn ShutdownConn
	err := json.Unmarshal(messageData, &shutdownConn)
	if err != nil {
		fmt.Println("couldn't unmarshal message, during removing user from chat room", err)
		return
	}

	chatID := shutdownConn.ChatID
	username := shutdownConn.Username

	server.sendByeToOthers(username, chatID)

	for a := 0; a < len(server.rooms[chatID].Users); a++ {
		if server.rooms[chatID].Users[a].username == username {
			server.rooms[chatID].Users = append(server.rooms[chatID].Users[:a], server.rooms[chatID].Users[a+1:]...)
			break
		}
	}
}

func (server *Server) sendMessageToOthers(username, chatID string, messageData []byte) {
	for _, user := range server.rooms[chatID].Users {
		if user.username == username {
			continue
		}

		_, err := user.conn.Write(messageData)
		if err != nil {
			fmt.Println("couldn't send message to user: ", err)
		}
	}
}

type ShutdownConn struct {
	Username string `json:"username" validate:"required"`
	ChatID   string `json:"chat_id" validate:"required"`
	Type     string `json:"type"`
}

type MessageType struct {
	Type string `json:"type"`
}

type Message struct {
	Username string `json:"username"`
	Message  string `json:"message"`
	Type     string `json:"type"`
}

func (server *Server) handleServerShutdownConn(listener net.Listener, cancel context.CancelFunc) {
	signalChan := make(chan os.Signal, 1)
	signal.Notify(signalChan, os.Interrupt, syscall.SIGTERM)

	go func() {
		<-signalChan
		cancel()
		listener.Close()
		
		message := Message{
			Username: "server",
			Message:  "[Server] This chatroom was closed!",
			Type:     ShutdownMessage,
		}

		messageData, err := json.Marshal(&message)
		if err != nil {
			fmt.Println("couldn't marshal message, during handling server shutdown: ", err)
			return
		}
		messageData = append(messageData, '\n')

		for _, chatRoom := range server.rooms {
			for _, user := range chatRoom.Users {
				user.conn.Write(messageData)
				user.conn.Close()
			}
		}

		
	}()
}
