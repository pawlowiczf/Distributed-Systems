package main

import (
	"bufio"
	"encoding/json"
	"flag"
	"fmt"
	"io"
	"log"
	"math/rand"
	"net"
	"os"
	"os/signal"
	"syscall"
)

func RandomUsername() string {
	names := []string{"marek", "adam", "filip", "kuba", "dorota", "kamil", "kacper", "andrzej", "tomek"}
	digits := "0123456789"
	randomDigits := make([]byte, 3)

	for a := 0; a < 3; a++ {
		randomDigits[a] = digits[rand.Intn(len(digits))]
	}

	return names[rand.Intn(len(names))] + string(randomDigits)
}

func main() {
	chatID := flag.String("chatID", "aabbc", "Enter chatID to access the particular chat room")
	address := flag.String("address", "127.0.0.2:9090", "Enter your source address IPv4 and port")
	username := flag.String("username", RandomUsername(), "Enter your username")

	flag.Parse()

	localAddr, err := net.ResolveTCPAddr("tcp", *address)
	if err != nil {
		log.Fatalf("cannot resolve TCP IP addr: %s", err)
	}

	dialer := net.Dialer{
		LocalAddr: localAddr,
	}

	fmt.Printf("Dialing server. My IP is: %s\n", localAddr.String())
	fmt.Printf("You're (%s)\n", *username)

	conn, err := dialer.Dial("tcp", "127.0.0.1:8080")
	if err != nil {
		log.Fatalf("cannot dial the server: %s", err)
	}

	defer conn.Close()
	handleShutdownConn(conn, *username, *chatID)

	go func() {
		fmt.Fprintf(conn, "%s\n", *chatID)
		fmt.Fprintf(conn, "%s\n", *username)

		reader := bufio.NewReader(os.Stdin)

		for {
			messageText, err := reader.ReadString('\n')
			if err != nil {
				if err == io.EOF {
					fmt.Println("Stopped reading from stdin...")
					return
				}
				fmt.Println("couldn't read message from stdin: ", err)
				continue
			}

			var message Message
			message.Username = *username
			message.Message = messageText
			message.Type = NormalMessage

			messageData, err := json.Marshal(message)
			if err != nil {
				fmt.Println("couldn't marshal message: ", err)
				continue
			}

			messageData = append(messageData, '\n')
			_, err = conn.Write(messageData)
			if err != nil {
				fmt.Println("couldn't send message to server: ", err)
			}
		}

	}()

	reader := bufio.NewReader(conn)
	for {
		messageData, err := reader.ReadBytes('\n')
		if err != nil {
			if err == io.EOF {
				return
			}
			fmt.Println("cannot read message from data: ", err)
			return
		}

		var message Message
		err = json.Unmarshal(messageData, &message)
		if err != nil {
			fmt.Println("couldn't unmarshal message: ", err)
			continue
		}

		if message.Type == NormalMessage {
			fmt.Printf("[%s] (%s): %s", *chatID, message.Username, message.Message)
			continue 
		}
		if message.Type == JoinedLeftMessage {
			fmt.Println(message.Message)
			continue 
		}
		if message.Type == ShutdownMessage {
			fmt.Println(message.Message)
			return 
		}
	}
}

const (
	NormalMessage   = "normal-message"
	ShutdownMessage = "shutdown-message"
	JoinedLeftMessage = "joined-left-message"
)

type Message struct {
	Username string `json:"username"`
	Message  string `json:"message"`
	Type     string `json:"type"`
}

type ShutdownConn struct {
	Username string `json:"username"`
	ChatID   string `json:"chat_id"`
	Type     string `json:"type"`
}

func handleShutdownConn(conn net.Conn, username string, chatID string) {
	signalChan := make(chan os.Signal, 1)
	signal.Notify(signalChan, os.Interrupt, syscall.SIGTERM)

	go func() {
		<-signalChan
		message := ShutdownConn{
			Username: username,
			ChatID:   chatID,
			Type: ShutdownMessage,
		}

		data, err := json.Marshal(message)
		if err != nil {
			fmt.Println("couldn't marshal message: ", err)
			return
		}
		data = append(data, '\n')
		conn.Write(data)
	}()
}
