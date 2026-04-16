package controller;

import dao.KriteriaDAO;
import model.Kriteria;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KriteriaServlet.java
 * --------------------
 * Controller untuk manajemen kriteria penilaian.
 *
 * Endpoints:
 *   GET  /kriteria          → tampilkan halaman daftar kriteria
 *   POST /kriteria?action=tambah  → tambah kriteria baru
 *   POST /kriteria?action=update  → update satu kriteria
 *   POST /kriteria?action=bobot   → update semua bobot sekaligus
 *   POST /kriteria?action=hapus   → hapus kriteria
 *
 * Layer   : Controller (Servlet)
 * Package : controller
 */
@WebServlet(name = "KriteriaServlet", urlPatterns = {"/kriteria"})
public class KriteriaServlet extends HttpServlet {

    // ─────────────────────────────────────────────────────────
    // GET – Tampilkan halaman kriteria
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        try {
            KriteriaDAO dao = new KriteriaDAO();
            List<Kriteria> list = dao.getAll();
            double totalBobot  = dao.getTotalBobot();

            req.setAttribute("kriteriaList", list);
            req.setAttribute("totalBobot",   totalBobot);
            req.setAttribute("bobotValid",   Math.abs(totalBobot - 100) < 0.01);

        } catch (SQLException e) {
            req.setAttribute("error", "Gagal memuat kriteria: " + e.getMessage());
        }

        req.getRequestDispatcher("/kriteria.jsp").forward(req, res);
    }

    // ─────────────────────────────────────────────────────────
    // POST – Proses aksi
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        KriteriaDAO dao = new KriteriaDAO();

        try {
            switch (action == null ? "" : action) {

                case "tambah":
                    handleTambah(req, dao);
                    req.getSession().setAttribute("sukses", "Kriteria berhasil ditambahkan.");
                    break;

                case "update":
                    handleUpdate(req, dao);
                    req.getSession().setAttribute("sukses", "Kriteria berhasil diperbarui.");
                    break;

                case "bobot":
                    handleUpdateBobot(req, dao);
                    req.getSession().setAttribute("sukses", "Bobot berhasil disimpan.");
                    break;

                case "hapus":
                    int idHapus = Integer.parseInt(req.getParameter("id_kriteria"));
                    dao.delete(idHapus);
                    req.getSession().setAttribute("sukses", "Kriteria berhasil dihapus.");
                    break;

                default:
                    req.getSession().setAttribute("error", "Aksi tidak dikenal.");
            }

        } catch (SQLException e) {
            req.getSession().setAttribute("error", "Kesalahan database: " + e.getMessage());
        } catch (NumberFormatException e) {
            req.getSession().setAttribute("error", "Format angka tidak valid.");
        }

        res.sendRedirect(req.getContextPath() + "/kriteria");
    }

    // ─────────────────────────────────────────────────────────
    // PRIVATE HANDLERS
    // ─────────────────────────────────────────────────────────

    private void handleTambah(HttpServletRequest req, KriteriaDAO dao) throws SQLException {
        Kriteria k = new Kriteria();
        k.setNamaKriteria(req.getParameter("nama_kriteria"));
        k.setJenis(req.getParameter("jenis"));
        k.setBobot(Double.parseDouble(req.getParameter("bobot")));
        k.setSatuan(req.getParameter("satuan"));
        k.setKeterangan(req.getParameter("keterangan"));
        dao.insert(k);
    }

    private void handleUpdate(HttpServletRequest req, KriteriaDAO dao) throws SQLException {
        Kriteria k = new Kriteria();
        k.setIdKriteria(Integer.parseInt(req.getParameter("id_kriteria")));
        k.setNamaKriteria(req.getParameter("nama_kriteria"));
        k.setJenis(req.getParameter("jenis"));
        k.setBobot(Double.parseDouble(req.getParameter("bobot")));
        k.setSatuan(req.getParameter("satuan"));
        k.setKeterangan(req.getParameter("keterangan"));
        dao.update(k);
    }

    private void handleUpdateBobot(HttpServletRequest req, KriteriaDAO dao) throws SQLException {
        // Parameter: bobot_<id>=<nilai> untuk setiap kriteria
        Map<Integer, Double> bobotMap = new HashMap<>();
        for (String param : req.getParameterMap().keySet()) {
            if (param.startsWith("bobot_")) {
                int id = Integer.parseInt(param.substring(6));
                double bobot = Double.parseDouble(req.getParameter(param));
                bobotMap.put(id, bobot);
            }
        }
        dao.updateBobotBatch(bobotMap);
    }
}
