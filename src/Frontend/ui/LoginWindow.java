package Frontend.ui;

import Backend.DataBaseHandler.InstructorHandler;
import Backend.DataBaseHandler.StudentHandler;
import Backend.DataBaseHandler.UserAuthenticationHandler;
import Backend.domain.Instructor;
import Backend.domain.Student;
import Backend.domain.User;
import Backend.util.Authentication;
import com.formdev.flatlaf.FlatDarkLaf;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LoginWindow extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnSignIn;
    private JLabel lblMsg;

    private static final int STUDENT_MAX_ATTEMPTS = 5;
    private static final long STUDENT_LOCK_MILLIS = 30_000L;
    private static final Map<String, Integer> studentFailedAttempts = new HashMap<>();
    private static final Map<String, Long> studentLockUntil = new HashMap<>();

    public LoginWindow() {
        UIManager.put("Component.arc", 20);
        UIManager.put("TextComponent.arc", 20);
        UIManager.put("Button.arc", 999);
        UIManager.put("Component.focusWidth", 1);

        setTitle("College ERP | Sign In");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(580, 480));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new MigLayout("insets 24, align 50% 50%", "[grow,center]", "[grow]"));
        root.setBackground(new Color(0x0B1224));
        setContentPane(root);

        JPanel card = new JPanel(new MigLayout("insets 28 28 24 28, gapy 12, fillx", "[grow,fill]", "[]8[]16[]8[]16[]16[]push[]"));
        card.setBackground(new Color(0x141C2F));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0x23314F), 1, true), BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        root.add(card, "w 460!, growy");

        JPanel brand = new JPanel(new MigLayout("insets 0, gap 8", "[][grow]", "[]"));
        brand.setOpaque(false);
        JLabel icon = new JLabel("\uD83C\uDF93");
        icon.setForeground(new Color(0x4EA3FF));
        icon.setFont(icon.getFont().deriveFont(Font.PLAIN, 28f));
        JLabel title = new JLabel("College ERP");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        title.setForeground(new Color(0xDDE6F7));
        brand.add(icon);
        brand.add(title, "gapleft 4, wrap");
        card.add(brand, "wrap");

        JLabel h = new JLabel("Welcome back!");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 18f));
        h.setForeground(new Color(0xE8EEF9));
        JLabel sub = new JLabel("Sign in with your username and password.");
        sub.setForeground(new Color(0x9FB0D3));
        card.add(h, "wrap");
        card.add(sub, "wrap 16");

        JPanel form = new JPanel(new MigLayout("insets 0, gap 10", "[right]8[grow,fill]", "[][]"));
        form.setOpaque(false);

        JLabel lUsername = new JLabel("Username:");
        lUsername.setForeground(new Color(0xCFE0FF));
        txtUsername = new JTextField();
        StyleField(txtUsername);

        JLabel lPass = new JLabel("Password:");
        lPass.setForeground(new Color(0xCFE0FF));
        txtPassword = new JPasswordField();
        StyleField(txtPassword);

        form.add(lUsername);
        form.add(txtUsername, "wrap");
        form.add(lPass);
        form.add(txtPassword, "wrap");
        card.add(form, "growx, wrap");

        lblMsg = new JLabel(" ");
        lblMsg.setForeground(new Color(0xFFB3B3));
        card.add(lblMsg, "growx, wrap");

        JPanel actions = new JPanel(new MigLayout("insets 0, gap 10", "[grow][right]", "[]"));
        actions.setOpaque(false);

        JButton btnClear = new JButton("Clear");
        StyleGhostButton(btnClear);
        btnClear.addActionListener(e -> {
            txtUsername.setText("");
            txtPassword.setText("");
            lblMsg.setText(" ");
            txtUsername.requestFocusInWindow();
        });

        btnSignIn = new JButton("Log In");
        StylePrimaryButton(btnSignIn);
        btnSignIn.addActionListener(e -> AttemptLogin());

        getRootPane().setDefaultButton(btnSignIn);

        actions.add(btnClear, "left");
        actions.add(btnSignIn, "right");
        card.add(actions, "growx, wrap");

    }

    private static JLabel LinkLabel(String text) {
        JLabel l = new JLabel("<html><u>" + text + "</u></html>");
        l.setForeground(new Color(0x8FB7FF));
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return l;
    }

    private void StyleField(JTextField f) {
        f.putClientProperty("JComponent.roundRect", true);
        f.setBackground(new Color(0x0F1729));
        f.setForeground(new Color(0xE8EEF9));
        f.setCaretColor(new Color(0xE8EEF9));
        f.setSelectionColor(new Color(0x1F3E7A));
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0x273657), 1, true), BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
    }

    private void StylePrimaryButton(JButton b) {
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setBackground(new Color(0x4EA3FF));
        b.setForeground(new Color(0x00152A));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void StyleGhostButton(JButton b) {
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setBackground(new Color(0x0F1729));
        b.setForeground(new Color(0xE8EEF9));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x273657), 1, true),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private String NormalizeUser(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private boolean IsStudentLocked(String usernameKey) {
        Long until = studentLockUntil.get(usernameKey);
        if (until == null) return false;

        long now = System.currentTimeMillis();
        if (now >= until) {
            studentLockUntil.remove(usernameKey);
            studentFailedAttempts.remove(usernameKey);
            return false;
        }

        long remaining = (until - now + 999) / 1000;
        ShowError("Too many failed attempts. Student login locked for " + remaining + " seconds.");
        return true;
    }

    private void HandleStudentFailedLogin(String usernameKey) {
        int attempts = studentFailedAttempts.getOrDefault(usernameKey, 0) + 1;
        studentFailedAttempts.put(usernameKey, attempts);

        if (attempts >= STUDENT_MAX_ATTEMPTS) {
            long until = System.currentTimeMillis() + STUDENT_LOCK_MILLIS;
            studentLockUntil.put(usernameKey, until);
            ShowError("Too many failed attempts. Student login locked for 30 seconds.");
        }
        else {
            int left = STUDENT_MAX_ATTEMPTS - attempts;
            ShowError("Invalid Credentials. " + left + " attempt" + (left == 1 ? "" : "s") + " left before lock.");
        }
    }

    private void ClearStudentLock(String usernameKey) {
        studentFailedAttempts.remove(usernameKey);
        studentLockUntil.remove(usernameKey);
    }

    private void AttemptLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        String usernameKey = NormalizeUser(username);

        if (username.isEmpty() || password.isEmpty()) {
            ShowError("Please enter username and password.");
            return;
        }

        if (IsStudentLocked(usernameKey)) {
            return;
        }

        btnSignIn.setEnabled(false);
        lblMsg.setForeground(new Color(0x9FB0D3));
        lblMsg.setText("Signing you in…");

        SwingUtilities.invokeLater(() -> {
            try {
                User u = UserAuthenticationHandler.FindUserName(username);
                boolean result = Authentication.Login(username,password);

                String role = (u == null || u.GetRole() == null)
                        ? ""
                        : u.GetRole().trim();

                if (!result) {
                    if ("STUDENT".equalsIgnoreCase(role)) {
                        HandleStudentFailedLogin(usernameKey);
                    } else {
                        ShowError("Invalid Credentials");
                    }
                }
                else {
                    if ("STUDENT".equalsIgnoreCase(role)) {
                        ClearStudentLock(usernameKey);
                    }
                    ShowSuccess("Login Successful");
                    RouteToDashboard(u);
                }
            }
            catch (Exception ex) {
                ShowError(ex.getMessage() != null ? ex.getMessage() : "Login failed.");
            }
            finally {
                btnSignIn.setEnabled(true);
            }
        });
    }

    private void ShowError(String msg) {
        lblMsg.setForeground(new Color(0xFFB3B3));
        lblMsg.setText(msg);
    }

    private void ShowSuccess(String msg) {
        lblMsg.setForeground(new Color(0xB6F8C1));
        lblMsg.setText(msg);
    }

    private void RouteToDashboard(User u) {
        String role = (u.GetRole() == null ? "" : u.GetRole().trim().toUpperCase());
        switch (role) {
            case "ADMIN":
                new DashboardUIAdmin(u.GetUserID()).setVisible(true);
                dispose();
                break;
            case "INSTRUCTOR":
            case "TEACHER":
                int instructorId = -1;
                try {
                    Instructor ins = InstructorHandler.FindInstructorByUserID(u.GetUserID());
                    if (ins != null) {
                        instructorId = ins.GetInstructorID();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No instructor profile found for this account. Opening limited dashboard.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Failed to load instructor profile: " + ex.getMessage(),
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }

                new DashboardUIInstructor(instructorId).setVisible(true);
                dispose();
                break;
            case "STUDENT":
                int RollNumber = -1;
                try {
                    Student CurrentStudent = StudentHandler.FindStudentByUserID(u.GetUserID());
                    if (CurrentStudent != null) {
                        RollNumber = CurrentStudent.GetRollNumber();
                    }
                    else {
                        JOptionPane.showMessageDialog(this,
                                "No student profile found for this account. Opening limited dashboard.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
                catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Failed to load student profile: " + ex.getMessage(),
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                }
                new DashboardUIStudent(RollNumber).setVisible(true);
                dispose();
                break;
            default:
                ShowError("Unknown role: " + role);
        }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(new FlatDarkLaf()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
    }
}
