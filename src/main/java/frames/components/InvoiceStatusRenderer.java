package frames.components;



import javax.swing.table.DefaultTableCellRenderer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class InvoiceStatusRenderer extends DefaultTableCellRenderer {
    private final Color ROW_COLOR;
    private final Color ALTERNATE_ROW_COLOR;
    private final Color SELECTION_COLOR;

    public InvoiceStatusRenderer(Color rowColor, Color alternateRowColor, Color selectionColor) {
        this.ROW_COLOR = rowColor;
        this.ALTERNATE_ROW_COLOR = alternateRowColor;
        this.SELECTION_COLOR = selectionColor;
        setHorizontalAlignment(SwingConstants.CENTER);
        setFont(new Font("Arial", Font.BOLD, 18));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Maintain alternating row background colors
        if (isSelected) {
            setBackground(SELECTION_COLOR);
            setForeground(Color.BLACK);
        } else {
            setBackground(row % 2 == 0 ? ROW_COLOR : ALTERNATE_ROW_COLOR);
        }

        try {
            Object totalAmountObj = table.getValueAt(row, 2); // Total amount (index 2)
            Object incomeObj = table.getValueAt(row, 3);      // Income (index 3)

            if (totalAmountObj != null && incomeObj != null && value != null) {
                float totalAmount = parseFloat(totalAmountObj);
                float income = parseFloat(incomeObj);
                float remaining = parseFloat(value);

                // Set text and color based on payment status
                String remainingText = String.format("%.2f", remaining);
                if (income >= totalAmount) {
                    setForeground(new Color(34, 139, 34)); // Forest Green
                    setText(remainingText + " (مدفوع بالكامل)");
                } else if (income <= 0) {
                    setForeground(new Color(255, 69, 0)); // Red-Orange
                    setText(remainingText + " (غير مدفوع)");
                } else {
                    setForeground(new Color(255, 165, 0)); // Orange
                    setText(remainingText + " (مدفوع جزئياً)");
                }
            }
        } catch (Exception e) {
            System.out.println("Error formatting cell: " + e.getMessage());
        }

        return this;
    }

    private float parseFloat(Object value) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        } else {
            return Float.parseFloat(value.toString().replace(",", ""));
        }
    }
}
