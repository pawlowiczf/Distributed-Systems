import socket

serverIP = "127.0.0.1"
serverPort = 8080

clientIP = "127.0.0.2"
clientPort = 9090

request = (300).to_bytes(4, byteorder='little')

clientSocket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
clientSocket.bind((clientIP, clientPort))

clientSocket.sendto(request, (serverIP, serverPort))

print("Sent message to server")

data, address = clientSocket.recvfrom(1024)
response = int.from_bytes(data, byteorder='little')

print(f"[Client] Received response from server: {response}")

clientSocket.close()


