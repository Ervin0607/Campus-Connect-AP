package Frontend.AdminPages;

import Backend.AdminUtils.UserRoleFactory;
import Backend.DataBaseHandler.InstructorHandler;
import Backend.DataBaseHandler.StudentHandler;
import Backend.DataBaseHandler.UserAuthenticationHandler;
import Backend.domain.User;
import Backend.domain.Instructor;
import Backend.domain.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class UserPages extends JPanel {

    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel       = new Color(0x1E1E1E);
    private static final Color ColorAccent      = new Color(0xB0F2B4);
    private static final Color ColorText        = new Color(0xFFFFFF);
    private static final Color ColorTextDim     = new Color(0xB0B0B0);
    private static final Color ColorDivider     = new Color(0x2A2A2A);

    private final JComboBox<String> RoleFilter   = new JComboBox<>(new String[]{"All Roles", "STUDENT", "INSTRUCTOR", "ADMIN"});
    private final JComboBox<String> StatusFilter = new JComboBox<>(new String[]{"All Status", "ACTIVE", "SUSPENDED", "PENDING"});
    private final JTextField SearchField         = new JTextField();

    private final JLabel TotalUsersLabel   = new JLabel("-");
    private final JLabel StudentsLabel     = new JLabel("-");
    private final JLabel InstructorsLabel  = new JLabel("-");
    private final JLabel AdminsLabel       = new JLabel("-");
    private final JLabel ActiveLabel       = new JLabel("-");
    private final JLabel SuspendedLabel    = new JLabel("-");

    private final UsersTableModel TableModel = new UsersTableModel();
    private final JTable UsersTable          = new JTable(TableModel);
    private final JPanel DetailsPanel        = new JPanel(new BorderLayout());

    private final RoleBarChart RoleChart     = new RoleBarChart();
    private final RegistrationChart RegChart = new RegistrationChart();

    private List<User> AllUsers = new ArrayList<>();

    public UserPages() {
        setOpaque(true);
        setBackground(ColorBackground);
        setLayout(new BorderLayout(16, 16));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        SeedUserIdFromDb();
        add(BuildTop(), BorderLayout.NORTH);
        add(BuildCenter(), BorderLayout.CENTER);

        WireFilters();
        RefreshData();
    }

    private JComponent BuildTop() {
        JPanel RootPanel = new JPanel(new BorderLayout(12, 12));
        RootPanel.setOpaque(false);

        JPanel FiltersPanel = new JPanel(new BorderLayout(10, 0));
        FiltersPanel.setOpaque(false);

        JPanel RightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        RightPanel.setOpaque(false);

        StyleCombo(RoleFilter);
        StyleCombo(StatusFilter);
        StyleField(SearchField);
        SearchField.setColumns(18);
        SearchField.putClientProperty("JTextField.placeholderText", "Search by UserID / Username");

        JButton AddButton = PrimaryButton("Add User");
        AddButton.addActionListener(e -> OnAddUser());

        RightPanel.add(new JLabel(Chip("Role")));
        RightPanel.add(RoleFilter);
        RightPanel.add(new JLabel(Chip("Status")));
        RightPanel.add(StatusFilter);
        RightPanel.add(SearchField);
        RightPanel.add(AddButton);

        FiltersPanel.add(RightPanel, BorderLayout.EAST);
        RootPanel.add(FiltersPanel, BorderLayout.NORTH);

        JPanel CardsAndChartsPanel = new JPanel(new GridBagLayout());
        CardsAndChartsPanel.setOpaque(false);
        GridBagConstraints GridConstraints = new GridBagConstraints();
        GridConstraints.insets = new Insets(8, 8, 8, 8);
        GridConstraints.fill = GridBagConstraints.BOTH;

        JPanel StatsCardsPanel = new JPanel(new GridLayout(1, 6, 12, 12));
        StatsCardsPanel.setOpaque(false);
        StatsCardsPanel.add(StatCard("Total Users", TotalUsersLabel));
        StatsCardsPanel.add(StatCard("Students", StudentsLabel));
        StatsCardsPanel.add(StatCard("Instructors", InstructorsLabel));
        StatsCardsPanel.add(StatCard("Admins", AdminsLabel));
        StatsCardsPanel.add(StatCard("Active", ActiveLabel));
        StatsCardsPanel.add(StatCard("Suspended", SuspendedLabel));

        GridConstraints.gridx = 0;
        GridConstraints.gridy = 0;
        GridConstraints.weightx = 1;
        GridConstraints.weighty = 0.0;
        CardsAndChartsPanel.add(StatsCardsPanel, GridConstraints);

        JPanel ChartsRowPanel = new JPanel(new GridLayout(1, 2, 12, 12));
        RoleChart.setPreferredSize(new Dimension(440, 240));
        RegChart.setPreferredSize(new Dimension(440, 240));
        ChartsRowPanel.setOpaque(false);
        ChartsRowPanel.add(WrapModule("Users by Role", RoleChart));
        ChartsRowPanel.add(WrapModule("Registrations Over Time", RegChart));

        GridConstraints.gridy = 1;
        GridConstraints.weighty = 0.45;
        CardsAndChartsPanel.add(ChartsRowPanel, GridConstraints);

        RootPanel.add(CardsAndChartsPanel, BorderLayout.CENTER);
        return RootPanel;
    }

    private JComponent BuildCenter() {
        JSplitPane SplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        SplitPane.setBackground(ColorBackground);
        SplitPane.setBorder(null);
        SplitPane.setResizeWeight(0.64);

        JPanel TablePanel = new JPanel(new BorderLayout());
        TablePanel.setOpaque(false);
        ConfigureTable();
        JScrollPane ScrollPane = new JScrollPane(UsersTable);
        ScrollPane.getViewport().setBackground(ColorBackground);
        ScrollPane.setBorder(BorderFactory.createLineBorder(ColorDivider));
        TablePanel.add(WrapModule("All Users", ScrollPane), BorderLayout.CENTER);

        DetailsPanel.setOpaque(false);
        DetailsPanel.add(WrapModule("Details", DetailsPlaceholder()), BorderLayout.CENTER);

        SplitPane.setLeftComponent(TablePanel);
        SplitPane.setRightComponent(DetailsPanel);
        return SplitPane;
    }

    private JPanel StatCard(String Title, JLabel ValueLabel) {
        JPanel CardPanel = RoundedPanel();
        CardPanel.setBackground(ColorPanel);
        CardPanel.setLayout(new BorderLayout());
        CardPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel TitleLabel = new JLabel(Title);
        TitleLabel.setForeground(ColorTextDim);
        TitleLabel.setFont(TitleLabel.getFont().deriveFont(Font.PLAIN, 13f));

        ValueLabel.setForeground(ColorText);
        ValueLabel.setFont(ValueLabel.getFont().deriveFont(Font.BOLD, 22f));

        CardPanel.add(TitleLabel, BorderLayout.NORTH);
        CardPanel.add(ValueLabel, BorderLayout.CENTER);
        return CardPanel;
    }

    private JPanel WrapModule(String Title, JComponent Content) {
        JPanel ModulePanel = RoundedPanel();
        ModulePanel.setBackground(ColorPanel);
        ModulePanel.setLayout(new BorderLayout());
        ModulePanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel TitleLabel = new JLabel(Title);
        TitleLabel.setForeground(ColorText);
        TitleLabel.setFont(TitleLabel.getFont().deriveFont(Font.BOLD, 16f));
        ModulePanel.add(TitleLabel, BorderLayout.NORTH);
        ModulePanel.add(Content, BorderLayout.CENTER);
        return ModulePanel;
    }

    private JPanel RoundedPanel() {
        return new JPanel() {
            @Override
            public boolean isOpaque() {
                return false;
            }

            @Override
            protected void paintComponent(Graphics GraphicsContext) {
                Graphics2D Graphics2DContext = (Graphics2D) GraphicsContext.create();
                Graphics2DContext.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Graphics2DContext.setColor(getBackground());
                Graphics2DContext.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                Graphics2DContext.dispose();
                super.paintComponent(GraphicsContext);
            }
        };
    }

    private void SeedUserIdFromDb() {
        try {
            int MaxUserId = UserAuthenticationHandler.GetMaxUserID();
            UserRoleFactory.InitializeFromDatabase(MaxUserId);
        }
        catch (Exception ExceptionObject) {
            ExceptionObject.printStackTrace();
        }
    }

    private String Chip(String Text) {
        return " " + Text + " ";
    }

    private void StyleCombo(JComboBox<?> ComboBox) {
        ComboBox.setBackground(ColorPanel);
        ComboBox.setForeground(ColorText);
        ComboBox.setBorder(BorderFactory.createLineBorder(ColorDivider));
    }

    private void StyleField(JTextField TextField) {
        TextField.setBackground(ColorPanel);
        TextField.setForeground(ColorText);
        TextField.setCaretColor(ColorText);
        TextField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ColorDivider), new EmptyBorder(6, 8, 6, 8)));
    }

    private JButton PrimaryButton(String Text) {
        JButton PrimaryButton = new JButton(Text);
        PrimaryButton.setBackground(ColorAccent);
        PrimaryButton.setForeground(Color.BLACK);
        PrimaryButton.setFocusPainted(false);
        PrimaryButton.setBorder(new EmptyBorder(8, 14, 8, 14));
        return PrimaryButton;
    }

    private void ConfigureTable() {
        UsersTable.setFillsViewportHeight(true);
        UsersTable.setRowHeight(32);
        UsersTable.setBackground(ColorBackground);
        UsersTable.setForeground(ColorText);
        UsersTable.setGridColor(ColorDivider);
        UsersTable.setSelectionBackground(new Color(0x2A2A2A));
        UsersTable.setSelectionForeground(ColorText);
        UsersTable.setAutoCreateRowSorter(true);

        JTableHeader TableHeader = UsersTable.getTableHeader();
        TableHeader.setBackground(ColorPanel);
        TableHeader.setForeground(ColorTextDim);
        TableHeader.setReorderingAllowed(false);

        UsersTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonCellRenderer("Edit"));
        UsersTable.getColumnModel().getColumn(5).setCellEditor(new ButtonCellEditor("Edit", this::OnEditUser));
        UsersTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonCellRenderer("Delete"));
        UsersTable.getColumnModel().getColumn(6).setCellEditor(new ButtonCellEditor("Delete", this::OnDeleteUser));

        UsersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent MouseEventClick) {
                if (MouseEventClick.getClickCount() == 2) {
                    int ViewRowIndex = UsersTable.getSelectedRow();
                    if (ViewRowIndex >= 0) {
                        int ModelRowIndex = UsersTable.convertRowIndexToModel(ViewRowIndex);
                        ShowDetails(TableModel.GetRow(ModelRowIndex));
                    }
                }
            }
        });
    }

    private JComponent DetailsPlaceholder() {
        JPanel PlaceholderPanel = new JPanel(new GridBagLayout());
        PlaceholderPanel.setOpaque(false);
        JLabel PlaceholderLabel = new JLabel("Double-click a user to view details");
        PlaceholderLabel.setForeground(ColorTextDim);
        PlaceholderLabel.setFont(PlaceholderLabel.getFont().deriveFont(Font.PLAIN, 16f));
        PlaceholderPanel.add(PlaceholderLabel);
        return PlaceholderPanel;
    }

    private void ShowDetails(User CurrentUser) {
        JPanel DetailsBoxPanel = new JPanel();
        DetailsBoxPanel.setOpaque(false);
        DetailsBoxPanel.setLayout(new BoxLayout(DetailsBoxPanel, BoxLayout.Y_AXIS));
        DetailsBoxPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        DetailsBoxPanel.add(Detail("UserID", String.valueOf(CurrentUser.GetUserID())));
        DetailsBoxPanel.add(Detail("Username", Safe(CurrentUser.GetUserName())));
        DetailsBoxPanel.add(Detail("Role", Safe(CurrentUser.GetRole())));
        DetailsBoxPanel.add(Detail("Status", Safe(CurrentUser.GetStatus())));
        DetailsBoxPanel.add(Detail("Password (protected)", "********"));

        DetailsBoxPanel.add(Spacer());
        DetailsBoxPanel.add(Separator());

        String CurrentRole = CurrentUser.GetRole() == null ? "" : CurrentUser.GetRole();

        if ("Instructor".equalsIgnoreCase(CurrentRole)) {
            DetailsBoxPanel.add(Section("Instructor Details"));
            Instructor CurrentInstructor = null;
            try {
                CurrentInstructor = InstructorHandler.FindInstructorByUserID(CurrentUser.GetUserID());
            }
            catch (SQLException ExceptionObject) {
                showError(ExceptionObject);
            }
            DetailsBoxPanel.add(Detail("Name", Safe(CurrentInstructor == null ? null : CurrentInstructor.GetName())));
            DetailsBoxPanel.add(Detail("Email", Safe(CurrentInstructor == null ? null : CurrentInstructor.GetEmail())));
            DetailsBoxPanel.add(Detail("Qualification", Safe(CurrentInstructor == null ? null : CurrentInstructor.GetQualification())));
            DetailsBoxPanel.add(Detail("Joining Date", Safe(CurrentInstructor == null ? null : CurrentInstructor.GetJoiningDate())));
            DetailsBoxPanel.add(Detail("Department", Safe(CurrentInstructor == null ? null : CurrentInstructor.GetDepartment())));

        }
        else if ("Student".equalsIgnoreCase(CurrentRole)) {
            DetailsBoxPanel.add(Section("Student Details"));
            Student CurrentStudent = null;
            try {
                CurrentStudent = StudentHandler.FindStudentByUserID(CurrentUser.GetUserID());
            }
            catch (SQLException ExceptionObject) {
                showError(ExceptionObject);
            }
            DetailsBoxPanel.add(Detail("Name", Safe(CurrentStudent == null ? null : CurrentStudent.GetName())));
            DetailsBoxPanel.add(Detail("Roll Number", Safe(CurrentStudent == null ? null : CurrentStudent.GetRollNumber())));
            DetailsBoxPanel.add(Detail("Program", Safe(CurrentStudent == null ? null : CurrentStudent.GetProgram())));
            DetailsBoxPanel.add(Detail("Year", Safe(CurrentStudent == null ? null : CurrentStudent.GetYear())));
        }
        else {
            DetailsBoxPanel.add(Section("Admin Details"));
        }

        DetailsPanel.removeAll();
        DetailsPanel.add(WrapModule("Details", new JScrollPane(DetailsBoxPanel)), BorderLayout.CENTER);
        DetailsPanel.revalidate();
        DetailsPanel.repaint();
    }

    private JComponent Section(String Text) {
        JLabel SectionLabel = new JLabel(Text);
        SectionLabel.setForeground(ColorText);
        SectionLabel.setFont(SectionLabel.getFont().deriveFont(Font.BOLD, 15f));
        JPanel SectionPanel = new JPanel(new BorderLayout());
        SectionPanel.setOpaque(false);
        SectionPanel.add(SectionLabel, BorderLayout.CENTER);
        SectionPanel.setBorder(new EmptyBorder(8, 0, 4, 0));
        return SectionPanel;
    }

    private JComponent Detail(String Key, String Value) {
        JPanel DetailRowPanel = new JPanel(new BorderLayout());
        DetailRowPanel.setOpaque(false);
        JLabel KeyLabel = new JLabel(Key);
        KeyLabel.setForeground(ColorTextDim);
        JLabel ValueLabel = new JLabel(Value);
        ValueLabel.setForeground(ColorText);
        DetailRowPanel.add(KeyLabel, BorderLayout.WEST);
        DetailRowPanel.add(ValueLabel, BorderLayout.EAST);
        DetailRowPanel.setBorder(new EmptyBorder(3, 0, 3, 0));
        return DetailRowPanel;
    }

    private JComponent Separator() {
        JSeparator Separator = new JSeparator(SwingConstants.HORIZONTAL);
        Separator.setForeground(ColorDivider);
        Separator.setBackground(ColorDivider);
        return Separator;
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
        RoleFilter.addActionListener(e -> ApplyFilters());
        StatusFilter.addActionListener(e -> ApplyFilters());
        SearchField.addActionListener(e -> ApplyFilters());
    }

    private void ApplyFilters() {
        String SelectedRole = (String) RoleFilter.getSelectedItem();
        String SelectedStatus = (String) StatusFilter.getSelectedItem();
        String SearchQuery = SearchField.getText().trim().toLowerCase();

        List<User> FilteredUsersList = AllUsers.stream()
                .filter(UserItem -> "All Roles".equals(SelectedRole) || Safe(UserItem.GetRole()).equalsIgnoreCase(SelectedRole))
                .filter(UserItem -> "All Status".equals(SelectedStatus) || Safe(UserItem.GetStatus()).equalsIgnoreCase(SelectedStatus))
                .filter(UserItem -> SearchQuery.isBlank()
                        || String.valueOf(UserItem.GetUserID()).contains(SearchQuery)
                        || Safe(UserItem.GetUserName()).toLowerCase().contains(SearchQuery))
                .sorted(Comparator.comparingInt(User::GetUserID))
                .collect(Collectors.toList());

        TableModel.SetRows(FilteredUsersList);
        UpdateAnalytics(FilteredUsersList);
        RoleChart.SetData(FilteredUsersList);
        RegChart.SetData(FetchRegistrationCounts());
    }

    private void UpdateAnalytics(List<User> UsersList) {
        int TotalUsersCount = UsersList.size();
        int StudentCount = (int) UsersList.stream().filter(UserItem -> Safe(UserItem.GetRole()).equalsIgnoreCase("Student")).count();
        int InstructorCount = (int) UsersList.stream().filter(UserItem -> Safe(UserItem.GetRole()).equalsIgnoreCase("Instructor")).count();
        int AdminCount = (int) UsersList.stream().filter(UserItem -> Safe(UserItem.GetRole()).equalsIgnoreCase("Admin")).count();
        int ActiveCount = (int) UsersList.stream().filter(UserItem -> Safe(UserItem.GetStatus()).equalsIgnoreCase("Active")).count();
        int SuspendedCount = (int) UsersList.stream().filter(UserItem -> Safe(UserItem.GetStatus()).equalsIgnoreCase("Suspended")).count();

        TotalUsersLabel.setText(String.valueOf(TotalUsersCount));
        StudentsLabel.setText(String.valueOf(StudentCount));
        InstructorsLabel.setText(String.valueOf(InstructorCount));
        AdminsLabel.setText(String.valueOf(AdminCount));
        ActiveLabel.setText(String.valueOf(ActiveCount));
        SuspendedLabel.setText(String.valueOf(SuspendedCount));
    }

    private static LocalDate parseIsoDate(String DateString) {
        try {
            return LocalDate.parse(DateString.trim());
        }
        catch (Exception ExceptionObject) {
            return null;
        }
    }

    private static String toIso(LocalDate LocalDateValue) {
        return (LocalDateValue == null) ? "" : LocalDateValue.toString();
    }

    private void OnEditUser(int ModelRowIndex) {
        User SelectedUser = TableModel.GetRow(ModelRowIndex);
        String RoleUpper = (SelectedUser.GetRole() == null) ? "" : SelectedUser.GetRole().trim().toUpperCase(Locale.ROOT);

        if ("INSTRUCTOR".equals(RoleUpper)) {
            try {
                Instructor CurrentInstructor = InstructorHandler.FindInstructorByUserID(SelectedUser.GetUserID());
                if (CurrentInstructor == null) {
                    JOptionPane.showMessageDialog(this, "No instructor record found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JDialog EditDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Instructor", true);
                EditDialog.setSize(520, 360);
                EditDialog.setLocationRelativeTo(this);
                JPanel FormPanel = new JPanel(new GridLayout(0, 2, 8, 8));
                FormPanel.setOpaque(false);
                FormPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

                JLabel NameLabel = new JLabel("Name:");
                JLabel EmailLabel = new JLabel("Email:");
                JLabel QualificationLabel = new JLabel("Qualification:");
                JLabel JoiningDateLabel = new JLabel("Joining Date (YYYY-MM-DD):");
                JLabel DepartmentLabel = new JLabel("Department:");

                for (JLabel LabelItem : new JLabel[]{NameLabel, EmailLabel, QualificationLabel, JoiningDateLabel, DepartmentLabel}) {
                    LabelItem.setForeground(ColorText);
                }

                JTextField NameField = new JTextField(CurrentInstructor.GetName());
                JTextField EmailField = new JTextField(CurrentInstructor.GetEmail());
                JTextField QualificationField = new JTextField(CurrentInstructor.GetQualification());
                JTextField JoiningDateField = new JTextField(toIso(CurrentInstructor.GetJoiningDate()));
                JTextField DepartmentField = new JTextField(CurrentInstructor.GetDepartment());

                FormPanel.add(NameLabel);
                FormPanel.add(NameField);
                FormPanel.add(EmailLabel);
                FormPanel.add(EmailField);
                FormPanel.add(QualificationLabel);
                FormPanel.add(QualificationField);
                FormPanel.add(JoiningDateLabel);
                FormPanel.add(JoiningDateField);
                FormPanel.add(DepartmentLabel);
                FormPanel.add(DepartmentField);

                JPanel ButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                ButtonsPanel.setOpaque(false);
                JButton CancelButton = new JButton("Cancel");
                JButton SaveButton = PrimaryButton("Save");
                ButtonsPanel.add(CancelButton);
                ButtonsPanel.add(SaveButton);

                EditDialog.add(FormPanel, BorderLayout.CENTER);
                EditDialog.add(ButtonsPanel, BorderLayout.SOUTH);

                CancelButton.addActionListener(e -> EditDialog.dispose());
                SaveButton.addActionListener(e -> {
                    LocalDate JoiningDate = parseIsoDate(JoiningDateField.getText());
                    if (NameField.getText().trim().isEmpty() || EmailField.getText().trim().isEmpty() || QualificationField.getText().trim().isEmpty() || JoiningDate == null || DepartmentField.getText().trim().isEmpty()) {
                        JOptionPane.showMessageDialog(EditDialog, "All fields required; check date format.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try {
                        CurrentInstructor.SetName(NameField.getText().trim());
                        CurrentInstructor.SetEmail(EmailField.getText().trim());
                        CurrentInstructor.SetQualification(QualificationField.getText().trim());
                        CurrentInstructor.SetJoiningDate(JoiningDate);
                        CurrentInstructor.SetDepartment(DepartmentField.getText().trim());
                        boolean UpdateOk = InstructorHandler.UpdateInstructor(CurrentInstructor);
                        if (!UpdateOk) {
                            JOptionPane.showMessageDialog(EditDialog, "No rows updated.", "Update", JOptionPane.WARNING_MESSAGE);
                        }
                        EditDialog.dispose();
                        RefreshData();
                    }
                    catch (SQLException ExceptionObject) {
                        showError(ExceptionObject);
                    }
                });
                EditDialog.setVisible(true);
                return;
            }
            catch (SQLException ExceptionObject) {
                showError(ExceptionObject);
                return;
            }
        }

        if ("STUDENT".equals(RoleUpper)) {
            try {
                Student CurrentStudent = StudentHandler.FindStudentByUserID(SelectedUser.GetUserID());
                if (CurrentStudent == null) {
                    JOptionPane.showMessageDialog(this, "No student record found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                JDialog EditDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Student", true);
                EditDialog.setSize(520, 300);
                EditDialog.setLocationRelativeTo(this);
                JPanel FormPanel = new JPanel(new GridLayout(0, 2, 8, 8));
                FormPanel.setOpaque(false);
                FormPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

                JLabel NameLabel = new JLabel("Name:");
                JLabel RollNumberLabel = new JLabel("Roll Number:");
                JLabel ProgramLabel = new JLabel("Program:");
                JLabel YearLabel = new JLabel("Year:");

                for (JLabel LabelItem : new JLabel[]{NameLabel, RollNumberLabel, ProgramLabel, YearLabel}) {
                    LabelItem.setForeground(ColorText);
                }

                JTextField NameField = new JTextField(CurrentStudent.GetName());
                JTextField RollField = new JTextField(String.valueOf(CurrentStudent.GetRollNumber()));
                JTextField ProgramField = new JTextField(CurrentStudent.GetProgram());
                JSpinner YearSpinner = new JSpinner(new SpinnerNumberModel(CurrentStudent.GetYear(), 1, 10, 1));

                FormPanel.add(NameLabel);
                FormPanel.add(NameField);
                FormPanel.add(RollNumberLabel);
                FormPanel.add(RollField);
                FormPanel.add(ProgramLabel);
                FormPanel.add(ProgramField);
                FormPanel.add(YearLabel);
                FormPanel.add(YearSpinner);

                JPanel ButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                ButtonsPanel.setOpaque(false);
                JButton CancelButton = new JButton("Cancel");
                JButton SaveButton = PrimaryButton("Save");
                ButtonsPanel.add(CancelButton);
                ButtonsPanel.add(SaveButton);

                EditDialog.add(FormPanel, BorderLayout.CENTER);
                EditDialog.add(ButtonsPanel, BorderLayout.SOUTH);

                CancelButton.addActionListener(e -> EditDialog.dispose());
                SaveButton.addActionListener(e -> {
                    String NameValue = NameField.getText().trim();
                    String RollValue = RollField.getText().trim();
                    String ProgramValue = ProgramField.getText().trim();
                    if (ProgramValue.isEmpty()) {
                        JOptionPane.showMessageDialog(this,
                                "Program is required.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    if (ProgramValue.matches("\\d+")) {
                        JOptionPane.showMessageDialog(this,
                                "Program must be text (not a number).",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    int YearValue = (Integer) YearSpinner.getValue();
                    if (NameValue.isEmpty() || RollValue.isEmpty() || ProgramValue.isEmpty()) {
                        JOptionPane.showMessageDialog(EditDialog, "Name, Roll Number, Program are required.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int RollInt;
                    try {
                        RollInt = Integer.parseInt(RollValue);
                        if (RollInt <= 0) {
                            JOptionPane.showMessageDialog(EditDialog,
                                    "Roll Number must be a positive integer greater than 0.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(EditDialog,
                                "Roll Number must be a valid integer.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try {
                        Student ExistingByRoll = StudentHandler.FindStudentByRollNumber(RollInt);
                        if (ExistingByRoll != null && ExistingByRoll.GetUserID() != SelectedUser.GetUserID()) {
                            JOptionPane.showMessageDialog(EditDialog,
                                    "A student with this Roll Number already exists.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    catch (SQLException ex) {
                        showError(ex);
                        return;
                    }

                    try {
                        boolean UpdateOk = new StudentHandler().UpdateStudent(RollValue, NameValue, ProgramValue, YearValue);
                        if (!UpdateOk) {
                            JOptionPane.showMessageDialog(EditDialog, "No rows updated.", "Update", JOptionPane.WARNING_MESSAGE);
                        }
                        EditDialog.dispose();
                        RefreshData();
                    } catch (SQLException ExceptionObject) {
                        showError(ExceptionObject);
                    }
                });
                EditDialog.setVisible(true);
                return;
            }
            catch (SQLException ExceptionObject) {
                showError(ExceptionObject);
                return;
            }
        }

        JTextField UserNameField = new JTextField(SelectedUser.GetUserName());
        int OptionResult = JOptionPane.showConfirmDialog(this, UserNameField, "Edit Username", JOptionPane.OK_CANCEL_OPTION);
        if (OptionResult == JOptionPane.OK_OPTION) {
            String NewUserNameValue = UserNameField.getText().trim();
            if (NewUserNameValue.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                boolean UsernameUpdated = UserAuthenticationHandler.UpdateUser(SelectedUser, NewUserNameValue);
                if (!UsernameUpdated) {
                    JOptionPane.showMessageDialog(this, "No rows updated.", "Update", JOptionPane.WARNING_MESSAGE);
                }
                RefreshData();
            }
            catch (SQLException ExceptionObject) {
                showError(ExceptionObject);
            }
        }
    }

    private void OnDeleteUser(int ModelRowIndex) {
        User SelectedUser = TableModel.GetRow(ModelRowIndex);

        int ConfirmResult = JOptionPane.showConfirmDialog(this,
                "Delete user '" + SelectedUser.GetUserName() + "' (UserID=" + SelectedUser.GetUserID() + ")?\n" +
                        "(Related Student/Instructor record will also be removed if present.)",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (ConfirmResult != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String RoleUpper = (SelectedUser.GetRole() == null) ? "" : SelectedUser.GetRole().trim().toUpperCase(Locale.ROOT);

            switch (RoleUpper) {
                case "STUDENT": {
                    Student ExistingStudent = null;
                    try {
                        ExistingStudent = StudentHandler.FindStudentByUserID(SelectedUser.GetUserID());
                    }
                    catch (SQLException IgnoredException) {
                    }
                    if (ExistingStudent != null) {
                        boolean ChildDeleteOk = new StudentHandler().DeleteStudent(ExistingStudent);
                        if (!ChildDeleteOk) {
                            System.err.println("WARN: Student row not deleted for UserID=" + SelectedUser.GetUserID());
                        }
                    }
                    break;
                }
                case "INSTRUCTOR": {
                    boolean ChildDeleteOk = new InstructorHandler().DeleteInstructor(SelectedUser.GetUserID());
                    if (!ChildDeleteOk) {
                        System.err.println("WARN: Instructor row not deleted for UserID=" + SelectedUser.GetUserID());
                    }
                    break;
                }
            }

            boolean UserDeleteOk = UserAuthenticationHandler.DeleteUser(SelectedUser);
            if (!UserDeleteOk) {
                JOptionPane.showMessageDialog(this, "Delete failed (users row not removed).", "Delete", JOptionPane.WARNING_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(this, "User deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            }

            RefreshData();

        }
        catch (SQLException ExceptionObject) {
            showError(ExceptionObject);
        }
    }

    private void OnAddUser() {
        final JDialog AddDialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Create User",
                true
        );
        AddDialog.setSize(520, 420);
        AddDialog.setLocationRelativeTo(this);
        AddDialog.setLayout(new BorderLayout(10, 10));
        AddDialog.getContentPane().setBackground(ColorBackground);

        JPanel FormPanel = new JPanel(new GridBagLayout());
        FormPanel.setOpaque(false);
        FormPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints GridConstraints = new GridBagConstraints();
        GridConstraints.insets = new Insets(6, 6, 6, 6);
        GridConstraints.fill = GridBagConstraints.HORIZONTAL;
        GridConstraints.gridx = 0;
        GridConstraints.gridy = 0;

        JLabel NameLabel = new JLabel("Name:");
        JLabel RoleLabel = new JLabel("Role:");
        JLabel EmptyLabel = new JLabel("");
        JLabel UsernameLabel = new JLabel("Username:");

        for (JLabel LabelItem : new JLabel[]{NameLabel, RoleLabel, EmptyLabel, UsernameLabel}) {
            LabelItem.setForeground(ColorText);
        }

        JTextField NameField = new JTextField();
        JComboBox<String> RoleBox = new JComboBox<>(new String[]{"STUDENT", "INSTRUCTOR", "ADMIN"});

        JCheckBox CustomUsernameCheckBox = new JCheckBox("Custom Username?");
        CustomUsernameCheckBox.setOpaque(false);
        CustomUsernameCheckBox.setForeground(ColorText);
        JTextField UsernameField = new JTextField();
        UsernameField.setEnabled(false);
        CustomUsernameCheckBox.addActionListener(e -> UsernameField.setEnabled(CustomUsernameCheckBox.isSelected()));

        GridConstraints.gridx = 0;
        GridConstraints.weightx = 0;
        FormPanel.add(NameLabel, GridConstraints);
        GridConstraints.gridx = 1;
        GridConstraints.weightx = 1;
        FormPanel.add(NameField, GridConstraints);

        GridConstraints.gridy++;
        GridConstraints.gridx = 0;
        GridConstraints.weightx = 0;
        FormPanel.add(RoleLabel, GridConstraints);
        GridConstraints.gridx = 1;
        GridConstraints.weightx = 1;
        FormPanel.add(RoleBox, GridConstraints);

        GridConstraints.gridy++;
        GridConstraints.gridx = 0;
        GridConstraints.weightx = 0;
        FormPanel.add(EmptyLabel, GridConstraints);
        GridConstraints.gridx = 1;
        GridConstraints.weightx = 1;
        FormPanel.add(CustomUsernameCheckBox, GridConstraints);

        GridConstraints.gridy++;
        GridConstraints.gridx = 0;
        GridConstraints.weightx = 0;
        FormPanel.add(UsernameLabel, GridConstraints);
        GridConstraints.gridx = 1;
        GridConstraints.weightx = 1;
        FormPanel.add(UsernameField, GridConstraints);

        JPanel RoleCardsPanel = new JPanel(new CardLayout());
        RoleCardsPanel.setOpaque(false);

        JPanel StudentFormPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        StudentFormPanel.setOpaque(false);
        JLabel RollNumberLabel = new JLabel("Roll Number:");
        RollNumberLabel.setForeground(ColorText);
        JTextField RollField = new JTextField();
        JLabel ProgramLabel = new JLabel("Program:");
        ProgramLabel.setForeground(ColorText);
        JTextField ProgramField = new JTextField();
        JLabel YearLabel = new JLabel("Year:");
        YearLabel.setForeground(ColorText);
        JSpinner YearSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        StudentFormPanel.add(RollNumberLabel);
        StudentFormPanel.add(RollField);
        StudentFormPanel.add(ProgramLabel);
        StudentFormPanel.add(ProgramField);
        StudentFormPanel.add(YearLabel);
        StudentFormPanel.add(YearSpinner);

        JPanel InstructorFormPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        InstructorFormPanel.setOpaque(false);
        JLabel EmailLabel = new JLabel("Email:");
        EmailLabel.setForeground(ColorText);
        JTextField EmailField = new JTextField();
        JLabel QualificationLabel = new JLabel("Qualification:");
        QualificationLabel.setForeground(ColorText);
        JTextField QualificationField = new JTextField();
        JLabel JoiningDateLabel = new JLabel("Joining Date (YYYY-MM-DD):");
        JoiningDateLabel.setForeground(ColorText);
        JTextField JoiningDateField = new JTextField(toIso(LocalDate.now()));
        JLabel DepartmentLabel = new JLabel("Department:");
        DepartmentLabel.setForeground(ColorText);
        JTextField DepartmentField = new JTextField();
        JLabel InstructorIdLabel = new JLabel("InstructorID (optional):");
        InstructorIdLabel.setForeground(ColorText);
        JTextField InstructorIdField = new JTextField();
        InstructorIdField.setEnabled(true);

        InstructorFormPanel.add(EmailLabel);
        InstructorFormPanel.add(EmailField);
        InstructorFormPanel.add(QualificationLabel);
        InstructorFormPanel.add(QualificationField);
        InstructorFormPanel.add(JoiningDateLabel);
        InstructorFormPanel.add(JoiningDateField);
        InstructorFormPanel.add(DepartmentLabel);
        InstructorFormPanel.add(DepartmentField);
        InstructorFormPanel.add(InstructorIdLabel);
        InstructorFormPanel.add(InstructorIdField);

        JPanel AdminFormPanel = new JPanel(new BorderLayout());
        AdminFormPanel.setOpaque(false);
        JLabel AdminNoteLabel = new JLabel("No extra fields for ADMIN.");
        AdminNoteLabel.setForeground(ColorTextDim);
        AdminFormPanel.add(AdminNoteLabel, BorderLayout.WEST);

        RoleCardsPanel.add(StudentFormPanel, "STUDENT");
        RoleCardsPanel.add(InstructorFormPanel, "INSTRUCTOR");
        RoleCardsPanel.add(AdminFormPanel, "ADMIN");

        RoleBox.addActionListener(e -> {
            String SelectedRole = (String) RoleBox.getSelectedItem();
            CardLayout CardLayoutManager = (CardLayout) RoleCardsPanel.getLayout();
            CardLayoutManager.show(RoleCardsPanel, SelectedRole);
        });

        ((CardLayout) RoleCardsPanel.getLayout()).show(RoleCardsPanel, "STUDENT");

        GridConstraints.gridy++;
        GridConstraints.gridx = 0;
        GridConstraints.gridwidth = 2;
        GridConstraints.weightx = 1;
        FormPanel.add(RoleCardsPanel, GridConstraints);

        JPanel ButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ButtonsPanel.setOpaque(false);
        JButton CancelButton = new JButton("Cancel");
        JButton SaveButton = PrimaryButton("Create");
        ButtonsPanel.add(CancelButton);
        ButtonsPanel.add(SaveButton);

        AddDialog.add(FormPanel, BorderLayout.CENTER);
        AddDialog.add(ButtonsPanel, BorderLayout.SOUTH);

        CancelButton.addActionListener(e -> AddDialog.dispose());
        SaveButton.addActionListener(e -> {
            String NameValue = NameField.getText().trim();
            String RoleUpper = ((String) RoleBox.getSelectedItem()).trim().toUpperCase(Locale.ROOT);
            String CustomUsernameValue = CustomUsernameCheckBox.isSelected() ? UsernameField.getText().trim() : null;

            if (NameValue.isEmpty()) {
                JOptionPane.showMessageDialog(AddDialog, "Name is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if ("STUDENT".equals(RoleUpper)) {
                String ProgramValue = ProgramField.getText().trim();
                String RollValue = RollField.getText().trim();
                int YearValue = (Integer) YearSpinner.getValue();

                boolean hasStudentDetails = !ProgramValue.isEmpty() || !RollValue.isEmpty() || YearValue != 1;

                if (hasStudentDetails) {
                    if (RollValue.isEmpty() || ProgramValue.isEmpty()) {
                        JOptionPane.showMessageDialog(AddDialog, "Roll Number and Program are required when filling student details.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    int RollInt;
                    try {
                        RollInt = Integer.parseInt(RollValue);
                        if (RollInt <= 0) {
                            JOptionPane.showMessageDialog(AddDialog,
                                    "Roll Number must be a positive integer greater than 0.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(AddDialog,
                                "Roll Number must be a valid integer.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        Student ExistingByRoll = StudentHandler.FindStudentByRollNumber(RollInt);
                        if (ExistingByRoll != null) {
                            JOptionPane.showMessageDialog(AddDialog,
                                    "A student with this Roll Number already exists.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    catch (SQLException ex) {
                        showError(ex);
                        return;
                    }
                }
            }

            try {
                Backend.AdminUtils.UserRoleFactory UserRoleFactoryInstance = new Backend.AdminUtils.UserRoleFactory();
                boolean CreateOk;
                if (CustomUsernameValue == null || CustomUsernameValue.isBlank()) {
                    CreateOk = UserRoleFactoryInstance.Create(NameValue, RoleUpper);
                }
                else {
                    int CurrentYear = java.time.Year.now().getValue();
                    CreateOk = UserRoleFactoryInstance.Create(NameValue, RoleUpper, CustomUsernameValue, CurrentYear);
                }
                if (!CreateOk) {
                    JOptionPane.showMessageDialog(AddDialog, "User create failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int NewUserId = UserAuthenticationHandler.GetMaxUserID();

                switch (RoleUpper) {
                    case "STUDENT": {
                        String ProgramValue = ProgramField.getText().trim();
                        String RollValue = RollField.getText().trim();
                        int YearValue = (Integer) YearSpinner.getValue();

                        boolean hasStudentDetails =
                                !ProgramValue.isEmpty() ||
                                        !RollValue.isEmpty() ||
                                        YearValue != 1;

                        if (!hasStudentDetails) {
                            break;
                        }

                        if (RollValue.isEmpty() || ProgramValue.isEmpty()) {
                            JOptionPane.showMessageDialog(AddDialog, "Roll Number and Program are required when filling student details.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        int RollInt;
                        try {
                            RollInt = Integer.parseInt(RollValue);
                            if (RollInt <= 0) {
                                JOptionPane.showMessageDialog(AddDialog, "Roll Number must be a positive integer greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }
                        catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(AddDialog,
                                    "Roll Number must be a valid integer.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        try {
                            Student ExistingStudent = StudentHandler.FindStudentByUserID(NewUserId);
                            if (ExistingStudent != null) {
                                new StudentHandler().DeleteStudent(ExistingStudent);
                            }

                            Student NewStudent = new Student();
                            NewStudent.SetUserID(NewUserId);
                            NewStudent.SetName(NameValue);
                            NewStudent.SetProgram(ProgramValue);
                            NewStudent.SetYear(YearValue);
                            NewStudent.SetRollNumber(RollInt);

                            boolean StudentAdded = StudentHandler.AddStudent(NewStudent);
                            if (!StudentAdded) {
                                throw new SQLException("Failed to insert student row.");
                            }
                        }
                        catch (SQLException ex) {
                            showError(ex);
                            return;
                        }
                        break;
                    }

                    case "INSTRUCTOR": {
                        String EmailValue = EmailField.getText().trim();
                        String QualificationValue = QualificationField.getText().trim();
                        LocalDate JoiningDateValue = parseIsoDate(JoiningDateField.getText());
                        String DepartmentValue = DepartmentField.getText().trim();
                        Integer InstructorIdValue = null;
                        if (!InstructorIdField.getText().trim().isEmpty()) {
                            try {
                                InstructorIdValue = Integer.parseInt(InstructorIdField.getText().trim());
                            }
                            catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(AddDialog, "InstructorID must be a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }

                        boolean hasInstructorDetails = !EmailValue.isEmpty() || !QualificationValue.isEmpty() || (JoiningDateField.getText() != null && !JoiningDateField.getText().trim().isEmpty()) || !DepartmentValue.isEmpty() || InstructorIdValue != null;

                        if (!hasInstructorDetails) {
                            break;
                        }

                        if (EmailValue.isEmpty() || QualificationValue.isEmpty()
                                || JoiningDateValue == null || DepartmentValue.isEmpty()) {
                            JOptionPane.showMessageDialog(AddDialog,
                                    "Email, Qualification, Joining Date, Department are required when filling instructor details.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        try {
                            Instructor ExistingInstructor = InstructorHandler.FindInstructorByUserID(NewUserId);
                            if (ExistingInstructor != null) {
                                new InstructorHandler().DeleteInstructor(NewUserId);
                            }

                            Instructor NewInstructor = new Instructor();
                            NewInstructor.SetUserID(NewUserId);
                            NewInstructor.SetName(NameValue);
                            NewInstructor.SetEmail(EmailValue);
                            NewInstructor.SetQualification(QualificationValue);
                            NewInstructor.SetJoiningDate(JoiningDateValue);
                            NewInstructor.SetDepartment(DepartmentValue);
                            if (InstructorIdValue != null) {
                                NewInstructor.SetInstructorID(InstructorIdValue);
                            }

                            boolean InstructorAdded = InstructorHandler.AddInstructor(NewInstructor);
                            if (!InstructorAdded) {
                                throw new SQLException("Failed to insert instructor row.");
                            }
                        }
                        catch (SQLException ex) {
                            showError(ex);
                            return;
                        }
                        break;
                    }

                    case "ADMIN": {
                        break;
                    }
                }


                AddDialog.dispose();
                RefreshData();
                JOptionPane.showMessageDialog(this, "User created.", "Success", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ExceptionObject) {
                showError(ExceptionObject);
            }
        });

        AddDialog.setVisible(true);
    }

    private void showError(Exception ExceptionObject) {
        ExceptionObject.printStackTrace();
        JOptionPane.showMessageDialog(this, ExceptionObject.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    public void RefreshData() {
        try {
            AllUsers = LoadAllUsers();
        }
        catch (SQLException ExceptionObject) {
            showError(ExceptionObject);
            AllUsers = new ArrayList<>();
        }
        ApplyFilters();
        RoleChart.revalidate();
        RoleChart.repaint();
        RegChart.revalidate();
        RegChart.repaint();
    }

    private List<User> LoadAllUsers() throws SQLException {
        List<User> UserList = UserAuthenticationHandler.GetAllUsers();
        UserList.sort(Comparator.comparingInt(User::GetUserID));
        return UserList;
    }

    private static class UsersTableModel extends AbstractTableModel {
        private final String[] ColumnNames = {"UserID", "UserName", "Role", "Password", "Status", "Edit", "Delete"};
        private List<User> RowsList = new ArrayList<>();

        public void SetRows(List<User> NewRowsList) {
            this.RowsList = new ArrayList<>(NewRowsList);
            fireTableDataChanged();
        }

        public User GetRow(int ModelIndex) {
            return RowsList.get(ModelIndex);
        }

        @Override
        public int getRowCount() {
            return RowsList.size();
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
            User CurrentUser = RowsList.get(RowIndex);
            return switch (ColumnIndex) {
                case 0 -> CurrentUser.GetUserID();
                case 1 -> CurrentUser.GetUserName();
                case 2 -> CurrentUser.GetRole();
                case 3 -> "********";
                case 4 -> CurrentUser.GetStatus();
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
        public Component getTableCellRendererComponent(JTable Table, Object Value, boolean IsSelected,
                                                       boolean HasFocus, int Row, int Column) {
            setText(Value == null ? "" : Value.toString());
            return this;
        }
    }

    private static class ButtonCellEditor extends DefaultCellEditor {
        private final JButton Button = new JButton();
        private final java.util.function.IntConsumer ClickHandler;
        private int ModelRowIndex = -1;

        public ButtonCellEditor(String ButtonLabel, java.util.function.IntConsumer OnClickHandler) {
            super(new JTextField());
            this.ClickHandler = OnClickHandler;
            Button.setText(ButtonLabel);
            Button.setFocusPainted(false);

            Button.addActionListener(ClickEvent -> {
                final int ClickedRowIndex = ModelRowIndex;
                fireEditingStopped();
                SwingUtilities.invokeLater(() -> {
                    try {
                        if (ClickedRowIndex >= 0) {
                            ClickHandler.accept(ClickedRowIndex);
                        }
                    } catch (Exception ExceptionObject) {
                        ExceptionObject.printStackTrace();
                    }
                });
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable Table, Object Value, boolean IsSelected, int ViewRowIndex, int ColumnIndex) {
            this.ModelRowIndex = Table.convertRowIndexToModel(ViewRowIndex);
            Button.setText(Value == null ? "" : Value.toString());
            return Button;
        }

        @Override
        public Object getCellEditorValue() {
            return Button.getText();
        }
    }

    private class RoleBarChart extends JPanel {
        private Map<String, Integer> RoleCountsMap = new LinkedHashMap<>();

        public RoleBarChart() {
            setPreferredSize(new Dimension(440, 240));
            setMinimumSize(new Dimension(320, 180));
        }

        public void SetData(List<User> UserList) {
            RoleCountsMap.clear();
            RoleCountsMap.put("STUDENT", (int) UserList.stream().filter(UserItem -> Safe(UserItem.GetRole()).equalsIgnoreCase("STUDENT")).count());
            RoleCountsMap.put("INSTRUCTOR", (int) UserList.stream().filter(UserItem -> Safe(UserItem.GetRole()).equalsIgnoreCase("INSTRUCTOR")).count());
            RoleCountsMap.put("ADMIN", (int) UserList.stream().filter(UserItem -> Safe(UserItem.GetRole()).equalsIgnoreCase("ADMIN")).count());
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics GraphicsContext) {
            super.paintComponent(GraphicsContext);
            Graphics2D Graphics2DContext = (Graphics2D) GraphicsContext.create();
            Graphics2DContext.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int Width = getWidth();
            int Height = getHeight();
            int Padding = 24;
            int InnerWidth = Math.max(1, Width - Padding * 2);
            int InnerHeight = Math.max(1, Height - Padding * 2);
            Graphics2DContext.setColor(ColorBackground);
            Graphics2DContext.fillRoundRect(8, 8, Width - 16, Height - 16, 12, 12);

            if (RoleCountsMap.isEmpty()) {
                Graphics2DContext.setColor(ColorTextDim);
                Graphics2DContext.drawString("No data", Padding, Padding + 16);
                Graphics2DContext.dispose();
                return;
            }

            int MaxCount = Math.max(1, RoleCountsMap.values().stream().max(Integer::compareTo).orElse(1));
            int CategoryCount = RoleCountsMap.size();
            int Gap = 18;
            int BarWidth = Math.max(24, (InnerWidth - Gap * (CategoryCount - 1)) / CategoryCount);
            int BaseY = Height - Padding - 28;

            Graphics2DContext.setColor(ColorDivider);
            Graphics2DContext.drawLine(Padding, BaseY, Width - Padding, BaseY);

            int Index = 0;
            int BarX = Padding;
            for (Map.Entry<String, Integer> Entry : RoleCountsMap.entrySet()) {
                int Value = Entry.getValue();
                int BarHeight = (int) Math.round((InnerHeight - 60) * (Value / (double) MaxCount));
                BarHeight = Math.max(0, BarHeight);
                int BarY = BaseY - BarHeight;

                Graphics2DContext.setColor(ColorAccent);
                Graphics2DContext.fillRoundRect(BarX, BarY, BarWidth, BarHeight, 8, 8);

                Graphics2DContext.setColor(ColorText);
                String ValueString = String.valueOf(Value);
                int ValueStringWidth = Graphics2DContext.getFontMetrics().stringWidth(ValueString);
                Graphics2DContext.drawString(ValueString, BarX + (BarWidth - ValueStringWidth) / 2, BarY - 6);

                Graphics2DContext.setColor(ColorTextDim);
                String Label = Entry.getKey();
                int LabelWidth = Graphics2DContext.getFontMetrics().stringWidth(Label);
                Graphics2DContext.drawString(Label, BarX + (BarWidth - LabelWidth) / 2, BaseY + 16);

                BarX += BarWidth + Gap;
                Index++;
            }
            Graphics2DContext.dispose();
        }
    }

    private class RegistrationChart extends JPanel {
        private NavigableMap<LocalDate, Integer> RegistrationSeries = new TreeMap<>();

        public RegistrationChart() {
            setPreferredSize(new Dimension(440, 240));
            setMinimumSize(new Dimension(320, 180));
        }

        public void SetData(NavigableMap<LocalDate, Integer> CountsByDay) {
            RegistrationSeries = (CountsByDay == null) ? new TreeMap<>() : CountsByDay;
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics GraphicsContext) {
            super.paintComponent(GraphicsContext);
            Graphics2D Graphics2DContext = (Graphics2D) GraphicsContext.create();
            Graphics2DContext.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int Width = getWidth();
            int Height = getHeight();
            int Padding = 28;
            int Left = Padding + 8;
            int Right = Width - Padding;
            int Top = Padding;
            int Bottom = Height - Padding - 16;
            int InnerWidth = Math.max(1, Right - Left);
            int InnerHeight = Math.max(1, Bottom - Top);

            Graphics2DContext.setColor(ColorBackground);
            Graphics2DContext.fillRoundRect(8, 8, Width - 16, Height - 16, 12, 12);

            if (RegistrationSeries.isEmpty()) {
                Graphics2DContext.setColor(ColorTextDim);
                Graphics2DContext.drawString("No registration data", Left, Top + 16);
                Graphics2DContext.dispose();
                return;
            }

            int MaxCount = Math.max(1, RegistrationSeries.values().stream().max(Integer::compareTo).orElse(1));
            int PointCount = RegistrationSeries.size();
            int Step = (PointCount <= 1) ? InnerWidth : InnerWidth / (PointCount - 1);

            Graphics2DContext.setColor(ColorDivider);
            Graphics2DContext.drawLine(Left, Bottom, Right, Bottom);
            Graphics2DContext.drawLine(Left, Bottom, Left, Top);

            Graphics2DContext.setColor(ColorAccent);
            Graphics2DContext.setStroke(new BasicStroke(2f));

            int Index = 0;
            int PreviousX = -1;
            int PreviousY = -1;
            for (Map.Entry<LocalDate, Integer> Entry : RegistrationSeries.entrySet()) {
                int X = Left + Index * Step;
                double Ratio = Entry.getValue() / (double) MaxCount;
                int Y = Bottom - (int) Math.round((InnerHeight - 8) * Ratio);
                Y = Math.min(Bottom, Math.max(Top, Y));

                if (PreviousX >= 0) {
                    Graphics2DContext.drawLine(PreviousX, PreviousY, X, Y);
                }
                Graphics2DContext.fillOval(X - 3, Y - 3, 6, 6);
                PreviousX = X;
                PreviousY = Y;
                Index++;
            }

            Graphics2DContext.setColor(ColorTextDim);
            Index = 0;
            for (LocalDate DateValue : RegistrationSeries.keySet()) {
                int X = Left + Index * Step;
                String MonthLabel = DateValue.getMonth().toString().substring(0, 3);
                Graphics2DContext.drawString(MonthLabel, X - 10, Bottom + 14);
                Index++;
            }
            Graphics2DContext.dispose();
        }
    }

    private NavigableMap<LocalDate, Integer> FetchRegistrationCounts() {
        NavigableMap<LocalDate, Integer> CountsMap = new TreeMap<>();
        LocalDate StartDate = LocalDate.now().withDayOfMonth(1);
        for (int Index = 5; Index >= 0; Index--) {
            CountsMap.put(StartDate.minusMonths(Index), 5 + (int) (Math.random() * 20));
        }
        return CountsMap;
    }
}
