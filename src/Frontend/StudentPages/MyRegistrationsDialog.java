package Frontend.StudentPages;

import Backend.DataBaseHandler.CourseHandler;
import Backend.DataBaseHandler.InstructorHandler;
import Backend.DataBaseHandler.OfferingHandler;
import Backend.DataBaseHandler.RegistrationHandler;
import Backend.domain.Course;
import Backend.domain.Offerings;
import Backend.domain.Registration;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class MyRegistrationsDialog extends JDialog {
    private static final Color COLOR_BACKGROUND   = new Color(0x121212);
    private static final Color COLOR_TEXT_PRIMARY = new Color(0xFFFFFF);
    private static final Color COLOR_TEXT_SECONDARY = new Color(0xB0B0B0);
    private static final Color COLOR_PANEL        = new Color(0x1E1E1E);

    private final int StudentRollNumber;

    private final JComboBox<String> SemesterFilterCombo = new JComboBox<>(new String[]{
            "All Semesters", "SPRING", "SUMMER", "FALL", "WINTER"
    });
    private final JComboBox<String> YearFilterCombo = new JComboBox<>();

    private final DefaultTableModel RegistrationsTableModel = new DefaultTableModel(
            new String[]{"OfferingID", "Code", "Title", "Professor", "Credits", "Status", "Semester", "Year"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    private final JTable RegistrationsTable = new JTable(RegistrationsTableModel);
    private final JLabel CreditSummaryLabel = new JLabel();

    private final JButton RefreshButton = new JButton("Refresh");
    private final JButton DropButton    = new JButton("Drop Selected");
    private final JButton LockButton    = new JButton("Lock Choices");
    private final JButton PrintButton   = new JButton("Print");

    private List<RegistrationHandler.RegRow> AllRegistrationRows = new ArrayList<>();

    public MyRegistrationsDialog(Window OwnerWindow, int StudentRollNumber) {
        super(OwnerWindow, "My Registrations", ModalityType.APPLICATION_MODAL);
        this.StudentRollNumber = StudentRollNumber;

        setSize(880, 560);
        setLocationRelativeTo(OwnerWindow);
        getContentPane().setBackground(COLOR_BACKGROUND);
        setLayout(new BorderLayout(10, 10));

        JPanel TopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        TopPanel.setOpaque(false);

        YearFilterCombo.addItem("All Years");
        int CurrentYear = LocalDate.now().getYear();
        for (int Year = CurrentYear; Year >= CurrentYear - 4; Year--) {
            YearFilterCombo.addItem(String.valueOf(Year));
        }

        JLabel SemesterLabel = new JLabel("Semester");
        SemesterLabel.setForeground(COLOR_TEXT_SECONDARY);
        JLabel YearLabel = new JLabel("Year");
        YearLabel.setForeground(COLOR_TEXT_SECONDARY);

        TopPanel.add(SemesterLabel);
        TopPanel.add(SemesterFilterCombo);
        TopPanel.add(YearLabel);
        TopPanel.add(YearFilterCombo);

        TopPanel.add(RefreshButton);
        TopPanel.add(DropButton);
        TopPanel.add(LockButton);
        TopPanel.add(PrintButton);

        JScrollPane ScrollPane = new JScrollPane(RegistrationsTable);
        RegistrationsTable.setRowHeight(28);
        StyleTable();

        CreditSummaryLabel.setForeground(COLOR_TEXT_PRIMARY);
        CreditSummaryLabel.setBorder(new EmptyBorder(6, 12, 6, 12));

        add(TopPanel, BorderLayout.NORTH);
        add(ScrollPane, BorderLayout.CENTER);
        add(CreditSummaryLabel, BorderLayout.SOUTH);

        RefreshButton.addActionListener(e -> ReloadFromDB());
        SemesterFilterCombo.addActionListener(e -> Refilter());
        YearFilterCombo.addActionListener(e -> Refilter());

        DropButton.addActionListener(e -> {
            int SelectedRow = RegistrationsTable.getSelectedRow();
            if (SelectedRow < 0) {
                JOptionPane.showMessageDialog(this, "Select a row.");
                return;
            }
            int ModelRow = RegistrationsTable.convertRowIndexToModel(SelectedRow);
            Object OfferingIdObject = RegistrationsTableModel.getValueAt(ModelRow, 0);
            if (!(OfferingIdObject instanceof Integer)) {
                JOptionPane.showMessageDialog(this, "Invalid row.");
                return;
            }
            int OfferingId = (int) OfferingIdObject;
            try {
                boolean DropOk = RegistrationHandler.DropByStudentAndOffering(StudentRollNumber, OfferingId);
                if (!DropOk) {
                    JOptionPane.showMessageDialog(this, "Drop failed.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Drop failed: " + ex.getMessage());
            }
            ReloadFromDB();
        });

        LockButton.addActionListener(e -> {
            String SelectedSemester = Objects.toString(SemesterFilterCombo.getSelectedItem(), "All Semesters");
            String SelectedYearText = Objects.toString(YearFilterCombo.getSelectedItem(), "All Years");

            if ("All Semesters".equalsIgnoreCase(SelectedSemester) || "All Years".equalsIgnoreCase(SelectedYearText)) {
                JOptionPane.showMessageDialog(this, "Pick a specific semester and year to lock.");
                return;
            }
            int SelectedYear;
            try {
                SelectedYear = Integer.parseInt(SelectedYearText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid year selected.");
                return;
            }

            try {
                int RequiredCredits = 20;
                int TotalCredits = RegistrationHandler.SumCreditsForTerm(StudentRollNumber, SelectedSemester, SelectedYear);
                if (TotalCredits != RequiredCredits) {
                    JOptionPane.showMessageDialog(this, "Need exactly " + RequiredCredits + " credits. Current: " + TotalCredits + ".");
                    return;
                }
                boolean LockOk = RegistrationHandler.LockRegistrations(StudentRollNumber, SelectedSemester, SelectedYear);
                if (!LockOk) {
                    JOptionPane.showMessageDialog(this, "Lock failed.");
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Lock failed: " + ex.getMessage());
            }
            ReloadFromDB();
        });

        PrintButton.addActionListener(e -> {
            try {
                String HeaderText = "Registrations - " + SemesterFilterCombo.getSelectedItem() + " " + YearFilterCombo.getSelectedItem();
                boolean Printed = RegistrationsTable.print(
                        JTable.PrintMode.FIT_WIDTH,
                        new MessageFormat(HeaderText),
                        null
                );
                if (!Printed) {
                    JOptionPane.showMessageDialog(this, "Print canceled.");
                }
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage());
            }
        });

        ReloadFromDB();
    }

    private void StyleTable() {
        RegistrationsTable.setBackground(COLOR_PANEL);
        RegistrationsTable.setForeground(COLOR_TEXT_PRIMARY);
        RegistrationsTable.setFillsViewportHeight(true);
        RegistrationsTable.setShowHorizontalLines(false);
        RegistrationsTable.setShowVerticalLines(false);
        RegistrationsTable.setGridColor(COLOR_BACKGROUND.darker());
        RegistrationsTable.setSelectionBackground(new Color(0x333333));
        RegistrationsTable.setSelectionForeground(COLOR_TEXT_PRIMARY);

        RegistrationsTable.getTableHeader().setBackground(COLOR_PANEL);
        RegistrationsTable.getTableHeader().setForeground(COLOR_TEXT_SECONDARY);
        RegistrationsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        RegistrationsTable.getColumnModel().getColumn(0).setMinWidth(0);
        RegistrationsTable.getColumnModel().getColumn(0).setMaxWidth(0);
        RegistrationsTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        DefaultTableCellRenderer CenterRenderer = new DefaultTableCellRenderer();
        CenterRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        RegistrationsTable.getColumnModel().getColumn(4).setCellRenderer(CenterRenderer);
        RegistrationsTable.getColumnModel().getColumn(5).setCellRenderer(CenterRenderer);
        RegistrationsTable.getColumnModel().getColumn(6).setCellRenderer(CenterRenderer);
        RegistrationsTable.getColumnModel().getColumn(7).setCellRenderer(CenterRenderer);
    }

    private Integer GetSelectedYearOrNull() {
        String SelectedYearText = Objects.toString(YearFilterCombo.getSelectedItem(), "All Years");
        if ("All Years".equalsIgnoreCase(SelectedYearText)) {
            return null;
        }
        try {
            return Integer.parseInt(SelectedYearText);
        } catch (Exception e) {
            return null;
        }
    }

    private void ReloadFromDB() {
        SwingUtilities.invokeLater(() -> {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new SwingWorker<List<RegistrationHandler.RegRow>, Void>() {
                @Override
                protected List<RegistrationHandler.RegRow> doInBackground() {
                    try {
                        List<Registration> RegistrationList = RegistrationHandler.ListByStudentRollNumber(StudentRollNumber);
                        if (RegistrationList.isEmpty()) {
                            return new ArrayList<>();
                        }

                        List<Integer> OfferingIds = new ArrayList<>();
                        for (Registration RegistrationRow : RegistrationList) {
                            OfferingIds.add(RegistrationRow.GetOfferingID());
                        }

                        List<Offerings> OfferingsList = OfferingHandler.FindOfferingsByIDs(OfferingIds);
                        Map<Integer, Offerings> OfferingsByIdMap = new HashMap<>();
                        for (Offerings OfferingRow : OfferingsList) {
                            OfferingsByIdMap.put(OfferingRow.GetOfferingID(), OfferingRow);
                        }

                        Map<Integer, Course> CourseCache = new HashMap<>();
                        Map<Integer, Backend.domain.Instructor> InstructorCache = new HashMap<>();
                        List<RegistrationHandler.RegRow> ResultRows = new ArrayList<>();

                        for (Registration RegistrationRow : RegistrationList) {
                            Offerings CurrentOffering = OfferingsByIdMap.get(RegistrationRow.GetOfferingID());
                            if (CurrentOffering == null) {
                                continue;
                            }

                            Course CurrentCourse = CourseCache.computeIfAbsent(CurrentOffering.GetCourseID(), Key -> {
                                try {
                                    return CourseHandler.FindCourseByID(Key);
                                } catch (SQLException e) {
                                    return null;
                                }
                            });

                            Backend.domain.Instructor CurrentInstructor = InstructorCache.computeIfAbsent(CurrentOffering.GetInstructorID(), Key -> {
                                try {
                                    return InstructorHandler.FindInstructorByInstructorID(Key);
                                } catch (SQLException e) {
                                    return null;
                                }
                            });

                            RegistrationHandler.RegRow NewRow = new RegistrationHandler.RegRow();
                            NewRow.OfferingID = CurrentOffering.GetOfferingID();
                            NewRow.Code = (CurrentCourse != null && CurrentCourse.GetCode() != null)
                                    ? CurrentCourse.GetCode()
                                    : null;
                            NewRow.Title = (CurrentCourse != null && CurrentCourse.GetTitle() != null)
                                    ? CurrentCourse.GetTitle()
                                    : null;
                            NewRow.Credits = (CurrentCourse != null) ? CurrentCourse.GetCredits() : 0;
                            NewRow.Professor = (CurrentInstructor != null && CurrentInstructor.GetName() != null)
                                    ? CurrentInstructor.GetName()
                                    : "TBA";
                            NewRow.Status = RegistrationRow.GetStatus();
                            NewRow.Semester = CurrentOffering.GetSemester();
                            NewRow.Year = CurrentOffering.GetYear();

                            ResultRows.add(NewRow);
                        }
                        return ResultRows;
                    } catch (SQLException e) {
                        return new ArrayList<>();
                    }
                }

                @Override
                protected void done() {
                    try {
                        AllRegistrationRows = get();
                    }
                    catch (Exception ex) {
                        AllRegistrationRows = new ArrayList<>();
                    }
                    Refilter();
                    setCursor(Cursor.getDefaultCursor());
                }
            }.execute();
        });
    }

    private void Refilter() {
        String SelectedSemester = Objects.toString(SemesterFilterCombo.getSelectedItem(), "All Semesters");
        Integer SelectedYear = GetSelectedYearOrNull();

        RegistrationsTableModel.setRowCount(0);
        int TotalCreditsForTerm = 0;
        boolean IsLockedTerm = false;

        for (RegistrationHandler.RegRow RegistrationRow : AllRegistrationRows) {
            boolean SemesterMatches = "All Semesters".equalsIgnoreCase(SelectedSemester)
                    || SelectedSemester.equalsIgnoreCase(RegistrationRow.Semester);
            boolean YearMatches = (SelectedYear == null) || (SelectedYear == RegistrationRow.Year);
            if (!SemesterMatches || !YearMatches) {
                continue;
            }

            RegistrationsTableModel.addRow(new Object[]{RegistrationRow.OfferingID, RegistrationRow.Code, RegistrationRow.Title, RegistrationRow.Professor, RegistrationRow.Credits, RegistrationRow.Status, RegistrationRow.Semester, RegistrationRow.Year
            });

            if ("ENROLLED".equalsIgnoreCase(RegistrationRow.Status) || "LOCKED".equalsIgnoreCase(RegistrationRow.Status)) {
                TotalCreditsForTerm += RegistrationRow.Credits;
            }
        }

        if (!"All Semesters".equalsIgnoreCase(SelectedSemester) && SelectedYear != null) {
            try {
                IsLockedTerm = RegistrationHandler.IsTermLocked(StudentRollNumber, SelectedSemester, SelectedYear);
            }
            catch (SQLException ignored) { }
        }

        String CreditsText = ("All Semesters".equalsIgnoreCase(SelectedSemester) || SelectedYear == null) ? "-" : (TotalCreditsForTerm + " / 20");

        CreditSummaryLabel.setText("Total Credits (selected term): " + CreditsText + (IsLockedTerm ? "  (LOCKED)" : ""));
    }
}
