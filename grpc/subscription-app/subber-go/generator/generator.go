package generator

import (
	"math/rand"
	"subber-go/manager"
	"subber-go/pb"
	"time"

	"github.com/rs/zerolog/log"
)

type Event struct {
	AssetName pb.AssetName
	Value     float64
}

type Generator struct {
	EventsChan chan Event
	sm         *manager.ShutdownManager
}

func NewGenerator(eventsChan chan Event, sm *manager.ShutdownManager) *Generator {
	return &Generator{
		EventsChan: eventsChan,
		sm:         sm,
	}
}

func (generator *Generator) GenerateRandomData() {
	for {
		select {
		case <-generator.sm.Ctx.Done():
			log.Info().Msg("shutting down random data generator")
			return 
		default:
			event := Event{
				AssetName: pb.AssetName(1 + rand.Intn(4)),
				Value:     float64(29 + rand.Intn(6)),
			}
	
			generator.EventsChan <- event
			time.Sleep(1 * time.Second)
		}
	}
}
