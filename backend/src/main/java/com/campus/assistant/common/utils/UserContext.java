package com.campus.assistant.common.utils;

import com.campus.assistant.entity.User;

public final class UserContext {

    private static final ThreadLocal<User> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(User user) {
        USER_HOLDER.set(user);
    }

    public static User get() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        User user = get();
        return user == null ? null : user.getId();
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
