/* ============================================================
   app.js  –  SPK Ayam Bakar Asep (SAW Method)
   ============================================================ */

// ─── DATA DEFINITIONS ─────────────────────────────────────────────────────────

const CRITERIA_DEF = [
  { id: 'c1', name: 'Harga',          type: 'cost',    weight: 25, unit: 'Rp' },
  { id: 'c2', name: 'Porsi',          type: 'benefit', weight: 20, unit: 'poin' },
  { id: 'c3', name: 'Rasa',           type: 'benefit', weight: 25, unit: 'poin' },
  { id: 'c4', name: 'Nilai Gizi',     type: 'benefit', weight: 15, unit: 'poin' },
  { id: 'c5', name: 'Kelengkapan',    type: 'benefit', weight: 15, unit: 'poin' },
];

/* Skala Penilaian:
   Harga       → Rp (cost – makin murah makin bagus)
   Porsi       → 1-5 (benefit – makin besar makin baik)
   Rasa        → 1-5 (benefit)
   Nilai Gizi  → 1-5 (benefit)
   Kelengkapan → 1-5 (benefit – seberapa lengkap lauk/pelengkap)
*/

const DEFAULT_ALTERNATIVES = [
  { name: 'Paket Hemat',    values: [22000, 3, 3, 2, 2] },
  { name: 'Paket Spesial',  values: [35000, 4, 5, 3, 4] },
  { name: 'Paket Premium',  values: [55000, 5, 5, 4, 5] },
  { name: 'Paket Diet',     values: [30000, 3, 4, 5, 3] },
  { name: 'Paket Keluarga', values: [85000, 5, 5, 4, 5] },
  { name: 'Paket Jumbo',    values: [70000, 5, 4, 3, 5] },
];

// ─── STATE ────────────────────────────────────────────────────────────────────

let criteria    = JSON.parse(JSON.stringify(CRITERIA_DEF));
let alternatives = JSON.parse(JSON.stringify(DEFAULT_ALTERNATIVES));

// ─── INIT ─────────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
  renderCriteria();
  renderAlternativesTable();
});

// ─── CRITERIA ─────────────────────────────────────────────────────────────────

function renderCriteria() {
  const grid = document.getElementById('criteria-grid');
  grid.innerHTML = criteria.map((c, i) => `
    <div class="criterion-card" id="crit-card-${i}">
      <div class="crit-top">
        <span class="crit-name">${c.name} <span style="color:var(--text3);font-size:11px;">(${c.unit})</span></span>
        <span class="crit-type-badge badge-${c.type}">${c.type === 'benefit' ? '+ Benefit' : '− Cost'}</span>
      </div>
      <div class="crit-weight-row">
        <input
          id="w-input-${i}"
          class="weight-input"
          type="number"
          min="0"
          max="100"
          value="${c.weight}"
          oninput="onWeightChange(${i}, this.value)"
        />
        <span class="weight-pct">%</span>
        <div class="crit-mini-bar">
          <div class="crit-mini-bar-fill" id="mini-bar-${i}" style="width:${c.weight}%"></div>
        </div>
      </div>
    </div>
  `).join('');

  updateWeightStatus();
}

function onWeightChange(idx, val) {
  criteria[idx].weight = parseFloat(val) || 0;
  document.getElementById(`mini-bar-${idx}`).style.width = Math.min(criteria[idx].weight, 100) + '%';
  updateWeightStatus();
}

function updateWeightStatus() {
  const total = criteria.reduce((s, c) => s + (c.weight || 0), 0);
  const pct   = Math.min(total, 100);
  const bar   = document.getElementById('weight-bar');
  const label = document.getElementById('weight-label');

  bar.style.width = pct + '%';
  label.textContent = `Total Bobot: ${total}%`;

  if (total === 100) {
    bar.classList.remove('over');
    label.style.color = 'var(--green)';
  } else if (total > 100) {
    bar.classList.add('over');
    label.style.color = 'var(--red)';
  } else {
    bar.classList.remove('over');
    label.style.color = 'var(--text2)';
  }
}

