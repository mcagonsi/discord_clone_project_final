package discord_rest_api.utils;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

public class InviteCodeGenerator {
    // URL-safe alphanumeric characters
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Set<String> USED_CODES = new HashSet<>();

 

    public static String generateCode(int length ) {
        if (length < 4) throw new IllegalArgumentException("Code length must be at least 4");

        String code;
        do {
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
            }
            code = sb.toString();
        } while (USED_CODES.contains(code)); // Avoid duplicates in current runtime

        USED_CODES.add(code);
        return code;
    }

    // public static void main(String[] args) {
    //     // Example: Generate 10 invitation codes
    //     for (int i = 0; i < 10; i++) {
    //         System.out.println(generateCode(8)); // 8-char code like Discord
    //     }
    // }
}
