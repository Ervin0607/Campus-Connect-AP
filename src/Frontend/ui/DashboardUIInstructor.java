package Frontend.ui;

import Frontend.InstructorPages.AnnouncementPages;
import Frontend.InstructorPages.InstructorProfilePanel;
import Frontend.InstructorPages.MySections;
import Frontend.components.CalendarWidget;
import Frontend.components.LeftNavPanelInstructor;
import Frontend.components.NotificationPopup;
import Backend.DataBaseHandler.InstructorHandler;
import Backend.DataBaseHandler.SemestersHandler;
import Backend.DataBaseHandler.OfferingHandler;
import Backend.DataBaseHandler.CourseHandler;
import Backend.domain.Instructor;
import Backend.domain.Offerings;
import Backend.domain.Course;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

public class DashboardUIInstructor extends JFrame {

    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel = new Color(0x1E1E1E);
    private static final Color ColorAccent = new Color(0xB0F2B4);
    private static final Color ColorTextPrimary = new Color(0xFFFFFF);
    private static final Color ColorTextSecondary = new Color(0xB0B0B0);
    private static final Color ColorWarning = new Color(0xFFD54F);

    private final CardLayout CenterCards = new CardLayout();
    private final JPanel CenterHolder = new JPanel(CenterCards);

    private final int InstructorId;
    private String InstructorName = "-";
    private String CurrentCenterLabel = "Dashboard";

    private JLabel PageTitleLabel;

    private final DateTimeFormatter AnnDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DashboardUIInstructor(int InstructorId) {
        this.InstructorId = InstructorId;

        setTitle("Campus Connect — Instructor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ColorBackground);
        setLayout(new BorderLayout());

        try {
            Instructor InstructorObject = InstructorHandler.FindInstructorByUserID(InstructorId);
            if (InstructorObject != null) {
                InstructorName = Objects.toString(InstructorObject.GetName(), "-");
            }
        }
        catch (SQLException ExceptionObject) {
            ExceptionObject.printStackTrace();
            InstructorName = "-";
        }

        add(CreateLeftNavigation(), BorderLayout.WEST);
        add(CreateMainContent(), BorderLayout.CENTER);
    }

    private JComponent CreateLeftNavigation() {
        LeftNavPanelInstructor.Theme ThemeObject = new LeftNavPanelInstructor.Theme(ColorPanel, ColorAccent, ColorTextPrimary, ColorTextSecondary);

        LeftNavPanelInstructor NavigationPanel = new LeftNavPanelInstructor(ThemeObject, "Dashboard");

        NavigationPanel.SetOnSelect(Label -> {
            if ("Log Out".equals(Label)) {
                ConfirmAndLogout();
            }
            else {
                CurrentCenterLabel = Label;
                CenterCards.show(CenterHolder, Label);
                NavigationPanel.SetActive(Label);

                if (PageTitleLabel != null) {
                    PageTitleLabel.setText(" " + Label);
                }
            }
        });

        return NavigationPanel;
    }

    private void ConfirmAndLogout() {
        int Choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (Choice == JOptionPane.YES_OPTION) {
            SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
            this.dispose();
        }
    }

    private JComponent CreateMainContent() {
        JPanel MainPanel = new JPanel(new BorderLayout(25, 10));
        MainPanel.setBackground(ColorBackground);
        MainPanel.setBorder(new EmptyBorder(20, 25, 25, 25));

        MainPanel.add(CreateTopBar(), BorderLayout.NORTH);

        CenterHolder.setOpaque(false);

        CenterHolder.add(CreateDashboardGrid(), "Dashboard");
        CenterHolder.add(new MySections(InstructorId), "My Sections");
        CenterHolder.add(new AnnouncementPages(InstructorId), "Announcements");
        CenterHolder.add(new InstructorProfilePanel(InstructorId), "Profile");

        MainPanel.add(CenterHolder, BorderLayout.CENTER);
        CenterCards.show(CenterHolder, "Dashboard");
        return MainPanel;
    }

    private JComponent CreateTopBar() {
        JPanel TopBar = new JPanel(new BorderLayout());
        TopBar.setOpaque(false);

        PageTitleLabel = new JLabel(" Instructor Dashboard");
        PageTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        PageTitleLabel.setForeground(ColorTextPrimary);
        TopBar.add(PageTitleLabel, BorderLayout.WEST);

        JPanel RightSidePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 0));
        RightSidePanel.setOpaque(false);

