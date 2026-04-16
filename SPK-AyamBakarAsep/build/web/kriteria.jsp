<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Kriteria – SPK Ayam Bakar Asep</title>
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
      <h1 class="page-title">📋 Manajemen Kriteria</h1>
      <p class="page-desc">Kelola kriteria penilaian dan bobot kepentingan. Total bobot harus = 100%.</p>
    </div>
  </div>

  <%-- STATUS BOBOT --%>
  <div class="bobot-status ${bobotValid ? 'bobot-ok' : 'bobot-warn'}">
    <span class="bobot-icon">${bobotValid ? '✅' : '⚠️'}</span>
    <span>Total Bobot: <strong><fmt:formatNumber value="${totalBobot}" maxFractionDigits="2"/>%</strong>
      — ${bobotValid ? 'Valid. Siap untuk perhitungan SAW.' : 'Belum valid. Sesuaikan hingga 100%.'}
    </span>
  </div>

  <%-- UPDATE BOBOT FORM --%>
  <section class="section-card">
    <div class="section-head">
      <h2>⚖️ Bobot Kriteria</h2>
      <button class="btn-sm btn-primary" form="form-bobot" type="submit">💾 Simpan Bobot</button>
    </div>
    <form id="form-bobot" method="post" action="${pageContext.request.contextPath}/kriteria">
      <input type="hidden" name="action" value="bobot"/>
      <div class="kriteria-bobot-grid">
        <c:forEach var="k" items="${kriteriaList}">
        <div class="bobot-card">
          <div class="bobot-top">
            <span class="bobot-name">${k.namaKriteria}</span>
            <span class="badge-type badge-${k.jenis}">${k.jenis == 'benefit' ? '+ Benefit' : '− Cost'}</span>
          </div>
          <div class="bobot-input-row">
            <input type="number" name="bobot_${k.idKriteria}"
                   id="bobot-${k.idKriteria}"
                   value="${k.bobot}" min="0" max="100" step="0.01"
                   class="weight-input"
                   oninput="updateBar(${k.idKriteria}, this.value)"/>
            <span class="bobot-pct">%</span>
            <div class="mini-bar">
              <div class="mini-bar-fill" id="bar-${k.idKriteria}"
                   style="width:${k.bobot}%"></div>
            </div>
          </div>
          <p class="bobot-keterangan">${empty k.keterangan ? '–' : k.keterangan}</p>
        </div>
        </c:forEach>
      </div>
    </form>
  </section>

  <%-- TABEL KRITERIA --%>
  <section class="section-card">
    <div class="section-head">
      <h2>📋 Daftar Kriteria</h2>
      <button class="btn-sm btn-secondary" onclick="toggleModal('modal-tambah')">+ Tambah Kriteria</button>
    </div>
    <div class="table-wrap">
      <table class="data-table" id="tbl-kriteria">
        <thead>
          <tr>
            <th>No</th><th>Nama Kriteria</th><th>Jenis</th>
            <th>Bobot</th><th>Satuan</th><th>Keterangan</th><th>Aksi</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="k" items="${kriteriaList}" varStatus="s">
          <tr>
            <td>${s.index + 1}</td>
            <td><strong>${k.namaKriteria}</strong></td>
            <td><span class="badge-type badge-${k.jenis}">${k.jenis == 'benefit' ? '+ Benefit' : '− Cost'}</span></td>
            <td><fmt:formatNumber value="${k.bobot}" maxFractionDigits="2"/>%</td>
            <td>${k.satuan}</td>
            <td class="text-muted">${empty k.keterangan ? '–' : k.keterangan}</td>
            <td class="action-col">
              <button class="btn-icon" title="Edit"
                onclick="openEditKriteria(${k.idKriteria},'${k.namaKriteria}',
                  '${k.jenis}',${k.bobot},'${k.satuan}','${k.keterangan}')">✏️</button>
              <form method="post" action="${pageContext.request.contextPath}/kriteria"
                    style="display:inline"
                    onsubmit="return confirm('Hapus kriteria ini?')">
                <input type="hidden" name="action" value="hapus"/>
                <input type="hidden" name="id_kriteria" value="${k.idKriteria}"/>
                <button type="submit" class="btn-icon btn-danger" title="Hapus">🗑️</button>
              </form>
            </td>
          </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </section>

</main>

<%-- MODAL TAMBAH --%>
<div id="modal-tambah" class="modal-overlay" style="display:none">
  <div class="modal-box">
    <div class="modal-head">
      <h3>➕ Tambah Kriteria Baru</h3>
      <button class="modal-close" onclick="toggleModal('modal-tambah')">✕</button>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/kriteria">
      <input type="hidden" name="action" value="tambah"/>
      <div class="form-group">
        <label>Nama Kriteria</label>
        <input type="text" name="nama_kriteria" required placeholder="contoh: Kelezatan"/>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Jenis</label>
          <select name="jenis">
            <option value="benefit">Benefit (+ lebih baik)</option>
            <option value="cost">Cost (− lebih kecil lebih baik)</option>
          </select>
        </div>
        <div class="form-group">
          <label>Bobot (%)</label>
          <input type="number" name="bobot" min="0" max="100" step="0.01" value="0" required/>
        </div>
      </div>
      <div class="form-group">
        <label>Satuan</label>
        <input type="text" name="satuan" placeholder="poin / Rupiah / gram"/>
      </div>
      <div class="form-group">
        <label>Keterangan</label>
        <textarea name="keterangan" rows="2" placeholder="Deskripsi singkat..."></textarea>
      </div>
      <div class="form-actions">
        <button type="button" class="btn-outline" onclick="toggleModal('modal-tambah')">Batal</button>
        <button type="submit" class="btn-primary">Simpan</button>
      </div>
    </form>
  </div>
</div>

<%-- MODAL EDIT --%>
<div id="modal-edit" class="modal-overlay" style="display:none">
  <div class="modal-box">
    <div class="modal-head">
      <h3>✏️ Edit Kriteria</h3>
      <button class="modal-close" onclick="toggleModal('modal-edit')">✕</button>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/kriteria">
      <input type="hidden" name="action" value="update"/>
      <input type="hidden" name="id_kriteria" id="edit-id"/>
      <div class="form-group">
        <label>Nama Kriteria</label>
        <input type="text" name="nama_kriteria" id="edit-nama" required/>
      </div>
      <div class="form-row">
        <div class="form-group">
          <label>Jenis</label>
          <select name="jenis" id="edit-jenis">
            <option value="benefit">Benefit</option>
            <option value="cost">Cost</option>
          </select>
        </div>
        <div class="form-group">
          <label>Bobot (%)</label>
          <input type="number" name="bobot" id="edit-bobot" min="0" max="100" step="0.01"/>
        </div>
      </div>
      <div class="form-group">
        <label>Satuan</label>
        <input type="text" name="satuan" id="edit-satuan"/>
      </div>
      <div class="form-group">
        <label>Keterangan</label>
        <textarea name="keterangan" id="edit-keterangan" rows="2"></textarea>
      </div>
      <div class="form-actions">
        <button type="button" class="btn-outline" onclick="toggleModal('modal-edit')">Batal</button>
        <button type="submit" class="btn-primary">Perbarui</button>
      </div>
    </form>
  </div>
</div>

<%@ include file="WEB-INF/jspf/footer.jspf" %>
<script src="${pageContext.request.contextPath}/assets/js/app.js"></script>
</body>
</html>
