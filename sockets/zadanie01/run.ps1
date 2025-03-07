Start-Process cmd -ArgumentList '/k java ServerUDP.java'
Start-Sleep -Seconds 3
Start-Process cmd -ArgumentList '/k java ClientUDP.java'
Start-Process cmd -ArgumentList '/k java ClientUDP.java'



