/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package universityscdproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.ImageIcon;
 import java.awt.Image;           

import javax.swing.JOptionPane;
 import javax.swing.table.DefaultTableModel;
 
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.FileInputStream;

 
 






/**
 *
 * @author haide
 */
public class admin extends javax.swing.JFrame {
            String savedImagePath = "";  
        private Home homePage; 
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(admin.class.getName());
    
 

// Add this constructor so Admin knows about Home
public void setHomeReference(Home home) {
    this.homePage = home;
}

 public admin() {
    initComponents();
    loadProfilePicture();
    loadReturnTable();
    updateDashboardStats();

    universityscdproject.TableActionEvent event = new universityscdproject.TableActionEvent() {
        @Override
        public void onDelete(int row) {
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            String carId = table.getValueAt(row, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(admin.this, "Delete Car ID: " + carId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deleteFromDatabase(carId);
             }
        }
        
        @Override
        public void onApprove(int row) {
            if (jTable1.isEditing()) {
                jTable1.getCellEditor().stopCellEditing();
            }
            int bookingId = Integer.parseInt(jTable1.getValueAt(row, 0).toString()); 
            try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/car_rental_db", "root", "")) {
                 String updateSql = "UPDATE bookings SET status = 'Approved' WHERE id = ?";
                PreparedStatement pstUpdate = con.prepareStatement(updateSql);
                pstUpdate.setInt(1, bookingId);
                pstUpdate.executeUpdate();

                 String fetchSql = "SELECT car_id, user_email, total_price, payment_method FROM bookings WHERE id = ?";
                PreparedStatement pstFetch = con.prepareStatement(fetchSql);
                pstFetch.setInt(1, bookingId);
                ResultSet rs = pstFetch.executeQuery();

                if (rs.next()) {
                     String paySql = "INSERT INTO payments (booking_id, car_id, user_email, amount, payment_method) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement pstPay = con.prepareStatement(paySql);
                    pstPay.setInt(1, bookingId);
                    pstPay.setInt(2, rs.getInt("car_id"));
                    pstPay.setString(3, rs.getString("user_email"));
                    pstPay.setDouble(4, rs.getDouble("total_price"));
                    pstPay.setString(5, rs.getString("payment_method"));
                    pstPay.executeUpdate();
                }
                JOptionPane.showMessageDialog(null, "Booking Approved and Payment Recorded!");
                loadBookingTableData(); 
                
                updateDashboardStats();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        }

        @Override
        public void onReject(int row) {
            if (jTable1.isEditing()) {
                jTable1.getCellEditor().stopCellEditing();
            }
            int id = Integer.parseInt(jTable1.getValueAt(row, 0).toString());
            String reason = JOptionPane.showInputDialog(null, "Enter Rejection Reason:");
            if (reason != null && !reason.trim().isEmpty()) {
                updateBookingStatus(id, "Rejected", reason);
            }
        }
    };

     javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);

     table.getColumnModel().getColumn(6).setCellRenderer(new universityscdproject.TableActionCellRenderer());
    table.getColumnModel().getColumn(6).setCellEditor(new universityscdproject.TableActionCellEditor(event));    
    table.getColumnModel().getColumn(6).setPreferredWidth(150); 
    table.setRowHeight(50); 
     for (int i = 0; i < table.getColumnCount(); i++) {
        if (i != 6) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
    }

     jTable1.getColumnModel().getColumn(9).setCellRenderer(new universityscdproject.TableActionCellRenderer());
    jTable1.getColumnModel().getColumn(9).setCellEditor(new universityscdproject.TableActionCellEditor(event));
    jTable1.getColumnModel().getColumn(9).setPreferredWidth(180);
    jTable1.setRowHeight(60); 
     for (int i = 0; i < jTable1.getColumnCount(); i++) {
        if (i != 9) jTable1.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
    }

    loadTableData();
    loadBookingTableData(); 
}
 
