import socket

serverIP = "127.0.0.1"
serverPort = 8080

request = 'żółta gęś'
clientSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
clientSocket.sendto(bytes(request, 'UTF-8'), (serverIP, serverPort))

clientSocket.close()


