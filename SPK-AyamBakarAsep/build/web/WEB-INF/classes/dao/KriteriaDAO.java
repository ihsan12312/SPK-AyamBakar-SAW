package dao;

import config.DBConnection;
import model.Kriteria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * KriteriaDAO.java
 * ----------------
 * Data Access Object untuk tabel tb_kriteria.
 * Menyediakan operasi CRUD dan validasi bobot.
 *
 * Layer   : DAO (Data Access Object)
 * Package : dao
 */
public class KriteriaDAO {

    private final Connection conn;

    public KriteriaDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    // ─────────────────────────────────────────────────────────
    // READ – Ambil semua kriteria
    // ─────────────────────────────────────────────────────────

    /**
     * Mengambil semua kriteria dari database, diurutkan by id.
     * @return List<Kriteria>
     */
    public List<Kriteria> getAll() throws SQLException {
        List<Kriteria> list = new ArrayList<>();
        String sql = "SELECT * FROM tb_kriteria ORDER BY id_kriteria ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Mengambil satu kriteria berdasarkan ID.
     */
    public Kriteria getById(int id) throws SQLException {
        String sql = "SELECT * FROM tb_kriteria WHERE id_kriteria = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────
    // CREATE – Tambah kriteria baru
    // ─────────────────────────────────────────────────────────

    /**
     * Menambahkan kriteria baru ke database.
     * @return id yang di-generate, atau -1 jika gagal
     */
    public int insert(Kriteria k) throws SQLException {
        String sql = "INSERT INTO tb_kriteria (nama_kriteria, jenis, bobot, satuan, keterangan)"
                   + " VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, k.getNamaKriteria());
            ps.setString(2, k.getJenis());
            ps.setDouble(3, k.getBobot());
            ps.setString(4, k.getSatuan());
            ps.setString(5, k.getKeterangan());
            ps.executeUpdate();

            try (ResultSet gen = ps.getGeneratedKeys()) {
                if (gen.next()) return gen.getInt(1);
            }
        }
        return -1;
    }

    // ─────────────────────────────────────────────────────────
    // UPDATE – Edit kriteria
    // ─────────────────────────────────────────────────────────

    /**
     * Memperbarui data kriteria. Mengembalikan jumlah baris yang diperbarui.
     */
    public int update(Kriteria k) throws SQLException {
        String sql = "UPDATE tb_kriteria SET nama_kriteria=?, jenis=?, bobot=?, "
                   + "satuan=?, keterangan=? WHERE id_kriteria=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, k.getNamaKriteria());
            ps.setString(2, k.getJenis());
            ps.setDouble(3, k.getBobot());
            ps.setString(4, k.getSatuan());
            ps.setString(5, k.getKeterangan());
            ps.setInt(6, k.getIdKriteria());
            return ps.executeUpdate();
        }
    }

    /**
     * Update hanya bobot dari semua kriteria secara batch.
     * Parameter: map idKriteria → bobot baru.
     */
    public void updateBobotBatch(java.util.Map<Integer, Double> bobotMap) throws SQLException {
        String sql = "UPDATE tb_kriteria SET bobot=? WHERE id_kriteria=?";

        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (java.util.Map.Entry<Integer, Double> entry : bobotMap.entrySet()) {
                ps.setDouble(1, entry.getValue());
                ps.setInt(2, entry.getKey());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ─────────────────────────────────────────────────────────
    // DELETE – Hapus kriteria
    // ─────────────────────────────────────────────────────────

    public int delete(int id) throws SQLException {
        String sql = "DELETE FROM tb_kriteria WHERE id_kriteria=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    // ─────────────────────────────────────────────────────────
    // VALIDASI
    // ─────────────────────────────────────────────────────────

    /**
     * Menghitung total bobot semua kriteria.
     * Untuk SAW, total harus = 100.
     */
    public double getTotalBobot() throws SQLException {
        String sql = "SELECT SUM(bobot) FROM tb_kriteria";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        }
        return 0;
    }

    // ─────────────────────────────────────────────────────────
    // HELPER – Mapping ResultSet → Kriteria
    // ─────────────────────────────────────────────────────────

    private Kriteria mapRow(ResultSet rs) throws SQLException {
        return new Kriteria(
            rs.getInt("id_kriteria"),
            rs.getString("nama_kriteria"),
            rs.getString("jenis"),
            rs.getDouble("bobot"),
            rs.getString("satuan"),
            rs.getString("keterangan")
        );
    }
}