 public void updateDashboardStats() {
    try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/car_rental_db", "root", "")) {
        
         String sqlLive = "SELECT SUM(number_of_cars) FROM cars";
        PreparedStatement pst1 = con.prepareStatement(sqlLive);
        ResultSet rs1 = pst1.executeQuery();
        int totalLive = 0;
        if (rs1.next()) totalLive = rs1.getInt(1);
        jTextField1.setText(String.valueOf(totalLive));

         String sqlOrders = "SELECT COUNT(*) FROM bookings WHERE status != 'Rejected'";
        PreparedStatement pst2 = con.prepareStatement(sqlOrders);
        ResultSet rs2 = pst2.executeQuery();
        int totalOrders = 0;
        if (rs2.next()) totalOrders = rs2.getInt(1);
        jTextField2.setText(String.valueOf(totalOrders));

         String sqlEarnings = "SELECT SUM(total_price) FROM bookings WHERE status = 'Approved'";
        PreparedStatement pst3 = con.prepareStatement(sqlEarnings);
        ResultSet rs3 = pst3.executeQuery();
        double earnings = 0;
        if (rs3.next()) earnings = rs3.getDouble(1);
        jTextField3.setText("PR" + earnings);

         String sqlRented = "SELECT COUNT(b.id) FROM bookings b " +
                           "LEFT JOIN returns r ON b.`order-id` = r.order_id " +
                           "WHERE b.status = 'Approved' AND r.order_id IS NULL";
        PreparedStatement pst4 = con.prepareStatement(sqlRented);
        ResultSet rs4 = pst4.executeQuery();
        int rentedCount = 0;
        if (rs4.next()) rentedCount = rs4.getInt(1);
        jTextField6.setText(String.valueOf(rentedCount));

         int remaining = totalLive - rentedCount;
        jTextField5.setText(String.valueOf(remaining));

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

private void updateBookingStatus(int id, String status, String reason) {
    try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/car_rental_db", "root", "")) {
         String sql = "UPDATE bookings SET status = ?, rejection_reason = ? WHERE id = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, status);
        pst.setString(2, reason);
        pst.setInt(3, id);
        pst.executeUpdate();
        
        loadBookingTableData();
        JOptionPane.showMessageDialog(null, "Booking " + status);
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(null, e.getMessage());
    }
}
 

private void loadBookingTableData() {
 

     DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
    model.setRowCount(0); 

    try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/car_rental_db", "root", "")) {
         String sql = "SELECT id, `order-id`, pickup_date, return_date, total_price, total_days, request_time, drivinglicence, status FROM bookings ORDER BY id DESC"; 
        
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
    model.addRow(new Object[]{
        rs.getInt("id"),                
        rs.getString("pickup_date"),     
        rs.getString("return_date"),
        rs.getDouble("total_price"),
        rs.getInt("total_days"),
        rs.getString("request_time"),
        rs.getString("drivinglicence"),  
        rs.getString("status"),          
        rs.getString("order-id"),
         ""
    });
}
    } catch (SQLException e) {
        e.printStackTrace();
    }

}
public void loadReturnTable() {
    DefaultTableModel model = (DefaultTableModel) jTable2.getModel();
    model.setRowCount(0); 

     String sql = "SELECT b.id, b.`order-id`, b.return_date, " +
                 "IFNULL(r.status, 'On Rent') AS status_text, " +
                 "IFNULL(r.fine, 0) AS fine_amt " +
                 "FROM bookings b " +
                 "LEFT JOIN returns r ON b.`order-id` = r.order_id " +
                 "WHERE b.status = 'Approved'";

    try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/car_rental_db", "root", "")) {
        PreparedStatement pst = con.prepareStatement(sql);
        ResultSet rs = pst.executeQuery();

        while (rs.next()) {
            model.addRow(new Object[]{
                rs.getInt("id"),
                rs.getString("order-id"),
                rs.getString("return_date"), 
                rs.getString("status_text"), 
                rs.getDouble("fine_amt")
            });
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}

private void deleteFromDatabase(String id) {
    try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/car_rental_db", "root", "")) {
        String sql = "DELETE FROM cars WHERE car_id = ?";
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, id); 
        
        if (pst.executeUpdate() > 0) {
            JOptionPane.showMessageDialog(this, "Car Deleted Successfully!");
            loadTableData(); 
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}

private void loadTableData() {
    int totalFleetSum = 0; 
 javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer();
centerRenderer.setHorizontalAlignment(javax.swing.JLabel.CENTER);

  for (int i = 0; i < 6; i++) {
    table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
}
    
    try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/car_rental_db", "root", "")) {
        String sql = "SELECT * FROM cars";
        ResultSet rs = con.prepareStatement(sql).executeQuery();

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); 

        while (rs.next()) {
            int currentCars = rs.getInt("number_of_cars");
            totalFleetSum += currentCars; 

            model.addRow(new Object[]{
                rs.getString("car_id"),      
                rs.getString("brand"),       
                rs.getString("model"),       
                currentCars,                 
                rs.getString("price_per_day"),  
                "",                      
                ""                        
            });
        }
        
         for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(totalFleetSum, i, 5); 
        }
        
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jLabel29 = new javax.swing.JLabel();
        jButton11 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        parent = new javax.swing.JPanel();
        secondpanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        submitbtn1 = new javax.swing.JButton();
        carid1 = new javax.swing.JTextField();
        brand1 = new javax.swing.JTextField();
        model1 = new javax.swing.JTextField();
        noofcar1 = new javax.swing.JTextField();
        perday1 = new javax.swing.JTextField();
        uploadbtn1 = new javax.swing.JButton();
        upload1 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton9 = new javax.swing.JButton();
        thirdpanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        updateidbtn = new javax.swing.JButton();
        updatebtn = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        updateid = new javax.swing.JTextField();
        updatebrand = new javax.swing.JTextField();
        updateidmodel = new javax.swing.JTextField();
        updateidnumber = new javax.swing.JTextField();
        updaterent = new javax.swing.JTextField();
        jLabel23 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        searchbtn = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        firstpanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jTextField2 = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jTextField3 = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jTextField6 = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jTextField5 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        fifthpanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jLabel28 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel30 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jPasswordField1 = new javax.swing.JPasswordField();
        jPasswordField2 = new javax.swing.JPasswordField();
        jPasswordField3 = new javax.swing.JPasswordField();
        jLabel25 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jLabel27 = new javax.swing.JLabel();
        jButton12 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(102, 102, 102));

        jButton1.setText("DashBord");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Add New Car");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Update the Car");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setText("Return Cars");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton6.setText("Booking");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jButton7.setText("Logout");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jLabel29.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jButton11.setText("Upload Picture");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jButton5.setText("live cars and Delete");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton10.setText("Change Password");
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButton7, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(46, 46, 46)
                        .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(80, 80, 80)
                        .addComponent(jButton11)))
                .addContainerGap(77, Short.MAX_VALUE))
            .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButton10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(jLabel29, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton11)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(99, 99, 99))
        );

        parent.setLayout(new java.awt.CardLayout());

        secondpanel.setBackground(new java.awt.Color(204, 204, 204));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setText("Add the New car");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setText("Car ID:");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setText("Brand:");

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setText("Model:");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel13.setText("Number of cars:");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel14.setText("Price Per Day:");

        submitbtn1.setText("Submit");
        submitbtn1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                submitbtn1ActionPerformed(evt);
            }
        });

        brand1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                brand1ActionPerformed(evt);
            }
        });

        uploadbtn1.setText("Upload Picture");
        uploadbtn1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadbtn1ActionPerformed(evt);
            }
        });

        upload1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel15.setText("Description:");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane3.setViewportView(jTextArea1);

        jButton9.setText("Clear all");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout secondpanelLayout = new javax.swing.GroupLayout(secondpanel);
        secondpanel.setLayout(secondpanelLayout);
        secondpanelLayout.setHorizontalGroup(
            secondpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, secondpanelLayout.createSequentialGroup()
                .addGroup(secondpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, secondpanelLayout.createSequentialGroup()
                        .addGap(401, 401, 401)
                        .addGroup(secondpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(secondpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE)
                                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(uploadbtn1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jButton9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(secondpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(carid1)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(upload1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(noofcar1)
                            .addComponent(perday1)
                            .addComponent(submitbtn1, javax.swing.GroupLayout.DEFAULT_SIZE, 178, Short.MAX_VALUE)
                            .addComponent(brand1)
                            .addComponent(model1)))
                    .addGroup(secondpanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel2)))
                .addGap(529, 529, 529))
        );
        secondpanelLayout.setVerticalGroup(
            secondpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(secondpanelLayout.createSequentialGroup()
                .addGap(70, 70, 70)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(secondpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(carid1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31)
                .addGroup(secondpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(brand1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addGap(43, 43, 43)
                .addGroup(secondpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(model1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addGroup(secondpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(noofcar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(36, 36, 36)
                .addGroup(secondpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(perday1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(secondpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(uploadbtn1)
                    .addComponent(upload1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(secondpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(secondpanelLayout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addGap(117, 117, 117)
                        .addComponent(jButton9))
                    .addGroup(secondpanelLayout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45)
                        .addComponent(submitbtn1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        parent.add(secondpanel, "card3");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setText("Update the Car");

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel16.setText("Car ID:");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel17.setText("Brand:");

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel18.setText("Model:");

        jLabel19.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel19.setText("No of Cars:");

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel20.setText("Proce per day:");

        updateidbtn.setText("Upload picture");

        updatebtn.setText("Update the car");
        updatebtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updatebtnActionPerformed(evt);
            }
        });

        jLabel21.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        updateid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateidActionPerformed(evt);
            }
        });

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel23.setText("Description:");

        jTextArea2.setColumns(20);
        jTextArea2.setRows(5);
        jScrollPane4.setViewportView(jTextArea2);

        searchbtn.setText("Search");
        searchbtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchbtnActionPerformed(evt);
            }
        });

        jButton8.setText("Clear all");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout thirdpanelLayout = new javax.swing.GroupLayout(thirdpanel);
        thirdpanel.setLayout(thirdpanelLayout);
        thirdpanelLayout.setHorizontalGroup(
            thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, thirdpanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(487, 487, 487))
            .addGroup(thirdpanelLayout.createSequentialGroup()
                .addGap(447, 447, 447)
                .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(thirdpanelLayout.createSequentialGroup()
                        .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel20, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(updateidbtn, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(updaterent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
                            .addComponent(updateidnumber, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel21, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(thirdpanelLayout.createSequentialGroup()
                        .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(updateidmodel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(updatebrand, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(updateid, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(thirdpanelLayout.createSequentialGroup()
                        .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton8))
                        .addGap(18, 18, 18)
                        .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(thirdpanelLayout.createSequentialGroup()
                                .addComponent(updatebtn, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                .addGap(36, 36, 36)
                .addComponent(searchbtn, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(392, 392, 392))
        );
        thirdpanelLayout.setVerticalGroup(
            thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(thirdpanelLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(updateid, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchbtn))
                .addGap(39, 39, 39)
                .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(updatebrand, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(updateidmodel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(39, 39, 39)
                .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(updateidnumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(41, 41, 41)
                .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(updaterent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(updateidbtn)
                    .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addGroup(thirdpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(updatebtn)
                    .addComponent(jButton8))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        parent.add(thirdpanel, "card4");

        firstpanel.setBackground(new java.awt.Color(255, 204, 153));

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));
        jPanel2.setBorder(javax.swing.BorderFactory.createMatteBorder(15, 0, 0, 0, new java.awt.Color(255, 153, 153)));

        jTextField1.setBackground(new java.awt.Color(204, 204, 204));
        jTextField1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel5.setText("  Total Live Cars");

        jPanel3.setBackground(new java.awt.Color(204, 204, 204));
        jPanel3.setBorder(javax.swing.BorderFactory.createMatteBorder(15, 1, 1, 1, new java.awt.Color(255, 153, 153)));

        jTextField2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(85, 85, 85)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(91, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(204, 204, 204));
        jPanel4.setBorder(javax.swing.BorderFactory.createMatteBorder(15, 1, 1, 1, new java.awt.Color(255, 153, 153)));

        jTextField3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(94, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(64, Short.MAX_VALUE))
        );

        jPanel5.setBackground(new java.awt.Color(204, 204, 204));
        jPanel5.setBorder(javax.swing.BorderFactory.createMatteBorder(15, 1, 1, 1, new java.awt.Color(255, 153, 153)));

        jTextField6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(68, 68, 68)
                .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBackground(new java.awt.Color(204, 204, 204));
        jPanel6.setBorder(javax.swing.BorderFactory.createMatteBorder(15, 1, 1, 1, new java.awt.Color(255, 153, 153)));

        jTextField5.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(78, 78, 78)
                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(89, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(66, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Total order in 30 days");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel6.setText("Rented Car");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel7.setText("Remaining car");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel9.setText("Total Earning for 30 days");

        jLabel26.setText("Dash Board");

        javax.swing.GroupLayout firstpanelLayout = new javax.swing.GroupLayout(firstpanel);
        firstpanel.setLayout(firstpanelLayout);
        firstpanelLayout.setHorizontalGroup(
            firstpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(firstpanelLayout.createSequentialGroup()
                .addGap(134, 134, 134)
                .addGroup(firstpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 129, Short.MAX_VALUE)
                .addGroup(firstpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, firstpanelLayout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(117, 117, 117))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, firstpanelLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(141, 141, 141)))
                .addGroup(firstpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addContainerGap(140, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, firstpanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(firstpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(157, 157, 157)
                .addGroup(firstpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(306, 306, 306))
            .addGroup(firstpanelLayout.createSequentialGroup()
                .addGap(409, 409, 409)
                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        firstpanelLayout.setVerticalGroup(
            firstpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, firstpanelLayout.createSequentialGroup()
                .addGap(59, 59, 59)
                .addComponent(jLabel26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 82, Short.MAX_VALUE)
                .addGroup(firstpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, firstpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(jLabel9)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(firstpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(97, 97, 97)
                .addGroup(firstpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(firstpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(135, 135, 135))
        );

        parent.add(firstpanel, "card2");

        fifthpanel.setBackground(new java.awt.Color(204, 204, 204));

        table.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Car ID", "Brand", "Model", "Number of cars", "Price per day", "Total Live Car", "Delete"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, true, true, true, true, true, true
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.setRowHeight(40);
        jScrollPane1.setViewportView(table);

        jScrollPane2.setViewportView(jScrollPane1);

        jLabel28.setText("Live cars and Delete");

        javax.swing.GroupLayout fifthpanelLayout = new javax.swing.GroupLayout(fifthpanel);
        fifthpanel.setLayout(fifthpanelLayout);
        fifthpanelLayout.setHorizontalGroup(
            fifthpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fifthpanelLayout.createSequentialGroup()
                .addGroup(fifthpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(fifthpanelLayout.createSequentialGroup()
                        .addGap(128, 128, 128)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 780, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(fifthpanelLayout.createSequentialGroup()
                        .addGap(449, 449, 449)
                        .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(326, Short.MAX_VALUE))
        );
        fifthpanelLayout.setVerticalGroup(
            fifthpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(fifthpanelLayout.createSequentialGroup()
                .addGap(83, 83, 83)
                .addComponent(jLabel28, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(454, Short.MAX_VALUE))
        );

        parent.add(fifthpanel, "card6");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "From", "TO", "PR", "Days", "Time", "Driving Licence", "Order status", "order ID", "Accept and Reject"
            }
        ));
        jScrollPane5.setViewportView(jTable1);

        jLabel30.setText("Booking Details");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(32, 32, 32)
                        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 1019, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(388, 388, 388)
                        .addComponent(jLabel30, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(183, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addComponent(jLabel30)
                .addGap(35, 35, 35)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(307, Short.MAX_VALUE))
        );

        parent.add(jPanel8, "card7");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("Manage Return car");

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "ID", "Order ID", "Return day", "Status", "Fine"
            }
        ));
        jScrollPane6.setViewportView(jTable2);

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(376, 376, 376)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(119, 119, 119)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 676, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(439, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(45, 45, 45)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 324, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(329, Short.MAX_VALUE))
        );

        parent.add(jPanel9, "card7");

        jLabel8.setText("Change Password");

        jLabel22.setText("Current Password");

        jLabel24.setText("New Password:");

        jLabel25.setText("Confirm Password:");

        jCheckBox1.setText("Show Password");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jCheckBox2.setText("Show Password");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        jCheckBox3.setText("Show Password");
        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox3ActionPerformed(evt);
            }
        });

        jLabel27.setText("Email:");

        jButton12.setText("Submit");
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jCheckBox1))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel7Layout.createSequentialGroup()
                        .addGap(431, 431, 431)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGap(69, 69, 69)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addComponent(jButton12)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(jPasswordField3)))))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jCheckBox3)
                            .addComponent(jCheckBox2))))
                .addGap(544, 544, 544))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(201, 201, 201)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel27, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox1)
                .addGap(18, 18, 18)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel25)
                            .addComponent(jPasswordField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addComponent(jCheckBox2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBox3)
                .addGap(31, 31, 31)
                .addComponent(jButton12)
                .addContainerGap(245, Short.MAX_VALUE))
        );

        parent.add(jPanel7, "card8");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(parent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(parent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        parent.removeAll();
        parent.add(firstpanel);
        parent.repaint();
        parent.revalidate();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
          parent.removeAll();
        parent.add(secondpanel);
        parent.repaint();
        parent.revalidate();
                                  
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
           parent.removeAll();
        parent.add(thirdpanel);
        parent.repaint();
        parent.revalidate();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
 parent.removeAll();
        parent.add(jPanel9);
        parent.repaint();
        parent.revalidate();
                                      
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        this.dispose();

     Login login = new Login();
    login.setVisible(true);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void brand1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_brand1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_brand1ActionPerformed

    private void updateidActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateidActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_updateidActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
         parent.removeAll();
        parent.add(fifthpanel);
        parent.repaint();
        parent.revalidate();
        
    }//GEN-LAST:event_jButton5ActionPerformed

    private void submitbtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_submitbtn1ActionPerformed
        // TODO add your handling code here:
        String id = carid1.getText();
        String brand = brand1.getText();
        String model = model1.getText();
        String cNum = noofcar1.getText();
        String price = perday1.getText();
        String desc = jTextArea1.getText(); 
        
        if (id.isEmpty() || brand.isEmpty() || savedImagePath == null || savedImagePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields and upload an image!");
            return;
        }

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/car_rental_db", "root", "")) {
             String checkSql = "SELECT car_id FROM cars WHERE car_id = ?";
            PreparedStatement checkPst = con.prepareStatement(checkSql);
            checkPst.setString(1, id);
            ResultSet rs = checkPst.executeQuery();
            
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "This Car ID (" + id + ") already exists! Please use a different ID.");
                return;
            }

             String sql = "INSERT INTO cars (car_id, brand, model, number_of_cars, price_per_day, image_path, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, id);
                pst.setString(2, brand);
                pst.setString(3, model);
                pst.setString(4, cNum);
                pst.setString(5, price);
                pst.setString(6, savedImagePath);
                pst.setString(7, desc);

                int result = pst.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Car Added Successfully!");
                    if (homePage != null) { homePage.loadCars(); }
                    loadTableData();
                    clearAddFields(); // Fixed: Now calling the method below
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database Error: " + e.getMessage());
        }
   
    }//GEN-LAST:event_submitbtn1ActionPerformed

    private void uploadbtn1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadbtn1ActionPerformed
        // TODO add your handling code here:

    JFileChooser chooser = new JFileChooser();
    chooser.showOpenDialog(null);
    File f = chooser.getSelectedFile();
    
    if (f != null) {
         savedImagePath = f.getAbsolutePath();
         upload1.setIcon(new ImageIcon(new ImageIcon(savedImagePath).getImage().getScaledInstance(upload1.getWidth(), upload1.getHeight(), Image.SCALE_SMOOTH)));
    }

    }//GEN-LAST:event_uploadbtn1ActionPerformed

    private void searchbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchbtnActionPerformed
    try {
        java.sql.Connection con = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/car_rental_db", "root", "");
        String sql = "SELECT * FROM cars WHERE car_id = ?";
        java.sql.PreparedStatement pst = con.prepareStatement(sql);
        pst.setString(1, updateid.getText());
        java.sql.ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            updatebrand.setText(rs.getString("brand"));
            updateidmodel.setText(rs.getString("model"));
            updateidnumber.setText(rs.getString("number_of_cars"));
            updaterent.setText(rs.getString("price_per_day"));
            jTextArea2.setText(rs.getString("description")); 
            
            savedImagePath = rs.getString("image_path");
            if (savedImagePath != null && !savedImagePath.isEmpty()) {
                javax.swing.ImageIcon icon = new javax.swing.ImageIcon(savedImagePath);
                java.awt.Image img = icon.getImage().getScaledInstance(jLabel21.getWidth(), jLabel21.getHeight(), java.awt.Image.SCALE_SMOOTH);
                jLabel21.setIcon(new javax.swing.ImageIcon(img));
            }
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "No car found with ID: " + updateid.getText());
            clearUpdateFields();  
        }
    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Search Error: " + e.getMessage());
    
}
    }//GEN-LAST:event_searchbtnActionPerformed

    private void updatebtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updatebtnActionPerformed

    try {
        java.sql.Connection con = java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/car_rental_db", "root", "");
        String sql = "UPDATE cars SET brand=?, model=?, number_of_cars=?, price_per_day=?, description=?, image_path=? WHERE car_id=?";
        java.sql.PreparedStatement pst = con.prepareStatement(sql);
        
        pst.setString(1, updatebrand.getText());
        pst.setString(2, updateidmodel.getText());
        pst.setString(3, updateidnumber.getText());
        pst.setString(4, updaterent.getText());
        pst.setString(5, jTextArea2.getText()); 
        pst.setString(6, savedImagePath);
        pst.setString(7, updateid.getText());

        int result = pst.executeUpdate();
        if (result > 0) {
            javax.swing.JOptionPane.showMessageDialog(this, "Car Updated Successfully!");
            loadTableData();
            clearUpdateFields(); 
            
            if (homePage != null) {
                homePage.loadCars();
            }
        }
    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Update Error: " + e.getMessage());
    }

    }//GEN-LAST:event_updatebtnActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
     clearUpdateFields();

    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
