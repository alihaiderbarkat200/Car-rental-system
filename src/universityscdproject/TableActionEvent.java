/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package universityscdproject;

public interface TableActionEvent {
    public void onDelete(int row);
    public void onApprove(int row); 
    public void onReject(int row);  
}