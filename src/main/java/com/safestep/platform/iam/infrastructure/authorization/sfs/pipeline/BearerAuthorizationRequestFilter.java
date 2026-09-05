package com.safestep.platform.iam.infrastructure.authorization.sfs.pipeline;

import com.safestep.platform.iam.infrastructure.authorization.sfs.model.UsernamePasswordAuthenticationTokenBuilder;
import com.safestep.platform.iam.infrastructure.tokens.jwt.BearerTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Bearer Authorization Request Filter.
 * <p>
 * This class is responsible for filtering requests and setting the user authentication. It extends the
 * OncePerRequestFilter class.
 * </p>
 *
 * @see OncePerRequestFilter
 */
@Slf4j
public class BearerAuthorizationRequestFilter extends OncePerRequestFilter {

    private final BearerTokenService tokenService;

    @Qualifier("defaultUserDetailsService")
    private final UserDetailsService userDetailsService;

    public BearerAuthorizationRequestFilter(BearerTokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * This method is responsible for filtering requests and setting the user authentication.
     *
     * @param request
     *            The request object.
     * @param response
     *            The response object.
     * @param filterChain
     *            The filter chain object.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = tokenService.getBearerTokenFrom(request);
            if (token != null && tokenService.validateToken(token)) {
                String username = tokenService.getUsernameFromToken(token);
                var userDetails = userDetailsService.loadUserByUsername(username);
                if (isAccountUsable(userDetails)) {
                    var securityContext = SecurityContextHolder.createEmptyContext();
                    securityContext.setAuthentication(UsernamePasswordAuthenticationTokenBuilder.build(userDetails,
                            request));
                    SecurityContextHolder.setContext(securityContext);
                } else {
                    log.info("Bearer token belongs to a disabled or locked account");
                }
            } else {
                log.info("Token is not valid");
            }

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private boolean isAccountUsable(org.springframework.security.core.userdetails.UserDetails userDetails) {
        return userDetails.isEnabled() && userDetails.isAccountNonLocked() && userDetails.isAccountNonExpired()
                && userDetails.isCredentialsNonExpired();
    }
}
