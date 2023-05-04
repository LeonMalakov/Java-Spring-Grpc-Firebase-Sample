package com.server.sample.serversample.interceptors;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogInterceptorServer implements io.grpc.ServerInterceptor {
    private final Logger _logger = LoggerFactory.getLogger("GRPC");

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        var interceptedCall = new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(serverCall) {
            @Override
            public void sendMessage(RespT message) {
                _logger.info(String.format("[ServerInterceptor] Send: %s - %s",
                        serverCall.getMethodDescriptor().getFullMethodName(), message.toString()));
                super.sendMessage(message);
            }
        };

        var listener = serverCallHandler.startCall(interceptedCall, metadata);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            @Override
            public void onMessage(ReqT message) {
                _logger.info(String.format("[ServerInterceptor] Receive: %s - %s",
                        serverCall.getMethodDescriptor().getFullMethodName(), message.toString()));
                super.onMessage(message);
            }
        };
    }
}
