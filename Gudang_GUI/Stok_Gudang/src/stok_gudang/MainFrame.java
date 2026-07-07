package stok_gudang;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

/**
 * MainFrame — Dashboard + Navigasi utama Sistem Manajemen Gudang Koperasi.
 * Semua panel CRUD (Kategori, Vendor, Barang, Transaksi Masuk) diimplementasikan penuh.
 * Tema: Dark Mode / Dashboard Telemetri.
 */
public class MainFrame extends JFrame {

    // ─────────────────────────────────────────────────────────
    //  PALET WARNA
    // ─────────────────────────────────────────────────────────
    private static final Color BG_DEEP        = new Color(0x12, 0x12, 0x12);
    private static final Color BG_SIDEBAR     = new Color(0x18, 0x18, 0x18);
    private static final Color BG_CARD        = new Color(0x1E, 0x1E, 0x1E);
    private static final Color BG_FIELD       = new Color(0x2A, 0x2A, 0x2A);
    private static final Color ACCENT_RED     = new Color(0xE0, 0x06, 0x13);
    private static final Color ACCENT_RED_H   = new Color(0xFF, 0x1A, 0x28);
    private static final Color ACCENT_GREEN   = new Color(0x2E, 0x7D, 0x32);
    private static final Color SIDEBAR_ACTIVE = new Color(0x2C, 0x0A, 0x0A);
    private static final Color TEXT_WHITE     = Color.WHITE;
    private static final Color TEXT_MUTED     = new Color(0x88, 0x88, 0x88);
    private static final Color BORDER_SUBTLE  = new Color(0x2E, 0x2E, 0x2E);
    private static final Color BORDER_RED     = new Color(0xE0, 0x06, 0x13, 160);

