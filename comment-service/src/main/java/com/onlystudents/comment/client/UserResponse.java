package com.onlystudents.comment.client;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
}
