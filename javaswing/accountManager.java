package javaswing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class accountManager {
    private static final String CSV_FILE = "alumni_users.csv";
    private static final String HEADER = "Username,Password,FirstName,LastName,Mobile,Email,SemesterBatch,NSUID,Department,Major,Profession,Designation,CompanyName,Country,SecurityQuestion,SecurityAnswer,ProfilePhoto";

    // Password policy: min 8 chars, at least 1 upper, 1 lower, 1 digit, 1 special character
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^a-zA-Z0-9]");
    
    // Mobile number policy: exactly 11 digits
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[0-9]{11}$");

    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MS = 60 * 60 * 1000L;
    private static final Map<String, AccountLockoutInfo> lockoutTracker = new ConcurrentHashMap<>();

    private static class AccountLockoutInfo {
        int failedAttempts = 0;
        long lockTime = 0;
    }

    public static synchronized void initStorage() {
        File file = new File(CSV_FILE);
        if (!file.exists()) {
            try {
                File parent = file.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }
                file.createNewFile();
                Files.write(file.toPath(), (HEADER + System.lineSeparator()).getBytes());
                registerUser("Raiyan", "Passw0rd!", "Raiyan", "Choudhury", "01611177123", "raiyan.choudhury.253@northsouth.edu",
                        "FALL 2025", "2531141042", "ECE", "CSE", "Software Engineer", "Junior Developer", "RC Tech Crop.",
                        "Bangladesh", "What is your primary school name?", "Gregory", "");
            } catch (IOException e) {
                System.err.println("Error creating CSV storage: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Unexpected error initializing storage: " + e.getMessage());
            }
        }
    }

    /**
     * Validates that the phone number contains exactly 11 digits.
     */
    public static String validateMobileNumber(String mobile) {
        if (mobile == null || mobile.trim().isEmpty()) {
            return "Mobile number is required.";
        }
        if (!MOBILE_PATTERN.matcher(mobile.trim()).matches()) {
            return "Mobile number must be exactly 11 digits.";
        }
        return null;
    }

    /**
     * Validates password strength.
     */
    public static String validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required.";
        }
        if (password.length() < PASSWORD_MIN_LENGTH) {
            return "Password must be at least " + PASSWORD_MIN_LENGTH + " characters long.";
        }
        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            return "Password must contain at least one lowercase letter.";
        }
        if (!DIGIT_PATTERN.matcher(password).find()) {
            return "Password must contain at least one number.";
        }
        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            return "Password must contain at least one special character (e.g. !@#$%).";
        }
        return null;
    }

    public static boolean isPasswordStrong(String password) {
        return validatePasswordStrength(password) == null;
    }

    public static String[] getUserProfile(String username) {
        File file = new File(CSV_FILE);
        if (!file.exists() || username == null) return null;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",", -1);
                if (parts.length >= 14 && parts[0].trim().equalsIgnoreCase(username.trim())) {
                    return parts;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV profile: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error fetching profile: " + e.getMessage());
        }
        return null;
    }

    public static synchronized boolean updateProfilePhoto(String username, String photoPath) {
        File file = new File(CSV_FILE);
        if (!file.exists()) return false;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            List<String> updatedLines = new ArrayList<>();
            boolean updated = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (i == 0) {
                    updatedLines.add(line);
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length >= 14 && parts[0].trim().equalsIgnoreCase(username.trim())) {
                    String[] newParts = new String[17];
                    System.arraycopy(parts, 0, newParts, 0, Math.min(parts.length, 17));
                    for (int j = parts.length; j < 17; j++) newParts[j] = "";
                    newParts[16] = clean(photoPath);
                    line = String.join(",", newParts);
                    updated = true;
                }
                updatedLines.add(line);
            }

            if (updated) {
                Files.write(file.toPath(), updatedLines);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error updating profile photo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error updating profile photo: " + e.getMessage());
        }
        return false;
    }

    public static String getSecurityQuestion(String username) {
        File file = new File(CSV_FILE);
        if (!file.exists() || username == null) return null;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",", -1);
                if (parts.length >= 15 && parts[0].trim().equalsIgnoreCase(username.trim())) {
                    return parts[14].trim();
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading security question: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error reading security question: " + e.getMessage());
        }
        return null;
    }

    public static synchronized boolean registerUser(String username, String password, String firstName, String lastName,
            String mobile, String email, String semesterBatch, String nsuId, String department, String major,
            String profession, String designation, String companyName, String country, String securityQuestion,
            String securityAnswer, String profilePhotoPath) {

        try {
            if (username == null || password == null || firstName == null || securityAnswer == null) {
                throw new IllegalArgumentException("Required registration parameters cannot be null.");
            }

            if (validateMobileNumber(mobile) != null) return false;

            if (checkDuplicates(username, nsuId, mobile, email) != null) return false;

            if (!isPasswordStrong(password)) return false;

            String hashedPassword = hashPassword(password);
            if (hashedPassword == null) {
                throw new SecurityException("Failed to safely generate password hash.");
            }

            File file = new File(CSV_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            String csvLine = String.join(",", clean(username), clean(hashedPassword), clean(firstName), clean(lastName),
                    clean(mobile), clean(email), clean(semesterBatch), clean(nsuId), clean(department), clean(major),
                    clean(profession), clean(designation), clean(companyName), clean(country), clean(securityQuestion),
                    clean(securityAnswer.toLowerCase()), clean(profilePhotoPath)) + System.lineSeparator();

            Files.write(file.toPath(), csvLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;

        } catch (IOException e) {
            System.err.println("I/O Error writing user to CSV: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException | SecurityException e) {
            System.err.println("Registration validation/security error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected registration exception: " + e.getMessage());
            return false;
        }
    }

    public static synchronized boolean resetPasswordWithSecurityAnswer(String username, String answer, String newPassword) {
        File file = new File(CSV_FILE);
        if (!file.exists()) return false;
        if (!isPasswordStrong(newPassword)) return false;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            List<String> updatedLines = new ArrayList<>();
            boolean updated = false;

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (i == 0) {
                    updatedLines.add(line);
                    continue;
                }

                String[] parts = line.split(",", -1);
                if (parts.length >= 16) {
                    if (parts[0].trim().equalsIgnoreCase(username.trim()) && parts[15].trim().equalsIgnoreCase(answer.trim())) {
                        parts[1] = clean(hashPassword(newPassword));
                        line = String.join(",", parts);
                        updated = true;
                    }
                }
                updatedLines.add(line);
            }

            if (updated) {
                Files.write(file.toPath(), updatedLines);
                lockoutTracker.remove(username);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error resetting password: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during password reset: " + e.getMessage());
        }
        return false;
    }

    public static boolean isAccountLocked(String username) {
        if (username == null) return false;
        AccountLockoutInfo info = lockoutTracker.get(username);
        if (info == null) return false;

        if (info.failedAttempts >= MAX_FAILED_ATTEMPTS) {
            if (System.currentTimeMillis() - info.lockTime < LOCK_DURATION_MS) {
                return true;
            } else {
                lockoutTracker.remove(username);
                return false;
            }
        }
        return false;
    }

    public static long getRemainingLockoutMinutes(String username) {
        if (username == null) return 0;
        AccountLockoutInfo info = lockoutTracker.get(username);
        if (info == null) return 0;

        long elapsed = System.currentTimeMillis() - info.lockTime;
        return Math.max(0, (LOCK_DURATION_MS - elapsed) / (1000 * 60));
    }

    public static String checkDuplicates(String username, String nsuId, String mobile, String email) {
        File file = new File(CSV_FILE);
        if (!file.exists()) return null;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",", -1);
                if (parts.length >= 14) {
                    if (username != null && !username.isEmpty() && parts[0].trim().equalsIgnoreCase(username.trim()))
                        return "Username '" + username + "' is already taken!";
                    if (nsuId != null && !nsuId.isEmpty() && parts[7].trim().equalsIgnoreCase(nsuId.trim()))
                        return "An account with NSU ID '" + nsuId + "' already exists!";
                    if (mobile != null && !mobile.isEmpty() && parts[4].trim().equals(mobile.trim()))
                        return "Mobile number '" + mobile + "' is already registered!";
                    if (email != null && !email.isEmpty() && parts[5].trim().equalsIgnoreCase(email.trim()))
                        return "Email address '" + email + "' is already registered!";
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking duplicates: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during duplicate check: " + e.getMessage());
        }
        return null;
    }

    public static boolean authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) return false;
        if (isAccountLocked(username)) return false;

        Map<String, String> users = loadUserPasswords();
        if (!users.containsKey(username)) return false;

        boolean isValid = verifyPassword(rawPassword, users.get(username));
        AccountLockoutInfo info = lockoutTracker.computeIfAbsent(username, k -> new AccountLockoutInfo());

        if (isValid) {
            lockoutTracker.remove(username);
            return true;
        } else {
            info.failedAttempts++;
            if (info.failedAttempts >= MAX_FAILED_ATTEMPTS) {
                info.lockTime = System.currentTimeMillis();
            }
            return false;
        }
    }

    private static Map<String, String> loadUserPasswords() {
        Map<String, String> users = new HashMap<>();
        File file = new File(CSV_FILE);
        if (!file.exists()) return users;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",", -1);
                if (parts.length >= 2) {
                    users.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading user credentials: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error loading user credentials: " + e.getMessage());
        }
        return users;
    }

    private static String clean(String field) {
        return field == null ? "" : field.replace(",", " ").trim();
    }

    private static String hashPassword(String password) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error during password hashing: " + e.getMessage());
            return null;
        }
    }

    private static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) return false;
        String[] parts = storedHash.split(":");
        if (parts.length != 2) return false;

        try {
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            byte[] actualHash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            int diff = expectedHash.length ^ actualHash.length;
            for (int i = 0; i < expectedHash.length && i < actualHash.length; i++) {
                diff |= expectedHash[i] ^ actualHash[i];
            }
            return diff == 0;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println("Base64 decoding error during password verification: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error during password verification: " + e.getMessage());
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }
}