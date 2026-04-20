package controller;

import dao.AlternatifDAO;
import dao.HasilDAO;
import dao.KriteriaDAO;
import model.Alternatif;
import model.HasilSAW;
import model.Kriteria;
import util.SAWCalculator;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * HasilServlet.java
 * -----------------
 * Controller untuk perhitungan SAW dan tampilan hasil ranking.
 *
 * Endpoints:
 *   GET  /hasil              → tampilkan halaman hasil (kosong jika belum hitung)
 *   POST /hasil?action=hitung → jalankan SAW, simpan ke DB, tampilkan hasil
 *   POST /hasil?action=hapus  → hapus satu sesi riwayat
 *
 * Layer   : Controller (Servlet)
 * Package : controller
 */
@WebServlet(name = "HasilServlet", urlPatterns = {"/hasil"})
public class HasilServlet extends HttpServlet {

    // ─────────────────────────────────────────────────────────
    // GET – Tampilkan halaman hasil (ambil riwayat terbaru)
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        try {
            KriteriaDAO   critDAO = new KriteriaDAO();
            AlternatifDAO altDAO  = new AlternatifDAO();
            HasilDAO      hasilDAO = new HasilDAO();

            req.setAttribute("kriteriaList",   critDAO.getAll());
            req.setAttribute("alternatifList", altDAO.getAllWithNilai());
            req.setAttribute("riwayat",        hasilDAO.getRiwayatSesi(10));
            req.setAttribute("totalBobot",     critDAO.getTotalBobot());

            // Jika ada param view_sesi, load dari history
            String viewSesi = req.getParameter("view_sesi");
            if (viewSesi != null && !viewSesi.isEmpty()) {
                int idSesiView = Integer.parseInt(viewSesi);
                List<HasilSAW> historyHasil = hasilDAO.getDetailSesi(idSesiView);
                if (!historyHasil.isEmpty()) {
                    req.setAttribute("hasilSAW", historyHasil);
                    req.setAttribute("idSesi", idSesiView);
                    req.setAttribute("isHistory", true);
                } else {
                    req.setAttribute("error", "Data sesi tidak ditemukan.");
                }
            } else if (req.getSession().getAttribute("hasilSAW") != null) {
                // Jika ada hasil di session (baru dihitung), pindahkan ke request
                req.setAttribute("hasilSAW",   req.getSession().getAttribute("hasilSAW"));
                req.setAttribute("idSesi",     req.getSession().getAttribute("idSesi"));
                req.getSession().removeAttribute("hasilSAW");
                req.getSession().removeAttribute("idSesi");
            }

        } catch (SQLException | NumberFormatException e) {
            req.setAttribute("error", "Error: " + e.getMessage());
        }

        req.getRequestDispatcher("/hasil.jsp").forward(req, res);
    }

    // ─────────────────────────────────────────────────────────
    // POST – Jalankan perhitungan SAW
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        if ("hapus".equals(action)) {
            handleHapusSesi(req, res);
            return;
        }

        // Default action: hitung SAW
        try {
            KriteriaDAO   critDAO  = new KriteriaDAO();
            AlternatifDAO altDAO   = new AlternatifDAO();
            HasilDAO      hasilDAO = new HasilDAO();

            List<Kriteria>   kriteriaList   = critDAO.getAll();
            List<Alternatif> alternatifList = altDAO.getAllWithNilai();

            // ── Validasi bobot total ──────────────────────────
            double totalBobot = critDAO.getTotalBobot();
            if (Math.abs(totalBobot - 100) > 0.01) {
                req.getSession().setAttribute("error",
                    "Total bobot kriteria = " + totalBobot + "%. Harus tepat 100%!");
                res.sendRedirect(req.getContextPath() + "/hasil");
                return;
            }

            if (alternatifList.size() < 2) {
                req.getSession().setAttribute("error",
                    "Minimal 2 alternatif diperlukan.");
                res.sendRedirect(req.getContextPath() + "/hasil");
                return;
            }

            // ── Jalankan SAW ──────────────────────────────────
            List<HasilSAW> hasilList = SAWCalculator.hitung(alternatifList, kriteriaList);

            // ── Simpan ke database ────────────────────────────
            String catatan = req.getParameter("catatan");
            int idSesi = hasilDAO.simpanHasil(hasilList, catatan);

            // ── Teruskan ke session → redirect → GET ──────────
            req.getSession().setAttribute("hasilSAW",     hasilList);
            req.getSession().setAttribute("kriteriaList", kriteriaList);
            req.getSession().setAttribute("idSesi",       idSesi);
            req.getSession().setAttribute("sukses",
                "Perhitungan SAW berhasil! Pemenang: "
                + hasilList.get(0).getAlternatif().getNamaPaket());

        } catch (SQLException e) {
            req.getSession().setAttribute("error", "Error database: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            req.getSession().setAttribute("error", "Data tidak valid: " + e.getMessage());
        }

        res.sendRedirect(req.getContextPath() + "/hasil");
    }

    // ─────────────────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────────────────

    private void handleHapusSesi(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        try {
            int idSesi = Integer.parseInt(req.getParameter("id_sesi"));
            new HasilDAO().deleteSesi(idSesi);
            req.getSession().setAttribute("sukses", "Riwayat sesi dihapus.");
        } catch (Exception e) {
            req.getSession().setAttribute("error", "Gagal hapus sesi: " + e.getMessage());
        }
        res.sendRedirect(req.getContextPath() + "/hasil");
    }
}