clearAddFields();         
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
           parent.removeAll();
        parent.add(jPanel8);
        parent.repaint();
        parent.revalidate();
    }//GEN-LAST:event_jButton6ActionPerformed

    public void loadProfilePicture() {
    try {
         Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/car_rental_db", "root", "");
        
         String sql = "SELECT profile_picture FROM profile_picture ORDER BY id DESC LIMIT 1";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            byte[] imgBytes = rs.getBytes("profile_picture");
            if (imgBytes != null) {
                 ImageIcon imageIcon = new ImageIcon(imgBytes);
                Image img = imageIcon.getImage();
                
                 Image scaledImg = img.getScaledInstance(jLabel29.getWidth(), jLabel29.getHeight(), Image.SCALE_SMOOTH);
                jLabel29.setIcon(new ImageIcon(scaledImg));
            }
        }
        con.close();
    } catch (Exception e) {
        System.out.println("Error loading image: " + e.getMessage());
    }
}
    
    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
                                           
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "jpg", "png", "jpeg", "gif");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        
        if (result == JFileChooser.APPROVE_OPTION) { 
            File selectedFile = fileChooser.getSelectedFile();
            String path = selectedFile.getAbsolutePath();
            
            try {
                 ImageIcon imageIcon = new ImageIcon(path);
                Image img = imageIcon.getImage();
                Image scaledImg = img.getScaledInstance(jLabel29.getWidth(), jLabel29.getHeight(), Image.SCALE_SMOOTH);
                jLabel29.setIcon(new ImageIcon(scaledImg));

                 Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/car_rental_db", "root", "");
                
                  String sql = "INSERT INTO profile_picture (profile_picture) VALUES (?)";
                  PreparedStatement ps = con.prepareStatement(sql);
                
                FileInputStream fis = new FileInputStream(selectedFile);
                ps.setBinaryStream(1, fis, (int) selectedFile.length());
                
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Image Saved Successfully!");
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
                e.printStackTrace();
            }
        } 
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
        if (jCheckBox1.isSelected()) {
            jPasswordField1.setEchoChar((char)0); 
        } else {
            jPasswordField1.setEchoChar('*');    
        }

    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        // TODO add your handling code here:
        if (jCheckBox2.isSelected()) {
            jPasswordField2.setEchoChar((char)0);
        } else {
            jPasswordField2.setEchoChar('*');
        }
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed
        // TODO add your handling code here:
        if (jCheckBox3.isSelected()) {
            jPasswordField3.setEchoChar((char)0);
        } else {
            jPasswordField3.setEchoChar('*');
        }
    }//GEN-LAST:event_jCheckBox3ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
          parent.removeAll();
        parent.add(jPanel7);
        parent.repaint();
        parent.revalidate();
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        // TODO add your handling code here:
        
    String email = jTextField4.getText().trim();
    String currentPassword = new String(jPasswordField1.getPassword());
    String newPassword = new String(jPasswordField2.getPassword());
    String confirmPassword = new String(jPasswordField2.getPassword());

     if (email.isEmpty() || currentPassword.isEmpty() ||
        newPassword.isEmpty() || confirmPassword.isEmpty()) {

        JOptionPane.showMessageDialog(this,
                "All fields are required!");
        return;
    }

     if (!newPassword.equals(confirmPassword)) {
        JOptionPane.showMessageDialog(this,
                "New password and Confirm password do not match!");
        return;
    }

    try {
        Connection con = DBConnection.getConnection();

         String checkSql =
            "SELECT * FROM users WHERE email=? AND password=?";
        PreparedStatement pst = con.prepareStatement(checkSql);
        pst.setString(1, email);
        pst.setString(2, currentPassword);

        ResultSet rs = pst.executeQuery();

        if (rs.next()) {

             String updateSql =
                "UPDATE users SET password=? WHERE email=?";
            PreparedStatement pst2 =
                con.prepareStatement(updateSql);
            pst2.setString(1, newPassword);
            pst2.setString(2, email);

            int updated = pst2.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(this,
                        "Password updated successfully!");

                 jPasswordField1.setText("");
                jPasswordField2.setText("");
                jPasswordField3.setText("");
            }

        } else {
            JOptionPane.showMessageDialog(this,
                    "Email or Current Password is incorrect!");
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Error: " + e.getMessage());
    }

    }//GEN-LAST:event_jButton12ActionPerformed

    
 
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new admin().setVisible(true));
    }
    
   private void clearUpdateFields() {
     updateid.setText("");
    updatebrand.setText("");
    updateidmodel.setText("");
    updateidnumber.setText("");
    updaterent.setText("");
    
     jTextArea2.setText(""); 
    
     jLabel21.setIcon(null);  
    
     savedImagePath = "";     
}
   private void clearAddFields() {
     carid1.setText("");
    brand1.setText("");
    model1.setText("");
    noofcar1.setText("");
    perday1.setText("");
    jTextArea1.setText("");
    upload1.setIcon(null);
    savedImagePath = "";  
}
   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField brand1;
    private javax.swing.JTextField carid1;
    private javax.swing.JPanel fifthpanel;
    private javax.swing.JPanel firstpanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JPasswordField jPasswordField3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField model1;
    private javax.swing.JTextField noofcar1;
    private javax.swing.JPanel parent;
    private javax.swing.JTextField perday1;
    private javax.swing.JButton searchbtn;
    private javax.swing.JPanel secondpanel;
    private javax.swing.JButton submitbtn1;
    private javax.swing.JTable table;
    private javax.swing.JPanel thirdpanel;
    private javax.swing.JTextField updatebrand;
    private javax.swing.JButton updatebtn;
    private javax.swing.JTextField updateid;
    private javax.swing.JButton updateidbtn;
    private javax.swing.JTextField updateidmodel;
    private javax.swing.JTextField updateidnumber;
    private javax.swing.JTextField updaterent;
    private javax.swing.JLabel upload1;
    private javax.swing.JButton uploadbtn1;
    // End of variables declaration//GEN-END:variables
}
