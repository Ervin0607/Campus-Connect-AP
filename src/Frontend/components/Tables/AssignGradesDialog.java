package Frontend.components.Tables;

import Backend.DataBaseHandler.*;
import Backend.domain.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignGradesDialog extends JDialog {

    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel = new Color(0x1E1E1E);
    private static final Color ColorTextPrimary = new Color(0xFFFFFF);
    private static final Color ColorTextSecondary = new Color(0xB0B0B0);
    private static final Color ColorAccent = new Color(0xB0F2B4);

    private final GradesTableModel TableModel;
    private List<List<Object>> OldData;
    private TableRowSorter<GradesTableModel> Sorter;
    private boolean IsPublished = false;
    private final Offerings CurrentOffering;
    private final String CourseName;

    public AssignGradesDialog(Frame Owner, String CourseName, Offerings Offering) {
        super(Owner, "Assign Grades - " + CourseName, true);
        this.CurrentOffering = Offering;
        this.CourseName = CourseName;

        setSize(900, 650);
        setLocationRelativeTo(Owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ColorBackground);

        try {
            this.IsPublished = RegistrationHandler.isOfferingPublished(Offering.GetOfferingID());
        }
        catch (SQLException e) { e.printStackTrace(); }

        JPanel HeaderPanel = new JPanel(new BorderLayout(0, 10));
        HeaderPanel.setOpaque(false);
        HeaderPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel TitleLabel = new JLabel("Student Grades", SwingConstants.CENTER);
        TitleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        TitleLabel.setForeground(ColorTextPrimary);
        HeaderPanel.add(TitleLabel, BorderLayout.NORTH);

        JTextField SearchField = new JTextField();
        SearchField.putClientProperty("JTextField.placeholderText", "Search by Name or Roll No...");
        SearchField.setBackground(ColorPanel);
        SearchField.setForeground(ColorTextPrimary);
        SearchField.setCaretColor(ColorTextSecondary);
        SearchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        SearchField.setBorder(new EmptyBorder(10, 15, 10, 15));

        JPanel SearchContainer = new JPanel(new BorderLayout());
        SearchContainer.setOpaque(false);
        SearchContainer.setBorder(new EmptyBorder(0, 150, 0, 150));
        SearchContainer.add(SearchField, BorderLayout.CENTER);

        HeaderPanel.add(SearchContainer, BorderLayout.SOUTH);
        add(HeaderPanel, BorderLayout.NORTH);

        HashMap<String, Integer> Components = Offering.GetGradingComponents();
        String[] ComponentNames = Components.keySet().toArray(new String[0]);

        String[] Columns = new String[3 + Components.size()];
        Columns[0] = "Roll No";
        Columns[1] = "Name";
        for (int i = 0; i < Components.size(); i++) {
            Columns[i + 2] = ComponentNames[i] + " (" + Components.get(ComponentNames[i]) + ")";
        }
        Columns[Columns.length - 1] = "Total (100)";

        List<List<Object>> DataRows = new ArrayList<>();
        try {
            List<StudentGradeRecord> GradeRecords = StudentGradeRecordHandler.FindByOfferingID(Offering.GetOfferingID());
            for (StudentGradeRecord Record : GradeRecords) {
                DataRows.add(CreateRowFromRecord(Record, ComponentNames));
            }
        }
        catch (SQLException e){
            throw new RuntimeException(e);
        }

        TableModel = new GradesTableModel(Columns, DataRows);
        JTable GradesTable = new JTable(TableModel);
        Sorter = new TableRowSorter<>(TableModel);
        GradesTable.setRowSorter(Sorter);

        SearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { Filter(); }
            @Override public void removeUpdate(DocumentEvent e) { Filter(); }
            @Override public void changedUpdate(DocumentEvent e) { Filter(); }
            private void Filter() {
                String Text = SearchField.getText();
                if (Text.trim().length() == 0) Sorter.setRowFilter(null);
                else Sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Text));
            }
        });

        GradesTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        GradesTable.setForeground(ColorTextPrimary);
        GradesTable.setBackground(ColorPanel);
        GradesTable.setGridColor(ColorBackground.darker());
        GradesTable.setRowHeight(35);
        GradesTable.setSelectionBackground(new Color(0x3B82F6));
        GradesTable.setSelectionForeground(Color.WHITE);
        GradesTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        GradesTable.getTableHeader().setBackground(ColorPanel);
        GradesTable.getTableHeader().setForeground(ColorTextSecondary);
        GradesTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorTextSecondary));

        JScrollPane ScrollPane = new JScrollPane(GradesTable);
        ScrollPane.getViewport().setBackground(ColorPanel);
        ScrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel TableContainer = new JPanel(new BorderLayout());
        TableContainer.setOpaque(false);
        TableContainer.setBorder(new EmptyBorder(0, 20, 0, 20));
        TableContainer.add(ScrollPane, BorderLayout.CENTER);
        add(TableContainer, BorderLayout.CENTER);

        JPanel ButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        ButtonPanel.setOpaque(false);
        ButtonPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JButton PublishButton = new JButton("Publish Grades");
        JButton EditButton = new JButton("Edit Grades");
        JButton SaveButton = new JButton("Save Changes");
        JButton CancelButton = new JButton("Cancel");
        JButton ExportButton = new JButton("Export CSV");
        JButton ImportButton = new JButton("Import CSV");
        JButton CloseButton = new JButton("Close");

        SaveButton.setVisible(false);
        CancelButton.setVisible(false);

        ImportButton.setFont(new Font("SansSerif", Font.BOLD, 12));

        ExportButton.setFont(new Font("SansSerif", Font.BOLD, 12));

        PublishButton.setBackground(ColorAccent);
        PublishButton.setForeground(Color.BLACK);
        PublishButton.setFont(new Font("SansSerif", Font.BOLD, 12));

        if (IsPublished) {
            PublishButton.setText("Grades Published");
            PublishButton.setEnabled(false);
            PublishButton.setBackground(ColorPanel);
            PublishButton.setForeground(ColorTextSecondary);
        }

        try {
            if (SemestersHandler.IsMaintenanceMode()) {
                EditButton.setEnabled(false);
                EditButton.setToolTipText("Editing disabled: System is under maintenance.");
                ImportButton.setEnabled(false);
                if (PublishButton.isEnabled()) {
                    PublishButton.setEnabled(false);
                    PublishButton.setToolTipText("Publishing disabled: System is under maintenance.");
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }


        PublishButton.addActionListener(e -> {
            int Confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to PUBLISH grades?\n" + "This will mark the course as COMPLETED for all enrolled students.\n" + "Students will be able to see their final grades immediately.", "Confirm Publication", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (Confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean Success = RegistrationHandler.PublishGrades(Offering.GetOfferingID());
                    if (Success) {
                        try {
                            Course C = Backend.DataBaseHandler.CourseHandler.FindCourseByID(Offering.GetCourseID());
                            String CourseCode = C != null ? C.GetCode() : "";
                            String CourseTitle = C != null ? C.GetTitle() : "";

                            Notification GradeNotif = new Notification("Grades Declared", "Final grades have been published for " + CourseCode + ": " + CourseTitle, "GRADE", LocalDate.now().toString()
                            );

                            List<StudentGradeRecord> StudentsList = StudentGradeRecordHandler.FindByOfferingID(Offering.GetOfferingID());
                            for (StudentGradeRecord Rec : StudentsList) {
                                NotificationHandler.AddNotification(Rec.getRollNumber(), GradeNotif);
                            }
                        }
                        catch (Exception Ex) {
                            Ex.printStackTrace();
                        }

                        JOptionPane.showMessageDialog(this, "Grades Published Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        PublishButton.setText("Grades Published");
                        PublishButton.setEnabled(false);
                        PublishButton.setBackground(ColorPanel);
                        PublishButton.setForeground(ColorTextSecondary);

                    }
                    else {
                        JOptionPane.showMessageDialog(this, "No registrations were updated. \n(Are there enrolled students?)", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

        EditButton.addActionListener(e -> {
            SearchField.setText("");
            OldData = DeepCopyList(TableModel.GetData());
            TableModel.setEditable(true);

            EditButton.setVisible(false);
            PublishButton.setVisible(false);
            CloseButton.setVisible(false);
            ExportButton.setVisible(false);
            ImportButton.setVisible(false);
            SaveButton.setVisible(true);
            CancelButton.setVisible(true);
            SearchField.setEnabled(false);
        });

        SaveButton.addActionListener(e -> {
            if (GradesTable.isEditing()) GradesTable.getCellEditor().stopCellEditing();
            try {
                List<List<Object>> Rows = TableModel.GetData();
                for (int k = 0; k < Rows.size(); k++){
                    HashMap<String, Integer> UpdatedGrades = new HashMap<>();
                    for (int j = 0; j < ComponentNames.length; j++) {
                        Object GradeObj = Rows.get(k).get(j+2);
                        int GradeValue = 0;
                        try { GradeValue = Integer.parseInt(GradeObj.toString()); } catch(Exception ex) { }

                        if (GradeValue > Components.get(ComponentNames[j]) || GradeValue < 0){
                            JOptionPane.showMessageDialog(this,
                                    "Invalid Grade for " + ComponentNames[j] + " (Row " + (k+1) + ").\nMust be between 0 and " + Components.get(ComponentNames[j]),
                                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
                            TableModel.SetData(OldData);
                            return;
                        }
                        UpdatedGrades.put(ComponentNames[j], GradeValue);
                    }
                    String RollStr = String.valueOf(Rows.get(k).get(0));
                    StudentGradeRecordHandler.UpdateGradeRecord(Offering.GetOfferingID(), Integer.parseInt(RollStr), UpdatedGrades);
                }
                JOptionPane.showMessageDialog(this, "Grades Saved Successfully.");
            }
            catch (SQLException ex) {
                System.out.println(ex.getMessage());
                throw new RuntimeException(ex);
            }

            TableModel.setEditable(false);
            SaveButton.setVisible(false);
            CancelButton.setVisible(false);
            EditButton.setVisible(true);
            PublishButton.setVisible(true);
            CloseButton.setVisible(true);
            ExportButton.setVisible(true);
            ImportButton.setVisible(true);
            SearchField.setEnabled(true);
        });

        CancelButton.addActionListener(e -> {
            if (GradesTable.isEditing()) GradesTable.getCellEditor().stopCellEditing();
            TableModel.SetData(OldData);
            TableModel.setEditable(false);
            SaveButton.setVisible(false);
            CancelButton.setVisible(false);
            EditButton.setVisible(true);
            PublishButton.setVisible(true);
            CloseButton.setVisible(true);
            ExportButton.setVisible(true);
            ImportButton.setVisible(true);
            SearchField.setEnabled(true);
        });

        ExportButton.addActionListener(e -> {
            JFileChooser FileChooser = new JFileChooser();
            FileChooser.setDialogTitle("Export Grades");
            String SafeName = CourseName.replaceAll("[^a-zA-Z0-9]", "_");
            FileChooser.setSelectedFile(new File(SafeName + "_Grades.csv"));

            if (FileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File FileToSave = FileChooser.getSelectedFile();
                if (!FileToSave.getName().toLowerCase().endsWith(".csv")) {
                    FileToSave = new File(FileToSave.getParent(), FileToSave.getName() + ".csv");
                }

                try (PrintWriter Writer = new PrintWriter(FileToSave)) {
                    StringBuilder HeaderLine = new StringBuilder();
                    for (int i = 0; i < TableModel.getColumnCount(); i++) {
                        HeaderLine.append(TableModel.getColumnName(i));
                        if (i < TableModel.getColumnCount() - 1) HeaderLine.append(",");
                    }
                    Writer.println(HeaderLine.toString());

                    for (List<Object> Row : TableModel.GetData()) {
                        StringBuilder RowLine = new StringBuilder();
                        for (int i = 0; i < Row.size(); i++) {
                            Object Val = Row.get(i);
                            RowLine.append(Val != null ? Val.toString() : "");
                            if (i < Row.size() - 1) RowLine.append(",");
                        }
                        Writer.println(RowLine.toString());
                    }
                    JOptionPane.showMessageDialog(this, "Export Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error exporting file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        ImportButton.addActionListener(e -> ImportGradesFromCSV());

        CloseButton.addActionListener(e -> dispose());

        ButtonPanel.add(PublishButton);
        ButtonPanel.add(EditButton);
        ButtonPanel.add(SaveButton);
        ButtonPanel.add(CancelButton);
        ButtonPanel.add(ImportButton);
        ButtonPanel.add(ExportButton);
        ButtonPanel.add(CloseButton);

        add(ButtonPanel, BorderLayout.SOUTH);
    }

    private void ImportGradesFromCSV() {
        JFileChooser FileChooser = new JFileChooser();
        FileChooser.setDialogTitle("Import Grades CSV");
        FileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));

        if (FileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File FileToRead = FileChooser.getSelectedFile();
            try (BufferedReader Reader = new BufferedReader(new FileReader(FileToRead))) {
                String HeaderLine = Reader.readLine();
                if (HeaderLine == null) throw new RuntimeException("Empty CSV file.");

                String[] Headers = HeaderLine.split(",");
                if (Headers.length < 3) throw new RuntimeException("Invalid CSV format. Expected at least Roll No, Name, and 1 Component.");

                HashMap<String, Integer> NewComponents = new HashMap<>();
                List<String> NewComponentNames = new ArrayList<>();
                int TotalPercentage = 0;

                for (int i = 2; i < Headers.length - 1; i++) {
                    String Header = Headers[i].trim();
                    if (!Header.contains("(") || !Header.contains(")")) {
                        throw new RuntimeException("Header format invalid: " + Header + ". Expected format: 'Name (MaxMarks)'");
                    }
                    String CompName = Header.substring(0, Header.lastIndexOf("(")).trim();
                    String MarksStr = Header.substring(Header.lastIndexOf("(") + 1, Header.lastIndexOf(")"));
                    int MaxMarks = Integer.parseInt(MarksStr);

                    NewComponents.put(CompName, MaxMarks);
                    NewComponentNames.add(CompName);
                    TotalPercentage += MaxMarks;
                }

                if (TotalPercentage != 100) {
                    throw new RuntimeException("Total component weightage in CSV is " + TotalPercentage + "%. Must be exactly 100%.");
                }

                OfferingHandler.UpdateGradingComponent(CurrentOffering.GetOfferingID(), NewComponents);

                String RowLine;
                while ((RowLine = Reader.readLine()) != null) {
                    String[] Values = RowLine.split(",");
                    if (Values.length < 1) continue;

                    int RollNumber;
                    try {
                        RollNumber = Integer.parseInt(Values[0].trim());
                    } catch (NumberFormatException e) { continue; }

                    HashMap<String, Integer> StudentGrades = new HashMap<>();
                    for (int i = 0; i < NewComponentNames.size(); i++) {
                        int CSVIndex = i + 2;
                        int Score = 0;
                        if (CSVIndex < Values.length && !Values[CSVIndex].trim().isEmpty()) {
                            try {
                                Score = Integer.parseInt(Values[CSVIndex].trim());
                            } catch (NumberFormatException ignored) {}
                        }

                        String CompName = NewComponentNames.get(i);
                        int Max = NewComponents.get(CompName);
                        if (Score < 0) Score = 0;
                        if (Score > Max) Score = Max;

                        StudentGrades.put(CompName, Score);
                    }

                    StudentGradeRecordHandler.UpdateGradeRecord(CurrentOffering.GetOfferingID(), RollNumber, StudentGrades);
                }

                JOptionPane.showMessageDialog(this, "Import Successful! Course components and grades updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();

            }
            catch (Exception Ex) {
                Ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Import Failed: " + Ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private List<Object> CreateRowFromRecord(StudentGradeRecord Record, String[] ComponentKeys) {
        List<Object> Row = new ArrayList<>();
        Student S;
        try {
            S = StudentHandler.FindStudentByRollNumber(Record.getRollNumber());
        } catch (SQLException e){
            throw new RuntimeException(e);
        }

        Row.add(String.valueOf(Record.getRollNumber()));
        Row.add(S.GetName());

        Map<String, Integer> Grades = Record.getGrade();
        int Total = 0;

        for (String Key : ComponentKeys) {
            int Score = 0;
            if(Grades != null && Grades.containsKey(Key)) {
                Score = Grades.get(Key);
            }
            Row.add(Score);
            Total += Score;
        }

        Row.add(Total);
        return Row;
    }

    private List<List<Object>> DeepCopyList(List<List<Object>> Original) {
        List<List<Object>> Copy = new ArrayList<>();
        for (List<Object> Row : Original) Copy.add(new ArrayList<>(Row));
        return Copy;
    }
}