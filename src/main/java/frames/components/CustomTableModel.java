
package frames.components;

import javax.swing.table.DefaultTableModel;

public class CustomTableModel extends DefaultTableModel {
    public enum TableType {
        CLIENT, INVOICE_HEADER, INVOICE_DETAILS
    }

    private TableType tableType;

    public CustomTableModel(Object[][] data, Object[] columnNames, TableType tableType) {
        super(data, columnNames);
        this.tableType = tableType;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        if (tableType == TableType.CLIENT || tableType == TableType.INVOICE_HEADER) {
            return false; // Make all cells non-editable for these tables
        } else if (tableType == TableType.INVOICE_DETAILS) {
            // Example: Only column 2 (Quantity) and column 3 (Price) are editable
            return column == 1 ||column == 2 || column == 3;
        }
        return false;
    }
}
