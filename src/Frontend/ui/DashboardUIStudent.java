package Frontend.ui;

import Frontend.StudentPages.*;
import Frontend.components.CalendarWidget;
import Frontend.components.LeftNavPanelStudent;
import Backend.DataBaseHandler.SemestersHandler;
import Backend.DataBaseHandler.RegistrationHandler;
import Backend.DataBaseHandler.OfferingHandler;
import Backend.DataBaseHandler.CourseHandler;
import Backend.DataBaseHandler.InstructorHandler;
import Backend.domain.Offerings;
import Backend.domain.Course;
import Backend.domain.Instructor;

import Frontend.components.NotificationPopup;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

public class DashboardUIStudent extends JFrame {

    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel = new Color(0x1E1E1E);
    private static final Color ColorAccent = new Color(0xB0F2B4);
    private static final Color ColorTextPrimary = new Color(0xFFFFFF);
    private static final Color ColorTextSecondary = new Color(0xB0B0B0);
    private static final Color ColorWarning = new Color(0xFFD54F);

    private int RollNumber = -1;

    private final CardLayout CenterCards = new CardLayout();
    private final JPanel CenterHolder = new JPanel(CenterCards);

    private String CurrentCenterLabel = "Dashboard";
    private JLabel PageTitleLabel;
    private JPanel DashboardPanel;

    private SemestersHandler.SemesterProfile CurrentSemesterProfile;

    private TimeTable TimeTablePanel;
    private Announcement AnnouncementPanel;
    private GradeBookPage GradeBookPanel;

