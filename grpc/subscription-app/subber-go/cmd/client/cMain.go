package main

import (
	"context"
	"flag"
	"fmt"
	"io"
	"os"
	"os/signal"
	"subber-go/pb"
	"syscall"
	"time"

	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"

	"github.com/google/uuid"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/credentials/insecure"
	"google.golang.org/grpc/keepalive"
	"google.golang.org/grpc/status"
)

func main() {
	log.Logger = log.Output(zerolog.ConsoleWriter{Out: os.Stderr})

	clientUUIDRandom, _ := uuid.NewRandom()
	clientUUID := flag.String("uuid", clientUUIDRandom.String(), "Provide unique client UUID")
	flag.Parse()

	conn, err := grpc.NewClient(
		"0.0.0.0:8080",
		grpc.WithTransportCredentials(insecure.NewCredentials()),
		grpc.WithKeepaliveParams(keepalive.ClientParameters{
			Timeout:             10 * time.Second,
			Time:                30 * time.Second,
			PermitWithoutStream: true,
		}),
	)

	if err != nil {
		log.Fatal().Err(err).Msg("cannot create new gRPC client")
	}

	log.Printf("Started new client, with UUID: %s", *clientUUID)
	subberClient := pb.NewSubberClient(conn)

	req := &pb.SubscriptionRequest{
		ClientUUID: *clientUUID,
		AssetName:  2,
		Threshold:  30,
		Condition:  &pb.SubscriptionRequest_GreaterThan{},
	}

	ctx := context.Background()
	ctx, stop := signal.NotifyContext(ctx, interruptSignals...)
	defer stop()

	stream, err := subberClient.Subscription(ctx, req)
	if err != nil {
		st, ok := status.FromError(err)
		if ok && st.Code() == codes.Unavailable {
			log.Warn().Err(err).Msg("server unavailable - retrying...")
			time.Sleep(5 * time.Second)
			stream, err = subberClient.Subscription(ctx, req)
		}
		if err != nil {
			log.Fatal().Err(err).Msg("grpc server error")
		}
	}

	for {
		rsp, err := stream.Recv()
		if err != nil {
			st, ok := status.FromError(err)
			if ok && st.Code() == codes.Unavailable {
				log.Error().Err(err).Msg("server unavailable, waiting and retrying to receive")
				// RETRY CONNECTION TODO
				time.Sleep(1 * time.Second)
				continue
			}
			if ok && st.Code() == codes.Canceled {
				log.Info().Msg("stream canceled, shutting down client")
				return
			}
			if err == io.EOF {
				log.Warn().Msg("server ends stream (probably dead), reconnecting again...")
				for a := 0; a < 7; a++ {
					time.Sleep(2 * time.Second)
					stream, err = subberClient.Subscription(ctx, req)
					if err == nil {
						break
					}
				}
				if err != nil {
					log.Fatal().Err(err).Msg("server unresponsive, shutting down client")
				}
			}

			if err != nil {
				log.Fatal().Err(err).Msg("stream receive error")
			}
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

var interruptSignals = []os.Signal{
	os.Interrupt,
	syscall.SIGTERM,
	syscall.SIGINT,
}
