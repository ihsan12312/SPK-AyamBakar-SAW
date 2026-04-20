package controller;

import dao.AlternatifDAO;
import dao.HasilDAO;
import dao.KriteriaDAO;
import model.Alternatif;
import model.HasilSAW;
import model.Kriteria;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * IndexServlet.java
 * -----------------
 * Controller untuk halaman utama (Dashboard).
 * Menampilkan ringkasan statistik: jumlah kriteria, alternatif,
 * total bobot, dan riwayat 5 sesi terakhir.
 *
 * URL      : /
 * Method   : GET
 * Layer    : Controller (Servlet)
 * Package  : controller
 */
@WebServlet(name = "IndexServlet", urlPatterns = {"", "/index"})
public class IndexServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            KriteriaDAO   kriteriaDAO   = new KriteriaDAO();
            AlternatifDAO alternatifDAO = new AlternatifDAO();
            HasilDAO      hasilDAO      = new HasilDAO();

            // Statistik dashboard
            List<Kriteria>   kriteriaList   = kriteriaDAO.getAll();
            List<Alternatif> alternatifList = alternatifDAO.getAll();
            double           totalBobot     = kriteriaDAO.getTotalBobot();
            List<Object[]>   riwayat        = hasilDAO.getRiwayatSesi(5);

            request.setAttribute("kriteriaList",   kriteriaList);
            request.setAttribute("alternatifList", alternatifList);
            request.setAttribute("jumlahKriteria", kriteriaList.size());
            request.setAttribute("jumlahAlternatif", alternatifList.size());
            request.setAttribute("totalBobot",     totalBobot);
            request.setAttribute("bobotValid",     Math.abs(totalBobot - 100) < 0.01);
            request.setAttribute("riwayat",        riwayat);

            request.getRequestDispatcher("/index.jsp").forward(request, response);

        } catch (SQLException e) {
            request.setAttribute("error", "Gagal memuat data: " + e.getMessage());
            request.getRequestDispatcher("/index.jsp").forward(request, response);
        }
    }
}
