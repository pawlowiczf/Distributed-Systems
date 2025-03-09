cd ./server 
Start-Process cmd -ArgumentList '/k go run server.go'
Start-Sleep -Seconds 3
cd ..
Start-Process cmd -ArgumentList '/k go run client/client.go --address 127.0.0.2:9091'
Start-Sleep -Seconds 2
Start-Process cmd -ArgumentList '/k go run client/client.go --address 127.0.0.3:9092'

Start-Sleep -Seconds 2
Start-Process cmd -ArgumentList '/k go run client/client.go --address 127.0.0.4:9093 --chatID gjaua'
Start-Process cmd -ArgumentList '/k go run client/client.go --address 127.0.0.5:9094 --chatID gjaua'


