package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection.java
 * -----------------
 * Kelas konfigurasi koneksi ke database MySQL.
 * Menggunakan pola Singleton agar hanya ada satu koneksi aktif.
 *
 * Konfigurasi:
 *   Host     : localhost
 *   Port     : 3306
 *   Database : spk_ayambakar
 *   User     : root
 *   Password : (kosong - default XAMPP)
 *
 * Layer    : Config / Infrastructure
 * Package  : config
 */
public class DBConnection {

    // ── Konstanta koneksi ─────────────────────────────────────
    private static final String DRIVER   = "com.mysql.cj.jdbc.Driver";
    private static final String URL      = "jdbc:mysql://localhost:3306/spk_ayambakar"
                                         + "?useSSL=false&serverTimezone=Asia/Jakarta"
                                         + "&characterEncoding=UTF-8";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";          // ganti sesuai password MySQL Anda

    // ── Singleton instance ────────────────────────────────────
    private static DBConnection instance;
    private Connection connection;

    // ── Constructor privat ────────────────────────────────────
    private DBConnection() {
        try {
            Class.forName(DRIVER);
            this.connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("[DBConnection] Koneksi berhasil ke " + URL);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("[DBConnection] Driver tidak ditemukan: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("[DBConnection] Gagal konek ke database: " + e.getMessage(), e);
        }
    }

    /**
     * Mendapatkan instance tunggal DBConnection (Singleton).
     * Jika koneksi sudah tutup atau null, buat koneksi baru.
     */
    public static DBConnection getInstance() {
        try {
            if (instance == null || instance.connection.isClosed()) {
                instance = new DBConnection();
            }
        } catch (SQLException e) {
            instance = new DBConnection();
        }
        return instance;
    }

    /**
     * Mendapatkan objek java.sql.Connection aktif.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Menutup koneksi database secara manual.
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DBConnection] Koneksi ditutup.");
            }
        } catch (SQLException e) {
            System.err.println("[DBConnection] Gagal menutup koneksi: " + e.getMessage());
        }
    }
}
