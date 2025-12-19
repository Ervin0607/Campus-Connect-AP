package Frontend.StudentPages;

import Backend.DataBaseHandler.*;
import Backend.domain.*;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

public class Courses extends JPanel {

    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel = new Color(0x1E1E1E);
    private static final Color ColorAccent = new Color(0xB0F2B4);
    private static final Color ColorDanger = new Color(0xFF6B6B);
    private static final Color ColorTextPrimary = new Color(0xFFFFFF);
    private static final Color ColorTextSecondary = new Color(0xB0B0B0);
    private static final Color ColorWarning = new Color(0xFFD54F);

    private final JPanel CourseListPanel;
    private JTextField SearchField;
    private JLabel TotalCreditsLabel;
    private JButton ViewButton;
    private JLabel StatusLabel;

    private List<RegistrationItemPanel> AllCoursesList = new ArrayList<>();
    private Set<Integer> SelectedOfferingIDs = new HashSet<>();
    private int CurrentTotalCredits = 0;
    private boolean IsLocked = false;
    private int MaxCredits = 20;
    private Student CurrentStudent;

    private boolean IsMaintenanceMode = false;
    private boolean IsDeadlinePassed = false;
    private SemestersHandler.SemesterProfile CurrentSemesterProfile;

    public Courses(int StudentRollNumber) {
        super(new BorderLayout(10, 15));
        try {
            CurrentStudent = StudentHandler.FindStudentByRollNumber(StudentRollNumber);
            IsMaintenanceMode = SemestersHandler.IsMaintenanceMode();
            CurrentSemesterProfile = SemestersHandler.GetCurrentSemester();
            CheckRegistrationDeadline();
        } catch (SQLException ExceptionObject) {
            throw new RuntimeException(ExceptionObject);
        }

        setOpaque(false);
        setBorder(new EmptyBorder(10, 15, 15, 15));

        add(CreateHeaderPanel(), BorderLayout.NORTH);

        CourseListPanel = new JPanel();
        CourseListPanel.setLayout(new BoxLayout(CourseListPanel, BoxLayout.Y_AXIS));
        CourseListPanel.setOpaque(false);

        JScrollPane ScrollPane = new JScrollPane(CourseListPanel);
        ScrollPane.setOpaque(false);
        ScrollPane.getViewport().setOpaque(false);
        ScrollPane.setBorder(BorderFactory.createEmptyBorder());
        ScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(ScrollPane, BorderLayout.CENTER);

        add(CreateBottomActionPanel(), BorderLayout.SOUTH);

        LoadExistingRegistrations();

        AllCoursesList = InitialiseCoursePanels();
        UpdateCourseList("");
    }

    private void CheckRegistrationDeadline() {
        if (CurrentSemesterProfile != null && CurrentSemesterProfile.RegistrationEndDate != null) {
            try {
                LocalDate RegistrationDeadline = LocalDate.parse(CurrentSemesterProfile.RegistrationEndDate);
                LocalDate CurrentDate = LocalDate.now();
                if (CurrentDate.isAfter(RegistrationDeadline)) {
                    IsDeadlinePassed = true;
                }
            }
            catch (DateTimeParseException ExceptionObject) {
                System.err.println("Error parsing registration deadline: " + ExceptionObject.getMessage());
            }
        }
    }

