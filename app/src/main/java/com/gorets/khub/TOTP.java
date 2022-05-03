package com.gorets.khub;

import java.security.MessageDigest;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TOTP {
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

    public static String getOTP (String secret) {
        Pattern pattern = Pattern.compile("\\d");

        String TC = String.valueOf(new Date().getTime() / 1000 / 30);
        String TOTP = hash("STREEBOG", TC + secret); //hash("SHA-512", TC + secret);

        Matcher matcher = pattern.matcher(TOTP);
        String result = "";
        while (matcher.find()) {
            result += matcher.group();
        }
        result = result.substring(result.length() - 6);

        for (int i = 3; i < result.length(); i+=3){
            if (result.length() - i >= i/3){
                String before = result.substring(0, i + (i/3 - 1));
                String after = result.substring(i + (i/3 - 1), result.length());
                result = before + " " + after;
            }
        }
        return result;
    }

    public static String hash(String algorithm, String srcStr) {
        if (Objects.equals(algorithm, "STREEBOG")) {
            return Streebog.getHash(srcStr);
        } else{
            try {
                MessageDigest md = MessageDigest.getInstance(algorithm);
                byte[] bytes = md.digest(srcStr.getBytes("utf-8"));
                return toHex(bytes);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String toHex(byte[] bytes) {
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }

}
