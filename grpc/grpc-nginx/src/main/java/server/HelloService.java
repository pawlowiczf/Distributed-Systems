package server;

import app.pb.HelloRequest;
import app.pb.HelloResponse;
import app.pb.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;

public class HelloService extends HelloServiceGrpc.HelloServiceImplBase {
    private final int port;

    public HelloService(int port) {
        this.port = port;
    }

    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        String name = request.getName();

        String message = "Hello " + name + "!";

        HelloResponse response = HelloResponse.newBuilder()
                .setMessage(message)
                .setPort(port)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void helloStream(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        String name = request.getName();

        String message = "Hello " + name + "!";

        HelloResponse response = HelloResponse.newBuilder()
                .setMessage(message)
                .setPort(port)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
