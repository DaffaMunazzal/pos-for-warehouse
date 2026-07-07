/*
 * Sistem Manajemen Gudang Koperasi
 * Koneksi.java - Database connection utility
 * Package: stok_gudang
 *
 * Pastikan file MySQL Connector/J (mysql-connector-j-x.x.x.jar)
 * sudah ditambahkan ke Libraries project NetBeans.
 */
package stok_gudang;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class untuk mengelola koneksi ke database MySQL.
 * Gunakan Koneksi.getKoneksi() di manapun Anda butuh Connection JDBC.
 */
public class Koneksi {

    // =========================================================
    //  SESUAIKAN NILAI-NILAI INI DENGAN KONFIGURASI SERVER ANDA
    // =========================================================
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/db_koperasi_gudang";
    private static final String DB_USER     = "root";
    private static final String DB_PASSWORD = "";          // ganti jika ada password
    private static final String DB_DRIVER   = "com.mysql.cj.jdbc.Driver";

    private static Connection connection = null;

    /**
     * Mengembalikan objek Connection yang sudah siap digunakan.
     * Koneksi dibuat sekali (singleton sederhana) dan di-reuse.
     *
     * @return java.sql.Connection yang aktif
     */
    public static Connection getKoneksi() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName(DB_DRIVER);
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("[DB] Koneksi berhasil dibuat ke: " + DB_URL);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver tidak ditemukan: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DB] Gagal konek ke database: " + e.getMessage());
        }
        return connection;
    }
}
