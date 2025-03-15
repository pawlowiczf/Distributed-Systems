cd ./server 
Start-Process cmd -ArgumentList '/k go run server.go'
Start-Sleep -Seconds 2
cd ..
cd ./client
Start-Process cmd -ArgumentList '/k go run client.go --address 127.0.0.2:2001 --udpaddress 127.0.0.2:2002'
Start-Sleep -Seconds 2
Start-Process cmd -ArgumentList '/k go run client.go --address 127.0.0.3:3001 --udpaddress 127.0.0.3:3002'

Start-Sleep -Seconds 2
Start-Process cmd -ArgumentList '/k go run client.go --address 127.0.0.4:4001 --udpaddress 127.0.0.4:4002 --chatID gjaua'
Start-Sleep -Seconds 1
Start-Process cmd -ArgumentList '/k go run client.go --address 127.0.0.5:5005 --udpaddress 127.0.0.5:5006 --chatID gjaua'

cd ..


