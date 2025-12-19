package Frontend.AdminPages;

import Backend.DataBaseHandler.SemestersHandler;
import Backend.DataBaseHandler.SemestersHandler.SemesterProfile;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.io.File;

public class AdminSettingsPanel extends JPanel {

    private static final Color COLOR_BACKGROUND = new Color(0x121212);
    private static final Color COLOR_PANEL      = new Color(0x1E1E1E);
    private static final Color COLOR_ACCENT     = new Color(0xB0F2B4);
    private static final Color COLOR_TEXT       = new Color(0xFFFFFF);
    private static final Color COLOR_TEXT_MUTED = new Color(0xB0B0B0);
    private static final Color COLOR_GRID       = new Color(0x2A2A2A);

    private JComboBox<Integer> YearComboBox;
    private JComboBox<String> SemesterComboBox;
    private DatePickerButton RegistrationStartDateButton;
    private DatePickerButton RegistrationEndDateButton;
    private DatePickerButton SemesterStartDateButton;
    private DatePickerButton SemesterEndDateButton;
    private ToggleSwitch MaintenanceToggleSwitch;

    private final Runnable OnMaintenanceChangeCallback;

    public AdminSettingsPanel(Runnable OnMaintenanceChangeCallback) {
        this.OnMaintenanceChangeCallback = OnMaintenanceChangeCallback;

        setLayout(new BorderLayout(20, 20));
        setBackground(COLOR_BACKGROUND);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel TitleLabel = new JLabel("System Administration");
        TitleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        TitleLabel.setForeground(COLOR_TEXT);
        add(TitleLabel, BorderLayout.NORTH);

        JPanel ContentGridPanel = new JPanel(new GridBagLayout());
        ContentGridPanel.setOpaque(false);

        GridBagConstraints Constraints = new GridBagConstraints();
        Constraints.gridx = 0;
        Constraints.gridy = 0;
        Constraints.weightx = 1.0;
        Constraints.weighty = 0.0;
        Constraints.fill = GridBagConstraints.HORIZONTAL;
        Constraints.insets = new Insets(0, 0, 20, 0);

        ContentGridPanel.add(CreateSemesterConfigPanel(), Constraints);

        Constraints.gridy = 1;
        ContentGridPanel.add(CreateMaintenancePanel(), Constraints);

        Constraints.gridy = 2;
        ContentGridPanel.add(CreateDatabaseBackupPanel(), Constraints);

        Constraints.gridy = 3;
        Constraints.weighty = 1.0;
        ContentGridPanel.add(Box.createVerticalGlue(), Constraints);

        add(ContentGridPanel, BorderLayout.CENTER);

        LoadCurrentSettings();
    }

    public AdminSettingsPanel() {
        this(() -> {});
    }

