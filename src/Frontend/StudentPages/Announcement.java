package Frontend.StudentPages;

import Backend.DataBaseHandler.*;
import Backend.domain.*;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.*;

public class Announcement extends JPanel {

    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel = new Color(0x1E1E1E);
    private static final Color ColorPanelAlt = new Color(0x181818);
    private static final Color ColorAccent = new Color(0xB0F2B4);
    private static final Color ColorTextPrimary = new Color(0xFFFFFF);
    private static final Color ColorTextSecondary = new Color(0xB0B0B0);

    private Student CurrentStudent;
    private SemestersHandler.SemesterProfile CurrentSemesterProfile;
    private final DateTimeFormatter DateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JPanel AnnouncementListPanel;
    private JTextField SearchField;
    private JComboBox<String> CourseFilterComboBox;

    private List<StudentAnnouncementRecord> AllAnnouncements = new ArrayList<>();
    private final Map<String, String> CourseCodeToTitleMap = new HashMap<>();

    public Announcement(int StudentRollNumber) {
        super(new BorderLayout(10, 15));

        try {
            this.CurrentStudent = StudentHandler.FindStudentByRollNumber(StudentRollNumber);
            this.CurrentSemesterProfile = SemestersHandler.GetCurrentSemester();
        } catch (SQLException ExceptionObject) {
            throw new RuntimeException(ExceptionObject);
        }

        setOpaque(false);
        setBorder(new EmptyBorder(10, 15, 15, 15));

        add(CreateHeaderPanel(), BorderLayout.NORTH);

        AnnouncementListPanel = new JPanel();
        AnnouncementListPanel.setLayout(new BoxLayout(AnnouncementListPanel, BoxLayout.Y_AXIS));
        AnnouncementListPanel.setOpaque(false);
        AnnouncementListPanel.setBorder(new EmptyBorder(10, 5, 10, 5));

        JScrollPane ScrollPane = new JScrollPane(AnnouncementListPanel);
        ScrollPane.setOpaque(false);
        ScrollPane.getViewport().setOpaque(false);
        ScrollPane.setBorder(BorderFactory.createEmptyBorder());
        ScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(ScrollPane, BorderLayout.CENTER);

        ReloadAnnouncements();
    }

    public void ReloadAnnouncements() {
        LoadAnnouncements();
        RefreshList();
    }

