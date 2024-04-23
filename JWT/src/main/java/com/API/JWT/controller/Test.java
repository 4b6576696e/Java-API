package com.API.JWT.controller;

import com.API.JWT.dto.response.UserInfoResponse;
import com.API.JWT.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class Test {

    @Autowired
    private UserRepo userRepo;

    @GetMapping("/all")
    private ResponseEntity<List<UserInfoResponse>> getAllUsers() {
        System.out.println("Hello world");

        List<UserInfoResponse> userList = userRepo.findAll().stream().map((user) -> {
            return UserInfoResponse.builder()
                    .name(user.getName())
                    .email(user.getEmail())
                    .build();
                })
                .toList();

        return ResponseEntity.ok().body(userList);
    }
}
