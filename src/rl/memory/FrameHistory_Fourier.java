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
package rl.memory;

import ale.screen.ScreenMatrix;
import image.FourierTransform;
import image.Utils;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import rl.domain.State;
import rl.functionapproximation.FourierMultiframeBasis;

/**
 * A time-ordered list of frames.
 *
 * @author Marc G. Bellemare
 */
public class FrameHistory_Fourier {

    /**
     * The list of recent frames
     */
    protected LinkedList<double[]> frames;

    /**
     * The maximum length of history we need to keep Should be same as images
     * per state (essential)
     */
    protected int maxLength;
    
    private FourierTransform FT;

    /**
     * Create a new FrameHistory which needs to keep no more than the last
     * 'maxLength' frames.
     *
     * @param stateLength
     */
    public FrameHistory_Fourier(int stateLength, FourierTransform ft) {
        this.maxLength = stateLength;
        this.FT = ft;
        frames = new LinkedList<>();
    }

    /**
     * Append a new frame to the end of the history.
     *
     * @param frame
     */
    public void addFrame(Frame frame) {
        frames.addLast(FT.transform(frame));
        while (frames.size() > maxLength) {
            frames.removeFirst();
        }
    }

    /**
     * Removes the t-to-last frame. For example, removeLast(0) removes the last
     * frame added by addFrame(frame).
     */
    public void removeLast(int t) {
        frames.remove(frames.size() - t - 1);
    }

    public int maxHistoryLength() {
        return maxLength;
    }

    public State getState() {
        double[] img = frames.getFirst();
        int length = img.length;
        double[] ret = new double[maxLength * length];
        int j = 0;
        
        // Duplicate first frame until we have enough frames for a single state
        if (frames.size() < maxLength) {
            int repeat = maxLength-frames.size();
            for(;j<repeat;j++) {
                System.arraycopy(img, 0, ret, j * length, length);
            }
        }
        for (double[] f : frames) {
            System.arraycopy(f, 0, ret, j * length, length);
            j++;
        }
        return (new State(ret));
    }
    
}
