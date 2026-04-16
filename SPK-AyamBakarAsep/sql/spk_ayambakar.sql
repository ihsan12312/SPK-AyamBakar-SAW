-- ============================================================
-- DATABASE: spk_ayambakar
-- Sistem Pendukung Keputusan - Ayam Bakar Asep
-- Metode: Simple Additive Weighting (SAW)
-- ============================================================

CREATE DATABASE IF NOT EXISTS spk_ayambakar
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE spk_ayambakar;

-- ─── TABEL KRITERIA ──────────────────────────────────────────
-- Menyimpan kriteria penilaian beserta bobot dan jenisnya.
-- Jenis: 'benefit' = lebih besar lebih baik
--        'cost'    = lebih kecil lebih baik
CREATE TABLE IF NOT EXISTS tb_kriteria (
    id_kriteria  INT AUTO_INCREMENT PRIMARY KEY,
    nama_kriteria VARCHAR(100)  NOT NULL,
    jenis         ENUM('benefit','cost') NOT NULL DEFAULT 'benefit',
    bobot         DECIMAL(5,2)  NOT NULL DEFAULT 0.00,  -- nilai 0-100 (persen)
    satuan        VARCHAR(30)   NOT NULL DEFAULT 'poin',
    keterangan    TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ─── TABEL ALTERNATIF ────────────────────────────────────────
-- Menyimpan nama paket menu yang akan dievaluasi.
CREATE TABLE IF NOT EXISTS tb_alternatif (
    id_alternatif INT AUTO_INCREMENT PRIMARY KEY,
    nama_paket    VARCHAR(150)  NOT NULL,
    deskripsi     TEXT,
    harga         INT           NOT NULL DEFAULT 0,
    is_active     TINYINT(1)    NOT NULL DEFAULT 1,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ─── TABEL NILAI ALTERNATIF ───────────────────────────────────
-- Menyimpan nilai (skor) tiap alternatif terhadap tiap kriteria.
CREATE TABLE IF NOT EXISTS tb_nilai (
    id_nilai      INT AUTO_INCREMENT PRIMARY KEY,
    id_alternatif INT NOT NULL,
    id_kriteria   INT NOT NULL,
    nilai         DECIMAL(12,4) NOT NULL DEFAULT 0,
    UNIQUE KEY uq_alt_krit (id_alternatif, id_kriteria),
    FOREIGN KEY (id_alternatif) REFERENCES tb_alternatif(id_alternatif) ON DELETE CASCADE,
    FOREIGN KEY (id_kriteria)   REFERENCES tb_kriteria(id_kriteria)     ON DELETE CASCADE
) ENGINE=InnoDB;

-- ─── TABEL SESI PERHITUNGAN ──────────────────────────────────
-- Menyimpan log tiap kali perhitungan SAW dijalankan.
CREATE TABLE IF NOT EXISTS tb_sesi (
    id_sesi    INT AUTO_INCREMENT PRIMARY KEY,
    catatan    VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ─── TABEL HASIL SAW ─────────────────────────────────────────
-- Menyimpan ranking hasil perhitungan SAW per sesi.
CREATE TABLE IF NOT EXISTS tb_hasil (
    id_hasil      INT AUTO_INCREMENT PRIMARY KEY,
    id_sesi       INT NOT NULL,
    id_alternatif INT NOT NULL,
    skor_akhir    DECIMAL(8,6)  NOT NULL DEFAULT 0,
    ranking       INT           NOT NULL DEFAULT 0,
    FOREIGN KEY (id_sesi)       REFERENCES tb_sesi(id_sesi)             ON DELETE CASCADE,
    FOREIGN KEY (id_alternatif) REFERENCES tb_alternatif(id_alternatif) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ============================================================
-- DATA AWAL (SEED DATA)
-- ============================================================

-- Kriteria
INSERT INTO tb_kriteria (nama_kriteria, jenis, bobot, satuan, keterangan) VALUES
('Harga',         'cost',    25, 'Rupiah', 'Harga paket menu dalam rupiah'),
('Porsi',         'benefit', 20, 'Poin',   'Besar porsi makanan (skala 1-5)'),
('Rasa',          'benefit', 25, 'Poin',   'Cita rasa masakan (skala 1-5)'),
('Nilai Gizi',    'benefit', 15, 'Poin',   'Kandungan gizi menu (skala 1-5)'),
('Kelengkapan',   'benefit', 15, 'Poin',   'Kelengkapan lauk-pauk (skala 1-5)');

-- Alternatif Paket Menu
INSERT INTO tb_alternatif (nama_paket, deskripsi, harga) VALUES
('Paket Hemat',    '1 potong ayam bakar + nasi + teh',                          22000),
('Paket Spesial',  '2 potong ayam bakar + nasi + lalapan + sambal + es teh',    35000),
('Paket Premium',  '2 potong ayam bakar + nasi + sup + lalapan + jus + dessert',55000),
('Paket Diet',     '1 potong ayam bakar tanpa nasi + salad + air mineral',       30000),
('Paket Keluarga', '5 potong ayam bakar + 4 nasi + lalapan + 4 minuman',         85000),
('Paket Jumbo',    '3 potong ayam bakar + 2 nasi + sup + lalapan + 2 jus',       70000);

-- Nilai tiap Alternatif per Kriteria
-- Format: (id_alternatif, id_kriteria, nilai)
-- Kriteria: 1=Harga, 2=Porsi, 3=Rasa, 4=NilaiGizi, 5=Kelengkapan
INSERT INTO tb_nilai (id_alternatif, id_kriteria, nilai) VALUES
-- Paket Hemat
(1,1,22000),(1,2,3),(1,3,3),(1,4,2),(1,5,2),
-- Paket Spesial
(2,1,35000),(2,2,4),(2,3,5),(2,4,3),(2,5,4),
-- Paket Premium
(3,1,55000),(3,2,5),(3,3,5),(3,4,4),(3,5,5),
-- Paket Diet
(4,1,30000),(4,2,3),(4,3,4),(4,4,5),(4,5,3),
-- Paket Keluarga
(5,1,85000),(5,2,5),(5,3,5),(5,4,4),(5,5,5),
-- Paket Jumbo
(6,1,70000),(6,2,5),(6,3,4),(6,4,3),(6,5,5);
