package com.campus.assistant.vo;

import com.campus.assistant.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String studentNo;
    private String college;
    private String roleCode;
    private Integer status;
    private Integer initialPassword;

    public static UserVO from(User user) {
        if (user == null) {
            return null;
        }
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .studentNo(user.getStudentNo())
                .college(user.getCollege())
                .roleCode(user.getRoleCode())
                .status(user.getStatus())
                .initialPassword(user.getInitialPassword())
                .build();
    }
}
