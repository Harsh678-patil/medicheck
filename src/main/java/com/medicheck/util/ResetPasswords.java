package com.medicheck.util;

import com.medicheck.config.DBConnection;
import java.sql.*;

/**
 * One-time utility to reset all seeded user passwords with correct BCrypt hashes.
 * Run with: mvn compile exec:java -Dexec.mainClass=com.medicheck.util.ResetPasswords
 */
public class ResetPasswords {

    public static void main(String[] args) throws Exception {
        System.out.println("=== MediCheck Password Reset Utility ===\n");

        // Generate correct BCrypt hashes
        String adminHash  = PasswordUtil.hash("Admin@123");
        String doctorHash = PasswordUtil.hash("Doctor@123");
        String pharmaHash = PasswordUtil.hash("Pharma@123");

        System.out.println("Generated hashes:");
        System.out.println("  admin       -> " + adminHash);
        System.out.println("  dr.krishnan -> " + doctorHash);
        System.out.println("  pharmrajeev -> " + pharmaHash);

        // Update directly in DB
        DBConnection db = DBConnection.getInstance();
        try (Connection conn = db.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET password_hash = ? WHERE username = ?"
            );

            ps.setString(1, adminHash);  ps.setString(2, "admin");
            int r1 = ps.executeUpdate();

            ps.setString(1, doctorHash); ps.setString(2, "dr.krishnan");
            int r2 = ps.executeUpdate();

            ps.setString(1, pharmaHash); ps.setString(2, "pharmrajeev");
            int r3 = ps.executeUpdate();

            System.out.println("\n✅ Passwords updated in database:");
            System.out.println("   admin        : " + r1 + " row(s) updated");
            System.out.println("   dr.krishnan  : " + r2 + " row(s) updated");
            System.out.println("   pharmrajeev  : " + r3 + " row(s) updated");
            System.out.println("\n✅ You can now log in with:");
            System.out.println("   Username: admin      Password: Admin@123");
            System.out.println("   Username: dr.krishnan Password: Doctor@123");
            System.out.println("   Username: pharmrajeev Password: Pharma@123");
        } catch (Exception e) {
            System.err.println("❌ DB Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            db.destroyPool();
        }
    }
}
