package com.mycompany.loginapp;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DEMO en memoria: usuarios (con contraseña encriptada) y galerías residirán sólo
 * durante la ejecución.  No hay archivos ni base de datos.
 *
 * - Un usuario "admin" se crea por defecto con contraseña "admin" (SHA‑256).
 * - Panel "Usuarios" sólo visible si el usuario autenticado es admin.
 * - Cada usuario puede subir ≤5 imágenes y añadirles una descripción.
 */
public class LoginApp {

    public static void main(String[] args) {
        // Carga mínima: inyecta admin
        InMemoryStore.init();
        EventQueue.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

/***********************  ALMACÉN EN MEMORIA  **********************/
class InMemoryStore {
    static final List<User> USERS = new ArrayList<>();

    static void init() {
        if (USERS.isEmpty()) {
            USERS.add(new User("admin", Crypto.hash("admin"), true));
        }
    }

    static User find(String username) {
        return USERS.stream().filter(u -> u.username.equals(username)).findFirst().orElse(null);
    }

    static void save(User u) {
        User existing = find(u.username);
        if (existing == null) {
            USERS.add(u);
        } else {
            existing.passwordHash = u.passwordHash;
            existing.admin = u.admin;
        }
    }
}

/*************************  ENCRIPTACIÓN  *************************/
class Crypto {
    static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

/*************************  MODELOS  *************************/
class User {
    final String username;
    String passwordHash;
    boolean admin;
    final List<GalleryItem> gallery = new ArrayList<>();

    User(String u, String hash, boolean admin) {
        this.username = u;
        this.passwordHash = hash;
        this.admin = admin;
    }
}

class GalleryItem {
    final File file;
    final String description;

    GalleryItem(File f, String d) {
        file = f;
        description = d;
    }
}

/***************************  LOGIN  *****************************/
class LoginFrame extends JFrame {
    private final JTextField userField = new JTextField(15);
    private final JPasswordField passField = new JPasswordField(15);

    LoginFrame() {
        super("Login");
        build();
    }

    private void build() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx = 0; g.gridy = 0;                 p.add(new JLabel("Usuario:"), g);
        g.gridx = 1;                              p.add(userField, g);
        g.gridx = 0; g.gridy = 1;                 p.add(new JLabel("Contraseña:"), g);
        g.gridx = 1;                              p.add(passField, g);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2;
        JButton login = new JButton("Ingresar"); login.addActionListener(this::onLogin); p.add(login, g);

        setContentPane(p);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void onLogin(ActionEvent e) {
        String u = userField.getText().trim();
        String p = new String(passField.getPassword());
        User user = InMemoryStore.find(u);
        if (user != null && user.passwordHash.equals(Crypto.hash(p))) {
            dispose();
            new MainFrame(user).setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

/***********************  VENTANA PRINCIPAL  **********************/
class MainFrame extends JFrame {
    MainFrame(User user) {
        super("Sistema – " + user.username);
        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Galería", new GalleryPanel(user));
        if (user.admin) tabs.add("Usuarios", new UserAdminPanel());
        add(tabs);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}

/***********************  PANEL GALERÍA  **************************/
class GalleryPanel extends JPanel {
    private static final int MAX = 5;
    private final User user;
    private final JPanel thumbs = new JPanel(new FlowLayout(FlowLayout.LEFT));

    GalleryPanel(User u) {
        super(new BorderLayout());
        user = u;
        JButton add = new JButton("Añadir imagen");
        add.addActionListener(this::addImage);
        add(add, BorderLayout.NORTH);
        add(new JScrollPane(thumbs), BorderLayout.CENTER);
        refresh();
    }

    private void refresh() {
        thumbs.removeAll();
        for (GalleryItem gi : user.gallery) {
            JLabel lbl = makeThumb(gi.file);
            lbl.setToolTipText(gi.description);
            thumbs.add(lbl);
        }
        revalidate();
        repaint();
    }

    private void addImage(ActionEvent e) {
        if (user.gallery.size() >= MAX) {
            JOptionPane.showMessageDialog(this, "Máximo de " + MAX + " imágenes alcanzado");
            return;
        }
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File img = fc.getSelectedFile();
            String desc = JOptionPane.showInputDialog(this, "Descripción de la imagen:");
            if (desc == null) return; // cancelado
            user.gallery.add(new GalleryItem(img, desc));
            refresh();
        }
    }

    private JLabel makeThumb(File imgFile) {
        ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
        Image scaled = icon.getImage().getScaledInstance(120, 90, Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(scaled));
    }
}

/***********************  ADMIN USUARIOS  *************************/
class UserAdminPanel extends JPanel {
    private final JList<String> list = new JList<>();

    UserAdminPanel() {
        super(new BorderLayout());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshList();
        add(new JScrollPane(list), BorderLayout.CENTER);

        JButton addBtn = new JButton("Nuevo / Modificar"); addBtn.addActionListener(e -> addOrEdit());
        add(addBtn, BorderLayout.SOUTH);
    }

    private void refreshList() {
        List<String> names = InMemoryStore.USERS.stream().map(u -> u.username + (u.admin ? " (admin)" : "")).collect(Collectors.toList());
        list.setListData(names.toArray(new String[0]));
    }

    private void addOrEdit() {
        JPanel p = new JPanel(new GridLayout(0, 2, 4, 4));
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JCheckBox adminChk = new JCheckBox("Admin");
        p.add(new JLabel("Usuario:"));   p.add(userField);
        p.add(new JLabel("Contraseña:")); p.add(passField);
        p.add(new JLabel());              p.add(adminChk);

        int op = JOptionPane.showConfirmDialog(this, p, "Crear / Modificar", JOptionPane.OK_CANCEL_OPTION);
        if (op == JOptionPane.OK_OPTION) {
            String u = userField.getText().trim();
            String pw = new String(passField.getPassword());
            if (u.isEmpty() || pw.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Usuario y contraseña requeridos");
                return;
            }
            InMemoryStore.save(new User(u, Crypto.hash(pw), adminChk.isSelected()));
            refreshList();
        }
    }
}
