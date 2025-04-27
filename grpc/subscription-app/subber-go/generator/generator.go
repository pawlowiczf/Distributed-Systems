package generator

import (
	"math/rand"
	"subber-go/pb"
	"time"
)

type Event struct {
	AssetName pb.AssetName
	Value     float64
}

type Generator struct {
	EventsChan chan Event
}

func NewGenerator(eventsChan chan Event) *Generator {
	return &Generator{
		EventsChan: eventsChan,
	}
}

func (generator *Generator) GenerateRandomData() {
	for {
		event := Event{
			AssetName: pb.AssetName(1 + rand.Intn(4)),
			Value:     float64(29 + rand.Intn(6)),
		}

		generator.EventsChan <- event
		time.Sleep(1 * time.Second)
	}
}