    private JPanel CreateHeaderPanel() {
        JPanel HeaderPanel = new JPanel(new BorderLayout(20, 0));
        HeaderPanel.setOpaque(false);

        JPanel TitlePanel = new JPanel(new BorderLayout());
        TitlePanel.setOpaque(false);

        String SemesterTitle = (CurrentSemesterProfile != null)
                ? CurrentSemesterProfile.Semester + " " + CurrentSemesterProfile.Year + " Announcements"
                : "Course Announcements";

        JLabel TitleLabel = new JLabel(SemesterTitle);
        TitleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        TitleLabel.setForeground(ColorTextPrimary);
        TitleLabel.setBorder(new EmptyBorder(0, 5, 2, 0));

        JLabel SubtitleLabel = new JLabel("All announcements from courses you are registered in");
        SubtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        SubtitleLabel.setForeground(ColorTextSecondary);
        SubtitleLabel.setBorder(new EmptyBorder(0, 7, 0, 0));

        TitlePanel.add(TitleLabel, BorderLayout.NORTH);
        TitlePanel.add(SubtitleLabel, BorderLayout.SOUTH);

        HeaderPanel.add(TitlePanel, BorderLayout.WEST);

        JPanel RightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        RightPanel.setOpaque(false);

        CourseFilterComboBox = new JComboBox<>();
        CourseFilterComboBox.setPreferredSize(new Dimension(180, 36));
        CourseFilterComboBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        CourseFilterComboBox.setBackground(ColorPanel);
        CourseFilterComboBox.setForeground(ColorTextPrimary);
        CourseFilterComboBox.addItem("All Courses");
        CourseFilterComboBox.addActionListener(ActionEventObject -> RefreshList());

        SearchField = new JTextField();
        SearchField.putClientProperty("JTextField.placeholderText", "Search in announcements...");
        SearchField.setBackground(ColorPanel);
        SearchField.setForeground(ColorTextPrimary);
        SearchField.setCaretColor(ColorTextPrimary);
        SearchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        SearchField.setBorder(new EmptyBorder(8, 12, 8, 12));
        SearchField.setPreferredSize(new Dimension(260, 36));
        SearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent DocumentEventObject) {
                RefreshList();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent DocumentEventObject) {
                RefreshList();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent DocumentEventObject) {
                RefreshList();
            }
        });

        RightPanel.add(CourseFilterComboBox);
        RightPanel.add(SearchField);

        HeaderPanel.add(RightPanel, BorderLayout.EAST);
        return HeaderPanel;
    }

    private void LoadAnnouncements() {
        AllAnnouncements.clear();
        CourseCodeToTitleMap.clear();

        if (CurrentSemesterProfile == null || CurrentStudent == null) {
            return;
        }

        try {
            List<Integer> EnrolledOfferingIdList = RegistrationHandler.getEnrolledOfferingIDs(CurrentStudent.GetRollNumber());
            if (EnrolledOfferingIdList == null) {
                return;
            }

            for (int OfferingID : EnrolledOfferingIdList) {
                Offerings CurrentOffering = OfferingHandler.FindOffering(OfferingID);
                if (CurrentOffering == null) {
                    continue;
                }

                if (CurrentOffering.GetYear() != CurrentSemesterProfile.Year) {
                    continue;
                }
                if (!CurrentOffering.GetSemester().equalsIgnoreCase(CurrentSemesterProfile.Semester)) {
                    continue;
                }

                Course CurrentCourse = CourseHandler.FindCourseByID(CurrentOffering.GetCourseID());
                Instructor CurrentInstructor = InstructorHandler.FindInstructorByInstructorID(CurrentOffering.GetInstructorID());

                String CourseCode = String.valueOf(CurrentCourse.GetCode());
                String CourseTitle = CurrentCourse.GetTitle();
                String InstructorName = CurrentInstructor != null ? CurrentInstructor.GetName() : "Instructor";

                CourseCodeToTitleMap.put(CourseCode, CourseTitle);

                List<Map<String, String>> AnnouncementRecords = Backend.DataBaseHandler.OfferingHandler.GetAnnouncementsForOffering(OfferingID);
                if (AnnouncementRecords == null) {
                    continue;
                }

                for (Map<String, String> AnnouncementMap : AnnouncementRecords) {
                    String DateString = AnnouncementMap.getOrDefault("Date", "").trim();
                    String MessageText = AnnouncementMap.getOrDefault("Data", "").trim();
                    if (MessageText.isEmpty()) {
                        continue;
                    }

                    LocalDate AnnouncementDate = ParseDateSafe(DateString);
                    AllAnnouncements.add(new StudentAnnouncementRecord(
                            AnnouncementDate,
                            DateString,
                            MessageText,
                            CourseCode,
                            CourseTitle,
                            InstructorName
                    ));
                }
            }

            AllAnnouncements.sort((FirstAnnouncement, SecondAnnouncement) -> {
                if (FirstAnnouncement.AnnouncementDate == null && SecondAnnouncement.AnnouncementDate == null) {
                    return 0;
                }
                if (FirstAnnouncement.AnnouncementDate == null) {
                    return 1;
                }
                if (SecondAnnouncement.AnnouncementDate == null) {
                    return -1;
                }
                return SecondAnnouncement.AnnouncementDate.compareTo(FirstAnnouncement.AnnouncementDate);
            });

            CourseFilterComboBox.removeAllItems();
            CourseFilterComboBox.addItem("All Courses");
            CourseCodeToTitleMap.keySet().stream().sorted().forEach(CourseCode -> {
                String CourseTitle = CourseCodeToTitleMap.get(CourseCode);
                CourseFilterComboBox.addItem(CourseCode + " - " + CourseTitle);
            });

        }
        catch (SQLException ExceptionObject) {
            ExceptionObject.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading announcements: " + ExceptionObject.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private LocalDate ParseDateSafe(String DateString) {
        if (DateString == null || DateString.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(DateString, DateFormatter);
        }
        catch (DateTimeParseException ExceptionObject) {
            System.err.println("Invalid announcement date: " + DateString);
            return null;
        }
    }

    private void RefreshList() {
        AnnouncementListPanel.removeAll();

        if (AllAnnouncements.isEmpty()) {
            JLabel EmptyLabel = new JLabel("No announcements yet.", SwingConstants.CENTER);
            EmptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
            EmptyLabel.setForeground(ColorTextSecondary);

            JPanel WrapperPanel = new JPanel(new BorderLayout());
            WrapperPanel.setOpaque(false);
            WrapperPanel.add(EmptyLabel, BorderLayout.CENTER);
            WrapperPanel.setBorder(new EmptyBorder(40, 0, 0, 0));
            AnnouncementListPanel.add(WrapperPanel);
        }
        else {
            String SearchText = SearchField != null ? SearchField.getText().toLowerCase().trim() : "";
            String CourseFilter = CourseFilterComboBox != null ? (String) CourseFilterComboBox.getSelectedItem() : "All Courses";
            String SelectedCourseCode = null;
            if (CourseFilter != null && !CourseFilter.equals("All Courses") && CourseFilter.contains(" - ")) {
                SelectedCourseCode = CourseFilter.split(" - ", 2)[0].trim();
            }

            for (StudentAnnouncementRecord AnnouncementRecord : AllAnnouncements) {
                if (SelectedCourseCode != null && !AnnouncementRecord.CourseCode.equals(SelectedCourseCode)) {
                    continue;
                }

                if (!SearchText.isEmpty() && !AnnouncementRecord.MessageText.toLowerCase().contains(SearchText)) {
                    continue;
                }

                AnnouncementListPanel.add(new AnnouncementCard(AnnouncementRecord));
                AnnouncementListPanel.add(Box.createVerticalStrut(8));
            }

            if (AnnouncementListPanel.getComponentCount() == 0) {
                JLabel NoneLabel = new JLabel("No announcements match your filters.", SwingConstants.CENTER);
                NoneLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
                NoneLabel.setForeground(ColorTextSecondary);

                JPanel WrapperPanel = new JPanel(new BorderLayout());
                WrapperPanel.setOpaque(false);
                WrapperPanel.add(NoneLabel, BorderLayout.CENTER);
                WrapperPanel.setBorder(new EmptyBorder(40, 0, 0, 0));
                AnnouncementListPanel.add(WrapperPanel);
            }
        }

        AnnouncementListPanel.add(Box.createVerticalGlue());
        AnnouncementListPanel.revalidate();
        AnnouncementListPanel.repaint();
    }

    private static class StudentAnnouncementRecord {
        final LocalDate AnnouncementDate;
        final String RawDate;
        final String MessageText;
        final String CourseCode;
        final String CourseTitle;
        final String InstructorName;

        public StudentAnnouncementRecord(LocalDate AnnouncementDate, String RawDate, String MessageText, String CourseCode, String CourseTitle, String InstructorName) {
            this.AnnouncementDate = AnnouncementDate;
            this.RawDate = RawDate;
            this.MessageText = MessageText;
            this.CourseCode = CourseCode;
            this.CourseTitle = CourseTitle;
            this.InstructorName = InstructorName;
        }
    }

    private static class AnnouncementCard extends RoundedPanel {
        public AnnouncementCard(StudentAnnouncementRecord AnnouncementRecord) {
            super();
            setBackground(ColorPanel);
            setLayout(new BorderLayout(8, 4));
            setBorder(new EmptyBorder(10, 14, 10, 14));

            JPanel TopPanel = new JPanel(new BorderLayout());
            TopPanel.setOpaque(false);

            JLabel DateLabel = new JLabel(AnnouncementRecord.RawDate == null || AnnouncementRecord.RawDate.isEmpty() ? "Unknown date" : AnnouncementRecord.RawDate);
            DateLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
            DateLabel.setForeground(ColorAccent);

            JLabel CourseLabel = new JLabel(AnnouncementRecord.CourseCode + " • " + AnnouncementRecord.CourseTitle);
            CourseLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            CourseLabel.setForeground(ColorTextPrimary);
            CourseLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            TopPanel.add(DateLabel, BorderLayout.WEST);
            TopPanel.add(CourseLabel, BorderLayout.EAST);

            JLabel InstructorLabel = new JLabel("By " + AnnouncementRecord.InstructorName);
            InstructorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            InstructorLabel.setForeground(ColorTextSecondary);
            InstructorLabel.setBorder(new EmptyBorder(2, 0, 4, 0));

            JTextArea MessageArea = new JTextArea(AnnouncementRecord.MessageText);
            MessageArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
            MessageArea.setForeground(ColorTextPrimary);
            MessageArea.setBackground(ColorPanel);
            MessageArea.setLineWrap(true);
            MessageArea.setWrapStyleWord(true);
            MessageArea.setEditable(false);
            MessageArea.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

            add(TopPanel, BorderLayout.NORTH);
            add(InstructorLabel, BorderLayout.CENTER);
            add(MessageArea, BorderLayout.SOUTH);

            setMaximumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int CornerRadius = 14;

        public RoundedPanel() {
            super();
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics GraphicsContext) {
            Graphics2D Graphics2DContext = (Graphics2D) GraphicsContext.create();
            Graphics2DContext.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Graphics2DContext.setColor(getBackground());
            Graphics2DContext.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), CornerRadius, CornerRadius));
            Graphics2DContext.dispose();
            super.paintComponent(GraphicsContext);
        }
    }

    public static void main(String[] Arguments) {
        try {
            FlatDarkLaf.setup();
        }
        catch (Exception ignored) {

        }
        SwingUtilities.invokeLater(() -> {
            JFrame Frame = new JFrame("Student Announcements Debug");
            Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Frame.setSize(1100, 750);
            Frame.getContentPane().setBackground(ColorBackground);
            JPanel MainHolderPanel = new JPanel(new BorderLayout());
            MainHolderPanel.setBackground(ColorBackground);
            MainHolderPanel.setBorder(new EmptyBorder(20, 25, 25, 25));
            MainHolderPanel.add(new Announcement(2024136), BorderLayout.CENTER);
            Frame.add(MainHolderPanel);
            Frame.setLocationRelativeTo(null);
            Frame.setVisible(true);
        });
    }
}