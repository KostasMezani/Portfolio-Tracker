package com.portfoliotracker.util;

public class ValidationUtils {

    public static boolean isNotEmpty(String value){
        return value != null && !value.isBlank();
    }

    public static boolean isValidPassword(String password){
        return password!= null && password.length() >= 6;
    }

    public static boolean isValidEmail(String email){
       return email != null && email.contains("@");
    }

    public static boolean passwordsMatch(String p1, String p2) {
       return p1 != null && p1.equals(p2);
    }
}
