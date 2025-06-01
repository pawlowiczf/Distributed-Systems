package service

import (
	"context"
	"pcbook-go/pb"
	"pcbook-go/sample"
	"testing"

	"github.com/stretchr/testify/require"
	"google.golang.org/grpc/codes"
)

func TestServerCreateLaptop(t *testing.T) {
	t.Parallel()

	testCases := []struct {
		name   string
		laptop *pb.Laptop
		store  LaptopStore
		code   codes.Code
	}{
		{
			name: "success_with_id",
			laptop: sample.NewLaptop(),
			store: NewInMemoryLaptopStore(),
			code: codes.OK,
		},
	}

	for _, test := range testCases {
		t.Run(test.name, func(t *testing.T) {
			t.Parallel()

			req := &pb.CreateLaptopRequest{
				Laptop: test.laptop,
			}
			server := NewLaptopServer(test.store)

			rsp, err := server.CreateLaptop(context.Background(), req)
			if test.code == codes.OK {
				require.NoError(t, err)
				require.NotNil(t, rsp)
				require.NotEmpty(t, rsp.GetId())
			} else {
				require.Error(t, err)
				require.Nil(t, rsp)
			}
		})
	}
}
