# SPK Ayam Bakar Asep
### Sistem Pendukung Keputusan – Metode Simple Additive Weighting (SAW)
**Java Web Application · MySQL · NetBeans · Apache Tomcat**

---

## 📁 Struktur Proyek

```
SPK-AyamBakarAsep/
│
├── nbproject/                      ← Konfigurasi NetBeans
│   ├── project.xml
│   └── project.properties
│
├── src/java/                       ← Source Code Java
│   ├── config/
│   │   └── DBConnection.java       ← Koneksi singleton ke MySQL
│   │
│   ├── model/                      ← POJO / Entity
│   │   ├── Kriteria.java
│   │   ├── Alternatif.java
│   │   └── HasilSAW.java
│   │
│   ├── dao/                        ← Data Access Object (CRUD MySQL)
│   │   ├── KriteriaDAO.java
│   │   ├── AlternatifDAO.java
│   │   └── HasilDAO.java
│   │
│   ├── util/
│   │   └── SAWCalculator.java      ← Algoritma SAW (murni Java)
│   │
│   └── controller/                 ← Servlet (HTTP Controller)
│       ├── IndexServlet.java       → URL: /
│       ├── KriteriaServlet.java    → URL: /kriteria
│       ├── AlternatifServlet.java  → URL: /alternatif
│       └── HasilServlet.java       → URL: /hasil
│
├── web/                            ← Frontend (JSP + Aset)
│   ├── WEB-INF/
│   │   ├── web.xml                 ← Konfigurasi Servlet & error page
│   │   └── jspf/
│   │       ├── navbar.jspf         ← Navigasi (shared)
│   │       └── footer.jspf         ← Footer (shared)
│   │
│   ├── assets/
│   │   ├── css/style.css           ← Desain dark-theme modern
│   │   └── js/app.js               ← UI helpers (modal, bar, dll)
│   │
│   ├── index.jsp                   ← Dashboard
│   ├── kriteria.jsp                ← Kelola Kriteria
│   ├── alternatif.jsp              ← Kelola Paket Menu
│   ├── hasil.jsp                   ← Hasil SAW + Ranking
│   └── error.jsp                   ← Halaman Error
│
└── sql/
    └── spk_ayambakar.sql           ← Script buat database + seed data
```

---

## 🗄️ Struktur Database

| Tabel           | Fungsi                                              |
|-----------------|-----------------------------------------------------|
| `tb_kriteria`   | Kriteria penilaian (nama, jenis, bobot)             |
| `tb_alternatif` | Paket menu yang dievaluasi                          |
| `tb_nilai`      | Nilai tiap alternatif per kriteria (matriks X)      |
| `tb_sesi`       | Log tiap kali perhitungan SAW dijalankan            |
| `tb_hasil`      | Ranking hasil SAW tersimpan per sesi                |

---

## ⚙️ Cara Menjalankan

### 1. Persiapan Database
1. Buka **XAMPP** → Start **MySQL**
2. Buka **phpMyAdmin** → klik **Import**
3. Pilih file `sql/spk_ayambakar.sql` → klik **Go**

### 2. Buka di NetBeans
1. **File → Open Project** → pilih folder `SPK-AyamBakarAsep`
2. Tambahkan library **MySQL Connector/J** (mysql-connector-j-8.x.jar):
   - Klik kanan project → **Properties** → **Libraries** → **Add JAR/Folder**
   - Download dari: https://dev.mysql.com/downloads/connector/j/
3. Tambahkan library **JSTL** (jstl-1.2.jar) jika belum ada
4. Konfigurasikan Tomcat: **Tools → Servers → Add Server → Tomcat**

### 3. Jalankan
- Klik kanan project → **Run**
- Buka browser: `http://localhost:8080/SPK-AyamBakarAsep/`

---

## 🔧 Konfigurasi Koneksi DB

Edit file: `src/java/config/DBConnection.java`

```java
private static final String URL      = "jdbc:mysql://localhost:3306/spk_ayambakar...";
private static final String USERNAME = "root";
private static final String PASSWORD = "";   // ← ganti sesuai password MySQL Anda
```

---

## 📐 Algoritma SAW (5 Langkah)

1. **Matriks Keputusan X** — nilai tiap alternatif per kriteria
2. **Nilai Referensi** — max (benefit) atau min (cost) per kolom
3. **Normalisasi R** — `r_ij = x_ij/max` (benefit) atau `min/x_ij` (cost)
4. **Skor Akhir V_i** — `V_i = Σ(w_j × r_ij)`
5. **Ranking** — urutkan V_i secara menurun

---

## 🍗 Kriteria Default

| Kriteria    | Jenis   | Bobot |
|-------------|---------|-------|
| Harga       | Cost    | 25%   |
| Porsi       | Benefit | 20%   |
| Rasa        | Benefit | 25%   |
| Nilai Gizi  | Benefit | 15%   |
| Kelengkapan | Benefit | 15%   |

---

*© 2026 · SPK Ayam Bakar Asep · Metode SAW*
