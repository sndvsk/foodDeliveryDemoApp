package com.example.FoodDeliveryDemoApp.component.userItems.user.controller;

import com.example.FoodDeliveryDemoApp.component.userItems.user.dto.AuthenticationRequest;
import com.example.FoodDeliveryDemoApp.component.userItems.user.dto.AuthenticationResponse;
import com.example.FoodDeliveryDemoApp.component.userItems.user.dto.RegisterRequest;
import com.example.FoodDeliveryDemoApp.security.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@CrossOrigin
@RestController
@RequestMapping("/api/v2/auth")
public class AuthenticationController {

    private final AuthenticationService service;

    public AuthenticationController(AuthenticationService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<AuthenticationResponse> registerAdmin(@RequestBody RegisterRequest request,
                                                           Authentication authentication) {
        return ResponseEntity.ok(service.registerByAdmin(request, authentication));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = service.authenticate(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        AuthenticationResponse res = service.refreshToken(request, response);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("/logout")
    public void fakeLogout() {
        throw new IllegalStateException("This method shouldn't be called. It's implemented by Spring Security filters.");
    }

}
