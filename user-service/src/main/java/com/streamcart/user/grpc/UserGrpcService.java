package com.streamcart.user.grpc;

import com.streamcart.proto.user.GetUserByEmailRequest;
import com.streamcart.proto.user.UserResponse;
import com.streamcart.proto.user.UserServiceGrpc;
import com.streamcart.user.model.User;
import com.streamcart.user.repository.UserRepository;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * gRPC counterpart of AuthController. Where a REST controller returns a value,
 * a gRPC service pushes results into a StreamObserver:
 *   onNext(response) -> onCompleted()   for success
 *   onError(StatusRuntimeException)     for failure, with a gRPC status code
 *
 * Note: Spring Security only guards the HTTP server (8081). This Netty server (9091)
 * is unauthenticated by design - it must only be reachable from inside the network.
 */
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserRepository userRepository;

    @Override
    public void getUserByEmail(GetUserByEmailRequest request, StreamObserver<UserResponse> responseObserver) {
        String email = request.getEmail();

        if (email.isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("email must not be blank")
                    .asRuntimeException());
            return;
        }

        userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    responseObserver.onNext(toResponse(user));
                    responseObserver.onCompleted();
                },
                () -> {
                    log.debug("gRPC GetUserByEmail: no user for {}", email);
                    responseObserver.onError(Status.NOT_FOUND
                            .withDescription("User not found: " + email)
                            .asRuntimeException());
                }
        );
    }

    private UserResponse toResponse(User user) {
        return UserResponse.newBuilder()
                .setId(user.getId())
                .setEmail(user.getEmail())
                .setName(user.getName())
                .setRole(user.getRole().name())
                .build();
    }
}
