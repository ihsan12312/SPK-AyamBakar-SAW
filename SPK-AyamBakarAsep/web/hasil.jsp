<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Hasil SAW – SPK Ayam Bakar Asep</title>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700;800&display=swap" rel="stylesheet"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css"/>
</head>
<body>

<%@ include file="WEB-INF/jspf/navbar.jspf" %>

<main class="main-wrap">

  <%-- Flash messages --%>
  <c:if test="${not empty sessionScope.sukses}">
    <div class="alert alert-success">${sessionScope.sukses}</div>
    <c:remove var="sukses" scope="session"/>
  </c:if>
  <c:if test="${not empty sessionScope.error}">
    <div class="alert alert-error">${sessionScope.error}</div>
    <c:remove var="error" scope="session"/>
  </c:if>

  <div class="page-header">
    <div>
      <h1 class="page-title">⚡ Perhitungan SAW</h1>
      <p class="page-desc">Jalankan metode Simple Additive Weighting untuk menemukan paket menu terbaik.</p>
    </div>
    <%-- Tombol Hitung --%>
    <form method="post" action="${pageContext.request.contextPath}/hasil" id="form-hitung">
      <input type="hidden" name="action" value="hitung"/>
      <input type="text" name="catatan" placeholder="Catatan sesi (opsional)"
             class="input-catatan"/>
      <button type="submit" class="btn-primary" id="btn-hitung-saw">
        ⚡ Hitung Sekarang
      </button>
    </form>
  </div>

  <%-- Total bobot warning --%>
  <c:if test="${totalBobot != 100}">
  <div class="alert alert-error">
    ⚠️ Total bobot kriteria = <fmt:formatNumber value="${totalBobot}" maxFractionDigits="2"/>%.
    Harap sesuaikan di <a href="${pageContext.request.contextPath}/kriteria">halaman Kriteria</a> hingga tepat 100%.
  </div>
  </c:if>

  <%-- ====== HASIL SAW (jika ada) ====== --%>
  <c:if test="${not empty hasilSAW}">
    <c:set var="terbaik" value="${hasilSAW[0]}"/>
    <c:set var="skorMax" value="${hasilSAW[0].skorAkhir}"/>

    <%-- WINNER CARD --%>
    <div class="winner-card">
      <div class="winner-trophy">🏆</div>
      <div class="winner-info">
        <div class="winner-label">🥇 REKOMENDASI TERBAIK</div>
        <div class="winner-name">${terbaik.alternatif.namaPaket}</div>
        <div class="winner-price">${terbaik.alternatif.hargaFormatted}</div>
        <div class="winner-desc">${terbaik.alternatif.deskripsi}</div>
      </div>
      <div class="winner-score-box">
        <div class="ws-label">Skor (Vi)</div>
        <div class="ws-value"><fmt:formatNumber value="${terbaik.skorAkhir * 100}" maxFractionDigits="1"/></div>
        <div class="ws-sub">dari 100</div>
      </div>
    </div>

    <%-- RANKING TABLE --%>
    <section class="section-card">
      <div class="section-head"><h2>📋 Tabel Ranking Lengkap</h2></div>
      <div class="table-wrap">
        <table class="data-table result-table">
          <thead>
            <tr>
              <th>Rank</th><th>Paket Menu</th><th>Harga</th>
              <th>Skor Akhir (Vi)</th><th>Status</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="h" items="${hasilSAW}" varStatus="s">
            <tr class="${s.index == 0 ? 'row-winner' : ''}">
              <td>
                <span class="rank-badge rank-${s.index < 3 ? s.index+1 : 'n'}">
                  ${h.ranking}
                </span>
              </td>
              <td><strong>${h.alternatif.namaPaket}</strong></td>
              <td>${h.alternatif.hargaFormatted}</td>
              <td>
                <div class="score-bar-wrap">
                  <div class="score-bg">
                    <div class="score-fill"
                         style="width:${h.skorAkhir / skorMax * 100}%"></div>
                  </div>
                  <span class="score-num"><fmt:formatNumber value="${h.skorAkhir}" pattern="0.0000"/></span>
                </div>
              </td>
              <td>
                <c:choose>
                  <c:when test="${s.index == 0}"><span class="tag tag-best">⭐ Terbaik</span></c:when>
                  <c:when test="${s.index < 3}"> <span class="tag tag-good">Unggulan</span></c:when>
                  <c:otherwise>                  <span class="tag tag-avg">Cukup Baik</span></c:otherwise>
                </c:choose>
              </td>
            </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </section>

    <%-- MATRIKS TERNORMALISASI --%>
    <section class="section-card">
      <div class="section-head"><h2>🔢 Matriks Ternormalisasi (R)</h2></div>
      <div class="table-wrap">
        <table class="data-table norm-table">
          <thead>
            <tr>
              <th>Alternatif</th>
              <c:forEach var="k" items="${sessionScope.kriteriaList != null ? sessionScope.kriteriaList : kriteriaList}">
                <th>${k.namaKriteria}</th>
              </c:forEach>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="h" items="${hasilSAW}">
            <tr>
              <td><strong>${h.alternatif.namaPaket}</strong></td>
              <c:forEach begin="0" end="${h.nilaiNormal.length - 1}" var="j">
                <c:set var="r" value="${h.nilaiNormal[j]}"/>
                <td class="${r >= 0.999 ? 'cell-hi' : r <= 0.5 ? 'cell-lo' : ''}">
                  <fmt:formatNumber value="${r}" pattern="0.0000"/>
                </td>
              </c:forEach>
            </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </section>

    <%-- LANGKAH PERHITUNGAN --%>
    <section class="section-card">
      <div class="section-head"><h2>📐 Langkah Perhitungan SAW</h2></div>
      <div class="steps-accordion">

        <div class="step-item">
          <div class="step-num">1</div>
          <div class="step-body">
            <h4>Menentukan Kriteria dan Bobot</h4>
            <p>Setiap kriteria diberi bobot (w<sub>j</sub>) yang jumlahnya = 1.
               Kriteria <em>Benefit</em>: nilai lebih besar lebih baik.
               Kriteria <em>Cost</em>: nilai lebih kecil lebih baik.</p>
          </div>
        </div>

        <div class="step-item">
          <div class="step-num">2</div>
          <div class="step-body">
            <h4>Membangun Matriks Keputusan (X)</h4>
            <p>Matriks X berisi nilai tiap alternatif (i) terhadap tiap kriteria (j).</p>
          </div>
        </div>

        <div class="step-item">
          <div class="step-num">3</div>
          <div class="step-body">
            <h4>Normalisasi Matriks → R</h4>
            <p>
              Benefit: <code>r<sub>ij</sub> = x<sub>ij</sub> ÷ max(x<sub>ij</sub>)</code><br/>
              Cost: &nbsp;&nbsp;<code>r<sub>ij</sub> = min(x<sub>ij</sub>) ÷ x<sub>ij</sub></code>
            </p>
          </div>
        </div>

        <div class="step-item">
          <div class="step-num">4</div>
          <div class="step-body">
            <h4>Menghitung Skor Akhir (V<sub>i</sub>)</h4>
            <p><code>V<sub>i</sub> = Σ (w<sub>j</sub> × r<sub>ij</sub>)</code></p>
            <div class="table-wrap" style="margin-top:10px">
              <table class="data-table">
                <thead>
                  <tr><th>Rank</th><th>Alternatif</th><th>V<sub>i</sub></th></tr>
                </thead>
                <tbody>
                  <c:forEach var="h" items="${hasilSAW}">
                  <tr>
                    <td><strong>#${h.ranking}</strong></td>
                    <td>${h.alternatif.namaPaket}</td>
                    <td><code><fmt:formatNumber value="${h.skorAkhir}" pattern="0.0000"/></code></td>
                  </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div class="step-item">
          <div class="step-num">5</div>
          <div class="step-body">
            <h4>Penentuan Alternatif Terbaik</h4>
            <p>Alternatif dengan nilai <strong>V<sub>i</sub> tertinggi</strong> adalah rekomendasi terbaik.</p>
            <p>✅ Rekomendasi: <strong>${terbaik.alternatif.namaPaket}</strong>
               dengan skor <strong><fmt:formatNumber value="${terbaik.skorAkhir}" pattern="0.0000"/></strong></p>
          </div>
        </div>

      </div>
    </section>

  </c:if><%-- end hasilSAW --%>

  <%-- PLACEHOLDER jika belum ada hasil --%>
  <c:if test="${empty hasilSAW}">
  <div class="result-placeholder">
    <div class="ph-icon">📊</div>
    <p>Klik <strong>"Hitung Sekarang"</strong> untuk menjalankan perhitungan SAW.</p>
    <p class="text-muted">Pastikan data kriteria dan alternatif sudah terisi dengan benar.</p>
  </div>
  </c:if>

  <%-- RIWAYAT SESI --%>
  <c:if test="${not empty riwayat}">
  <section class="section-card">
    <div class="section-head">
      <h2>📈 Riwayat Sesi Perhitungan</h2>
    </div>
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr><th>No</th><th>Catatan</th><th>Pemenang</th><th>Skor</th><th>Waktu</th><th>Aksi</th></tr>
        </thead>
        <tbody>
          <c:forEach var="row" items="${riwayat}" varStatus="s">
          <tr>
            <td>${s.index + 1}</td>
            <td>${row[1]}</td>
            <td><span class="badge-gold">🏆 ${row[3]}</span></td>
            <td><fmt:formatNumber value="${row[4]}" pattern="0.0000"/></td>
            <td><fmt:formatDate value="${row[2]}" pattern="dd/MM/yyyy HH:mm"/></td>
            <td>
              <form method="post" action="${pageContext.request.contextPath}/hasil"
                    style="display:inline"
                    onsubmit="return confirm('Hapus sesi ini?')">
                <input type="hidden" name="action" value="hapus"/>
                <input type="hidden" name="id_sesi" value="${row[0]}"/>
                <button type="submit" class="btn-icon btn-danger" title="Hapus">🗑️</button>
              </form>
            </td>
          </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </section>
  </c:if>

</main>

<%@ include file="WEB-INF/jspf/footer.jspf" %>
<script src="${pageContext.request.contextPath}/assets/js/app.js"></script>
</body>
</html>
