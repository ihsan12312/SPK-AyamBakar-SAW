package model;

/**
 * Kriteria.java
 * -------------
 * Model / POJO untuk tabel tb_kriteria.
 * Merepresentasikan satu baris kriteria penilaian SAW.
 *
 * Layer   : Model
 * Package : model
 */
public class Kriteria {

    private int    idKriteria;
    private String namaKriteria;
    private String jenis;        // "benefit" atau "cost"
    private double bobot;        // 0 - 100 (persen)
    private String satuan;
    private String keterangan;

    // ── Constructor kosong ────────────────────────────────────
    public Kriteria() {}

    // ── Constructor penuh ─────────────────────────────────────
    public Kriteria(int idKriteria, String namaKriteria,
                    String jenis, double bobot,
                    String satuan, String keterangan) {
        this.idKriteria   = idKriteria;
        this.namaKriteria = namaKriteria;
        this.jenis        = jenis;
        this.bobot        = bobot;
        this.satuan       = satuan;
        this.keterangan   = keterangan;
    }

    // ── Getter & Setter ───────────────────────────────────────

    public int getIdKriteria()             { return idKriteria; }
    public void setIdKriteria(int v)       { this.idKriteria = v; }

    public String getNamaKriteria()        { return namaKriteria; }
    public void setNamaKriteria(String v)  { this.namaKriteria = v; }

    public String getJenis()               { return jenis; }
    public void setJenis(String v)         { this.jenis = v; }

    public double getBobot()               { return bobot; }
    public void setBobot(double v)         { this.bobot = v; }

    public String getSatuan()              { return satuan; }
    public void setSatuan(String v)        { this.satuan = v; }

    public String getKeterangan()          { return keterangan; }
    public void setKeterangan(String v)    { this.keterangan = v; }

    /** Mengembalikan bobot sebagai desimal (0.0 - 1.0) */
    public double getBobotDesimal()        { return bobot / 100.0; }

    /** True jika jenis = benefit */
    public boolean isBenefit()             { return "benefit".equalsIgnoreCase(jenis); }

    @Override
    public String toString() {
        return "Kriteria{id=" + idKriteria + ", nama=" + namaKriteria
             + ", jenis=" + jenis + ", bobot=" + bobot + "%}";
    }
}
