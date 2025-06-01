package main

import (
	"net"
	"os"
	"subber-go/generator"
	"subber-go/manager"
	"subber-go/pb"
	"subber-go/service"
	"time"

	"github.com/rs/zerolog"
	"github.com/rs/zerolog/log"

	"google.golang.org/grpc"
	"google.golang.org/grpc/keepalive"
)

func main() {
	log.Logger = log.Output(zerolog.ConsoleWriter{Out: os.Stderr})

	sm := manager.CreateShutdownManager()
	defer sm.Stop()

	eventsChan := make(chan generator.Event)
	store := service.NewSubscriptionStore(eventsChan, sm)

	gen := generator.NewGenerator(eventsChan, sm)

	RunDataFlow(store, gen, sm)
	RunGRPCServer(store, sm)

	sm.WaitGroup.Wait()

}

func RunDataFlow(store *service.SubscriptionStore, gen *generator.Generator, sm *manager.ShutdownManager) {
	sm.WaitGroup.Go(func() error {
		store.ReceiveEvents()
		return nil
	})
	sm.WaitGroup.Go(func() error {
		gen.GenerateRandomData()
		return nil
	})
}

func RunGRPCServer(store *service.SubscriptionStore, sm *manager.ShutdownManager) {
	grpcServer := grpc.NewServer(grpc.KeepaliveParams(keepalive.ServerParameters{
		Time:    30 * time.Second, // interval between pings
		Timeout: 10 * time.Second, // how long to wait for answer
	}))

	subberServer := service.NewSubberServer(store, sm)
	pb.RegisterSubberServer(grpcServer, subberServer)

	listener, err := net.Listen("tcp", "localhost:8080")
	if err != nil {
		log.Fatal().Err(err).Msg("cannot create new listener")
	}

	sm.WaitGroup.Go(func() error {
		log.Info().Msg("running gRPC server")
		err = grpcServer.Serve(listener)
		if err != nil {
			log.Fatal().Err(err).Msg("grpc server error")
			return err
		}
		return nil
	})

	sm.WaitGroup.Go(func() error {
		<-sm.Ctx.Done()

		log.Info().Msg("graceful shutdown gRPC server")
		grpcServer.GracefulStop()
		log.Info().Msg("gRPC server is stopped")

		return nil
	})
}
