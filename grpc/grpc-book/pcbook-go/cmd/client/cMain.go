package main

import (
	"context"
	"log"
	"pcbook-go/pb"
	"pcbook-go/sample"

	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

func main() {
	conn, err := grpc.NewClient("0.0.0.0:8080", grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		log.Fatal("cannot create new client: ", err)
	}

	laptopClient := pb.NewLaptopServiceClient(conn)

	laptop := sample.NewLaptop()
	req := &pb.CreateLaptopRequest{
		Laptop: laptop,
	}

	rsp, err := laptopClient.CreateLaptop(context.Background(), req)
	if err != nil {
		log.Fatal("error create laptop rcp: ", err)
	}

	log.Printf("Response: %v\n", rsp)
}
