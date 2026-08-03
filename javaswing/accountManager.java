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
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class accountManager {
    private static final String CSV_FILE = "alumni_users.csv";
    private static final String HEADER = "Username,Password,FirstName,LastName,Mobile,Email,SemesterBatch,NSUID,Department,Major,Profession,Designation,CompanyName,Country,SecurityQuestion,SecurityAnswer,ProfilePhoto";

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
                file.createNewFile();
                Files.write(file.toPath(), (HEADER + System.lineSeparator()).getBytes());
                registerUser("Raiyan", "1234", "Raiyan", "Ahmed", "+8801700000000", "raiyan@northsouth.edu",
                        "Spring 2022", "2210000000", "ECE", "CSE", "Software Engineer", "Junior Developer", "Tech Corp",
                        "Bangladesh", "What is your primary school name?", "Dhaka College", "");
            } catch (IOException e) {
                System.err.println("Error creating CSV storage: " + e.getMessage());
            }
        }
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
        }
        return null;
    }

    public static synchronized boolean registerUser(String username, String password, String firstName, String lastName,
            String mobile, String email, String semesterBatch, String nsuId, String department, String major,
            String profession, String designation, String companyName, String country, String securityQuestion,
            String securityAnswer, String profilePhotoPath) {

        if (checkDuplicates(username, nsuId, mobile, email) != null) return false;

        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) return false;

        try {
            File file = new File(CSV_FILE);
            String csvLine = String.join(",", clean(username), clean(hashedPassword), clean(firstName), clean(lastName),
                    clean(mobile), clean(email), clean(semesterBatch), clean(nsuId), clean(department), clean(major),
                    clean(profession), clean(designation), clean(companyName), clean(country), clean(securityQuestion),
                    clean(securityAnswer.toLowerCase()), clean(profilePhotoPath)) + System.lineSeparator();

            Files.write(file.toPath(), csvLine.getBytes(), StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
            return false;
        }
    }

    public static synchronized boolean resetPasswordWithSecurityAnswer(String username, String answer, String newPassword) {
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
        }
        return false;
    }

    public static boolean isAccountLocked(String username) {
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
                    if (!username.isEmpty() && parts[0].trim().equalsIgnoreCase(username.trim()))
                        return "Username '" + username + "' is already taken!";
                    if (!nsuId.isEmpty() && parts[7].trim().equalsIgnoreCase(nsuId.trim()))
                        return "An account with NSU ID '" + nsuId + "' already exists!";
                    if (!mobile.isEmpty() && parts[4].trim().equals(mobile.trim()))
                        return "Mobile number '" + mobile + "' is already registered!";
                    if (!email.isEmpty() && parts[5].trim().equalsIgnoreCase(email.trim()))
                        return "Email address '" + email + "' is already registered!";
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking duplicates: " + e.getMessage());
        }
        return null;
    }

    public static boolean authenticate(String username, String rawPassword) {
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
            e.printStackTrace();
            return null;
        }
    }

    private static boolean verifyPassword(String password, String storedHash) {
        String[] parts = storedHash.split(":");
        if (parts.length != 2) return false;

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

        try {
            byte[] actualHash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            int diff = expectedHash.length ^ actualHash.length;
            for (int i = 0; i < expectedHash.length && i < actualHash.length; i++) {
                diff |= expectedHash[i] ^ actualHash[i];
            }
            return diff == 0;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
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