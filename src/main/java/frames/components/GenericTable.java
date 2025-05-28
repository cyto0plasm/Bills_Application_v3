package frames.components;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.border.EmptyBorder;

public class GenericTable extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JLabel totalRowsLabel;
    private final String[] columns;
    private List<Object[]> data;
    private boolean editable;
    private List<Integer> editableColumns;
    private Color headerColor;

    // UI Constants
    private static final Color HEADER_COLOR = new Color(45, 85, 151);
    private static final Color ROW_COLOR = new Color(240, 243, 245);
    private static final Color ALTERNATE_ROW_COLOR = new Color(225, 230, 235);
    private static final Color SELECTION_COLOR = new Color(196, 208, 233);
    private static final int ROW_HEIGHT = 40;
    private static final int CELL_PADDING = 8;

    public GenericTable(String[] columns, List<Object[]> data, boolean editable, List<Integer> editableColumns,Color headerColor) {
        this.columns = columns;
        this.data = data;
        this.editable = editable;
        this.editableColumns = editableColumns;
        this.headerColor= headerColor;
        initializeTable();
        setSize(1000, 450);
    }

    private void initializeTable() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Create a JPanel wrapper around the table with margin (padding)
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        createToolbar();
        createTableModel();
        setupTable();
        customizeTableHeader();
        customizeTableCells(table);
        setupScrollPane();
        populateTable();
        updateTotalRowsLabel();

        // Add the table to the wrapper panel with margins
        tablePanel.add(createTablePanel(), BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel createTablePanel() {
        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(Color.WHITE);
        tableWrapper.add(new JScrollPane(table), BorderLayout.CENTER);
        return tableWrapper;
    }

    /**
     * Creates the toolbar with a search field and row count label.
     */
    private void createToolbar() {
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        toolbarPanel.setBackground(Color.WHITE);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Tahoma", Font.PLAIN, 18));
        searchField.setPreferredSize(new Dimension(200, 30));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(HEADER_COLOR),
                BorderFactory.createEmptyBorder(5, 10, 5, 15)
        ));

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTable();
            }
        });

        totalRowsLabel = new JLabel("Total rows: 0");
        totalRowsLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
        totalRowsLabel.setBorder(new EmptyBorder(0, 10, 0, 10));

        //JButton printButton = new JButton("Print");
        //printButton.addActionListener(e -> printTable());
        JButton exportImageButton = new JButton("Export as Image");
        exportImageButton.addActionListener(e -> exportTableAsImage());
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.add(totalRowsLabel);
        //rightPanel.add(printButton);
        rightPanel.setBackground(Color.WHITE);
        rightPanel.add(exportImageButton);

        toolbarPanel.add(searchField, BorderLayout.CENTER);
        toolbarPanel.add(rightPanel, BorderLayout.EAST);

        add(toolbarPanel, BorderLayout.NORTH);
    }

    private void exportTableAsImage() {
        try {
            // Calculate the total height including header
            JTableHeader header = table.getTableHeader();
            int totalHeight = header.getHeight() + (table.getRowCount() * table.getRowHeight());
            int totalWidth = table.getWidth();

            // Create a properly sized BufferedImage
            BufferedImage image = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // Set rendering hints for better quality
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Fill background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, totalWidth, totalHeight);

            // Paint header
            header.paint(g2d);

            // Translate graphics for table content
            g2d.translate(0, header.getHeight());

            // Paint table content
            table.paint(g2d);
            g2d.dispose();

            // Create file chooser with proper configuration
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save Table as Image");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "PNG Images (*.png)", "png"));
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File outputFile = fileChooser.getSelectedFile();
                String filePath = outputFile.getAbsolutePath();

                // Ensure .png extension
                if (!filePath.toLowerCase().endsWith(".png")) {
                    outputFile = new File(filePath + ".png");
                }

                // Check if file exists
                if (outputFile.exists()) {
                    int response = JOptionPane.showConfirmDialog(this,
                            "File already exists. Do you want to overwrite it?",
                            "Confirm Overwrite",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (response != JOptionPane.YES_OPTION) {
                        return;
                    }
                }

                // Save image
                if (ImageIO.write(image, "png", outputFile)) {
                    JOptionPane.showMessageDialog(this,
                            "Table image saved successfully to:\n" + outputFile.getAbsolutePath(),
                            "Export Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    throw new IOException("Failed to save image - no appropriate writer found");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error exporting table as image:\n" + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text)); // Case-insensitive filter
        }
        updateTotalRowsLabel();
    }

    private void updateTotalRowsLabel() {
        int totalRows = model.getRowCount();
        int filteredRows = sorter.getViewRowCount();
        if (totalRows == filteredRows) {
            totalRowsLabel.setText("Total rows: " + totalRows);
        } else {
            totalRowsLabel.setText("Showing " + filteredRows + " of " + totalRows + " rows");
        }
    }

    private void createTableModel() {
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return editable && (editableColumns == null || editableColumns.contains(column));
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (getRowCount() > 0) {
                    Object value = getValueAt(0, columnIndex);
                    if (value != null) {
                        return value.getClass();
                    }
                }
                return super.getColumnClass(columnIndex);
            }
        };
        sorter = new TableRowSorter<>(model);
    }

    private void customizeTableHeader() {
        JTableHeader header = table.getTableHeader();
        header.setBackground( headerColor);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));

        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value.toString(), SwingConstants.CENTER); // Center align text
                label.setFont(new Font("Arial", Font.BOLD, 20));
                label.setForeground(Color.WHITE);
                label.setOpaque(true);
                label.setBackground(headerColor);
                label.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT); // RTL layout
                return label;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
    }

