package com.pcbook.service;

import com.pcbook.pb.CreateLaptopRequest;
import com.pcbook.pb.CreateLaptopResponse;
import com.pcbook.pb.Laptop;
import com.pcbook.pb.LaptopServiceGrpc;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.UUID;
import java.util.logging.Logger;

public class LaptopService extends LaptopServiceGrpc.LaptopServiceImplBase {
    private static final Logger logger = Logger.getLogger(LaptopService.class.getName());
    private final LaptopStore store;

    public LaptopService(LaptopStore store) {
        this.store = store;
    }

    @Override
    public void createLaptop(CreateLaptopRequest request, StreamObserver<CreateLaptopResponse> responseObserver) {
        Laptop laptop = request.getLaptop();

        String id = laptop.getId();
        logger.info("got a create-laptop request with ID: " + id);

        UUID uuid;
        if (id.isEmpty()) {
            uuid = UUID.randomUUID();
        } else {
            try {
                uuid = UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
                return;
            }
        }

        Laptop other = laptop.toBuilder().setId(uuid.toString()).build();
        try {
            store.Save(other);
        } catch (AlreadyExistsException e) {
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription(e.getMessage()).asRuntimeException());
            return;
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
            return;
        }

        CreateLaptopResponse rsp = CreateLaptopResponse.newBuilder().setId(uuid.toString()).build();
        responseObserver.onNext(rsp);
        responseObserver.onCompleted();

        logger.info("saved laptop with ID: " + uuid.toString());
    }
}
