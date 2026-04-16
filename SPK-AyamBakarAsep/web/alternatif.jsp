<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8"/>
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Alternatif – SPK Ayam Bakar Asep</title>
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
      <h1 class="page-title">🍽️ Manajemen Alternatif Paket Menu</h1>
      <p class="page-desc">Tambah, edit, dan isi nilai tiap paket menu sesuai kriteria yang telah ditentukan.</p>
    </div>
    <button class="btn-primary" onclick="toggleModal('modal-tambah')">+ Tambah Paket</button>
  </div>

  <%-- MATRIKS NILAI --%>
  <section class="section-card">
    <div class="section-head">
      <h2>📊 Matriks Nilai Alternatif</h2>
      <p class="text-muted" style="font-size:13px">Klik <strong>✏️ Nilai</strong> untuk mengisi penilaian per paket menu.</p>
    </div>
    <div class="table-wrap">
      <table class="data-table matrix-table" id="tbl-alternatif">
        <thead>
          <tr>
            <th>No</th>
            <th>Paket Menu</th>
            <th>Harga</th>
            <c:forEach var="k" items="${kriteriaList}">
              <th title="${k.keterangan}">${k.namaKriteria}
                <span class="badge-type badge-${k.jenis}" style="font-size:9px">${k.jenis}</span>
              </th>
            </c:forEach>
            <th>Aksi</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="alt" items="${alternatifList}" varStatus="s">
          <tr>
            <td>${s.index + 1}</td>
            <td><strong>${alt.namaPaket}</strong><br/>
              <small class="text-muted">${alt.deskripsi}</small></td>
            <td><strong>${alt.hargaFormatted}</strong></td>
            <c:forEach var="k" items="${kriteriaList}">
              <td class="nilai-cell">
                <c:set var="val" value="${alt.nilaiMap[k.idKriteria]}"/>
                <c:choose>
                  <c:when test="${not empty val and val != 0}">
                    <span class="nilai-val"><fmt:formatNumber value="${val}" maxFractionDigits="2"/></span>
                  </c:when>
                  <c:otherwise><span class="nilai-empty">–</span></c:otherwise>
                </c:choose>
              </td>
            </c:forEach>
            <td class="action-col">
              <button class="btn-sm btn-secondary"
                      onclick="openNilaiModal(${alt.idAlternatif}, '${alt.namaPaket}')">
                ✏️ Nilai
              </button>
              <button class="btn-icon"
                      onclick="openEditModal(${alt.idAlternatif},'${alt.namaPaket}',
                               '${alt.deskripsi}',${alt.harga})">✏️</button>
              <form method="post" action="${pageContext.request.contextPath}/alternatif"
                    style="display:inline"
                    onsubmit="return confirm('Hapus paket ini?')">
                <input type="hidden" name="action" value="hapus"/>
                <input type="hidden" name="id_alternatif" value="${alt.idAlternatif}"/>
                <button type="submit" class="btn-icon btn-danger">🗑️</button>
              </form>
            </td>
          </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </section>

</main>

<%-- MODAL TAMBAH PAKET --%>
<div id="modal-tambah" class="modal-overlay" style="display:none">
  <div class="modal-box">
    <div class="modal-head">
      <h3>➕ Tambah Paket Menu</h3>
      <button class="modal-close" onclick="toggleModal('modal-tambah')">✕</button>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/alternatif">
      <input type="hidden" name="action" value="tambah"/>
      <div class="form-group">
        <label>Nama Paket</label>
        <input type="text" name="nama_paket" required placeholder="contoh: Paket Super Hemat"/>
      </div>
      <div class="form-group">
        <label>Deskripsi</label>
        <textarea name="deskripsi" rows="2" placeholder="Isi paket..."></textarea>
      </div>
      <div class="form-group">
        <label>Harga (Rp)</label>
        <input type="number" name="harga" min="0" required placeholder="25000"/>
      </div>
      <div class="form-actions">
        <button type="button" class="btn-outline" onclick="toggleModal('modal-tambah')">Batal</button>
        <button type="submit" class="btn-primary">Simpan</button>
      </div>
    </form>
  </div>
</div>

<%-- MODAL EDIT PAKET --%>
<div id="modal-edit" class="modal-overlay" style="display:none">
  <div class="modal-box">
    <div class="modal-head">
      <h3>✏️ Edit Paket Menu</h3>
      <button class="modal-close" onclick="toggleModal('modal-edit')">✕</button>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/alternatif">
      <input type="hidden" name="action" value="update"/>
      <input type="hidden" name="id_alternatif" id="edit-id"/>
      <div class="form-group">
        <label>Nama Paket</label>
        <input type="text" name="nama_paket" id="edit-nama" required/>
      </div>
      <div class="form-group">
        <label>Deskripsi</label>
        <textarea name="deskripsi" id="edit-deskripsi" rows="2"></textarea>
      </div>
      <div class="form-group">
        <label>Harga (Rp)</label>
        <input type="number" name="harga" id="edit-harga" min="0"/>
      </div>
      <div class="form-actions">
        <button type="button" class="btn-outline" onclick="toggleModal('modal-edit')">Batal</button>
        <button type="submit" class="btn-primary">Perbarui</button>
      </div>
    </form>
  </div>
</div>

<%-- MODAL ISIAN NILAI --%>
<div id="modal-nilai" class="modal-overlay" style="display:none">
  <div class="modal-box modal-lg">
    <div class="modal-head">
      <h3>📝 Isi Nilai – <span id="nilai-paket-name"></span></h3>
      <button class="modal-close" onclick="toggleModal('modal-nilai')">✕</button>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/alternatif">
      <input type="hidden" name="action" value="nilai"/>
      <input type="hidden" name="id_alternatif" id="nilai-alt-id"/>
      <div class="nilai-form-grid">
        <c:forEach var="k" items="${kriteriaList}">
        <div class="form-group">
          <label>
            ${k.namaKriteria}
            <span class="badge-type badge-${k.jenis}">${k.jenis}</span>
            <small class="text-muted">(${k.satuan})</small>
          </label>
          <input type="number" name="nilai_${k.idKriteria}"
                 step="any" min="0"
                 placeholder="Masukkan nilai..."
                 class="nilai-input" id="ni-krit-${k.idKriteria}"/>
          <small class="text-muted">${k.keterangan}</small>
        </div>
        </c:forEach>
      </div>
      <div class="form-actions">
        <button type="button" class="btn-outline" onclick="toggleModal('modal-nilai')">Batal</button>
        <button type="submit" class="btn-primary">💾 Simpan Nilai</button>
      </div>
    </form>
  </div>
</div>

<%@ include file="WEB-INF/jspf/footer.jspf" %>

<%-- Pass existing nilai to JS for pre-filling --%>
<script>
const existingNilai = {
  <c:forEach var="alt" items="${alternatifList}">
  "${alt.idAlternatif}": {
    <c:forEach var="k" items="${kriteriaList}">
    "${k.idKriteria}": ${not empty alt.nilaiMap[k.idKriteria] ? alt.nilaiMap[k.idKriteria] : 0},
    </c:forEach>
  },
  </c:forEach>
};
</script>
<script src="${pageContext.request.contextPath}/assets/js/app.js"></script>
</body>
</html>
