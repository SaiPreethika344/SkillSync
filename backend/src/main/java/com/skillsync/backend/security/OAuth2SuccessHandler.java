package com.skillsync.backend.security;

import com.skillsync.backend.model.User;
import com.skillsync.backend.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                          Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;
        
        if (existingUser.isEmpty()) {
            user = new User();
            user.setEmail(email);
            user.setName(name != null ? name : email);
            user.setPremium(false);
            String randomPassword = UUID.randomUUID().toString();
            user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(randomPassword));
            user = userRepository.save(user);
        } else {
            user = existingUser.get();
        }
        
        String token = jwtService.generateToken(user);
        String redirectUrl = frontendUrl + "/oauth2/callback?token=" + token + "&isPremium=" + user.isPremium();
        response.sendRedirect(redirectUrl);
    }
}
