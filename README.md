# Sistem Manajemen Gudang Koperasi

Aplikasi desktop manajemen gudang berbasis **Java Swing** dengan database **MySQL**, dibangun menggunakan **NetBeans IDE** (Ant build, tanpa Maven).

---

## Tampilan

| Halaman | Deskripsi |
|---------|-----------|
| Login / Register | Autentikasi pengguna dengan CardLayout |
| Dashboard | Ringkasan telemetri (stat cards + placeholder chart) |
| Transaksi Masuk | Form input barang masuk + riwayat tabel |
| Kategori | CRUD data kategori barang |
| Vendor | CRUD data pemasok |
| Barang | CRUD master barang dengan indikator stok menipis |

---

## Prasyarat

| Kebutuhan | Versi |
|-----------|-------|
| JDK | 17 atau lebih baru |
| NetBeans IDE | 18+ |
| MySQL Server | 5.7 / 8.x (via XAMPP/WampServer/standalone) |
| MySQL Connector/J | 8.x (file `.jar`) |

---

## Struktur Proyek

```
Gudang_GUI/
├── db_koperasi_gudang.sql          ← Script SQL: buat database + tabel + data awal
└── Stok_Gudang/                    ← Proyek NetBeans
    ├── build.xml
    ├── manifest.mf
    ├── nbproject/
    └── src/
        └── stok_gudang/
            ├── Stok_Gudang.java    ← Entry point (main class)
            ├── Koneksi.java        ← Helper koneksi JDBC (singleton)
            ├── AuthFrame.java      ← Halaman Login & Register
            └── MainFrame.java      ← Dashboard & semua panel CRUD
```

---

## Instalasi & Setup

### 1. Import Database

1. Pastikan MySQL sudah berjalan (aktifkan XAMPP / WampServer).
2. Buka **phpMyAdmin** → `http://localhost/phpmyadmin`.
3. Klik **New** → buat database bernama `db_koperasi_gudang`.
4. Klik database tersebut → tab **Import**.
5. Pilih file `db_koperasi_gudang.sql` dari folder ini → klik **Import**.

### 2. Tambahkan MySQL Connector ke NetBeans

1. Unduh **MySQL Connector/J** dari [mysql.com/downloads/connector/j](https://dev.mysql.com/downloads/connector/j/) (pilih *Platform Independent*, format ZIP).
2. Ekstrak dan simpan file `.jar`-nya.
3. Di NetBeans, klik kanan project **Stok_Gudang** → **Properties** → **Libraries** → **Add JAR/Folder** → pilih file `.jar` tadi.

### 3. Konfigurasi Koneksi

Buka `src/stok_gudang/Koneksi.java` dan sesuaikan nilai berikut:

```java
private static final String DB_URL      = "jdbc:mysql://localhost:3306/db_koperasi_gudang";
private static final String DB_USER     = "root";
private static final String DB_PASSWORD = "";   // isi jika MySQL Anda berpassword
```

### 4. Jalankan Aplikasi

1. Pastikan **Main Class** sudah diset ke `stok_gudang.Stok_Gudang`:
   - Klik kanan project → **Properties** → **Run** → **Main Class** → ketik `stok_gudang.Stok_Gudang`.
2. Tekan **F6** (atau klik tombol Run ▶).

---

## Akun Default

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | admin |
| `operator` | `operator123` | operator |

> Anda juga dapat membuat akun baru melalui panel **Register** di layar login.

---

## Fitur Lengkap

### Autentikasi (`AuthFrame.java`)
- **Login** — verifikasi ke `tabel_user` via JDBC PreparedStatement.
- **Register** — INSERT akun baru dengan role `operator`.
- Validasi input + pesan error/sukses inline.
- Transisi antar panel Login ↔ Register menggunakan **CardLayout** tanpa membuka jendela baru.

### Dashboard
- Stat cards real-time: Total Barang, Total Vendor, Transaksi Masuk bulan ini, Stok Menipis.
- Data dimuat di background thread (**SwingWorker**) agar UI tetap responsif.
- Dua placeholder panel siap diisi library chart (misal **JFreeChart**).

### Transaksi Masuk
- Form input: Tanggal, Kode Barang (combo dari DB), Jumlah, Vendor (combo dari DB).
- **Auto-fill** nama barang saat kode dipilih.
- Simpan: INSERT ke `tabel_transaksi_masuk` + UPDATE `stok` di `tabel_barang` dalam satu **DB Transaction** — otomatis rollback jika ada kegagalan.
- Tabel riwayat dimuat via SwingWorker dengan tombol Refresh.

### Kategori, Vendor, Barang
- **Tambah**: isi form → klik Simpan.
- **Edit**: klik baris di tabel → form terisi otomatis → ubah → Simpan.
- **Hapus**: pilih baris → klik Hapus Dipilih → dialog konfirmasi.
- Panel **Barang**: baris berwarna oranye gelap jika `stok < stok_minimum` sebagai indikator visual.

---

## Desain & Tema

- **Dark Mode** penuh dengan palet warna kustom (tidak menggunakan LookAndFeel bawaan).
- Warna utama: `#121212` (background), `#E00613` (aksen merah), putih bersih untuk teks.
- Tombol di-render manual via `paintComponent` override (efek hover + press).
- Sidebar bergaya dashboard telemetri dengan garis merah aktif di sebelah kiri.
- Font: **Segoe UI** di seluruh aplikasi.

---

## Pengembangan Lanjutan

Beberapa area yang siap dikembangkan:

- [ ] Integrasi **JFreeChart** untuk grafik arus barang dan distribusi kategori di Dashboard.
- [ ] **Transaksi Keluar** — panel stok keluar gudang.
- [ ] **Laporan PDF** menggunakan iText atau JasperReports.
- [ ] **Hashing password** (BCrypt / SHA-256) untuk keamanan produksi.
- [ ] **Role-based access** — batasi menu berdasarkan role `admin` vs `operator`.
- [ ] Panel **Stok Opname** dan rekonsiliasi stok.

---

## Lisensi

Proyek ini dibuat untuk keperluan akademik / internal koperasi.
Bebas dimodifikasi sesuai kebutuhan.
