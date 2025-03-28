/**
 * Nguyen Dinh Lam
 * Email: kiminonawa1305@gmail.com
 * Phone number: +84 855354919
 * Create at: 3:19 PM - 02/03/2025
 * User: lam-nguyen
 **/

package com.lamnguyen.fashion_e_commerce.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lamnguyen.fashion_e_commerce.domain.ApiResponse;
import com.lamnguyen.fashion_e_commerce.service.authentication.IRedisManager;
import com.lamnguyen.fashion_e_commerce.service.business.user.IUserService;
import com.lamnguyen.fashion_e_commerce.util.JwtTokenUtil;
import com.lamnguyen.fashion_e_commerce.util.property.ApplicationProperty;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Component
public class CheckBlacklistTokenFilter extends OncePerRequestFilter {
    ApplicationProperty applicationProperty;
    IRedisManager manager;
    JwtTokenUtil jwtTokenUtil;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        var token = request.getHeader("Authorization");
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        token = token.substring("Bearer ".length());
        var jwt = jwtTokenUtil.decodeToken(token);
        if (!manager.existAccessTokenIdInBlacklist(jwtTokenUtil.getPayload(jwt).getUserId(), jwt.getId())) {
            filterChain.doFilter(request, response);
            return;
        }


        var writer = response.getWriter();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(writer, ApiResponse.<String>builder()
                .code(HttpServletResponse.SC_UNAUTHORIZED)
                .error("Token not available!")
                .build());
        writer.flush();
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return applicationProperty.getWhiteList().contains(request.getServletPath());
    }
}
