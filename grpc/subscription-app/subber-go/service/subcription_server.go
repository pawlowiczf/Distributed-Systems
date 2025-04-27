package service

import (
	"fmt"
	"subber-go/pb"

	"google.golang.org/grpc"
	"google.golang.org/grpc/peer"
)

type SubberServer struct {
	pb.UnimplementedSubberServer
	store *SubscriptionStore
}

func NewSubberServer(store *SubscriptionStore) *SubberServer {
	return &SubberServer{
		store: store,
	}

}

func (ss *SubberServer) Subscription(req *pb.SubscriptionRequest, stream grpc.ServerStreamingServer[pb.SubscriptionResponse]) error {

	// reqCopy, _ := proto.Clone(req).(*pb.SubscriptionRequest)
	p, _ := peer.FromContext(stream.Context())

	fmt.Printf("New subscription request\nClient UUID: %s\nClient Address: %s\n", req.GetClientUUID(), p.Addr.String())

	ss.store.AddSubscription(req, stream)

	<-stream.Context().Done()
	ss.store.RemoveSubscription()
	fmt.Printf("Subscription cancelled, client UUID: %s\n", req.GetClientUUID())
	return nil
}
