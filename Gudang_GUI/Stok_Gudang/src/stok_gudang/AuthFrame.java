package stok_gudang;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.*;

public class AuthFrame extends JFrame {

    private static final Color BG_DEEP = new Color(0x12, 0x12, 0x12); // #121212
    private static final Color BG_CARD = new Color(0x1E, 0x1E, 0x1E); // #1E1E1E
    private static final Color BG_FIELD = new Color(0x2A, 0x2A, 0x2A); // #2A2A2A
    private static final Color ACCENT_RED = new Color(0xE0, 0x06, 0x13); // #E00613
    private static final Color ACCENT_RED_H = new Color(0xFF, 0x1A, 0x28); // hover
    private static final Color TEXT_WHITE = new Color(0xFF, 0xFF, 0xFF);
    private static final Color TEXT_MUTED = new Color(0x88, 0x88, 0x88);
    private static final Color BORDER_SUBTLE = new Color(0x33, 0x33, 0x33);

    private static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_FIELD = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_BTN = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONT_LINK = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font FONT_BADGE = new Font("Segoe UI", Font.BOLD, 10);

    private CardLayout cardLayout;
    private JPanel cardContainer;

    private static final String CARD_LOGIN = "LOGIN";
    private static final String CARD_REGISTER = "REGISTER";

    private JTextField loginUserField;
    private JPasswordField loginPassField;
    private JLabel loginMessage;

    private JTextField regUserField;
    private JPasswordField regPassField;
    private JPasswordField regConfirmPassField;
    private JLabel regMessage;

    public AuthFrame() {
        super("Sistem Manajemen Gudang Koperasi");
        initUI();
        setVisible(true);
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BG_DEEP);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(BG_DEEP);
        wrapper.setBorder(new EmptyBorder(24, 32, 24, 32));

        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(BG_CARD);
        cardContainer.setBorder(new CompoundBorder(
                new LineBorder(BORDER_SUBTLE, 1, true),
                new EmptyBorder(32, 32, 32, 32)));

        cardContainer.add(buildLoginPanel(), CARD_LOGIN);
        cardContainer.add(buildRegisterPanel(), CARD_REGISTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        wrapper.add(cardContainer, gbc);

        add(wrapper);
        cardLayout.show(cardContainer, CARD_LOGIN);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_CARD);
        panel.setOpaque(true);

