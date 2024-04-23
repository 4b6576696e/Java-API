package com.API.JWT.dto.response;

import com.API.JWT.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    private String name;
    private String email;
    private Set<Role> roles;
}
