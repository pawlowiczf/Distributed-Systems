package main

import (
	"context"
	"flag"
	"fmt"
	"log"
	"subber-go/pb"

	"github.com/google/uuid"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/status"
)

func main() {
	clientUUIDRandom, _ := uuid.NewRandom()
	clientUUID := flag.String("uuid", clientUUIDRandom.String(), "Provide unique client UUID")
	flag.Parse()

	conn, err := grpc.NewClient(
		"0.0.0.0:8080",
		grpc.WithTransportCredentials(insecure.NewCredentials()),
	)
	if err != nil {
		log.Fatalf("cannot create new gRPC client: %v", err)
	}

	log.Printf("Started new client, with UUID: %s", *clientUUID)
	subberClient := pb.NewSubberClient(conn)

	req := &pb.SubscriptionRequest{
		ClientUUID: *clientUUID,
		AssetName:  2,
		Threshold:  30,
		Condition:  &pb.SubscriptionRequest_GreaterThan{},
	}

	stream, _ := subberClient.Subscription(context.Background(), req)
	for {
		rsp, err := stream.Recv()
		if err != nil {
			st, ok := status.FromError(err)
			if ok {
				if st.Code() == codes.Unavailable {
					fmt.Println(err)
				}
			}
			fmt.Println(err)
			return
		}
		PrettyResponsePrint(rsp)
	}
}

func PrettyResponsePrint(rsp *pb.SubscriptionResponse) {
	fmt.Println("-------------")
	fmt.Printf("\tAsset name: %s\n", rsp.GetAssetName())
	fmt.Printf("\tCurrent price: %f\n", rsp.GetCurrentPrice())
	fmt.Printf("\tMessage: %s\n", rsp.GetMessage())
	fmt.Printf("\tTime: %s\n", rsp.GetCurrentTime().AsTime().String())
	fmt.Println("-------------")
}
