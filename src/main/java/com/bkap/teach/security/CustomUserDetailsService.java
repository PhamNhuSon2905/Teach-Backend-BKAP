package com.bkap.teach.security;

import com.bkap.teach.entity.User;
import com.bkap.teach.enums.Status;
import com.bkap.teach.repository.UserRepository;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Người dùng không tồn tại : " + username));


        if (user.getStatus() == Status.LOCKED) {
            throw new LockedException("Tài khoản của nguời dùng này đã bị khóa : " + username);
        }

        return UserPrincipal.from(user);
    }
}
