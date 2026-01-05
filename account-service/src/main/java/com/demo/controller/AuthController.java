package com.demo.controller;

import com.demo.payload.request.LoginRequest;
import com.demo.payload.request.SignupRequest;
import com.demo.service.AuthService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * @author Vito Nguyen (<a href="https://github.com/cuongnh28">...</a>)
 */

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth-controller", description = "Authentication endpoints for sign-in and sign-up")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/signin")
    @Operation(summary = "Authenticate user", description = "Validate credentials and return JWT plus user info")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tạo quiz thành công"),
            @ApiResponse(responseCode = "400", description = "Request không hợp lệ")
    })
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.authenticateUser(loginRequest));
    }

    @PostMapping("/signup")
    @Operation(summary = "Register new user", description = "Create a new account and assign roles")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        return authService.registerUser(signUpRequest);
    }

}
