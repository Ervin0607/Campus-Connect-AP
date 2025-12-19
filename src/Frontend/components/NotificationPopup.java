package Frontend.components;

import Backend.DataBaseHandler.*;
import Backend.domain.Notification;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class NotificationPopup extends JPopupMenu {

    private final int studentRollNumber;
    private final JPanel contentPanel;
    private final JLabel statusLabel;

    private static final Color COL_BG = new Color(0x1E1E1E);
    private static final Color COL_ITEM_BG = new Color(0x252525);
    private static final Color COL_TEXT = new Color(0xFFFFFF);
    private static final Color COL_SEC = new Color(0xB0B0B0);
    private static final Color COL_ACCENT = new Color(0xB0F2B4);
    private static final Color COL_BTN_RED = new Color(0xD32F2F);

    public NotificationPopup(int studentRollNumber) {
        this.studentRollNumber = studentRollNumber;


        setBackground(COL_BG);
        setBorder(BorderFactory.createLineBorder(new Color(0x333333)));
        setPreferredSize(new Dimension(350, 400));
        setLayout(new BorderLayout());

        add(CreateHeader(), BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COL_BG);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);

        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel("Loading...", SwingConstants.CENTER);
        statusLabel.setForeground(COL_SEC);
        statusLabel.setBorder(new EmptyBorder(20, 0, 0, 0));
        contentPanel.add(statusLabel);
    }

    private JPanel CreateHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COL_BG);
        header.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Notifications");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(COL_TEXT);

        JButton clearBtn = new JButton("Clear all");
        clearBtn.setFocusPainted(false);
        clearBtn.setBackground(COL_BTN_RED);
        clearBtn.setForeground(Color.WHITE);
        clearBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        clearBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        clearBtn.addActionListener(e -> {
            try {
                NotificationHandler.ClearAll(studentRollNumber);
                contentPanel.removeAll();
                JLabel empty = new JLabel("No new notifications", SwingConstants.CENTER);
                empty.setForeground(COL_SEC);
                empty.setBorder(new EmptyBorder(20,0,0,0));
                contentPanel.add(empty);
                contentPanel.revalidate();
                contentPanel.repaint();
            }
            catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to clear notifications.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        header.add(title, BorderLayout.WEST);
        header.add(clearBtn, BorderLayout.EAST);
        return header;
    }

    public void LoadNotifications() {
        SwingWorker<List<JPanel>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<JPanel> doInBackground() {
                List<JPanel> panels = new ArrayList<>();
                try {
                    List<Notification> storedNotifs = NotificationHandler.GetNotifications(studentRollNumber);

                    if (storedNotifs != null) {
                        for (Notification n : storedNotifs) {
                            String iconPath = switch (n.getType()) {
                                case "GRADE" -> "Frontend/assets/grade.svg";
                                case "ANNOUNCEMENT" -> "Frontend/assets/announcement.svg";
                                default -> "Frontend/assets/notificationBell.svg";
                            };
                            panels.add(CreateItem(n.getTitle(), n.getMessage(), iconPath));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return panels;
            }

            @Override
            protected void done() {
                contentPanel.removeAll();
                try {
                    List<JPanel> result = get();
                    if (result.isEmpty()) {
                        JLabel empty = new JLabel("No new notifications", SwingConstants.CENTER);
                        empty.setForeground(COL_SEC);
                        empty.setBorder(new EmptyBorder(20,0,0,0));
                        contentPanel.add(empty);
                    } else {
                        for (JPanel p : result) {
                            contentPanel.add(p);
                            contentPanel.add(Box.createVerticalStrut(8));
                        }
                    }
                } catch (Exception e) {
                    JLabel err = new JLabel("Error loading data");
                    err.setForeground(Color.RED);
                    contentPanel.add(err);
                    e.printStackTrace();
                }
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        };
        worker.execute();
    }

    private JPanel CreateItem(String title, String subtitle, String iconPath) {
        JPanel panel = new JPanel(new BorderLayout(12, 5));
        panel.setBackground(COL_ITEM_BG);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        panel.setMaximumSize(new Dimension(325, 70));
        panel.setPreferredSize(new Dimension(325, 70));

        JLabel icon = new JLabel("●");
        icon.setForeground(COL_ACCENT);
        icon.setFont(new Font("SansSerif", Font.BOLD, 20));
        icon.setVerticalAlignment(SwingConstants.TOP);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 13));
        t.setForeground(COL_TEXT);

        JLabel s = new JLabel(subtitle);
        s.setFont(new Font("SansSerif", Font.PLAIN, 12));
        s.setForeground(COL_SEC);

        textPanel.add(t);
        textPanel.add(s);

        panel.add(icon, BorderLayout.WEST);
        panel.add(textPanel, BorderLayout.CENTER);

        return panel;
    }
}