        try {
            if (SemestersHandler.IsMaintenanceMode()) {
                JLabel WarningLabel = new JLabel("⚠️ Maintenance Mode");
                WarningLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                WarningLabel.setForeground(ColorWarning);
                WarningLabel.setBorder(new EmptyBorder(0, 0, 0, 20));
                RightSidePanel.add(WarningLabel);
            }
        }
        catch (SQLException ExceptionObject) {
            ExceptionObject.printStackTrace();
        }



        FlatSVGIcon BellIcon = new FlatSVGIcon("Frontend/assets/notificationBell.svg", 28, 28);
        BellIcon.setColorFilter(new FlatSVGIcon.ColorFilter(ColorObject -> ColorTextPrimary));
        JLabel BellLabel = new JLabel(BellIcon);
        BellLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        NotificationPopup NotifPopup = new NotificationPopup(InstructorId);

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
                    PageTitleLabel.setText(" Instructor Profile");
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
        Constraints.gridwidth = 2;
        Constraints.weightx = 1.0;
        Constraints.weighty = 0.12;
        GridPanel.add(CreateStatsStrip(), Constraints);

        Constraints.gridy = 1;
        Constraints.gridwidth = 1;
        Constraints.weighty = 0.88;

        Constraints.gridx = 0;
        Constraints.weightx = 0.45;
        GridPanel.add(CreateLeftColumnStack(), Constraints);

        Constraints.gridx = 1;
        Constraints.weightx = 0.55;
        GridPanel.add(CreateRightColumnStack(), Constraints);

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

    private JComponent CreateStatsStrip() {
        JPanel StripPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        StripPanel.setOpaque(false);

        int SectionsCount = GetInstructorOfferings().size();
        int TotalStudents = EstimateTotalStudents();
        int GradedThisWeek = 0;
        int PendingGrading = 0;

        StripPanel.add(CreateStatCard("Sections", String.valueOf(SectionsCount)));
        StripPanel.add(CreateStatCard("Students", String.valueOf(TotalStudents)));
        StripPanel.add(CreateStatCard("Graded This Week", String.valueOf(GradedThisWeek)));
        StripPanel.add(CreateStatCard("Pending Grading", String.valueOf(PendingGrading)));
        return StripPanel;
    }

    private JComponent CreateStatCard(String Title, String Value) {
        JPanel CardPanel = new RoundedPanel();
        CardPanel.setBackground(ColorPanel);
        CardPanel.setLayout(new BorderLayout(8, 8));
        CardPanel.setBorder(new EmptyBorder(14, 16, 14, 16));
        JLabel TitleLabel = new JLabel(Title);
        TitleLabel.setForeground(ColorTextSecondary);
        TitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JLabel ValueLabel = new JLabel(Value);
        ValueLabel.setForeground(ColorTextPrimary);
        ValueLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        CardPanel.add(TitleLabel, BorderLayout.NORTH);
        CardPanel.add(ValueLabel, BorderLayout.CENTER);
        return CardPanel;
    }

    private int EstimateTotalStudents() {
        int Total = 0;
        for (Offerings OfferingObject : GetInstructorOfferings()) {
            Total += OfferingObject.GetCurrentEnrollment();
        }
        return Total;
    }

    private JComponent CreateLeftColumnStack() {
        JPanel ColumnPanel = new JPanel(new GridBagLayout());
        ColumnPanel.setOpaque(false);

        GridBagConstraints Constraints = new GridBagConstraints();
        Constraints.gridx = 0;
        Constraints.fill = GridBagConstraints.BOTH;
        Constraints.insets = new Insets(0, 0, 8, 0);

        Constraints.gridy = 0;
        Constraints.weightx = 1.0;
        Constraints.weighty = 0.45;
        ColumnPanel.add(CreateMySectionsOverviewPanel(), Constraints);

        Constraints.gridy = 1;
        Constraints.weighty = 0.55;
        ColumnPanel.add(CreateMessagesPanel(), Constraints);

        return ColumnPanel;
    }

