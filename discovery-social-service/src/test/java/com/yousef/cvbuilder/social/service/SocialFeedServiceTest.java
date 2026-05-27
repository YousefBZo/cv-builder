package com.yousef.cvbuilder.social.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.yousef.cvbuilder.identity.grpc.AuthServiceGrpc;
import com.yousef.cvbuilder.identity.grpc.VerifyRequest;
import com.yousef.cvbuilder.identity.grpc.VerifyResponse;
import com.yousef.cvbuilder.social.entity.PublicCv;
import com.yousef.cvbuilder.social.repository.PublicCvRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SocialFeedServiceTest {

    @Mock
    private PublicCvRepository publicCvRepository;

    @Mock
    private AuthServiceGrpc.AuthServiceBlockingStub authStub;

    private SocialFeedService socialFeedService;

    @BeforeEach
    void setUp() throws Exception {
        socialFeedService = new SocialFeedService(publicCvRepository);
        Field field = SocialFeedService.class.getDeclaredField("authServiceBlockingStub");
        field.setAccessible(true);
        field.set(socialFeedService, authStub);
    }

    @Test
    void feedFiltersSuspendedAccountsThroughGrpcVerification() {
        PublicCv activeCv = PublicCv.builder().id("cv-active").userId("active-user").build();
        PublicCv suspendedCv = PublicCv.builder().id("cv-suspended").userId("suspended-user").build();

        when(publicCvRepository.findAll()).thenReturn(List.of(activeCv, suspendedCv));
        when(authStub.verifyAccount(VerifyRequest.newBuilder().setUserId("active-user").build()))
                .thenReturn(VerifyResponse.newBuilder().setIsValid(true).setStatus("ACTIVE").build());
        when(authStub.verifyAccount(VerifyRequest.newBuilder().setUserId("suspended-user").build()))
                .thenReturn(VerifyResponse.newBuilder().setIsValid(false).setStatus("SUSPENDED").build());

        assertThat(socialFeedService.getAllPublicFeeds()).containsExactly(activeCv);
    }

    @Test
    void detailsDenySuspendedOwner() {
        PublicCv suspendedCv = PublicCv.builder().id("cv-1").userId("suspended-user").build();

        when(publicCvRepository.findById("cv-1")).thenReturn(Optional.of(suspendedCv));
        when(authStub.verifyAccount(VerifyRequest.newBuilder().setUserId("suspended-user").build()))
                .thenReturn(VerifyResponse.newBuilder().setIsValid(false).setStatus("SUSPENDED").build());

        assertThatThrownBy(() -> socialFeedService.getPublicCvDetails("cv-1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access Denied")
                .hasMessageContaining("SUSPENDED");
    }
}