    // ─────────────────────────────────────────────────────────
    //  FONT
    // ─────────────────────────────────────────────────────────
    private static final Font FONT_APP_NAME = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_ROLE     = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_NAV      = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_NAV_ACT  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font FONT_SECTION  = new Font("Segoe UI", Font.BOLD,  10);
    private static final Font FONT_STAT_VAL = new Font("Segoe UI", Font.BOLD,  26);
    private static final Font FONT_STAT_LBL = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_CARD_TTL = new Font("Segoe UI", Font.BOLD,  14);
    private static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD,  10);
    private static final Font FONT_FIELD    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BTN      = new Font("Segoe UI", Font.BOLD,  13);

    // ─────────────────────────────────────────────────────────
    //  CARD IDs
    // ─────────────────────────────────────────────────────────
    private static final String CARD_DASHBOARD = "DASHBOARD";
    private static final String CARD_TRANSAKSI = "TRANSAKSI_MASUK";
    private static final String CARD_KATEGORI  = "KATEGORI";
    private static final String CARD_VENDOR    = "VENDOR";
    private static final String CARD_BARANG    = "BARANG";

    // ─────────────────────────────────────────────────────────
    //  STATE
    // ─────────────────────────────────────────────────────────
    private final String  loggedUser;
    private final String  loggedRole;
    private CardLayout    contentCardLayout;
    private JPanel        contentCardPanel;
    private JButton       activeBtn = null;

    // Sidebar buttons
    private JButton btnDashboard, btnTransaksi, btnKategori, btnVendor, btnBarang;

    // ── Transaksi Masuk ──
    private JTextField         tfTanggal;
    private JComboBox<String>  cbKodeBarang;
    private JTextField         tfNamaBarangTrans;
    private JTextField         tfJumlah;
    private JComboBox<String>  cbVendorTrans;
    private JLabel             lblTransMsg;
    private DefaultTableModel  modelTransaksi;

    // ── Kategori ──
    private JTextField        tfNamaKategori;
    private JLabel            lblKategoriMsg;
    private DefaultTableModel modelKategori;
    private int               editIdKategori = -1;

    // ── Vendor ──
    private JTextField        tfNamaVendor, tfAlamatVendor, tfTeleponVendor;
    private JLabel            lblVendorMsg;
    private DefaultTableModel modelVendor;
    private int               editIdVendor = -1;

    // ── Barang ──
    private JTextField        tfKodeBarang, tfNamaBarang, tfStok, tfStokMin, tfSatuan;
    private JComboBox<String> cbKategoriBarang;
    private JLabel            lblBarangMsg;
    private DefaultTableModel modelBarang;
    private boolean           isEditBarang = false;

    // ─────────────────────────────────────────────────────────
    public MainFrame(String username, String role) {
        super("Gudang Koperasi — " + username);
        this.loggedUser = username;
        this.loggedRole = role;
        initUI();
        setVisible(true);
    }

    // =========================================================
    //  INIT UTAMA
    // =========================================================
    private void initUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { doLogout(); }
        });
        getContentPane().setBackground(BG_DEEP);
        setLayout(new BorderLayout(0, 0));
        add(buildSidebar(),     BorderLayout.WEST);
        add(buildContentArea(), BorderLayout.CENTER);
        switchCard(CARD_DASHBOARD, btnDashboard);
    }

    // =========================================================
    //  SIDEBAR
    // =========================================================
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, BORDER_SUBTLE));

        // Brand
        JPanel brand = new JPanel();
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBackground(BG_SIDEBAR);
        brand.setBorder(new EmptyBorder(24, 20, 20, 20));
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JPanel logoBox = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT_RED);
                g2.fillRoundRect(0, 0, 22, 22, 5, 5);
                g2.setColor(TEXT_WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString("G", 7, 16);
                g2.dispose();
            }
        };
        logoBox.setPreferredSize(new Dimension(22, 22));
        logoBox.setMaximumSize(new Dimension(22, 22));
        logoBox.setOpaque(false);
        logoBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel appName = new JLabel("SIM GUDANG");
        appName.setFont(FONT_APP_NAME);
        appName.setForeground(TEXT_WHITE);
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel(loggedRole.toUpperCase() + "  |  " + loggedUser);
        roleLabel.setFont(FONT_ROLE);
        roleLabel.setForeground(TEXT_MUTED);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        brand.add(logoBox);
        brand.add(Box.createVerticalStrut(8));
        brand.add(appName);
        brand.add(Box.createVerticalStrut(4));
        brand.add(roleLabel);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_SUBTLE);
        sep.setBackground(BORDER_SUBTLE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Menu Utama
        JLabel secMenu = makeSectionLabel("MENU UTAMA");
        btnDashboard = makeSidebarButton("Dashboard");
        btnDashboard.addActionListener(e -> switchCard(CARD_DASHBOARD, btnDashboard));

        btnTransaksi = makeSidebarButton("Transaksi Masuk");
        btnTransaksi.addActionListener(e -> {
            switchCard(CARD_TRANSAKSI, btnTransaksi);
            refreshComboBarang();
            refreshComboVendorTrans();
            loadTransaksiTable();
        });

        // Data Master
        JLabel secData = makeSectionLabel("DATA MASTER");
        btnKategori = makeSidebarButton("Kategori");
        btnKategori.addActionListener(e -> {
            switchCard(CARD_KATEGORI, btnKategori);
            loadKategoriTable();
        });

        btnVendor = makeSidebarButton("Vendor");
        btnVendor.addActionListener(e -> {
            switchCard(CARD_VENDOR, btnVendor);
            loadVendorTable();
        });

        btnBarang = makeSidebarButton("Barang");
        btnBarang.addActionListener(e -> {
            switchCard(CARD_BARANG, btnBarang);
            loadBarangTable();
            refreshComboKategoriBarang();
        });

        // Logout
        JPanel logoutArea = new JPanel(new BorderLayout());
        logoutArea.setBackground(BG_SIDEBAR);
        logoutArea.setBorder(new EmptyBorder(12, 12, 20, 12));

        JButton btnLogout = new JButton("Logout") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed() ? ACCENT_RED.darker()
                        : getModel().isRollover() ? ACCENT_RED_H : new Color(0x2A, 0x0A, 0x0A);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogout.setFont(FONT_NAV);
        btnLogout.setForeground(new Color(0xFF, 0x77, 0x77));
        btnLogout.setContentAreaFilled(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(new EmptyBorder(10, 16, 10, 16));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogout.addActionListener(e -> doLogout());
        logoutArea.add(btnLogout, BorderLayout.CENTER);

        sidebar.add(brand);
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(secMenu);
        sidebar.add(btnDashboard);
        sidebar.add(btnTransaksi);
        sidebar.add(Box.createVerticalStrut(16));
        sidebar.add(secData);
        sidebar.add(btnKategori);
        sidebar.add(btnVendor);
        sidebar.add(btnBarang);
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(logoutArea);
        return sidebar;
    }

    // =========================================================
    //  CONTENT AREA
    // =========================================================
    private JPanel buildContentArea() {
        contentCardLayout = new CardLayout();
        contentCardPanel  = new JPanel(contentCardLayout);
        contentCardPanel.setBackground(BG_DEEP);
        contentCardPanel.add(buildDashboardCard(), CARD_DASHBOARD);
        contentCardPanel.add(buildTransaksiCard(), CARD_TRANSAKSI);
        contentCardPanel.add(buildKategoriCard(),  CARD_KATEGORI);
        contentCardPanel.add(buildVendorCard(),    CARD_VENDOR);
        contentCardPanel.add(buildBarangCard(),    CARD_BARANG);
        return contentCardPanel;
    }

    // =========================================================
    //  CARD: DASHBOARD
    // =========================================================
    private JPanel buildDashboardCard() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_DEEP);
        root.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DEEP);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel pageTitle = new JLabel("Dashboard Telemetri");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        pageTitle.setForeground(TEXT_WHITE);
        JLabel pageSub = new JLabel("Ringkasan operasional gudang secara real-time");
        pageSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pageSub.setForeground(TEXT_MUTED);
        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setBackground(BG_DEEP);
        titleGroup.add(pageTitle);
        titleGroup.add(Box.createVerticalStrut(4));
        titleGroup.add(pageSub);

        JLabel timestamp = new JLabel("Update: " + LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        timestamp.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timestamp.setForeground(TEXT_MUTED);

        header.add(titleGroup, BorderLayout.WEST);
        header.add(timestamp,  BorderLayout.EAST);

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setBackground(BG_DEEP);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        statsRow.add(buildStatCard("Total Barang",    "—", "SKU terdaftar",    new Color(0x1A, 0x3A, 0x5C)));
        statsRow.add(buildStatCard("Total Vendor",    "—", "Pemasok aktif",    new Color(0x1A, 0x3A, 0x1A)));
        statsRow.add(buildStatCard("Transaksi Masuk", "—", "Bulan ini",        new Color(0x3A, 0x1A, 0x1A)));
        statsRow.add(buildStatCard("Stok Menipis",    "—", "Di bawah minimum", new Color(0x2A, 0x22, 0x08)));
        loadStatCards(statsRow);

        JPanel chartArea = new JPanel(new GridLayout(1, 2, 16, 0));
        chartArea.setBackground(BG_DEEP);
        chartArea.add(buildChartPlaceholder("Grafik Arus Masuk Barang",
                "Tambahkan chart library (misal JFreeChart) di sini"));
        chartArea.add(buildChartPlaceholder("Distribusi Kategori",
                "Tambahkan Pie Chart atau Bar Chart di sini"));

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(BG_DEEP);
        body.add(statsRow);
        body.add(Box.createVerticalStrut(20));
        body.add(chartArea);

        root.add(header, BorderLayout.NORTH);
        root.add(body,   BorderLayout.CENTER);
        return root;
    }

    private JPanel buildStatCard(String title, String value, String sub, Color bgTint) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bgTint);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER_SUBTLE, 1, true), new EmptyBorder(16, 18, 16, 18)));
        JLabel ico = new JLabel(title);
        ico.setFont(new Font("Segoe UI", Font.BOLD, 11));
        ico.setForeground(TEXT_MUTED);
        ico.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel val = new JLabel(value);
        val.setName("STAT_VALUE");
        val.setFont(FONT_STAT_VAL);
        val.setForeground(TEXT_WHITE);
        val.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel(sub);
        label.setFont(FONT_STAT_LBL);
        label.setForeground(TEXT_MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(ico);
        card.add(Box.createVerticalStrut(8));
        card.add(val);
        card.add(Box.createVerticalStrut(4));
        card.add(label);
        return card;
    }

    private void loadStatCards(JPanel statsRow) {
        new SwingWorker<int[], Void>() {
            @Override protected int[] doInBackground() {
                int[] data = {0, 0, 0, 0};
                try (Connection conn = Koneksi.getKoneksi(); Statement st = conn.createStatement()) {
                    ResultSet rs;
                    rs = st.executeQuery("SELECT COUNT(*) FROM tabel_barang");
                    if (rs.next()) data[0] = rs.getInt(1);
                    rs = st.executeQuery("SELECT COUNT(*) FROM tabel_vendor");
                    if (rs.next()) data[1] = rs.getInt(1);
                    rs = st.executeQuery("SELECT COUNT(*) FROM tabel_transaksi_masuk WHERE MONTH(tanggal)=MONTH(CURDATE()) AND YEAR(tanggal)=YEAR(CURDATE())");
                    if (rs.next()) data[2] = rs.getInt(1);
                    rs = st.executeQuery("SELECT COUNT(*) FROM tabel_barang WHERE stok < stok_minimum");
                    if (rs.next()) data[3] = rs.getInt(1);
                } catch (SQLException ex) { System.err.println("[Dashboard] " + ex.getMessage()); }
                return data;
            }
            @Override protected void done() {
                try {
                    int[] data = get();
                    String[] vals = {String.valueOf(data[0]), String.valueOf(data[1]),
                                     String.valueOf(data[2]), data[3] > 0 ? "! " + data[3] : "Aman"};
                    Component[] cards = statsRow.getComponents();
                    for (int i = 0; i < cards.length && i < vals.length; i++) {
                        if (cards[i] instanceof JPanel card) {
                            for (Component c : card.getComponents()) {
                                if (c instanceof JLabel lbl && "STAT_VALUE".equals(lbl.getName()))
                                    lbl.setText(vals[i]);
                            }
                        }
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    private JPanel buildChartPlaceholder(String title, String hint) {
        JPanel ph = new JPanel(new BorderLayout());
        ph.setBackground(new Color(0x1A, 0x1A, 0x1A));
        ph.setBorder(new CompoundBorder(new LineBorder(BORDER_RED, 1, false), new EmptyBorder(16, 16, 16, 16)));
        JLabel lbl = new JLabel(title, SwingConstants.LEFT);
        lbl.setFont(FONT_CARD_TTL);
        lbl.setForeground(TEXT_WHITE);
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        JPanel zone = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x33, 0x33, 0x33, 80));
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{6,6}, 0));
                g2.drawRoundRect(8, 8, getWidth() - 16, getHeight() - 16, 10, 10);
                g2.setColor(TEXT_MUTED);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, (getWidth() - fm.stringWidth(hint)) / 2, (getHeight() + fm.getAscent()) / 2);
                g2.dispose();
            }
        };
        zone.setBackground(new Color(0x1A, 0x1A, 0x1A));
        zone.setPreferredSize(new Dimension(0, 200));
        ph.add(lbl,  BorderLayout.NORTH);
        ph.add(zone, BorderLayout.CENTER);
        return ph;
    }

    // =========================================================
    //  CARD: TRANSAKSI MASUK
    // =========================================================
    private JPanel buildTransaksiCard() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG_DEEP);
        root.setBorder(new EmptyBorder(28, 28, 28, 28));

        root.add(buildPageHeader("Transaksi Masuk", "Input penerimaan barang ke gudang"), BorderLayout.NORTH);

        // Form
        JPanel formCard = buildCardPanel("Form Input Barang Masuk");
        JPanel formInner = (JPanel) formCard.getComponent(1);

        JPanel grid = new JPanel(new GridLayout(2, 4, 14, 8));
        grid.setBackground(BG_CARD);
        grid.add(makeFormLabel("TANGGAL"));
        grid.add(makeFormLabel("KODE BARANG"));
        grid.add(makeFormLabel("JUMLAH"));
        grid.add(makeFormLabel("VENDOR"));

        tfTanggal = makeFormTextField();
        tfTanggal.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        grid.add(tfTanggal);

        cbKodeBarang = new JComboBox<>();
        styleComboBox(cbKodeBarang);
        cbKodeBarang.addActionListener(e -> {
            String item = (String) cbKodeBarang.getSelectedItem();
            if (item != null && item.contains("|")) autoFillNamaBarangTrans(item.split("\\|")[0]);
            else tfNamaBarangTrans.setText("");
        });
        grid.add(cbKodeBarang);

        tfJumlah = makeFormTextField();
        grid.add(tfJumlah);

        cbVendorTrans = new JComboBox<>();
        styleComboBox(cbVendorTrans);
        grid.add(cbVendorTrans);

        JPanel infoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        infoRow.setBackground(BG_CARD);
        tfNamaBarangTrans = makeFormTextField();
        tfNamaBarangTrans.setEditable(false);
        tfNamaBarangTrans.setBackground(new Color(0x22, 0x22, 0x22));
        tfNamaBarangTrans.setForeground(TEXT_MUTED);
        tfNamaBarangTrans.setPreferredSize(new Dimension(340, 36));
        infoRow.add(makeFormLabel("NAMA BARANG (auto-fill)  "));
        infoRow.add(tfNamaBarangTrans);

        lblTransMsg = new JLabel(" ");
        lblTransMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setBackground(BG_CARD);
        btnRow.setBorder(new EmptyBorder(12, 0, 0, 0));
        JButton btnSimpan = makeActionButton("Simpan",  true,  ACCENT_RED);
        JButton btnReset  = makeActionButton("Reset",   false, new Color(0x33, 0x33, 0x33));
        btnSimpan.addActionListener(e -> doSimpanTransaksi());
        btnReset.addActionListener(e  -> doResetTransaksiForm());
        btnRow.add(btnSimpan);
        btnRow.add(Box.createHorizontalStrut(10));
        btnRow.add(btnReset);
        btnRow.add(Box.createHorizontalStrut(16));
        btnRow.add(lblTransMsg);

        formInner.add(grid);
        formInner.add(Box.createVerticalStrut(10));
        formInner.add(infoRow);
        formInner.add(btnRow);

        // Tabel
        JPanel tableCard = buildCardPanel("Riwayat Transaksi Masuk");
        JPanel tableInner = (JPanel) tableCard.getComponent(1);
        String[] cols = {"No", "Tanggal", "Kode Barang", "Nama Barang", "Jumlah", "Vendor"};
        modelTransaksi = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelTransaksi);
        styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        JButton btnRefresh = makeSmallButton("Refresh");
        btnRefresh.addActionListener(e -> loadTransaksiTable());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_CARD);

        JPanel tableTop = new JPanel(new BorderLayout());
        tableTop.setBackground(BG_CARD);
        tableTop.setBorder(new EmptyBorder(0, 0, 10, 0));
        tableTop.add(new JLabel("") , BorderLayout.WEST);
        tableTop.add(btnRefresh,      BorderLayout.EAST);
        tableInner.add(tableTop);
        tableInner.add(scroll);

        JPanel body = new JPanel(new GridLayout(2, 1, 0, 16));
        body.setBackground(BG_DEEP);
        body.add(formCard);
        body.add(tableCard);

        root.add(body, BorderLayout.CENTER);

        // Load data awal setelah UI siap
        SwingUtilities.invokeLater(() -> {
            refreshComboBarang();
            refreshComboVendorTrans();
            loadTransaksiTable();
        });
        return root;
    }

    private void refreshComboBarang() {
        cbKodeBarang.removeAllItems();
        cbKodeBarang.addItem("-- Pilih Barang --");
        try (Connection conn = Koneksi.getKoneksi();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT kode_barang, nama_barang FROM tabel_barang ORDER BY kode_barang")) {
            while (rs.next())
                cbKodeBarang.addItem(rs.getString("kode_barang") + "|" + rs.getString("nama_barang"));
        } catch (SQLException ex) { System.err.println("[refreshComboBarang] " + ex.getMessage()); }
        cbKodeBarang.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) { String v = value.toString(); setText(v.contains("|") ? v.split("\\|",2)[0]+" — "+v.split("\\|",2)[1] : v); }
                setBackground(isSelected ? SIDEBAR_ACTIVE : BG_FIELD);
                setForeground(TEXT_WHITE);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
    }

    private void refreshComboVendorTrans() {
        cbVendorTrans.removeAllItems();
        cbVendorTrans.addItem("-- Pilih Vendor --");
        try (Connection conn = Koneksi.getKoneksi();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_vendor, nama_vendor FROM tabel_vendor ORDER BY nama_vendor")) {
            while (rs.next())
                cbVendorTrans.addItem(rs.getInt("id_vendor") + "|" + rs.getString("nama_vendor"));
        } catch (SQLException ex) { System.err.println("[refreshComboVendor] " + ex.getMessage()); }
        cbVendorTrans.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) { String v = value.toString(); setText(v.contains("|") ? v.split("\\|",2)[1] : v); }
                setBackground(isSelected ? SIDEBAR_ACTIVE : BG_FIELD);
                setForeground(TEXT_WHITE);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
    }

    private void autoFillNamaBarangTrans(String kode) {
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement("SELECT nama_barang FROM tabel_barang WHERE kode_barang=?")) {
            ps.setString(1, kode);
            ResultSet rs = ps.executeQuery();
            tfNamaBarangTrans.setText(rs.next() ? rs.getString(1) : "");
        } catch (SQLException ex) { tfNamaBarangTrans.setText(""); }
    }

    private void doSimpanTransaksi() {
        String tanggal    = tfTanggal.getText().trim();
        String kodeItem   = (String) cbKodeBarang.getSelectedItem();
        String jumlahStr  = tfJumlah.getText().trim();
        String vendorItem = (String) cbVendorTrans.getSelectedItem();

        if (tanggal.isEmpty())                             { setMsg(lblTransMsg, "Tanggal tidak boleh kosong!", true); return; }
        if (kodeItem == null || kodeItem.startsWith("--")) { setMsg(lblTransMsg, "Pilih kode barang!", true); return; }
        if (jumlahStr.isEmpty())                           { setMsg(lblTransMsg, "Jumlah tidak boleh kosong!", true); return; }

        int jumlah;
        try { jumlah = Integer.parseInt(jumlahStr); if (jumlah <= 0) throw new NumberFormatException(); }
        catch (NumberFormatException ex) { setMsg(lblTransMsg, "Jumlah harus angka positif!", true); return; }

        if (vendorItem == null || vendorItem.startsWith("--")) { setMsg(lblTransMsg, "Pilih vendor!", true); return; }

        String kodeBarang = kodeItem.split("\\|")[0];
        int idVendor;
        try { idVendor = Integer.parseInt(vendorItem.split("\\|")[0]); }
        catch (NumberFormatException ex) { setMsg(lblTransMsg, "Data vendor tidak valid!", true); return; }

        try (Connection conn = Koneksi.getKoneksi()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psI = conn.prepareStatement(
                         "INSERT INTO tabel_transaksi_masuk (tanggal,kode_barang,jumlah,id_vendor) VALUES(?,?,?,?)");
                 PreparedStatement psU = conn.prepareStatement(
                         "UPDATE tabel_barang SET stok = stok + ? WHERE kode_barang = ?")) {
                psI.setString(1, tanggal); psI.setString(2, kodeBarang);
                psI.setInt(3, jumlah);    psI.setInt(4, idVendor);
                psI.executeUpdate();
                psU.setInt(1, jumlah);    psU.setString(2, kodeBarang);
                psU.executeUpdate();
                conn.commit();
                setMsg(lblTransMsg, "Transaksi berhasil disimpan. Stok diperbarui.", false);
                doResetTransaksiForm();
                loadTransaksiTable();
            } catch (SQLException ex) {
                conn.rollback();
                setMsg(lblTransMsg, "Gagal: " + ex.getMessage(), true);
            } finally { conn.setAutoCommit(true); }
        } catch (SQLException ex) { setMsg(lblTransMsg, "Koneksi gagal.", true); }
    }

    private void doResetTransaksiForm() {
        tfTanggal.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        cbKodeBarang.setSelectedIndex(0);
        tfNamaBarangTrans.setText("");
        tfJumlah.setText("");
        cbVendorTrans.setSelectedIndex(0);
    }

    private void loadTransaksiTable() {
        if (modelTransaksi == null) return;
        modelTransaksi.setRowCount(0);
        new SwingWorker<List<Object[]>, Void>() {
            @Override protected List<Object[]> doInBackground() {
                List<Object[]> rows = new ArrayList<>();
                String sql = "SELECT tm.id_transaksi,tm.tanggal,tm.kode_barang,b.nama_barang,tm.jumlah,v.nama_vendor " +
                             "FROM tabel_transaksi_masuk tm " +
                             "LEFT JOIN tabel_barang b ON tm.kode_barang=b.kode_barang " +
                             "LEFT JOIN tabel_vendor v ON tm.id_vendor=v.id_vendor " +
                             "ORDER BY tm.tanggal DESC, tm.id_transaksi DESC";
                try (Connection conn = Koneksi.getKoneksi();
                     Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                    int i = 1;
                    while (rs.next()) rows.add(new Object[]{i++, rs.getString("tanggal"),
                        rs.getString("kode_barang"), rs.getString("nama_barang"),
                        rs.getInt("jumlah"), rs.getString("nama_vendor") != null ? rs.getString("nama_vendor") : "-"});
                } catch (SQLException ex) { System.err.println("[loadTransaksi] " + ex.getMessage()); }
                return rows;
            }
            @Override protected void done() {
                try { for (Object[] r : get()) modelTransaksi.addRow(r); }
                catch (Exception ex) { ex.printStackTrace(); }
            }
        }.execute();
    }

    // =========================================================
    //  CARD: KATEGORI — CRUD Penuh
    // =========================================================
    private JPanel buildKategoriCard() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG_DEEP);
        root.setBorder(new EmptyBorder(28, 28, 28, 28));
        root.add(buildPageHeader("Kategori Barang", "Kelola data kategori gudang"), BorderLayout.NORTH);

        // Form
        JPanel formCard = buildCardPanel("Tambah / Edit Kategori");
        JPanel formInner = (JPanel) formCard.getComponent(1);

        JPanel row = new JPanel(new GridLayout(2, 1, 0, 6));
        row.setBackground(BG_CARD);
        row.add(makeFormLabel("NAMA KATEGORI"));
        tfNamaKategori = makeFormTextField();
        row.add(tfNamaKategori);

        lblKategoriMsg = new JLabel(" ");
        lblKategoriMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setBackground(BG_CARD);
        btnRow.setBorder(new EmptyBorder(12, 0, 0, 0));
        JButton btnSimpan = makeActionButton("Simpan",  true,  ACCENT_RED);
        JButton btnBatal  = makeActionButton("Batal",   false, new Color(0x33,0x33,0x33));
        btnSimpan.addActionListener(e -> doSimpanKategori());
        btnBatal.addActionListener(e  -> doResetKategoriForm());
        btnRow.add(btnSimpan);
        btnRow.add(Box.createHorizontalStrut(10));
        btnRow.add(btnBatal);
        btnRow.add(Box.createHorizontalStrut(16));
        btnRow.add(lblKategoriMsg);

        formInner.add(row);
        formInner.add(btnRow);

        // Tabel
        JPanel tableCard = buildCardPanel("Daftar Kategori");
        JPanel tableInner = (JPanel) tableCard.getComponent(1);
        String[] cols = {"ID", "Nama Kategori", "Aksi"};
        modelKategori = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelKategori);
        styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(2).setMaxWidth(130);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row < 0) return;
                int id   = (int)    modelKategori.getValueAt(row, 0);
                String nm = (String) modelKategori.getValueAt(row, 1);
                if (e.getClickCount() == 2 || table.getSelectedColumn() == 2) {
                    // Double-click = edit; kolom Aksi = hapus pilihan
                }
                // Single click baris: isi form untuk edit
                editIdKategori = id;
                tfNamaKategori.setText(nm);
                lblKategoriMsg.setText("Mode edit ID: " + id);
                lblKategoriMsg.setForeground(TEXT_MUTED);
            }
        });

        // Tombol Hapus di bawah tabel
        JPanel tblBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        tblBtnRow.setBackground(BG_CARD);
        JButton btnHapus = makeActionButton("Hapus Dipilih", true, new Color(0x7A,0x0A,0x0A));
        btnHapus.addActionListener(e -> doHapusKategori(table));
        tblBtnRow.add(btnHapus);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_CARD);
        tableInner.add(scroll);
        tableInner.add(tblBtnRow);

        JPanel body = new JPanel(new GridLayout(2, 1, 0, 16));
        body.setBackground(BG_DEEP);
        body.add(formCard);
        body.add(tableCard);
        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private void doSimpanKategori() {
        String nama = tfNamaKategori.getText().trim();
        if (nama.isEmpty()) { setMsg(lblKategoriMsg, "Nama kategori tidak boleh kosong!", true); return; }
        try (Connection conn = Koneksi.getKoneksi()) {
            if (editIdKategori >= 0) {
                // Update
                PreparedStatement ps = conn.prepareStatement("UPDATE tabel_kategori SET nama_kategori=? WHERE id_kategori=?");
                ps.setString(1, nama); ps.setInt(2, editIdKategori);
                ps.executeUpdate();
                setMsg(lblKategoriMsg, "Kategori berhasil diperbarui.", false);
            } else {
                // Insert
                PreparedStatement ps = conn.prepareStatement("INSERT INTO tabel_kategori (nama_kategori) VALUES(?)");
                ps.setString(1, nama);
                ps.executeUpdate();
                setMsg(lblKategoriMsg, "Kategori berhasil ditambahkan.", false);
            }
            doResetKategoriForm();
            loadKategoriTable();
        } catch (SQLException ex) {
            setMsg(lblKategoriMsg, ex.getMessage().contains("Duplicate") ? "Nama sudah ada!" : "Gagal: "+ex.getMessage(), true);
        }
    }

    private void doHapusKategori(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { setMsg(lblKategoriMsg, "Pilih baris yang akan dihapus!", true); return; }
        int id   = (int)    modelKategori.getValueAt(row, 0);
        String nm = (String) modelKategori.getValueAt(row, 1);
        int conf = JOptionPane.showConfirmDialog(this, "Hapus kategori \"" + nm + "\"?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tabel_kategori WHERE id_kategori=?")) {
            ps.setInt(1, id); ps.executeUpdate();
            setMsg(lblKategoriMsg, "Kategori dihapus.", false);
            doResetKategoriForm();
            loadKategoriTable();
        } catch (SQLException ex) {
            setMsg(lblKategoriMsg, "Gagal hapus (mungkin masih dipakai barang): " + ex.getMessage(), true);
        }
    }

    private void doResetKategoriForm() {
        editIdKategori = -1;
        tfNamaKategori.setText("");
        lblKategoriMsg.setText(" ");
    }

    private void loadKategoriTable() {
        if (modelKategori == null) return;
        modelKategori.setRowCount(0);
        try (Connection conn = Koneksi.getKoneksi();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_kategori, nama_kategori FROM tabel_kategori ORDER BY nama_kategori")) {
            while (rs.next())
                modelKategori.addRow(new Object[]{rs.getInt(1), rs.getString(2), "Klik baris untuk edit"});
        } catch (SQLException ex) { System.err.println("[loadKategori] " + ex.getMessage()); }
    }

    // =========================================================
    //  CARD: VENDOR — CRUD Penuh
    // =========================================================
    private JPanel buildVendorCard() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG_DEEP);
        root.setBorder(new EmptyBorder(28, 28, 28, 28));
        root.add(buildPageHeader("Data Vendor", "Kelola data pemasok gudang"), BorderLayout.NORTH);

        // Form
        JPanel formCard = buildCardPanel("Tambah / Edit Vendor");
        JPanel formInner = (JPanel) formCard.getComponent(1);

        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 8));
        grid.setBackground(BG_CARD);
        grid.add(makeFormLabel("NAMA VENDOR"));
        grid.add(makeFormLabel("ALAMAT"));
        grid.add(makeFormLabel("TELEPON"));
        tfNamaVendor    = makeFormTextField();
        tfAlamatVendor  = makeFormTextField();
        tfTeleponVendor = makeFormTextField();
        grid.add(tfNamaVendor);
        grid.add(tfAlamatVendor);
        grid.add(tfTeleponVendor);

        lblVendorMsg = new JLabel(" ");
        lblVendorMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setBackground(BG_CARD);
        btnRow.setBorder(new EmptyBorder(12, 0, 0, 0));
        JButton btnSimpan = makeActionButton("Simpan", true,  ACCENT_RED);
        JButton btnBatal  = makeActionButton("Batal",  false, new Color(0x33,0x33,0x33));
        btnSimpan.addActionListener(e -> doSimpanVendor());
        btnBatal.addActionListener(e  -> doResetVendorForm());
        btnRow.add(btnSimpan);
        btnRow.add(Box.createHorizontalStrut(10));
        btnRow.add(btnBatal);
        btnRow.add(Box.createHorizontalStrut(16));
        btnRow.add(lblVendorMsg);
        formInner.add(grid);
        formInner.add(btnRow);

        // Tabel
        JPanel tableCard = buildCardPanel("Daftar Vendor");
        JPanel tableInner = (JPanel) tableCard.getComponent(1);
        String[] cols = {"ID", "Nama Vendor", "Alamat", "Telepon"};
        modelVendor = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelVendor);
        styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row < 0) return;
                editIdVendor = (int) modelVendor.getValueAt(row, 0);
                tfNamaVendor.setText((String) modelVendor.getValueAt(row, 1));
                tfAlamatVendor.setText((String) modelVendor.getValueAt(row, 2));
                tfTeleponVendor.setText((String) modelVendor.getValueAt(row, 3));
                lblVendorMsg.setText("Mode edit ID: " + editIdVendor);
                lblVendorMsg.setForeground(TEXT_MUTED);
            }
        });

        JPanel tblBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        tblBtnRow.setBackground(BG_CARD);
        JButton btnHapus = makeActionButton("Hapus Dipilih", true, new Color(0x7A,0x0A,0x0A));
        btnHapus.addActionListener(e -> doHapusVendor(table));
        tblBtnRow.add(btnHapus);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_CARD);
        tableInner.add(scroll);
        tableInner.add(tblBtnRow);

        JPanel body = new JPanel(new GridLayout(2, 1, 0, 16));
        body.setBackground(BG_DEEP);
        body.add(formCard);
        body.add(tableCard);
        root.add(body, BorderLayout.CENTER);
        return root;
    }

    private void doSimpanVendor() {
        String nama    = tfNamaVendor.getText().trim();
        String alamat  = tfAlamatVendor.getText().trim();
        String telepon = tfTeleponVendor.getText().trim();
        if (nama.isEmpty()) { setMsg(lblVendorMsg, "Nama vendor tidak boleh kosong!", true); return; }
        try (Connection conn = Koneksi.getKoneksi()) {
            if (editIdVendor >= 0) {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE tabel_vendor SET nama_vendor=?,alamat=?,telepon=? WHERE id_vendor=?");
                ps.setString(1,nama); ps.setString(2,alamat); ps.setString(3,telepon); ps.setInt(4,editIdVendor);
                ps.executeUpdate();
                setMsg(lblVendorMsg, "Vendor diperbarui.", false);
            } else {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO tabel_vendor (nama_vendor,alamat,telepon) VALUES(?,?,?)");
                ps.setString(1,nama); ps.setString(2,alamat); ps.setString(3,telepon);
                ps.executeUpdate();
                setMsg(lblVendorMsg, "Vendor ditambahkan.", false);
            }
            doResetVendorForm(); loadVendorTable();
        } catch (SQLException ex) { setMsg(lblVendorMsg, "Gagal: " + ex.getMessage(), true); }
    }

    private void doHapusVendor(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { setMsg(lblVendorMsg, "Pilih baris yang akan dihapus!", true); return; }
        int id = (int) modelVendor.getValueAt(row, 0);
        String nm = (String) modelVendor.getValueAt(row, 1);
        if (JOptionPane.showConfirmDialog(this, "Hapus vendor \""+nm+"\"?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tabel_vendor WHERE id_vendor=?")) {
            ps.setInt(1, id); ps.executeUpdate();
            setMsg(lblVendorMsg, "Vendor dihapus.", false);
            doResetVendorForm(); loadVendorTable();
        } catch (SQLException ex) { setMsg(lblVendorMsg, "Gagal hapus: " + ex.getMessage(), true); }
    }

    private void doResetVendorForm() {
        editIdVendor = -1;
        tfNamaVendor.setText(""); tfAlamatVendor.setText(""); tfTeleponVendor.setText("");
        lblVendorMsg.setText(" ");
    }

    private void loadVendorTable() {
        if (modelVendor == null) return;
        modelVendor.setRowCount(0);
        try (Connection conn = Koneksi.getKoneksi(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_vendor,nama_vendor,alamat,telepon FROM tabel_vendor ORDER BY nama_vendor")) {
            while (rs.next()) modelVendor.addRow(new Object[]{
                rs.getInt(1), rs.getString(2),
                rs.getString(3) != null ? rs.getString(3) : "",
                rs.getString(4) != null ? rs.getString(4) : ""});
        } catch (SQLException ex) { System.err.println("[loadVendor] " + ex.getMessage()); }
    }

    // =========================================================
    //  CARD: BARANG — CRUD Penuh
    // =========================================================
    private JPanel buildBarangCard() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(BG_DEEP);
        root.setBorder(new EmptyBorder(28, 28, 28, 28));
        root.add(buildPageHeader("Master Barang", "Kelola data barang di gudang"), BorderLayout.NORTH);

        // Form
        JPanel formCard = buildCardPanel("Tambah / Edit Barang");
        JPanel formInner = (JPanel) formCard.getComponent(1);

        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 8));
        grid.setBackground(BG_CARD);
        grid.add(makeFormLabel("KODE BARANG"));
        grid.add(makeFormLabel("NAMA BARANG"));
        grid.add(makeFormLabel("KATEGORI"));
        tfKodeBarang = makeFormTextField();
        tfNamaBarang = makeFormTextField();
        cbKategoriBarang = new JComboBox<>();
        styleComboBox(cbKategoriBarang);
        grid.add(tfKodeBarang);
        grid.add(tfNamaBarang);
        grid.add(cbKategoriBarang);

        JPanel grid2 = new JPanel(new GridLayout(2, 3, 14, 8));
        grid2.setBackground(BG_CARD);
        grid2.add(makeFormLabel("STOK AWAL"));
        grid2.add(makeFormLabel("STOK MINIMUM"));
        grid2.add(makeFormLabel("SATUAN"));
        tfStok    = makeFormTextField();
        tfStokMin = makeFormTextField();
        tfSatuan  = makeFormTextField();
        tfSatuan.setText("pcs");
        grid2.add(tfStok);
        grid2.add(tfStokMin);
        grid2.add(tfSatuan);

        lblBarangMsg = new JLabel(" ");
        lblBarangMsg.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setBackground(BG_CARD);
        btnRow.setBorder(new EmptyBorder(12, 0, 0, 0));
        JButton btnSimpan = makeActionButton("Simpan", true,  ACCENT_RED);
        JButton btnBatal  = makeActionButton("Batal",  false, new Color(0x33,0x33,0x33));
        btnSimpan.addActionListener(e -> doSimpanBarang());
        btnBatal.addActionListener(e  -> doResetBarangForm());
        btnRow.add(btnSimpan);
        btnRow.add(Box.createHorizontalStrut(10));
        btnRow.add(btnBatal);
        btnRow.add(Box.createHorizontalStrut(16));
        btnRow.add(lblBarangMsg);

        formInner.add(grid);
        formInner.add(Box.createVerticalStrut(8));
        formInner.add(grid2);
        formInner.add(btnRow);

        // Tabel
        JPanel tableCard = buildCardPanel("Daftar Barang");
        JPanel tableInner = (JPanel) tableCard.getComponent(1);
        String[] cols = {"Kode", "Nama Barang", "Kategori", "Stok", "Stok Min", "Satuan"};
        modelBarang = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(modelBarang);
        styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(3).setMaxWidth(70);
        table.getColumnModel().getColumn(4).setMaxWidth(80);
        table.getColumnModel().getColumn(5).setMaxWidth(70);

        // Warnai baris stok menipis
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                setBackground(BG_CARD);
                setForeground(TEXT_WHITE);
                try {
                    int stok    = Integer.parseInt(tbl.getValueAt(row, 3).toString());
                    int stokMin = Integer.parseInt(tbl.getValueAt(row, 4).toString());
                    if (!isSelected && stok < stokMin) setBackground(new Color(0x2A, 0x1A, 0x0A));
                } catch (Exception ignored) {}
                if (isSelected) { setBackground(SIDEBAR_ACTIVE); setForeground(TEXT_WHITE); }
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row < 0) return;
                isEditBarang = true;
                tfKodeBarang.setText((String) modelBarang.getValueAt(row, 0));
                tfKodeBarang.setEditable(false); // kode tidak bisa diubah saat edit
                tfNamaBarang.setText((String) modelBarang.getValueAt(row, 1));
                tfStok.setText(String.valueOf(modelBarang.getValueAt(row, 3)));
                tfStokMin.setText(String.valueOf(modelBarang.getValueAt(row, 4)));
                tfSatuan.setText((String) modelBarang.getValueAt(row, 5));
                // Pilih kategori yang sesuai di combo
                String katNama = (String) modelBarang.getValueAt(row, 2);
                for (int i = 0; i < cbKategoriBarang.getItemCount(); i++) {
                    String item = cbKategoriBarang.getItemAt(i);
                    if (item != null && item.contains("|") && item.split("\\|",2)[1].equals(katNama))
                        cbKategoriBarang.setSelectedIndex(i);
                }
                setMsg(lblBarangMsg, "Mode edit: " + tfKodeBarang.getText(), false);
            }
        });

        JPanel tblBtnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        tblBtnRow.setBackground(BG_CARD);
        JLabel legendWarn = new JLabel("  Latar oranye = stok di bawah minimum");
        legendWarn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        legendWarn.setForeground(new Color(0xFF, 0xA0, 0x00));
        JButton btnHapus = makeActionButton("Hapus Dipilih", true, new Color(0x7A,0x0A,0x0A));
        btnHapus.addActionListener(e2 -> doHapusBarang(table));
        tblBtnRow.add(legendWarn);
        tblBtnRow.add(Box.createHorizontalStrut(16));
        tblBtnRow.add(btnHapus);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG_CARD);
        tableInner.add(scroll);
        tableInner.add(tblBtnRow);

        JPanel body = new JPanel(new GridLayout(2, 1, 0, 16));
        body.setBackground(BG_DEEP);
        body.add(formCard);
        body.add(tableCard);
        root.add(body, BorderLayout.CENTER);

        SwingUtilities.invokeLater(this::refreshComboKategoriBarang);
        return root;
    }

    private void refreshComboKategoriBarang() {
        if (cbKategoriBarang == null) return;
        cbKategoriBarang.removeAllItems();
        cbKategoriBarang.addItem("-- Pilih Kategori --");
        try (Connection conn = Koneksi.getKoneksi(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_kategori, nama_kategori FROM tabel_kategori ORDER BY nama_kategori")) {
            while (rs.next())
                cbKategoriBarang.addItem(rs.getInt(1) + "|" + rs.getString(2));
        } catch (SQLException ex) { System.err.println("[refreshComboKat] " + ex.getMessage()); }
        cbKategoriBarang.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) { String v = value.toString(); setText(v.contains("|") ? v.split("\\|",2)[1] : v); }
                setBackground(isSelected ? SIDEBAR_ACTIVE : BG_FIELD);
                setForeground(TEXT_WHITE);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
    }

    private void doSimpanBarang() {
        String kode   = tfKodeBarang.getText().trim();
        String nama   = tfNamaBarang.getText().trim();
        String katItem = (String) cbKategoriBarang.getSelectedItem();
        String stokStr    = tfStok.getText().trim();
        String stokMinStr = tfStokMin.getText().trim();
        String satuan = tfSatuan.getText().trim();

        if (kode.isEmpty() || nama.isEmpty()) { setMsg(lblBarangMsg, "Kode dan nama tidak boleh kosong!", true); return; }
        int stok, stokMin;
        try { stok = Integer.parseInt(stokStr); stokMin = Integer.parseInt(stokMinStr); }
        catch (NumberFormatException ex) { setMsg(lblBarangMsg, "Stok dan stok minimum harus angka!", true); return; }

        Integer idKat = null;
        if (katItem != null && katItem.contains("|")) {
            try { idKat = Integer.parseInt(katItem.split("\\|")[0]); } catch (NumberFormatException ignored) {}
        }

        try (Connection conn = Koneksi.getKoneksi()) {
            if (isEditBarang) {
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE tabel_barang SET nama_barang=?,id_kategori=?,stok=?,stok_minimum=?,satuan=? WHERE kode_barang=?");
                ps.setString(1, nama);
                if (idKat != null) ps.setInt(2, idKat); else ps.setNull(2, Types.INTEGER);
                ps.setInt(3, stok); ps.setInt(4, stokMin); ps.setString(5, satuan);
                ps.setString(6, kode); ps.executeUpdate();
                setMsg(lblBarangMsg, "Barang diperbarui.", false);
            } else {
                PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO tabel_barang (kode_barang,nama_barang,id_kategori,stok,stok_minimum,satuan) VALUES(?,?,?,?,?,?)");
                ps.setString(1, kode); ps.setString(2, nama);
                if (idKat != null) ps.setInt(3, idKat); else ps.setNull(3, Types.INTEGER);
                ps.setInt(4, stok); ps.setInt(5, stokMin); ps.setString(6, satuan);
                ps.executeUpdate();
                setMsg(lblBarangMsg, "Barang ditambahkan.", false);
            }
            doResetBarangForm(); loadBarangTable();
        } catch (SQLException ex) {
            setMsg(lblBarangMsg, ex.getMessage().contains("Duplicate") ? "Kode barang sudah ada!" : "Gagal: "+ex.getMessage(), true);
        }
    }

    private void doHapusBarang(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { setMsg(lblBarangMsg, "Pilih baris yang akan dihapus!", true); return; }
        String kode = (String) modelBarang.getValueAt(row, 0);
        String nm   = (String) modelBarang.getValueAt(row, 1);
        if (JOptionPane.showConfirmDialog(this, "Hapus barang \""+nm+"\"?\nSemua transaksi terkait juga akan dihapus.", "Konfirmasi",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try (Connection conn = Koneksi.getKoneksi();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM tabel_barang WHERE kode_barang=?")) {
            ps.setString(1, kode); ps.executeUpdate();
            setMsg(lblBarangMsg, "Barang dihapus.", false);
            doResetBarangForm(); loadBarangTable();
        } catch (SQLException ex) { setMsg(lblBarangMsg, "Gagal hapus: " + ex.getMessage(), true); }
    }

    private void doResetBarangForm() {
        isEditBarang = false;
        tfKodeBarang.setText(""); tfKodeBarang.setEditable(true);
        tfNamaBarang.setText(""); tfStok.setText("0");
        tfStokMin.setText("5");   tfSatuan.setText("pcs");
        cbKategoriBarang.setSelectedIndex(0);
        lblBarangMsg.setText(" ");
    }

    private void loadBarangTable() {
        if (modelBarang == null) return;
        modelBarang.setRowCount(0);
        String sql = "SELECT b.kode_barang,b.nama_barang,k.nama_kategori,b.stok,b.stok_minimum,b.satuan " +
                     "FROM tabel_barang b LEFT JOIN tabel_kategori k ON b.id_kategori=k.id_kategori " +
                     "ORDER BY b.kode_barang";
        try (Connection conn = Koneksi.getKoneksi(); Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) modelBarang.addRow(new Object[]{
                rs.getString(1), rs.getString(2),
                rs.getString(3) != null ? rs.getString(3) : "-",
                rs.getInt(4), rs.getInt(5), rs.getString(6)});
        } catch (SQLException ex) { System.err.println("[loadBarang] " + ex.getMessage()); }
    }

    // =========================================================
    //  HELPER — UI Factory
    // =========================================================

    /** Header halaman: judul besar + subjudul */
    private JPanel buildPageHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DEEP);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));
        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lTitle.setForeground(TEXT_WHITE);
        JLabel lSub = new JLabel(subtitle);
        lSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lSub.setForeground(TEXT_MUTED);
        JPanel tg = new JPanel(); tg.setLayout(new BoxLayout(tg, BoxLayout.Y_AXIS)); tg.setBackground(BG_DEEP);
        tg.add(lTitle); tg.add(Box.createVerticalStrut(3)); tg.add(lSub);
        header.add(tg, BorderLayout.WEST);
        return header;
    }

    /**
     * Membuat panel kartu dengan judul dan panel isi (BoxLayout Y_AXIS).
     * Komponen index 0 = JLabel judul, index 1 = JPanel isi.
     */
    private JPanel buildCardPanel(String title) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(new CompoundBorder(new LineBorder(BORDER_SUBTLE, 1), new EmptyBorder(18, 22, 18, 22)));
        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_CARD_TTL);
        lbl.setForeground(TEXT_WHITE);
        lbl.setBorder(new EmptyBorder(0, 0, 14, 0));
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setBackground(BG_CARD);
        card.add(lbl,   BorderLayout.NORTH);
        card.add(inner, BorderLayout.CENTER);
        return card;
    }

    private JLabel makeFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    private JTextField makeFormTextField() {
        JTextField tf = new JTextField();
        tf.setBackground(BG_FIELD);
        tf.setForeground(TEXT_WHITE);
        tf.setCaretColor(TEXT_WHITE);
        tf.setFont(FONT_FIELD);
        tf.setBorder(new CompoundBorder(new LineBorder(BORDER_SUBTLE, 1), new EmptyBorder(7, 10, 7, 10)));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return tf;
    }

    private void styleComboBox(JComboBox<String> cb) {
        cb.setBackground(BG_FIELD);
        cb.setForeground(TEXT_WHITE);
        cb.setFont(FONT_FIELD);
        cb.setBorder(new LineBorder(BORDER_SUBTLE, 1));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }

    private JButton makeActionButton(String text, boolean fill, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = fill ? color : new Color(0x33, 0x33, 0x33);
                Color hov  = fill ? color.brighter() : new Color(0x44, 0x44, 0x44);
                g2.setColor(getModel().isPressed() ? base.darker() : getModel().isRollover() ? hov : base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(fill ? TEXT_WHITE : TEXT_MUTED);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(Math.max(90, text.length() * 9), 36));
        return btn;
    }

    private JButton makeSmallButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? new Color(0x3A,0x3A,0x3A) : new Color(0x2A,0x2A,0x2A));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(TEXT_MUTED);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(5, 12, 5, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton makeSidebarButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getClientProperty("active") != null && (boolean) getClientProperty("active")) {
                    g2.setColor(SIDEBAR_ACTIVE); g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(ACCENT_RED);     g2.fillRect(0, 0, 3, getHeight());
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0x26, 0x26, 0x26)); g2.fillRect(0, 0, getWidth(), getHeight());
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_NAV);
        btn.setForeground(TEXT_MUTED);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(11, 24, 11, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return btn;
    }

    private JLabel makeSectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SECTION);
        lbl.setForeground(new Color(0x44, 0x44, 0x44));
        lbl.setBorder(new EmptyBorder(8, 20, 4, 20));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void switchCard(String cardId, JButton btn) {
        if (activeBtn != null) {
            activeBtn.putClientProperty("active", false);
            activeBtn.setFont(FONT_NAV);
            activeBtn.setForeground(TEXT_MUTED);
            activeBtn.repaint();
        }
        btn.putClientProperty("active", true);
        btn.setFont(FONT_NAV_ACT);
        btn.setForeground(TEXT_WHITE);
        btn.repaint();
        activeBtn = btn;
        contentCardLayout.show(contentCardPanel, cardId);
    }

    private void styleTable(JTable table) {
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(SIDEBAR_ACTIVE);
        table.setSelectionForeground(TEXT_WHITE);
        table.getTableHeader().setBackground(new Color(0x28, 0x28, 0x28));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBorder(new MatteBorder(0, 0, 1, 0, BORDER_SUBTLE));
    }

    private void setMsg(JLabel lbl, String text, boolean isError) {
        lbl.setText(text);
        lbl.setForeground(isError ? ACCENT_RED : ACCENT_GREEN.brighter());
    }

    // =========================================================
    //  LOGOUT
    // =========================================================
    private void doLogout() {
        if (JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin logout?",
                "Konfirmasi Logout", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            new AuthFrame();
            this.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame("admin", "admin"));
    }
}
