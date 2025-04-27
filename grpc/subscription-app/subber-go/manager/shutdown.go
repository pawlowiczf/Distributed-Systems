package manager

import (
	"context"
	"os"
	"os/signal"
	"syscall"

	"golang.org/x/sync/errgroup"
)

type ShutdownManager struct {
	Ctx       context.Context
	WaitGroup *errgroup.Group
	Stop      context.CancelFunc
}

func CreateShutdownManager() *ShutdownManager {
	ctx, stop := signal.NotifyContext(context.Background(), interruptSignals...)

	waitGroup, ctx := errgroup.WithContext(ctx)
	gs := &ShutdownManager{
		Ctx:       ctx,
		WaitGroup: waitGroup,
		Stop: stop,
	}

	return gs
}

var interruptSignals = []os.Signal{
	os.Interrupt,
	syscall.SIGTERM,
	syscall.SIGINT,
}
