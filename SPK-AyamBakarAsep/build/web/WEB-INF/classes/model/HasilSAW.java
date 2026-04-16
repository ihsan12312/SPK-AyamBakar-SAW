package model;

/**
 * HasilSAW.java
 * -------------
 * Model untuk menyimpan hasil akhir perhitungan SAW
 * per alternatif: ranking, skor, dan data normalisasi.
 *
 * Layer   : Model
 * Package : model
 */
public class HasilSAW {

    private int         ranking;
    private Alternatif  alternatif;
    private double      skorAkhir;      // Vi = Σ(wj × rij)
    private double[]    nilaiNormal;    // rij per kriteria
    private double[]    nilaiTerbobot;  // wj × rij per kriteria

    // ── Constructor ───────────────────────────────────────────
    public HasilSAW() {}

    public HasilSAW(int ranking, Alternatif alternatif, double skorAkhir,
                    double[] nilaiNormal, double[] nilaiTerbobot) {
        this.ranking        = ranking;
        this.alternatif     = alternatif;
        this.skorAkhir      = skorAkhir;
        this.nilaiNormal    = nilaiNormal;
        this.nilaiTerbobot  = nilaiTerbobot;
    }

    // ── Getter & Setter ───────────────────────────────────────

    public int getRanking()                         { return ranking; }
    public void setRanking(int v)                   { this.ranking = v; }

    public Alternatif getAlternatif()               { return alternatif; }
    public void setAlternatif(Alternatif v)         { this.alternatif = v; }

    public double getSkorAkhir()                    { return skorAkhir; }
    public void setSkorAkhir(double v)              { this.skorAkhir = v; }

    public double[] getNilaiNormal()                { return nilaiNormal; }
    public void setNilaiNormal(double[] v)          { this.nilaiNormal = v; }

    public double[] getNilaiTerbobot()              { return nilaiTerbobot; }
    public void setNilaiTerbobot(double[] v)        { this.nilaiTerbobot = v; }

    /** Skor dalam format % (0–100 tampak) */
    public String getSkorPersen() {
        return String.format("%.2f", skorAkhir * 100);
    }

    /** Skor 4 desimal untuk tabel teknis */
    public String getSkorFormatted() {
        return String.format("%.4f", skorAkhir);
    }

    /** Label status berdasarkan ranking */
    public String getStatusLabel() {
        if (ranking == 1) return "Terbaik";
        if (ranking <= 3) return "Unggulan";
        return "Cukup Baik";
    }

    @Override
    public String toString() {
        return "HasilSAW{rank=" + ranking
             + ", name=" + (alternatif != null ? alternatif.getNamaPaket() : "?")
             + ", skor=" + String.format("%.4f", skorAkhir) + "}";
    }
}