// ─── ALTERNATIVES TABLE ────────────────────────────────────────────────────────

function renderAlternativesTable() {
  // Header
  const thead = document.getElementById('alt-thead');
  thead.innerHTML = `<tr>
    <th>No</th>
    <th>Paket Menu</th>
    ${criteria.map(c => `<th>${c.name} (${c.unit})</th>`).join('')}
    <th style="text-align:center">Hapus</th>
  </tr>`;

  // Body
  const tbody = document.getElementById('alt-tbody');
  tbody.innerHTML = alternatives.map((alt, i) => `
    <tr id="alt-row-${i}">
      <td style="color:var(--text3);font-weight:600;">${i + 1}</td>
      <td>
        <input
          class="alt-name-input"
          type="text"
          value="${alt.name}"
          oninput="alternatives[${i}].name = this.value"
          placeholder="Nama paket..."
        />
      </td>
      ${criteria.map((c, ci) => `
        <td>
          <input
            type="number"
            min="0"
            value="${alt.values[ci] ?? ''}"
            oninput="alternatives[${i}].values[${ci}] = parseFloat(this.value) || 0"
            placeholder="0"
          />
        </td>
      `).join('')}
      <td style="text-align:center">
        <button class="icon-btn" onclick="removeAlternative(${i})" title="Hapus">🗑️</button>
      </td>
    </tr>
  `).join('');
}

function addAlternative() {
  alternatives.push({
    name: `Paket Baru ${alternatives.length + 1}`,
    values: criteria.map(() => 0)
  });
  renderAlternativesTable();
  showToast('✅ Paket baru ditambahkan');
}

function removeAlternative(idx) {
  if (alternatives.length <= 2) { showToast('⚠️ Minimal 2 alternatif diperlukan'); return; }
  alternatives.splice(idx, 1);
  renderAlternativesTable();
  showToast('🗑️ Paket dihapus');
}

function resetToDefault() {
  criteria     = JSON.parse(JSON.stringify(CRITERIA_DEF));
  alternatives = JSON.parse(JSON.stringify(DEFAULT_ALTERNATIVES));
  renderCriteria();
  renderAlternativesTable();
  document.getElementById('result-content').style.display = 'none';
  document.getElementById('result-placeholder').style.display = 'flex';
  showToast('↺ Data direset ke default');
}

// ─── SAW CALCULATION ─────────────────────────────────────────────────────────

function runSAW() {
  // ── Validation ───────────────────────────────────────────────────────────────
  const totalW = criteria.reduce((s, c) => s + (c.weight || 0), 0);
  if (Math.abs(totalW - 100) > 0.01) {
    showToast(`⚠️ Total bobot = ${totalW}% (harus 100%)`); return;
  }
  if (alternatives.length < 2) {
    showToast('⚠️ Minimal 2 alternatif diperlukan'); return;
  }

  const n  = alternatives.length;
  const m  = criteria.length;

  // ── Build decision matrix X[i][j] ────────────────────────────────────────────
  const X = alternatives.map(alt =>
    criteria.map((_, j) => parseFloat(alt.values[j]) || 0)
  );

  // ── Step 1: Reference values (best per column) ────────────────────────────────
  const refVal = criteria.map((c, j) => {
    const col = X.map(row => row[j]);
    return c.type === 'benefit' ? Math.max(...col) : Math.min(...col);
  });

  // ── Step 2: Normalise R[i][j] ─────────────────────────────────────────────────
  const R = X.map(row =>
    row.map((xij, j) => {
      if (xij === 0 && refVal[j] === 0) return 0;
      return criteria[j].type === 'benefit'
        ? xij / refVal[j]
        : refVal[j] / xij;
    })
  );

  // ── Step 3: Weights ────────────────────────────────────────────────────────────
  const W = criteria.map(c => c.weight / 100);

  // ── Step 4: Final scores V[i] = Σ wj * Rij ────────────────────────────────────
  const V = R.map(row =>
    row.reduce((sum, rij, j) => sum + W[j] * rij, 0)
  );

  // ── Step 5: Sort ───────────────────────────────────────────────────────────────
  const ranked = alternatives.map((alt, i) => ({
    idx: i, name: alt.name, score: V[i], rVec: R[i], xVec: X[i]
  })).sort((a, b) => b.score - a.score);

  // ── Render ────────────────────────────────────────────────────────────────────
  renderResults(ranked, R, X, refVal, W, V);
  document.getElementById('result-placeholder').style.display = 'none';
  document.getElementById('result-content').style.display = 'block';
  document.getElementById('result').scrollIntoView({ behavior: 'smooth', block: 'start' });
  showToast('✅ Perhitungan SAW selesai!');
}

