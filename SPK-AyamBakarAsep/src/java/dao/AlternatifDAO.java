package dao;

import config.DBConnection;
import model.Alternatif;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AlternatifDAO.java
 * ------------------
 * Data Access Object untuk tabel tb_alternatif dan tb_nilai.
 * Menyediakan operasi CRUD beserta pengelolaan nilai tiap kriteria.
 *
 * Layer   : DAO (Data Access Object)
 * Package : dao
 */
public class AlternatifDAO {

    private final Connection conn;

    public AlternatifDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    // ─────────────────────────────────────────────────────────
    // READ – Ambil semua alternatif (tanpa nilai)
    // ─────────────────────────────────────────────────────────

    /**
     * Mengambil semua alternatif aktif tanpa data nilai.
     */
    public List<Alternatif> getAll() throws SQLException {
        List<Alternatif> list = new ArrayList<>();
        String sql = "SELECT * FROM tb_alternatif WHERE is_active=1 ORDER BY id_alternatif ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Mengambil satu alternatif berdasarkan ID.
     */
    public Alternatif getById(int id) throws SQLException {
        String sql = "SELECT * FROM tb_alternatif WHERE id_alternatif=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ─────────────────────────────────────────────────────────
    // READ – Ambil semua alternatif LENGKAP dengan nilai
    // ─────────────────────────────────────────────────────────

    /**
     * Mengambil semua alternatif beserta nilai per kriteria.
     * Digunakan oleh SAWCalculator untuk membangun matriks keputusan.
     */
    public List<Alternatif> getAllWithNilai() throws SQLException {
        List<Alternatif> list = getAll();

        // Ambil semua nilai sekaligus (lebih efisien dari N query)
        String sql = "SELECT id_alternatif, id_kriteria, nilai FROM tb_nilai";
        Map<Integer, Map<Integer, Double>> nilaiIndex = new HashMap<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int altId  = rs.getInt("id_alternatif");
                int critId = rs.getInt("id_kriteria");
                double val = rs.getDouble("nilai");
                nilaiIndex.computeIfAbsent(altId, k -> new HashMap<>()).put(critId, val);
            }
        }

        // Masukkan nilai ke tiap alternatif
        for (Alternatif alt : list) {
            Map<Integer, Double> nilaiAlt = nilaiIndex.getOrDefault(alt.getIdAlternatif(), new HashMap<>());
            alt.setNilaiMap(nilaiAlt);
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────
    // CREATE – Tambah alternatif baru
    // ─────────────────────────────────────────────────────────

    /**
     * Menambahkan alternatif baru. Mengembalikan ID yang di-generate.
     */
    public int insert(Alternatif a) throws SQLException {
        String sql = "INSERT INTO tb_alternatif (nama_paket, deskripsi, harga) VALUES (?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.getNamaPaket());
            ps.setString(2, a.getDeskripsi());
            ps.setInt(3, a.getHarga());
            ps.executeUpdate();

            try (ResultSet gen = ps.getGeneratedKeys()) {
                if (gen.next()) return gen.getInt(1);
            }
        }
        return -1;
    }

    // ─────────────────────────────────────────────────────────
    // UPDATE – Edit alternatif
    // ─────────────────────────────────────────────────────────

    public int update(Alternatif a) throws SQLException {
        String sql = "UPDATE tb_alternatif SET nama_paket=?, deskripsi=?, harga=? WHERE id_alternatif=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getNamaPaket());
            ps.setString(2, a.getDeskripsi());
            ps.setInt(3, a.getHarga());
            ps.setInt(4, a.getIdAlternatif());
            return ps.executeUpdate();
        }
    }

    // ─────────────────────────────────────────────────────────
    // DELETE – Hapus alternatif (soft delete)
    // ─────────────────────────────────────────────────────────

    public int delete(int id) throws SQLException {
        String sql = "UPDATE tb_alternatif SET is_active=0 WHERE id_alternatif=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    // ─────────────────────────────────────────────────────────
    // NILAI – CRUD nilai alternatif per kriteria
    // ─────────────────────────────────────────────────────────

    /**
     * Menyimpan atau memperbarui nilai (UPSERT) satu sel matriks.
     */
    public void upsertNilai(int idAlternatif, int idKriteria, double nilai) throws SQLException {
        String sql = "INSERT INTO tb_nilai (id_alternatif, id_kriteria, nilai) VALUES (?,?,?)"
                   + " ON DUPLICATE KEY UPDATE nilai=VALUES(nilai)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAlternatif);
            ps.setInt(2, idKriteria);
            ps.setDouble(3, nilai);
            ps.executeUpdate();
        }
    }

    /**
     * Menyimpan semua nilai satu alternatif secara batch (lebih efisien).
     * Parameter nilaiMap: key = idKriteria, value = nilai.
     */
    public void saveNilaiBatch(int idAlternatif, Map<Integer, Double> nilaiMap) throws SQLException {
        String sql = "INSERT INTO tb_nilai (id_alternatif, id_kriteria, nilai) VALUES (?,?,?)"
                   + " ON DUPLICATE KEY UPDATE nilai=VALUES(nilai)";

        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map.Entry<Integer, Double> entry : nilaiMap.entrySet()) {
                ps.setInt(1, idAlternatif);
                ps.setInt(2, entry.getKey());
                ps.setDouble(3, entry.getValue());
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
    // HELPER – Mapping ResultSet → Alternatif
    // ─────────────────────────────────────────────────────────

    private Alternatif mapRow(ResultSet rs) throws SQLException {
        return new Alternatif(
            rs.getInt("id_alternatif"),
            rs.getString("nama_paket"),
            rs.getString("deskripsi"),
            rs.getInt("harga"),
            rs.getInt("is_active") == 1
        );
    }
}
