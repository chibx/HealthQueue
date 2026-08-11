package com.healthqueue.utils;

import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.github.f4b6a3.uuid.UuidCreator;

import io.jsonwebtoken.Claims;

public class AuthContext {
    public static record UserCtx(UUID uuid, String type) {
    }

    public static @Nullable UserCtx getUserCtxFromClaims(Claims claims) {
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

        final UserCtx userCtx = new UserCtx(UuidCreator.fromString(uuid), type);

        return userCtx;
    }
}