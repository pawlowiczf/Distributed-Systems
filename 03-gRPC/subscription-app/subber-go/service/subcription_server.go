package service

import (
	"context"
	"subber-go/manager"
	"subber-go/pb"

	"github.com/rs/zerolog/log"
	"google.golang.org/grpc"
	"google.golang.org/grpc/peer"
)

type SubberServer struct {
	pb.UnimplementedSubberServer

	sm    *manager.ShutdownManager
	store *SubscriptionStore
}

func NewSubberServer(store *SubscriptionStore, sm *manager.ShutdownManager) *SubberServer {
	return &SubberServer{
		store: store,
		sm:    sm,
	}

}

func (ss *SubberServer) Subscription(req *pb.SubscriptionRequest, stream grpc.ServerStreamingServer[pb.SubscriptionResponse]) error {

	// reqCopy, _ := proto.Clone(req).(*pb.SubscriptionRequest)
	p, _ := peer.FromContext(stream.Context())
	log.Info().Str("client UUID", req.GetClientUUID()).Str("client address", p.Addr.String()).Msg("new subscription request")

	ss.store.AddSubscription(req, stream)

	select {
	case <-stream.Context().Done():
	case <-ss.sm.Ctx.Done():
		// closing stream (server shuts down), client receives EOF
		return nil
	}

	ss.store.RemoveSubscription(req)
	log.Info().Str("client UUID", req.GetClientUUID()).Msg("subscription cancelled")

	return nil
}

func (ss *SubberServer) ListOptions(ctx context.Context, req *pb.ListOptionsRequest) (*pb.ListOptionsResponse, error) {
	rsp := &pb.ListOptionsResponse{
		AssetName: []pb.AssetName{
			1,2,3,4,
		},
	}
	return rsp, nil 
}
