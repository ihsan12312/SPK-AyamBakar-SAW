/* ============================================================
   app.js – SPK Ayam Bakar Asep (Client-side UI helpers)
   Pure JavaScript, no framework dependency.
   ============================================================ */

// ─── WEIGHT BAR UPDATE ───────────────────────────────────────
/**
 * Update mini progress bar saat user mengubah input bobot.
 * Dipanggil dari kriteria.jsp: oninput="updateBar(id, value)"
 */
function updateBar(id, val) {
  const bar = document.getElementById('bar-' + id);
  if (bar) {
    const pct = Math.min(Math.max(parseFloat(val) || 0, 0), 100);
    bar.style.width = pct + '%';
  }
}

// ─── MODAL TOGGLE ─────────────────────────────────────────────
/**
 * Tampilkan/sembunyikan modal overlay.
 * @param {string} id - ID elemen modal
 */
function toggleModal(id) {
  const modal = document.getElementById(id);
  if (!modal) return;
  const isHidden = modal.style.display === 'none' || modal.style.display === '';
  modal.style.display  = isHidden ? 'flex' : 'none';
  document.body.style.overflow = isHidden ? 'hidden' : '';
}

// Tutup modal jika klik di luar modal-box
document.querySelectorAll('.modal-overlay').forEach(overlay => {
  overlay.addEventListener('click', function (e) {
    if (e.target === this) {
      this.style.display = 'none';
      document.body.style.overflow = '';
    }
  });
});

// Tutup modal dengan tombol Escape
document.addEventListener('keydown', function (e) {
  if (e.key === 'Escape') {
    document.querySelectorAll('.modal-overlay').forEach(m => {
      m.style.display = 'none';
    });
    document.body.style.overflow = '';
  }
});

// ─── EDIT KRITERIA MODAL ──────────────────────────────────────
/**
 * Isi form modal edit kriteria dengan data yang diklik.
 * Dipanggil dari kriteria.jsp.
 */
function openEditKriteria(id, nama, jenis, bobot, satuan, keterangan) {
  document.getElementById('edit-id').value         = id;
  document.getElementById('edit-nama').value        = nama;
  document.getElementById('edit-jenis').value       = jenis;
  document.getElementById('edit-bobot').value       = bobot;
  document.getElementById('edit-satuan').value      = satuan || '';
  document.getElementById('edit-keterangan').value  = keterangan || '';
  toggleModal('modal-edit');
}

// ─── EDIT ALTERNATIF MODAL ────────────────────────────────────
/**
 * Isi form modal edit paket menu.
 */
function openEditModal(id, nama, deskripsi, harga) {
  document.getElementById('edit-id').value       = id;
  document.getElementById('edit-nama').value     = nama;
  document.getElementById('edit-deskripsi').value = deskripsi || '';
  document.getElementById('edit-harga').value    = harga;
  toggleModal('modal-edit');
}

// ─── NILAI MODAL ──────────────────────────────────────────────
/**
 * Buka modal isian nilai untuk satu alternatif.
 * Pre-fill dari data existingNilai (diberikan oleh JSP sebagai variabel JS).
 */
function openNilaiModal(altId, altName) {
  document.getElementById('nilai-alt-id').value     = altId;
  document.getElementById('nilai-paket-name').textContent = altName;

  // Pre-fill nilai dari data yang ada
  if (typeof existingNilai !== 'undefined' && existingNilai[altId]) {
    const nilaiAlt = existingNilai[altId];
    Object.keys(nilaiAlt).forEach(kritId => {
      const input = document.getElementById('ni-krit-' + kritId);
      if (input) {
        const val = nilaiAlt[kritId];
        input.value = (val && val !== 0) ? val : '';
      }
    });
  }

  toggleModal('modal-nilai');
}

// ─── FLASH MESSAGE AUTO-HIDE ──────────────────────────────────
document.querySelectorAll('.alert').forEach(el => {
  setTimeout(() => {
    el.style.transition = 'opacity .5s ease';
    el.style.opacity = '0';
    setTimeout(() => el.remove(), 600);
  }, 4500);
});

// ─── ACTIVE NAV LINK ──────────────────────────────────────────
(function () {
  const path    = window.location.pathname;
  const navMap  = {
    '/':          'nav-home',
    '/index':     'nav-home',
    '/kriteria':  'nav-kriteria',
    '/alternatif':'nav-alternatif',
    '/hasil':     'nav-hasil',
  };

  // Find matching nav link
  Object.entries(navMap).forEach(([route, navId]) => {
    if (path.endsWith(route)) {
      const el = document.getElementById(navId);
      if (el) el.classList.add('active');
    }
  });
})();

// ─── TABLE SEARCH (optional helper) ──────────────────────────
/**
 * Filter tabel berdasarkan input pencarian.
 * @param {string} inputId - ID elemen input
 * @param {string} tableId - ID elemen table
 */
function filterTable(inputId, tableId) {
  const query = document.getElementById(inputId)
                         .value.toLowerCase();
  const rows  = document.querySelectorAll('#' + tableId + ' tbody tr');
  rows.forEach(row => {
    row.style.display = row.textContent.toLowerCase().includes(query)
      ? '' : 'none';
  });
}
