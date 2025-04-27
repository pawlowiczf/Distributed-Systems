package main

import (
	"log"
	"net"
	"subber-go/generator"
	"subber-go/pb"
	"subber-go/service"

	"google.golang.org/grpc"
)

func main() {
	grpcServer := grpc.NewServer()

	eventsChan := make(chan generator.Event)

	gen := generator.NewGenerator(eventsChan)
	

	store := service.NewSubscriptionStore(eventsChan)
	
	subberServer := service.NewSubberServer(store)

	pb.RegisterSubberServer(grpcServer, subberServer)

	listener, err := net.Listen("tcp", "0.0.0.0:8080")
	if err != nil {
		log.Fatalf("cannot create new listener: %v", err)
	}

	go store.ReceiveEvents()
	go gen.GenerateRandomData()
	grpcServer.Serve(listener)
}
