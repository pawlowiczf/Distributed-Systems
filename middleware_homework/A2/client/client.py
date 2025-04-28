import grpc
import time
import sys
import streaming_pb2
import streaming_pb2_grpc

def run(user_id):
    while True:
        channel = grpc.insecure_channel('localhost:9090')
        stub = streaming_pb2_grpc.CurrencyServiceStub(channel)
        try:
            print(f"Próba połączenia z serwerem jako {user_id}...")
            request = streaming_pb2.UserRequest(
                userId=user_id,
                currencyEnum=streaming_pb2.CurrencyEnum.DOL
            )

            responses = stub.Connect(request)
            for response in responses:
                print(f"Odebrano: {response}")
        except grpc.RpcError as e:
            print(f"Rozłączono lub błąd: {e.code()}")
            print("Czekam 5 sekund i próbuję ponownie...")
            time.sleep(5)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Użycie: python client.py <userId>")
        sys.exit(1)

    user_id = sys.argv[1]
    run(user_id)
