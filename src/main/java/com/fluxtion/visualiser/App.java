/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fluxtion.visualiser;

/**
 * Entry point for Fluxtion graph visualiser.
 * 
 * @author Greg Higgins (greg.higgins@V12technology.com)
 */
public class App {

    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VisualiserAppFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        VisualiserAppFrame frame = new VisualiserAppFrame();
        frame.display();
    }
}
