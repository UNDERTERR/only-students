package com.onlystudents.common.event.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoUpdatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String nickname;
    private String avatar;
    private String bio;
}
