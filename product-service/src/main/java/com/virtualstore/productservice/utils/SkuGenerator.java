package com.virtualstore.productservice.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SkuGenerator {

    private static final SecureRandom RNG = new SecureRandom();
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int DEFAULT_RANDOM_LEN = 7; 

    /**
     * Public helper: returns SKU like SKU-ELE-APP-4F7Z9AC
     */
    public String generate(String category, String brand) {
        String cat = normalizeSegment(category);
        String br = normalizeSegment(brand);
        String rand = randomAlphanumeric(DEFAULT_RANDOM_LEN);
        return String.format("SKU-%s-%s-%s", cat, br, rand);
    }

    private String normalizeSegment(String input) {
        if (input == null || input.isBlank())
            return "GEN";
        // remove non-alphanumerics, uppercase
        String cleaned = input.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (cleaned.length() >= 3)
            return cleaned.substring(0, 3);
        // pad with X if <3
        return String.format("%-3s", cleaned).replace(' ', 'X');
    }

    private String randomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = RNG.nextInt(ALPHANUM.length());
            sb.append(ALPHANUM.charAt(idx));
        }
        return sb.toString();
    }
}