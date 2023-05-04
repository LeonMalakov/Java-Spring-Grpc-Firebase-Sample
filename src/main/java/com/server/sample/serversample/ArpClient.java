package com.server.sample.serversample;

import arp.ArpGrpc;
import arp.ArpHealthStateSubscribeResponse;
import arp.ArpWakeUpNotifyResponse;
import com.server.sample.serversample.interceptors.LogInterceptorClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ArpClient {
    private enum State {
        Initial,
        WakeUpNotifyDone,
        HealthStateSubscribeDone,
        Disposed
    }

    private ManagedChannel _channel;
    private ArpGrpc.ArpStub _stub;
    private State _state;

    @Autowired
    public ArpClient(JobScheduler jobScheduler) {
        jobScheduler.enqueue(this::run);
    }

    @Job(name = "ArpClient")
    public void run() {
        _channel = ManagedChannelBuilder.forAddress("127.0.0.1", 3000)
                .usePlaintext()
                .intercept(new LogInterceptorClient())
                .build();

        _stub = ArpGrpc.newStub(_channel);

        _state = State.Initial;

        next();
    }

    public CompletableFuture waitFinished() {
        return CompletableFuture.runAsync(() -> {
            while (_state != State.Disposed) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void next() {
        switch (_state) {
            case Initial:
                wakeUpNotify();
                _state = State.WakeUpNotifyDone;
                break;

            case WakeUpNotifyDone:
                healthStateSubscribe();
                _state = State.HealthStateSubscribeDone;
                break;

            case HealthStateSubscribeDone:
                _channel.shutdown();
                _state = State.Disposed;
                break;
        }
    }

    private void wakeUpNotify() {
        var request = arp.ArpWakeUpNotifyRequest.newBuilder()
                .setId("IncomeDbGateway")
                .build();

        var streamObserver = new StreamObserver<ArpWakeUpNotifyResponse>() {
            @Override
            public void onNext(ArpWakeUpNotifyResponse arpWakeUpNotifyResponse) {
                // Print response as JSON
                System.out.println("ArpClient.wakeUpNotify: " + arpWakeUpNotifyResponse.toString());
            }

            @Override
            public void onError(Throwable throwable) {
                // Print error
                System.out.println("ArpClient.wakeUpNotify: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                // Print completed
                System.out.println("ArpClient.wakeUpNotify: completed");
                next();
            }
        };

        _stub.wakeUpNotify(request, streamObserver);
    }

    private void healthStateSubscribe(){
        var request = arp.ArpHealthStateSubscribeRequest.newBuilder()
                .setServiceId("IncomeDbGateway")
                .setServices(arp.ArpServiceCollection.newBuilder()
                        .addList("Core")
                        .addList("IncomeDbGateway"))
                .build();

        var streamObserver = new StreamObserver<arp.ArpHealthStateSubscribeResponse>() {

            @Override
            public void onNext(ArpHealthStateSubscribeResponse arpHealthStateSubscribeResponse) {
                System.out.println("ArpClient.healthStateSubscribe: " + arpHealthStateSubscribeResponse.toString());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("ArpClient.healthStateSubscribe: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("ArpClient.healthStateSubscribe: completed");
                next();
            }
        };

        _stub.healthStateSubscribe(request, streamObserver);
    }
}
