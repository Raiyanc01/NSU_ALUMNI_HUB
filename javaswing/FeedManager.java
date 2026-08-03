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

public class FeedManager {
    private static final String POSTS_FILE = "posts.csv";
    private static final String COMMENTS_FILE = "comments.csv";
    private static final String LIKES_FILE = "likes.csv";
    
    private static final DateTimeFormatter POST_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter COMMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    public static synchronized List<String[]> loadAllPosts() {
        List<String[]> posts = new ArrayList<>();
        File file = new File(POSTS_FILE);
        if (!file.exists()) return posts;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    posts.add(parts);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading posts: " + e.getMessage());
        }
        return posts;
    }

    public static synchronized boolean savePost(String author, String content) {
        try {
            File file = new File(POSTS_FILE);
            if (!file.exists()) file.createNewFile();
            
            String postId = UUID.randomUUID().toString().substring(0, 8);
            String time = LocalDateTime.now().format(POST_DATE_FORMAT);
            String csvLine = postId + "," + sanitize(author) + "," + sanitize(content) + ",0," + time + System.lineSeparator();
            
            Files.write(file.toPath(), csvLine.getBytes(), StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving post: " + e.getMessage());
            return false;
        }
    }

    public static synchronized void editPost(String postId, String newContent) {
        modifyCsvFile(POSTS_FILE, 0, postId, row -> {
            row[2] = sanitize(newContent);
            return row;
        });
    }

    public static synchronized void deletePost(String postId) {
        deleteFromCsv(POSTS_FILE, 0, postId);
        deleteFromCsv(COMMENTS_FILE, 0, postId);
    }

    public static synchronized boolean hasUserLikedPost(String postId, String username) {
        File file = new File(LIKES_FILE);
        if (!file.exists()) return false;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(postId) && parts[1].equals(username)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking post likes: " + e.getMessage());
        }
        return false;
    }

    public static synchronized void toggleLikePost(String postId, String username, boolean shouldLike) {
        if (shouldLike) {
            try {
                File file = new File(LIKES_FILE);
                if (!file.exists()) file.createNewFile();
                String line = postId + "," + username + System.lineSeparator();
                Files.write(file.toPath(), line.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Error liking post: " + e.getMessage());
            }
        } else {
            deleteFromCsv(LIKES_FILE, 0, postId);
        }
    }

    public static synchronized List<String[]> loadCommentsForPost(String postId) {
        List<String[]> comments = new ArrayList<>();
        File file = new File(COMMENTS_FILE);
        if (!file.exists()) return comments;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 4 && parts[0].equals(postId)) {
                    comments.add(new String[] {
                        parts[1], parts[2], parts[3],
                        parts.length > 4 ? parts[4] : "null",
                        parts.length > 5 ? parts[5] : ""
                    });
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading comments: " + e.getMessage());
        }
        return comments;
    }

    public static synchronized void saveComment(String postId, String author, String text, String parentId) {
        try {
            File file = new File(COMMENTS_FILE);
            if (!file.exists()) file.createNewFile();
            
            String commentId = UUID.randomUUID().toString().substring(0, 8);
            String time = LocalDateTime.now().format(COMMENT_DATE_FORMAT);
            String line = postId + "," + commentId + "," + sanitize(author) + "," + sanitize(text) + "," + parentId + "," + time + System.lineSeparator();
            
            Files.write(file.toPath(), line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error saving comment: " + e.getMessage());
        }
    }

    public static synchronized void editComment(String commentId, String newText) {
        modifyCsvFile(COMMENTS_FILE, 1, commentId, row -> {
            row[3] = sanitize(newText);
            return row;
        });
    }

    public static synchronized void deleteComment(String commentId) {
        deleteFromCsv(COMMENTS_FILE, 1, commentId);
    }

    @FunctionalInterface
    private interface RowModifier {
        String[] modify(String[] row);
    }

    private static void modifyCsvFile(String filePath, int keyIndex, String targetKey, RowModifier modifier) {
        File file = new File(filePath);
        if (!file.exists()) return;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            List<String> updated = new ArrayList<>();
            for (String line : lines) {
                String[] parts = line.split(",", -1);
                if (parts.length > keyIndex && parts[keyIndex].equals(targetKey)) {
                    parts = modifier.modify(parts);
                    line = String.join(",", parts);
                }
                updated.add(line);
            }
            Files.write(file.toPath(), updated);
        } catch (IOException e) {
            System.err.println("Error modifying file " + filePath + ": " + e.getMessage());
        }
    }

    private static void deleteFromCsv(String filePath, int keyIndex, String targetKey) {
        File file = new File(filePath);
        if (!file.exists()) return;

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            List<String> remaining = new ArrayList<>();
            for (String line : lines) {
                String[] parts = line.split(",", -1);
                if (parts.length > keyIndex && parts[keyIndex].equals(targetKey)) {
                    continue;
                }
                remaining.add(line);
            }
            Files.write(file.toPath(), remaining);
        } catch (IOException e) {
            System.err.println("Error deleting from file " + filePath + ": " + e.getMessage());
        }
    }

    private static String sanitize(String text) {
        return text == null ? "" : text.replace(",", ";").replace("\n", " ").trim();
    }
}