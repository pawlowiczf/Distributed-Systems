package service

import (
	"context"
	"fmt"
	"subber-go/generator"
	"subber-go/manager"
	"subber-go/pb"
	"sync"
	"time"

	"github.com/google/uuid"
	"github.com/rs/zerolog/log"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/timestamppb"
)

type SubscriptionStore struct {
	sm *manager.ShutdownManager

	Clients    map[uuid.UUID]*Client
	EventsChan chan generator.Event
	mutex      sync.Mutex
}

type Client struct {
	ClientUUID    uuid.UUID
	ClientStream  grpc.ServerStreamingServer[pb.SubscriptionResponse]
	Buffer        []*pb.SubscriptionResponse
	Subscriptions []*Subscription
}

type Condition interface{}
type GreaterThan struct{}
type LessThan struct{}

type Subscription struct {
	AssetName pb.AssetName
	Condition Condition
	Threshold float64
}

func NewSubscriptionStore(eventsChan chan generator.Event, sm *manager.ShutdownManager) *SubscriptionStore {
	return &SubscriptionStore{
		Clients:    make(map[uuid.UUID]*Client),
		EventsChan: eventsChan,
		mutex:      sync.Mutex{},
		sm:         sm,
	}
}

func (ss *SubscriptionStore) AddSubscription(req *pb.SubscriptionRequest, stream grpc.ServerStreamingServer[pb.SubscriptionResponse]) {
	ss.mutex.Lock()
	defer ss.mutex.Unlock()

	clientUUIDString := req.GetClientUUID()
	clientUUID, err := uuid.Parse(clientUUIDString)
	if err != nil {
		clientUUID, _ = uuid.NewRandom()
	}

	// Client is already registered. Check if it is a new, unique sub. Send buffered messages.
	if client, ok := ss.Clients[clientUUID]; ok {
		client.ClientStream = stream
		ss.SendBufferedMessages(client)

		if !ss.CheckIfUniqueSubscription(client, req) {
			return
		}

		subscription := &Subscription{
			AssetName: req.GetAssetName(),
			Condition: DetermineConditionType(req),
			Threshold: req.GetThreshold(),
		}

		ss.Clients[clientUUID].Subscriptions = append(ss.Clients[clientUUID].Subscriptions, subscription)
		return
	}

	client := &Client{
		ClientUUID:    clientUUID,
		ClientStream:  stream,
		Buffer:        []*pb.SubscriptionResponse{},
		Subscriptions: []*Subscription{},
	}
	subscription := &Subscription{
		AssetName: req.GetAssetName(),
		Condition: DetermineConditionType(req),
		Threshold: req.GetThreshold(),
	}

	ss.Clients[clientUUID] = client
	ss.Clients[clientUUID].Subscriptions = append(ss.Clients[clientUUID].Subscriptions, subscription)
}

func DetermineConditionType(req *pb.SubscriptionRequest) Condition {
	switch req.GetCondition().(type) {
	case *pb.SubscriptionRequest_GreaterThan:
		return &GreaterThan{}
	case *pb.SubscriptionRequest_LessThan:
		return &LessThan{}
	default:
		return &LessThan{}
	}
}

func (ss *SubscriptionStore) CheckIfUniqueSubscription(client *Client, req *pb.SubscriptionRequest) bool {
	//
	for _, subscription := range client.Subscriptions {
		//
		if subscription.AssetName != req.GetAssetName() {
			continue
		}
		if subscription.Threshold != req.GetThreshold() {
			continue
		}

		switch subscription.Condition.(type) {
		case *GreaterThan:
			if _, ok := req.GetCondition().(*pb.SubscriptionRequest_GreaterThan); ok {
				return false
			}
		case *LessThan:
			if _, ok := req.GetCondition().(*pb.SubscriptionRequest_LessThan); ok {
				return false
			}
		}
	}

	return true
}

func (ss *SubscriptionStore) ShouldSendMessage(event generator.Event, subscription *Subscription) bool {
	if event.AssetName != subscription.AssetName {
		return false
	}

	switch subscription.Condition.(type) {
	case *GreaterThan:
		if event.Value < subscription.Threshold {
			return false
		}
	case *LessThan:
		if event.Value > subscription.Threshold {
			return false
		}
	}
	return true
}

func (ss *SubscriptionStore) ReceiveEvents() {
	for {
		select {
		case event := <-ss.EventsChan:
			ss.NotifyClients(event)
		case <-ss.sm.Ctx.Done():
			log.Info().Msg("shutting down event receiver")
			return
		}
	}
}

func (ss *SubscriptionStore) SendBufferedMessages(client *Client) {
	for _, message := range client.Buffer {
		err := client.ClientStream.Send(message)
		if err != nil {
			fmt.Println(err)
		}
	}
	client.Buffer = []*pb.SubscriptionResponse{}
}

func (ss *SubscriptionStore) NotifyClients(event generator.Event) {
	ss.mutex.Lock()
	defer ss.mutex.Unlock()

	var message string
	value := event.Value

	for _, client := range ss.Clients {
		for _, subscription := range client.Subscriptions {
			//
			if !ss.ShouldSendMessage(event, subscription) {
				continue
			}

			threshold := subscription.Threshold

			switch subscription.Condition.(type) {
			case *GreaterThan:
				if value < threshold {
					continue
				}
				message = fmt.Sprintf("%s exceeded %f", pb.AssetName_name[int32(event.AssetName)], subscription.Threshold)

			case *LessThan:
				if value > threshold {
					continue
				}
				message = fmt.Sprintf("%s dropped below %f", pb.AssetName_name[int32(event.AssetName)], subscription.Threshold)
			}

			ss.SendMessage(client, event, message)
		}
	}
}

func (ss *SubscriptionStore) SendMessage(client *Client, event generator.Event, message string) {
	//
	rsp := &pb.SubscriptionResponse{
		AssetName:       event.AssetName,
		CurrentPrice:    event.Value,
		Message:         message,
		CurrentTime:     timestamppb.New(time.Now()),
		LastTimeOccured: []*timestamppb.Timestamp{},
	}

	if client.ClientStream == nil {
		client.Buffer = append(client.Buffer, rsp)
		return
	}

	err := client.ClientStream.Send(rsp)
	if err != nil {
		st, ok := status.FromError(err)
		if ok && st.Code() == codes.Unavailable {
			client.ClientStream = nil
			client.Buffer = append(client.Buffer, rsp)
			return
		}

		if client.ClientStream.Context().Err() == context.Canceled {
			log.Info().Msg("client context cancelled")
			return
		}

		fmt.Println(err)
	}
}

func (ss *SubscriptionStore) RemoveSubscription(req *pb.SubscriptionRequest) {
	ss.mutex.Lock()
	defer ss.mutex.Unlock()

	clientUUID, _ := uuid.Parse(req.GetClientUUID())

	client := ss.Clients[clientUUID]
	for a, subscription := range client.Subscriptions {
		if subscription.AssetName != req.GetAssetName() {
			continue
		}
		if subscription.Threshold != req.GetThreshold() {
			continue
		}

		switch req.GetCondition().(type) {
		case *pb.SubscriptionRequest_GreaterThan:
			if _, ok := subscription.Condition.(LessThan); ok {
				continue
			}
		case *pb.SubscriptionRequest_LessThan:
			if _, ok := subscription.Condition.(GreaterThan); ok {
				continue
			}
		}

		client.Subscriptions = append(client.Subscriptions[:a], client.Subscriptions[a+1:]...)
		ss.Clients[clientUUID] = client
		return
		// TODO: jeśli jest więcej subskrybcji, to musimy je też tu usunąć
	}
}
