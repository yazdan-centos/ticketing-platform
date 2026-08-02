package com.mapnaom.ticketingplatform.service;

import com.mapnaom.ticketingplatform.model.Customer;
import com.mapnaom.ticketingplatform.repository.AppUserRepository;
import com.mapnaom.ticketingplatform.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock private AppUserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private AdminUserService service;

    @Test
    void deleteRejectsUserWithAssociatedData() {
        Customer user = user(10L);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(userRepository.hasAssociatedData(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("associated data");
        verify(userRepository, never()).delete(user);
    }

    @Test
    void deleteSoftDeletesUserWithoutAssociatedData() {
        Customer user = user(20L);
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));
        when(userRepository.hasAssociatedData(20L)).thenReturn(false);

        service.delete(20L);

        verify(userRepository).delete(user);
    }

    private Customer user(Long id) {
        Customer user = new Customer();
        user.setId(id);
        user.setDeleted(false);
        return user;
    }
}