private void customizeTableCells(JTable table) {
    Font cellFont = new Font("Arial", Font.PLAIN, 18);
    Font editorFont = new Font("Arial", Font.BOLD, 20); // Bigger font for editing

    // Renderer for displaying table cells
    DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel(value != null ? value.toString() : "", SwingConstants.CENTER);
            label.setOpaque(true);
            label.setFont(cellFont);
            label.setBorder(BorderFactory.createEmptyBorder(5, CELL_PADDING, 5, CELL_PADDING));

            // Default row coloring
            if (isSelected) {
                label.setBackground(SELECTION_COLOR);
                label.setForeground(Color.BLACK);
            } else {
                label.setBackground(row % 2 == 0 ? ROW_COLOR : ALTERNATE_ROW_COLOR);
                label.setForeground(Color.BLACK);
            }

            // Custom coloring for "المتبقي" column
            if (table.getColumnName(column).equals("المتبقي")) {
                try {
                    float totalAmount = parseFloat(table.getValueAt(row, getColumnIndex(table, "المبلغ الكلي")));
                    float income = parseFloat(table.getValueAt(row, getColumnIndex(table, "الدخل")));
                    float remaining = parseFloat(value);

                    String remainingText = String.format("%.2f", remaining);
                    
                    if (income == totalAmount) {
                        label.setForeground(new Color(34, 139, 34)); // Green (Fully paid)
                        label.setText(remainingText + " (مدفوع بالكامل)");
                    } else if (income > totalAmount) {
                        label.setText(remainingText + " (باقي للعميل)");
                    } else if (income <= 0) {
                        label.setForeground(new Color(255, 69, 0)); // Red (Not paid)
                        label.setText(remainingText + " (غير مدفوع)");
                    } else {
                        label.setForeground(new Color(255, 165, 0)); // Orange (Partially paid)
                        label.setText(remainingText + " (مدفوع جزئياً)");
                    }
                } catch (Exception e) {
                    label.setText(value != null ? value.toString() : "");
                    System.out.println("Error formatting cell: " + e.getMessage());
                }
            }

            label.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            return label;
        }
    };

    // Editor for entering values (bigger font and centered)
    DefaultCellEditor cellEditor = new DefaultCellEditor(new JTextField()) {
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            JTextField editor = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
            editor.setFont(editorFont);
            editor.setHorizontalAlignment(JTextField.CENTER);
            return editor;
        }
    };

    for (int i = 0; i < table.getColumnCount(); i++) {
        table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        table.getColumnModel().getColumn(i).setCellEditor(cellEditor);
    }
}

// Helper method to find column index by name
private int getColumnIndex(JTable table, String columnName) {
    for (int i = 0; i < table.getColumnCount(); i++) {
        if (table.getColumnName(i).equals(columnName)) {
            return i;
        }
    }
    return -1;
}

// Helper method to parse float values safely
private float parseFloat(Object value) {
    if (value instanceof Number) {
        return ((Number) value).floatValue();
    } else {
        return Float.parseFloat(value.toString().replace(",", ""));
    }
}

    private void setupTable() {
        table = new JTable(model);
        table.setRowHeight(ROW_HEIGHT);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);
        table.setAutoCreateRowSorter(true);
        table.setRowSorter(sorter);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(SELECTION_COLOR);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
table.setBackground(new Color(235,235,235));
        table.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT); // Ensure RTL layout for the table

        // Set the width of the first column (ID)
        TableColumn idColumn = table.getColumnModel().getColumn(0);
        idColumn.setPreferredWidth(120);
        idColumn.setMinWidth(80);
        idColumn.setMaxWidth(130);
    }

    private void setupScrollPane() {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Customize scrollbar
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = HEADER_COLOR.brighter();
                this.trackColor = ROW_COLOR;
            }
        });

        add(scrollPane, BorderLayout.CENTER);
    }

    private void populateTable() {
        if (data != null && !data.isEmpty()) {
            for (Object[] row : data) {
                model.addRow(row);
            }
        }
        updateTotalRowsLabel();
    }

    public void updateData(List<Object[]> newData) {
        model.setRowCount(0);
        this.data = newData;

        populateTable();

        // Reset the sorter to apply new data properly
        sorter.setRowFilter(null);
        sorter.modelStructureChanged();

        updateTotalRowsLabel();
    }

    public void addRowSelectionListener(ListSelectionListener listener) {
        table.getSelectionModel().addListSelectionListener(listener);
    }

    public void clearSearchField() {
        if (searchField != null) {
            searchField.setText("");
        }
    }

    public JTable getTable() {
        return table;
    }

    public int getSelectedRow() {
        return table.getSelectedRow();
    }

    public Object[] getSelectedRowData() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }

        Object[] rowData = new Object[table.getColumnCount()];
        for (int i = 0; i < table.getColumnCount(); i++) {
            rowData[i] = table.getValueAt(selectedRow, i);
        }
        return rowData;
    }

    private void printTable() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Invoice Print");

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // Scale to fit within page
            double scaleX = pageFormat.getImageableWidth() / this.getWidth();
            double scaleY = pageFormat.getImageableHeight() / this.getHeight();
            double scale = Math.min(scaleX, scaleY);
            g2d.scale(scale, scale);

            this.paint(g2d);

            return Printable.PAGE_EXISTS;
        });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Printing failed: " + e.getMessage(),
                        "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
