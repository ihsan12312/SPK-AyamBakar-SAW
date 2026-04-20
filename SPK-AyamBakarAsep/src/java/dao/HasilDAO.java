package dao;

import config.DBConnection;
import model.Alternatif;
import model.HasilSAW;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * HasilDAO.java
 * -------------
 * Data Access Object untuk tabel tb_sesi dan tb_hasil.
 * Menyimpan hasil perhitungan SAW ke database dan mengambil riwayat.
 *
 * Layer   : DAO (Data Access Object)
 * Package : dao
 */
public class HasilDAO {

    private final Connection conn;

    public HasilDAO() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    // ─────────────────────────────────────────────────────────
    // SIMPAN HASIL – Buat sesi lalu simpan ranking
    // ─────────────────────────────────────────────────────────

    /**
     * Menyimpan seluruh hasil SAW dalam satu transaksi:
     * 1. Buat sesi baru di tb_sesi
     * 2. Simpan semua ranking di tb_hasil
     *
     * @return idSesi yang baru dibuat
     */
    public int simpanHasil(List<HasilSAW> hasilList, String catatan) throws SQLException {
        int idSesi = -1;

        conn.setAutoCommit(false);
        try {
            // Step 1: Buat sesi
            String sqlSesi = "INSERT INTO tb_sesi (catatan) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlSesi, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, catatan != null ? catatan : "Perhitungan SAW");
                ps.executeUpdate();
                try (ResultSet gen = ps.getGeneratedKeys()) {
                    if (gen.next()) idSesi = gen.getInt(1);
                }
            }

            // Step 2: Simpan ranking
            String sqlHasil = "INSERT INTO tb_hasil (id_sesi, id_alternatif, skor_akhir, ranking) VALUES (?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlHasil)) {
                for (HasilSAW h : hasilList) {
                    ps.setInt(1, idSesi);
                    ps.setInt(2, h.getAlternatif().getIdAlternatif());
                    ps.setDouble(3, h.getSkorAkhir());
                    ps.setInt(4, h.getRanking());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }

        return idSesi;
    }

    // ─────────────────────────────────────────────────────────
    // READ – Ambil riwayat sesi
    // ─────────────────────────────────────────────────────────

    /**
     * Mengambil daftar sesi terakhir (maks. 10 sesi).
     * Setiap item adalah: [id_sesi, catatan, created_at, pemenang, skor_pemenang]
     */
    public List<Object[]> getRiwayatSesi(int limit) throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT s.id_sesi, s.catatan, s.created_at, "
                   + "       a.nama_paket, h.skor_akhir "
                   + "FROM tb_sesi s "
                   + "JOIN tb_hasil h ON s.id_sesi = h.id_sesi AND h.ranking = 1 "
                   + "JOIN tb_alternatif a ON h.id_alternatif = a.id_alternatif "
                   + "ORDER BY s.created_at DESC "
                   + "LIMIT ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt("id_sesi"),
                        rs.getString("catatan"),
                        rs.getTimestamp("created_at"),
                        rs.getString("nama_paket"),
                        rs.getDouble("skor_akhir")
                    });
                }
            }
        }
        return list;
    }

    /**
     * Mengambil detail hasil dari satu sesi historis.
     * Tidak mengambil nilaiNormal dan nilaiTerbobot (karena tidak disimpan di DB),
     * tetapi cukup untuk menampilkan pemenang, tabel ranking, dan grafik.
     */
    public List<HasilSAW> getDetailSesi(int idSesi) throws SQLException {
        List<HasilSAW> list = new ArrayList<>();
        String sql = "SELECT h.ranking, h.skor_akhir, "
                   + "       a.id_alternatif, a.nama_paket, a.harga, a.deskripsi "
                   + "FROM tb_hasil h "
                   + "JOIN tb_alternatif a ON h.id_alternatif = a.id_alternatif "
                   + "WHERE h.id_sesi = ? "
                   + "ORDER BY h.ranking ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSesi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Alternatif alt = new Alternatif();
                    alt.setIdAlternatif(rs.getInt("id_alternatif"));
                    alt.setNamaPaket(rs.getString("nama_paket"));
                    alt.setHarga(rs.getInt("harga"));
                    alt.setDeskripsi(rs.getString("deskripsi"));
                    
                    HasilSAW h = new HasilSAW();
                    h.setRanking(rs.getInt("ranking"));
                    h.setSkorAkhir(rs.getDouble("skor_akhir"));
                    h.setAlternatif(alt);
                    // nilaiNormal dan nilaiTerbobot otomatis null
                    
                    list.add(h);
                }
            }
        }
        return list;
    }

    /**
     * Menghapus satu sesi dan semua hasilnya (CASCADE).
     */
    public int deleteSesi(int idSesi) throws SQLException {
        String sql = "DELETE FROM tb_sesi WHERE id_sesi=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idSesi);
            return ps.executeUpdate();
        }
    }
}
