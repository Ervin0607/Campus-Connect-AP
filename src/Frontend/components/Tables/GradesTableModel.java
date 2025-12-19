package Frontend.components.Tables;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class GradesTableModel extends AbstractTableModel {
    private final String[] ColumnNames;
    private List<List<Object>> Data;
    private boolean Editable = false;

    public GradesTableModel(String[] ColumnNames, List<List<Object>> InitialData) {
        this.ColumnNames = ColumnNames;
        this.Data = InitialData;
    }

    @Override
    public int getRowCount() {
        return Data.size();
    }

    @Override
    public int getColumnCount() {
        return ColumnNames.length;
    }

    @Override
    public Object getValueAt(int RowIndex, int ColumnIndex) {
        return Data.get(RowIndex).get(ColumnIndex);
    }

    @Override
    public String getColumnName(int ColumnIndex) {
        return ColumnNames[ColumnIndex];
    }

    @Override
    public Class<?> getColumnClass(int ColumnIndex) {
        if (Data.isEmpty()) return Object.class;
        Object Value = getValueAt(0, ColumnIndex);
        return (Value != null) ? Value.getClass() : String.class;
    }

    @Override
    public boolean isCellEditable(int RowIndex, int ColumnIndex) {
        if (!Editable) return false;
        if (ColumnIndex <= 1) return false;
        if (ColumnIndex == getColumnCount() - 1) return false;
        return true;
    }

    public void setEditable(boolean EditableFlag) {
        this.Editable = EditableFlag;
    }

    @Override
    public void setValueAt(Object Value, int RowIndex, int ColumnIndex) {
        try {
            int IntValue = Integer.parseInt(Value.toString());
            Data.get(RowIndex).set(ColumnIndex, IntValue);
            fireTableCellUpdated(RowIndex, ColumnIndex);
            RecalculateTotal(RowIndex);
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format");
        }
    }

    private void RecalculateTotal(int RowIndex) {
        int Total = 0;
        List<Object> RowData = Data.get(RowIndex);

        for (int ColumnIndex = 2; ColumnIndex < getColumnCount() - 1; ColumnIndex++) {
            Object Value = RowData.get(ColumnIndex);
            if (Value instanceof Number) {
                Total += ((Number) Value).intValue();
            }
            else if (Value instanceof String) {
                try {
                    Total += Integer.parseInt((String) Value);
                } catch (NumberFormatException ignored) {}
            }
        }

        int TotalColumnIndex = getColumnCount() - 1;
        RowData.set(TotalColumnIndex, Total);
        fireTableCellUpdated(RowIndex, TotalColumnIndex);
    }

    public List<List<Object>> GetData() {
        return Data;
    }

    public void SetData(List<List<Object>> NewData) {
        this.Data = NewData;
        fireTableDataChanged();
    }
}
