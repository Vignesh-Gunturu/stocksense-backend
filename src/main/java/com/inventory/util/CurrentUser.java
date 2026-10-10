package com.inventory.util;

import com.inventory.security.UserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUser {

    public static UserPrincipal get() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static Long businessId() {
        return get().getBusinessId();
    }

    public static Long userId() {
        return get().getId();
    }
}
