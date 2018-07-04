package com.da2win.xunwu.base;

import com.da2win.xunwu.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 * @author Darwin
 * @date 2018/7/4
 */
public class LoginUserUtil {
    public static User load() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal != null && principal instanceof User) {
            return (User) principal;
        }
        return null;
    }

    public static Long getLoginUserId() {
        User user = load();
        if (user == null) {
            return -1L;
        }
        return user.getId();
    }
}
