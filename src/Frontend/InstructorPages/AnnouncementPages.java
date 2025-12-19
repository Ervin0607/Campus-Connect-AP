package Frontend.InstructorPages;

import Backend.DataBaseHandler.*;
import Backend.domain.Course;
import Backend.domain.Instructor;
import Backend.domain.Notification;
import Backend.domain.Offerings;
import Backend.domain.StudentGradeRecord;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.Map;

import static Backend.DataBaseHandler.OfferingHandler.*;

public class AnnouncementPages extends JPanel {

    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel = new Color(0x1E1E1E);
    private static final Color ColorAccent = new Color(0xB0F2B4);
    private static final Color ColorTextPrimary = new Color(0xFFFFFF);
    private static final Color ColorTextSecondary = new Color(0xB0B0B0);

    private Instructor CurrentInstructor;

    private JPanel SectionListPanel;
    private AnnouncementDetailPanel DetailPanel;
    private JTextField SearchField;

    private List<SectionItemPanel> AllSectionsList = new ArrayList<>();

    public AnnouncementPages(int CurrentInstructorID) {
        super(new BorderLayout(10, 15));

        try {
            this.CurrentInstructor = InstructorHandler.FindInstructorByInstructorID(CurrentInstructorID);
        }
        catch (SQLException ExceptionObject) {
            throw new RuntimeException(ExceptionObject);
        }

        setOpaque(false);
        setBorder(new EmptyBorder(10, 15, 15, 15));

        add(CreateHeaderPanel(), BorderLayout.NORTH);

        JSplitPane SplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        SplitPane.setResizeWeight(0.35);
        SplitPane.setBorder(null);

        SectionListPanel = new JPanel();
        SectionListPanel.setLayout(new BoxLayout(SectionListPanel, BoxLayout.Y_AXIS));
        SectionListPanel.setOpaque(false);

        JScrollPane ScrollPane = new JScrollPane(SectionListPanel);
        ScrollPane.setOpaque(false);
        ScrollPane.getViewport().setOpaque(false);
        ScrollPane.setBorder(BorderFactory.createEmptyBorder());
        ScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel LeftContainerPanel = new JPanel(new BorderLayout());
        LeftContainerPanel.setOpaque(false);
        LeftContainerPanel.add(ScrollPane, BorderLayout.CENTER);
        LeftContainerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0x333333)), "My Sections", 0, 0, new Font("SansSerif", Font.PLAIN, 12), ColorTextSecondary));

        SplitPane.setLeftComponent(LeftContainerPanel);

        DetailPanel = new AnnouncementDetailPanel();
        SplitPane.setRightComponent(DetailPanel);

        add(SplitPane, BorderLayout.CENTER);

        AllSectionsList = InitializeSectionPanels();
        UpdateSectionList("");
    }

    private JPanel CreateHeaderPanel() {
        JPanel HeaderPanel = new JPanel(new BorderLayout(20, 0));
        HeaderPanel.setOpaque(false);

        JLabel TitleLabel = new JLabel("Announcements");
        TitleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        TitleLabel.setForeground(ColorTextPrimary);
        TitleLabel.setBorder(new EmptyBorder(0, 5, 5, 0));
        HeaderPanel.add(TitleLabel, BorderLayout.WEST);

        SearchField = new JTextField();
        SearchField.putClientProperty("JTextField.placeholderText", "Search by course name or code...");
        SearchField.setBackground(ColorPanel);
        SearchField.setForeground(ColorTextPrimary);
        SearchField.setCaretColor(ColorTextSecondary);
        SearchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        SearchField.setBorder(new EmptyBorder(8, 12, 8, 12));
        SearchField.setPreferredSize(new Dimension(300, 40));

        SearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent DocumentEventObject) {
                UpdateSectionList(SearchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent DocumentEventObject) {
                UpdateSectionList(SearchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent DocumentEventObject) {
                UpdateSectionList(SearchField.getText());
            }
        });

        JPanel SearchContainerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        SearchContainerPanel.setOpaque(false);
        SearchContainerPanel.add(SearchField);

        HeaderPanel.add(SearchContainerPanel, BorderLayout.EAST);
        return HeaderPanel;
    }

    private List<SectionItemPanel> InitializeSectionPanels() {
        List<SectionItemPanel> SectionPanels = new ArrayList<>();
        List<Offerings> OfferingsList;
        try {
            OfferingsList = GetAllOfferings();
        }
        catch (SQLException ExceptionObject) {
            JOptionPane.showMessageDialog(this, "Error fetching offerings: " + ExceptionObject.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            return SectionPanels;
        }

        for (Offerings CurrentOffering : OfferingsList) {
            if (CurrentOffering.GetInstructorID() == CurrentInstructor.GetInstructorID()) {
                try {
                    Course CurrentCourse = CourseHandler.FindCourseByID(CurrentOffering.GetCourseID());
                    SectionItemPanel SectionPanel = new SectionItemPanel(CurrentCourse.GetCode() + "", CurrentCourse.GetTitle(), CurrentCourse.GetCredits(), CurrentOffering.GetCurrentEnrollment(), CurrentOffering.GetCapacity(), CurrentOffering.GetSemester(), CurrentOffering.GetYear(), CurrentOffering.GetOfferingID());
                    SectionPanel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent MouseEventObject) {
                            OnSectionSelected(SectionPanel);
                        }
                    });
                    SectionPanels.add(SectionPanel);
                }
                catch (SQLException ExceptionObject) {
                    System.out.println("Error finding course details: " + ExceptionObject.getMessage());
                }
            }
        }
        return SectionPanels;
    }

    private void UpdateSectionList(String FilterText) {
        SectionListPanel.removeAll();
        String LowerFilter = FilterText.toLowerCase().trim();

        for (SectionItemPanel SectionPanel : AllSectionsList) {
            if (SectionPanel.Name.toLowerCase().contains(LowerFilter) ||
                    SectionPanel.Code.toLowerCase().contains(LowerFilter)) {

                SectionListPanel.add(SectionPanel);
                SectionListPanel.add(Box.createVerticalStrut(10));
            }
        }
        SectionListPanel.add(Box.createVerticalGlue());
        SectionListPanel.revalidate();
        SectionListPanel.repaint();
    }

    private void OnSectionSelected(SectionItemPanel SelectedSectionPanel) {
        try {
            Offerings CurrentOffering = FindOffering(SelectedSectionPanel.OfferingID);
            DetailPanel.LoadForOffering(CurrentOffering, SelectedSectionPanel.Name, SelectedSectionPanel.Code);
        }
        catch (SQLException ExceptionObject) {
            JOptionPane.showMessageDialog(this,
                    "Error loading offering: " + ExceptionObject.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class SectionItemPanel extends RoundedPanel {
        final String Code;
        final String Name;
        final int Credits;
        final int OfferingID;

        public SectionItemPanel(String Code, String Name, int Credits,
                                int EnrolledCount, int Capacity,
                                String Semester, int Year, int OfferingID) {
            super();
            this.Code = Code;
            this.Name = Name;
            this.Credits = Credits;
            this.OfferingID = OfferingID;

            setBackground(ColorPanel);
            setLayout(new GridBagLayout());
            setBorder(new EmptyBorder(15, 20, 15, 20));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            GridBagConstraints GridConstraints = new GridBagConstraints();

            JPanel InfoPanel = CreateInfoPanel(Name, Code);
            GridConstraints.gridx = 0;
            GridConstraints.gridy = 0;
            GridConstraints.weightx = 1.0;
            GridConstraints.fill = GridBagConstraints.HORIZONTAL;
            GridConstraints.anchor = GridBagConstraints.WEST;
            add(InfoPanel, GridConstraints);

            JPanel SemesterPanel = CreateMetricPanel("Sem", Semester);
            GridConstraints.gridx = 1;
            GridConstraints.weightx = 0;
            GridConstraints.insets = new Insets(0, 20, 0, 20);
            add(SemesterPanel, GridConstraints);

            JPanel YearPanel = CreateMetricPanel("Year", String.valueOf(Year));
            GridConstraints.gridx = 2;
            GridConstraints.insets = new Insets(0, 0, 0, 20);
            add(YearPanel, GridConstraints);

            JPanel EnrollmentPanel = CreateEnrollmentPanel(EnrolledCount, Capacity);
            GridConstraints.gridx = 3;
            GridConstraints.weightx = 0;
            GridConstraints.anchor = GridBagConstraints.EAST;
            GridConstraints.insets = new Insets(0, 0, 0, 0);
            add(EnrollmentPanel, GridConstraints);

            setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        }

        private static JPanel CreateInfoPanel(String CourseName, String CourseCode) {
            JPanel InfoPanel = new JPanel();
            InfoPanel.setOpaque(false);
            InfoPanel.setLayout(new BoxLayout(InfoPanel, BoxLayout.Y_AXIS));

            JLabel NameLabel = new JLabel(CourseName);
            NameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            NameLabel.setForeground(ColorTextPrimary);
            NameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel CodeLabel = new JLabel(CourseCode);
            CodeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            CodeLabel.setForeground(ColorTextSecondary);
            CodeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            InfoPanel.add(NameLabel);
            InfoPanel.add(Box.createVerticalStrut(5));
            InfoPanel.add(CodeLabel);

            return InfoPanel;
        }

        private static JPanel CreateMetricPanel(String TitleText, String ValueText) {
            JPanel MetricPanel = new JPanel();
            MetricPanel.setOpaque(false);
            MetricPanel.setLayout(new BoxLayout(MetricPanel, BoxLayout.Y_AXIS));
            MetricPanel.setPreferredSize(new Dimension(60, 50));

            JLabel ValueLabel = new JLabel(ValueText);
            ValueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            ValueLabel.setForeground(ColorTextPrimary);
            ValueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel TitleLabel = new JLabel(TitleText);
            TitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            TitleLabel.setForeground(ColorTextSecondary);
            TitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            MetricPanel.add(ValueLabel);
            MetricPanel.add(Box.createVerticalStrut(5));
            MetricPanel.add(TitleLabel);

            return MetricPanel;
        }

        private static JPanel CreateEnrollmentPanel(int EnrolledCount, int Capacity) {
            JPanel EnrollmentPanel = new JPanel(new BorderLayout(0, 5));
            EnrollmentPanel.setOpaque(false);
            EnrollmentPanel.setPreferredSize(new Dimension(150, 40));

            String EnrollmentText = EnrolledCount + " / " + Capacity + " Students";
            JLabel CountLabel = new JLabel(EnrollmentText);
            CountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            CountLabel.setForeground(ColorTextSecondary);
            CountLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            JProgressBar ProgressBar = new JProgressBar(0, Capacity);
            ProgressBar.setValue(EnrolledCount);
            ProgressBar.setForeground(ColorAccent);
            ProgressBar.setBackground(ColorBackground.darker());
            ProgressBar.setBorderPainted(false);

            EnrollmentPanel.add(CountLabel, BorderLayout.NORTH);
            EnrollmentPanel.add(ProgressBar, BorderLayout.SOUTH);

            return EnrollmentPanel;
        }
    }

    private class AnnouncementDetailPanel extends JPanel {

        private JLabel CourseLabel;
        private DefaultListModel<String> AnnouncementListModel;
        private JList<String> AnnouncementsList;
        private JTextField DateField;
        private JTextArea MessageArea;

        private Offerings CurrentOffering;
        private String CurrentCourseCode;
        private String CurrentCourseName;

        private List<Map<String, String>> CurrentAnnouncements = new ArrayList<>();

        public AnnouncementDetailPanel() {
            super(new BorderLayout(10, 10));
            setOpaque(false);
            setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(0x333333)),
                    "Announcements",
                    0, 0,
                    new Font("SansSerif", Font.PLAIN, 12),
                    ColorTextSecondary
            ));

            JPanel HeaderPanel = new JPanel(new BorderLayout());
            HeaderPanel.setOpaque(false);
            CourseLabel = new JLabel("Select a section to view/post announcements");
            CourseLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            CourseLabel.setForeground(ColorTextPrimary);
            HeaderPanel.add(CourseLabel, BorderLayout.WEST);
            add(HeaderPanel, BorderLayout.NORTH);

            JSplitPane SplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            SplitPane.setResizeWeight(0.6);
            SplitPane.setBorder(null);

            AnnouncementListModel = new DefaultListModel<>();
            AnnouncementsList = new JList<>(AnnouncementListModel);
            AnnouncementsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            AnnouncementsList.setFont(new Font("SansSerif", Font.PLAIN, 13));
            AnnouncementsList.setBackground(ColorPanel);
            AnnouncementsList.setForeground(ColorTextPrimary);

            JScrollPane ListScrollPane = new JScrollPane(AnnouncementsList);
            ListScrollPane.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(0x333333)),
                    "Existing Announcements",
                    0, 0,
                    new Font("SansSerif", Font.PLAIN, 12),
                    ColorTextSecondary
            ));

            SplitPane.setTopComponent(ListScrollPane);

            JPanel FormPanel = new JPanel(new GridBagLayout());
            FormPanel.setOpaque(false);
            GridBagConstraints GridConstraints = new GridBagConstraints();
            GridConstraints.insets = new Insets(4, 4, 4, 4);
            GridConstraints.fill = GridBagConstraints.HORIZONTAL;

            GridConstraints.gridx = 0;
            GridConstraints.gridy = 0;
            GridConstraints.weightx = 0;
            JLabel DateLabel = new JLabel("Date (DD/MM/YYYY):");
            DateLabel.setForeground(ColorTextSecondary);
            FormPanel.add(DateLabel, GridConstraints);

            GridConstraints.gridx = 1;
            GridConstraints.weightx = 1.0;
            DateField = new JTextField(10);
            DateField.setBackground(ColorPanel);
            DateField.setForeground(ColorTextPrimary);
            DateField.setCaretColor(ColorTextPrimary);
            DateField.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
            DateField.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            FormPanel.add(DateField, GridConstraints);

            GridConstraints.gridx = 0;
            GridConstraints.gridy = 1;
            GridConstraints.weightx = 0;
            GridConstraints.anchor = GridBagConstraints.NORTHWEST;
            JLabel MessageLabel = new JLabel("Message:");
            MessageLabel.setForeground(ColorTextSecondary);
            FormPanel.add(MessageLabel, GridConstraints);

            GridConstraints.gridx = 1;
            GridConstraints.weightx = 1.0;
            GridConstraints.weighty = 1.0;
            GridConstraints.fill = GridBagConstraints.BOTH;
            MessageArea = new JTextArea(5, 20);
            MessageArea.setLineWrap(true);
            MessageArea.setWrapStyleWord(true);
            MessageArea.setBackground(ColorPanel);
            MessageArea.setForeground(ColorTextPrimary);
            MessageArea.setCaretColor(ColorTextPrimary);
            MessageArea.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
            JScrollPane MessageScrollPane = new JScrollPane(MessageArea);
            FormPanel.add(MessageScrollPane, GridConstraints);

            GridConstraints.gridx = 0;
            GridConstraints.gridy = 2;
            GridConstraints.gridwidth = 2;
            GridConstraints.weightx = 0;
            GridConstraints.weighty = 0;
            GridConstraints.fill = GridBagConstraints.NONE;
            GridConstraints.anchor = GridBagConstraints.EAST;

            JPanel ButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            ButtonsPanel.setOpaque(false);

            JButton PostButton = new JButton("Post Announcement");
            JButton DeleteButton = new JButton("Delete Selected");

            PostButton.addActionListener(ActionEventObject -> OnPostAnnouncement());
            DeleteButton.addActionListener(ActionEventObject -> OnDeleteAnnouncement());

            ButtonsPanel.add(DeleteButton);
            ButtonsPanel.add(PostButton);

            FormPanel.add(ButtonsPanel, GridConstraints);

            JPanel FormContainerPanel = new JPanel(new BorderLayout());
            FormContainerPanel.setOpaque(false);
            FormContainerPanel.add(FormPanel, BorderLayout.CENTER);

            SplitPane.setBottomComponent(FormContainerPanel);

            add(SplitPane, BorderLayout.CENTER);
        }

        public void LoadForOffering(Offerings SelectedOffering, String CourseName, String CourseCode) {
            this.CurrentOffering = SelectedOffering;
            this.CurrentCourseCode = CourseCode;
            this.CurrentCourseName = CourseName;
            CourseLabel.setText(CourseName + " (" + CourseCode + ") - Offering " + SelectedOffering.GetOfferingID());
            ReloadAnnouncementList();
        }

        private void ReloadAnnouncementList() {
            AnnouncementListModel.clear();
            CurrentAnnouncements.clear();
            if (CurrentOffering == null) {
                return;
            }

            try {
                CurrentAnnouncements = GetAnnouncementsForOffering(CurrentOffering.GetOfferingID());
                for (Map<String, String> Announcement : CurrentAnnouncements) {
                    String DateText = Announcement.getOrDefault("Date", "");
                    String DataText = Announcement.getOrDefault("Data", "");
                    String PreviewText = DataText.replace("\n", " ");
                    if (PreviewText.length() > 80) {
                        PreviewText = PreviewText.substring(0, 77) + "...";
                    }
                    AnnouncementListModel.addElement(DateText + " - " + PreviewText);
                }
            } catch (SQLException ExceptionObject) {
                JOptionPane.showMessageDialog(this,
                        "Error loading announcements: " + ExceptionObject.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void OnPostAnnouncement() {
            if (CurrentOffering == null) {
                JOptionPane.showMessageDialog(this,
                        "Select a section first.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String DateText = DateField.getText().trim();
            String MessageText = MessageArea.getText().trim();

            if (DateText.isEmpty() || MessageText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter both date and message.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                boolean InsertOk = AddAnnouncementToOffering(CurrentOffering.GetOfferingID(), DateText, MessageText);
                if (!InsertOk) {
                    JOptionPane.showMessageDialog(this,
                            "Failed to save announcement.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    Notification AnnNotif = new Notification(
                            "New Announcement",
                            "New announcement posted in " + CurrentCourseCode + ": " + CurrentCourseName,
                            "ANNOUNCEMENT",
                            LocalDate.now().toString()
                    );

                    List<StudentGradeRecord> StudentsList = StudentGradeRecordHandler.FindByOfferingID(CurrentOffering.GetOfferingID());

                    for (StudentGradeRecord Rec : StudentsList) {
                        NotificationHandler.AddNotification(Rec.getRollNumber(), AnnNotif);
                    }
                } catch (Exception Ex) {
                    System.err.println("Failed to send notifications for announcement: " + Ex.getMessage());
                    Ex.printStackTrace();
                }

                MessageArea.setText("");
                ReloadAnnouncementList();
            } catch (SQLException ExceptionObject) {
                JOptionPane.showMessageDialog(this,
                        "Database error: " + ExceptionObject.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void OnDeleteAnnouncement() {
            if (CurrentOffering == null) {
                return;
            }
            int SelectedIndex = AnnouncementsList.getSelectedIndex();
            if (SelectedIndex < 0) {
                JOptionPane.showMessageDialog(this,
                        "Select an announcement to delete.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int ConfirmResult = JOptionPane.showConfirmDialog(this,
                    "Delete selected announcement?",
                    "Confirm", JOptionPane.YES_NO_OPTION);
            if (ConfirmResult != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                List<Map<String, String>> Announcements = GetAnnouncementsForOffering(CurrentOffering.GetOfferingID());
                if (SelectedIndex >= 0 && SelectedIndex < Announcements.size()) {
                    Announcements.remove(SelectedIndex);
                    SaveAnnouncementsForOffering(CurrentOffering.GetOfferingID(), Announcements);
                }
                ReloadAnnouncementList();
            } catch (SQLException ExceptionObject) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting announcement: " + ExceptionObject.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int CornerRadius = 15;

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
        catch (Exception IgnoredException) {}

        SwingUtilities.invokeLater(() -> {
            JFrame Frame = new JFrame("Instructor Announcements Page Debug");
            Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Frame.setSize(1200, 800);
            Frame.getContentPane().setBackground(ColorBackground);

            JPanel MainHolderPanel = new JPanel(new BorderLayout());
            MainHolderPanel.setBackground(ColorBackground);
            MainHolderPanel.setBorder(new EmptyBorder(20, 25, 25, 25));
            MainHolderPanel.add(new AnnouncementPages(1), BorderLayout.CENTER);

            Frame.add(MainHolderPanel);
            Frame.setLocationRelativeTo(null);
            Frame.setVisible(true);
        });
    }
}