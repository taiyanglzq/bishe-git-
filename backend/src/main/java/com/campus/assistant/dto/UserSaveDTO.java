package com.campus.assistant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserSaveDTO {

    private Long id;

    @NotBlank(message = "登录号不能为空")
    private String loginNo;

    /**
     * 兼容旧前端字段。实际保存时优先使用 loginNo。
     */
    private String username;

    private String password;

    @NotBlank(message = "姓名不能为空")
    private String realName;

    private String studentNo;
    private String college;

    @NotBlank(message = "角色不能为空")
    private String roleCode;

    private Integer status;
}