        JLabel badge = makeBadge("● GUDANG KOPERASI");
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Selamat Datang");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Masuk ke dasbor manajemen Anda");
        sub.setFont(FONT_LABEL);
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lUser = makeLabel("USERNAME");
        loginUserField = makeTextField("Masukkan username...");

        JLabel lPass = makeLabel("PASSWORD");
        loginPassField = makePasswordField("Masukkan password...");

        loginMessage = new JLabel(" ");
        loginMessage.setFont(FONT_LINK);
        loginMessage.setForeground(ACCENT_RED);
        loginMessage.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnLogin = makeButton("LOGIN", true);
        btnLogin.addActionListener(e -> doLogin());

        JLabel linkRegister = new JLabel("Belum punya akun? Register");
        linkRegister.setFont(FONT_LINK);
        linkRegister.setForeground(TEXT_MUTED);
        linkRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        linkRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkRegister.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                linkRegister.setForeground(TEXT_WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                linkRegister.setForeground(TEXT_MUTED);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                loginMessage.setText(" ");
                cardLayout.show(cardContainer, CARD_REGISTER);
            }
        });

        panel.add(badge);
        panel.add(Box.createVerticalStrut(18));
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(30));
        panel.add(lUser);
        panel.add(Box.createVerticalStrut(6));
        panel.add(loginUserField);
        panel.add(Box.createVerticalStrut(16));
        panel.add(lPass);
        panel.add(Box.createVerticalStrut(6));
        panel.add(loginPassField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(loginMessage);
        panel.add(Box.createVerticalStrut(14));
        panel.add(btnLogin);
        panel.add(Box.createVerticalStrut(20));
        panel.add(linkRegister);

        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_CARD);
        panel.setOpaque(true);

        JLabel badge = makeBadge("◆ AKUN BARU");
        badge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Buat Akun");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Daftarkan akun operator baru");
        sub.setFont(FONT_LABEL);
        sub.setForeground(TEXT_MUTED);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lUser = makeLabel("USERNAME");
        regUserField = makeTextField("Buat username...");

        JLabel lPass = makeLabel("PASSWORD");
        regPassField = makePasswordField("Buat password...");

        JLabel lConfirm = makeLabel("KONFIRMASI PASSWORD");
        regConfirmPassField = makePasswordField("Ulangi password...");

        regMessage = new JLabel(" ");
        regMessage.setFont(FONT_LINK);
        regMessage.setForeground(ACCENT_RED);
        regMessage.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnDaftar = makeButton("DAFTAR", true);
        btnDaftar.addActionListener(e -> doRegister());

        JLabel linkLogin = new JLabel("Sudah punya akun? Masuk");
        linkLogin.setFont(FONT_LINK);
        linkLogin.setForeground(TEXT_MUTED);
        linkLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        linkLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLogin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                linkLogin.setForeground(TEXT_WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                linkLogin.setForeground(TEXT_MUTED);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                regMessage.setText(" ");
                cardLayout.show(cardContainer, CARD_LOGIN);
            }
        });

        panel.add(badge);
        panel.add(Box.createVerticalStrut(18));
        panel.add(title);
        panel.add(Box.createVerticalStrut(6));
        panel.add(sub);
        panel.add(Box.createVerticalStrut(24));
        panel.add(lUser);
        panel.add(Box.createVerticalStrut(6));
        panel.add(regUserField);
        panel.add(Box.createVerticalStrut(14));
        panel.add(lPass);
        panel.add(Box.createVerticalStrut(6));
        panel.add(regPassField);
        panel.add(Box.createVerticalStrut(14));
        panel.add(lConfirm);
        panel.add(Box.createVerticalStrut(6));
        panel.add(regConfirmPassField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(regMessage);
        panel.add(Box.createVerticalStrut(14));
        panel.add(btnDaftar);
        panel.add(Box.createVerticalStrut(16));
        panel.add(linkLogin);

        return panel;
    }

    private void doLogin() {
        String username = loginUserField.getText().trim();
        String password = new String(loginPassField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage(loginMessage, "Username dan password tidak boleh kosong!", true);
            return;
        }

        String sql = "SELECT id_user, role FROM tabel_user WHERE username = ? AND password = ?";
        try (Connection conn = Koneksi.getKoneksi();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                showMessage(loginMessage, "Login berhasil! Memuat dasbor...", false);
                Timer timer = new Timer(600, ev -> {
                    new MainFrame(username, role);
                    AuthFrame.this.dispose();
                });
                timer.setRepeats(false);
                timer.start();
            } else {
                showMessage(loginMessage, "Username atau password salah.", true);
            }
        } catch (SQLException ex) {
            showMessage(loginMessage, "Koneksi database gagal: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

    private void doRegister() {
        String username = regUserField.getText().trim();
        String password = new String(regPassField.getPassword()).trim();
        String confirm = new String(regConfirmPassField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showMessage(regMessage, "Semua kolom harus diisi!", true);
            return;
        }
        if (!password.equals(confirm)) {
            showMessage(regMessage, "Password tidak cocok!", true);
            return;
        }
        if (password.length() < 6) {
            showMessage(regMessage, "Password minimal 6 karakter.", true);
            return;
        }

        String sql = "INSERT INTO tabel_user (username, password, role) VALUES (?, ?, 'operator')";
        try (Connection conn = Koneksi.getKoneksi();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();

            showMessage(regMessage, "Akun berhasil dibuat! Silakan login.", false);
            regUserField.setText("");
            regPassField.setText("");
            regConfirmPassField.setText("");

            Timer timer = new Timer(1200, ev -> cardLayout.show(cardContainer, CARD_LOGIN));
            timer.setRepeats(false);
            timer.start();

        } catch (SQLException ex) {
            if (ex.getMessage().toLowerCase().contains("duplicate")) {
                showMessage(regMessage, "Username sudah digunakan!", true);
            } else {
                showMessage(regMessage, "Gagal mendaftar: " + ex.getMessage(), true);
            }
            ex.printStackTrace();
        }
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BADGE);
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JLabel makeBadge(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BADGE);
        lbl.setForeground(ACCENT_RED);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        return lbl;
    }

    private JTextField makeTextField(String placeholder) {
        JTextField tf = new JTextField();
        styleTextField(tf, placeholder);
        return tf;
    }

    private JPasswordField makePasswordField(String placeholder) {
        JPasswordField pf = new JPasswordField();
        styleTextField(pf, placeholder);
        return pf;
    }

    private void styleTextField(JTextField tf, String placeholder) {
        tf.setBackground(BG_FIELD);
        tf.setForeground(TEXT_WHITE);
        tf.setCaretColor(TEXT_WHITE);
        tf.setFont(FONT_FIELD);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_SUBTLE, 1),
                new EmptyBorder(10, 12, 10, 12)));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Simulasi placeholder
        tf.setForeground(TEXT_MUTED);
        if (tf instanceof JPasswordField pf) {
            pf.setEchoChar((char) 0);
            pf.setText(placeholder);
            pf.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (new String(pf.getPassword()).equals(placeholder)) {
                        pf.setText("");
                        pf.setEchoChar('●');
                        pf.setForeground(TEXT_WHITE);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (pf.getPassword().length == 0) {
                        pf.setEchoChar((char) 0);
                        pf.setText(placeholder);
                        pf.setForeground(TEXT_MUTED);
                    }
                }
            });
        } else {
            tf.setText(placeholder);
            tf.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (tf.getText().equals(placeholder)) {
                        tf.setText("");
                        tf.setForeground(TEXT_WHITE);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (tf.getText().isEmpty()) {
                        tf.setText(placeholder);
                        tf.setForeground(TEXT_MUTED);
                    }
                }
            });
        }
    }

    private JButton makeButton(String text, boolean primary) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isPressed()
                        ? ACCENT_RED.darker()
                        : getModel().isRollover() ? ACCENT_RED_H : ACCENT_RED;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(TEXT_WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setOpaque(false);
        return btn;
    }

    private void showMessage(JLabel lbl, String text, boolean isError) {
        lbl.setText(text);
        lbl.setForeground(isError ? ACCENT_RED : new Color(0x4C, 0xAF, 0x50));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AuthFrame::new);
    }
}