    private JComponent CreateMySectionsOverviewPanel() {
        JPanel PanelObject = (JPanel) CreateModulePanel("My Sections — Overview");

        JPanel ListPanel = new JPanel();
        ListPanel.setOpaque(false);
        ListPanel.setLayout(new BoxLayout(ListPanel, BoxLayout.Y_AXIS));

        List<Offerings> OfferingsList = GetInstructorOfferings();
        if (OfferingsList.isEmpty()) {
            JLabel NoneLabel = new JLabel("You are not teaching any sections currently.");
            NoneLabel.setForeground(ColorTextSecondary);
            NoneLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
            ListPanel.add(NoneLabel);
        }
        else {
            for (Offerings OfferingObject : OfferingsList) {
                try {
                    Course CourseObject = CourseHandler.FindCourseByID(OfferingObject.GetCourseID());
                    String Code = CourseObject != null ? String.valueOf(CourseObject.GetCode()) : "";
                    String Title = CourseObject != null ? CourseObject.GetTitle() : "Unknown Course";

                    String Header = Code + " • " + Title;
                    String SubTitle = "Sem " + OfferingObject.GetSemester() + " " + OfferingObject.GetYear()
                            + " • " + OfferingObject.GetCurrentEnrollment() + "/" + OfferingObject.GetCapacity() + " students";

                    ListPanel.add(MakeLine(Header, SubTitle));
                    ListPanel.add(Box.createVerticalStrut(6));
                }
                catch (SQLException ExceptionObject) {
                    ExceptionObject.printStackTrace();
                }
            }
        }

        JScrollPane ScrollPaneObject = new JScrollPane(ListPanel);
        ScrollPaneObject.setOpaque(false);
        ScrollPaneObject.getViewport().setOpaque(false);
        ScrollPaneObject.setBorder(BorderFactory.createEmptyBorder());

        PanelObject.add(ScrollPaneObject, BorderLayout.CENTER);
        return PanelObject;
    }

    private JComponent CreateMessagesPanel() {
        JPanel PanelObject = (JPanel) CreateModulePanel("Class Messages / Announcements");

        JPanel ListPanel = new JPanel();
        ListPanel.setOpaque(false);
        ListPanel.setLayout(new BoxLayout(ListPanel, BoxLayout.Y_AXIS));

        List<InstructorAnnouncement> AnnouncementsList = LoadInstructorAnnouncements();

        if (AnnouncementsList.isEmpty()) {
            JLabel NoneLabel = new JLabel("You haven't posted any announcements yet.", SwingConstants.LEFT);
            NoneLabel.setForeground(ColorTextSecondary);
            NoneLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
            ListPanel.add(NoneLabel);
        }
        else {
            for (InstructorAnnouncement AnnouncementObject : AnnouncementsList) {
                String PrimaryText = AnnouncementObject.CourseCode + " • " + AnnouncementObject.CourseTitle;
                String SecondaryText = (AnnouncementObject.RawDate == null || AnnouncementObject.RawDate.isEmpty()
                        ? "" : (AnnouncementObject.RawDate + " — ")) + Shorten(AnnouncementObject.Message, 80);
                ListPanel.add(MakeLine(PrimaryText, SecondaryText));
                ListPanel.add(Box.createVerticalStrut(4));
            }
        }

        JScrollPane ScrollPaneObject = new JScrollPane(ListPanel);
        ScrollPaneObject.setOpaque(false);
        ScrollPaneObject.getViewport().setOpaque(false);
        ScrollPaneObject.setBorder(BorderFactory.createEmptyBorder());

        PanelObject.add(ScrollPaneObject, BorderLayout.CENTER);
        return PanelObject;
    }

    private JComponent CreateRightColumnStack() {
        JPanel ColumnPanel = new JPanel(new GridBagLayout());
        ColumnPanel.setOpaque(false);

        GridBagConstraints Constraints = new GridBagConstraints();
        Constraints.gridx = 0;
        Constraints.fill = GridBagConstraints.BOTH;
        Constraints.insets = new Insets(0, 0, 8, 0);

        Constraints.gridy = 0;
        Constraints.weightx = 1.0;
        Constraints.weighty = 0.45;
        ColumnPanel.add(CreateMiniCalendarPanel(), Constraints);

        Constraints.gridy = 1;
        Constraints.weighty = 0.30;
        ColumnPanel.add(CreateMyClassesTomorrowPanel(), Constraints);

        Constraints.gridy = 2;
        Constraints.weighty = 0.25;
        ColumnPanel.add(CreateQuickActionsPanel(), Constraints);

        return ColumnPanel;
    }

