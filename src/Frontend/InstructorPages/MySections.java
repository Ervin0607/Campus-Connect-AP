package Frontend.InstructorPages;

import Backend.DataBaseHandler.CourseHandler;
import Backend.DataBaseHandler.InstructorHandler;
import Backend.domain.Instructor;
import Backend.domain.Course;
import Backend.domain.Offerings;
import Frontend.components.Tables.AssignGradesDialog;
import Frontend.components.Tables.ViewStatsDialog;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicMenuItemUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

import Frontend.components.Tables.GradeComponentsDialog;
import Frontend.components.Tables.GradeSlabsDialog;

import static Backend.DataBaseHandler.OfferingHandler.*;

public class MySections extends JPanel {

    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel = new Color(0x1E1E1E);
    private static final Color ColorAccent = new Color(0xB0F2B4);
    private static final Color ColorTextPrimary = new Color(0xFFFFFF);
    private static final Color ColorTextSecondary = new Color(0xB0B0B0);

    private final JPanel SectionsListPanel;
    private JTextField SearchField;

    private List<CourseItemPanel> AllSectionsList = new ArrayList<>();
    private final Instructor CurrentInstructor;

    public MySections(int CurrentInstructorID) {
        super(new BorderLayout(10, 15));
        try {
            this.CurrentInstructor = InstructorHandler.FindInstructorByInstructorID(CurrentInstructorID);
        } catch (SQLException ExceptionObject) {
            throw new RuntimeException(ExceptionObject);
        }

        setOpaque(false);
        setBorder(new EmptyBorder(10, 15, 15, 15));

        add(CreateHeaderPanel(), BorderLayout.NORTH);

        SectionsListPanel = new JPanel();
        SectionsListPanel.setLayout(new BoxLayout(SectionsListPanel, BoxLayout.Y_AXIS));
        SectionsListPanel.setOpaque(false);

        JScrollPane ScrollPane = new JScrollPane(SectionsListPanel);
        ScrollPane.setOpaque(false);
        ScrollPane.getViewport().setOpaque(false);
        ScrollPane.setBorder(BorderFactory.createEmptyBorder());
        ScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(ScrollPane, BorderLayout.CENTER);

        AllSectionsList = InitialiseSectionPanels();
        UpdateCourseList("");
    }

