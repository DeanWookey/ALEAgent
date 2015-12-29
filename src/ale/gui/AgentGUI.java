/*
 * Java Arcade Learning Environment (A.L.E) Agent
 *  Copyright (C) 2011-2012 Marc G. Bellemare <mgbellemare@ualberta.ca>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ale.gui;

import java.awt.image.BufferedImage;
import javax.swing.JFrame;

/** GUI for the Java ALE agent.
 * 
 * @author Marc G. Bellemare
 */
public final class AgentGUI extends JFrame implements AbstractUI {
    /** An object in which we display the screen image */
    protected final ScreenDisplay panel;

    /** Create a new GUI
     * 
     */
    public AgentGUI(){
        // Create the keyboard and image panel
        panel = new ScreenDisplay();
        add(panel);

        this.setSize(panel.getPreferredSize());

        pack();
        setLocationRelativeTo(null);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /** When die() is called, we want to safely close the GUI */
    public void die() {
        this.dispose();
    }

    public void setImage(BufferedImage img) {
        panel.setImage(img);
    }

    public void setCenterString(String s) {
        panel.setCenterString(s);
    }

    public void addMessage(String s) {
        panel.addMessage(s);
    }

    public void updateFrameCount() {
        panel.updateFrameCount();
    }

    public void refresh() {
        this.repaint();
    }

    @Override
    public int getKeyboardAction() {
        return 0;
    }

    @Override
    public boolean quitRequested() {
        return false;
    }
}
