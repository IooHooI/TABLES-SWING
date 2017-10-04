package org.swing;

import com.github.lgooddatepicker.components.DatePicker;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by bender on 02.10.17.
 */
public class SomeForm extends JFrame {
    private JComboBox<String> comboBox1;
    private JList<Object> list1;
    private JList<Object> list2;
    private DefaultListModel<Object> defaultListModel1;
    private DefaultListModel<Object> defaultListModel2;
    private DefaultComboBoxModel<String> defaultComboBoxModel1;
    private DefaultComboBoxModel<String> defaultComboBoxModel2;
    private JComboBox<String> comboBox2;
    private DatePicker datePicker1;
    private DatePicker datePicker2;
    private JPanel somePanel;
    private JButton generateButton;
    private JCheckBox checkBox1;
    private JCheckBox checkBox2;
    private Workbook workbook;
    private List<Integer> rowIndexes;
    private List<Integer> collumnIndexes;

    private SomeForm() {
        rowIndexes = new ArrayList<>();
        collumnIndexes = new ArrayList<>();
        setContentPane(somePanel);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTransferHandler(new FileTransferHandler());
        setPreferredSize(new Dimension(500, 500));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        defaultListModel1 = new DefaultListModel<>();
        defaultListModel2 = new DefaultListModel<>();
        defaultComboBoxModel1 = new DefaultComboBoxModel<>();
        defaultComboBoxModel2 = new DefaultComboBoxModel<>();
        list1.setModel(defaultListModel1);
        list2.setModel(defaultListModel2);
        comboBox1.setModel(defaultComboBoxModel1);
        comboBox2.setModel(defaultComboBoxModel2);
        comboBox1.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    if (!defaultListModel1.contains(defaultComboBoxModel1.getSelectedItem())) {
                        defaultListModel1.add(0, defaultComboBoxModel1.getSelectedItem());
                        rowIndexes.add(getRowIndex(defaultComboBoxModel1.getSelectedItem().toString()));
                    }
                }
            }
        });
        comboBox2.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    if (!defaultListModel2.contains(defaultComboBoxModel2.getSelectedItem())) {
                        defaultListModel2.add(0, defaultComboBoxModel2.getSelectedItem());
                        collumnIndexes.add(getCollumnIndex(defaultComboBoxModel2.getSelectedItem().toString()));
                    }
                }
            }
        });
        generateButton.addActionListener(e -> {
            XSSFWorkbook result = createResultForOneTable();
            JFileChooser fc = new JFileChooser();
            if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    FileOutputStream fileStream = new FileOutputStream(fc.getSelectedFile());
                    result.write(fileStream);
                    result.close();
                } catch (Exception exc) {
                    System.out.println("Что-то пошло не так...");
                }
            }
        });
    }

    private Integer getCollumnIndex(String s) {
        Sheet dataSheet = workbook.getSheetAt(0);
        for (Cell cell : dataSheet.getRow(2)) {
            if (cell.getStringCellValue().equals(s)) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

    private Integer getRowIndex(String s) {
        Sheet dataSheet = workbook.getSheetAt(0);
        for (Row aDataSheet : dataSheet) {
            Cell cell = aDataSheet.getCell(1);
            if (cell.getStringCellValue().replaceAll("\n", " ").equals(s)) {
                return cell.getRowIndex();
            }
        }
        return -1;
    }

    public void outputExelData(String filePath) {
        setTitle(filePath);
        comboBox1.setPrototypeDisplayValue("XXXXXXXXXXXXXXXX");
        comboBox2.setPrototypeDisplayValue("XXXXXXXXXXXXXXXX");
        list1.setPrototypeCellValue("XXXXXXXXXXXXXXXX");
        list2.setPrototypeCellValue("XXXXXXXXXXXXXXXX");
        try (FileInputStream excelFile = new FileInputStream(new File(filePath))) {
            workbook = new XSSFWorkbook(excelFile);
            Sheet dataSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = dataSheet.iterator();
            iterator.forEachRemaining(p -> {
                if (!p.getCell(2).getStringCellValue().isEmpty() && defaultComboBoxModel1.getIndexOf(p.getCell(2).getStringCellValue()) == -1 && !"Код объекта".equals(p.getCell(2).getStringCellValue())) {
                    defaultComboBoxModel1.addElement(p.getCell(1).getStringCellValue());
                }
            });
            dataSheet.getRow(2).iterator().forEachRemaining(p -> {
                if (!p.getStringCellValue().isEmpty() && defaultComboBoxModel2.getIndexOf(p.getStringCellValue()) == -1) {
                    defaultComboBoxModel2.addElement(p.getStringCellValue());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private XSSFWorkbook createResultForOneTable() {
        XSSFWorkbook result = new XSSFWorkbook();
        XSSFSheet sheet = result.createSheet("Результирующая таблица");
        Sheet dataSheet = workbook.getSheetAt(0);
        int ratio = checkBox2.isSelected() ? 2 : 4;
        int rowCount = defaultListModel1.getSize() + 1;
        int collumnCount = defaultListModel2.getSize() * ratio + 2;
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("Объект");
        cell = row.createCell(1);
        cell.setCellValue("Код объекта");
        for (int i = 2; i < collumnCount; i += ratio) {
            cell = row.createCell(i);
            cell.setCellValue(dataSheet.getRow(2).getCell(collumnIndexes.get((i - 2) / ratio)).getStringCellValue());
            row.createCell(i + 1);
            row.createCell(i + 2);
            row.createCell(i + 3);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, i, i + 3));
        }
        CellStyle cellStyle = result.createCellStyle();
        CreationHelper createHelper = result.getCreationHelper();
        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.mm.yyyy"));
        for (int i = 1; i < rowCount; i++) {
            if (checkBox2.isSelected()) {
            } else {
                row = sheet.createRow(i);

                cell = row.createCell(0);
                cell.setCellValue(dataSheet.getRow(rowIndexes.get(i - 1)).getCell(1).getStringCellValue());

                cell = row.createCell(1);
                cell.setCellValue(dataSheet.getRow(rowIndexes.get(i - 1)).getCell(2).getStringCellValue());
                for (int j = 2; j < collumnCount; j += ratio) {
                    cell = row.createCell(j);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(dataSheet.getRow(rowIndexes.get(i - 1)).getCell(collumnIndexes.get((j - 2) / ratio)).getDateCellValue());
                    cell = row.createCell(j + 1);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(dataSheet.getRow(rowIndexes.get(i - 1)).getCell(collumnIndexes.get((j - 2) / ratio) + 1).getDateCellValue());
                    cell = row.createCell(j + 2);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(dataSheet.getRow(rowIndexes.get(i - 1)).getCell(collumnIndexes.get((j - 2) / ratio) + 2).getDateCellValue());
                    cell = row.createCell(j + 3);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(dataSheet.getRow(rowIndexes.get(i - 1)).getCell(collumnIndexes.get((j - 2) / ratio) + 3).getDateCellValue());
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            // handle exception
        }
        SwingUtilities.invokeLater(() -> {
            JFrame.setDefaultLookAndFeelDecorated(true);
            new SomeForm();
        });
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        somePanel = new JPanel();
        somePanel.setLayout(new GridLayoutManager(11, 6, new Insets(0, 0, 0, 0), -1, -1));
        somePanel.setFont(new Font("Liberation Mono", Font.BOLD, somePanel.getFont().getSize()));
        final Spacer spacer1 = new Spacer();
        somePanel.add(spacer1, new GridConstraints(5, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Объекты");
        somePanel.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        datePicker1 = new DatePicker();
        datePicker1.setForeground(new Color(-12828863));
        datePicker1.setText("");
        datePicker1.setToolTipText("Начиная с");
        somePanel.add(datePicker1, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        datePicker2 = new DatePicker();
        datePicker2.setForeground(new Color(-12828863));
        datePicker2.setText("");
        datePicker2.setToolTipText("Заканчивая по");
        somePanel.add(datePicker2, new GridConstraints(9, 2, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        generateButton = new JButton();
        generateButton.setText("Сгенерировать");
        somePanel.add(generateButton, new GridConstraints(10, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkBox1 = new JCheckBox();
        checkBox1.setText("Включать объекты, по которым нет плановых дат");
        somePanel.add(checkBox1, new GridConstraints(7, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBox1 = new JComboBox();
        comboBox1.setEditable(true);
        somePanel.add(comboBox1, new GridConstraints(1, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        comboBox2 = new JComboBox();
        comboBox2.setEditable(true);
        somePanel.add(comboBox2, new GridConstraints(3, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Этапы");
        somePanel.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        somePanel.add(scrollPane1, new GridConstraints(4, 0, 3, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        list1 = new JList();
        final DefaultListModel defaultListModel3 = new DefaultListModel();
        list1.setModel(defaultListModel3);
        scrollPane1.setViewportView(list1);
        final JScrollPane scrollPane2 = new JScrollPane();
        somePanel.add(scrollPane2, new GridConstraints(4, 2, 3, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        list2 = new JList();
        scrollPane2.setViewportView(list2);
        checkBox2 = new JCheckBox();
        checkBox2.setText("Отображать только плановые даты");
        somePanel.add(checkBox2, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return somePanel;
    }
}