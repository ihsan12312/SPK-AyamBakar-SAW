package model;

import java.util.HashMap;
import java.util.Map;

/**
 * Alternatif.java
 * ---------------
 * Model / POJO untuk tabel tb_alternatif.
 * Merepresentasikan satu paket menu yang dievaluasi.
 *
 * Layer   : Model
 * Package : model
 */
public class Alternatif {

    private int    idAlternatif;
    private String namaPaket;
    private String deskripsi;
    private int    harga;
    private boolean isActive;

    /**
     * Map nilai per kriteria: key = idKriteria, value = nilai
     * Diisi oleh DAO saat mengambil data lengkap.
     */
    private Map<Integer, Double> nilaiMap = new HashMap<>();

    // ── Constructor kosong ────────────────────────────────────
    public Alternatif() {}

    // ── Constructor dasar ─────────────────────────────────────
    public Alternatif(int idAlternatif, String namaPaket,
                      String deskripsi, int harga, boolean isActive) {
        this.idAlternatif = idAlternatif;
        this.namaPaket    = namaPaket;
        this.deskripsi    = deskripsi;
        this.harga        = harga;
        this.isActive     = isActive;
    }

    // ── Getter & Setter ───────────────────────────────────────

    public int getIdAlternatif()            { return idAlternatif; }
    public void setIdAlternatif(int v)      { this.idAlternatif = v; }

    public String getNamaPaket()            { return namaPaket; }
    public void setNamaPaket(String v)      { this.namaPaket = v; }

    public String getDeskripsi()            { return deskripsi; }
    public void setDeskripsi(String v)      { this.deskripsi = v; }

    public int getHarga()                   { return harga; }
    public void setHarga(int v)             { this.harga = v; }

    public boolean isActive()               { return isActive; }
    public void setActive(boolean v)        { this.isActive = v; }

    public Map<Integer, Double> getNilaiMap()             { return nilaiMap; }
    public void setNilaiMap(Map<Integer, Double> nilaiMap){ this.nilaiMap = nilaiMap; }

    /** Mendapatkan nilai untuk kriteria tertentu; default 0 jika tidak ada. */
    public double getNilai(int idKriteria) {
        return nilaiMap.getOrDefault(idKriteria, 0.0);
    }

    /** Menyimpan satu nilai ke dalam map. */
    public void setNilai(int idKriteria, double nilai) {
        nilaiMap.put(idKriteria, nilai);
    }

    /** Harga dalam format Rupiah (Rp 55.000) */
    public String getHargaFormatted() {
        return "Rp " + String.format("%,.0f", (double) harga).replace(',', '.');
    }

    @Override
    public String toString() {
        return "Alternatif{id=" + idAlternatif + ", nama=" + namaPaket
             + ", harga=" + harga + "}";
    }
}
