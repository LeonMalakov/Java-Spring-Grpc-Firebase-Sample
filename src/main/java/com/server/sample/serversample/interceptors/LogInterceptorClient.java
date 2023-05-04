package com.server.sample.serversample.interceptors;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogInterceptorClient implements io.grpc.ClientInterceptor {
    private final Logger _logger = LoggerFactory.getLogger("GRPC");

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> listener, Metadata headers) {
                var interceptedListener =
                        new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(listener) {
                            @Override
                            public void onMessage(RespT message) {
                                _logger.info(String.format("[ClientInterceptor] Receive: %s - %s",
                                        methodDescriptor.getFullMethodName(), message.toString()));
                                super.onMessage(message);
                            }
                        };
                super.start(interceptedListener, headers);
            }

            @Override
            public void sendMessage(ReqT message) {
                _logger.info(String.format("[ClientInterceptor] Send: %s - %s",
                        methodDescriptor.getFullMethodName(), message.toString()));
                super.sendMessage(message);
            }
        };
    }
}