    private JPanel CreateBottomActionPanel() {
        JPanel BottomPanel = new JPanel(new BorderLayout());
        BottomPanel.setOpaque(false);
        BottomPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        TotalCreditsLabel = new JLabel("Total Credits: 0" + "/" + MaxCredits);
        TotalCreditsLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        TotalCreditsLabel.setForeground(ColorAccent);

        JPanel LeftContainerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        LeftContainerPanel.setOpaque(false);
        LeftContainerPanel.add(TotalCreditsLabel);

        if (IsDeadlinePassed) {
            StatusLabel = new JLabel("⛔ Registration Closed");
            StatusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            StatusLabel.setForeground(ColorDanger);
            LeftContainerPanel.add(StatusLabel);
        }

        ViewButton = new JButton("View Selected Courses");
        ViewButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        ViewButton.setBackground(ColorAccent);
        ViewButton.setForeground(Color.BLACK);
        ViewButton.setFocusPainted(false);
        ViewButton.setPreferredSize(new Dimension(220, 45));
        ViewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (IsDeadlinePassed) {
            ViewButton.setText("Registration Closed");
            ViewButton.setBackground(ColorPanel);
            ViewButton.setForeground(ColorDanger);
        }

        ViewButton.addActionListener(ActionEventObject -> OpenReviewDialog());

        BottomPanel.add(LeftContainerPanel, BorderLayout.WEST);
        BottomPanel.add(ViewButton, BorderLayout.EAST);

        return BottomPanel;
    }