    private JPanel CreateSemesterConfigPanel() {
        JPanel ConfigPanel = new RoundedPanel(20);
        ConfigPanel.setBackground(COLOR_PANEL);
        ConfigPanel.setLayout(new BorderLayout(20, 20));
        ConfigPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel HeaderLabel = new JLabel("Academic Semester Setup");
        HeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        HeaderLabel.setForeground(COLOR_TEXT);
        ConfigPanel.add(HeaderLabel, BorderLayout.NORTH);

        JPanel FormPanel = new JPanel(new GridBagLayout());
        FormPanel.setOpaque(false);
        GridBagConstraints Constraints = new GridBagConstraints();
        Constraints.insets = new Insets(10, 10, 10, 10);
        Constraints.fill = GridBagConstraints.HORIZONTAL;
        Constraints.anchor = GridBagConstraints.WEST;

        Constraints.gridx = 0;
        Constraints.gridy = 0;
        FormPanel.add(CreateLabel("Academic Year"), Constraints);

        Constraints.gridx = 1;
        Integer[] YearValues = {2023, 2024, 2025, 2026, 2027};
        YearComboBox = new JComboBox<>(YearValues);
        StyleCombo(YearComboBox);
        FormPanel.add(YearComboBox, Constraints);

        Constraints.gridx = 2;
        FormPanel.add(CreateLabel("Semester"), Constraints);

        Constraints.gridx = 3;
        String[] SemesterValues = {"Spring", "Monsoon", "Winter", "Summer"};
        SemesterComboBox = new JComboBox<>(SemesterValues);
        StyleCombo(SemesterComboBox);
        FormPanel.add(SemesterComboBox, Constraints);

        Constraints.gridx = 0;
        Constraints.gridy = 1;
        FormPanel.add(CreateLabel("Registration Start"), Constraints);
        Constraints.gridx = 1;
        RegistrationStartDateButton = new DatePickerButton();
        FormPanel.add(RegistrationStartDateButton, Constraints);

        Constraints.gridx = 2;
        FormPanel.add(CreateLabel("Registration End"), Constraints);
        Constraints.gridx = 3;
        RegistrationEndDateButton = new DatePickerButton();
        FormPanel.add(RegistrationEndDateButton, Constraints);

        Constraints.gridx = 0;
        Constraints.gridy = 2;
        FormPanel.add(CreateLabel("Semester Start"), Constraints);
        Constraints.gridx = 1;
        SemesterStartDateButton = new DatePickerButton();
        FormPanel.add(SemesterStartDateButton, Constraints);

        Constraints.gridx = 2;
        FormPanel.add(CreateLabel("Semester End"), Constraints);
        Constraints.gridx = 3;
        SemesterEndDateButton = new DatePickerButton();
        FormPanel.add(SemesterEndDateButton, Constraints);

        ConfigPanel.add(FormPanel, BorderLayout.CENTER);

        JPanel FooterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        FooterPanel.setOpaque(false);

        JButton SaveSemesterConfigButton = new JButton("Update Semester Config");
        StyleButton(SaveSemesterConfigButton, COLOR_ACCENT, Color.BLACK);
        SaveSemesterConfigButton.addActionListener(Event -> SaveSemesterConfig());

        FooterPanel.add(SaveSemesterConfigButton);
        ConfigPanel.add(FooterPanel, BorderLayout.SOUTH);

        return ConfigPanel;
    }