    private JComponent CreateMiniCalendarPanel() {
        JPanel PanelObject = (JPanel) CreateModulePanel("Calendar (This Month)");
        CalendarWidget.Theme ThemeObject = new CalendarWidget.Theme();
        ThemeObject.Background = ColorBackground;
        ThemeObject.Panel = ColorPanel;
        ThemeObject.TextPrimary = ColorTextPrimary;
        ThemeObject.TextSecondary = ColorTextSecondary;
        ThemeObject.Accent = ColorAccent;

        CalendarWidget CalendarObject = new CalendarWidget(YearMonth.now(), ThemeObject);
        CalendarObject.setPreferredSize(new Dimension(360, 280));

        JPanel WrapperPanel = new JPanel(new BorderLayout());
        WrapperPanel.setOpaque(false);
        WrapperPanel.add(CalendarObject, BorderLayout.CENTER);
        PanelObject.add(WrapperPanel, BorderLayout.CENTER);
        return PanelObject;
    }

    private static class InstructorTomorrowClass {
        String CourseCode;
        String CourseTitle;
        boolean IsLab;
        String StartString;
        String EndString;
        int StartMinutes;
    }

    private JComponent CreateMyClassesTomorrowPanel() {
        JPanel PanelObject = (JPanel) CreateModulePanel("My Classes Tomorrow");

        JPanel ListPanel = new JPanel();
        ListPanel.setOpaque(false);
        ListPanel.setLayout(new BoxLayout(ListPanel, BoxLayout.Y_AXIS));

        List<InstructorTomorrowClass> ClassesList = LoadInstructorTomorrowClasses();

        if (ClassesList.isEmpty()) {
            JLabel NoneLabel = new JLabel("No classes scheduled for tomorrow.", SwingConstants.LEFT);
            NoneLabel.setForeground(ColorTextSecondary);
            NoneLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
            ListPanel.add(NoneLabel);
        }
        else {
            for (InstructorTomorrowClass ClassItem : ClassesList) {
                ListPanel.add(MakeTomorrowClassRow(ClassItem));
                ListPanel.add(Box.createVerticalStrut(4));
            }
        }

        JScrollPane ScrollPaneObject = new JScrollPane(ListPanel);
        ScrollPaneObject.setOpaque(false);
        ScrollPaneObject.getViewport().setOpaque(false);
        ScrollPaneObject.setBorder(BorderFactory.createEmptyBorder());

        PanelObject.add(ScrollPaneObject, BorderLayout.CENTER);
        return PanelObject;
    }

    private JComponent MakeTomorrowClassRow(InstructorTomorrowClass ClassItem) {
        JPanel CardPanel = new RoundedPanel();
        CardPanel.setBackground(ColorBackground);
        CardPanel.setLayout(new BorderLayout(6, 3));
        CardPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        String TypeLabelText = ClassItem.IsLab ? "Lab" : "Lecture";

        JLabel TimeLabel = new JLabel(ClassItem.StartString + " - " + ClassItem.EndString);
        TimeLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        TimeLabel.setForeground(ColorAccent);

        JLabel CourseLabel = new JLabel(ClassItem.CourseCode + " • " + ClassItem.CourseTitle);
        CourseLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        CourseLabel.setForeground(ColorTextPrimary);

        JLabel TypeLabel = new JLabel(TypeLabelText);
        TypeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        TypeLabel.setForeground(ColorTextSecondary);

        JPanel TopPanel = new JPanel(new BorderLayout());
        TopPanel.setOpaque(false);
        TopPanel.add(TimeLabel, BorderLayout.WEST);
        TopPanel.add(TypeLabel, BorderLayout.EAST);

        CardPanel.add(TopPanel, BorderLayout.NORTH);
        CardPanel.add(CourseLabel, BorderLayout.SOUTH);

        return CardPanel;
    }