    private void OpenReviewDialog() {
        if (SelectedOfferingIDs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "You haven't selected any courses yet.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Frame OwnerFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        new RegisteredCoursesDialog(OwnerFrame, this).setVisible(true);
    }

    void LockRegistration() {
        if (IsMaintenanceMode) {
            JOptionPane.showMessageDialog(this, "System is under maintenance.", "Blocked", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (IsDeadlinePassed) {
            JOptionPane.showMessageDialog(this, "Registration deadline has passed.", "Deadline Passed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (CurrentSemesterProfile != null) {
                boolean LockSuccess = RegistrationHandler.LockRegistrations(
                        CurrentStudent.GetRollNumber(),
                        CurrentSemesterProfile.Semester,
                        CurrentSemesterProfile.Year
                );

                if (!LockSuccess && SelectedOfferingIDs.isEmpty()) {
                }
            }

            for (int CurrentOfferingID : SelectedOfferingIDs) {
                Offerings CurrentOffering = OfferingHandler.FindOffering(CurrentOfferingID);
                if (CurrentOffering == null) {
                    continue;
                }

                HashMap<String, Integer> GradesMap = new HashMap<>();
                for (String GradingComponent : CurrentOffering.GetGradingComponents().keySet()) {
                    GradesMap.put(GradingComponent, 0);
                }

                StudentGradeRecord NewGradeRecord = new StudentGradeRecord();
                NewGradeRecord.setOfferingID(CurrentOffering.GetOfferingID());
                NewGradeRecord.setRollNumber(CurrentStudent.GetRollNumber());
                NewGradeRecord.setGrade(GradesMap);

                try {
                    StudentGradeRecordHandler.AddGradeRecord(NewGradeRecord);
                }
                catch (SQLException IgnoredException) {
                }
            }

            IsLocked = true;
            ViewButton.setText("Registration Finalized");
            ViewButton.setEnabled(true);
            ViewButton.setBackground(ColorPanel);
            ViewButton.setForeground(ColorTextSecondary);

            for (RegistrationItemPanel CoursePanel : AllCoursesList) {
                CoursePanel.ActionButton.setEnabled(false);
                if (SelectedOfferingIDs.contains(CoursePanel.OfferingID)) {
                    CoursePanel.ActionButton.setText("Registered");
                    CoursePanel.ActionButton.setBackground(ColorPanel);
                }
            }

            System.out.println("LOCKED Courses: " + SelectedOfferingIDs);
            JOptionPane.showMessageDialog(this, "Registration confirmed successfully!");

        }
        catch (SQLException ExceptionObject) {
            ExceptionObject.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + ExceptionObject.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void UpdateCredits(int CreditsDelta) {
        CurrentTotalCredits += CreditsDelta;
        TotalCreditsLabel.setText("Total Credits: " + CurrentTotalCredits + "/" + MaxCredits);
    }

    private void LoadExistingRegistrations() {
        try {
            if (CurrentSemesterProfile == null) {
                return;
            }

            IsLocked = RegistrationHandler.IsTermLocked(CurrentStudent.GetRollNumber(), CurrentSemesterProfile.Semester, CurrentSemesterProfile.Year);

            List<Integer> EnrolledOfferingIDList = RegistrationHandler.getEnrolledOfferingIDs(CurrentStudent.GetRollNumber());

            if (!EnrolledOfferingIDList.isEmpty()) {
                SelectedOfferingIDs.addAll(EnrolledOfferingIDList);

                for (int CurrentOfferingID : EnrolledOfferingIDList) {
                    Offerings CurrentOffering = OfferingHandler.FindOffering(CurrentOfferingID);
                    if (CurrentOffering == null) {
                        continue;
                    }

                    if (CurrentOffering.GetYear() != CurrentSemesterProfile.Year ||
                            !CurrentOffering.GetSemester().equalsIgnoreCase(CurrentSemesterProfile.Semester)) {
                        continue;
                    }

                    Course CurrentCourse = CourseHandler.FindCourseByID(CurrentOffering.GetCourseID());
                    CurrentTotalCredits += CurrentCourse.GetCredits();
                }
                UpdateCredits(0);
            }

            if (IsLocked) {
                ViewButton.setText("Registration Finalized");
                ViewButton.setBackground(ColorPanel);
                ViewButton.setForeground(ColorTextSecondary);
            }
            else if (!IsDeadlinePassed) {
                ViewButton.setText("View Selected Courses");
                ViewButton.setBackground(ColorAccent);
                ViewButton.setForeground(Color.BLACK);
            }
            ViewButton.setEnabled(true);

        }
        catch (SQLException ExceptionObject) {
            ExceptionObject.printStackTrace();
        }
    }

    private List<RegistrationItemPanel> InitialiseCoursePanels() {
        List<RegistrationItemPanel> RegistrationPanelList = new ArrayList<>();
        List<Offerings> OfferingsList = new ArrayList<>();
        try {
            OfferingsList = OfferingHandler.GetAllOfferings();
        }
        catch (SQLException ExceptionObject) {
            ExceptionObject.printStackTrace();
        }

        for (Offerings CurrentOffering : OfferingsList) {
            if (CurrentSemesterProfile != null) {
                if (CurrentOffering.GetYear() != CurrentSemesterProfile.Year) {
                    continue;
                }
                if (!CurrentOffering.GetSemester().equalsIgnoreCase(CurrentSemesterProfile.Semester)) {
                    continue;
                }
            }

            try {
                Instructor CurrentInstructor = InstructorHandler.FindInstructorByInstructorID(CurrentOffering.GetInstructorID());
                Course CurrentCourse = CourseHandler.FindCourseByID(CurrentOffering.GetCourseID());
                RegistrationPanelList.add(new RegistrationItemPanel(
                        this,
                        String.valueOf(CurrentCourse.GetCode()),
                        CurrentCourse.GetTitle(),
                        CurrentInstructor.GetName(),
                        CurrentCourse.GetCredits(),
                        CurrentOffering.GetOfferingID(),
                        CurrentOffering.GetSemester(),
                        CurrentOffering.GetYear(),
                        CurrentOffering.GetCurrentEnrollment(),
                        CurrentOffering.GetCapacity()
                ));
            }
            catch (SQLException ExceptionObject) {
                System.out.println(ExceptionObject.getMessage());
            }
        }
        return RegistrationPanelList;
    }

    private JPanel CreateHeaderPanel() {
        JPanel HeaderPanel = new JPanel(new BorderLayout(20, 0));
        HeaderPanel.setOpaque(false);

        String SemesterTitle = (CurrentSemesterProfile != null) ? CurrentSemesterProfile.Semester + " " + CurrentSemesterProfile.Year + " Registration" : "Course Registration";

        JLabel TitleLabel = new JLabel(SemesterTitle);
        TitleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        TitleLabel.setForeground(ColorTextPrimary);
        TitleLabel.setBorder(new EmptyBorder(0, 5, 5, 0));
        HeaderPanel.add(TitleLabel, BorderLayout.WEST);

        SearchField = new JTextField();
        SearchField.putClientProperty("JTextField.placeholderText", "Search courses...");
        SearchField.setBackground(ColorPanel);
        SearchField.setForeground(ColorTextPrimary);
        SearchField.setCaretColor(ColorTextSecondary);
        SearchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        SearchField.setBorder(new EmptyBorder(8, 12, 8, 12));
        SearchField.setPreferredSize(new Dimension(300, 40));
        SearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent DocumentEventObject) {
                UpdateCourseList(SearchField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent DocumentEventObject) {
                UpdateCourseList(SearchField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent DocumentEventObject) {
                UpdateCourseList(SearchField.getText());
            }
        });
        JPanel SearchContainerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        SearchContainerPanel.setOpaque(false);
        SearchContainerPanel.add(SearchField);
        HeaderPanel.add(SearchContainerPanel, BorderLayout.EAST);
        return HeaderPanel;
    }

    private void UpdateCourseList(String FilterText) {
        CourseListPanel.removeAll();
        String LowerFilterText = FilterText.toLowerCase().trim();
        for (RegistrationItemPanel CoursePanel : AllCoursesList) {
            if (CoursePanel.Name.toLowerCase().contains(LowerFilterText)
                    || CoursePanel.Code.toLowerCase().contains(LowerFilterText)
                    || CoursePanel.Professor.toLowerCase().contains(LowerFilterText)) {
                CourseListPanel.add(CoursePanel);
                CourseListPanel.add(Box.createVerticalStrut(10));
            }
        }
        CourseListPanel.add(Box.createVerticalGlue());
        CourseListPanel.revalidate();
        CourseListPanel.repaint();
    }

    private void RefreshCourseList() {
        AllCoursesList.clear();
        AllCoursesList = InitialiseCoursePanels();

        if (IsLocked || IsDeadlinePassed) {
            for (RegistrationItemPanel CoursePanel : AllCoursesList) {
                CoursePanel.ActionButton.setEnabled(false);
                if (SelectedOfferingIDs.contains(CoursePanel.OfferingID)) {
                    CoursePanel.ActionButton.setText("Registered");
                    CoursePanel.ActionButton.setBackground(ColorPanel);
                }
            }
        }
        UpdateCourseList(SearchField.getText());
    }

    private static class RegistrationItemPanel extends RoundedPanel {
        final String Code;
        final String Name;
        final String Professor;
        final int Credits;
        final int OfferingID;
        final String Semester;
        final int Year;
        final Courses ParentPage;
        boolean IsSelected = false;
        JButton ActionButton;

        public RegistrationItemPanel(Courses ParentPage, String Code, String Name, String Professor, int Credits, int OfferingID, String Semester, int Year, int EnrolledCount, int Capacity) {
            super();
            this.ParentPage = ParentPage;
            this.Code = Code;
            this.Name = Name;
            this.Professor = Professor;
            this.Credits = Credits;
            this.OfferingID = OfferingID;
            this.Semester = Semester;
            this.Year = Year;

            setBackground(ColorPanel);
            setLayout(new GridBagLayout());
            setBorder(new EmptyBorder(15, 20, 15, 20));
            GridBagConstraints GridConstraints = new GridBagConstraints();

            FlatSVGIcon CourseIcon = new FlatSVGIcon("Frontend/assets/book.svg", 32, 32);
            CourseIcon.setColorFilter(new FlatSVGIcon.ColorFilter(ColorValue -> ColorTextSecondary));
            JLabel IconLabel = new JLabel(CourseIcon);
            IconLabel.setBorder(new EmptyBorder(0, 0, 0, 15));

            JPanel InfoPanel = new JPanel();
            InfoPanel.setOpaque(false);
            InfoPanel.setLayout(new BoxLayout(InfoPanel, BoxLayout.Y_AXIS));
            JLabel NameLabel = new JLabel(Name);
            NameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            NameLabel.setForeground(ColorTextPrimary);
            JLabel CodeLabel = new JLabel(Code);
            CodeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            CodeLabel.setForeground(ColorTextSecondary);
            InfoPanel.add(NameLabel);
            InfoPanel.add(Box.createVerticalStrut(5));
            InfoPanel.add(CodeLabel);

            JPanel InfoWrapperPanel = new JPanel(new BorderLayout());
            InfoWrapperPanel.setOpaque(false);
            InfoWrapperPanel.add(IconLabel, BorderLayout.WEST);
            InfoWrapperPanel.add(InfoPanel, BorderLayout.CENTER);

            GridConstraints.gridx = 0;
            GridConstraints.gridy = 0;
            GridConstraints.weightx = 1.0;
            GridConstraints.fill = GridBagConstraints.HORIZONTAL;
            GridConstraints.anchor = GridBagConstraints.WEST;
            add(InfoWrapperPanel, GridConstraints);

            JPanel CreditsPanel = CreateMetricPanel("Credits", String.valueOf(Credits));
            GridConstraints.gridx = 1;
            GridConstraints.weightx = 0;
            GridConstraints.fill = GridBagConstraints.NONE;
            GridConstraints.anchor = GridBagConstraints.CENTER;
            GridConstraints.insets = new Insets(0, 20, 0, 20);
            add(CreditsPanel, GridConstraints);

            JPanel SemesterPanel = CreateMetricPanel("Sem", Semester);
            GridConstraints.gridx = 2;
            GridConstraints.weightx = 0;
            GridConstraints.insets = new Insets(0, 0, 0, 20);
            add(SemesterPanel, GridConstraints);

            JPanel YearPanel = CreateMetricPanel("Year", String.valueOf(Year));
            GridConstraints.gridx = 3;
            GridConstraints.weightx = 0;
            GridConstraints.insets = new Insets(0, 0, 0, 20);
            add(YearPanel, GridConstraints);

            JPanel ProfessorPanel = CreateProfessorPanel(Professor);
            GridConstraints.gridx = 4;
            GridConstraints.weightx = 0;
            GridConstraints.anchor = GridBagConstraints.WEST;
            GridConstraints.insets = new Insets(0, 0, 0, 20);
            add(ProfessorPanel, GridConstraints);

            JPanel EnrollmentPanel = CreateEnrollmentPanel(EnrolledCount, Capacity);
            GridConstraints.gridx = 5;
            GridConstraints.weightx = 0;
            GridConstraints.insets = new Insets(0, 0, 0, 30);
            add(EnrollmentPanel, GridConstraints);

            ActionButton = new JButton("Register");
            ActionButton.setPreferredSize(new Dimension(100, 35));
            ActionButton.setFont(new Font("SansSerif", Font.BOLD, 12));
            ActionButton.setFocusPainted(false);
            SetButtonStyle(false);

            if (ParentPage.SelectedOfferingIDs.contains(OfferingID)) {
                IsSelected = true;
                SetButtonStyle(true);
            } else {
                SetButtonStyle(false);
            }

            if (ParentPage.IsLocked || ParentPage.IsDeadlinePassed) {
                ActionButton.setEnabled(false);
                if (IsSelected) {
                    ActionButton.setText("Registered");
                    ActionButton.setBackground(ColorPanel);
                    ActionButton.setForeground(ColorAccent);
                } else {
                    ActionButton.setText("Closed");
                    ActionButton.setBackground(ColorPanel);
                    ActionButton.setForeground(ColorTextSecondary);
                }
            } else if (ParentPage.IsMaintenanceMode) {
                ActionButton.setEnabled(false);
                if (IsSelected) {
                    ActionButton.setText("Drop");
                    ActionButton.setBackground(ColorPanel);
                    ActionButton.setForeground(ColorAccent);
                } else {
                    ActionButton.setText("Register");
                    ActionButton.setBackground(ColorPanel);
                    ActionButton.setForeground(ColorTextSecondary);
                }
            } else {
                if (EnrolledCount >= Capacity && !IsSelected) {
                    ActionButton.setEnabled(false);
                    ActionButton.setText("Full");
                    ActionButton.setBackground(ColorPanel);
                    ActionButton.setForeground(ColorTextSecondary);
                } else {
                    ActionButton.addActionListener(ActionEventObject -> ToggleSelection());
                }
            }

            GridConstraints.gridx = 6;
            GridConstraints.weightx = 0;
            GridConstraints.anchor = GridBagConstraints.EAST;
            GridConstraints.insets = new Insets(0, 0, 0, 0);
            add(ActionButton, GridConstraints);

            setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        }

        private void ToggleSelection() {
            if (ParentPage.IsLocked || ParentPage.IsMaintenanceMode || ParentPage.IsDeadlinePassed) {
                return;
            }

            IsSelected = !IsSelected;
            if (IsSelected) {
                for (RegistrationItemPanel OtherPanel : ParentPage.AllCoursesList) {
                    if (ParentPage.SelectedOfferingIDs.contains(OtherPanel.OfferingID)
                            && OtherPanel.Code.equals(Code)
                            && OtherPanel.OfferingID != OfferingID) {

                        JOptionPane.showMessageDialog(ParentPage,
                                "You are already registered for " + Code + " (Another Section).\n" +
                                        "Please drop the existing section before registering for this one.",
                                "Duplicate Course Selection", JOptionPane.WARNING_MESSAGE);

                        IsSelected = false;
                        return;
                    }
                }

                try {
                    boolean EnrollmentStatus = RegistrationHandler.EnrollIfPossible(ParentPage.CurrentStudent.GetRollNumber(), OfferingID);
                    if (!EnrollmentStatus) {
                        JOptionPane.showMessageDialog(ParentPage, "Enrollment failed. Course might be full or duplicate.", "Error", JOptionPane.ERROR_MESSAGE);
                        IsSelected = false;
                        return;
                    }
                    ParentPage.SelectedOfferingIDs.add(OfferingID);
                    ParentPage.UpdateCredits(Credits);
                    SetButtonStyle(true);
                    ParentPage.RefreshCourseList();
                }
                catch (Exception ExceptionObject) {
                    if (ExceptionObject instanceof Backend.exceptions.CapacityFullException) {
                        JOptionPane.showMessageDialog(ParentPage, "Course Capacity Full!", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        System.out.println("Error registering: " + ExceptionObject.getMessage());
                    }
                    IsSelected = false;
                }
            }
            else {
                try {
                    boolean DropStatus = RegistrationHandler.DropByStudentAndOffering(ParentPage.CurrentStudent.GetRollNumber(), OfferingID);
                    if (DropStatus) {
                        ParentPage.SelectedOfferingIDs.remove(OfferingID);
                        ParentPage.UpdateCredits(-Credits);
                        SetButtonStyle(false);
                        ParentPage.RefreshCourseList();
                    }
                    else {
                        System.out.println("Drop failed.");
                    }
                }
                catch (SQLException ExceptionObject) {
                    System.out.println("Error dropping: " + ExceptionObject.getMessage());
                }
            }
        }

        private void SetButtonStyle(boolean IsRegistered) {
            if (ParentPage.IsMaintenanceMode || ParentPage.IsDeadlinePassed) {
                return;
            }

            if (IsRegistered) {
                ActionButton.setText("Drop");
                ActionButton.setBackground(ColorDanger);
                ActionButton.setForeground(Color.WHITE);
            }
            else {
                ActionButton.setText("Register");
                ActionButton.setBackground(ColorAccent);
                ActionButton.setForeground(Color.BLACK);
            }
        }

        private JPanel CreateMetricPanel(String TitleText, String ValueText) {
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

        private JPanel CreateProfessorPanel(String ProfessorName) {
            JPanel ProfessorPanel = new JPanel();
            ProfessorPanel.setOpaque(false);
            ProfessorPanel.setLayout(new BoxLayout(ProfessorPanel, BoxLayout.Y_AXIS));
            ProfessorPanel.setPreferredSize(new Dimension(160, 50));

            JLabel ProfessorLabel = new JLabel(ProfessorName);
            ProfessorLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            ProfessorLabel.setForeground(ColorTextPrimary);
            ProfessorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel TitleLabel = new JLabel("Professor");
            TitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            TitleLabel.setForeground(ColorTextSecondary);
            TitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            ProfessorPanel.add(ProfessorLabel);
            ProfessorPanel.add(Box.createVerticalStrut(5));
            ProfessorPanel.add(TitleLabel);
            return ProfessorPanel;
        }

        private JPanel CreateEnrollmentPanel(int EnrolledCount, int Capacity) {
            JPanel EnrollmentPanel = new JPanel(new BorderLayout(0, 5));
            EnrollmentPanel.setOpaque(false);
            EnrollmentPanel.setPreferredSize(new Dimension(120, 40));

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
            ProgressBar.setPreferredSize(new Dimension(120, 6));

            EnrollmentPanel.add(CountLabel, BorderLayout.NORTH);
            EnrollmentPanel.add(ProgressBar, BorderLayout.SOUTH);
            return EnrollmentPanel;
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

    private static class RegisteredCoursesDialog extends JDialog {
        public RegisteredCoursesDialog(Frame OwnerFrame, Courses ParentPage) {
            super(OwnerFrame, "Review Selected Courses", true);
            setSize(900, 550);
            setLocationRelativeTo(OwnerFrame);
            setLayout(new BorderLayout(10, 10));
            getContentPane().setBackground(ColorBackground);

            JLabel TitleLabel = new JLabel("Confirm Your Selection", SwingConstants.CENTER);
            TitleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
            TitleLabel.setForeground(ColorTextPrimary);
            TitleLabel.setBorder(new EmptyBorder(20, 0, 10, 0));
            add(TitleLabel, BorderLayout.NORTH);

            String[] ColumnNames = {"Course Code", "Course Name", "Professor", "Credits"};
            DefaultTableModel TableModel = new DefaultTableModel(ColumnNames, 0) {
                @Override
                public boolean isCellEditable(int RowIndex, int ColumnIndex) {
                    return false;
                }
            };

            for (RegistrationItemPanel CoursePanel : ParentPage.AllCoursesList) {
                if (ParentPage.SelectedOfferingIDs.contains(CoursePanel.OfferingID)) {
                    TableModel.addRow(new Object[]{CoursePanel.Code, CoursePanel.Name, CoursePanel.Professor, CoursePanel.Credits});
                }
            }

            JTable CoursesTable = new JTable(TableModel);
            StyleTable(CoursesTable);
            JScrollPane TableScrollPane = new JScrollPane(CoursesTable);
            TableScrollPane.getViewport().setBackground(ColorPanel);
            TableScrollPane.setBorder(BorderFactory.createEmptyBorder());

            JPanel TableWrapperPanel = new JPanel(new BorderLayout());
            TableWrapperPanel.setOpaque(false);
            TableWrapperPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
            TableWrapperPanel.add(TableScrollPane);
            add(TableWrapperPanel, BorderLayout.CENTER);

            JPanel ButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            ButtonPanel.setOpaque(false);
            ButtonPanel.setBorder(new EmptyBorder(15, 20, 20, 20));

            JButton PrintButton = new JButton("Print");
            JButton CancelButton = new JButton("Go Back");
            JButton ConfirmButton = new JButton("Lock & Submit");

            PrintButton.setBackground(ColorPanel);
            PrintButton.setForeground(ColorTextPrimary);
            PrintButton.setFont(new Font("SansSerif", Font.BOLD, 14));
            PrintButton.addActionListener(ActionEventObject -> {
                try {
                    java.text.MessageFormat HeaderFormat = new java.text.MessageFormat("Course Registration - " + ParentPage.CurrentStudent.GetRollNumber());
                    java.text.MessageFormat FooterFormat = new java.text.MessageFormat("Page {0,number,integer}");
                    CoursesTable.print(JTable.PrintMode.FIT_WIDTH, HeaderFormat, FooterFormat);
                } catch (java.awt.print.PrinterException ExceptionObject) {
                    JOptionPane.showMessageDialog(this, "Printing Failed: " + ExceptionObject.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            CancelButton.addActionListener(ActionEventObject -> dispose());

            ConfirmButton.setBackground(ColorAccent);
            ConfirmButton.setForeground(Color.BLACK);
            ConfirmButton.setFont(new Font("SansSerif", Font.BOLD, 14));

            if (ParentPage.IsLocked) {
                ConfirmButton.setText("Already Locked");
                ConfirmButton.setEnabled(false);
                ConfirmButton.setBackground(ColorPanel);
                ConfirmButton.setForeground(ColorTextSecondary);
            }
            else if (ParentPage.IsMaintenanceMode) {
                ConfirmButton.setText("Maintenance Mode");
                ConfirmButton.setEnabled(false);
                ConfirmButton.setBackground(ColorPanel);
                ConfirmButton.setForeground(ColorWarning);
            }
            else if (ParentPage.IsDeadlinePassed) {
                ConfirmButton.setText("Deadline Passed");
                ConfirmButton.setEnabled(false);
                ConfirmButton.setBackground(ColorPanel);
                ConfirmButton.setForeground(ColorDanger);
            }
            else if (ParentPage.CurrentTotalCredits < 20) {
                ConfirmButton.setText("Min 20 Credits Required");
                ConfirmButton.setEnabled(false);
                ConfirmButton.setBackground(ColorPanel);
                ConfirmButton.setForeground(ColorDanger);
            }

            ConfirmButton.addActionListener(ActionEventObject -> {
                int ConfirmationChoice = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to lock these courses?\nThis action cannot be undone.",
                        "Final Confirmation", JOptionPane.YES_NO_OPTION);

                if (ConfirmationChoice == JOptionPane.YES_OPTION) {
                    ParentPage.LockRegistration();
                    dispose();
                }
            });

            ButtonPanel.add(PrintButton);
            ButtonPanel.add(Box.createHorizontalStrut(10));
            ButtonPanel.add(CancelButton);
            ButtonPanel.add(Box.createHorizontalStrut(10));
            ButtonPanel.add(ConfirmButton);
            add(ButtonPanel, BorderLayout.SOUTH);
        }

        private void StyleTable(JTable CoursesTable) {
            CoursesTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
            CoursesTable.setForeground(ColorTextPrimary);
            CoursesTable.setBackground(ColorPanel);
            CoursesTable.setGridColor(ColorBackground.darker());
            CoursesTable.setRowHeight(30);
            CoursesTable.setSelectionBackground(new Color(0x3B82F6));
            CoursesTable.setSelectionForeground(Color.WHITE);
            CoursesTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
            CoursesTable.getTableHeader().setBackground(ColorPanel);
            CoursesTable.getTableHeader().setForeground(ColorTextSecondary);
            CoursesTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorBackground.darker()));
        }
    }

    public static void main(String[] Arguments) {
        try {
            FlatDarkLaf.setup();
        }
        catch (Exception ExceptionObject) {
        }
        SwingUtilities.invokeLater(() -> {
            JFrame DebugFrame = new JFrame("Registration Debug");
            DebugFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            DebugFrame.setSize(1200, 800);
            DebugFrame.getContentPane().setBackground(ColorBackground);
            JPanel MainHolderPanel = new JPanel(new BorderLayout());
            MainHolderPanel.setBackground(ColorBackground);
            MainHolderPanel.setBorder(new EmptyBorder(20, 25, 25, 25));
            MainHolderPanel.add(new Courses(2024136), BorderLayout.CENTER);
            DebugFrame.add(MainHolderPanel);
            DebugFrame.setLocationRelativeTo(null);
            DebugFrame.setVisible(true);
        });
    }
}
