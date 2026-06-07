package com.unispace.controller;

import com.unispace.dto.request.ProfileUpdateRequest;
import com.unispace.dto.response.UserResponse;
import com.unispace.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Tag(name = "User", description = "내 프로필 조회/수정")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "내 프로필 조회")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Principal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getName()));
    }

    @Operation(summary = "프로필 수정 (소속 등)")
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(Principal principal,
                                                 @Valid @RequestBody ProfileUpdateRequest req) {
        return ResponseEntity.ok(userService.updateProfile(principal.getName(), req));
    }
}
