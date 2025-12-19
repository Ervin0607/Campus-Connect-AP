package Frontend.components.Tables;

import Backend.DataBaseHandler.OfferingHandler;
import Backend.DataBaseHandler.SemestersHandler;
import Backend.domain.Offerings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashMap;

public class GradeSlabsDialog extends JDialog {

    private static final Color ColorBackground = new Color(0x121212);
    private static final Color ColorPanel = new Color(0x1E1E1E);
    private static final Color ColorTextPrimary = new Color(0xFFFFFF);
    private static final Color ColorTextSecondary = new Color(0xB0B0B0);

    private Object[][] OldData;

    public GradeSlabsDialog(Frame Owner, String Title, String CourseName, String CourseCode, Offerings Offering) {
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

        String[] ColumnNames = {"Grade", "Percentage"};

        HashMap<String, Integer> Slabs = Offering.GetGradingSlabs();
        if (Slabs == null) {
            Slabs = new HashMap<>();
        }

        String[] GradeLetters = {"A+", "A-", "B+", "B-", "C+", "C-", "D+", "D-"};
        int[] DefaultPercents = {90, 80, 70, 60, 50, 40, 30, 20};

        Object[][] Data = new Object[GradeLetters.length][2];
        for (int Index = 0; Index < GradeLetters.length; Index++) {
            String Grade = GradeLetters[Index];
            Integer Value = Slabs.get(Grade);
            if (Value == null) {
                Value = DefaultPercents[Index];
            }
            Data[Index][0] = Grade;
            Data[Index][1] = Value;
        }

        EditableTableModelGradeSlabs GradeSlabsTableModel = new EditableTableModelGradeSlabs(ColumnNames, Data);
        JTable GradeSlabsTable = new JTable(GradeSlabsTableModel);

        GradeSlabsTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        GradeSlabsTable.setForeground(ColorTextPrimary);
        GradeSlabsTable.setBackground(ColorPanel);
        GradeSlabsTable.setGridColor(ColorBackground.darker());
        GradeSlabsTable.setRowHeight(30);
        GradeSlabsTable.setSelectionBackground(new Color(0x3B82F6));
        GradeSlabsTable.setSelectionForeground(Color.WHITE);
        GradeSlabsTable.setBorder(null);

        GradeSlabsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        GradeSlabsTable.getTableHeader().setForeground(ColorTextSecondary);
        GradeSlabsTable.getTableHeader().setBackground(ColorPanel);
        GradeSlabsTable.getTableHeader().setBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ColorBackground.darker())
        );

        JScrollPane ScrollPane = new JScrollPane(GradeSlabsTable);
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

        SaveButton.setVisible(false);
        SaveButton.addActionListener(e -> {
            if (GradeSlabsTable.isEditing()) {
                GradeSlabsTable.getCellEditor().stopCellEditing();
            }

            Object[][] CurrentData = GradeSlabsTableModel.GetData();
            int RowCount = CurrentData.length;

            for (int Index = 0; Index < RowCount; Index++) {
                String GradeLabel = String.valueOf(CurrentData[Index][0]);
                int CurrentScore;
                try {
                    CurrentScore = Integer.parseInt(String.valueOf(CurrentData[Index][1]));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid input for " + GradeLabel + ". Please enter a whole number.",
                            "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (CurrentScore < 0 || CurrentScore > 100) {
                    JOptionPane.showMessageDialog(this,
                            GradeLabel + " threshold must be between 0 and 100.",
                            "Range Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (Index > 0) {
                    int PreviousScore = Integer.parseInt(String.valueOf(CurrentData[Index - 1][1]));
                    String PreviousLabel = String.valueOf(CurrentData[Index - 1][0]);

                    if (CurrentScore >= PreviousScore) {
                        JOptionPane.showMessageDialog(this,
                                "Invalid Hierarchy: " + GradeLabel + " (" + CurrentScore + ") cannot be greater than or equal to " + PreviousLabel + " (" + PreviousScore + ").",
                                "Logic Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            GradeSlabsTableModel.setEditable(false);
            SaveButton.setVisible(false);
            CancelButton.setVisible(false);
            EditButton.setVisible(true);

            int RowCountModel = GradeSlabsTableModel.getRowCount();
            int ColumnCountModel = GradeSlabsTableModel.getColumnCount();
            OldData = new Object[RowCountModel][ColumnCountModel];
            for (int i = 0; i < RowCountModel; i++) {
                for (int j = 0; j < ColumnCountModel; j++) {
                    OldData[i][j] = GradeSlabsTableModel.getValueAt(i, j);
                }
            }

            HashMap<String, Integer> GradingComponents = new HashMap<>();
            Object[][] TableData = GradeSlabsTableModel.GetData();
            for (int i = 0; i < RowCountModel; i++) {
                GradingComponents.put(
                        String.valueOf(TableData[i][0]),
                        Integer.parseInt(String.valueOf(TableData[i][1]))
                );
            }

            try {
                OfferingHandler.UpdateGradeSlabs(Offering.GetOfferingID(), GradingComponents);
            }
            catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        CancelButton.setVisible(false);
        CancelButton.addActionListener(e -> {
            if (GradeSlabsTable.isEditing()) {
                GradeSlabsTable.getCellEditor().stopCellEditing();
            }

            GradeSlabsTableModel.setData(OldData);

            SaveButton.setVisible(false);
            CancelButton.setVisible(false);
            EditButton.setVisible(true);
            GradeSlabsTableModel.setEditable(false);
        });

        EditButton.addActionListener(e -> {
            int RowCount = GradeSlabsTableModel.getRowCount();
            int ColumnCount = GradeSlabsTableModel.getColumnCount();
            OldData = new Object[RowCount][ColumnCount];
            for (int i = 0; i < RowCount; i++) {
                for (int j = 0; j < ColumnCount; j++) {
                    OldData[i][j] = GradeSlabsTableModel.getValueAt(i, j);
                }
            }

            EditButton.setVisible(false);
            SaveButton.setVisible(true);
            CancelButton.setVisible(true);
            GradeSlabsTableModel.setEditable(true);
        });

        CloseButton.addActionListener(e -> dispose());

        ButtonPanel.add(EditButton);
        ButtonPanel.add(SaveButton);
        ButtonPanel.add(CancelButton);
        ButtonPanel.add(Box.createHorizontalStrut(10));
        ButtonPanel.add(CloseButton);

        add(ButtonPanel, BorderLayout.SOUTH);
    }
}
