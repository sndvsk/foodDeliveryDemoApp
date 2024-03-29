package com.example.FoodDeliveryDemoApp.security.service;

import com.example.FoodDeliveryDemoApp.component.userItems.Role;
import com.example.FoodDeliveryDemoApp.component.userItems.admin.domain.Admin;
import com.example.FoodDeliveryDemoApp.component.userItems.admin.repository.AdminRepository;
import com.example.FoodDeliveryDemoApp.component.userItems.customer.domain.Customer;
import com.example.FoodDeliveryDemoApp.component.userItems.customer.repository.CustomerRepository;
import com.example.FoodDeliveryDemoApp.component.userItems.owner.domain.Owner;
import com.example.FoodDeliveryDemoApp.component.userItems.owner.repository.OwnerRepository;
import com.example.FoodDeliveryDemoApp.component.userItems.user.domain.User;
import com.example.FoodDeliveryDemoApp.component.userItems.user.repository.UserRepository;
import com.example.FoodDeliveryDemoApp.component.utils.UserDetailValidation;
import com.example.FoodDeliveryDemoApp.exception.CustomAccessDeniedException;
import com.example.FoodDeliveryDemoApp.exception.CustomBadRequestException;
import com.example.FoodDeliveryDemoApp.exception.CustomNotFoundException;
import com.example.FoodDeliveryDemoApp.component.userItems.user.dto.AuthenticationRequest;
import com.example.FoodDeliveryDemoApp.component.userItems.user.dto.AuthenticationResponse;
import com.example.FoodDeliveryDemoApp.component.userItems.user.dto.RegisterRequest;
import com.example.FoodDeliveryDemoApp.exception.CustomUnauthorizedException;
import com.example.FoodDeliveryDemoApp.security.jwt.JwtService;
import com.example.FoodDeliveryDemoApp.security.token.Token;
import com.example.FoodDeliveryDemoApp.security.token.TokenType;
import com.example.FoodDeliveryDemoApp.security.token.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AdminRepository adminRepository;
    private final OwnerRepository ownerRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(AdminRepository adminRepository,
                                 OwnerRepository ownerRepository,
                                 CustomerRepository customerRepository,
                                 UserRepository userRepository,
                                 TokenRepository tokenRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 AuthenticationManager authenticationManager) {
        this.adminRepository = adminRepository;
        this.ownerRepository = ownerRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    private Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public AuthenticationResponse register(RegisterRequest request) {
        User user = createUser(request);
        User savedUser = saveUser(user);
        return createAuthenticationResponse(savedUser);
    }

    public AuthenticationResponse registerByAdmin(RegisterRequest request, Authentication authentication) {
        validateRequest(request, authentication);
        return register(request);
    }

    private AuthenticationResponse createAuthenticationResponse(User user) {
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void validateRequest(RegisterRequest request, Authentication authentication) {
        List<Role> userRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(Role::valueOf)
                .toList();

        if (request.getRole().equals(Role.ADMIN) && !userRoles.contains(Role.ADMIN)) {
            throw new CustomAccessDeniedException("Only admins can create new admins");
        }
    }

    private void validateUser(RegisterRequest request) {
        Optional<? extends User> existingUserByEmail = findByEmail(request.getEmail());
        Optional<? extends User> existingUserByUsername = findByUsername(request.getUsername());

        if (existingUserByEmail.isPresent()) {
            throw new CustomBadRequestException("User with the provided email already exists");
        }

        if (existingUserByUsername.isPresent()) {
            throw new CustomBadRequestException("User with the provided username already exists");
        }

        switch (request.getRole()) {
            case ADMIN -> {
                if (!userHasAdminRole()) {
                    throw new CustomAccessDeniedException("Regular users cannot create admins");
                }
            }
            case OWNER, CUSTOMER -> {
                // No specific checks for these roles
            }
            default -> throw new CustomBadRequestException("Invalid role: " + request.getRole());
        }

        UserDetailValidation.validateRegisterUser(request);
    }

    private boolean userHasAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(Role.ADMIN.name()));
    }

    private User createUser(RegisterRequest request) {
        validateUser(request);
        return User.builder()
                .username(request.getUsername())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .telephone(request.getTelephone())
                .role(request.getRole())
                .createdAt(Instant.now())
                .build();
    }

    private User saveUser(User user) {
        logger.info("User type: " + user.getRole().name() + " is registered with username " + user.getUsername() + ".");

        switch (user.getRole()) {
            case ADMIN -> {
                Admin admin = Admin.builder()
                        .level(3L)
                        .user(user)
                        .build();
                adminRepository.save(admin);
            }
            case OWNER -> {
                Owner owner = Owner.builder()
                        .approved(false)
                        .user(user)
                        .build();
                ownerRepository.save(owner);
            }
            case CUSTOMER -> {
                Customer customer = Customer.builder()
                        .user(user)
                        .build();
                customerRepository.save(customer);
            }
            default -> throw new CustomBadRequestException("Invalid role: " + user.getRole());
        }

        return userRepository.save(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Authentication authentication;

        UserDetailValidation.validateLogin(request);
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            logger.info("User: ´" + request.getUsername() + "´ logged in.");
        } catch (AuthenticationException e) {
            if (getUserByUsername(request.getUsername()) != null) {
                throw new CustomUnauthorizedException("Invalid password");
            }

            throw new CustomUnauthorizedException("Invalid username or password");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        var user = this.findByUsername(request.getUsername())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No such user with username: " + username));
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .user(user)
                .build();

        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokensByUser(user.getId());

        if (validUserTokens.isEmpty())
            return;

        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(validUserTokens);
    }

    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userName;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new CustomBadRequestException("Invalid or missing authorization header");
        }
        refreshToken = authHeader.substring(7);
        userName = jwtService.extractUsernameFromClaims(refreshToken);
        if (userName != null) {
            var user = this.findByUsername(userName)
                    .orElseThrow(() -> new CustomNotFoundException("No user with such username: " + userName));
            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                return authResponse;
            }
        }
        throw new CustomBadRequestException("Invalid refresh token");
    }

}
