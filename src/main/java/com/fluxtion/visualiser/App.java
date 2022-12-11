/*
 * Copyright (C) 2017 V12 Technology Limited
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.fluxtion.visualiser;

import java.awt.*;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.io.File;

/**
 * Entry point for Fluxtion graph visualiser.
 *
 * @author Greg Higgins (greg.higgins@V12technology.com)
 */
public class App {
    private static File fileToOpen;

    static {
        try {
            Desktop.getDesktop().setOpenFileHandler(new FileHandler());
        } catch (Exception ex) {
        }
    }

    public static void main(String[] args) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VisualiserAppFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        VisualiserAppFrame frame = new VisualiserAppFrame();
        frame.display();
        if (fileToOpen != null) {
            System.out.println("loading - " + fileToOpen);
            frame.loadFile(fileToOpen);
        } else if (args.length > 0) {
            System.out.println("loading - " + args[0]);
            frame.loadFile(new File(args[0]));
        }
    }

    public static class FileHandler implements OpenFilesHandler {

        @Override
        public void openFiles(OpenFilesEvent e) {
            // Handle file open event on Mac
            System.out.println("Received open files event " + e.toString());
            if (!e.getFiles().isEmpty()) {
                fileToOpen = e.getFiles().get(0);
            }
        }
    }
}
