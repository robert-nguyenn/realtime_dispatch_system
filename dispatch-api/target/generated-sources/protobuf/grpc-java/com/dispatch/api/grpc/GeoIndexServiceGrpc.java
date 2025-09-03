package com.dispatch.api.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Service definition for geo-index operations
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.59.0)",
    comments = "Source: geoindex.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class GeoIndexServiceGrpc {

  private GeoIndexServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "geoindex.GeoIndexService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversRequest,
      com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversResponse> getFindNearestDriversMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "FindNearestDrivers",
      requestType = com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversRequest.class,
      responseType = com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversRequest,
      com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversResponse> getFindNearestDriversMethod() {
    io.grpc.MethodDescriptor<com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversRequest, com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversResponse> getFindNearestDriversMethod;
    if ((getFindNearestDriversMethod = GeoIndexServiceGrpc.getFindNearestDriversMethod) == null) {
      synchronized (GeoIndexServiceGrpc.class) {
        if ((getFindNearestDriversMethod = GeoIndexServiceGrpc.getFindNearestDriversMethod) == null) {
          GeoIndexServiceGrpc.getFindNearestDriversMethod = getFindNearestDriversMethod =
              io.grpc.MethodDescriptor.<com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversRequest, com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "FindNearestDrivers"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GeoIndexServiceMethodDescriptorSupplier("FindNearestDrivers"))
              .build();
        }
      }
    }
    return getFindNearestDriversMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationRequest,
      com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationResponse> getUpdateDriverLocationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdateDriverLocation",
      requestType = com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationRequest.class,
      responseType = com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationRequest,
      com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationResponse> getUpdateDriverLocationMethod() {
    io.grpc.MethodDescriptor<com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationRequest, com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationResponse> getUpdateDriverLocationMethod;
    if ((getUpdateDriverLocationMethod = GeoIndexServiceGrpc.getUpdateDriverLocationMethod) == null) {
      synchronized (GeoIndexServiceGrpc.class) {
        if ((getUpdateDriverLocationMethod = GeoIndexServiceGrpc.getUpdateDriverLocationMethod) == null) {
          GeoIndexServiceGrpc.getUpdateDriverLocationMethod = getUpdateDriverLocationMethod =
              io.grpc.MethodDescriptor.<com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationRequest, com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdateDriverLocation"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GeoIndexServiceMethodDescriptorSupplier("UpdateDriverLocation"))
              .build();
        }
      }
    }
    return getUpdateDriverLocationMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.dispatch.api.grpc.GeoIndexProto.RemoveDriverRequest,
      com.dispatch.api.grpc.GeoIndexProto.RemoveDriverResponse> getRemoveDriverMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RemoveDriver",
      requestType = com.dispatch.api.grpc.GeoIndexProto.RemoveDriverRequest.class,
      responseType = com.dispatch.api.grpc.GeoIndexProto.RemoveDriverResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dispatch.api.grpc.GeoIndexProto.RemoveDriverRequest,
      com.dispatch.api.grpc.GeoIndexProto.RemoveDriverResponse> getRemoveDriverMethod() {
    io.grpc.MethodDescriptor<com.dispatch.api.grpc.GeoIndexProto.RemoveDriverRequest, com.dispatch.api.grpc.GeoIndexProto.RemoveDriverResponse> getRemoveDriverMethod;
    if ((getRemoveDriverMethod = GeoIndexServiceGrpc.getRemoveDriverMethod) == null) {
      synchronized (GeoIndexServiceGrpc.class) {
        if ((getRemoveDriverMethod = GeoIndexServiceGrpc.getRemoveDriverMethod) == null) {
          GeoIndexServiceGrpc.getRemoveDriverMethod = getRemoveDriverMethod =
              io.grpc.MethodDescriptor.<com.dispatch.api.grpc.GeoIndexProto.RemoveDriverRequest, com.dispatch.api.grpc.GeoIndexProto.RemoveDriverResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RemoveDriver"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dispatch.api.grpc.GeoIndexProto.RemoveDriverRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dispatch.api.grpc.GeoIndexProto.RemoveDriverResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GeoIndexServiceMethodDescriptorSupplier("RemoveDriver"))
              .build();
        }
      }
    }
    return getRemoveDriverMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationRequest,
      com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationResponse> getGetDriverLocationMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetDriverLocation",
      requestType = com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationRequest.class,
      responseType = com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationRequest,
      com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationResponse> getGetDriverLocationMethod() {
    io.grpc.MethodDescriptor<com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationRequest, com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationResponse> getGetDriverLocationMethod;
    if ((getGetDriverLocationMethod = GeoIndexServiceGrpc.getGetDriverLocationMethod) == null) {
      synchronized (GeoIndexServiceGrpc.class) {
        if ((getGetDriverLocationMethod = GeoIndexServiceGrpc.getGetDriverLocationMethod) == null) {
          GeoIndexServiceGrpc.getGetDriverLocationMethod = getGetDriverLocationMethod =
              io.grpc.MethodDescriptor.<com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationRequest, com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetDriverLocation"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationResponse.getDefaultInstance()))
              .setSchemaDescriptor(new GeoIndexServiceMethodDescriptorSupplier("GetDriverLocation"))
              .build();
        }
      }
    }
    return getGetDriverLocationMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static GeoIndexServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GeoIndexServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GeoIndexServiceStub>() {
        @java.lang.Override
        public GeoIndexServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GeoIndexServiceStub(channel, callOptions);
        }
      };
    return GeoIndexServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static GeoIndexServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GeoIndexServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GeoIndexServiceBlockingStub>() {
        @java.lang.Override
        public GeoIndexServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GeoIndexServiceBlockingStub(channel, callOptions);
        }
      };
    return GeoIndexServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static GeoIndexServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<GeoIndexServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<GeoIndexServiceFutureStub>() {
        @java.lang.Override
        public GeoIndexServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new GeoIndexServiceFutureStub(channel, callOptions);
        }
      };
    return GeoIndexServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Service definition for geo-index operations
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Find nearest drivers to a given location
     * </pre>
     */
    default void findNearestDrivers(com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversRequest request,
        io.grpc.stub.StreamObserver<com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getFindNearestDriversMethod(), responseObserver);
    }

    /**
     * <pre>
     * Update driver location in the geo-index
     * </pre>
     */
    default void updateDriverLocation(com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationRequest request,
        io.grpc.stub.StreamObserver<com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdateDriverLocationMethod(), responseObserver);
    }

    /**
     * <pre>
     * Remove driver from geo-index
     * </pre>
     */
    default void removeDriver(com.dispatch.api.grpc.GeoIndexProto.RemoveDriverRequest request,
        io.grpc.stub.StreamObserver<com.dispatch.api.grpc.GeoIndexProto.RemoveDriverResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRemoveDriverMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get driver location
     * </pre>
     */
    default void getDriverLocation(com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationRequest request,
        io.grpc.stub.StreamObserver<com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetDriverLocationMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service GeoIndexService.
   * <pre>
   * Service definition for geo-index operations
   * </pre>
   */
  public static abstract class GeoIndexServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return GeoIndexServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service GeoIndexService.
   * <pre>
   * Service definition for geo-index operations
   * </pre>
   */
  public static final class GeoIndexServiceStub
      extends io.grpc.stub.AbstractAsyncStub<GeoIndexServiceStub> {
    private GeoIndexServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GeoIndexServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GeoIndexServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Find nearest drivers to a given location
     * </pre>
     */
    public void findNearestDrivers(com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversRequest request,
        io.grpc.stub.StreamObserver<com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getFindNearestDriversMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Update driver location in the geo-index
     * </pre>
     */
    public void updateDriverLocation(com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationRequest request,
        io.grpc.stub.StreamObserver<com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdateDriverLocationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Remove driver from geo-index
     * </pre>
     */
    public void removeDriver(com.dispatch.api.grpc.GeoIndexProto.RemoveDriverRequest request,
        io.grpc.stub.StreamObserver<com.dispatch.api.grpc.GeoIndexProto.RemoveDriverResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRemoveDriverMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get driver location
     * </pre>
     */
    public void getDriverLocation(com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationRequest request,
        io.grpc.stub.StreamObserver<com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetDriverLocationMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service GeoIndexService.
   * <pre>
   * Service definition for geo-index operations
   * </pre>
   */
  public static final class GeoIndexServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<GeoIndexServiceBlockingStub> {
    private GeoIndexServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GeoIndexServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GeoIndexServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Find nearest drivers to a given location
     * </pre>
     */
    public com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversResponse findNearestDrivers(com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getFindNearestDriversMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Update driver location in the geo-index
     * </pre>
     */
    public com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationResponse updateDriverLocation(com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdateDriverLocationMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Remove driver from geo-index
     * </pre>
     */
    public com.dispatch.api.grpc.GeoIndexProto.RemoveDriverResponse removeDriver(com.dispatch.api.grpc.GeoIndexProto.RemoveDriverRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRemoveDriverMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get driver location
     * </pre>
     */
    public com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationResponse getDriverLocation(com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetDriverLocationMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service GeoIndexService.
   * <pre>
   * Service definition for geo-index operations
   * </pre>
   */
  public static final class GeoIndexServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<GeoIndexServiceFutureStub> {
    private GeoIndexServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected GeoIndexServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new GeoIndexServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Find nearest drivers to a given location
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversResponse> findNearestDrivers(
        com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getFindNearestDriversMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Update driver location in the geo-index
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationResponse> updateDriverLocation(
        com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdateDriverLocationMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Remove driver from geo-index
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dispatch.api.grpc.GeoIndexProto.RemoveDriverResponse> removeDriver(
        com.dispatch.api.grpc.GeoIndexProto.RemoveDriverRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRemoveDriverMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get driver location
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationResponse> getDriverLocation(
        com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetDriverLocationMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_FIND_NEAREST_DRIVERS = 0;
  private static final int METHODID_UPDATE_DRIVER_LOCATION = 1;
  private static final int METHODID_REMOVE_DRIVER = 2;
  private static final int METHODID_GET_DRIVER_LOCATION = 3;

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
        case METHODID_FIND_NEAREST_DRIVERS:
          serviceImpl.findNearestDrivers((com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversRequest) request,
              (io.grpc.stub.StreamObserver<com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversResponse>) responseObserver);
          break;
        case METHODID_UPDATE_DRIVER_LOCATION:
          serviceImpl.updateDriverLocation((com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationRequest) request,
              (io.grpc.stub.StreamObserver<com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationResponse>) responseObserver);
          break;
        case METHODID_REMOVE_DRIVER:
          serviceImpl.removeDriver((com.dispatch.api.grpc.GeoIndexProto.RemoveDriverRequest) request,
              (io.grpc.stub.StreamObserver<com.dispatch.api.grpc.GeoIndexProto.RemoveDriverResponse>) responseObserver);
          break;
        case METHODID_GET_DRIVER_LOCATION:
          serviceImpl.getDriverLocation((com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationRequest) request,
              (io.grpc.stub.StreamObserver<com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationResponse>) responseObserver);
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
          getFindNearestDriversMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversRequest,
              com.dispatch.api.grpc.GeoIndexProto.FindNearestDriversResponse>(
                service, METHODID_FIND_NEAREST_DRIVERS)))
        .addMethod(
          getUpdateDriverLocationMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationRequest,
              com.dispatch.api.grpc.GeoIndexProto.UpdateDriverLocationResponse>(
                service, METHODID_UPDATE_DRIVER_LOCATION)))
        .addMethod(
          getRemoveDriverMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dispatch.api.grpc.GeoIndexProto.RemoveDriverRequest,
              com.dispatch.api.grpc.GeoIndexProto.RemoveDriverResponse>(
                service, METHODID_REMOVE_DRIVER)))
        .addMethod(
          getGetDriverLocationMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationRequest,
              com.dispatch.api.grpc.GeoIndexProto.GetDriverLocationResponse>(
                service, METHODID_GET_DRIVER_LOCATION)))
        .build();
  }

  private static abstract class GeoIndexServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    GeoIndexServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.dispatch.api.grpc.GeoIndexProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("GeoIndexService");
    }
  }

  private static final class GeoIndexServiceFileDescriptorSupplier
      extends GeoIndexServiceBaseDescriptorSupplier {
    GeoIndexServiceFileDescriptorSupplier() {}
  }

  private static final class GeoIndexServiceMethodDescriptorSupplier
      extends GeoIndexServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    GeoIndexServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (GeoIndexServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new GeoIndexServiceFileDescriptorSupplier())
              .addMethod(getFindNearestDriversMethod())
              .addMethod(getUpdateDriverLocationMethod())
              .addMethod(getRemoveDriverMethod())
              .addMethod(getGetDriverLocationMethod())
              .build();
        }
      }
    }
    return result;
  }
}
