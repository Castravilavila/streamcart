package com.streamcart.order.client;

import com.streamcart.order.exception.CustomerNotFoundException;
import com.streamcart.order.exception.UserServiceUnavailableException;
import com.streamcart.proto.user.GetUserByEmailRequest;
import com.streamcart.proto.user.UserResponse;
import com.streamcart.proto.user.UserServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Thin wrapper around the generated gRPC stub. The rest of order-service never
 * sees gRPC types or StatusRuntimeException - only domain exceptions.
 */
@Slf4j
@Component
public class UserServiceClient {

    private static final long DEADLINE_SECONDS = 2;

    // Field injection is required here: the starter creates the stub from the
    // "user-service" channel defined in application.properties and injects it
    // after construction. A blocking stub is fine for a simple request/response call.
    @GrpcClient("user-service")
    private UserServiceGrpc.UserServiceBlockingStub userStub;

    public UserResponse getUserByEmail(String email) {
        GetUserByEmailRequest request = GetUserByEmailRequest.newBuilder()
                .setEmail(email)
                .build();

        try {
            // Deadline = max time we're willing to wait for this call. Without one, a hung
            // user-service would hang every order request with it.
            return userStub
                    .withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS)
                    .getUserByEmail(request);

        } catch (StatusRuntimeException e) {
            Status.Code code = e.getStatus().getCode();

            if (code == Status.Code.NOT_FOUND) {
                throw new CustomerNotFoundException(email);
            }

            // UNAVAILABLE (server down), DEADLINE_EXCEEDED (too slow), or anything unexpected.
            log.error("gRPC GetUserByEmail failed with status {}: {}", code, e.getStatus().getDescription());
            throw new UserServiceUnavailableException(e);
        }
    }
}
