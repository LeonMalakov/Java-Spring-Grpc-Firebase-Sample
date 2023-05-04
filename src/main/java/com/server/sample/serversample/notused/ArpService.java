package com.server.sample.serversample.notused;

import arp.*;
import io.grpc.stub.StreamObserver;

//@GRpcService
public class ArpService extends ArpGrpc.ArpImplBase {
    @Override
    public void wakeUpNotify(ArpWakeUpNotifyRequest request, StreamObserver<ArpWakeUpNotifyResponse> responseObserver) {
        // Print request.
        System.out.println("ArpService.wakeUpNotify: " + request.toString());

        // Create response.
        var response = ArpWakeUpNotifyResponse.newBuilder()
                .setResult(true)
                .build();

        // Send response.
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void healthStateSubscribe(ArpHealthStateSubscribeRequest request, StreamObserver<ArpHealthStateSubscribeResponse> responseObserver) {
        // Print request.
        System.out.println("ArpService.healthStateSubscribe: " + request.toString());

        // Create response.
        var response = ArpHealthStateSubscribeResponse.newBuilder()
                .setId("Core")
                .setStatus(ArpServiceStatus.Serving)
                .build();

        // Send response.
        responseObserver.onNext(response);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