    private List<CourseItemPanel> InitialiseSectionPanels() {
        List<CourseItemPanel> SectionPanels = new ArrayList<>();
        List<Offerings> OfferingsList = new ArrayList<>();
        try {
            OfferingsList = GetAllOfferings();
        }
        catch (SQLException ExceptionObject) {
            JOptionPane.showMessageDialog(this, "Error fetching courses: " + ExceptionObject.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        for (Offerings CurrentOffering : OfferingsList) {
            if (CurrentOffering.GetInstructorID() == CurrentInstructor.GetInstructorID()) {
                try {
                    Course CurrentCourse = CourseHandler.FindCourseByID(CurrentOffering.GetCourseID());
                    SectionPanels.add(new CourseItemPanel(
                            String.valueOf(CurrentCourse.GetCode()),
                            CurrentCourse.GetTitle(),
                            CurrentCourse.GetCredits(),
                            CurrentOffering.GetCurrentEnrollment(),
                            CurrentOffering.GetCapacity(),
                            CurrentOffering.GetSemester(),
                            CurrentOffering.GetYear(),
                            CurrentOffering.GetOfferingID()
                    ));
                }
                catch (SQLException ExceptionObject) {
                    System.out.println("Error finding course details: " + ExceptionObject.getMessage());
                }
            }
        }
        return SectionPanels;
    }

    private JPanel CreateHeaderPanel() {
        JPanel HeaderPanel = new JPanel(new BorderLayout(20, 0));
        HeaderPanel.setOpaque(false);

        JLabel TitleLabel = new JLabel("My Courses");
        TitleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        TitleLabel.setForeground(ColorTextPrimary);
        TitleLabel.setBorder(new EmptyBorder(0, 5, 5, 0));
        HeaderPanel.add(TitleLabel, BorderLayout.WEST);

        SearchField = new JTextField();
        SearchField.putClientProperty("JTextField.placeholderText", "Search by name or code...");
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
        SectionsListPanel.removeAll();
        String LowerFilter = FilterText.toLowerCase().trim();

        for (CourseItemPanel CoursePanel : AllSectionsList) {
            if (CoursePanel.Name.toLowerCase().contains(LowerFilter) ||
                    CoursePanel.Code.toLowerCase().contains(LowerFilter)) {

                SectionsListPanel.add(CoursePanel);
                SectionsListPanel.add(Box.createVerticalStrut(10));
            }
        }

        SectionsListPanel.add(Box.createVerticalGlue());
        SectionsListPanel.revalidate();
        SectionsListPanel.repaint();
    }

    private static class CourseItemPanel extends RoundedPanel {

        final String Code;
        final String Name;
        final int Credits;
        final int OfferingID;
        private Offerings CurrentOffering;

        private static final Color PopupBackgroundColor = new Color(0x2A2A2A);
        private static final Color SelectionBackgroundColor = new Color(0x3B82F6);
        private static final Color SelectionForegroundColor = Color.WHITE;

        private Frame OwnerFrame = (Frame) SwingUtilities.getWindowAncestor(this);

        public CourseItemPanel(String Code, String Name, int Credits, int EnrolledCount, int Capacity, String Semester, int Year, int OfferingID) {
            super();
            this.Code = Code;
            this.Name = Name;
            this.Credits = Credits;
            this.OfferingID = OfferingID;

            setBackground(ColorPanel);
            setLayout(new GridBagLayout());
            setBorder(new EmptyBorder(15, 20, 15, 20));

            GridBagConstraints GridConstraints = new GridBagConstraints();

            FlatSVGIcon CourseIcon = new FlatSVGIcon("Frontend/assets/plus.svg", 32, 32);
            CourseIcon.setColorFilter(new FlatSVGIcon.ColorFilter(ColorValue -> ColorTextSecondary));

            JButton IconButton = new JButton(CourseIcon);
            IconButton.setOpaque(false);
            IconButton.setContentAreaFilled(false);
            IconButton.setBorderPainted(false);
            IconButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            IconButton.setBorder(new EmptyBorder(0, 0, 0, 15));

            IconButton.addActionListener(ActionEventObject -> {
                JPopupMenu PopupMenu = CreatePopupMenu();
                PopupMenu.show(IconButton, 0, IconButton.getHeight());
            });

            JPanel InfoPanel = CreateInfoPanel(Name, Code);
            JPanel InfoWrapperPanel = new JPanel(new BorderLayout());
            InfoWrapperPanel.setOpaque(false);
            InfoWrapperPanel.add(IconButton, BorderLayout.WEST);
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
            GridConstraints.insets = new Insets(0, 40, 0, 40);
            add(CreditsPanel, GridConstraints);

            JPanel SemesterPanel = CreateMetricPanel("Sem", Semester);
            GridConstraints.gridx = 2;
            GridConstraints.weightx = 0;
            GridConstraints.insets = new Insets(0, 0, 0, 40);
            add(SemesterPanel, GridConstraints);

            JPanel YearPanel = CreateMetricPanel("Year", String.valueOf(Year));
            GridConstraints.gridx = 3;
            GridConstraints.weightx = 0;
            GridConstraints.insets = new Insets(0, 0, 0, 40);
            add(YearPanel, GridConstraints);

            JPanel EnrollmentPanel = CreateEnrollmentPanel(EnrolledCount, Capacity);
            GridConstraints.gridx = 4;
            GridConstraints.weightx = 0;
            GridConstraints.anchor = GridBagConstraints.EAST;
            GridConstraints.insets = new Insets(0, 0, 0, 0);
            add(EnrollmentPanel, GridConstraints);

            setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        }

        private JPopupMenu CreatePopupMenu() {
            JPopupMenu Menu = new JPopupMenu();
            Menu.setBackground(PopupBackgroundColor);
            Menu.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            Menu.add(CreateFancyMenuItem("Grade Components"));
            Menu.add(CreateFancyMenuItem("Grade Slabs"));
            Menu.add(CreateFancyMenuItem("View Stats"));
            Menu.add(CreateFancyMenuItem("Assign Grades"));

            return Menu;
        }

        private JMenuItem CreateFancyMenuItem(String Text) {
            JMenuItem MenuItem = new JMenuItem(Text);
            MenuItem.setFont(new Font("SansSerif", Font.PLAIN, 16));
            MenuItem.setForeground(ColorTextPrimary);
            MenuItem.setBackground(PopupBackgroundColor);
            MenuItem.setBorder(new EmptyBorder(12, 20, 12, 20));

            MenuItem.setUI(new BasicMenuItemUI() {
                @Override
                public void paintBackground(Graphics GraphicsContext, JMenuItem MenuItemComponent, Color BackgroundColor) {
                    ButtonModel Model = MenuItemComponent.getModel();
                    int Width = MenuItemComponent.getWidth();
                    int Height = MenuItemComponent.getHeight();

                    Graphics2D Graphics2DContext = (Graphics2D) GraphicsContext.create();
                    Graphics2DContext.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    if (Model.isArmed() || Model.isSelected()) {
                        Graphics2DContext.setColor(SelectionBackgroundColor);
                        Graphics2DContext.fill(new RoundRectangle2D.Float(4, 2, Width - 8, Height - 4, 10, 10));
                    }
                    else {
                        Graphics2DContext.setColor(MenuItemComponent.getBackground());
                        Graphics2DContext.fillRect(0, 0, Width, Height);
                    }
                    Graphics2DContext.dispose();
                }

                @Override
                protected void paintText(Graphics GraphicsContext, JMenuItem MenuItemComponent, Rectangle TextRectangle, String TextValue) {
                    ButtonModel Model = MenuItemComponent.getModel();
                    if (Model.isArmed() || Model.isSelected()) {
                        GraphicsContext.setColor(SelectionForegroundColor);
                    }
                    else {
                        GraphicsContext.setColor(MenuItemComponent.getForeground());
                    }
                    FontMetrics FontMetricsContext = GraphicsContext.getFontMetrics();
                    int X = (MenuItemComponent.getWidth() - FontMetricsContext.stringWidth(TextValue)) / 2;
                    int Y = (MenuItemComponent.getHeight() - FontMetricsContext.getHeight()) / 2 + FontMetricsContext.getAscent();
                    GraphicsContext.drawString(TextValue, X, Y);
                }

                @Override
                public Dimension getPreferredSize(JComponent Component) {
                    Dimension Size = super.getPreferredSize(Component);
                    Size.width = Math.max(Size.width, 150);
                    return Size;
                }
            });

            MenuItem.addActionListener(ActionEventObject -> HandleMenuClick(Text));
            return MenuItem;
        }

        private void HandleMenuClick(String ActionCommand) {
            System.out.println("Action: '" + ActionCommand + "' for course: " + this.Name + " (" + this.Code + ")");

            try {
                CurrentOffering = FindOffering(this.OfferingID);
            }
            catch (SQLException ExceptionObject) {
                throw new RuntimeException(ExceptionObject);
            }

            switch (ActionCommand) {
                case "Grade Components":
                    DisplayGradeComponents();
                    break;
                case "Grade Slabs":
                    DisplayGradeSlabs();
                    break;
                case "View Stats":
                    DisplayStats();
                    break;
                case "Assign Grades":
                    AssignGrades();
                    break;
            }
        }

        private void DisplayGradeComponents() {
            GradeComponentsDialog ComponentsDialog = new GradeComponentsDialog(OwnerFrame, "Grade Components", this.Name, this.Code, CurrentOffering);
            ComponentsDialog.setVisible(true);
        }

        private void DisplayGradeSlabs() {
            GradeSlabsDialog SlabsDialog = new GradeSlabsDialog(OwnerFrame, "Grade Slabs", this.Name, this.Code, CurrentOffering);
            SlabsDialog.setVisible(true);
        }

        private void DisplayStats() {
            ViewStatsDialog StatsDialog = new ViewStatsDialog(OwnerFrame, this.Name, CurrentOffering);
            StatsDialog.setVisible(true);
        }

        private void AssignGrades() {
            AssignGradesDialog GradesDialog = new AssignGradesDialog(OwnerFrame, "Assign Grades", CurrentOffering);
            GradesDialog.setVisible(true);
        }

        private JPanel CreateInfoPanel(String CourseName, String CourseCode) {
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

        private JPanel CreateEnrollmentPanel(int EnrolledCount, int Capacity) {
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
        catch (Exception ExceptionObject) {
            System.err.println("Failed to initialize LaF");
        }

        SwingUtilities.invokeLater(() -> {
            JFrame Frame = new JFrame("Instructor Courses Page Debug");
            Frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Frame.setSize(1200, 800);
            Frame.getContentPane().setBackground(ColorBackground);

            JPanel MainHolderPanel = new JPanel(new BorderLayout());
            MainHolderPanel.setBackground(ColorBackground);
            MainHolderPanel.setBorder(new EmptyBorder(20, 25, 25, 25));
            Instructor DebugInstructor = new Instructor();
            DebugInstructor.SetInstructorID(1);
            MainHolderPanel.add(new MySections(1), BorderLayout.CENTER);

            Frame.add(MainHolderPanel);
            Frame.setLocationRelativeTo(null);
            Frame.setVisible(true);
        });
    }
}
