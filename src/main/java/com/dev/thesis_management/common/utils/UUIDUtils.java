package com.dev.thesis_management.common.utils;

import java.util.UUID;

public class UUIDUtils {
    public static UUID parseUUID(String value) {
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            return null;
        }
    }
}
