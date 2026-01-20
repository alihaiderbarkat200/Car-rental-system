/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package universityscdproject;

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;

public class TableActionCellEditor extends DefaultCellEditor {
    private TableActionEvent event;

    public TableActionCellEditor(TableActionEvent event) {
        super(new JCheckBox());
        this.event = event;
    }
 
    
    @Override
public Component getTableCellEditorComponent(JTable jtable, Object o, boolean bln, int row, int column) {
    PanelAction action = new PanelAction();
    action.setLayout(new java.awt.GridBagLayout()); 
    
     String headerName = jtable.getColumnName(0);

    if (headerName.equalsIgnoreCase("Car ID")) { 
         action.jButton1.setVisible(true);  // Delete
        action.jButton2.setVisible(false); // Accept
        action.jButton3.setVisible(false); // Reject
        
         action.initEvent(event, row); 
    } else {
         action.jButton1.setVisible(false);
        action.jButton2.setVisible(true);
        action.jButton3.setVisible(true);
        
         action.initEvent(event, row);
    }
    
    action.setBackground(jtable.getSelectionBackground());
    return action;
}
}