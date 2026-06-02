package com.campus.assistant.common.utils;

import com.campus.assistant.common.exception.BusinessException;
import com.campus.assistant.entity.User;

import java.util.Set;

public final class RoleUtils {

    private RoleUtils() {
    }

    public static boolean hasAny(String... roleCodes) {
        User user = UserContext.get();
        if (user == null) {
            return false;
        }
        return Set.of(roleCodes).contains(user.getRoleCode());
    }

    public static void requireAny(String... roleCodes) {
        if (!hasAny(roleCodes)) {
            throw new BusinessException(403, "无权限执行该操作");
        }
    }
}