    private JPanel CreateMaintenancePanel() {
        JPanel MaintenancePanel = new RoundedPanel(20);
        MaintenancePanel.setBackground(COLOR_PANEL);
        MaintenancePanel.setLayout(new BorderLayout(20, 20));
        MaintenancePanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel InfoPanel = new JPanel(new GridLayout(2, 1));
        InfoPanel.setOpaque(false);

        JLabel HeaderLabel = new JLabel("Maintenance Mode");
        HeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        HeaderLabel.setForeground(COLOR_TEXT);

        JLabel DescriptionLabel = new JLabel("When enabled, students and instructors cannot access the system.");
        DescriptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        DescriptionLabel.setForeground(COLOR_TEXT_MUTED);

        InfoPanel.add(HeaderLabel);
        InfoPanel.add(DescriptionLabel);
        MaintenancePanel.add(InfoPanel, BorderLayout.CENTER);

        MaintenanceToggleSwitch = new ToggleSwitch();
        MaintenanceToggleSwitch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent MouseEventInstance) {
                ToggleMaintenanceMode();
            }
        });

        JPanel SwitchContainerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        SwitchContainerPanel.setOpaque(false);
        SwitchContainerPanel.add(MaintenanceToggleSwitch);
        MaintenancePanel.add(SwitchContainerPanel, BorderLayout.EAST);

        return MaintenancePanel;
    }

    private void LoadCurrentSettings() {
        try {
            SemestersHandler.InitTable();
            boolean IsMaintenanceModeOn = SemestersHandler.IsMaintenanceMode();
            MaintenanceToggleSwitch.setOn(IsMaintenanceModeOn);

            SemesterProfile CurrentSemester = SemestersHandler.GetCurrentSemester();
            if (CurrentSemester != null) {
                if (CurrentSemester.Year > 0) {
                    YearComboBox.setSelectedItem(CurrentSemester.Year);
                }
                if (CurrentSemester.Semester != null) {
                    SemesterComboBox.setSelectedItem(CurrentSemester.Semester);
                }
                RegistrationStartDateButton.SetDate(ParseDate(CurrentSemester.RegistrationStartDate));
                RegistrationEndDateButton.SetDate(ParseDate(CurrentSemester.RegistrationEndDate));
                SemesterStartDateButton.SetDate(ParseDate(CurrentSemester.StartDate));
                SemesterEndDateButton.SetDate(ParseDate(CurrentSemester.EndDate));
            }
        } catch (Exception ExceptionObject) {
            ExceptionObject.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load settings: " + ExceptionObject.getMessage());
        }
    }

    private void SaveSemesterConfig() {
        try {
            int SelectedYear = (Integer) YearComboBox.getSelectedItem();
            String SelectedSemester = (String) SemesterComboBox.getSelectedItem();
            String RegistrationStartDate = RegistrationStartDateButton.GetSelectedDateString();
            String RegistrationEndDate = RegistrationEndDateButton.GetSelectedDateString();
            String SemesterStartDate = SemesterStartDateButton.GetSelectedDateString();
            String SemesterEndDate = SemesterEndDateButton.GetSelectedDateString();

            SemesterProfile SemesterConfigurationProfile =
                    new SemesterProfile(SelectedYear, SelectedSemester, SemesterStartDate, SemesterEndDate, RegistrationStartDate, RegistrationEndDate);

            boolean IsUpdateSuccessful = SemestersHandler.UpdateCurrentSemester(SemesterConfigurationProfile);
            SemestersHandler.AddSemesterToHistory(SemesterConfigurationProfile);

            if (IsUpdateSuccessful) {
                JOptionPane.showMessageDialog(this, "Semester settings updated successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Update failed (No rows affected).");
            }
        } catch (Exception ExceptionObject) {
            JOptionPane.showMessageDialog(this, "Error saving config: " + ExceptionObject.getMessage());
        }
    }

    private void ToggleMaintenanceMode() {
        boolean NewMaintenanceState = !MaintenanceToggleSwitch.isOn();

        try {
            boolean IsUpdateSuccessful = SemestersHandler.SetMaintenanceMode(NewMaintenanceState);

            if (IsUpdateSuccessful) {
                MaintenanceToggleSwitch.setOn(NewMaintenanceState);
                if (NewMaintenanceState) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Maintenance Mode ENABLED.\nUsers will be locked out.",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

                if (OnMaintenanceChangeCallback != null) {
                    OnMaintenanceChangeCallback.run();
                }

            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to update maintenance mode.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (Exception ExceptionObject) {
            JOptionPane.showMessageDialog(
                    this,
                    "Database Error: " + ExceptionObject.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private LocalDate ParseDate(String DateString) {
        if (DateString == null || DateString.isEmpty()) return LocalDate.now();
        try {
            return LocalDate.parse(DateString);
        } catch (DateTimeParseException ExceptionObject) {
            return LocalDate.now();
        }
    }

    private class DatePickerButton extends JButton {
        private LocalDate SelectedDate;
        private final DateTimeFormatter DateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        public DatePickerButton() {
            this.SelectedDate = LocalDate.now();
            setText(SelectedDate.format(DateFormatter));
            setFont(new Font("SansSerif", Font.PLAIN, 14));
            setBackground(new Color(0x2A2A2A));
            setForeground(COLOR_TEXT);
            setFocusPainted(false);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x444444)),
                    new EmptyBorder(5, 10, 5, 10)
            ));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(150, 35));

            addActionListener(Event -> {
                Point ButtonScreenLocation = getLocationOnScreen();
                new CalendarPopup(SwingUtilities.getWindowAncestor(this), this,SelectedDate).ShowPopup(ButtonScreenLocation.x, ButtonScreenLocation.y + getHeight());
            });
        }

        public void SetDate(LocalDate DateValue) {
            this.SelectedDate = DateValue;
            setText(DateValue.format(DateFormatter));
        }

        public String GetSelectedDateString() {
            return SelectedDate.format(DateFormatter);
        }
    }

    private JPanel CreateDatabaseBackupPanel() {
        JPanel BackupPanel = new RoundedPanel(20);
        BackupPanel.setBackground(COLOR_PANEL);
        BackupPanel.setLayout(new BorderLayout(20, 20));
        BackupPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JPanel InfoPanel = new JPanel(new GridLayout(2, 1));
        InfoPanel.setOpaque(false);

        JLabel HeaderLabel = new JLabel("Database Backup & Restore");
        HeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        HeaderLabel.setForeground(COLOR_TEXT);

        JLabel DescriptionLabel = new JLabel("Backup current data or restore from a previous SQL snapshot (structure remains intact).");
        DescriptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        DescriptionLabel.setForeground(COLOR_TEXT_MUTED);

        InfoPanel.add(HeaderLabel);
        InfoPanel.add(DescriptionLabel);
        BackupPanel.add(InfoPanel, BorderLayout.CENTER);

        JButton BackupButton = new JButton("Backup Database");
        StyleButton(BackupButton, COLOR_ACCENT, Color.BLACK);

        JButton RestoreButton = new JButton("Restore from SQL ");
        StyleButton(RestoreButton, new Color(0x333333), COLOR_TEXT);

        BackupButton.setMaximumSize(new Dimension(200, 40));
        RestoreButton.setMaximumSize(new Dimension(200, 40));
        BackupButton.addActionListener(Event -> ChooseBackupLocation());
        RestoreButton.addActionListener(Event -> ChooseRestoreFile());
        JPanel ActionsPanel = new JPanel();
        ActionsPanel.setOpaque(false);
        ActionsPanel.setLayout(new BoxLayout(ActionsPanel, BoxLayout.Y_AXIS));

        BackupButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        RestoreButton.setAlignmentX(Component.RIGHT_ALIGNMENT);

        ActionsPanel.add(BackupButton);
        ActionsPanel.add(Box.createVerticalStrut(10));
        ActionsPanel.add(RestoreButton);

        BackupPanel.add(ActionsPanel, BorderLayout.EAST);
        return BackupPanel;
    }



    private void ShowBackupFormatChooser() {
        ChooseBackupLocation();
    }

    private void PerformBackup(File BackupFile) {
        try {
            Backend.util.DBBackup.BackUpAsSQL(BackupFile);
            JOptionPane.showMessageDialog(this, "Backup completed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            Backend.util.AdminUIReload.ReloadAll();
        }
        catch (Exception ExceptionObject) {
            JOptionPane.showMessageDialog(this, "Backup failed: " + ExceptionObject.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void PerformRestore(File BackupFile) {
        try {
            Backend.util.DBBackup.RestoreFromSQL(BackupFile);
            JOptionPane.showMessageDialog(this, "Database restored successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            Backend.util.AdminUIReload.ReloadAll();
        }
        catch (Exception ExceptionObject) {
            JOptionPane.showMessageDialog(this, "Restore failed: " + ExceptionObject.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ChooseBackupLocation() {
        JFileChooser FileChooserInstance = new JFileChooser();
        FileChooserInstance.setDialogTitle("Choose SQL backup file destination");
        FileChooserInstance.setSelectedFile(new File("erp_backup_" + java.time.LocalDate.now() + ".sql"));
        int DialogResult = FileChooserInstance.showSaveDialog(this);
        if (DialogResult == JFileChooser.APPROVE_OPTION) {
            File TargetFile = FileChooserInstance.getSelectedFile();
            PerformBackup(TargetFile);
        }
    }

    private void ChooseRestoreFile() {
        JFileChooser FileChooserInstance = new JFileChooser();
        FileChooserInstance.setDialogTitle("Select SQL backup file to restore");
        FileChooserInstance.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQL Files", "sql"));
        int DialogResult = FileChooserInstance.showOpenDialog(this);
        if (DialogResult == JFileChooser.APPROVE_OPTION) {
            File BackupFile = FileChooserInstance.getSelectedFile();
            PerformRestore(BackupFile);
        }
    }


    private class CalendarPopup extends JDialog {
        private final DatePickerButton ParentDatePickerButton;
        private YearMonth CurrentMonth;
        private LocalDate SelectedDate;
        private JPanel DaysPanel;
        private JLabel MonthLabel;

        public CalendarPopup(Window OwnerWindow, DatePickerButton ParentDatePickerButton, LocalDate InitialDate) {
            super(OwnerWindow, Dialog.ModalityType.APPLICATION_MODAL);
            this.ParentDatePickerButton = ParentDatePickerButton;
            this.SelectedDate = InitialDate;
            this.CurrentMonth = YearMonth.from(InitialDate);

            setUndecorated(true);
            setSize(300, 300);
            setLayout(new BorderLayout());

            JPanel MainPanel = new JPanel(new BorderLayout(10, 10));
            MainPanel.setBackground(COLOR_PANEL);
            MainPanel.setBorder(BorderFactory.createLineBorder(COLOR_ACCENT, 1));

            JPanel HeaderPanel = new JPanel(new BorderLayout());
            HeaderPanel.setOpaque(false);
            HeaderPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

            JButton PreviousMonthButton = CreateNavButton("<");
            PreviousMonthButton.addActionListener(Event -> ChangeMonth(-1));
            JButton NextMonthButton = CreateNavButton(">");
            NextMonthButton.addActionListener(Event -> ChangeMonth(1));

            MonthLabel = new JLabel("", SwingConstants.CENTER);
            MonthLabel.setForeground(COLOR_TEXT);
            MonthLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

            HeaderPanel.add(PreviousMonthButton, BorderLayout.WEST);
            HeaderPanel.add(MonthLabel, BorderLayout.CENTER);
            HeaderPanel.add(NextMonthButton, BorderLayout.EAST);
            MainPanel.add(HeaderPanel, BorderLayout.NORTH);

            DaysPanel = new JPanel(new GridLayout(0, 7, 2, 2));
            DaysPanel.setOpaque(false);
            DaysPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            MainPanel.add(DaysPanel, BorderLayout.CENTER);
            add(MainPanel);
            RefreshCalendar();
        }

        public void ShowPopup(int XPosition, int YPosition) {
            setLocation(XPosition, YPosition);
            setVisible(true);
        }

        private void ChangeMonth(int MonthOffset) {
            CurrentMonth = CurrentMonth.plusMonths(MonthOffset);
            RefreshCalendar();
        }

        private void RefreshCalendar() {
            MonthLabel.setText(CurrentMonth.getMonth().toString() + " " + CurrentMonth.getYear());
            DaysPanel.removeAll();
            String[] DayNames = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
            for (String DayName : DayNames) {
                JLabel DayLabel = new JLabel(DayName, SwingConstants.CENTER);
                DayLabel.setForeground(COLOR_TEXT_MUTED);
                DayLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
                DaysPanel.add(DayLabel);
            }
            LocalDate FirstDayOfMonth = CurrentMonth.atDay(1);
            int StartDayOfWeek = FirstDayOfMonth.getDayOfWeek().getValue() % 7;
            int DaysInMonth = CurrentMonth.lengthOfMonth();
            for (int Index = 0; Index < StartDayOfWeek; Index++) {
                DaysPanel.add(new JLabel(""));
            }

            for (int DayNumber = 1; DayNumber <= DaysInMonth; DayNumber++) {
                JButton DayButton = new JButton(String.valueOf(DayNumber));
                DayButton.setFocusPainted(false);
                DayButton.setBorder(null);
                DayButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
                LocalDate CurrentDate = CurrentMonth.atDay(DayNumber);
                if (CurrentDate.equals(SelectedDate)) {
                    DayButton.setBackground(COLOR_ACCENT);
                    DayButton.setForeground(Color.BLACK);
                    DayButton.setFont(new Font("SansSerif", Font.BOLD, 12));
                } else if (CurrentDate.equals(LocalDate.now())) {
                    DayButton.setBackground(new Color(0x333333));
                    DayButton.setForeground(COLOR_ACCENT);
                    DayButton.setBorder(BorderFactory.createLineBorder(COLOR_ACCENT));
                } else {
                    DayButton.setBackground(COLOR_BACKGROUND);
                    DayButton.setForeground(COLOR_TEXT);
                }
                DayButton.addActionListener(Event -> {
                    ParentDatePickerButton.SetDate(CurrentDate);
                    dispose();
                });
                DaysPanel.add(DayButton);
            }
            DaysPanel.revalidate();
            DaysPanel.repaint();
        }

        private JButton CreateNavButton(String ButtonText) {
            JButton NavigationButton = new JButton(ButtonText);
            NavigationButton.setFocusPainted(false);
            NavigationButton.setContentAreaFilled(false);
            NavigationButton.setBorder(null);
            NavigationButton.setForeground(COLOR_TEXT);
            NavigationButton.setFont(new Font("SansSerif", Font.BOLD, 16));
            NavigationButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            return NavigationButton;
        }
    }

    private JLabel CreateLabel(String LabelText) {
        JLabel LabelInstance = new JLabel(LabelText);
        LabelInstance.setFont(new Font("SansSerif", Font.BOLD, 14));
        LabelInstance.setForeground(COLOR_TEXT_MUTED);
        return LabelInstance;
    }

    private void StyleCombo(JComboBox<?> ComboBoxInstance) {
        ComboBoxInstance.setBackground(new Color(0x2A2A2A));
        ComboBoxInstance.setForeground(COLOR_TEXT);
        ComboBoxInstance.setFont(new Font("SansSerif", Font.PLAIN, 14));
        ComboBoxInstance.setPreferredSize(new Dimension(150, 35));
    }

    private void StyleButton(JButton ButtonInstance, Color BackgroundColor, Color ForegroundColor) {
        ButtonInstance.setBackground(BackgroundColor);
        ButtonInstance.setForeground(ForegroundColor);
        ButtonInstance.setFocusPainted(false);
        ButtonInstance.setFont(new Font("SansSerif", Font.BOLD, 14));
        ButtonInstance.setBorder(new EmptyBorder(10, 20, 10, 20));
        ButtonInstance.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private static class RoundedPanel extends JPanel {
        private int CornerRadius;

        RoundedPanel(int CornerRadius) {
            this.CornerRadius = CornerRadius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics GraphicsInstance) {
            Graphics2D Graphics2DInstance = (Graphics2D) GraphicsInstance.create();
            Graphics2DInstance.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Graphics2DInstance.setColor(getBackground());
            Graphics2DInstance.fillRoundRect(0, 0, getWidth(), getHeight(), CornerRadius, CornerRadius);
            Graphics2DInstance.dispose();
            super.paintComponent(GraphicsInstance);
        }
    }

    private static class ToggleSwitch extends JPanel {
        private boolean IsOn = false;

        public ToggleSwitch() {
            setPreferredSize(new Dimension(60, 30));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setOpaque(false);
        }

        public boolean isOn() {
            return IsOn;
        }

        public void setOn(boolean IsOn) {
            this.IsOn = IsOn;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics GraphicsInstance) {
            Graphics2D Graphics2DInstance = (Graphics2D) GraphicsInstance.create();
            Graphics2DInstance.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Graphics2DInstance.setColor(IsOn ? COLOR_ACCENT : new Color(0x444444));
            Graphics2DInstance.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            Graphics2DInstance.setColor(Color.WHITE);
            int KnobSize = getHeight() - 8;
            int KnobPositionX = IsOn ? (getWidth() - KnobSize - 4) : 4;
            Graphics2DInstance.fillOval(KnobPositionX, 4, KnobSize, KnobSize);
            Graphics2DInstance.dispose();
        }
    }
}
