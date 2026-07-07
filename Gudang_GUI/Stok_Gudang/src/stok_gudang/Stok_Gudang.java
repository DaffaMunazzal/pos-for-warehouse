/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package stok_gudang;

import javax.swing.SwingUtilities;

/**
 *
 * @author Admin
 */
public class Stok_Gudang {

    /**
     * @param args the command line arguments
     */
    /**
     * Entry point aplikasi — meluncurkan layar login.
     * AuthFrame menggunakan Swing, sehingga harus dijalankan
     * di atas Event Dispatch Thread (EDT) via invokeLater.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(AuthFrame::new);
    }
    
}
