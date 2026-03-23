package com.dev.thesis_management.common.utils;

import java.util.concurrent.ThreadLocalRandom;

public class OTPUtils {
    public static String generateOtp() {
        return String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 999999)
        );
    }
}
