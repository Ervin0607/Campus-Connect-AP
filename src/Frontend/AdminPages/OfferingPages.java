package Frontend.AdminPages;

import Backend.DataBaseHandler.InstructorHandler;
import Backend.DataBaseHandler.OfferingHandler;
import Backend.DataBaseHandler.CourseHandler;
import Backend.domain.Instructor;
import Backend.domain.Offerings;
import Backend.domain.Course;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class OfferingPages extends JPanel {

    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel      = new Color(0x1E1E1E);
    private static final Color ColorAccent     = new Color(0xB0F2B4);
    private static final Color ColorText       = new Color(0xFFFFFF);
    private static final Color ColorTextDim    = new Color(0xB0B0B0);
    private static final Color ColorDivider    = new Color(0x2A2A2A);

    private final JComboBox<String> SemesterFilter = new JComboBox<>(new String[]{"All Semesters", "SPRING", "SUMMER", "FALL", "WINTER"});
    private final JComboBox<String> CourseFilter   = new JComboBox<>();
    private final JComboBox<String> YearFilter     = new JComboBox<>();
    private final JTextField SearchField           = new JTextField();

    private final JLabel TotalOfferingsLabel    = new JLabel("-");
    private final JLabel CurrentOfferingsLabel  = new JLabel("-");
    private final JLabel PreviousOfferingsLabel = new JLabel("-");
    private final JLabel TotalCapacityLabel     = new JLabel("-");
    private final JLabel TotalEnrolledLabel     = new JLabel("-");

    private final OfferingsTableModel TableModel = new OfferingsTableModel();
    private final JTable OfferingsTable          = new JTable(TableModel);
    private final JPanel DetailsPanel            = new JPanel(new BorderLayout());

    private List<Offerings> AllOfferings = new ArrayList<>();
    private List<Course> AllCourses      = new ArrayList<>();
    private List<Instructor> AllInstructors = new ArrayList<>();
    private int NextOfferingID           = 1;

    private static class TimeSlot {
        String Day;
        String StartTime;
        String EndTime;
        TimeSlot(String DayValue, String StartTimeValue, String EndTimeValue) {
            Day = DayValue;
            StartTime = StartTimeValue;
            EndTime = EndTimeValue;
        }
        @Override
        public String toString() {
            return Day + " " + StartTime + "-" + EndTime;
        }
    }

    public OfferingPages() {
        setOpaque(true);
        setBackground(ColorBackground);
        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        SeedNextOfferingID();
        LoadCoursesFromDatabase();
        LoadInstructorsFromDatabase();

        add(BuildTop(), BorderLayout.NORTH);
        add(BuildCenter(), BorderLayout.CENTER);

        WireFilters();
        RefreshData();
    }

    private void SeedNextOfferingID() {
        try {
            int MaxOfferingId = OfferingHandler.GetMaxOfferingID();
            NextOfferingID = Math.max(1, MaxOfferingId + 1);
        }
        catch (SQLException SqlException) {
            SqlException.printStackTrace();
            NextOfferingID = 1;
        }
    }

    private void ShowScheduleEditorDialog(JDialog ParentDialog, String DialogTitle, DefaultListModel<TimeSlot> TimeSlotListModel, JLabel SummaryLabel) {
        JDialog ScheduleDialog = new JDialog(ParentDialog, DialogTitle, true);
        ScheduleDialog.setSize(500, 400);
        ScheduleDialog.setLocationRelativeTo(ParentDialog);
        ScheduleDialog.setLayout(new BorderLayout(8, 8));
        ScheduleDialog.getContentPane().setBackground(ColorBackground);

        JPanel ControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        ControlsPanel.setOpaque(false);

        String[] DayOptions = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        JComboBox<String> DayComboBox = new JComboBox<>(DayOptions);

        String[] TimeOptions = {
                "08:00", "09:00", "10:00", "11:00", "12:00",
                "13:00", "14:00", "15:00", "16:00", "17:00",
                "18:00", "19:00", "20:00"
        };
        JComboBox<String> StartTimeComboBox = new JComboBox<>(TimeOptions);
        JComboBox<String> EndTimeComboBox   = new JComboBox<>(TimeOptions);

        JButton AddTimeSlotButton = new JButton("+");
        AddTimeSlotButton.setMargin(new Insets(2, 6, 2, 6));

        ControlsPanel.add(new JLabel("Day:"));
        ControlsPanel.add(DayComboBox);
        ControlsPanel.add(new JLabel("Start:"));
        ControlsPanel.add(StartTimeComboBox);
        ControlsPanel.add(new JLabel("End:"));
        ControlsPanel.add(EndTimeComboBox);
        ControlsPanel.add(AddTimeSlotButton);

        ScheduleDialog.add(ControlsPanel, BorderLayout.NORTH);

        JList<TimeSlot> TimeSlotList = new JList<>(TimeSlotListModel);
        TimeSlotList.setVisibleRowCount(6);
        JScrollPane TimeSlotScrollPane = new JScrollPane(TimeSlotList);
        TimeSlotList.setBackground(ColorPanel);
        TimeSlotList.setForeground(ColorText);
        TimeSlotScrollPane.getViewport().setBackground(ColorPanel);
        ScheduleDialog.add(TimeSlotScrollPane, BorderLayout.CENTER);

        JPanel ButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ButtonsPanel.setOpaque(false);
        JButton RemoveButton = new JButton("Remove selected");
        JButton OkButton     = new JButton("OK");
        JButton CancelButton = new JButton("Cancel");
        ButtonsPanel.add(RemoveButton);
        ButtonsPanel.add(CancelButton);
        ButtonsPanel.add(OkButton);
        ScheduleDialog.add(ButtonsPanel, BorderLayout.SOUTH);

        AddTimeSlotButton.addActionListener(Event -> {
            String SelectedDay = (String) DayComboBox.getSelectedItem();
            String SelectedStartTime = (String) StartTimeComboBox.getSelectedItem();
            String SelectedEndTime = (String) EndTimeComboBox.getSelectedItem();
            if (SelectedStartTime == null || SelectedEndTime == null) {
                JOptionPane.showMessageDialog(ScheduleDialog, "Please select start and end time.");
                return;
            }
            int StartIndex = StartTimeComboBox.getSelectedIndex();
            int EndIndex = EndTimeComboBox.getSelectedIndex();
            if (StartIndex >= EndIndex) {
                JOptionPane.showMessageDialog(ScheduleDialog, "End time must be after start time.");
                return;
            }
            TimeSlotListModel.addElement(new TimeSlot(SelectedDay, SelectedStartTime, SelectedEndTime));
        });

        RemoveButton.addActionListener(Event -> {
            int SelectedIndex = TimeSlotList.getSelectedIndex();
            if (SelectedIndex >= 0) {
                TimeSlotListModel.remove(SelectedIndex);
            }
        });

        CancelButton.addActionListener(Event -> ScheduleDialog.dispose());

        OkButton.addActionListener(Event -> {
            if (TimeSlotListModel.isEmpty()) {
                SummaryLabel.setText("No slots");
            }
            else {
                StringBuilder SummaryBuilder = new StringBuilder();
                for (int Index = 0; Index < TimeSlotListModel.size(); Index++) {
                    if (Index > 0) {
                        SummaryBuilder.append(", ");
                    }
                    SummaryBuilder.append(TimeSlotListModel.get(Index).toString());
                }
                SummaryLabel.setText(SummaryBuilder.toString());
            }
            ScheduleDialog.dispose();
        });

        ScheduleDialog.setVisible(true);
    }

    private String BuildScheduleJsonFromList(DefaultListModel<TimeSlot> TimeSlotListModel) {
        if (TimeSlotListModel == null || TimeSlotListModel.isEmpty()) return null;
        StringBuilder JsonBuilder = new StringBuilder();
        JsonBuilder.append("[");
        for (int Index = 0; Index < TimeSlotListModel.size(); Index++) {
            TimeSlot CurrentTimeSlot = TimeSlotListModel.get(Index);
            if (Index > 0) JsonBuilder.append(",");
            JsonBuilder.append("{\"day\":\"").append(CurrentTimeSlot.Day).append("\",")
                    .append("\"start\":\"").append(CurrentTimeSlot.StartTime).append("\",")
                    .append("\"end\":\"").append(CurrentTimeSlot.EndTime).append("\"}");
        }
        JsonBuilder.append("]");
        return JsonBuilder.toString();
    }

    private void LoadCoursesFromDatabase() {
        CourseFilter.removeAllItems();
        CourseFilter.addItem("All Courses");
        try {
            AllCourses = CourseHandler.GetAllCourses();
            for (Course CurrentCourse : AllCourses) {
                CourseFilter.addItem(CurrentCourse.GetCourseID() + " - " + CurrentCourse.GetCode() + " - " + CurrentCourse.GetTitle());
            }
        }
        catch (SQLException SqlException) {
            SqlException.printStackTrace();
            AllCourses = new ArrayList<>();
        }
    }

    private void LoadInstructorsFromDatabase() {
        try {
            AllInstructors = InstructorHandler.GetAllInstructors();
        }
        catch (SQLException SqlException) {
            SqlException.printStackTrace();
            AllInstructors = new ArrayList<>();
        }
    }

    private JComponent BuildTop() {
        JPanel RootPanel = new JPanel(new BorderLayout(12, 12));
        RootPanel.setOpaque(false);

        JPanel FilterPanel = new JPanel(new BorderLayout(10, 0));
        FilterPanel.setOpaque(false);

        JPanel RightControlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        RightControlsPanel.setOpaque(false);

        StyleCombo(SemesterFilter);
        StyleCombo(CourseFilter);
        StyleCombo(YearFilter);
        StyleField(SearchField);
        SearchField.setColumns(18);
        SearchField.putClientProperty("JTextField.placeholderText", "Search by OfferingID / CourseID");

        JButton AddOfferingButton = PrimaryButton("Add Offering");
        AddOfferingButton.addActionListener(Event -> OnAddOffering());

        int CurrentYear = LocalDate.now().getYear();
        YearFilter.addItem("All Years");
        for (int YearValue = CurrentYear; YearValue >= CurrentYear - 4; YearValue--) {
            YearFilter.addItem(String.valueOf(YearValue));
        }

        RightControlsPanel.add(new JLabel(Chip("Course")));
        RightControlsPanel.add(CourseFilter);
        RightControlsPanel.add(new JLabel(Chip("Semester")));
        RightControlsPanel.add(SemesterFilter);
        RightControlsPanel.add(new JLabel(Chip("Year")));
        RightControlsPanel.add(YearFilter);
        RightControlsPanel.add(SearchField);
        RightControlsPanel.add(AddOfferingButton);

        FilterPanel.add(RightControlsPanel, BorderLayout.EAST);
        RootPanel.add(FilterPanel, BorderLayout.NORTH);

        JPanel CardsAndChartsPanel = new JPanel(new GridBagLayout());
        CardsAndChartsPanel.setOpaque(false);
        GridBagConstraints LayoutConstraints = new GridBagConstraints();
        LayoutConstraints.insets = new Insets(8, 8, 8, 8);
        LayoutConstraints.fill = GridBagConstraints.BOTH;

        JPanel CardsRowPanel = new JPanel(new GridLayout(1, 5, 12, 12));
        CardsRowPanel.setOpaque(false);
        CardsRowPanel.add(StatCard("Total Offerings", TotalOfferingsLabel));
        CardsRowPanel.add(StatCard("Current Offerings", CurrentOfferingsLabel));
        CardsRowPanel.add(StatCard("Previous Offerings", PreviousOfferingsLabel));
        CardsRowPanel.add(StatCard("Total Capacity", TotalCapacityLabel));
        CardsRowPanel.add(StatCard("Total Enrolled", TotalEnrolledLabel));

        LayoutConstraints.gridx = 0;
        LayoutConstraints.gridy = 0;
        LayoutConstraints.weightx = 1;
        LayoutConstraints.weighty = 0.0;
        CardsAndChartsPanel.add(CardsRowPanel, LayoutConstraints);

        RootPanel.add(CardsAndChartsPanel, BorderLayout.CENTER);
        return RootPanel;
    }

    private JComponent BuildCenter() {
        JSplitPane ContentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        ContentSplitPane.setBackground(ColorBackground);
        ContentSplitPane.setBorder(null);
        ContentSplitPane.setResizeWeight(0.64);

        JPanel TableWrapperPanel = new JPanel(new BorderLayout());
        TableWrapperPanel.setOpaque(false);
        ConfigureTable();
        JScrollPane OfferingsScrollPane = new JScrollPane(OfferingsTable);
        OfferingsScrollPane.getViewport().setBackground(ColorBackground);
        OfferingsScrollPane.setBorder(BorderFactory.createLineBorder(ColorDivider));
        TableWrapperPanel.add(WrapModule("All Offerings", OfferingsScrollPane), BorderLayout.CENTER);

        DetailsPanel.setOpaque(false);
        DetailsPanel.add(WrapModule("Details", DetailsPlaceholder()), BorderLayout.CENTER);

        ContentSplitPane.setLeftComponent(TableWrapperPanel);
        ContentSplitPane.setRightComponent(DetailsPanel);
        return ContentSplitPane;
    }

    private JPanel StatCard(String TitleText, JLabel ValueLabel) {
        JPanel CardPanel = RoundedPanel();
        CardPanel.setBackground(ColorPanel);
        CardPanel.setLayout(new BorderLayout());
        CardPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel TitleLabel = new JLabel(TitleText);
        TitleLabel.setForeground(ColorTextDim);
        TitleLabel.setFont(TitleLabel.getFont().deriveFont(Font.PLAIN, 13f));

        ValueLabel.setForeground(ColorText);
        ValueLabel.setFont(ValueLabel.getFont().deriveFont(Font.BOLD, 20f));

        CardPanel.add(TitleLabel, BorderLayout.NORTH);
        CardPanel.add(ValueLabel, BorderLayout.CENTER);
        return CardPanel;
    }

    private JPanel WrapModule(String TitleText, JComponent ContentComponent) {
        JPanel ModulePanel = RoundedPanel();
        ModulePanel.setBackground(ColorPanel);
        ModulePanel.setLayout(new BorderLayout());
        ModulePanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel TitleLabel = new JLabel(TitleText);
        TitleLabel.setForeground(ColorText);
        TitleLabel.setFont(TitleLabel.getFont().deriveFont(Font.BOLD, 16f));
        ModulePanel.add(TitleLabel, BorderLayout.NORTH);
        ModulePanel.add(ContentComponent, BorderLayout.CENTER);
        return ModulePanel;
    }

    private JPanel RoundedPanel() {
        return new JPanel() {
            @Override
            public boolean isOpaque() {
                return false;
            }
            @Override
            protected void paintComponent(Graphics GraphicsInstance) {
                Graphics2D Graphics2DInstance = (Graphics2D) GraphicsInstance.create();
                Graphics2DInstance.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Graphics2DInstance.setColor(getBackground());
                Graphics2DInstance.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                Graphics2DInstance.dispose();
                super.paintComponent(GraphicsInstance);
            }
        };
    }

    private String Chip(String TextValue) {
        return " " + TextValue + " ";
    }

    private void StyleCombo(JComboBox<?> ComboBoxInstance) {
        ComboBoxInstance.setBackground(ColorPanel);
        ComboBoxInstance.setForeground(ColorText);
        ComboBoxInstance.setBorder(BorderFactory.createLineBorder(ColorDivider));
    }

    private void StyleField(JTextField TextFieldInstance) {
        TextFieldInstance.setBackground(ColorPanel);
        TextFieldInstance.setForeground(ColorText);
        TextFieldInstance.setCaretColor(ColorText);
        TextFieldInstance.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ColorDivider), new EmptyBorder(6, 8, 6, 8)));
    }

    private JButton PrimaryButton(String ButtonText) {
        JButton ButtonInstance = new JButton(ButtonText);
        ButtonInstance.setBackground(ColorAccent);
        ButtonInstance.setForeground(Color.BLACK);
        ButtonInstance.setFocusPainted(false);
        ButtonInstance.setBorder(new EmptyBorder(8, 14, 8, 14));
        return ButtonInstance;
    }

    private void ConfigureTable() {
        OfferingsTable.setFillsViewportHeight(true);
        OfferingsTable.setRowHeight(32);
        OfferingsTable.setBackground(ColorBackground);
        OfferingsTable.setForeground(ColorText);
        OfferingsTable.setGridColor(ColorDivider);
        OfferingsTable.setSelectionBackground(new Color(0x2A2A2A));
        OfferingsTable.setSelectionForeground(ColorText);
        OfferingsTable.setAutoCreateRowSorter(true);

        JTableHeader TableHeader = OfferingsTable.getTableHeader();
        TableHeader.setBackground(ColorPanel);
        TableHeader.setForeground(ColorTextDim);
        TableHeader.setReorderingAllowed(false);

        if (OfferingsTable.getColumnModel().getColumnCount() >= 9) {
            OfferingsTable.getColumnModel().getColumn(7).setCellRenderer(new ButtonCellRenderer("Edit"));
            OfferingsTable.getColumnModel().getColumn(7).setCellEditor(new ButtonCellEditor("Edit", this::OnEditOffering));
            OfferingsTable.getColumnModel().getColumn(8).setCellRenderer(new ButtonCellRenderer("Delete"));
            OfferingsTable.getColumnModel().getColumn(8).setCellEditor(new ButtonCellEditor("Delete", this::OnDeleteOffering));
        }

        OfferingsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent MouseEventInstance) {
                if (MouseEventInstance.getClickCount() == 2) {
                    int ViewRowIndex = OfferingsTable.getSelectedRow();
                    if (ViewRowIndex >= 0) {
                        int ModelRowIndex = OfferingsTable.convertRowIndexToModel(ViewRowIndex);
                        ShowDetails(TableModel.GetRow(ModelRowIndex));
                    }
                }
            }
        });
    }

    private JComponent DetailsPlaceholder() {
        JPanel PlaceholderPanel = new JPanel(new GridBagLayout());
        PlaceholderPanel.setOpaque(false);
        JLabel PlaceholderLabel = new JLabel("Double-click an offering to view details");
        PlaceholderLabel.setForeground(ColorTextDim);
        PlaceholderLabel.setFont(PlaceholderLabel.getFont().deriveFont(Font.PLAIN, 16f));
        PlaceholderPanel.add(PlaceholderLabel);
        return PlaceholderPanel;
    }

    private void ShowDetails(Offerings SelectedOffering) {
        JPanel DetailsInfoPanel = new JPanel();
        DetailsInfoPanel.setOpaque(false);
        DetailsInfoPanel.setLayout(new BoxLayout(DetailsInfoPanel, BoxLayout.Y_AXIS));
        DetailsInfoPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        DetailsInfoPanel.add(Detail("OfferingID", String.valueOf(SelectedOffering.GetOfferingID())));
        DetailsInfoPanel.add(Detail("Course", GetCourseLabel(SelectedOffering.GetCourseID())));
        DetailsInfoPanel.add(Detail("Semester", Safe(SelectedOffering.GetSemester())));
        DetailsInfoPanel.add(Detail("Year", String.valueOf(SelectedOffering.GetYear())));
        DetailsInfoPanel.add(Detail("Capacity", String.valueOf(SelectedOffering.GetCapacity())));
        DetailsInfoPanel.add(Detail("Current Enrollment", String.valueOf(SelectedOffering.GetCurrentEnrollment())));

        String InstructorName = "-";
        try {
            Instructor InstructorInstance = InstructorHandler.FindInstructorByInstructorID(SelectedOffering.GetInstructorID());
            if (InstructorInstance != null) {
                InstructorName = Safe(InstructorInstance.GetName());
            }
        }
        catch (SQLException SqlException) {
            SqlException.printStackTrace();
        }

        DetailsInfoPanel.add(Spacer());
        DetailsInfoPanel.add(Separator());
        DetailsInfoPanel.add(Section("Instructor"));
        DetailsInfoPanel.add(Detail("Instructor ID", String.valueOf(SelectedOffering.GetInstructorID())));
        DetailsInfoPanel.add(Detail("Instructor Name", InstructorName));

        DetailsPanel.removeAll();
        DetailsPanel.add(WrapModule("Details", new JScrollPane(DetailsInfoPanel)), BorderLayout.CENTER);
        DetailsPanel.revalidate();
        DetailsPanel.repaint();
    }

    private String GetCourseLabel(int CourseId) {
        try {
            Course CourseInstance = CourseHandler.FindCourseByID(CourseId);
            if (CourseInstance != null) {
                return CourseId + " - " + Safe(CourseInstance.GetCode()) + " - " + Safe(CourseInstance.GetTitle());
            }
        }
        catch (SQLException SqlException) {
            SqlException.printStackTrace();
        }
        return String.valueOf(CourseId);
    }

    private JComponent Section(String SectionTitle) {
        JLabel SectionLabel = new JLabel(SectionTitle);
        SectionLabel.setForeground(ColorText);
        SectionLabel.setFont(SectionLabel.getFont().deriveFont(Font.BOLD, 15f));
        JPanel SectionPanel = new JPanel(new BorderLayout());
        SectionPanel.setOpaque(false);
        SectionPanel.add(SectionLabel, BorderLayout.CENTER);
        SectionPanel.setBorder(new EmptyBorder(8, 0, 4, 0));
        return SectionPanel;
    }

    private JComponent Detail(String DetailKey, String DetailValue) {
        JPanel DetailRowPanel = new JPanel(new BorderLayout());
        DetailRowPanel.setOpaque(false);
        JLabel KeyLabel = new JLabel(DetailKey);
        KeyLabel.setForeground(ColorTextDim);
        JLabel ValueLabel = new JLabel(DetailValue);
        ValueLabel.setForeground(ColorText);
        DetailRowPanel.add(KeyLabel, BorderLayout.WEST);
        DetailRowPanel.add(ValueLabel, BorderLayout.EAST);
        DetailRowPanel.setBorder(new EmptyBorder(3, 0, 3, 0));
        return DetailRowPanel;
    }

    private JComponent Separator() {
        JSeparator SeparatorInstance = new JSeparator(SwingConstants.HORIZONTAL);
        SeparatorInstance.setForeground(ColorDivider);
        SeparatorInstance.setBackground(ColorDivider);
        return SeparatorInstance;
    }

    private JComponent Spacer() {
        JPanel SpacerPanel = new JPanel();
        SpacerPanel.setOpaque(false);
        SpacerPanel.setPreferredSize(new Dimension(1, 6));
        return SpacerPanel;
    }

    private String Safe(Object Value) {
        return (Value == null) ? "-" : String.valueOf(Value);
    }

    private void WireFilters() {
        SemesterFilter.addActionListener(Event -> ApplyFilters());
        CourseFilter.addActionListener(Event -> ApplyFilters());
        YearFilter.addActionListener(Event -> ApplyFilters());
        SearchField.addActionListener(Event -> ApplyFilters());
    }

    private void ApplyFilters() {
        String SelectedSemester = (String) SemesterFilter.getSelectedItem();
        String SelectedCourseFilterLabel = (String) CourseFilter.getSelectedItem();
        String SelectedYearFilterLabel = (String) YearFilter.getSelectedItem();
        String QueryString = SearchField.getText().trim().toLowerCase();

        List<Offerings> FilteredOfferings = AllOfferings.stream()
                .filter(OfferingInstance -> "All Semesters".equals(SelectedSemester)
                        || Safe(OfferingInstance.GetSemester()).equalsIgnoreCase(SelectedSemester))
                .filter(OfferingInstance -> {
                    if (SelectedCourseFilterLabel == null || "All Courses".equals(SelectedCourseFilterLabel)) return true;
                    try {
                        int SelectedCourseId = Integer.parseInt(SelectedCourseFilterLabel.split(" - ")[0].trim());
                        return OfferingInstance.GetCourseID() == SelectedCourseId;
                    }
                    catch (Exception ExceptionObject) {
                        return true;
                    }
                })
                .filter(OfferingInstance -> "All Years".equals(SelectedYearFilterLabel)
                        || String.valueOf(OfferingInstance.GetYear()).equals(SelectedYearFilterLabel))
                .filter(OfferingInstance -> QueryString.isBlank()
                        || String.valueOf(OfferingInstance.GetOfferingID()).contains(QueryString)
                        || String.valueOf(OfferingInstance.GetCourseID()).contains(QueryString))
                .sorted(Comparator.comparingInt(Offerings::GetOfferingID))
                .collect(Collectors.toList());

        TableModel.SetRows(FilteredOfferings);

        int TotalCount = FilteredOfferings.size();
        int CurrentYear = LocalDate.now().getYear();
        int CurrentCount = (int) FilteredOfferings.stream().filter(OfferingInstance -> OfferingInstance.GetYear() >= CurrentYear).count();
        int PreviousCount = TotalCount - CurrentCount;
        int TotalCapacity = FilteredOfferings.stream().mapToInt(Offerings::GetCapacity).sum();
        int TotalEnrolled = FilteredOfferings.stream().mapToInt(Offerings::GetCurrentEnrollment).sum();

        TotalOfferingsLabel.setText(String.valueOf(TotalCount));
        CurrentOfferingsLabel.setText(String.valueOf(CurrentCount));
        PreviousOfferingsLabel.setText(String.valueOf(PreviousCount));
        TotalCapacityLabel.setText(String.valueOf(TotalCapacity));
        TotalEnrolledLabel.setText(String.valueOf(TotalEnrolled));
    }

    private void OnEditOffering(int ModelRowIndex) {
        Offerings SelectedOffering = TableModel.GetRow(ModelRowIndex);

        JPanel FormPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        FormPanel.setOpaque(false);
        JTextField CapacityField = new JTextField(String.valueOf(SelectedOffering.GetCapacity()));
        JTextField EnrolledField = new JTextField(String.valueOf(SelectedOffering.GetCurrentEnrollment()));
        JTextField YearField = new JTextField(String.valueOf(SelectedOffering.GetYear()));
        JTextField SemesterField = new JTextField(SelectedOffering.GetSemester());


        LoadInstructorsFromDatabase();

        JComboBox<String> InstructorComboBox = new JComboBox<>();
        for (Instructor Inst : AllInstructors) {
            InstructorComboBox.addItem(Inst.GetInstructorID() + " - " + Inst.GetName());
        }
        int CurrentInstructorId = SelectedOffering.GetInstructorID();
        for (int i = 0; i < InstructorComboBox.getItemCount(); i++) {
            String Label = (String) InstructorComboBox.getItemAt(i);
            try {
                int ParsedId = Integer.parseInt(Label.split(" - ")[0].trim());
                if (ParsedId == CurrentInstructorId) {
                    InstructorComboBox.setSelectedIndex(i);
                    break;
                }
            }
            catch (Exception ignored) {
            }
        }

        FormPanel.add(new JLabel("Semester:"));
        FormPanel.add(SemesterField);
        FormPanel.add(new JLabel("Year:"));
        FormPanel.add(YearField);
        FormPanel.add(new JLabel("Capacity:"));
        FormPanel.add(CapacityField);
        FormPanel.add(new JLabel("Current Enrollment:"));
        FormPanel.add(EnrolledField);
        FormPanel.add(new JLabel("Instructor:"));
        FormPanel.add(InstructorComboBox);

        for (Component ComponentInstance : FormPanel.getComponents()) {
            if (ComponentInstance instanceof JLabel) {
                ((JLabel) ComponentInstance).setForeground(ColorText);
            }
        }

        int DialogResult = JOptionPane.showConfirmDialog(this, FormPanel, "Edit Offering " + SelectedOffering.GetOfferingID(), JOptionPane.OK_CANCEL_OPTION);
        if (DialogResult != JOptionPane.OK_OPTION) return;

        try {
            int NewCapacity = Integer.parseInt(CapacityField.getText().trim());
            int NewEnrollment = Integer.parseInt(EnrolledField.getText().trim());
            int NewYear = Integer.parseInt(YearField.getText().trim());
            String NewSemester = SemesterField.getText().trim();

            String SelectedInstructorLabel = (String) InstructorComboBox.getSelectedItem();
            if (SelectedInstructorLabel == null || SelectedInstructorLabel.isBlank()) {
                JOptionPane.showMessageDialog(this, "Please select an instructor.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int NewInstructorId;
            try {
                NewInstructorId = Integer.parseInt(SelectedInstructorLabel.split(" - ")[0].trim());
            }
            catch (Exception ParseException) {
                JOptionPane.showMessageDialog(this, "Unable to parse instructor ID from selection: " + SelectedInstructorLabel, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (NewYear < 0 || NewCapacity <= 0 || NewEnrollment < 0 || NewInstructorId < 0) {
                JOptionPane.showMessageDialog(this, "Year and enrollment cannot be negative. Capacity must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Offerings UpdatedOffering = new Offerings();
            UpdatedOffering.SetOfferingID(SelectedOffering.GetOfferingID());
            UpdatedOffering.SetCourseID(SelectedOffering.GetCourseID());
            UpdatedOffering.SetInstructorID(NewInstructorId);
            UpdatedOffering.SetSemester(NewSemester);
            UpdatedOffering.SetYear(NewYear);
            UpdatedOffering.SetCapacity(NewCapacity);
            UpdatedOffering.SetCurrentEnrollment(NewEnrollment);

            boolean IsUpdateSuccessful = new OfferingHandler().UpdateOffering(UpdatedOffering);
            if (!IsUpdateSuccessful) {
                JOptionPane.showMessageDialog(this, "Update returned false.", "Update", JOptionPane.WARNING_MESSAGE);
            }
            else {
                RefreshData();
            }
        }
        catch (NumberFormatException NumberFormatExceptionObject) {
            JOptionPane.showMessageDialog(this, "Numeric fields must be numbers.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (SQLException SqlException) {
            showError(SqlException);
        }
        catch (Exception ExceptionObject) {
            ExceptionObject.printStackTrace();
            RefreshData();
        }
    }

    private void OnDeleteOffering(int ModelRowIndex) {
        Offerings SelectedOffering = TableModel.GetRow(ModelRowIndex);

        if (SelectedOffering.GetCurrentEnrollment() > 0) {
            JOptionPane.showMessageDialog(this, "Cannot delete this offering because there are " + SelectedOffering.GetCurrentEnrollment() + " students enrolled.", "Delete not allowed", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ConfirmResult = JOptionPane.showConfirmDialog(this, "Delete offering ID " + SelectedOffering.GetOfferingID() + " (Course " + GetCourseLabel(SelectedOffering.GetCourseID()) + ")?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (ConfirmResult != JOptionPane.YES_OPTION) return;

        try {
            boolean IsDeleteSuccessful = new OfferingHandler().DeleteOffering(SelectedOffering.GetOfferingID());
            if (!IsDeleteSuccessful) {
                JOptionPane.showMessageDialog(this, "Delete failed.", "Delete", JOptionPane.WARNING_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(this, "Offering deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            }
            RefreshData();
        }
        catch (SQLException SqlException) {
            showError(SqlException);
        }
        catch (Exception ExceptionObject) {
            ExceptionObject.printStackTrace();
            RefreshData();
        }
    }

    private void OnAddOffering() {
        final JDialog AddOfferingDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Create Offering",
                true
        );
        AddOfferingDialog.setSize(700, 500);
        AddOfferingDialog.setLocationRelativeTo(this);
        AddOfferingDialog.setLayout(new BorderLayout(10, 10));
        AddOfferingDialog.getContentPane().setBackground(ColorBackground);

        JPanel FormPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        FormPanel.setOpaque(false);
        FormPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        try {
            AllCourses = CourseHandler.GetAllCourses();
            for (Course CurrentCourse : AllCourses) {
                CourseFilter.addItem(CurrentCourse.GetCourseID() + " - " + CurrentCourse.GetCode() + " - " + CurrentCourse.GetTitle());
            }
        }
        catch (SQLException SqlException) {
            SqlException.printStackTrace();
            AllCourses = new ArrayList<>();
        }

        JComboBox<String> CourseComboBox = new JComboBox<>();
        for (Course CurrentCourse : AllCourses) {
            CourseComboBox.addItem(CurrentCourse.GetCourseID() + " - " + CurrentCourse.GetCode() + " - " + CurrentCourse.GetTitle());
        }

        JComboBox<String> SemesterComboBox = new JComboBox<>(new String[]{"SPRING", "SUMMER", "FALL", "WINTER"});
        JTextField YearField = new JTextField(String.valueOf(LocalDate.now().getYear()));
        JTextField CapacityField = new JTextField("30");
        JTextField EnrollmentField = new JTextField("0");


        LoadInstructorsFromDatabase();

        JComboBox<String> InstructorComboBox = new JComboBox<>();
        for (Instructor Inst : AllInstructors) {
            InstructorComboBox.addItem(Inst.GetInstructorID() + " - " + Inst.GetName());
        }

        DefaultListModel<TimeSlot> LectureTimeSlotModel = new DefaultListModel<>();
        DefaultListModel<TimeSlot> LabTimeSlotModel     = new DefaultListModel<>();

        JButton LectureScheduleButton = new JButton("Configure...");
        JLabel LectureScheduleSummaryLabel = new JLabel("No slots");
        LectureScheduleSummaryLabel.setForeground(ColorTextDim);
        JPanel LectureSchedulePanel = new JPanel(new BorderLayout(4, 4));
        LectureSchedulePanel.setOpaque(false);
        LectureSchedulePanel.add(LectureScheduleButton, BorderLayout.WEST);
        LectureSchedulePanel.add(LectureScheduleSummaryLabel, BorderLayout.CENTER);

        JButton LabScheduleButton = new JButton("Configure...");
        JLabel LabScheduleSummaryLabel = new JLabel("No slots");
        LabScheduleSummaryLabel.setForeground(ColorTextDim);
        JPanel LabSchedulePanel = new JPanel(new BorderLayout(4, 4));
        LabSchedulePanel.setOpaque(false);
        LabSchedulePanel.add(LabScheduleButton, BorderLayout.WEST);
        LabSchedulePanel.add(LabScheduleSummaryLabel, BorderLayout.CENTER);

        LectureScheduleButton.addActionListener(Event ->
                ShowScheduleEditorDialog(AddOfferingDialog, "Lecture Schedule", LectureTimeSlotModel, LectureScheduleSummaryLabel)
        );
        LabScheduleButton.addActionListener(Event ->
                ShowScheduleEditorDialog(AddOfferingDialog, "Lab Schedule", LabTimeSlotModel, LabScheduleSummaryLabel)
        );

        FormPanel.add(new JLabel("Course:"));
        FormPanel.add(CourseComboBox);
        FormPanel.add(new JLabel("Semester:"));
        FormPanel.add(SemesterComboBox);
        FormPanel.add(new JLabel("Year:"));
        FormPanel.add(YearField);
        FormPanel.add(new JLabel("Capacity:"));
        FormPanel.add(CapacityField);
        FormPanel.add(new JLabel("Current Enrollment:"));
        FormPanel.add(EnrollmentField);
        FormPanel.add(new JLabel("Instructor:"));
        FormPanel.add(InstructorComboBox);
        FormPanel.add(new JLabel("Lecture Schedule:"));
        FormPanel.add(LectureSchedulePanel);
        FormPanel.add(new JLabel("Lab Schedule:"));
        FormPanel.add(LabSchedulePanel);

        for (Component ComponentInstance : FormPanel.getComponents()) {
            if (ComponentInstance instanceof JLabel) {
                ((JLabel) ComponentInstance).setForeground(ColorText);
            }
        }

        JPanel ButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ButtonsPanel.setOpaque(false);
        JButton CancelButton = new JButton("Cancel");
        JButton SaveButton   = PrimaryButton("Create");
        ButtonsPanel.add(CancelButton);
        ButtonsPanel.add(SaveButton);

        AddOfferingDialog.add(FormPanel, BorderLayout.CENTER);
        AddOfferingDialog.add(ButtonsPanel, BorderLayout.SOUTH);

        CancelButton.addActionListener(Event -> AddOfferingDialog.dispose());
        SaveButton.addActionListener(Event -> {
            try {
                String SelectedCourseLabel = (String) CourseComboBox.getSelectedItem();
                if (SelectedCourseLabel == null || SelectedCourseLabel.isBlank()) {
                    JOptionPane.showMessageDialog(AddOfferingDialog, "Please select a course.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int CourseId;
                try {
                    CourseId = Integer.parseInt(SelectedCourseLabel.split(" - ")[0].trim());
                }
                catch (Exception ParseException) {
                    JOptionPane.showMessageDialog(AddOfferingDialog, "Unable to parse course ID from selection: " + SelectedCourseLabel, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String SelectedInstructorLabel = (String) InstructorComboBox.getSelectedItem();
                if (SelectedInstructorLabel == null || SelectedInstructorLabel.isBlank()) {
                    JOptionPane.showMessageDialog(AddOfferingDialog, "Please select an instructor.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int InstructorId;
                try {
                    InstructorId = Integer.parseInt(SelectedInstructorLabel.split(" - ")[0].trim());
                }
                catch (Exception ParseException) {
                    JOptionPane.showMessageDialog(AddOfferingDialog, "Unable to parse instructor ID from selection: " + SelectedInstructorLabel, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String SelectedSemester = (String) SemesterComboBox.getSelectedItem();
                int SelectedYear = Integer.parseInt(YearField.getText().trim());
                int CapacityValue = Integer.parseInt(CapacityField.getText().trim());
                int EnrollmentValue = Integer.parseInt(EnrollmentField.getText().trim());

                if (SelectedYear < 0 || CapacityValue <= 0 || EnrollmentValue < 0 || InstructorId < 0) {
                    JOptionPane.showMessageDialog(AddOfferingDialog, "Year and enrollment cannot be negative. Capacity must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String LectureScheduleJson = BuildScheduleJsonFromList(LectureTimeSlotModel);
                String LabScheduleJson     = BuildScheduleJsonFromList(LabTimeSlotModel);

                Offerings NewOffering = new Offerings();
                NewOffering.SetOfferingID(NextOfferingID);
                NewOffering.SetCourseID(CourseId);
                NewOffering.SetInstructorID(InstructorId);
                NewOffering.SetSemester(SelectedSemester);
                NewOffering.SetYear(SelectedYear);
                NewOffering.SetCapacity(CapacityValue);
                NewOffering.SetCurrentEnrollment(EnrollmentValue);
                NewOffering.SetLectureSchedule(LectureScheduleJson);
                NewOffering.SetLabSchedule(LabScheduleJson);

                boolean IsInsertSuccessful = OfferingHandler.AddOfferings(NewOffering);
                if (IsInsertSuccessful) {
                    NextOfferingID++;
                    AddOfferingDialog.dispose();
                    RefreshData();
                    JOptionPane.showMessageDialog(this, "Offering created.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Create failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            catch (NumberFormatException NumberFormatExceptionObject) {
                JOptionPane.showMessageDialog(AddOfferingDialog, "Numeric fields must be numbers.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            catch (SQLException SqlException) {
                showError(SqlException);
            }
            catch (Exception ExceptionObject) {
                ExceptionObject.printStackTrace();
                JOptionPane.showMessageDialog(AddOfferingDialog, "Create failed: " + ExceptionObject.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        AddOfferingDialog.setVisible(true);
    }

    private void showError(Exception ExceptionObject) {
        ExceptionObject.printStackTrace();
        JOptionPane.showMessageDialog(this, ExceptionObject.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    public void RefreshData() {
        try {
            AllOfferings = LoadAllOfferings();
        } catch (SQLException SqlException) {
            showError(SqlException);
            AllOfferings = new ArrayList<>();
        }
        ApplyFilters();
    }

    private List<Offerings> LoadAllOfferings() throws SQLException {
        List<Offerings> OfferingsList = new OfferingHandler().GetAllOfferings();
        OfferingsList.sort(Comparator.comparingInt(Offerings::GetOfferingID));
        return OfferingsList;
    }

    private class OfferingsTableModel extends AbstractTableModel {
        private final String[] ColumnNames = {"OfferingID", "Course", "Semester", "Year", "Capacity", "Enrolled", "Instructor", "Edit", "Delete"};
        private List<Offerings> OfferingRows = new ArrayList<>();

        public void SetRows(List<Offerings> NewRows) {
            this.OfferingRows = new ArrayList<>(NewRows);
            fireTableDataChanged();
        }

        public Offerings GetRow(int ModelIndex) {
            return OfferingRows.get(ModelIndex);
        }

        @Override
        public int getRowCount() {
            return OfferingRows.size();
        }

        @Override
        public int getColumnCount() {
            return ColumnNames.length;
        }

        @Override
        public String getColumnName(int ColumnIndex) {
            return ColumnNames[ColumnIndex];
        }

        @Override
        public boolean isCellEditable(int RowIndex, int ColumnIndex) {
            return ColumnIndex >= 7;
        }

        @Override
        public Object getValueAt(int RowIndex, int ColumnIndex) {
            Offerings CurrentOffering = OfferingRows.get(RowIndex);
            switch (ColumnIndex) {
                case 0:
                    return CurrentOffering.GetOfferingID();
                case 1:
                    return GetCourseLabel(CurrentOffering.GetCourseID());
                case 2:
                    return CurrentOffering.GetSemester();
                case 3:
                    return CurrentOffering.GetYear();
                case 4:
                    return CurrentOffering.GetCapacity();
                case 5:
                    return CurrentOffering.GetCurrentEnrollment();
                case 6:
                    return CurrentOffering.GetInstructorID();
                case 7:
                    return "Edit";
                case 8:
                    return "Delete";
                default:
                    return "";
            }
        }
    }

    private static class ButtonCellRenderer extends JButton implements TableCellRenderer {
        public ButtonCellRenderer(String ButtonLabel) {
            super(ButtonLabel);
            setFocusPainted(false);
        }
        @Override
        public Component getTableCellRendererComponent(JTable TableInstance, Object CellValue, boolean IsSelected,
                                                       boolean HasFocus, int RowIndex, int ColumnIndex) {
            setText(CellValue == null ? "" : CellValue.toString());
            return this;
        }
    }

    private static class ButtonCellEditor extends DefaultCellEditor {
        private final JButton ButtonInstance = new JButton();
        private final java.util.function.IntConsumer RowHandler;
        private int ModelRowIndex = -1;

        public ButtonCellEditor(String ButtonLabel, java.util.function.IntConsumer OnClickHandler) {
            super(new JTextField());
            this.RowHandler = OnClickHandler;
            ButtonInstance.setText(ButtonLabel);
            ButtonInstance.setFocusPainted(false);

            ButtonInstance.addActionListener(Event -> {
                final int ClickedRowIndex = ModelRowIndex;
                fireEditingStopped();
                SwingUtilities.invokeLater(() -> {
                    try {
                        if (ClickedRowIndex >= 0) {
                            RowHandler.accept(ClickedRowIndex);
                        }
                    } catch (Exception ExceptionObject) {
                        ExceptionObject.printStackTrace();
                    }
                });
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable TableInstance, Object CellValue, boolean IsSelected, int ViewRowIndex, int ColumnIndex) {
            this.ModelRowIndex = TableInstance.convertRowIndexToModel(ViewRowIndex);
            ButtonInstance.setText(CellValue == null ? "" : CellValue.toString());
            return ButtonInstance;
        }

        @Override
        public Object getCellEditorValue() {
            return ButtonInstance.getText();
        }
    }
}