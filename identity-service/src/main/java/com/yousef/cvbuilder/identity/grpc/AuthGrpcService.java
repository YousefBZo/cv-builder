package com.yousef.cvbuilder.identity.grpc;

import com.yousef.cvbuilder.identity.entity.UserAccount;
import com.yousef.cvbuilder.identity.repository.UserRepository;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private final UserRepository userRepository;

    @Override
    public void verifyAccount(VerifyRequest request, StreamObserver<VerifyResponse> responseObserver) {
        Optional<UserAccount> accountOpt = userRepository.findById(request.getUserId());

        VerifyResponse.Builder responseBuilder = VerifyResponse.newBuilder();

        String status = accountOpt.map(UserAccount::getStatus)
                .map(String::trim)
                .map(String::toUpperCase)
                .orElse("NOT_FOUND");

        if (accountOpt.isPresent() && "ACTIVE".equals(status)) {
            responseBuilder.setIsValid(true).setStatus("ACTIVE");
        } else {
            responseBuilder.setIsValid(false).setStatus(status);
        }

        log.info("gRPC account verification userId={} status={} valid={}",
                request.getUserId(), status, responseBuilder.getIsValid());

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
