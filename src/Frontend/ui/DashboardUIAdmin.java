package Frontend.ui;

import Frontend.AdminPages.AdminProfilePanel;
import Frontend.AdminPages.CoursePages;
import Frontend.AdminPages.AdminSettingsPanel;
import Frontend.AdminPages.OfferingPages;
import Frontend.components.LeftNavPanelAdmin;
import Frontend.components.NotificationPopup;
import Backend.DataBaseHandler.SemestersHandler;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

public class DashboardUIAdmin extends JFrame {

    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel = new Color(0x1E1E1E);
    private static final Color ColorAccent = new Color(0xB0F2B4);
    private static final Color ColorTextPrimary = new Color(0xFFFFFF);
    private static final Color ColorTextSecondary = new Color(0xB0B0B0);
    private static final Color ColorWarning = new Color(0xFFD54F);

    private final CardLayout CenterCards = new CardLayout();
    private final JPanel CenterHolder = new JPanel(CenterCards);

    private JLabel MaintenanceLabel;
    private JLabel PageTitleLabel;

    private String CurrentCenterLabel = "Overview";

    private final int AdminId;

    public DashboardUIAdmin(int AdminId) {
        this.AdminId = AdminId;

        setTitle("ERP Admin • Campus Connect");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ColorBackground);
        setLayout(new BorderLayout());

        LeftNavPanelAdmin.Theme NavTheme = new LeftNavPanelAdmin.Theme(
                ColorPanel, ColorAccent, ColorTextPrimary, ColorTextSecondary
        );
        LeftNavPanelAdmin LeftNav = new LeftNavPanelAdmin(NavTheme, "Overview");

        LeftNav.SetOnSelect(Label -> {
            if ("Log Out".equals(Label)) {
                int Choice = JOptionPane.showConfirmDialog(
                        DashboardUIAdmin.this,
                        "Are you sure you want to log out?",
                        "Confirm Logout",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (Choice == JOptionPane.YES_OPTION) {
                    SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
                    DashboardUIAdmin.this.dispose();
                }
                else {
                    LeftNav.SetActive(CurrentCenterLabel);
                }
            }
            else {
                CurrentCenterLabel = Label;
                CenterCards.show(CenterHolder, Label);
                LeftNav.SetActive(Label);

                if (PageTitleLabel != null) {
                    PageTitleLabel.setText(" " + Label);
                }
            }
        });

        add(LeftNav, BorderLayout.WEST);
        add(CreateMainContent(), BorderLayout.CENTER);

        RefreshMaintenanceState();
        Backend.util.AdminUIReload.Register(this::ReloadFromDatabase);
    }

    private void RefreshMaintenanceState() {
        try {
            boolean IsOn = SemestersHandler.IsMaintenanceMode();
            if (MaintenanceLabel != null) {
                MaintenanceLabel.setVisible(IsOn);
            }
        }
        catch (SQLException ExceptionObject) {
            ExceptionObject.printStackTrace();
        }
    }


    public void ReloadFromDatabase() {

        CenterHolder.removeAll();

        CenterHolder.add(CreateDashboardGrid(), "Overview");
        CenterHolder.add(new Frontend.AdminPages.UserPages(), "Users");
        CenterHolder.add(new CoursePages(), "Courses");
        CenterHolder.add(new OfferingPages(), "Offerings");
        CenterHolder.add(CreateSystemHealthPanel(), "System Health");
        CenterHolder.add(new AdminSettingsPanel(this::RefreshMaintenanceState), "Settings");
        CenterHolder.add(new AdminProfilePanel(AdminId), "Profile");

        CenterCards.show(CenterHolder, CurrentCenterLabel);

        RefreshMaintenanceState();

        revalidate();
        repaint();
    }

