package com.healthqueue.utils;

import org.jspecify.annotations.Nullable;

import io.jsonwebtoken.Claims;

public class AuthContext {
    public static record UserCtx(String uuid, String type) {
    }

    public static UserCtx getUserCtxFromClaims(Claims claims) {
        if (claims == null) {
            return null;
        }

        final @Nullable String uuid = claims.get("uuid", String.class);
        final @Nullable String type = claims.get("type", String.class);

        if (uuid == null || type == null) {
            return null;
        }

        boolean isUUIDValid = Utils.isValidUUID(uuid);
        if (!isUUIDValid) {
            return null;
        }

        final UserCtx userCtx = new UserCtx(uuid, type);

        return userCtx;
    }
}