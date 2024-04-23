package com.API.JWT.controller;

import com.API.JWT.advice.InvalidRefreshToken;
import com.API.JWT.dto.request.LoginRequest;
import com.API.JWT.dto.request.SignupRequest;
import com.API.JWT.dto.response.MessageResponse;
import com.API.JWT.dto.response.UserInfoResponse;
import com.API.JWT.model.ERole;
import com.API.JWT.model.RefreshToken;
import com.API.JWT.model.Role;
import com.API.JWT.model.User;
import com.API.JWT.repository.RefreshTokenRepo;
import com.API.JWT.repository.RoleRepo;
import com.API.JWT.repository.UserRepo;
import com.API.JWT.security.jwt.JwtUtils;
import com.API.JWT.security.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @PostMapping("/sign-up")
    private ResponseEntity<?> registerUser(@RequestBody @Valid SignupRequest signupRequest) {
        if (userRepo.existsByEmail(signupRequest.getEmail()))
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email already Exist."));

        User newUser = User.builder()
                .name(signupRequest.getName())
                .email(signupRequest.getEmail())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .build();

        Set<String> strRoles = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = Role.builder().name(ERole.USER).build();
            newUser.setRoles(Set.of(userRole));
        } else {
            Set<Role> finalRoles = roles;
            strRoles.forEach(role -> {
                switch (role) {
                    case "ADMIN":
                        Role adminRole = Role.builder().name(ERole.ADMIN).build();
                        finalRoles.add(adminRole);

                    case "USER":
                        Role userRole = Role.builder().name(ERole.USER).build();
                        finalRoles.add(userRole);
                }
            });
        }

        roles = roles.stream().map(roleRepo::save).collect(Collectors.toSet());

        newUser.setRoles(roles);
        userRepo.save(newUser);

        return ResponseEntity.ok(new MessageResponse("User registered Successfully"));
    }

    @PostMapping("/sign-in")
    private ResponseEntity<?> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User principal = (User) authentication.getPrincipal();

        ResponseCookie jwtCookie = jwtUtils.generateJWTCookie(principal);

        RefreshToken refreshToken = refreshTokenService.generateRefreshToken(principal.getId());

        ResponseCookie jwtRefreshCookie = jwtUtils.generateJWTRefreshCookie(refreshToken.getToken());

//        Set<String> roles = principal.getRoles().stream().map(Role::toString).collect(Collectors.toSet());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(HttpHeaders.SET_COOKIE, jwtRefreshCookie.toString())
                .body(UserInfoResponse.builder()
                        .name(principal.getName())
                        .email(principal.getEmail())
                        .roles(principal.getRoles())
                        .build());
    }

    @PostMapping("/refreshToken")
    private ResponseEntity<?> getNewAccessToken(HttpServletRequest request) {
        String token = jwtUtils.extractRefreshTokenFromCookie(request);

        if (token != null && !token.isEmpty()) {
            RefreshToken refreshToken = refreshTokenService.findByToken(token).get();
            User user = refreshToken.getUser();
            if (refreshTokenService.isValidToken(refreshToken)) {
                ResponseCookie cookie = jwtUtils.generateJWTCookie(user);

                UserInfoResponse userResponse = UserInfoResponse.builder()
                        .name(user.getName())
                        .email(user.getEmail())
                        .roles(user.getRoles())
                        .build();

                return ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, cookie.toString())
                        .body(userResponse);
            }
        }

        throw new InvalidRefreshToken("invalid refresh token");
    }
}