    private JComponent CreateQuickActionsPanel() {
        JPanel PanelObject = (JPanel) CreateModulePanel("Quick Actions");

        JPanel ButtonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        ButtonPanel.setOpaque(false);

        ButtonPanel.add(MakeActionButton("Open My Sections", () -> {
            CurrentCenterLabel = "My Sections";
            CenterCards.show(CenterHolder, "My Sections");
            if (PageTitleLabel != null) PageTitleLabel.setText(" My Sections");
        }));

        ButtonPanel.add(MakeActionButton("Post Announcement", () -> {
            CurrentCenterLabel = "Announcements";
            CenterCards.show(CenterHolder, "Announcements");
            if (PageTitleLabel != null) PageTitleLabel.setText(" Announcements");
            JOptionPane.showMessageDialog(
                    this,
                    "Select a section on the left, then type and post your announcement.",
                    "Post Announcement",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }));

        ButtonPanel.add(MakeActionButton("Profile", () -> {
            CurrentCenterLabel = "Profile";
            CenterCards.show(CenterHolder, "Profile");
            if (PageTitleLabel != null) PageTitleLabel.setText(" Instructor Profile");
        }));

        ButtonPanel.add(MakeActionButton("Log Out", this::ConfirmAndLogout));

        PanelObject.add(ButtonPanel, BorderLayout.CENTER);
        return PanelObject;
    }

    private JButton MakeActionButton(String Text, Runnable Action) {
        JButton ButtonObject = new JButton(Text);
        ButtonObject.setFocusPainted(false);
        ButtonObject.setBackground(ColorAccent);
        ButtonObject.setForeground(Color.BLACK);
        ButtonObject.setFont(new Font("SansSerif", Font.BOLD, 14));
        ButtonObject.setBorder(new EmptyBorder(10, 14, 10, 14));
        ButtonObject.setContentAreaFilled(true);
        ButtonObject.setOpaque(true);
        ButtonObject.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ButtonObject.addActionListener(EventObject -> {
            if (Action != null) Action.run();
        });

        return ButtonObject;
    }

    private JComponent MakeLine(String PrimaryText, String SecondaryText) {
        JPanel RowPanel = new RoundedPanel();
        RowPanel.setBackground(ColorBackground);
        RowPanel.setLayout(new BorderLayout(8, 4));
        RowPanel.setBorder(new EmptyBorder(8, 10, 8, 10));

        JLabel PrimaryLabel = new JLabel(PrimaryText);
        PrimaryLabel.setForeground(ColorTextPrimary);
        PrimaryLabel.setFont(new Font("SansSerif", Font.BOLD, 15));

        JLabel SecondaryLabel = new JLabel(SecondaryText);
        SecondaryLabel.setForeground(ColorTextSecondary);
        SecondaryLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel TextPanel = new JPanel();
        TextPanel.setOpaque(false);
        TextPanel.setLayout(new BoxLayout(TextPanel, BoxLayout.Y_AXIS));
        TextPanel.add(PrimaryLabel);
        TextPanel.add(Box.createVerticalStrut(3));
        TextPanel.add(SecondaryLabel);

        RowPanel.add(TextPanel, BorderLayout.CENTER);
        return RowPanel;
    }


    private String Shorten(String Text, int MaxLength) {
        if (Text == null) return "";
        String TrimmedText = Text.replace("\n", " ").trim();
        if (TrimmedText.length() <= MaxLength) return TrimmedText;
        return TrimmedText.substring(0, MaxLength - 3) + "...";
    }

    private List<Offerings> GetInstructorOfferings() {
        List<Offerings> ResultList = new ArrayList<>();
        try {
            List<Offerings> AllOfferings = OfferingHandler.GetAllOfferings();
            for (Offerings OfferingObject : AllOfferings) {
                if (OfferingObject.GetInstructorID() == InstructorId) {
                    ResultList.add(OfferingObject);
                }
            }
        }
        catch (SQLException ExceptionObject) {
            ExceptionObject.printStackTrace();
        }
        ResultList.sort(Comparator.comparing(Offerings::GetYear).thenComparing(Offerings::GetSemester).thenComparing(Offerings::GetOfferingID));
        return ResultList;
    }

    private static class InstructorAnnouncement {
        LocalDate Date;
        String RawDate;
        String Message;
        String CourseCode;
        String CourseTitle;

        InstructorAnnouncement(LocalDate AnnouncementDate, String RawDateValue, String MessageValue,
                               String CourseCodeValue, String CourseTitleValue) {
            this.Date = AnnouncementDate;
            this.RawDate = RawDateValue;
            this.Message = MessageValue;
            this.CourseCode = CourseCodeValue;
            this.CourseTitle = CourseTitleValue;
        }
    }

    private LocalDate ParseAnnouncementDate(String DateString) {
        if (DateString == null || DateString.isEmpty()) return null;
        try {
            return LocalDate.parse(DateString, AnnDateFormatter);
        } catch (DateTimeParseException ExceptionObject) {
            System.err.println("Invalid announcement date: " + DateString);
            return null;
        }
    }

    private List<InstructorAnnouncement> LoadInstructorAnnouncements() {
        List<InstructorAnnouncement> ResultList = new ArrayList<>();
        List<Offerings> OfferingsList = GetInstructorOfferings();
        if (OfferingsList.isEmpty()) return ResultList;

        for (Offerings OfferingObject : OfferingsList) {
            Course CourseObject;
            try {
                CourseObject = CourseHandler.FindCourseByID(OfferingObject.GetCourseID());
            }
            catch (SQLException ExceptionObject) {
                ExceptionObject.printStackTrace();
                continue;
            }
            String Code = CourseObject != null ? String.valueOf(CourseObject.GetCode()) : "";
            String Title = CourseObject != null ? CourseObject.GetTitle() : "Unknown Course";

            List<Map<String, String>> AnnouncementsList;
            try {
                AnnouncementsList = OfferingHandler.GetAnnouncementsForOffering(OfferingObject.GetOfferingID());
            }
            catch (SQLException ExceptionObject) {
                ExceptionObject.printStackTrace();
                continue;
            }

            if (AnnouncementsList == null) continue;

            for (Map<String, String> AnnouncementMap : AnnouncementsList) {
                String MessageValue = AnnouncementMap.getOrDefault("Data", "").trim();
                if (MessageValue.isEmpty()) continue;

                String RawDateValue = AnnouncementMap.getOrDefault("Date", "").trim();
                LocalDate AnnouncementDate = ParseAnnouncementDate(RawDateValue);

                ResultList.add(new InstructorAnnouncement(AnnouncementDate, RawDateValue, MessageValue, Code, Title));
            }
        }

        ResultList.sort((FirstAnnouncement, SecondAnnouncement) -> {
            if (FirstAnnouncement.Date == null && SecondAnnouncement.Date == null) return 0;
            if (FirstAnnouncement.Date == null) return 1;
            if (SecondAnnouncement.Date == null) return -1;
            return SecondAnnouncement.Date.compareTo(FirstAnnouncement.Date);
        });

        int MaximumAnnouncements = 7;
        if (ResultList.size() > MaximumAnnouncements) {
            return ResultList.subList(0, MaximumAnnouncements);
        }
        return ResultList;
    }

    private static class ScheduleEntry {
        String Day;
        int StartMinutes;
        int EndMinutes;
        String StartString;
        String EndString;
        boolean IsLab;
    }

    private String NormalizeJson(String JsonString) {
        if (JsonString == null) return null;
        String TrimmedString = JsonString.trim();
        if (TrimmedString.isEmpty()) return null;
        if ("null".equalsIgnoreCase(TrimmedString)) return null;
        return TrimmedString;
    }

    private List<ScheduleEntry> ParseScheduleJson(String JsonString, boolean IsLabFlag) {
        List<ScheduleEntry> ResultList = new ArrayList<>();
        if (JsonString == null) return ResultList;

        String TrimmedString = JsonString.trim();
        if (TrimmedString.isEmpty()) return ResultList;

        if (TrimmedString.startsWith("[")) TrimmedString = TrimmedString.substring(1);
        if (TrimmedString.endsWith("]")) TrimmedString = TrimmedString.substring(0, TrimmedString.length() - 1);
        if (TrimmedString.isEmpty()) return ResultList;

        List<String> ObjectStrings = new ArrayList<>();
        int BraceCount = 0;
        StringBuilder CurrentBuilder = new StringBuilder();
        for (int Index = 0; Index < TrimmedString.length(); Index++) {
            char Character = TrimmedString.charAt(Index);
            if (Character == '{') BraceCount++;
            if (Character == '}') BraceCount--;
            CurrentBuilder.append(Character);
            if (BraceCount == 0 && Character == '}') {
                ObjectStrings.add(CurrentBuilder.toString());
                CurrentBuilder.setLength(0);
            }
        }

        for (String ObjectString : ObjectStrings) {
            String DayString = ExtractJsonString(ObjectString, "day");
            String StartString = ExtractJsonString(ObjectString, "start");
            String EndString = ExtractJsonString(ObjectString, "end");
            if (DayString == null || StartString == null || EndString == null) continue;

            ScheduleEntry EntryObject = new ScheduleEntry();
            EntryObject.Day = DayString;
            EntryObject.StartString = StartString;
            EntryObject.EndString = EndString;
            EntryObject.StartMinutes = ParseTimeMinutes(StartString);
            EntryObject.EndMinutes = ParseTimeMinutes(EndString);
            EntryObject.IsLab = IsLabFlag;
            ResultList.add(EntryObject);
        }
        return ResultList;
    }

    private String ExtractJsonString(String JsonObject, String Key) {
        String Pattern = "\"" + Key + "\"";
        int Index = JsonObject.indexOf(Pattern);
        if (Index < 0) return null;
        Index = JsonObject.indexOf(':', Index);
        if (Index < 0) return null;
        Index++;
        while (Index < JsonObject.length() && Character.isWhitespace(JsonObject.charAt(Index))) Index++;
        if (Index >= JsonObject.length() || JsonObject.charAt(Index) != '"') return null;
        Index++;
        int EndIndex = JsonObject.indexOf('"', Index);
        if (EndIndex < 0) return null;
        return JsonObject.substring(Index, EndIndex);
    }

    private int ParseTimeMinutes(String TimeString) {
        try {
            String[] Parts = TimeString.split(":");
            int Hour = Integer.parseInt(Parts[0].trim());
            int Minute = Integer.parseInt(Parts[1].trim());
            return Hour * 60 + Minute;
        }
        catch (Exception ExceptionObject) {
            return 8 * 60;
        }
    }

    private int DayToIndex(String DayString) {
        if (DayString == null) return -1;
        String LowerCaseDay = DayString.trim().toLowerCase(Locale.ROOT);
        if (LowerCaseDay.startsWith("mon")) return 0;
        if (LowerCaseDay.startsWith("tue")) return 1;
        if (LowerCaseDay.startsWith("wed")) return 2;
        if (LowerCaseDay.startsWith("thu")) return 3;
        if (LowerCaseDay.startsWith("fri")) return 4;
        return -1;
    }

    private int TomorrowIndex() {
        DayOfWeek TomorrowDay = LocalDate.now().plusDays(1).getDayOfWeek();
        switch (TomorrowDay) {
            case MONDAY:
                return 0;
            case TUESDAY:
                return 1;
            case WEDNESDAY:
                return 2;
            case THURSDAY:
                return 3;
            case FRIDAY:
                return 4;
            default:
                return -1;
        }
    }

    private List<InstructorTomorrowClass> LoadInstructorTomorrowClasses() {
        List<InstructorTomorrowClass> ResultList = new ArrayList<>();
        int IndexTomorrow = TomorrowIndex();
        if (IndexTomorrow < 0) return ResultList;

        List<Offerings> OfferingsList = GetInstructorOfferings();
        if (OfferingsList.isEmpty()) return ResultList;

        for (Offerings OfferingObject : OfferingsList) {
            Course CourseObject;
            try {
                CourseObject = CourseHandler.FindCourseByID(OfferingObject.GetCourseID());
            } catch (SQLException ExceptionObject) {
                ExceptionObject.printStackTrace();
                continue;
            }
            String Code = CourseObject != null ? String.valueOf(CourseObject.GetCode()) : "";
            String Title = CourseObject != null ? CourseObject.GetTitle() : "Unknown Course";

            String LectureJson = NormalizeJson(OfferingObject.GetLectureSchedule());
            String LabJson = NormalizeJson(OfferingObject.GetLabSchedule());

            List<ScheduleEntry> ScheduleEntries = new ArrayList<>();
            ScheduleEntries.addAll(ParseScheduleJson(LectureJson, false));
            ScheduleEntries.addAll(ParseScheduleJson(LabJson, true));

            for (ScheduleEntry EntryObject : ScheduleEntries) {
                int DayIndex = DayToIndex(EntryObject.Day);
                if (DayIndex != IndexTomorrow) continue;

                InstructorTomorrowClass TomorrowClass = new InstructorTomorrowClass();
                TomorrowClass.CourseCode = Code;
                TomorrowClass.CourseTitle = Title;
                TomorrowClass.IsLab = EntryObject.IsLab;
                TomorrowClass.StartString = EntryObject.StartString;
                TomorrowClass.EndString = EntryObject.EndString;
                TomorrowClass.StartMinutes = EntryObject.StartMinutes;
                ResultList.add(TomorrowClass);
            }
        }

        ResultList.sort(Comparator.comparingInt(ClassItem -> ClassItem.StartMinutes));
        return ResultList;
    }

    public static void main(String[] Arguments) {
        SwingUtilities.invokeLater(() -> {
            DashboardUIInstructor UserInterface = new DashboardUIInstructor(1);
            UserInterface.setVisible(true);
        });
    }
}