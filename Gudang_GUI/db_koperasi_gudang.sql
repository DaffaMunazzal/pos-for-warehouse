CREATE DATABASE IF NOT EXISTS `db_koperasi_gudang` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `db_koperasi_gudang`;

CREATE TABLE IF NOT EXISTS `tabel_user` (
  `id_user` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL UNIQUE,
  `password` varchar(255) NOT NULL,
  `role` enum('admin','operator') NOT NULL DEFAULT 'operator',
  PRIMARY KEY (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tabel_kategori` (
  `id_kategori` int(11) NOT NULL AUTO_INCREMENT,
  `nama_kategori` varchar(100) NOT NULL UNIQUE,
  PRIMARY KEY (`id_kategori`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tabel_vendor` (
  `id_vendor` int(11) NOT NULL AUTO_INCREMENT,
  `nama_vendor` varchar(100) NOT NULL,
  `alamat` text DEFAULT NULL,
  `telepon` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id_vendor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tabel_barang` (
  `kode_barang` varchar(20) NOT NULL,
  `nama_barang` varchar(150) NOT NULL,
  `id_kategori` int(11) DEFAULT NULL,
  `stok` int(11) NOT NULL DEFAULT 0,
  `stok_minimum` int(11) NOT NULL DEFAULT 5,
  `satuan` varchar(20) DEFAULT 'pcs',
  PRIMARY KEY (`kode_barang`),
  KEY `fk_barang_kategori` (`id_kategori`),
  CONSTRAINT `fk_barang_kategori` FOREIGN KEY (`id_kategori`) REFERENCES `tabel_kategori` (`id_kategori`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabel: tabel_transaksi_masuk
CREATE TABLE IF NOT EXISTS `tabel_transaksi_masuk` (
  `id_transaksi` int(11) NOT NULL AUTO_INCREMENT,
  `tanggal` date NOT NULL DEFAULT curdate(),
  `kode_barang` varchar(20) NOT NULL,
  `jumlah` int(11) NOT NULL,
  `id_vendor` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_transaksi`),
  KEY `fk_transaksi_barang` (`kode_barang`),
  KEY `fk_transaksi_vendor` (`id_vendor`),
  CONSTRAINT `fk_transaksi_barang` FOREIGN KEY (`kode_barang`) REFERENCES `tabel_barang` (`kode_barang`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_transaksi_vendor` FOREIGN KEY (`id_vendor`) REFERENCES `tabel_vendor` (`id_vendor`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `tabel_user` (`username`, `password`, `role`) VALUES
('admin', 'admin123', 'admin'),
('operator', 'operator123', 'operator');

-- Data Kategori
INSERT INTO `tabel_kategori` (`nama_kategori`) VALUES
('Elektronik'),
('ATK'),
('Konsumsi'),
('Peralatan Kantor');

-- Data Vendor
INSERT INTO `tabel_vendor` (`nama_vendor`, `alamat`, `telepon`) VALUES
('PT. Sumber Makmur', 'Jl. Industri No. 12, Jakarta', '021-5551234'),
('CV. ATK Jaya Utama', 'Jl. Sudirman No. 45, Bandung', '022-4449876'),
('Distributor Sembako Nusantara', 'Jl. Merdeka No. 9, Surabaya', '031-3334567');

-- Data Barang (Stok awal)
INSERT INTO `tabel_barang` (`kode_barang`, `nama_barang`, `id_kategori`, `stok`, `stok_minimum`, `satuan`) VALUES
('BRG-001', 'Laptop ASUS Core i5', 1, 12, 3, 'unit'),
('BRG-002', 'Kertas A4 Sinar Dunia', 2, 45, 10, 'rim'),
('BRG-003', 'Kopi Instan Koperasi (Box)', 3, 2, 5, 'box'),
('BRG-004', 'Kursi Kerja Ergonomis', 4, 8, 2, 'unit');

-- Data Transaksi Masuk
INSERT INTO `tabel_transaksi_masuk` (`tanggal`, `kode_barang`, `jumlah`, `id_vendor`) VALUES
(CURDATE(), 'BRG-001', 5, 1),
(CURDATE(), 'BRG-002', 20, 2),
(DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'BRG-003', 2, 3);
