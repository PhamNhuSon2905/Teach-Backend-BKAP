package com.bkap.teach.security;

import com.bkap.teach.entity.User;
import com.bkap.teach.enums.Status;
import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

@Getter
public class UserPrincipal implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String password;
    private final String role;
    private final Status status;
    private final String fullname;
    private final String avatar;

    public UserPrincipal(Long id,
                         String username,
                         String password,
                         String role,
                         Status status,
                         String fullname,
                         String avatar) {

        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
        this.fullname = fullname;
        this.avatar = avatar;
    }

    public static UserPrincipal from(User user) {
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole().name(),
                user.getStatus(),
                user.getFullname(),
                user.getAvatar()
        );
    }

    public String getFullName() {
        return fullname;
    }

    public String getDisplayAvatar() {
        return (avatar != null && !avatar.isBlank())
                ? avatar
                : "/assets/images/default_admin.jpg";
    }

    public static void updateAuthentication(User updatedUser) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return;

        UserPrincipal newPrincipal = UserPrincipal.from(updatedUser);

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                newPrincipal,
                authentication.getCredentials(),
                newPrincipal.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public boolean isAccountNonLocked() {
        return status == Status.ACTIVE;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == Status.ACTIVE;
    }
}
