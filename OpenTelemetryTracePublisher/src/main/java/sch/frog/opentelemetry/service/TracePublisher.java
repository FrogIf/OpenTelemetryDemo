package sch.frog.opentelemetry.service;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceProto;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sch.frog.opentelemetry.util.CustomThreadFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TracePublisher {

    private static final Logger logger = LoggerFactory.getLogger(TracePublisher.class);

    private final String host;

    private final int port;

    private ExecutorService executorService;

    private ManagedChannel channel;

    public TracePublisher(String host, int port) {
        this.host = host;
        this.port = port;
        this.init();
    }

    private void init(){
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        executorService = new ThreadPoolExecutor(4, 4, 0, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(10), new CustomThreadFactory("trace-publisher-resp"), new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    public void destroy(){
        executorService.shutdown();
    }

    public void publish(List<ResourceSpans> resourceSpans){
        for (ResourceSpans resourceSpan : resourceSpans) {
            try {
                logger.info(JsonFormat.printer().print(resourceSpan));
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
            this.publish(ExportTraceServiceRequest.newBuilder().addResourceSpans(resourceSpan).build());
        }
    }

    private void publish(ExportTraceServiceRequest request){
        Descriptors.ServiceDescriptor serviceDescriptor = TraceServiceProto.getDescriptor().getServices().get(0);
        Descriptors.MethodDescriptor methodDescriptor = serviceDescriptor.getMethods().get(0);

        ListenableFuture<ExportTraceServiceResponse> future = ClientCalls.futureUnaryCall(channel.newCall(
                MethodDescriptor.<ExportTraceServiceRequest, ExportTraceServiceResponse>newBuilder()
                        .setType(MethodDescriptor.MethodType.UNARY)
                        .setFullMethodName(MethodDescriptor.generateFullMethodName(serviceDescriptor.getFullName(), methodDescriptor.getName()))
                        .setRequestMarshaller(REQUEST_MARSHALLER)
                        .setResponseMarshaller(RESPONSE_MARSHALER)
                        .build(),
                CallOptions.DEFAULT
        ), request);

        Futures.addCallback(future, new FutureCallback<>(){
            @Override
            public void onSuccess(@NullableDecl ExportTraceServiceResponse exportTraceServiceResponse) {
                logger.info("trace send finish : {}", exportTraceServiceResponse);
            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.info("trace send failed. ", throwable);
            }
        }, executorService);

    }

    private static final MethodDescriptor.Marshaller<ExportTraceServiceRequest> REQUEST_MARSHALLER =
            new MethodDescriptor.Marshaller<>() {
                @Override
                public InputStream stream(ExportTraceServiceRequest value) {
                    return new ByteArrayInputStream(value.toByteArray());
                }

                @Override
                public ExportTraceServiceRequest parse(InputStream stream) {
                    throw new UnsupportedOperationException("Only for serializing");
                }
            };

    private static final MethodDescriptor.Marshaller<ExportTraceServiceResponse> RESPONSE_MARSHALER =
            new MethodDescriptor.Marshaller<>() {
                @Override
                public InputStream stream(ExportTraceServiceResponse value) {
                    throw new UnsupportedOperationException("Only for parsing");
                }

                @Override
                public ExportTraceServiceResponse parse(InputStream stream) {
                    return ExportTraceServiceResponse.getDefaultInstance();
                }
            };

}
