package Frontend.components.Tables;

import javax.swing.table.AbstractTableModel;

public class EditableTableModelGradeComponents extends AbstractTableModel {
    private String[] ColumnNames;
    private Object[][] Data;
    private boolean Editable = false;

    public EditableTableModelGradeComponents(String[] ColumnNames, Object[][] InitialData) {
        this.ColumnNames = ColumnNames;
        this.Data = InitialData;
    }

    @Override
    public Class<?> getColumnClass(int ColumnIndex) {
        return Object.class;
    }

    @Override
    public void setValueAt(Object Value, int RowIndex, int ColumnIndex) {
        if (Value instanceof String) {
            String ValueString = ((String) Value).trim();
            try {
                int ValueInt = Integer.parseInt(ValueString);
                Data[RowIndex][ColumnIndex] = ValueInt;
            }
            catch (NumberFormatException e) {
                Data[RowIndex][ColumnIndex] = ValueString;
            }
        }
        else {
            Data[RowIndex][ColumnIndex] = Value;
        }
        fireTableCellUpdated(RowIndex, ColumnIndex);
    }

    public void addRow(Object[] RowData) {
        int OldRowCount = Data.length;
        Object[][] NewData = new Object[OldRowCount + 1][];
        if (OldRowCount > 0) {
            System.arraycopy(Data, 0, NewData, 0, OldRowCount);
        }
        NewData[OldRowCount] = RowData;
        this.Data = NewData;
        fireTableRowsInserted(OldRowCount, OldRowCount);
    }

    @Override
    public int getRowCount() {
        return Data.length;
    }

    @Override
    public int getColumnCount() {
        return ColumnNames.length;
    }

    @Override
    public Object getValueAt(int RowIndex, int ColumnIndex) {
        return Data[RowIndex][ColumnIndex];
    }

    @Override
    public String getColumnName(int ColumnIndex) {
        return ColumnNames[ColumnIndex];
    }

    @Override
    public boolean isCellEditable(int RowIndex, int ColumnIndex) {
        return Editable;
    }

    public void setEditable(boolean Editable) {
        this.Editable = Editable;
    }

    public void setData(Object[][] NewData) {
        this.Data = NewData;
        fireTableDataChanged();
    }

    public Object[][] GetData() {
        return Data;
    }

    public void RemoveRow(int SelectedRow) {
        if (SelectedRow < 0 || SelectedRow >= Data.length) {
            return;
        }
        int OldRowCount = Data.length;

        Object[][] NewData = new Object[OldRowCount - 1][];
        int NewDataIndex = 0;
        for (int i = 0; i < OldRowCount; i++) {
            if (i == SelectedRow) {
                continue;
            }
            NewData[NewDataIndex] = Data[i];
            NewDataIndex++;
        }
        this.Data = NewData;
        fireTableRowsDeleted(SelectedRow, SelectedRow);
    }
}
