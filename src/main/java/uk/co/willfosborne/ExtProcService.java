package uk.co.willfosborne;

import io.envoyproxy.envoy.service.ext_proc.v3.*;
import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

@GrpcService
public class ExtProcService extends ExternalProcessorGrpc.ExternalProcessorImplBase {
    private static final Logger log = LoggerFactory.getLogger(ExtProcService.class);
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
    private ProcessingResponse handleRequests(ProcessingRequest request) {
        log.info("Got a request!!!");
        log.info("Request: {}", request);
//        Multi<ProcessingResponse> responseStream
        if (request.hasRequestHeaders()) {
//            var headers = request.getRequestHeaders();
            log.info("Got request headers request");
            var resp = HeadersResponse.newBuilder()
                    .build();
            return ProcessingResponse.newBuilder()
                    .setRequestHeaders(resp)
                    .build();
        }
        if (request.hasResponseHeaders()) {
            log.info("Got response headers request");
            var resp = HeadersResponse.newBuilder()
                    .build();
            return ProcessingResponse.newBuilder()
                    .setResponseHeaders(resp)
                    .build();
        }
        if (request.hasRequestBody()) {
            var body = request.getRequestBody();
            byte[] data = request.getRequestBody()
                            .toByteArray();
            System.out.println(bytesToHex(data));
            log.info("Got request body request, attempting to parse frame...");
            Frame frame = Frame.parse(body.toByteArray());
            log.info("Parsed frame: {}", frame);
//            return ProcessingResponse.newBuilder()
//                    .setRequestBody(BodyResponse.newBuilder().build()
//                    .build();
            ImmediateResponse immediateResponse = ImmediateResponse.newBuilder()
//                    .setStatus(HttpStatus.newBuilder()
//                            .setCode(StatusCode.BadRequest)
//                            .build())
                    .build();
            return ProcessingResponse.newBuilder()
                    .setImmediateResponse(immediateResponse)
                    .build();
        }
        throw new IllegalArgumentException("Can't handle this type of request.");
    }

    @Override
    public StreamObserver<ProcessingRequest> process(StreamObserver<ProcessingResponse> responseObserver) {
        log.info("opening stream");
       return new StreamObserver<>() {
           @Override
           public void onNext(ProcessingRequest processingRequest) {
//               log.info("Got request: {}", processingRequest);
               responseObserver.onNext(handleRequests(processingRequest));
           }

           @Override
           public void onError(Throwable throwable) {
               log.error("Error", throwable);
           }

           @Override
           public void onCompleted() {
               log.info("Stream completed.");
           }
       };
    }

//    @Override
//    public Multi<ProcessingResponse> process(Multi<ProcessingRequest> request) {
//
//        return request.onItem()
//                .invoke((req) -> log.info("req: {}", req))
//                .flatMap(this::handleRequests);
//    }
}
