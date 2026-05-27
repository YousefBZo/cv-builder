package com.yousef.cvbuilder.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yousef.cvbuilder.identity.dto.RegisterRequest;
import com.yousef.cvbuilder.identity.entity.UserAccount;
import com.yousef.cvbuilder.identity.event.UserRegisteredEvent;
import com.yousef.cvbuilder.identity.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private IdentityService identityService;

    @Test
    void registerCreatesActiveUserAndPublishesRegistrationEvent() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("123456");
        request.setFullName("New User");

        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(org.mockito.ArgumentMatchers.any(UserAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserAccount saved = identityService.register(request);

        assertThat(saved.getId()).isNotBlank();
        assertThat(saved.getStatus()).isEqualTo("ACTIVE");
        assertThat(saved.getEmail()).isEqualTo("new@example.com");

        ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(kafkaTemplate).send(eq("user-registration-topic"), eq(saved.getId()), eventCaptor.capture());
        assertThat(eventCaptor.getValue().getUserId()).isEqualTo(saved.getId());
        assertThat(eventCaptor.getValue().getFullName()).isEqualTo("New User");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");

        when(userRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(UserAccount.builder().email("existing@example.com").build()));

        assertThatThrownBy(() -> identityService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already registered");
    }
}
