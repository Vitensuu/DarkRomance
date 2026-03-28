package com.team7.game1.server;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class DatabaseService {
    private Connection connection;

    public DatabaseService() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://localhost:3306/darkromance?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            String user = "darkuser";
            String password = "Sakira_3410";
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to database");
        }
    }

    public boolean register(String username) {
        String sql = "INSERT INTO users (username) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // дубликат username
            if (e.getErrorCode() == 1062) {
                System.out.println("Username already exists: " + username);
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    public boolean login(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void initPlayerStats(int userId) {
        String sql = "INSERT INTO player_stats (user_id) VALUES (?) ON DUPLICATE KEY UPDATE user_id=user_id";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void addScore(int userId, int points) {
        String sql = "UPDATE player_stats SET total_score = total_score + ? WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, points);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean unlockAchievement(int userId, int achievementId) {
        String sql = "UPDATE user_achievements SET is_unlocked = TRUE, unlocked_at = NOW() " +
            "WHERE user_id = ? AND achievement_id = ? AND is_unlocked = FALSE";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, achievementId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
