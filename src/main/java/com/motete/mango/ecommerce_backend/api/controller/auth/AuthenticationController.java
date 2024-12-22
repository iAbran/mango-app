package com.motete.mango.ecommerce_backend.api.controller.auth;

import com.motete.mango.ecommerce_backend.api.model.LoginBody;
import com.motete.mango.ecommerce_backend.api.model.LoginResponse;
import com.motete.mango.ecommerce_backend.api.model.RegistrationBody;
import com.motete.mango.ecommerce_backend.exception.EmailFailureException;
import com.motete.mango.ecommerce_backend.exception.UserAlreadyExistsException;
import com.motete.mango.ecommerce_backend.exception.UserNotVerifiedException;
import com.motete.mango.ecommerce_backend.model.LocalUser;
import com.motete.mango.ecommerce_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody List<RegistrationBody> registrationBodies) {
        for (RegistrationBody registrationBody : registrationBodies) {
            try {
                userService.registerUser(registrationBody);
            } catch (UserAlreadyExistsException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists: " + registrationBody.getUsername());
            } catch (EmailFailureException ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("User successfully registered");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginBody loginBody) {
        String jwt;
        try {
            jwt = userService.loginUser(loginBody);
        } catch (UserNotVerifiedException ex) {
            LoginResponse response = new LoginResponse();
            response.setSuccess(false);
            String reason = "USER_NOT_VERIFIED";
            if (ex.isNewEmailSent()) {
                reason += "_EMAIL_RESENT";
            }
            response.setFailureReason(reason);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (EmailFailureException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (jwt == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        } else {
            LoginResponse response = new LoginResponse();
            response.setJwt(jwt);
            response.setSuccess(true);
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        if (userService.verifyUser(token)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/me")
    public LocalUser getLoggedInUser(@AuthenticationPrincipal LocalUser user) {
        return user;
    }

    @GetMapping("/users")
    public List<LocalUser> getAllUsers() {
        return userService.getAllUsers();
    }
}
