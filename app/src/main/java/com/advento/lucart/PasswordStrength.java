package com.advento.lucart;

public class PasswordStrength {

    public static boolean isUpperCase(String password) {
        return password.matches(".*[A-Z].*");
    }

    public static boolean isLowerCase(String password) {
        return password.matches(".*[a-z].*");
    }

    public static boolean isNumber(String password) {
        return password.matches(".*\\d.*");
    }

    public static boolean isSpecialCharacter(String password) {
        return password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
    }

    public static boolean isValidLength(String password) {
        return password.length() >= 8;
    }

    public static int getPasswordStrength(String password) {
        int strength = 0;

        if (isUpperCase(password)) strength++;
        if (isLowerCase(password)) strength++;
        if (isNumber(password)) strength++;
        if (isSpecialCharacter(password)) strength++;
        if (isValidLength(password)) strength++;

        return strength; // Returns a value from 0 to 5
    }
}
