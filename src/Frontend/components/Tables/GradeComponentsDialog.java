package Frontend.components.Tables;

import Backend.DataBaseHandler.OfferingHandler;
import Backend.DataBaseHandler.SemestersHandler;
import Backend.DataBaseHandler.StudentGradeRecordHandler;
import Backend.domain.Offerings;
import Backend.domain.StudentGradeRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GradeComponentsDialog extends JDialog {

    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel = new Color(0x1E1E1E);
    private static final Color ColorTextPrimary = new Color(0xFFFFFF);
    private static final Color ColorTextSecondary = new Color(0xB0B0B0);

    private Object[][] OldData;

    public GradeComponentsDialog(Frame Owner, String Title, String CourseName, String CourseCode, Offerings Offering) {
        super(Owner, Title + " - " + CourseName, true);

        setSize(600, 450);
        setLocationRelativeTo(Owner);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(ColorBackground);

        JLabel HeaderLabel = new JLabel(Title, SwingConstants.CENTER);
        HeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        HeaderLabel.setForeground(ColorTextPrimary);
        HeaderLabel.setBorder(new EmptyBorder(10, 10, 0, 10));
        add(HeaderLabel, BorderLayout.NORTH);

        String[] ColumnNames = {"Component", "Percentage"};

        HashMap<String, Integer> GradingComponents = Offering.GetGradingComponents();
        Object[][] Data = new Object[GradingComponents.size()][2];
        Set Entries = GradingComponents.entrySet();
        Iterator EntriesIterator = Entries.iterator();

        int Index = 0;
        while (EntriesIterator.hasNext()) {
            Map.Entry Mapping = (Map.Entry) EntriesIterator.next();
            Data[Index][0] = Mapping.getKey();
            Data[Index][1] = Mapping.getValue();
            Index++;
        }

        EditableTableModelGradeComponents GradeComponentsTableModel = new EditableTableModelGradeComponents(ColumnNames, Data);
        JTable GradeComponentsTable = new JTable(GradeComponentsTableModel);

        GradeComponentsTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        GradeComponentsTable.setForeground(ColorTextPrimary);
        GradeComponentsTable.setBackground(ColorPanel);
        GradeComponentsTable.setGridColor(ColorBackground.darker());
        GradeComponentsTable.setRowHeight(30);
        GradeComponentsTable.setSelectionBackground(new Color(0x3B82F6));
        GradeComponentsTable.setSelectionForeground(Color.WHITE);
        GradeComponentsTable.setBorder(null);

        GradeComponentsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        GradeComponentsTable.getTableHeader().setForeground(ColorTextSecondary);
        GradeComponentsTable.getTableHeader().setBackground(ColorPanel);
        GradeComponentsTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, ColorBackground.darker()));

        JScrollPane ScrollPane = new JScrollPane(GradeComponentsTable);
        ScrollPane.getViewport().setBackground(ColorPanel);
        ScrollPane.setBorder(BorderFactory.createLineBorder(ColorBackground.darker()));

        JPanel CenterPanel = new JPanel(new BorderLayout(0, 10));
        CenterPanel.setOpaque(false);
        CenterPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel InfoLabel = new JLabel("Showing details for: " + CourseName + " (" + CourseCode + ")", SwingConstants.LEFT);
        InfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        InfoLabel.setForeground(ColorTextSecondary);
        InfoLabel.setBorder(new EmptyBorder(0, 0, 5, 0));

        CenterPanel.add(InfoLabel, BorderLayout.NORTH);
        CenterPanel.add(ScrollPane, BorderLayout.CENTER);

        add(CenterPanel, BorderLayout.CENTER);

        JPanel ButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        ButtonPanel.setOpaque(false);
        ButtonPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        JButton NewComponentButton = new JButton("New");
        JButton DeleteComponentButton = new JButton("Delete");
        JButton SaveButton = new JButton("Save");
        JButton CancelButton = new JButton("Cancel");
        JButton EditButton = new JButton("Edit");
        JButton CloseButton = new JButton("Close");

        try {
            if (SemestersHandler.IsMaintenanceMode()) {
                EditButton.setEnabled(false);
                EditButton.setToolTipText("Editing disabled: System is under maintenance.");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        NewComponentButton.setVisible(false);
        NewComponentButton.addActionListener(e -> {
            GradeComponentsTableModel.addRow(new Object[]{"New Component", "0"});
        });

        DeleteComponentButton.setVisible(false);
        DeleteComponentButton.addActionListener(e -> {
            int SelectedRow = GradeComponentsTable.getSelectedRow();
            if (SelectedRow != -1) {
                GradeComponentsTableModel.RemoveRow(SelectedRow);
            }
        });

        SaveButton.setVisible(false);
        SaveButton.addActionListener(e -> {
            if (GradeComponentsTable.isEditing()) {
                GradeComponentsTable.getCellEditor().stopCellEditing();
            }

            int RowCount = GradeComponentsTableModel.getRowCount();
            int ColumnCount = GradeComponentsTableModel.getColumnCount();

            HashMap<String, Integer> NewGradingComponents = new HashMap<>();
            HashMap<String, Integer> UpdatedGradeRecords = new HashMap<>();
            Object[][] TableData = GradeComponentsTableModel.GetData();
            int Total = 0;

            String GradeLabel = "";
            String ValueString = "";
            NewGradingComponents.clear();
            UpdatedGradeRecords.clear();

            try {
                for (int i = 0; i < RowCount; i++) {
                    GradeLabel = String.valueOf(TableData[i][0]);
                    ValueString = String.valueOf(TableData[i][1]);
                    int Score = Integer.parseInt(ValueString);

                    if (Score < 0 || Score > 100) {
                        JOptionPane.showMessageDialog(this,
                                "Input for " + GradeLabel + " (" + Score + ") must be between 0 and 100.",
                                "Invalid Range",
                                JOptionPane.ERROR_MESSAGE);
                        GradeComponentsTableModel.setData(OldData);
                        return;
                    }

                    NewGradingComponents.put(String.valueOf(TableData[i][0]),
                            Integer.parseInt(String.valueOf(TableData[i][1])));
                    UpdatedGradeRecords.put(String.valueOf(TableData[i][0]), 0);
                    Total += Integer.parseInt(String.valueOf(TableData[i][1]));
                }
            }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid input for " + GradeLabel + ": '" + ValueString + "' is not a valid number.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                GradeComponentsTableModel.setData(OldData);
                return;
            }

            if (Total != 100) {
                JOptionPane.showMessageDialog(this,
                        "Weightage of all grade components must add up to 100%",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                GradeComponentsTableModel.setData(OldData);
                return;
            }

            GradeComponentsTableModel.setEditable(false);
            SaveButton.setVisible(false);
            CancelButton.setVisible(false);
            EditButton.setVisible(true);
            NewComponentButton.setVisible(false);
            DeleteComponentButton.setVisible(false);

            OldData = new Object[RowCount][ColumnCount];
            for (int i = 0; i < RowCount; i++) {
                for (int j = 0; j < ColumnCount; j++) {
                    OldData[i][j] = GradeComponentsTableModel.getValueAt(i, j);
                }
            }

            try {
                OfferingHandler.UpdateGradingComponent(Offering.GetOfferingID(), NewGradingComponents);
            }
            catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            try {
                java.util.List<StudentGradeRecord> ListOfEnrolledStudents =
                        StudentGradeRecordHandler.FindByOfferingID(Offering.GetOfferingID());
                for (StudentGradeRecord StudentGradeRecord : ListOfEnrolledStudents) {
                    StudentGradeRecordHandler.UpdateGradeRecord(
                            Offering.GetOfferingID(),
                            StudentGradeRecord.getRollNumber(),
                            UpdatedGradeRecords
                    );
                }
            }
            catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        CancelButton.setVisible(false);
        CancelButton.addActionListener(e -> {
            if (GradeComponentsTable.isEditing()) {
                GradeComponentsTable.getCellEditor().stopCellEditing();
            }

            GradeComponentsTableModel.setData(OldData);

            SaveButton.setVisible(false);
            CancelButton.setVisible(false);
            EditButton.setVisible(true);
            GradeComponentsTableModel.setEditable(false);
            NewComponentButton.setVisible(false);
            DeleteComponentButton.setVisible(false);
        });

        EditButton.addActionListener(e -> {
            int RowCount = GradeComponentsTableModel.getRowCount();
            int ColumnCount = GradeComponentsTableModel.getColumnCount();
            OldData = new Object[RowCount][ColumnCount];
            for (int i = 0; i < RowCount; i++) {
                for (int j = 0; j < ColumnCount; j++) {
                    OldData[i][j] = GradeComponentsTableModel.getValueAt(i, j);
                }
            }

            EditButton.setVisible(false);
            NewComponentButton.setVisible(true);
            SaveButton.setVisible(true);
            CancelButton.setVisible(true);
            GradeComponentsTableModel.setEditable(true);
            DeleteComponentButton.setVisible(true);
        });

        CloseButton.addActionListener(e -> dispose());

        ButtonPanel.add(NewComponentButton);
        ButtonPanel.add(DeleteComponentButton);
        ButtonPanel.add(EditButton);
        ButtonPanel.add(SaveButton);
        ButtonPanel.add(CancelButton);
        ButtonPanel.add(Box.createHorizontalStrut(10));
        ButtonPanel.add(CloseButton);

        add(ButtonPanel, BorderLayout.SOUTH);
    }
}
