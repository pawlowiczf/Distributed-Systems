package sr.grpc.gen;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.71.0)",
    comments = "Source: calculator.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class AdvancedCalculatorGrpc {

  private AdvancedCalculatorGrpc() {}

  public static final java.lang.String SERVICE_NAME = "calculator.AdvancedCalculator";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<sr.grpc.gen.ComplexArithmeticOpArguments,
      sr.grpc.gen.ComplexArithmeticOpResult> getComplexOperationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ComplexOperation",
      requestType = sr.grpc.gen.ComplexArithmeticOpArguments.class,
      responseType = sr.grpc.gen.ComplexArithmeticOpResult.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<sr.grpc.gen.ComplexArithmeticOpArguments,
      sr.grpc.gen.ComplexArithmeticOpResult> getComplexOperationMethod() {
    io.grpc.MethodDescriptor<sr.grpc.gen.ComplexArithmeticOpArguments, sr.grpc.gen.ComplexArithmeticOpResult> getComplexOperationMethod;
    if ((getComplexOperationMethod = AdvancedCalculatorGrpc.getComplexOperationMethod) == null) {
      synchronized (AdvancedCalculatorGrpc.class) {
        if ((getComplexOperationMethod = AdvancedCalculatorGrpc.getComplexOperationMethod) == null) {
          AdvancedCalculatorGrpc.getComplexOperationMethod = getComplexOperationMethod =
              io.grpc.MethodDescriptor.<sr.grpc.gen.ComplexArithmeticOpArguments, sr.grpc.gen.ComplexArithmeticOpResult>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ComplexOperation"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  sr.grpc.gen.ComplexArithmeticOpArguments.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  sr.grpc.gen.ComplexArithmeticOpResult.getDefaultInstance()))
              .setSchemaDescriptor(new AdvancedCalculatorMethodDescriptorSupplier("ComplexOperation"))
              .build();
        }
      }
    }
    return getComplexOperationMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static AdvancedCalculatorStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AdvancedCalculatorStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AdvancedCalculatorStub>() {
        @java.lang.Override
        public AdvancedCalculatorStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AdvancedCalculatorStub(channel, callOptions);
        }
      };
    return AdvancedCalculatorStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports all types of calls on the service
   */
  public static AdvancedCalculatorBlockingV2Stub newBlockingV2Stub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AdvancedCalculatorBlockingV2Stub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AdvancedCalculatorBlockingV2Stub>() {
        @java.lang.Override
        public AdvancedCalculatorBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AdvancedCalculatorBlockingV2Stub(channel, callOptions);
        }
      };
    return AdvancedCalculatorBlockingV2Stub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static AdvancedCalculatorBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AdvancedCalculatorBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AdvancedCalculatorBlockingStub>() {
        @java.lang.Override
        public AdvancedCalculatorBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AdvancedCalculatorBlockingStub(channel, callOptions);
        }
      };
    return AdvancedCalculatorBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static AdvancedCalculatorFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<AdvancedCalculatorFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<AdvancedCalculatorFutureStub>() {
        @java.lang.Override
        public AdvancedCalculatorFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new AdvancedCalculatorFutureStub(channel, callOptions);
        }
      };
    return AdvancedCalculatorFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void complexOperation(sr.grpc.gen.ComplexArithmeticOpArguments request,
        io.grpc.stub.StreamObserver<sr.grpc.gen.ComplexArithmeticOpResult> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getComplexOperationMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service AdvancedCalculator.
   */
  public static abstract class AdvancedCalculatorImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return AdvancedCalculatorGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service AdvancedCalculator.
   */
  public static final class AdvancedCalculatorStub
      extends io.grpc.stub.AbstractAsyncStub<AdvancedCalculatorStub> {
    private AdvancedCalculatorStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AdvancedCalculatorStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AdvancedCalculatorStub(channel, callOptions);
    }

    /**
     */
    public void complexOperation(sr.grpc.gen.ComplexArithmeticOpArguments request,
        io.grpc.stub.StreamObserver<sr.grpc.gen.ComplexArithmeticOpResult> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getComplexOperationMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service AdvancedCalculator.
   */
  public static final class AdvancedCalculatorBlockingV2Stub
      extends io.grpc.stub.AbstractBlockingStub<AdvancedCalculatorBlockingV2Stub> {
    private AdvancedCalculatorBlockingV2Stub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AdvancedCalculatorBlockingV2Stub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AdvancedCalculatorBlockingV2Stub(channel, callOptions);
    }

    /**
     */
    public sr.grpc.gen.ComplexArithmeticOpResult complexOperation(sr.grpc.gen.ComplexArithmeticOpArguments request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getComplexOperationMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do limited synchronous rpc calls to service AdvancedCalculator.
   */
  public static final class AdvancedCalculatorBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<AdvancedCalculatorBlockingStub> {
    private AdvancedCalculatorBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AdvancedCalculatorBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AdvancedCalculatorBlockingStub(channel, callOptions);
    }

    /**
     */
    public sr.grpc.gen.ComplexArithmeticOpResult complexOperation(sr.grpc.gen.ComplexArithmeticOpArguments request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getComplexOperationMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service AdvancedCalculator.
   */
  public static final class AdvancedCalculatorFutureStub
      extends io.grpc.stub.AbstractFutureStub<AdvancedCalculatorFutureStub> {
    private AdvancedCalculatorFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AdvancedCalculatorFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new AdvancedCalculatorFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<sr.grpc.gen.ComplexArithmeticOpResult> complexOperation(
        sr.grpc.gen.ComplexArithmeticOpArguments request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getComplexOperationMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_COMPLEX_OPERATION = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_COMPLEX_OPERATION:
          serviceImpl.complexOperation((sr.grpc.gen.ComplexArithmeticOpArguments) request,
              (io.grpc.stub.StreamObserver<sr.grpc.gen.ComplexArithmeticOpResult>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getComplexOperationMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              sr.grpc.gen.ComplexArithmeticOpArguments,
              sr.grpc.gen.ComplexArithmeticOpResult>(
                service, METHODID_COMPLEX_OPERATION)))
        .build();
  }

  private static abstract class AdvancedCalculatorBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    AdvancedCalculatorBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return sr.grpc.gen.CalculatorProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("AdvancedCalculator");
    }
  }

  private static final class AdvancedCalculatorFileDescriptorSupplier
      extends AdvancedCalculatorBaseDescriptorSupplier {
    AdvancedCalculatorFileDescriptorSupplier() {}
  }

  private static final class AdvancedCalculatorMethodDescriptorSupplier
      extends AdvancedCalculatorBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    AdvancedCalculatorMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (AdvancedCalculatorGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new AdvancedCalculatorFileDescriptorSupplier())
              .addMethod(getComplexOperationMethod())
              .build();
        }
      }
    }
    return result;
  }
}
