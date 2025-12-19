package Frontend.AdminPages;

import Backend.DataBaseHandler.CourseHandler;
import Backend.domain.Course;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CoursePages extends JPanel {

    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel      = new Color(0x1E1E1E);
    private static final Color ColorAccent     = new Color(0xB0F2B4);
    private static final Color ColorText       = new Color(0xFFFFFF);
    private static final Color ColorTextDim    = new Color(0xB0B0B0);
    private static final Color ColorDivider    = new Color(0x2A2A2A);

    private final JComboBox<String> DeptFilter = new JComboBox<>(new String[]{
            "All Depts", "CSE", "ECE", "MTH", "OTHER"
    });
    private final JTextField SearchField       = new JTextField();

    private final CoursesTableModel TableModel = new CoursesTableModel();
    private final JTable CoursesTable          = new JTable(TableModel);

    private List<Course> AllCourses = new ArrayList<>();

    public interface OpenCourseListener {
        void open(Course CourseInstance);
    }

    private OpenCourseListener OpenCourseListenerInstance;
    public void setOpenCourseListener(OpenCourseListener ListenerInstance) {
        OpenCourseListenerInstance = ListenerInstance;
    }

    public CoursePages() {
        setOpaque(true);
        setBackground(ColorBackground);
        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        add(BuildTop(), BorderLayout.NORTH);
        add(BuildCenter(), BorderLayout.CENTER);

        WireFilters();
        RefreshData();
    }

    private JComponent BuildTop() {
        JPanel RootPanel = new JPanel(new BorderLayout(12, 12));
        RootPanel.setOpaque(false);

        JPanel TopBarPanel = new JPanel(new BorderLayout(10, 0));
        TopBarPanel.setOpaque(false);

        JPanel RightControlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        RightControlsPanel.setOpaque(false);

        StyleCombo(DeptFilter);

        StyleField(SearchField);
        SearchField.setColumns(18);
        SearchField.putClientProperty("JTextField.placeholderText", "Search by ID / Code / Title");

        JButton AddCourseButton = PrimaryButton("Add Course");
        AddCourseButton.addActionListener(Event -> OnAddCourse());

        RightControlsPanel.add(new JLabel(Chip("Dept")));
        RightControlsPanel.add(DeptFilter);
        RightControlsPanel.add(SearchField);
        RightControlsPanel.add(AddCourseButton);

        TopBarPanel.add(RightControlsPanel, BorderLayout.EAST);
        RootPanel.add(TopBarPanel, BorderLayout.NORTH);

        JPanel TableWrapperPanel = new JPanel(new BorderLayout());
        TableWrapperPanel.setOpaque(false);
        ConfigureTable();
        JScrollPane CourseScrollPane = new JScrollPane(CoursesTable);
        CourseScrollPane.getViewport().setBackground(ColorBackground);
        CourseScrollPane.setBorder(BorderFactory.createLineBorder(ColorDivider));
        TableWrapperPanel.add(WrapModule("All Courses", CourseScrollPane), BorderLayout.CENTER);

        RootPanel.add(TableWrapperPanel, BorderLayout.CENTER);
        return RootPanel;
    }

    private JComponent BuildCenter() {
        JPanel CenterPanel = new JPanel(new BorderLayout());
        CenterPanel.setOpaque(false);
        return CenterPanel;
    }

    private JPanel WrapModule(String Title, JComponent ContentComponent) {
        JPanel ModulePanel = RoundedPanel();
        ModulePanel.setBackground(ColorPanel);
        ModulePanel.setLayout(new BorderLayout());
        ModulePanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel TitleLabel = new JLabel(Title);
        TitleLabel.setForeground(ColorText);
        TitleLabel.setFont(TitleLabel.getFont().deriveFont(Font.BOLD, 16f));
        ModulePanel.add(TitleLabel, BorderLayout.NORTH);
        ModulePanel.add(ContentComponent, BorderLayout.CENTER);
        return ModulePanel;
    }

    private JPanel RoundedPanel() {
        return new JPanel() {
            @Override
            public boolean isOpaque() { return false; }
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

    private String Chip(String Text) {
        return " " + Text + " ";
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
        TextFieldInstance.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorDivider),
                new EmptyBorder(6, 8, 6, 8)
        ));
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
        CoursesTable.setFillsViewportHeight(true);
        CoursesTable.setRowHeight(32);
        CoursesTable.setBackground(ColorBackground);
        CoursesTable.setForeground(ColorText);
        CoursesTable.setGridColor(ColorDivider);
        CoursesTable.setSelectionBackground(new Color(0x2A2A2A));
        CoursesTable.setSelectionForeground(ColorText);
        CoursesTable.setAutoCreateRowSorter(true);

        JTableHeader TableHeader = CoursesTable.getTableHeader();
        TableHeader.setBackground(ColorPanel);
        TableHeader.setForeground(ColorTextDim);
        TableHeader.setReorderingAllowed(false);

        CoursesTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonCellRenderer("Edit"));
        CoursesTable.getColumnModel().getColumn(5).setCellEditor(new ButtonCellEditor("Edit", this::OnEditCourse));
        CoursesTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonCellRenderer("Delete"));
        CoursesTable.getColumnModel().getColumn(6).setCellEditor(new ButtonCellEditor("Delete", this::OnDeleteCourse));

        CoursesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent MouseEventInstance) {
                if (MouseEventInstance.getClickCount() == 2) {
                    int ViewRowIndex = CoursesTable.getSelectedRow();
                    if (ViewRowIndex >= 0) {
                        int ModelRowIndex = CoursesTable.convertRowIndexToModel(ViewRowIndex);
                        Course SelectedCourse = TableModel.GetRow(ModelRowIndex);
                        OnOpenCourse(SelectedCourse);
                    }
                }
            }
        });
    }

    private void WireFilters() {
        DeptFilter.addActionListener(Event -> ApplyFilters());
        SearchField.addActionListener(Event -> ApplyFilters());
    }

    public void RefreshData() {
        try {
            AllCourses = CourseHandler.GetAllCourses();
        }
        catch (SQLException SqlException) {
            ShowError(SqlException);
            AllCourses = new ArrayList<>();
        }
        ApplyFilters();
    }

    private static String DeriveDept(String CourseCode) {
        if (CourseCode == null) return "OTHER";
        String UpperCode = CourseCode.trim().toUpperCase(Locale.ROOT);
        if (UpperCode.startsWith("CSE")) return "CSE";
        if (UpperCode.startsWith("ECE")) return "ECE";
        if (UpperCode.startsWith("MTH")) return "MTH";
        return "OTHER";
    }

    private void ApplyFilters() {
        String SelectedDepartment = (String) DeptFilter.getSelectedItem();
        String QueryString = SearchField.getText().trim().toLowerCase();

        List<Course> FilteredCourses = AllCourses.stream().filter(CourseInstance -> "All Depts".equals(SelectedDepartment) || DeriveDept(CourseInstance.GetCode()).equalsIgnoreCase(SelectedDepartment)).filter(CourseInstance -> QueryString.isBlank() || String.valueOf(CourseInstance.GetCourseID()).contains(QueryString) || Safe(CourseInstance.GetCode()).toLowerCase().contains(QueryString) || Safe(CourseInstance.GetTitle()).toLowerCase().contains(QueryString)).sorted(Comparator.comparingInt(Course::GetCourseID)).collect(Collectors.toList());

        TableModel.SetRows(FilteredCourses);
    }

    private void OnOpenCourse(Course CurrentCourse) {
        if (OpenCourseListenerInstance != null) {
            OpenCourseListenerInstance.open(CurrentCourse);
            return;
        }
        JOptionPane.showMessageDialog(this, "Open details for CourseID=" + CurrentCourse.GetCourseID() + " (" + Safe(CurrentCourse.GetCode()) + " - " + Safe(CurrentCourse.GetTitle()) + ")", "Open Course", JOptionPane.INFORMATION_MESSAGE);
    }

    private void OnAddCourse() {
        JDialog DialogInstance = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Course", true);
        DialogInstance.setSize(520, 280);
        DialogInstance.setLocationRelativeTo(this);
        DialogInstance.setLayout(new BorderLayout(10, 10));
        DialogInstance.getContentPane().setBackground(ColorBackground);

        JPanel FormPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        FormPanel.setOpaque(false);
        FormPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel LabelCode = new JLabel("Code (e.g., CSE102):");
        JLabel LabelTitle = new JLabel("Title:");
        JLabel LabelCredits = new JLabel("Credits:");
        for (JLabel LabelInstance : new JLabel[]{LabelCode, LabelTitle, LabelCredits}) {
            LabelInstance.setForeground(ColorText);
        }

        JTextField CodeField = new JTextField();
        JTextField TitleField = new JTextField();
        JSpinner CreditsSpinner = new JSpinner(new SpinnerNumberModel(4, 0, 20, 1));

        FormPanel.add(LabelCode);
        FormPanel.add(CodeField);
        FormPanel.add(LabelTitle);
        FormPanel.add(TitleField);
        FormPanel.add(LabelCredits);
        FormPanel.add(CreditsSpinner);

        JPanel ButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ButtonPanel.setOpaque(false);
        JButton CancelButton = new JButton("Cancel");
        JButton SaveButton = PrimaryButton("Create");
        ButtonPanel.add(CancelButton);
        ButtonPanel.add(SaveButton);

        DialogInstance.add(FormPanel, BorderLayout.CENTER);
        DialogInstance.add(ButtonPanel, BorderLayout.SOUTH);

        CancelButton.addActionListener(Event -> DialogInstance.dispose());
        SaveButton.addActionListener(Event -> {
            String CodeValue = CodeField.getText().trim();
            String TitleValue = TitleField.getText().trim();
            int CreditsValue = (Integer) CreditsSpinner.getValue();

            if (TitleValue.isEmpty() || CodeValue.isEmpty()) {
                JOptionPane.showMessageDialog(DialogInstance, "Code and Title are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                Course NewCourse = new Course();
                NewCourse.SetCode(CodeValue);
                NewCourse.SetTitle(TitleValue);
                NewCourse.SetCredits(CreditsValue);

                int NextCourseId = CourseHandler.GetMaxCourseID() + 1;
                NewCourse.SetCourseID(NextCourseId);
                boolean IsInserted = CourseHandler.AddCourse(NewCourse);
                if (!IsInserted) throw new SQLException("Insert returned false.");

                DialogInstance.dispose();
                RefreshData();
                JOptionPane.showMessageDialog(this, "Course created.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            catch (SQLException SqlException) {
                ShowError(SqlException);
            }
        });

        DialogInstance.setVisible(true);
    }

    private void OnEditCourse(int ModelRowIndex) {
        Course CurrentCourse = TableModel.GetRow(ModelRowIndex);

        JDialog DialogInstance = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Course", true);
        DialogInstance.setSize(520, 300);
        DialogInstance.setLocationRelativeTo(this);
        DialogInstance.setLayout(new BorderLayout(10, 10));
        DialogInstance.getContentPane().setBackground(ColorBackground);

        JPanel FormPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        FormPanel.setOpaque(false);
        FormPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel LabelCourseId = new JLabel("CourseID:");
        JLabel LabelCode = new JLabel("Code:");
        JLabel LabelTitle = new JLabel("Title:");
        JLabel LabelCredits = new JLabel("Credits:");
        for (JLabel LabelInstance : new JLabel[]{LabelCourseId, LabelCode, LabelTitle, LabelCredits}) {
            LabelInstance.setForeground(ColorText);
        }

        JTextField CourseIdField = new JTextField(String.valueOf(CurrentCourse.GetCourseID()));
        CourseIdField.setEnabled(false);
        JTextField CodeField = new JTextField(Safe(CurrentCourse.GetCode()));
        JTextField TitleField = new JTextField(Safe(CurrentCourse.GetTitle()));
        JSpinner CreditsSpinner = new JSpinner(new SpinnerNumberModel(CurrentCourse.GetCredits(), 0, 20, 1));

        FormPanel.add(LabelCourseId);
        FormPanel.add(CourseIdField);
        FormPanel.add(LabelCode);
        FormPanel.add(CodeField);
        FormPanel.add(LabelTitle);
        FormPanel.add(TitleField);
        FormPanel.add(LabelCredits);
        FormPanel.add(CreditsSpinner);

        JPanel ButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ButtonPanel.setOpaque(false);
        JButton CancelButton = new JButton("Cancel");
        JButton SaveButton = PrimaryButton("Save");
        ButtonPanel.add(CancelButton);
        ButtonPanel.add(SaveButton);

        DialogInstance.add(FormPanel, BorderLayout.CENTER);
        DialogInstance.add(ButtonPanel, BorderLayout.SOUTH);

        CancelButton.addActionListener(Event -> DialogInstance.dispose());
        SaveButton.addActionListener(Event -> {
            String CodeValue = CodeField.getText().trim();
            String TitleValue = TitleField.getText().trim();
            int CreditsValue = (Integer) CreditsSpinner.getValue();
            if (TitleValue.isEmpty() || CodeValue.isEmpty()) {
                JOptionPane.showMessageDialog(DialogInstance, "Code and Title are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                CurrentCourse.SetCode(CodeValue);
                CurrentCourse.SetTitle(TitleValue);
                CurrentCourse.SetCredits(CreditsValue);

                boolean IsUpdated = CourseHandler.UpdateCourse(CurrentCourse);
                if (!IsUpdated) {
                    JOptionPane.showMessageDialog(DialogInstance, "No rows updated.", "Update", JOptionPane.WARNING_MESSAGE);
                }
                DialogInstance.dispose();
                RefreshData();
            }
            catch (SQLException SqlException) {
                ShowError(SqlException);
            }
        });

        DialogInstance.setVisible(true);
    }

    private void OnDeleteCourse(int ModelRowIndex) {
        Course CurrentCourse = TableModel.GetRow(ModelRowIndex);
        int ConfirmResult = JOptionPane.showConfirmDialog(
                this,
                "Delete course '" + Safe(CurrentCourse.GetTitle()) + "' (Code=" + CurrentCourse.GetCode() + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (ConfirmResult != JOptionPane.YES_OPTION) return;

        try {
            boolean IsDeleted = CourseHandler.DeleteCourse(CurrentCourse.GetCode());
            if (!IsDeleted) {
                JOptionPane.showMessageDialog(this, "Delete failed.", "Delete", JOptionPane.WARNING_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(this, "Course deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            }
            RefreshData();
        }
        catch (SQLException SqlException) {
            ShowError(SqlException);
        }
    }

    private void ShowError(Exception ExceptionObject) {
        ExceptionObject.printStackTrace();
        JOptionPane.showMessageDialog(this, ExceptionObject.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    private String Safe(Object SafeValue) {
        return (SafeValue == null) ? "-" : String.valueOf(SafeValue);
    }

    private static class CoursesTableModel extends AbstractTableModel {
        private final String[] ColumnNames = {"CourseID", "Code", "Title", "Credits", "Dept*", "Edit", "Delete"};
        private List<Course> CourseRows = new ArrayList<>();

        public void SetRows(List<Course> CourseList) {
            CourseRows = new ArrayList<>(CourseList);
            fireTableDataChanged();
        }

        public Course GetRow(int RowIndex) {
            return CourseRows.get(RowIndex);
        }

        @Override
        public int getRowCount() {
            return CourseRows.size();
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
            return ColumnIndex >= 5;
        }

        @Override
        public Object getValueAt(int RowIndex, int ColumnIndex) {
            Course CurrentCourse = CourseRows.get(RowIndex);
            return switch (ColumnIndex) {
                case 0 -> CurrentCourse.GetCourseID();
                case 1 -> CurrentCourse.GetCode();
                case 2 -> CurrentCourse.GetTitle();
                case 3 -> CurrentCourse.GetCredits();
                case 4 -> DeriveDept(CurrentCourse.GetCode());
                case 5 -> "Edit";
                case 6 -> "Delete";
                default -> "";
            };
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
                int RowIndex = ModelRowIndex;
                fireEditingStopped();
                SwingUtilities.invokeLater(() -> {
                    if (RowIndex >= 0) {
                        RowHandler.accept(RowIndex);
                    }
                });
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable TableInstance, Object CellValue, boolean IsSelected,
                                                     int ViewRowIndex, int ColumnIndex) {
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
