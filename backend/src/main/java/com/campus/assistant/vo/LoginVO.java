package com.campus.assistant.vo;

import lombok.Builder;
import lombok.Data;

/**
 * ?? VO?????????????????
 */
@Data
@Builder
public class LoginVO {

    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String roleCode;
    private Integer initialPassword;
}