// ─── RENDER RESULTS ───────────────────────────────────────────────────────────

function renderResults(ranked, R, X, refVal, W, V) {
  const best      = ranked[0];
  const maxScore  = best.score;

  // ── Winner card ───────────────────────────────────────────────────────────────
  const priceIdx  = criteria.findIndex(c => c.id === 'c1');
  const priceText = priceIdx >= 0
    ? formatPrice(alternatives[best.idx].values[priceIdx])
    : '';

  document.getElementById('winner-card').innerHTML = `
    <div class="winner-trophy">🏆</div>
    <div class="winner-info">
      <div class="winner-label">🥇 Rekomendasi Terbaik</div>
      <div class="winner-name">${best.name}</div>
      <div class="winner-price">${priceText ? 'Harga: ' + priceText : ''}</div>
    </div>
    <div class="winner-score-box">
      <div class="winner-score-label">Skor Vi</div>
      <div class="winner-score-val">${(best.score).toFixed(4)}</div>
    </div>
  `;

  // ── Ranking table ─────────────────────────────────────────────────────────────
  const tbody = document.getElementById('result-tbody');
  tbody.innerHTML = ranked.map((alt, rank) => {
    const pIdx = criteria.findIndex(c => c.id === 'c1');
    const price = pIdx >= 0 ? formatPrice(alternatives[alt.idx].values[pIdx]) : '-';
    const pct   = (alt.score / maxScore) * 100;
    const rankClass = rank === 0 ? 'rank-1' : rank === 1 ? 'rank-2' : rank === 2 ? 'rank-3' : 'rank-n';
    const tag   = rank === 0
      ? '<span class="tag-best">⭐ Terbaik</span>'
      : rank < 3
        ? '<span class="tag-good">Unggulan</span>'
        : '<span class="tag-avg">Cukup Baik</span>';
    return `
      <tr>
        <td><span class="rank-badge ${rankClass}">${rank + 1}</span></td>
        <td style="font-weight:600">${alt.name}</td>
        <td>${price}</td>
        <td class="score-bar-cell">
          <div class="score-bar-wrap">
            <div class="score-bar-bg">
              <div class="score-bar" style="width:${pct.toFixed(1)}%"></div>
            </div>
            <span style="font-size:13px;font-weight:700;color:var(--text)">${(alt.score).toFixed(4)}</span>
          </div>
        </td>
        <td>${tag}</td>
      </tr>
    `;
  }).join('');

  // ── Normalised matrix ─────────────────────────────────────────────────────────
  const normTable = document.getElementById('norm-table');
  const colMaxR   = criteria.map((_, j) => Math.max(...R.map(row => row[j])));
  normTable.innerHTML = `
    <thead>
      <tr>
        <th>Alternatif</th>
        ${criteria.map(c => `<th>${c.name}</th>`).join('')}
      </tr>
    </thead>
    <tbody>
      ${alternatives.map((alt, i) => `
        <tr>
          <td style="font-weight:600">${alt.name}</td>
          ${R[i].map((r, j) => {
            const hi = Math.abs(r - colMaxR[j]) < 0.0001;
            return `<td class="${hi ? 'norm-hi' : 'norm-lo'}">${r.toFixed(4)}</td>`;
          }).join('')}
        </tr>
      `).join('')}
    </tbody>
    <tfoot>
      <tr style="border-top:1px solid var(--border2)">
        <td style="font-weight:700;color:var(--text2)">Bobot (w)</td>
        ${W.map(w => `<td style="color:var(--gold);font-weight:700">${w.toFixed(2)}</td>`).join('')}
      </tr>
    </tfoot>
  `;

  // ── Step-by-step ──────────────────────────────────────────────────────────────
  const steps = document.getElementById('steps-box');
  steps.innerHTML = `
    <h4>Step 1 – Matriks Keputusan (X)</h4>
    ${buildMatrixHTML(alternatives.map(a => a.name), criteria.map(c => c.name), X,
      (val, ri, ci) => criteria[ci].id === 'c1' ? formatPrice(val) : val)}

    <h4>Step 2 – Nilai Referensi (Best per Kriteria)</h4>
    <table>
      <thead><tr><th>Kriteria</th><th>Jenis</th><th>Nilai Referensi</th></tr></thead>
      <tbody>
        ${criteria.map((c, j) => `
          <tr>
            <td>${c.name}</td>
            <td><span class="crit-type-badge badge-${c.type}" style="font-size:10px">${c.type}</span></td>
            <td><code>${c.id === 'c1' ? formatPrice(refVal[j]) : refVal[j]}</code></td>
          </tr>
        `).join('')}
      </tbody>
    </table>

    <h4>Step 3 – Formula Normalisasi</h4>
    <p>
      Benefit: <code>r<sub>ij</sub> = x<sub>ij</sub> / max(x<sub>ij</sub>)</code>&nbsp;&nbsp;&nbsp;
      Cost: <code>r<sub>ij</sub> = min(x<sub>ij</sub>) / x<sub>ij</sub></code>
    </p>

    <h4>Step 4 – Matriks Ternormalisasi (R) × Bobot</h4>
    ${buildMatrixHTML(
      alternatives.map(a => a.name),
      criteria.map((c, j) => `${c.name} (w=${W[j].toFixed(2)})`),
      R.map(row => row.map((r, j) => (r * W[j]).toFixed(4))),
      v => v
    )}

    <h4>Step 5 – Skor Akhir V<sub>i</sub> = Σ(w<sub>j</sub> · r<sub>ij</sub>)</h4>
    <table>
      <thead><tr><th>Rank</th><th>Alternatif</th><th>Skor (V<sub>i</sub>)</th></tr></thead>
      <tbody>
        ${ranked.map((alt, rank) => `
          <tr>
            <td style="font-weight:700">#${rank + 1}</td>
            <td>${alt.name}</td>
            <td><code>${alt.score.toFixed(4)}</code></td>
          </tr>
        `).join('')}
      </tbody>
    </table>
  `;
}

// ─── HELPERS ──────────────────────────────────────────────────────────────────

function buildMatrixHTML(rowLabels, colLabels, matrix, fmt = v => v) {
  return `
    <table>
      <thead>
        <tr>
          <th>Alternatif</th>
          ${colLabels.map(c => `<th>${c}</th>`).join('')}
        </tr>
      </thead>
      <tbody>
        ${matrix.map((row, i) => `
          <tr>
            <td style="font-weight:600">${rowLabels[i]}</td>
            ${row.map((v, j) => `<td>${fmt(v, i, j)}</td>`).join('')}
          </tr>
        `).join('')}
      </tbody>
    </table>
  `;
}

function formatPrice(val) {
  return 'Rp ' + Number(val).toLocaleString('id-ID');
}

function showToast(msg) {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.classList.add('show');
  setTimeout(() => t.classList.remove('show'), 3000);
}
