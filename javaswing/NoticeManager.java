package javaswing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NoticeManager {
    private static final String NOTICES_FILE = "notices.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static synchronized List<String[]> loadAllNotices() {
        List<String[]> notices = new ArrayList<>();
        File file = new File(NOTICES_FILE);

        if (!file.exists()) {
            saveNotice("📢 [EVENT] NSU Reunion 2026 Registration Open!", "Admin");
            saveNotice("📢 [SCHOLARSHIP] Higher Studies Abroad Seminar on Friday.", "Admin");
            saveNotice("📢 [NOTICE] Update your profile details before month end.", "Admin");
        }

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 2) {
                    notices.add(parts);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading notices file: " + e.getMessage());
        }
        return notices;
    }

    public static synchronized boolean saveNotice(String content, String postedBy) {
        try {
            File file = new File(NOTICES_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            String noticeId = UUID.randomUUID().toString().substring(0, 8);
            String time = LocalDateTime.now().format(DATE_FORMATTER);
            String csvLine = noticeId + "," + sanitize(content) + "," + sanitize(postedBy) + "," + time + System.lineSeparator();
            
            Files.write(file.toPath(), csvLine.getBytes(), StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving notice: " + e.getMessage());
            return false;
        }
    }

    public static synchronized void editNotice(String noticeId, String newContent) {
        File file = new File(NOTICES_FILE);
        if (!file.exists()) return;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            List<String> updatedLines = new ArrayList<>();
            for (String line : lines) {
                String[] parts = line.split(",", -1);
                if (parts.length > 0 && parts[0].equals(noticeId)) {
                    parts[1] = sanitize(newContent);
                    line = String.join(",", parts);
                }
                updatedLines.add(line);
            }
            Files.write(file.toPath(), updatedLines);
        } catch (IOException e) {
            System.err.println("Error editing notice: " + e.getMessage());
        }
    }

    public static synchronized void deleteNotice(String noticeId) {
        File file = new File(NOTICES_FILE);
        if (!file.exists()) return;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            List<String> remainingLines = new ArrayList<>();
            for (String line : lines) {
                String[] parts = line.split(",", -1);
                if (parts.length > 0 && parts[0].equals(noticeId)) {
                    continue;
                }
                remainingLines.add(line);
            }
            Files.write(file.toPath(), remainingLines);
        } catch (IOException e) {
            System.err.println("Error deleting notice: " + e.getMessage());
        }
    }

    private static String sanitize(String input) {
        return input == null ? "" : input.replace(",", ";").replace("\n", " ").trim();
    }
}