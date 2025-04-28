package server;

import app.pb.HelloRequest;
import app.pb.HelloResponse;
import app.pb.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;

public class HelloService extends HelloServiceGrpc.HelloServiceImplBase {

    @Override
    public void hello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
        String name = request.getName();

        String message = "Hello " + name + "!";

        HelloResponse response = HelloResponse.newBuilder().setMessage(message).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


}
