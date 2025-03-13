package main

import (
	"bufio"
	"context"
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

const (
	ServerTCPAddress = "127.0.0.1:8080"
	ServerUDPAddress = "127.0.0.1:8081"
	MulticastUDP     = "239.255.255.250:7000"
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

func setupTCPConn(address string, username string, chatID string) net.Conn {
	localAddr, err := net.ResolveTCPAddr("tcp", address)
	if err != nil {
		log.Fatalf("cannot resolve TCP IP addr: %s", err)
	}

	dialer := net.Dialer{
		LocalAddr: localAddr,
	}

	fmt.Printf("Dialing server. My IP is: %s\n", localAddr.String())
	fmt.Printf("You're (%s)\n", username)

	conn, err := dialer.Dial("tcp", ServerTCPAddress)
	if err != nil {
		log.Fatalf("cannot dial the server: %s", err)
	}

	fmt.Fprintf(conn, "%s\n", chatID)
	fmt.Fprintf(conn, "%s\n", username)

	return conn
}

func setupUDPConn(address string, username string, chatID string) *net.UDPConn {
	localAddr, err := net.ResolveUDPAddr("udp", address)
	if err != nil {
		log.Fatalf("cannot resolve UDP IP addr: %s", err)
	}

	udpConn, err := net.ListenUDP("udp", localAddr)
	if err != nil {
		log.Fatalf("cannot listen on udp conn: %s", err)
	}

	message := Message{
		Username: username,
		Message:  "",
		Type:     HelloUDP,
		ChatID:   chatID,
	}
	messageData, _ := json.Marshal(message)

	serverAddr, _ := net.ResolveUDPAddr("udp", ServerUDPAddress)
	_, err = udpConn.WriteToUDP(messageData, serverAddr)
	if err != nil {
		log.Fatalf("couldn't send hello message to udp server: %s", err)
	}

	return udpConn
}

func setupMulticastUDPConn() *net.UDPConn {
	addr, err := net.ResolveUDPAddr("udp", MulticastUDP)
	if err != nil {
		log.Fatalf("cannot resolve multicast udp address: %s", err)
	}

	ifaces, _ := net.Interfaces()
	mUdpConn, err := net.ListenMulticastUDP("udp", &ifaces[1], addr)
	if err != nil {
		log.Fatalf("cannot listen on multicast udp: %s", err)
	}

	return mUdpConn
}

func main() {
	chatID := flag.String("chatID", "aabbc", "Enter chatID to access the particular chat room")
	address := flag.String("address", "127.0.0.2:2001", "Enter your source address IPv4 and port")
	udpaddress := flag.String("udpaddress", "127.0.0.2:2002", "Enter your UDP address")
	username := flag.String("username", RandomUsername(), "Enter your username")

	flag.Parse()

	conn := setupTCPConn(*address, *username, *chatID)
	defer conn.Close()

	udpConn := setupUDPConn(*address, *username, *chatID)
	defer udpConn.Close()

	mUdpConn := setupMulticastUDPConn()
	defer mUdpConn.Close()

	ctx, cancel := context.WithCancel(context.Background())
	handleShutdownConn(conn, mUdpConn, udpConn, cancel, *username, *chatID)

	go func() {
		reader := bufio.NewReader(os.Stdin)

		for {
			messageText, err := reader.ReadString('\n')
			if err != nil {
				select {
				case <-ctx.Done():
					return
				default:
					if err == io.EOF {
						return
					}
					fmt.Println("couldn't read message from stdin: ", err)
					return
				}
			}

			var message Message
			message.Username = *username
			message.Message = messageText
			message.Type = NormalMessage

			if messageText[0] == 'U' && messageText[1] == '\r' {
				message.Message = rabbit
				message.ChatID = *chatID
				err := sendUDPMessageToServer(udpConn, message)
				if err != nil {
					cancel()
					return
				}

			} else if messageText[0] == 'M' && messageText[1] == '\r' {
				message.Message = rabbit
				message.ChatID = "[EVERYONE]"

				localAddr, err := net.ResolveUDPAddr("udp", *udpaddress)
				if err != nil {
					cancel()
					fmt.Println("cannot resolve udp addr", err)
					return
				}
				remoteAddr, err := net.ResolveUDPAddr("udp", MulticastUDP)
				if err != nil {
					cancel()
					fmt.Println("cannot resolve udp addr", err)
					return
				}

				conn, err := net.DialUDP("udp", localAddr, remoteAddr)
				if err != nil {
					cancel()
					fmt.Println("cannot dial multicast udp", err)
					return 
				}
				fmt.Println(conn.LocalAddr().String())

				messageData, err := json.Marshal(message)
				if err != nil {
					fmt.Println("cannot marshal message", err)
					continue
				}
				_, err = conn.Write(messageData)
				if err != nil {
					fmt.Println(err)

				}
				conn.Close()

			} else {
				err := sendTCPMessageToServer(conn, message)
				if err != nil {
					cancel()
					return
				}
			}

		}
	}()

	go func() {
		buffer := make([]byte, 1024)

		for {
			length, _, err := udpConn.ReadFromUDP(buffer)
			if err != nil {
				select {
				case <-ctx.Done():
					return

				default:
					if err == io.EOF {
						return
					}
					fmt.Println("cannot read message from udp conn: ", err)
					return
				}
			}

			var message Message
			err = json.Unmarshal(buffer[:length], &message)
			if err != nil {
				fmt.Println("couldn't unmarshal message, during handling udp conn")
				continue
			}

			fmt.Printf("[%s] (%s): %s", *chatID, message.Username, message.Message)
		}
	}()

	go func() {
		buffer := make([]byte, 1024)
		for {
			fmt.Println(mUdpConn.LocalAddr().String())
			// length, _, err := mUdpConn.ReadFrom(buffer)
			length, _, err := mUdpConn.ReadFromUDP(buffer)
			if err != nil {
				select {
				case <-ctx.Done():
					return

				default:
					if err == io.EOF {
						return
					}
					fmt.Println("cannot read message from udp conn: ", err)
					return
				}
			}

			var message Message
			err = json.Unmarshal(buffer[:length], &message)
			if err != nil {
				fmt.Println("couldn't unmarshal message, during handling udp conn")
				continue
			}

			fmt.Printf("[%s] (%s): %s", *chatID, message.Username, message.Message)
		}
	}()

	reader := bufio.NewReader(conn)
	for {
		messageData, err := reader.ReadBytes('\n')
		if err != nil {
			select {
			case <-ctx.Done():
				return

			default:
				if err == io.EOF {
					return
				}
				fmt.Println("cannot read message from data: ", err)
				return
			}
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
			cancel()
			return
		}
	}
}

const (
	NormalMessage     = "normal-message"
	ShutdownMessage   = "shutdown-message"
	JoinedLeftMessage = "joined-left-message"
	HelloUDP          = "hello-udp-from-client"
)

type Message struct {
	Username string `json:"username"`
	Message  string `json:"message"`
	Type     string `json:"type"`
	ChatID   string `json:"chat_id"`
}

type ShutdownConn struct {
	Username string `json:"username"`
	ChatID   string `json:"chat_id"`
	Type     string `json:"type"`
}

func sendTCPMessageToServer(conn net.Conn, message Message) error {
	messageData, err := json.Marshal(message)
	if err != nil {
		return fmt.Errorf("couldn't marshal message: %s", err)
	}

	messageData = append(messageData, '\n')
	_, err = conn.Write(messageData)
	if err != nil {
		fmt.Println("couldn't send message to server: ", err)
	}

	return nil
}

func sendUDPMessageToServer(conn *net.UDPConn, message Message) error {
	serverAddr, _ := net.ResolveUDPAddr("udp", ServerUDPAddress)

	messageData, err := json.Marshal(message)
	if err != nil {
		return fmt.Errorf("couldn't marshal message: %s", err)
	}

	messageData = append(messageData, '\n')
	_, err = conn.WriteToUDP(messageData, serverAddr)
	if err != nil {
		return fmt.Errorf("couldn't send message to server UDP: %s", err)
	}

	return nil
}
func handleShutdownConn(conn net.Conn, mUdpConn *net.UDPConn, udpConn *net.UDPConn, cancel context.CancelFunc, username string, chatID string) {
	signalChan := make(chan os.Signal, 1)
	signal.Notify(signalChan, os.Interrupt, syscall.SIGTERM)

	go func() {
		<-signalChan
		cancel()
		mUdpConn.Close()
		udpConn.Close()

		message := ShutdownConn{
			Username: username,
			ChatID:   chatID,
			Type:     ShutdownMessage,
		}

		data, err := json.Marshal(message)
		if err != nil {
			fmt.Println("couldn't marshal message: ", err)
			return
		}
		data = append(data, '\n')
		conn.Write(data)
		// conn.Close()
	}()
}

var rabbit string = `
         ,
        /|      __
       / |   ,-~ /
      Y :|  //  /
      | jj /( .^
      >-"~"-v"
     /       Y
    jo  o    |
   ( ~T~     j
    >._-' _./
   /   "~"  |
  Y     _,  |
 /| ;-"~ _  l
/ l/ ,-"~    \
\//\/      .- \
 Y        /    Y 
 l       I     !
 ]\      _\    /"\
(" ~----( ~   Y.  )
`
