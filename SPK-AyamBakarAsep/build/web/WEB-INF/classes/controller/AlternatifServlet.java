package controller;

import dao.AlternatifDAO;
import dao.KriteriaDAO;
import model.Alternatif;
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
 * AlternatifServlet.java
 * ----------------------
 * Controller untuk manajemen alternatif (paket menu) dan nilainya.
 *
 * Endpoints:
 *   GET  /alternatif                 → daftar paket + form nilai
 *   POST /alternatif?action=tambah   → tambah paket baru
 *   POST /alternatif?action=update   → update data paket
 *   POST /alternatif?action=nilai    → simpan nilai per kriteria
 *   POST /alternatif?action=hapus    → hapus (soft delete) paket
 *
 * Layer   : Controller (Servlet)
 * Package : controller
 */
@WebServlet(name = "AlternatifServlet", urlPatterns = {"/alternatif"})
public class AlternatifServlet extends HttpServlet {

    // ─────────────────────────────────────────────────────────
    // GET
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        try {
            AlternatifDAO altDAO  = new AlternatifDAO();
            KriteriaDAO   critDAO = new KriteriaDAO();

            List<Alternatif> altList  = altDAO.getAllWithNilai();
            List<Kriteria>   critList = critDAO.getAll();

            req.setAttribute("alternatifList", altList);
            req.setAttribute("kriteriaList",   critList);

        } catch (SQLException e) {
            req.setAttribute("error", "Gagal memuat data: " + e.getMessage());
        }

        req.getRequestDispatcher("/alternatif.jsp").forward(req, res);
    }

    // ─────────────────────────────────────────────────────────
    // POST
    // ─────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        AlternatifDAO dao = new AlternatifDAO();

        try {
            switch (action == null ? "" : action) {

                case "tambah":
                    int newId = handleTambah(req, dao);
                    // Langsung redirect ke form nilai jika ID berhasil
                    req.getSession().setAttribute("sukses",
                        "Paket berhasil ditambahkan. Silakan isi nilainya.");
                    res.sendRedirect(req.getContextPath() + "/alternatif");
                    return;

                case "update":
                    handleUpdate(req, dao);
                    req.getSession().setAttribute("sukses", "Paket berhasil diperbarui.");
                    break;

                case "nilai":
                    handleSimpanNilai(req, dao);
                    req.getSession().setAttribute("sukses", "Nilai berhasil disimpan.");
                    break;

                case "hapus":
                    int idHapus = Integer.parseInt(req.getParameter("id_alternatif"));
                    dao.delete(idHapus);
                    req.getSession().setAttribute("sukses", "Paket berhasil dihapus.");
                    break;

                default:
                    req.getSession().setAttribute("error", "Aksi tidak dikenal.");
            }

        } catch (SQLException e) {
            req.getSession().setAttribute("error", "Error database: " + e.getMessage());
        } catch (NumberFormatException e) {
            req.getSession().setAttribute("error", "Format angka tidak valid: " + e.getMessage());
        }

        res.sendRedirect(req.getContextPath() + "/alternatif");
    }

    // ─────────────────────────────────────────────────────────
    // PRIVATE HANDLERS
    // ─────────────────────────────────────────────────────────

    private int handleTambah(HttpServletRequest req, AlternatifDAO dao) throws SQLException {
        Alternatif a = new Alternatif();
        a.setNamaPaket(req.getParameter("nama_paket"));
        a.setDeskripsi(req.getParameter("deskripsi"));
        a.setHarga(Integer.parseInt(req.getParameter("harga").replaceAll("[^0-9]", "")));
        return dao.insert(a);
    }

    private void handleUpdate(HttpServletRequest req, AlternatifDAO dao) throws SQLException {
        Alternatif a = new Alternatif();
        a.setIdAlternatif(Integer.parseInt(req.getParameter("id_alternatif")));
        a.setNamaPaket(req.getParameter("nama_paket"));
        a.setDeskripsi(req.getParameter("deskripsi"));
        a.setHarga(Integer.parseInt(req.getParameter("harga").replaceAll("[^0-9]", "")));
        dao.update(a);
    }

    private void handleSimpanNilai(HttpServletRequest req, AlternatifDAO dao) throws SQLException {
        int idAlt = Integer.parseInt(req.getParameter("id_alternatif"));

        // Parameter: nilai_<idKriteria>=<nilai>
        Map<Integer, Double> nilaiMap = new HashMap<>();
        for (String param : req.getParameterMap().keySet()) {
            if (param.startsWith("nilai_")) {
                int idKrit  = Integer.parseInt(param.substring(6));
                double nilai = Double.parseDouble(req.getParameter(param));
                nilaiMap.put(idKrit, nilai);
            }
        }
        dao.saveNilaiBatch(idAlt, nilaiMap);
    }
}
