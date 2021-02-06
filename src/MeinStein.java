/*
 * MeinStein Copyright (c) 2007-2009 by Theo van der Storm
 *
 * This code is published on the CSVN website (http://www.csvn.nl)
 * to commemorate Theo. This is done with consent of Theo's family.
 *
 * The software is AS IS and under GPL v3, see below.
 *
 * This file is part of MeinStein Connect6.
 *
 * MeinStein is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MeinStein is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MeinStein Connect6.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

/**
 * MeinStein.java is a Java 1.4 Swing program.
 * It plays the game "EinStein wuerfelt nicht.".
 * Copyright (c) 2005, 2006 by Theo van der Storm
 *
 * @author      Theo van der Storm
 * @version     1.04.01
 * @see         tbd
 */
public class MeinStein {

    private static void createAndShowGUI() {
        MeinCtrl ctrl;
        MeinDisplay disp;
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame fdf = new JFrame("MeinStein v1.04.01");
        disp = new MeinDisplay();
        if (disp == null) {
            System.err.println("disp null");
        }
        disp.init();
        fdf.getContentPane().add(disp);
        fdf.setSize(330, 352);
        fdf.setVisible(true);
        JFrame fcf = new JFrame("MeinStein Controls");
        fcf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ctrl = new MeinCtrl(disp);
        if (ctrl == null) {
            System.err.println("ctrl null");
        }
        fcf.getContentPane().add(ctrl);
        fcf.pack();
        fcf.setVisible(true);
        StatV.init();
    }

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                System.out.println("Your java.version is: " +
                    System.getProperty("java.version"));
                createAndShowGUI();
            }
        });
    }
}

class MeinDisplay extends JPanel implements MouseListener {

    private static final long serialVersionUID = 5794155610416022746L;
    final int sqWidth = 64,  stBorder = sqWidth / 6;
    BufferedImage img = new BufferedImage(
        5 * sqWidth, 5 * sqWidth, BufferedImage.TYPE_INT_BGR);
    MeinCtrl ctrl;
    Color myBlue = new Color(20, 20, 200);
    Color myOran = new Color(240, 180, 120);
    Color darkStone = myBlue, darkSquare = Color.darkGray;
    Color lightStone = myOran, lightSquare = Color.gray;
    Font myFont = new Font(null, Font.BOLD, sqWidth / 2);
    int p1, p2 = -1;
    boolean rotatedBoard = true;

    public void flipBoard(boolean mode) {
        rotatedBoard = mode;
    }

    public void setColStone(boolean colStone) {
        if (colStone) {	// Coloured stones with dark appearance
            darkStone = myBlue;
            darkSquare = Color.darkGray;
            lightStone = myOran;
            lightSquare = Color.gray;
        } else {		// Black & White stones with light appearance
            darkStone = Color.black;
            darkSquare = Color.gray;
            lightStone = Color.white;
            lightSquare = Color.lightGray;
        }
    }

    public void drawSquare(boolean x, int num, int c, int r) {
        drawSquare(x, num, c, r, false);
    }

    public void drawSquare(boolean x, int num, int c, int r, boolean highLight) {
        Graphics2D g = img.createGraphics();
        byte[] fig = new byte[1];
        p2 = -1;
        if (rotatedBoard) {
            r = 4 - r;
            c = 4 - c;
        }
        if (highLight) {
            g.setColor(Color.green);
        } else {
            g.setColor((r + c) % 2 == 0 ? darkSquare : lightSquare);
        }
        g.fill(new Rectangle(c * sqWidth, r * sqWidth, sqWidth, sqWidth));
        if (num != 0) {
            g.setColor(x ? lightStone : darkStone);
            g.fill(new RoundRectangle2D.Float(
                c * sqWidth + stBorder, r * sqWidth + stBorder,
                sqWidth - 2 * stBorder, sqWidth - 2 * stBorder,
                stBorder, stBorder));
            g.setColor(x ? darkStone : lightStone);
            g.setFont(myFont);
            fig[0] = (byte) ('0' + num);
            g.drawBytes(fig, 0, 1,
                c * sqWidth + sqWidth * 7 / 20 + 1, r * sqWidth + sqWidth * 7 / 10);
        }
        g.dispose();
        if (highLight) {
            paintImmediately(0, 0, getWidth(), getHeight());
        }
        repaint();
    }

    public void mousePressed(MouseEvent e) {
        if (e.getX() >= 5 * sqWidth || e.getY() >= 5 * sqWidth) {
            return;
        }
        p1 = p2;
        p2 = e.getX() / sqWidth + 5 * (e.getY() / sqWidth);
        if (rotatedBoard) {
            p2 = 24 - p2;
        }
        p2 = ctrl.tryMove(p1, p2);
    }

    public void refCtrl(MeinCtrl c) {
        ctrl = c;
    }

    public void init() {
        setColStone(true);
        addMouseListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.drawImage(img, 0, 0, null);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
} // MeinDisplay
