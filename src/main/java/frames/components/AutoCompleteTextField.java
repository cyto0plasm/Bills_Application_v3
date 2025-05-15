/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package frames.components;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class AutoCompleteTextField {
    public static void attach(JTextField textField, List<String> items) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        JComboBox<String> comboBox = new JComboBox<>(model);
        comboBox.setFocusable(false);

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String input = textField.getText();
                model.removeAllElements();
                
                if (!input.isEmpty()) {
                    List<String> filteredItems = items.stream()
                        .filter(item -> item.toLowerCase().contains(input.toLowerCase()))
                        .collect(Collectors.toList());
                    
                    for (String item : filteredItems) {
                        model.addElement(item);
                    }

                    if (!filteredItems.isEmpty()) {
                        comboBox.showPopup();
                    } else {
                        comboBox.hidePopup();
                    }
                }
            }
        });

        comboBox.addActionListener(e -> {
            if (comboBox.getSelectedItem() != null) {
                textField.setText(comboBox.getSelectedItem().toString());
            }
        });

        textField.setLayout(new BorderLayout());
        textField.add(comboBox, BorderLayout.SOUTH);
    }}
