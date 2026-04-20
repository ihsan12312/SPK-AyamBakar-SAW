<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8"/>
  <title>Error – SPK Ayam Bakar Asep</title>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;700;800&display=swap" rel="stylesheet"/>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css"/>
</head>
<body>
<%@ include file="WEB-INF/jspf/navbar.jspf" %>
<main class="main-wrap">
  <div class="result-placeholder" style="min-height:50vh">
    <div class="ph-icon">⚠️</div>
    <h2 style="color:var(--red)">Terjadi Kesalahan (Status: <%= request.getAttribute("javax.servlet.error.status_code") %>)</h2>
    <p>Halaman tidak ditemukan atau terjadi error di server.</p>
    <div style="background:#2d2d2d; padding:10px; border-radius:5px; margin-top:10px; font-size:12px; color:#ff9999; text-align:left; overflow:auto;">
      <strong>Error Message:</strong> <%= request.getAttribute("javax.servlet.error.message") %><br/>
      <strong>Exception:</strong> <%= request.getAttribute("javax.servlet.error.exception") %>
    </div>
    <a href="${pageContext.request.contextPath}/" class="btn-primary" style="margin-top:14px">
      🏠 Kembali ke Dashboard
    </a>
  </div>
</main>
<%@ include file="WEB-INF/jspf/footer.jspf" %>
</body>
</html>
