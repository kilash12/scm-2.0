package com.scm.config;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.scm.entities.Providers;
import com.scm.entities.User;
import com.scm.helpers.AppConstants;
import com.scm.repsitories.UserRepo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuthAuthenicationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuthAuthenicationSuccessHandler.class);

    @Autowired
    private UserRepo userRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        logger.info("OAuth authentication success");

        // Identify the OAuth provider (google, github)
        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
        String provider = oauth2Token.getAuthorizedClientRegistrationId();
        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();

        // Prepare a new user object (temporary)
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setRoleList(List.of(AppConstants.ROLE_USER));
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setPassword("dummy"); // OAuth users don't have a password

        // Extract user details based on the provider
        if ("google".equalsIgnoreCase(provider)) {
            user.setEmail(oauthUser.getAttribute("email"));
            user.setProfilePic(oauthUser.getAttribute("picture"));
            user.setName(oauthUser.getAttribute("name"));
            user.setProviderUserId(oauthUser.getName());
            user.setProvider(Providers.GOOGLE);
            user.setAbout("This account is created using Google.");
        } else if ("github".equalsIgnoreCase(provider)) {
            String email = oauthUser.getAttribute("email");
            if (email == null) {
                email = oauthUser.getAttribute("login") + "@github.com";
            }
            String picture = oauthUser.getAttribute("avatar_url");
            String name = oauthUser.getAttribute("login");
            user.setEmail(email);
            user.setProfilePic(picture);
            user.setName(name);
            user.setProviderUserId(oauthUser.getName());
            user.setProvider(Providers.GITHUB);
            user.setAbout("This account is created using GitHub.");
        } else {
            logger.warn("Unsupported OAuth provider: {}", provider);
            response.sendRedirect("/login?error=unsupported_provider");
            return;
        }

        // Check if a user with this email already exists in the database
        User existingUser = userRepo.findByEmail(user.getEmail()).orElse(null);

        if (existingUser != null) {
            // User exists: update OAuth details if they are not already set
            if (existingUser.getProvider() == null || existingUser.getProvider() == Providers.SELF) {
                existingUser.setProvider(user.getProvider());
                existingUser.setProviderUserId(user.getProviderUserId());
                existingUser.setProfilePic(user.getProfilePic());
                existingUser.setName(user.getName());  // optionally update name
                existingUser.setEmailVerified(true);
                existingUser.setEnabled(true);
                userRepo.save(existingUser);
                logger.info("Existing user updated with OAuth info: {}", existingUser.getEmail());
            }
            // Use the existing user for the session
            user = existingUser;
        } else {
            // New user: save to database
            userRepo.save(user);
            logger.info("New OAuth user saved: {}", user.getEmail());
        }

        // Redirect to the user's profile page
        new DefaultRedirectStrategy().sendRedirect(request, response, "/user/profile");
    }
}