    private JComponent CreateMainContent() {
        JPanel MainPanel = new JPanel(new BorderLayout(25, 10));
        MainPanel.setBackground(ColorBackground);
        MainPanel.setBorder(new EmptyBorder(20, 25, 25, 25));

        MainPanel.add(CreateTopBar(), BorderLayout.NORTH);

        CenterHolder.setOpaque(false);

        CenterHolder.add(CreateDashboardGrid(), "Overview");
        CenterHolder.add(new Frontend.AdminPages.UserPages(), "Users");
        CenterHolder.add(new CoursePages(), "Courses");
        CenterHolder.add(new OfferingPages(), "Offerings");
        CenterHolder.add(CreateSystemHealthPanel(), "System Health");
        CenterHolder.add(new AdminSettingsPanel(this::RefreshMaintenanceState), "Settings");
        CenterHolder.add(new AdminProfilePanel(AdminId), "Profile");

        MainPanel.add(CenterHolder, BorderLayout.CENTER);

        CenterCards.show(CenterHolder, "Overview");
        return MainPanel;
    }

    private JComponent MakeSimpleCenter(String Title) {
        JPanel PagePanel = new JPanel(new BorderLayout());
        PagePanel.setOpaque(false);
        JPanel InnerPanel = new JPanel(new GridBagLayout());
        InnerPanel.setOpaque(false);
        JLabel LabelObject = new JLabel(Title + " Page");
        LabelObject.setFont(new Font("SansSerif", Font.BOLD, 28));
        LabelObject.setForeground(ColorTextPrimary);
        InnerPanel.add(LabelObject);
        PagePanel.add(InnerPanel, BorderLayout.CENTER);
        return PagePanel;
    }

