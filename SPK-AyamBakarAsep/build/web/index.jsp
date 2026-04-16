<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Dashboard – SPK Ayam Bakar Asep</title>
  <link rel="preconnect" href="https://fonts.googleapis.com"/>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@300;400;500;600;700;800&display=swap" rel="stylesheet"/>
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
  <c:if test="${not empty requestScope.error}">
    <div class="alert alert-error">${requestScope.error}</div>
  </c:if>

  <%-- HERO --%>
  <section class="hero">
    <div class="hero-content">
      <div class="hero-badge">Metode Simple Additive Weighting (SAW)</div>
      <h1>Sistem Pendukung Keputusan<br/><span class="gradient-text">Paket Menu Terbaik</span></h1>
      <p class="hero-desc">
        Temukan paket menu ayam bakar terbaik berdasarkan 5 kriteria penilaian
        menggunakan metode SAW yang akurat dan transparan.
      </p>
      <div class="hero-actions">
        <a href="${pageContext.request.contextPath}/hasil" class="btn-primary" id="btn-hitung">
          ⚡ Hitung Rekomendasi
        </a>
        <a href="${pageContext.request.contextPath}/kriteria" class="btn-outline-hero">
          ⚙️ Kelola Kriteria
        </a>
      </div>
    </div>
    <div class="hero-chips">
      <div class="chip chip-orange">🍗 Paket Hemat</div>
      <div class="chip chip-gold">🔥 Paket Spesial</div>
      <div class="chip chip-yellow">👑 Paket Premium</div>
      <div class="chip chip-green">🌿 Paket Diet</div>
      <div class="chip chip-blue">👨‍👩‍👦 Paket Keluarga</div>
    </div>
  </section>

  <%-- STAT CARDS --%>
  <section class="stats-grid">
    <div class="stat-card">
      <div class="stat-icon">📋</div>
      <div class="stat-body">
        <div class="stat-value">${jumlahKriteria}</div>
        <div class="stat-label">Kriteria Aktif</div>
      </div>
    </div>
    <div class="stat-card">
      <div class="stat-icon">🍽️</div>
      <div class="stat-body">
        <div class="stat-value">${jumlahAlternatif}</div>
        <div class="stat-label">Paket Menu</div>
      </div>
    </div>
    <div class="stat-card ${bobotValid ? 'stat-ok' : 'stat-warn'}">
      <div class="stat-icon">${bobotValid ? '✅' : '⚠️'}</div>
      <div class="stat-body">
        <div class="stat-value"><fmt:formatNumber value="${totalBobot}" maxFractionDigits="1"/>%</div>
        <div class="stat-label">Total Bobot ${bobotValid ? '(Valid)' : '(Belum 100%)'}</div>
      </div>
    </div>
    <div class="stat-card">
      <div class="stat-icon">📊</div>
      <div class="stat-body">
        <div class="stat-value">${riwayat.size()}</div>
        <div class="stat-label">Sesi Perhitungan</div>
      </div>
    </div>
  </section>

  <%-- RIWAYAT --%>
  <c:if test="${not empty riwayat}">
  <section class="section-card">
    <div class="section-head">
      <h2>📈 Riwayat Perhitungan Terakhir</h2>
    </div>
    <div class="table-wrap">
      <table class="data-table">
        <thead>
          <tr>
            <th>No</th><th>Sesi</th><th>Pemenang</th>
            <th>Skor</th><th>Waktu</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="row" items="${riwayat}" varStatus="s">
          <tr>
            <td>${s.index + 1}</td>
            <td>${row[1]}</td>
            <td><span class="badge-gold">🏆 ${row[3]}</span></td>
            <td><fmt:formatNumber value="${row[4]}" pattern="0.0000"/></td>
            <td><fmt:formatDate value="${row[2]}" pattern="dd/MM/yyyy HH:mm"/></td>
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
