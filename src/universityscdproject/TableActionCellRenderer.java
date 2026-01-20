/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package universityscdproject;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class TableActionCellRenderer extends DefaultTableCellRenderer {
 
 
 @Override
public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    PanelAction action = new PanelAction();
    
     action.setLayout(new java.awt.GridBagLayout()); 

    if (table.getColumnCount() == 7) { 
         action.jButton1.setVisible(true);
        action.jButton2.setVisible(false);
        action.jButton3.setVisible(false);
    } else {
         action.jButton1.setVisible(false); 
        action.jButton2.setVisible(true);  
        action.jButton3.setVisible(true);  
        
        action.jButton2.setText("Accept");
        action.jButton3.setText("Reject");
        
         action.jButton2.setBackground(java.awt.Color.GREEN);
        action.jButton3.setBackground(java.awt.Color.RED);
    }

    // Set Opaque for colors to show
    action.jButton2.setOpaque(true);
    action.jButton2.setBorderPainted(false);
    action.jButton3.setOpaque(true);
    action.jButton3.setBorderPainted(false);

    if (isSelected) {
        action.setBackground(table.getSelectionBackground());
    } else {
        action.setBackground(table.getBackground());
    }

    return action;
}
}


