package main

import (
	"bufio"
	"encoding/json"
	"fmt"
	"log"
	"net"
	"strings"

	"github.com/go-playground/validator/v10"
)

var validate = validator.New()

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
		message, err := reader.ReadString('\n')
		if err != nil {
			fmt.Println("cannot read message from data: ", err)
			return
		}

		var shutdownMessage ShutdownConn
		err = json.Unmarshal([]byte(message), &shutdownMessage)
		if err != nil {
			fmt.Println("couldn't unmarshall message: ", err)
			continue 
		}

		if err = validate.Struct(shutdownMessage); err == nil {
			server.sendByeToOthers(shutdownMessage.Username, chatID)

			for a := 0; a < len(server.rooms[chatID].Users); a++ {
				if server.rooms[chatID].Users[a].username == shutdownMessage.Username {
					server.rooms[chatID].Users = append(server.rooms[chatID].Users[:a], server.rooms[chatID].Users[a+1:]...)
					break
				}
			}
			conn.Close()
			return
		}

		for _, user := range server.rooms[chatID].Users {
			if user.username == username {
				continue
			}

			_, err := user.conn.Write([]byte(message))
			if err != nil {
				fmt.Println("couldn't send message to user: ", err)
			}
		}
	}
}

func (server *Server) sendHelloToOthers(newUsername string, chatID string) {
	for _, user := range server.rooms[chatID].Users {
		if user.username == newUsername {
			continue
		}
		fmt.Fprintf(user.conn, "[%s] (%s) joined the chat!\n", chatID, newUsername)
	}
}

func (server *Server) sendByeToOthers(username string, chatID string) {
	for _, user := range server.rooms[chatID].Users {
		// if user.username == username {
		// 	continue
		// }
		fmt.Fprintf(user.conn, "[%s] (%s) left the chat!\n", chatID, username)
	}
}

type ShutdownConn struct {
	Username string `json:"username" validate:"required"`
	ChatID   string `json:"chat_id" validate:"required"`
}
