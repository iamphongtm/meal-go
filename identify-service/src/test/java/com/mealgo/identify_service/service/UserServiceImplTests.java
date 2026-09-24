package com.mealgo.identify_service.service;

import com.mealgo.identify_service.domain.enums.UserRole;
import com.mealgo.identify_service.domain.model.User;
import com.mealgo.identify_service.domain.repository.UserRepository;
import com.mealgo.identify_service.dto.request.UserCreationRequest;
import com.mealgo.identify_service.dto.response.UserResponse;
import com.mealgo.identify_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createUserNormalizesEmailAndHashesPassword() {
        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setUserID(UUID.randomUUID());
                    return user;
                });

        UserResponse response = service.createUser(new UserCreationRequest(
                "  Owner@Example.COM ", "password123", UserRole.RESTAURANT));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        User persisted = captor.getValue();
        assertThat(persisted.getEmail()).isEqualTo("owner@example.com");
        assertThat(persisted.getPasswordHash()).isEqualTo("encoded-password");
        assertThat(response.role()).isEqualTo(UserRole.RESTAURANT);
    }
}
