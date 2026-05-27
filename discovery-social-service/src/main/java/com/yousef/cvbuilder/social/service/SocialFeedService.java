package com.yousef.cvbuilder.social.service;

import com.yousef.cvbuilder.identity.grpc.AuthServiceGrpc;
import com.yousef.cvbuilder.identity.grpc.VerifyRequest;
import com.yousef.cvbuilder.identity.grpc.VerifyResponse;
import com.yousef.cvbuilder.social.entity.PublicCv;
import com.yousef.cvbuilder.social.repository.PublicCvRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocialFeedService {

    private final PublicCvRepository publicCvRepository;

    // Automatically injects the gRPC client bound to identity-service via Eureka resolution
    @GrpcClient("identity-service")
    private AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub;

    @Transactional(readOnly = true)
    public List<PublicCv> getAllPublicFeeds() {
        Map<String, Boolean> accountValidityByUserId = new HashMap<>();
        return publicCvRepository.findAll().stream()
                .filter(cv -> accountValidityByUserId.computeIfAbsent(cv.getUserId(), this::isAccountActive))
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicCv getPublicCvDetails(String cvId) {
        PublicCv cv = publicCvRepository.findById(cvId)
                .orElseThrow(() -> new RuntimeException("Public CV not found"));

        // Synchronous gRPC validation call checking real-time account eligibility
        VerifyResponse response = verifyAccount(cv.getUserId());

        if (!response.getIsValid()) {
            throw new RuntimeException("Access Denied: The account owning this CV is suspended or inactive ("
                    + response.getStatus() + ")");
        }

        return cv;
    }

    private boolean isAccountActive(String userId) {
        try {
            return verifyAccount(userId).getIsValid();
        } catch (RuntimeException e) {
            log.warn("Fail-closed gRPC account verification for userId={}: {}", userId, e.getMessage());
            return false;
        }
    }

    private VerifyResponse verifyAccount(String userId) {
        VerifyRequest request = VerifyRequest.newBuilder().setUserId(userId).build();
        return authServiceBlockingStub.verifyAccount(request);
    }
}
