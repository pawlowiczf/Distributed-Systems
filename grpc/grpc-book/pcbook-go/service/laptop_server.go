package service

import (
	"context"
	"errors"
	"log"
	"pcbook-go/pb"

	"github.com/google/uuid"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

type LaptopServer struct {
	pb.UnimplementedLaptopServiceServer
	Store LaptopStore
}

func NewLaptopServer(store LaptopStore) *LaptopServer {
	return &LaptopServer{
		Store: store,
	}
}

func (server *LaptopServer) CreateLaptop(ctx context.Context, req *pb.CreateLaptopRequest) (*pb.CreateLaptopResponse, error) {
	laptop := req.GetLaptop()
	log.Printf("received a create-laptop request with id: %s", laptop.GetId())

	if len(laptop.GetId()) > 0 {
		_, err := uuid.Parse(laptop.GetId())
		if err != nil {
			return nil, status.Errorf(codes.InvalidArgument, "laptop ID is not a valid UUID: %v", err)
		}
	} else {
		id, err := uuid.NewRandom()
		if err != nil {
			return nil, status.Errorf(codes.Internal, "cannot generate a new laptop ID: %v", err)
		}
		laptop.Id = id.String()
	}

	// save the laptop to db
	err := server.Store.Save(laptop)
	if err != nil {
		code := codes.Internal
		if errors.Is(err, ErrAlreadyExists) {
			code = codes.AlreadyExists
		}
		return nil, status.Errorf(code, "canniot save laptop to the store: %v", err)
	}

	log.Printf("saved laptop with id: %s", laptop.GetId())

	return &pb.CreateLaptopResponse{
		Id: laptop.GetId(),
	}, nil
}

func (server *LaptopServer) SearchLaptop(req *pb.SearchLaptopRequest, stream grpc.ServerStreamingServer[pb.SearchLaptopResponse]) error {
	filter := req.GetFilter()

	err := server.Store.Search(context.Background(), filter, func(laptop *pb.Laptop) error {
		rsp := &pb.SearchLaptopResponse{
			Laptop: laptop,
		}
		err := stream.Send(rsp)
		return err 
	})
	if err != nil {
		return status.Errorf(codes.Internal, "unexpected error: %v", err)
	}

	return nil 
}
