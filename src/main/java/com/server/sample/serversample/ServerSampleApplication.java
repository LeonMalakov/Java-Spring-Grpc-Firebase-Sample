package com.server.sample.serversample;

import com.server.sample.serversample.interceptors.LogInterceptorServer;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.HealthStatusManager;
import org.jobrunr.jobs.mappers.JobMapper;
import org.jobrunr.storage.InMemoryStorageProvider;
import org.jobrunr.storage.StorageProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootApplication
public class ServerSampleApplication {
	public static void main(String[] args) {
		var healthManager = new HealthStatusManager();

		Server server = ServerBuilder
				.forPort(3100)
				.addService(healthManager.getHealthService())
				.intercept(new LogInterceptorServer())
				.build();

		try {
			server.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		SpringApplication.run(ServerSampleApplication.class, args);
	}

	@Bean
	public StorageProvider storageProvider(JobMapper jobMapper) {
		InMemoryStorageProvider storageProvider = new InMemoryStorageProvider();
		storageProvider.setJobMapper(jobMapper);
		return storageProvider;
	}
}