    private final DateTimeFormatter AnnDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DashboardUIStudent(int RollNumber) {
        this.RollNumber = RollNumber;

        try {
            CurrentSemesterProfile = SemestersHandler.GetCurrentSemester();
        } catch (SQLException ExceptionObject) {
            ExceptionObject.printStackTrace();
        }

        setTitle("Campus Connect Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        getContentPane().setBackground(ColorBackground);
        setLayout(new BorderLayout());

        add(CreateLeftNavigation(), BorderLayout.WEST);
        add(CreateMainContent(), BorderLayout.CENTER);
    }

    private JComponent CreateLeftNavigation() {
        LeftNavPanelStudent.Theme ThemeObject =
                new LeftNavPanelStudent.Theme(
                        ColorPanel, ColorAccent, ColorTextPrimary, ColorTextSecondary
                );

        LeftNavPanelStudent NavigationPanel = new LeftNavPanelStudent(ThemeObject, "Dashboard");

        NavigationPanel.SetOnSelect(Label -> {
            if ("Log Out".equals(Label)) {
                int Choice = JOptionPane.showConfirmDialog(
                        DashboardUIStudent.this,
                        "Are you sure you want to log out?",
                        "Confirm Logout",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (Choice == JOptionPane.YES_OPTION) {
                    SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
                    DashboardUIStudent.this.dispose();
                }
                else {
                    NavigationPanel.SetActive(CurrentCenterLabel);
                    CenterCards.show(CenterHolder, CurrentCenterLabel);
                }
            }
            else {

                if ("Dashboard".equals(Label)) {
                    RefreshDashboard();
                }
                CurrentCenterLabel = Label;
                CenterCards.show(CenterHolder, Label);
                NavigationPanel.SetActive(Label);

                if (PageTitleLabel != null) {
                    PageTitleLabel.setText(" " + Label);
                }

                if ("Time Table".equals(Label) && TimeTablePanel != null) {
                    TimeTablePanel.ReloadTimetableAsync();
                }
                if ("Announcements".equals(Label) && AnnouncementPanel != null) {
                    AnnouncementPanel.ReloadAnnouncements();
                }
                if ("Exams & Result".equals(Label) && GradeBookPanel != null) {
                    GradeBookPanel.ReloadGradeBook();
                }
            }
        });

        return NavigationPanel;
    }


    private JComponent CreateMainContent() {
        JPanel MainPanel = new JPanel(new BorderLayout(25, 10));
        MainPanel.setBackground(ColorBackground);
        MainPanel.setBorder(new EmptyBorder(20, 25, 25, 25));

        MainPanel.add(CreateTopBar(), BorderLayout.NORTH);

        CenterHolder.setOpaque(false);

        TimeTablePanel = new TimeTable(RollNumber);
        AnnouncementPanel = new Announcement(RollNumber);
        GradeBookPanel = new GradeBookPage(RollNumber);

        DashboardPanel = (JPanel) CreateDashboardGrid();
        CenterHolder.add(DashboardPanel, "Dashboard");
        CenterHolder.add(AnnouncementPanel, "Announcements");
        CenterHolder.add(new Courses(RollNumber), "Registrations");
        CenterHolder.add(GradeBookPanel, "Exams & Result");
        CenterHolder.add(TimeTablePanel, "Time Table");
        CenterHolder.add(new StudentProfilePanel(RollNumber), "Profile");

        MainPanel.add(CenterHolder, BorderLayout.CENTER);

        CenterCards.show(CenterHolder, "Dashboard");
        return MainPanel;
    }

    private JComponent CreateTopBar() {
        JPanel TopBar = new JPanel(new BorderLayout());
        TopBar.setOpaque(false);
        PageTitleLabel = new JLabel(" Dashboard");
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
        BellIcon.setColorFilter(new FlatSVGIcon.ColorFilter(OriginalColor -> ColorTextPrimary));
        JLabel BellLabel = new JLabel(BellIcon);
        BellLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        NotificationPopup notifPopup = new NotificationPopup(RollNumber);

        BellLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!notifPopup.isVisible()) {
                    notifPopup.LoadNotifications();
                    notifPopup.show(BellLabel, e.getX() - 300, e.getY() + 30);
                }
            }
        });
        RightSidePanel.add(BellLabel);


        FlatSVGIcon ProfileIcon = new FlatSVGIcon("Frontend/assets/pfp.svg", 28, 28);
        ProfileIcon.setColorFilter(new FlatSVGIcon.ColorFilter(OriginalColor -> ColorTextPrimary));
        JLabel ProfileLabel = new JLabel(ProfileIcon);
        ProfileLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ProfileLabel.setToolTipText("View Profile");
        ProfileLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent EventObject) {
                CurrentCenterLabel = "Profile";
                CenterCards.show(CenterHolder, "Profile");
                if (PageTitleLabel != null) {
                    PageTitleLabel.setText(" Student Profile");
                }
            }
        });
        RightSidePanel.add(ProfileLabel);
        TopBar.add(RightSidePanel, BorderLayout.EAST);
        return TopBar;
    }

    private void RefreshDashboard() {
        if (DashboardPanel != null) {
            CenterHolder.remove(DashboardPanel);
        }
        DashboardPanel = (JPanel) CreateDashboardGrid();
        CenterHolder.add(DashboardPanel, "Dashboard");

        CenterHolder.revalidate();
        CenterHolder.repaint();
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
        Constraints.weighty = 0.4;
        Constraints.gridheight = 1;
        GridPanel.add(CreateUpcomingEventsPanel(), Constraints);
        Constraints.gridx = 0;
        Constraints.gridy = 1;
        Constraints.weighty = 0.6;
        Constraints.gridheight = 2;
        GridPanel.add(CreateCourseInfoPanel(), Constraints);
        Constraints.gridx = 1;
        Constraints.gridy = 0;
        Constraints.weightx = 0.3;
        Constraints.weighty = 0.3;
        Constraints.gridheight = 1;
        GridPanel.add(CreateSchedulePanel(), Constraints);
        Constraints.gridx = 1;
        Constraints.gridy = 1;
        Constraints.weighty = 0.3;
        GridPanel.add(CreateQuizzesPanel(), Constraints);
        Constraints.gridx = 1;
        Constraints.gridy = 2;
        Constraints.weighty = 0.4;
        GridPanel.add(CreateCalendarPanel(), Constraints);
        return GridPanel;
    }

    private JComponent CreateModulePanel(String Title) {
        JPanel PanelObject = new RoundedPanel();
        PanelObject.setBackground(ColorPanel);
        PanelObject.setLayout(new BorderLayout(10, 10));
        PanelObject.setBorder(new EmptyBorder(15, 15, 15, 15));
        return PanelObject;
    }

    private JComponent CreateUpcomingEventsPanel() {
        JComponent PanelObject = CreateModulePanel("Upcoming Events");
        Font LabelFont = new Font("SansSerif", Font.BOLD, 18);
        Font ButtonFont = new Font("SansSerif", Font.PLAIN, 16);
        JPanel HeadingPanel = new JPanel();
        HeadingPanel.setOpaque(false);
        HeadingPanel.setLayout(new BoxLayout(HeadingPanel, BoxLayout.X_AXIS));
        JLabel TitleLabel = new JLabel("Upcoming Events");
        TitleLabel.setFont(LabelFont);
        TitleLabel.setForeground(ColorTextPrimary);
        HeadingPanel.add(TitleLabel);
        HeadingPanel.add(Box.createHorizontalGlue());
        JButton WorkshopsButton = new JButton("Workshops");
        WorkshopsButton.setFont(ButtonFont);
        HeadingPanel.add(WorkshopsButton);
        JButton WebinarsButton = new JButton("Webinars");
        WebinarsButton.setFont(ButtonFont);
        HeadingPanel.add(WebinarsButton);
        JButton CulturalButton = new JButton("Cultural");
        CulturalButton.setFont(ButtonFont);
        HeadingPanel.add(CulturalButton);
        PanelObject.add(HeadingPanel, BorderLayout.NORTH);
        JPanel WorkshopsPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        WorkshopsPanel.setOpaque(false);
        WorkshopsPanel.add(CreateEventCard("Machine Learning Workshop"));
        WorkshopsPanel.add(CreateEventCard("Cybersecurity Workshop"));
        WorkshopsPanel.add(CreateEventCard("Environmental World"));
        PanelObject.add(WorkshopsPanel, BorderLayout.CENTER);
        return PanelObject;
    }

    private JComponent CreateEventCard(String Title) {
        JPanel CardPanel = new RoundedPanel();
        CardPanel.setBackground(ColorBackground);
        CardPanel.setLayout(new BoxLayout(CardPanel, BoxLayout.Y_AXIS));
        CardPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        CardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel TitleLabel = new JLabel(Title);
        TitleLabel.setForeground(ColorTextPrimary);
        CardPanel.add(TitleLabel);
        JLabel DateLabel = new JLabel("Date & Location");
        DateLabel.setForeground(ColorTextSecondary);
        CardPanel.add(DateLabel);
        return CardPanel;
    }

    private JComponent CreateCourseInfoPanel() {
        JPanel PanelObject = new RoundedPanel();
        PanelObject.setBackground(ColorPanel);
        PanelObject.setLayout(new BorderLayout(10, 10));
        PanelObject.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel TitleLabel = new JLabel("My Courses this Term");
        TitleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        TitleLabel.setForeground(ColorTextPrimary);
        PanelObject.add(TitleLabel, BorderLayout.NORTH);
        JPanel ListPanel = new JPanel();
        ListPanel.setOpaque(false);
        ListPanel.setLayout(new BoxLayout(ListPanel, BoxLayout.Y_AXIS));
        List<Offerings> OfferingsList = GetRegisteredOfferingsForCurrentTerm();
        if (OfferingsList.isEmpty()) {
            JLabel NoneLabel = new JLabel("You are not registered for any courses this term.");
            NoneLabel.setForeground(ColorTextSecondary);
            NoneLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
            ListPanel.add(NoneLabel);
        } else {
            for (Offerings OfferingObject : OfferingsList) {
                try {
                    Course CourseObject = CourseHandler.FindCourseByID(OfferingObject.GetCourseID());
                    Instructor InstructorObject = InstructorHandler.FindInstructorByInstructorID(OfferingObject.GetInstructorID());
                    String Code = CourseObject != null ? String.valueOf(CourseObject.GetCode()) : "";
                    String CourseTitle = CourseObject != null ? CourseObject.GetTitle() : "Unknown Course";
                    String ProfessorName = InstructorObject != null ? InstructorObject.GetName() : "Instructor";
                    ListPanel.add(CreateCourseRow(Code, CourseTitle, ProfessorName, OfferingObject.GetSemester(), OfferingObject.GetYear()));
                    ListPanel.add(Box.createVerticalStrut(6));
                } catch (SQLException ExceptionObject) {
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

    private JComponent CreateCourseRow(String Code, String Title, String Professor, String Semester, int Year) {
        JPanel RowPanel = new RoundedPanel();
        RowPanel.setBackground(ColorBackground);
        RowPanel.setLayout(new BorderLayout(8, 4));
        RowPanel.setBorder(new EmptyBorder(8, 10, 8, 10));
        JLabel TitleLabel = new JLabel(Title);
        TitleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        TitleLabel.setForeground(ColorTextPrimary);
        JLabel CodeLabel = new JLabel(Code + " • Sem " + Semester + " " + Year);
        CodeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        CodeLabel.setForeground(ColorTextSecondary);
        JLabel ProfessorLabel = new JLabel("Instructor: " + Professor);
        ProfessorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        ProfessorLabel.setForeground(ColorTextSecondary);
        JPanel LeftPanel = new JPanel();
        LeftPanel.setOpaque(false);
        LeftPanel.setLayout(new BoxLayout(LeftPanel, BoxLayout.Y_AXIS));
        LeftPanel.add(TitleLabel);
        LeftPanel.add(Box.createVerticalStrut(3));
        LeftPanel.add(CodeLabel);
        LeftPanel.add(ProfessorLabel);
        RowPanel.add(LeftPanel, BorderLayout.CENTER);
        return RowPanel;
    }

    private JComponent CreateSchedulePanel() {
        JComponent PanelObject = CreateModulePanel("Tomorrow's Schedule");
        JPanel ContainerPanel = new JPanel();
        ContainerPanel.setOpaque(false);
        ContainerPanel.setLayout(new BorderLayout(8, 8));
        JLabel TitleLabel = new JLabel("Tomorrow's Schedule");
        TitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        TitleLabel.setForeground(ColorTextPrimary);
        TitleLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        ContainerPanel.add(TitleLabel, BorderLayout.NORTH);
        JPanel ListPanel = new JPanel();
        ListPanel.setOpaque(false);
        ListPanel.setLayout(new BoxLayout(ListPanel, BoxLayout.Y_AXIS));
        List<TomorrowClass> ClassesList = LoadTomorrowClasses();
        if (ClassesList.isEmpty()) {
            JLabel NoneLabel = new JLabel("You have no classes tomorrow 🎉", SwingConstants.LEFT);
            NoneLabel.setForeground(ColorTextSecondary);
            NoneLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
            ListPanel.add(NoneLabel);
        }
        {
            for (TomorrowClass ClassItem : ClassesList) {
                ListPanel.add(CreateTomorrowClassCard(ClassItem));
                ListPanel.add(Box.createVerticalStrut(6));
            }
        }
        JScrollPane ScrollPaneObject = new JScrollPane(ListPanel);
        ScrollPaneObject.setOpaque(false);
        ScrollPaneObject.getViewport().setOpaque(false);
        ScrollPaneObject.setBorder(BorderFactory.createEmptyBorder());
        ContainerPanel.add(ScrollPaneObject, BorderLayout.CENTER);
        PanelObject.add(ContainerPanel, BorderLayout.CENTER);
        return PanelObject;
    }

    private JComponent CreateTomorrowClassCard(TomorrowClass ClassItem) {
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

    private JComponent CreateQuizzesPanel() {
        JComponent PanelObject = CreateModulePanel("Upcoming Quizzes");
        JPanel ContainerPanel = new JPanel();
        ContainerPanel.setOpaque(false);
        ContainerPanel.setLayout(new BorderLayout(8, 8));
        JLabel TitleLabel = new JLabel("Upcoming Quizzes");
        TitleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        TitleLabel.setForeground(ColorTextPrimary);
        TitleLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        ContainerPanel.add(TitleLabel, BorderLayout.NORTH);
        JPanel ListPanel = new JPanel();
        ListPanel.setOpaque(false);
        ListPanel.setLayout(new BoxLayout(ListPanel, BoxLayout.Y_AXIS));
        List<QuizItem> QuizzesList = LoadQuizAnnouncements();
        if (QuizzesList.isEmpty()) {
            JLabel NoneLabel = new JLabel("No quiz-related announcements found.", SwingConstants.LEFT);
            NoneLabel.setForeground(ColorTextSecondary);
            NoneLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
            ListPanel.add(NoneLabel);
        }
        else {
            for (QuizItem QuizObject : QuizzesList) {
                ListPanel.add(CreateQuizCard(QuizObject));
                ListPanel.add(Box.createVerticalStrut(6));
            }
        }
        JScrollPane ScrollPaneObject = new JScrollPane(ListPanel);
        ScrollPaneObject.setOpaque(false);
        ScrollPaneObject.getViewport().setOpaque(false);
        ScrollPaneObject.setBorder(BorderFactory.createEmptyBorder());
        ContainerPanel.add(ScrollPaneObject, BorderLayout.CENTER);
        PanelObject.add(ContainerPanel, BorderLayout.CENTER);
        return PanelObject;
    }

    private JComponent CreateQuizCard(QuizItem QuizObject) {
        JPanel CardPanel = new RoundedPanel();
        CardPanel.setBackground(ColorBackground);
        CardPanel.setLayout(new BorderLayout(6, 3));
        CardPanel.setBorder(new EmptyBorder(8, 10, 8, 10));
        JLabel DateLabel = new JLabel(QuizObject.RawDate == null || QuizObject.RawDate.isEmpty() ? "Date: Not specified" : "Date: " + QuizObject.RawDate);
        DateLabel.setForeground(ColorAccent);
        DateLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        JLabel CourseLabel = new JLabel(QuizObject.CourseCode + " • " + QuizObject.CourseTitle);
        CourseLabel.setForeground(ColorTextPrimary);
        CourseLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        JTextArea MessageArea = new JTextArea(QuizObject.Message);
        MessageArea.setLineWrap(true);
        MessageArea.setWrapStyleWord(true);
        MessageArea.setEditable(false);
        MessageArea.setOpaque(false);
        MessageArea.setForeground(ColorTextSecondary);
        MessageArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        MessageArea.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        JPanel TopPanel = new JPanel(new BorderLayout());
        TopPanel.setOpaque(false);
        TopPanel.add(DateLabel, BorderLayout.WEST);
        TopPanel.add(CourseLabel, BorderLayout.EAST);
        CardPanel.add(TopPanel, BorderLayout.NORTH);
        CardPanel.add(MessageArea, BorderLayout.CENTER);
        return CardPanel;
    }

    private JComponent CreateCalendarPanel() {
        CalendarWidget.Theme ThemeData = new CalendarWidget.Theme();
        ThemeData.Background = new Color(0x121212);
        ThemeData.Panel = new Color(0x1E1E1E);
        ThemeData.TextPrimary = Color.WHITE;
        ThemeData.TextSecondary = new Color(0xB0B0B0);
        ThemeData.Accent = new Color(0xB0F2B4);
        CalendarWidget CalendarObject = new CalendarWidget(YearMonth.now(), ThemeData);
        JPanel PanelObject = new JPanel(new BorderLayout());
        PanelObject.setOpaque(false);
        PanelObject.add(CalendarObject, BorderLayout.CENTER);
        return PanelObject;
    }

    private static class RoundedPanel extends JPanel {
        private final int CornerRadius = 15;
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

    private List<Offerings> GetRegisteredOfferingsForCurrentTerm() {
        List<Offerings> ResultList = new ArrayList<>();
        if (CurrentSemesterProfile == null || RollNumber <= 0) return ResultList;
        try {
            String Semester = CurrentSemesterProfile.Semester;
            int Year = CurrentSemesterProfile.Year;
            Set<Integer> OfferingIds = RegistrationHandler.GetRegisteredOfferingIds(RollNumber, Semester, Year);
            if (OfferingIds == null || OfferingIds.isEmpty()) return ResultList;
            List<Integer> IdList = new ArrayList<>(OfferingIds);
            ResultList = OfferingHandler.FindOfferingsByIDs(IdList);
        }
        catch (SQLException ExceptionObject) {
            ExceptionObject.printStackTrace();
        }
        return ResultList;
    }

    private static class QuizItem {
        LocalDate Date;
        String RawDate;
        String Message;
        String CourseCode;
        String CourseTitle;
        QuizItem(LocalDate DateObject, String RawDateValue, String MessageValue, String CourseCodeValue, String CourseTitleValue) {
            this.Date = DateObject;
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
        }
        catch (DateTimeParseException ExceptionObject) {
            System.err.println("Invalid quiz announcement date: " + DateString);
            return null;
        }
    }

    private List<QuizItem> LoadQuizAnnouncements() {
        List<QuizItem> ResultList = new ArrayList<>();
        List<Offerings> OfferingsList = GetRegisteredOfferingsForCurrentTerm();
        if (OfferingsList.isEmpty()) return ResultList;
        LocalDate Today = LocalDate.now();
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
                String LowerCaseMessage = MessageValue.toLowerCase(Locale.ROOT);
                if (!LowerCaseMessage.contains("quiz")) continue;
                String RawDateValue = AnnouncementMap.getOrDefault("Date", "").trim();
                LocalDate AnnouncementDate = ParseAnnouncementDate(RawDateValue);
                if (AnnouncementDate != null && AnnouncementDate.isBefore(Today)) continue;
                ResultList.add(new QuizItem(AnnouncementDate, RawDateValue, MessageValue, Code, Title));
            }
        }
        ResultList.sort((FirstQuiz, SecondQuiz) -> {
            if (FirstQuiz.Date == null && SecondQuiz.Date == null) return 0;
            if (FirstQuiz.Date == null) return 1;
            if (SecondQuiz.Date == null) return -1;
            return FirstQuiz.Date.compareTo(SecondQuiz.Date);
        });
        if (ResultList.size() > 5) {
            return ResultList.subList(0, 5);
        }
        return ResultList;
    }

    private static class TomorrowClass {
        String CourseCode;
        String CourseTitle;
        boolean IsLab;
        String StartString;
        String EndString;
        int StartMinutes;
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
            case MONDAY: return 0;
            case TUESDAY: return 1;
            case WEDNESDAY: return 2;
            case THURSDAY: return 3;
            case FRIDAY: return 4;
            default: return -1;
        }
    }

    private List<TomorrowClass> LoadTomorrowClasses() {
        List<TomorrowClass> ResultList = new ArrayList<>();
        int IndexTomorrow = TomorrowIndex();
        if (IndexTomorrow < 0) return ResultList;
        List<Offerings> OfferingsList = GetRegisteredOfferingsForCurrentTerm();
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
                TomorrowClass TomorrowClassObject = new TomorrowClass();
                TomorrowClassObject.CourseCode = Code;
                TomorrowClassObject.CourseTitle = Title;
                TomorrowClassObject.IsLab = EntryObject.IsLab;
                TomorrowClassObject.StartString = EntryObject.StartString;
                TomorrowClassObject.EndString = EntryObject.EndString;
                TomorrowClassObject.StartMinutes = EntryObject.StartMinutes;
                ResultList.add(TomorrowClassObject);
            }
        }
        ResultList.sort(Comparator.comparingInt(ClassItem -> ClassItem.StartMinutes));
        return ResultList;
    }

    public static void main(String[] Arguments) {
        SwingUtilities.invokeLater(() -> {
            DashboardUIStudent UserInterface = new DashboardUIStudent(2025001);
            UserInterface.setVisible(true);
        });
    }
}