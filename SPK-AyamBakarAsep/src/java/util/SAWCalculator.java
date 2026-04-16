package util;

import model.Alternatif;
import model.HasilSAW;
import model.Kriteria;

import java.util.ArrayList;
import java.util.List;

/**
 * SAWCalculator.java
 * ------------------
 * Kelas utilitas yang mengimplementasikan algoritma
 * Simple Additive Weighting (SAW) secara murni (tanpa akses DB).
 *
 * Tahapan SAW:
 *   1. Bangun matriks keputusan X[i][j]
 *   2. Tentukan nilai referensi (max/min) per kolom
 *   3. Normalisasi: R[i][j] = xij/max (benefit) atau min/xij (cost)
 *   4. Hitung skor: Vi = Σ(wj × rij)
 *   5. Ranking berdasar Vi menurun
 *
 * Layer   : Util / Service
 * Package : util
 */
public class SAWCalculator {

    // ─────────────────────────────────────────────────────────
    // ENTRY POINT
    // ─────────────────────────────────────────────────────────

    /**
     * Menjalankan perhitungan SAW lengkap.
     *
     * @param alternativeList daftar alternatif (sudah berisi nilaiMap)
     * @param criteriaList    daftar kriteria (sudah berisi bobot & jenis)
     * @return List<HasilSAW> diurutkan dari ranking terbaik
     * @throws IllegalArgumentException jika data tidak valid
     */
    public static List<HasilSAW> hitung(List<Alternatif> alternativeList,
                                         List<Kriteria>  criteriaList) {

        // ── Validasi input ────────────────────────────────────
        if (alternativeList == null || alternativeList.isEmpty())
            throw new IllegalArgumentException("Daftar alternatif kosong.");
        if (criteriaList == null || criteriaList.isEmpty())
            throw new IllegalArgumentException("Daftar kriteria kosong.");

        int n = alternativeList.size();  // jumlah alternatif
        int m = criteriaList.size();     // jumlah kriteria

        // ── Step 1: Matriks keputusan X[i][j] ─────────────────
        double[][] X = buildDecisionMatrix(alternativeList, criteriaList);

        // ── Step 2: Nilai referensi per kolom ─────────────────
        double[] refVal = computeReferenceValues(X, criteriaList, n, m);

        // ── Step 3: Normalisasi R[i][j] ───────────────────────
        double[][] R = normalize(X, refVal, criteriaList, n, m);

        // ── Step 4: Bobot (sebagai desimal 0-1) ───────────────
        double[] W = new double[m];
        for (int j = 0; j < m; j++) {
            W[j] = criteriaList.get(j).getBobotDesimal();
        }

        // ── Step 5: Skor akhir Vi = Σ(wj × rij) ──────────────
        double[] V = computeScores(R, W, n, m);

        // ── Step 6: Susun dan ranking ─────────────────────────
        List<HasilSAW> hasilList = buildResult(alternativeList, V, R, W, m);
        hasilList.sort((a, b) -> Double.compare(b.getSkorAkhir(), a.getSkorAkhir()));

        // Tetapkan ranking
        for (int i = 0; i < hasilList.size(); i++) {
            hasilList.get(i).setRanking(i + 1);
        }

        return hasilList;
    }

    // ─────────────────────────────────────────────────────────
    // STEP 1 – Matriks Keputusan
    // ─────────────────────────────────────────────────────────

    private static double[][] buildDecisionMatrix(List<Alternatif> alts,
                                                   List<Kriteria>  crits) {
        int n = alts.size();
        int m = crits.size();
        double[][] X = new double[n][m];

        for (int i = 0; i < n; i++) {
            Alternatif alt = alts.get(i);
            for (int j = 0; j < m; j++) {
                X[i][j] = alt.getNilai(crits.get(j).getIdKriteria());
            }
        }
        return X;
    }

    // ─────────────────────────────────────────────────────────
    // STEP 2 – Nilai Referensi per Kolom
    // ─────────────────────────────────────────────────────────

    private static double[] computeReferenceValues(double[][] X,
                                                    List<Kriteria> crits,
                                                    int n, int m) {
        double[] ref = new double[m];
        for (int j = 0; j < m; j++) {
            boolean isBenefit = crits.get(j).isBenefit();
            ref[j] = isBenefit ? Double.MIN_VALUE : Double.MAX_VALUE;

            for (int i = 0; i < n; i++) {
                if (isBenefit) {
                    if (X[i][j] > ref[j]) ref[j] = X[i][j];
                } else {
                    if (X[i][j] < ref[j]) ref[j] = X[i][j];
                }
            }
        }
        return ref;
    }

    // ─────────────────────────────────────────────────────────
    // STEP 3 – Normalisasi
    // ─────────────────────────────────────────────────────────

    private static double[][] normalize(double[][] X, double[] ref,
                                         List<Kriteria> crits,
                                         int n, int m) {
        double[][] R = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                double xij = X[i][j];
                double rv  = ref[j];

                if (crits.get(j).isBenefit()) {
                    // Rij = xij / max(xij)
                    R[i][j] = (rv == 0) ? 0 : xij / rv;
                } else {
                    // Rij = min(xij) / xij
                    R[i][j] = (xij == 0) ? 0 : rv / xij;
                }
            }
        }
        return R;
    }

    // ─────────────────────────────────────────────────────────
    // STEP 4 – Skor Akhir
    // ─────────────────────────────────────────────────────────

    private static double[] computeScores(double[][] R, double[] W, int n, int m) {
        double[] V = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0;
            for (int j = 0; j < m; j++) {
                sum += W[j] * R[i][j];
            }
            V[i] = sum;
        }
        return V;
    }

    // ─────────────────────────────────────────────────────────
    // STEP 5 – Buat Objek Hasil
    // ─────────────────────────────────────────────────────────

    private static List<HasilSAW> buildResult(List<Alternatif> alts,
                                               double[] V, double[][] R,
                                               double[] W, int m) {
        List<HasilSAW> list = new ArrayList<>();
        for (int i = 0; i < alts.size(); i++) {
            // Nilai terbobot per kriteria
            double[] terbobot = new double[m];
            for (int j = 0; j < m; j++) {
                terbobot[j] = W[j] * R[i][j];
            }

            HasilSAW h = new HasilSAW(
                0,              // ranking diset setelah sort
                alts.get(i),
                V[i],
                R[i].clone(),
                terbobot
            );
            list.add(h);
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────
    // UTILITY – Format skor untuk tampilan
    // ─────────────────────────────────────────────────────────

    /** Mengembalikan persentase skor terhadap skor tertinggi (0-100). */
    public static double persenTerhadapTertinggi(double skor, double skorMax) {
        if (skorMax == 0) return 0;
        return (skor / skorMax) * 100.0;
    }
}
