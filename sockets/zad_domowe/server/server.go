package main

import (
	"bufio"
	"encoding/json"
	"fmt"
	"log"
	"net"
	"os"
	"os/signal"
	"strings"
	"syscall"
)

const (
	NormalMessage     = "normal-message"
	ShutdownMessage   = "shutdown-message"
	JoinedLeftMessage = "joined-left-message"
)

type Server struct {
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
	server.handleShutdownConn()

	for {
		conn, err := listener.Accept()
		if err != nil {
			fmt.Printf("cannot accept connection: %s\n", err)
			continue
		}

		fmt.Println("Accepted connection from: ", conn.RemoteAddr().String())
		go server.handleConnection(conn)
	}
}

func (server *Server) handleConnection(conn net.Conn) {
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

	if _, ok := server.rooms[chatID]; !ok {
		server.rooms[chatID] = &ChatRoom{
			Users: []User{{conn: conn, username: username}},
		}

	} else {
		server.rooms[chatID].Users = append(server.rooms[chatID].Users, User{conn, username})
		server.sendHelloToOthers(username, chatID)
	}

	for {
		messageData, err := reader.ReadBytes('\n')
		if err != nil {
			fmt.Println("cannot read message from data: ", err)
			return
		}

		var messageType MessageType
		err = json.Unmarshal(messageData, &messageType)
		if err != nil {
			fmt.Println("couldn't unmarshall message: ", err)
			continue
		}

		if messageType.Type == ShutdownMessage {
			server.removeUserFromChatRoom(messageData, conn)
			return
		}

		if messageType.Type == NormalMessage {
			server.sendMessageToOthers(username, chatID, messageData)
			continue
		}

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
	conn.Close()
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

func (server *Server) handleShutdownConn() {
	signalChan := make(chan os.Signal, 1)
	signal.Notify(signalChan, os.Interrupt, syscall.SIGTERM)

	go func() {
		<-signalChan

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