    private JComponent CreateTopBar() {
        JPanel TopBar = new JPanel(new BorderLayout());
        TopBar.setOpaque(false);

        PageTitleLabel = new JLabel(" Admin Dashboard");
        PageTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        PageTitleLabel.setForeground(ColorTextPrimary);
        TopBar.add(PageTitleLabel, BorderLayout.WEST);

        JPanel RightSidePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 0));
        RightSidePanel.setOpaque(false);

        MaintenanceLabel = new JLabel("⚠️ Maintenance Mode");
        MaintenanceLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        MaintenanceLabel.setForeground(ColorWarning);
        MaintenanceLabel.setBorder(new EmptyBorder(0, 0, 0, 20));
        MaintenanceLabel.setVisible(false);
        RightSidePanel.add(MaintenanceLabel);


        FlatSVGIcon BellIcon = new FlatSVGIcon("Frontend/assets/notificationBell.svg", 28, 28);
        BellIcon.setColorFilter(new FlatSVGIcon.ColorFilter(ColorObject -> ColorTextPrimary));
        JLabel BellLabel = new JLabel(BellIcon);
        BellLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        NotificationPopup NotifPopup = new NotificationPopup(AdminId);

        BellLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!NotifPopup.isVisible()) {
                    NotifPopup.LoadNotifications();
                    NotifPopup.show(BellLabel, e.getX() - 300, e.getY() + 30);
                }
            }
        });
        RightSidePanel.add(BellLabel);

        FlatSVGIcon ProfileIcon = new FlatSVGIcon("Frontend/assets/pfp.svg", 28, 28);
        ProfileIcon.setColorFilter(new FlatSVGIcon.ColorFilter(ColorObject -> ColorTextPrimary));

        JLabel ProfileLabel = new JLabel(ProfileIcon);
        ProfileLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ProfileLabel.setToolTipText("View Profile");

        ProfileLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent EventObject) {
                CurrentCenterLabel = "Profile";
                CenterCards.show(CenterHolder, "Profile");

                if (PageTitleLabel != null) {
                    PageTitleLabel.setText(" Admin Profile");
                }
            }
        });

        RightSidePanel.add(ProfileLabel);

        TopBar.add(RightSidePanel, BorderLayout.EAST);
        return TopBar;
    }

    private JComponent CreateDashboardGrid() {
        JPanel GridPanel = new JPanel(new GridBagLayout());
        GridPanel.setOpaque(false);
        GridBagConstraints Constraints = new GridBagConstraints();
        Constraints.insets = new Insets(5, 10, 10, 10);
        Constraints.fill = GridBagConstraints.BOTH;

        Constraints.gridx = 0;
        Constraints.gridy = 0;
        Constraints.weightx = 0.7;
        Constraints.weighty = 0.28;
        Constraints.gridheight = 1;
        GridPanel.add(CreateKpiRow(), Constraints);

        Constraints.gridx = 0;
        Constraints.gridy = 1;
        Constraints.weighty = 0.42;
        Constraints.gridheight = 1;
        GridPanel.add(CreatePendingApprovalsPanel(), Constraints);

        Constraints.gridx = 0;
        Constraints.gridy = 2;
        Constraints.weighty = 0.30;
        Constraints.gridheight = 1;
        GridPanel.add(CreateQuickActionsPanel(), Constraints);

        Constraints.gridx = 1;
        Constraints.gridy = 0;
        Constraints.weightx = 0.3;
        Constraints.weighty = 0.30;
        Constraints.gridheight = 1;
        GridPanel.add(CreateCollectionsPanel(), Constraints);

        Constraints.gridx = 1;
        Constraints.gridy = 1;
        Constraints.weighty = 0.35;
        Constraints.gridheight = 1;
        GridPanel.add(CreateRecentActivityPanel(), Constraints);

        Constraints.gridx = 1;
        Constraints.gridy = 2;
        Constraints.weighty = 0.35;
        Constraints.gridheight = 1;
        GridPanel.add(CreateSystemHealthPanel(), Constraints);

        return GridPanel;
    }

    private JComponent CreateModulePanel(String Title) {
        JPanel PanelObject = new RoundedPanel();
        PanelObject.setBackground(ColorPanel);
        PanelObject.setLayout(new BorderLayout(10, 10));
        PanelObject.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel TitleLabel = new JLabel(Title);
        TitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        TitleLabel.setForeground(ColorTextPrimary);
        PanelObject.add(TitleLabel, BorderLayout.NORTH);
        return PanelObject;
    }

    private JComponent CreateKpiRow() {
        JPanel WrapperPanel = (JPanel) CreateModulePanel("Key Metrics (Today)");
        JPanel RowPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        RowPanel.setOpaque(false);
        RowPanel.add(CreateKpiCard("Active Students", "3,482", "+24 from yesterday"));
        RowPanel.add(CreateKpiCard("Pending Approvals", "17", "Admissions, Leaves, Fees"));
        RowPanel.add(CreateKpiCard("Collection", "₹12.4L", "Settled: ₹9.1L"));
        RowPanel.add(CreateKpiCard("Open Tickets", "9", "Avg. response 2h"));
        WrapperPanel.add(RowPanel, BorderLayout.CENTER);
        return WrapperPanel;
    }

    private JComponent CreateKpiCard(String Title, String Value, String Subtext) {
        JPanel CardPanel = new RoundedPanel();
        CardPanel.setBackground(ColorBackground);
        CardPanel.setLayout(new BoxLayout(CardPanel, BoxLayout.Y_AXIS));
        CardPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel TitleLabel = new JLabel(Title);
        TitleLabel.setForeground(ColorTextSecondary);
        TitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JLabel ValueLabel = new JLabel(Value);
        ValueLabel.setForeground(ColorTextPrimary);
        ValueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));

        JLabel SubtextLabel = new JLabel(Subtext);
        SubtextLabel.setForeground(ColorTextSecondary);
        SubtextLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        TitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        ValueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        SubtextLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        CardPanel.add(TitleLabel);
        CardPanel.add(Box.createVerticalStrut(6));
        CardPanel.add(ValueLabel);
        CardPanel.add(Box.createVerticalStrut(4));
        CardPanel.add(SubtextLabel);
        return CardPanel;
    }

    private JComponent CreatePendingApprovalsPanel() {
        JPanel PanelObject = (JPanel) CreateModulePanel("Pending Approvals");
        String[] Columns = {"Type", "Requestor", "Details", "Requested On", "Status"};
        Object[][] Rows = {{"Leave", "Ananya S.", "Medical leave - 3 days", "2025-10-18", "Awaiting"}, {"Admission", "#ADM-9321", "B.Tech CSE (Lateral)", "2025-10-18", "Docs Review"}, {"Fee Waiver", "Rohit K.", "Merit scholarship 25%", "2025-10-17", "Finance"}, {"Purchase", "Lab Admin", "10× RTX 4070 PCs", "2025-10-17", "Director"}, {"Event", "Cultural Sec.", "TechFest budget", "2025-10-16", "Accounts"},
        };
        JTable TableObject = StyledTable(Columns, Rows);
        PanelObject.add(new JScrollPane(TableObject), BorderLayout.CENTER);
        return PanelObject;
    }

    private JComponent CreateQuickActionsPanel() {
        JPanel PanelObject = (JPanel) CreateModulePanel("Quick Actions");
        JPanel GridPanel = new JPanel(new GridLayout(2, 2, 12, 12));
        GridPanel.setOpaque(false);
        GridPanel.add(PrimaryButton("Add User"));
        GridPanel.add(PrimaryButton("Create Course"));
        GridPanel.add(PrimaryButton("Post Announcement"));
        GridPanel.add(PrimaryButton("Backup Now"));
        PanelObject.add(GridPanel, BorderLayout.CENTER);
        return PanelObject;
    }

    private JButton PrimaryButton(String Text) {
        JButton ButtonObject = new JButton(Text);
        ButtonObject.setFont(new Font("SansSerif", Font.BOLD, 14));
        ButtonObject.setFocusPainted(false);
        ButtonObject.setBackground(ColorAccent);
        ButtonObject.setForeground(Color.BLACK);
        ButtonObject.setBorder(new EmptyBorder(12, 16, 12, 16));
        ButtonObject.setCursor(new Cursor(Cursor.HAND_CURSOR));

        ButtonObject.addActionListener(EventObject -> {
            switch (Text) {
                case "Add User":
                    CurrentCenterLabel = "Users";
                    CenterCards.show(CenterHolder, "Users");
                    if (PageTitleLabel != null) PageTitleLabel.setText(" Users");
                    break;

                case "Create Course":
                    CurrentCenterLabel = "Courses";
                    CenterCards.show(CenterHolder, "Courses");
                    if (PageTitleLabel != null) PageTitleLabel.setText(" Courses");
                    break;
            }
        });

        return ButtonObject;
    }

    private JComponent CreateCollectionsPanel() {
        JPanel PanelObject = (JPanel) CreateModulePanel("Fee Collections (This Week)");
        String[] Columns = {"Date", "Receipts", "Amount"};
        Object[][] Rows = {{"Mon", 86, "₹1.9L"}, {"Tue", 102, "₹2.3L"}, {"Wed", 77, "₹1.6L"}, {"Thu", 95, "₹2.1L"}, {"Fri", 110, "₹2.5L"},
        };
        JTable TableObject = StyledTable(Columns, Rows);
        PanelObject.add(new JScrollPane(TableObject), BorderLayout.CENTER);
        return PanelObject;
    }

    private JComponent CreateRecentActivityPanel() {
        JPanel PanelObject = (JPanel) CreateModulePanel("Recent Activity");
        String[] Columns = {"Time", "Actor", "Action"};
        Object[][] Rows = {{"16:05", "admin", "Created role 'Finance Analyst'"}, {"15:40", "exam_head", "Published Mid-Sem Results (CSE)"}, {"14:12", "it_ops", "Restarted Mail Server"}, {"12:55", "accounts", "Processed 43 fee receipts"}, {"09:30", "admin", "Added user 'rkumar' to Dept. ECE"},
        };
        JTable TableObject = StyledTable(Columns, Rows);
        PanelObject.add(new JScrollPane(TableObject), BorderLayout.CENTER);
        return PanelObject;
    }

    private JComponent CreateSystemHealthPanel() {
        JPanel PanelObject = (JPanel) CreateModulePanel("System Health");

        JPanel ContentPanel = new JPanel();
        ContentPanel.setOpaque(false);
        ContentPanel.setLayout(new BoxLayout(ContentPanel, BoxLayout.Y_AXIS));

        String[] DatabaseStatus = GetDatabaseStatus();
        ContentPanel.add(HealthRow("Database", DatabaseStatus[0], DatabaseStatus[1]));
        ContentPanel.add(Box.createVerticalStrut(8));

        ContentPanel.add(HealthRow("Auth Service", "OK", "Using built-in auth"));
        ContentPanel.add(Box.createVerticalStrut(8));

        ContentPanel.add(HealthRow("Mail Queue", "N/A", "No monitoring hooked yet"));
        ContentPanel.add(Box.createVerticalStrut(8));

        ContentPanel.add(HealthRow("Backups", "N/A", "Status not tracked here"));

        PanelObject.add(ContentPanel, BorderLayout.CENTER);
        return PanelObject;
    }

    private String[] GetDatabaseStatus() {
        long StartTime = System.currentTimeMillis();
        try {
            SemestersHandler.IsMaintenanceMode();
            long Elapsed = System.currentTimeMillis() - StartTime;
            return new String[]{"OK", "Response " + Elapsed + " ms"};
        }
        catch (SQLException ExceptionObject) {
            String Message = ExceptionObject.getMessage();
            if (Message == null || Message.isEmpty()) Message = ExceptionObject.getClass().getSimpleName();
            return new String[]{"DOWN", Message};
        }
    }

    private JComponent HealthRow(String Name, String State, String Detail) {
        JPanel RowPanel = new RoundedPanel();
        RowPanel.setBackground(ColorBackground);
        RowPanel.setLayout(new BorderLayout());
        RowPanel.setBorder(new EmptyBorder(10, 12, 10, 12));

        JLabel NameLabel = new JLabel(Name);
        NameLabel.setForeground(ColorTextPrimary);
        NameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel StateLabel = new JLabel(State + " • " + Detail);
        StateLabel.setForeground(ColorTextSecondary);
        StateLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        RowPanel.add(NameLabel, BorderLayout.WEST);
        RowPanel.add(StateLabel, BorderLayout.EAST);
        return RowPanel;
    }

    private JTable StyledTable(String[] Columns, Object[][] Data) {
        DefaultTableModel ModelObject = new DefaultTableModel(Data, Columns) {
            @Override
            public boolean isCellEditable(int RowIndex, int ColumnIndex) {
                return false;
            }
        };
        JTable TableObject = new JTable(ModelObject);
        TableObject.setFillsViewportHeight(true);
        TableObject.setRowHeight(28);
        TableObject.setBackground(ColorBackground);
        TableObject.setForeground(ColorTextPrimary);
        TableObject.setGridColor(new Color(0x2A2A2A));
        TableObject.setSelectionBackground(new Color(0x2A2A2A));
        TableObject.setSelectionForeground(ColorTextPrimary);
        TableObject.getTableHeader().setBackground(ColorPanel);
        TableObject.getTableHeader().setForeground(ColorTextSecondary);
        TableObject.getTableHeader().setReorderingAllowed(false);
        return TableObject;
    }

    private static class RoundedPanel extends JPanel {
        private int CornerRadius = 15;

        public RoundedPanel() {
            super();
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics GraphicsObject) {
            Graphics2D Graphics2DObject = (Graphics2D) GraphicsObject.create();
            Graphics2DObject.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Graphics2DObject.setColor(getBackground());
            Graphics2DObject.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CornerRadius, CornerRadius));
            Graphics2DObject.dispose();
            super.paintComponent(GraphicsObject);
        }
    }

    public static void main(String[] Arguments) {
        try {
            com.formdev.flatlaf.FlatDarkLaf.setup();
        }
        catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new DashboardUIAdmin(1).setVisible(true));
    }
}