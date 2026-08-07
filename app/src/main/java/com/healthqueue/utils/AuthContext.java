package com.healthqueue.utils;

public class AuthContext {
    public static enum CtxType {
        Patient("patient"),
        Organization("organization");

        private final String text;

        CtxType(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    public static record UserCtx(String uuid, CtxType type) {
    }
}