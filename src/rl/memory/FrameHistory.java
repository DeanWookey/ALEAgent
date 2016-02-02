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
public class FrameHistory implements Cloneable {

    /**
     * The list of recent frames
     */
    protected LinkedList<Frame> frames;

    /**
     * The maximum length of history we need to keep Should be same as images
     * per state (essential)
     */
    protected int maxLength;

    /**
     * Create a new FrameHistory which needs to keep no more than the last
     * 'maxLength' frames.
     *
     * @param stateLength
     */
    public FrameHistory(int stateLength) {
        this.maxLength = stateLength;
        frames = new LinkedList<>();
    }

    /**
     * Append a new frame to the end of the history.
     *
     * @param frame
     */
    public void addFrame(Frame frame) {
        frames.addLast(frame);
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

    /**
     * Returns the t-to-last frame. For example, getLastFrame(0) returns the
     * last frame added by addFrame(frame).
     */
    public Frame getLastFrame(int t) {
        return frames.get(frames.size() - t - 1);
    }
    
    /*
    public State getState() {
        Frame[] arr = new Frame[maxLength];
        
        int j = 0;
        // Duplicate first frame until we have enough frames for a single state
        if (frames.size() < maxLength) {
            int repeat = maxLength-frames.size();
            for(;j<repeat;j++) {
                arr[j] = frames.getFirst();
            }
        }
        for(Frame f : frames) {
            arr[j] = f;
            j++;
        }
        
        return new State(arr);
    }
    */

    
    public State getState() {
        
                double[] img = frames.getFirst().toArray();
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
        for (Frame f : frames) {
            System.arraycopy(f.toArray(), 0, ret, j * length, length);
            j++;
        }
        return (new State(ret));
        /*
        double[][] img = frames.getFirst().image;
        int height = img.length;
        int width = img[0].length;
        double[] ret = new double[maxLength * height * width];
        int j = 0;
        
        // Duplicate first frame until we have enough frames for a single state
        if (frames.size() < maxLength) {
            int repeat = maxLength-frames.size();
            for(;j<repeat;j++) {
                for (int i = 0; i < height; i++) {
                    System.arraycopy(img[i], 0, ret, i * j * height, width);
                }
            }
        }
        for (Frame f : frames) {
            for (int i = 0; i < height; i++) {
                System.arraycopy(f.image[i], 0, ret, i * j * height, width);
            }
            j++;
        }
        return (new State(ret));
        */
    }

    /*
    double[] minValues;
    double[] maxValues;

    public State getScaledState() {
        double[][] img = frames.getFirst().image;
        int height = img.length;
        int width = img[0].length;

        // Scaling
        if (minValues == null || maxValues == null) {
            minValues = new double[maxLength * height * width];
            maxValues = new double[maxLength * height * width];
            Arrays.fill(minValues, 0);
            Arrays.fill(maxValues, 0xFFFFFF);
        }

        double[] ret = new double[maxLength * height * width];
        int j = 0;
        
        // Duplicate first frame until we have enough frames for a single state
        if (frames.size() < maxLength) {
            int repeat = maxLength-frames.size();
            for(;j<repeat;j++) {
                for (int i = 0; i < height; i++) {
                    System.arraycopy(img[i], 0, ret, i * j * height, width);
                }
            }
        }
        for (Frame f : frames) {
            for (int i = 0; i < height; i++) {
                System.arraycopy(f.image[i], 0, ret, i * j * height, width);
            }
            j++;
        }
        return (new ScaledState(ret, minValues, maxValues));
    }
    */
    
    FourierMultiframeBasis fa;
    int order = 15;
    public State getFourierState() {
        double[][] img = frames.getFirst().image;
        int height = img.length;
        int width = img[0].length;
        
        if(fa == null) {
            fa = new FourierMultiframeBasis(maxLength, height, width, order);
        }
        
        
        double[] ret = new double[maxLength * height * width];
        int j = 0;
        
        // Duplicate first frame until we have enough frames for a single state
        if (frames.size() < maxLength) {
            int repeat = maxLength-frames.size();
            for(;j<repeat;j++) {
                for (int i = 0; i < height; i++) {
                    System.arraycopy(img[i], 0, ret, i * j * height, width);
                }
            }
        }
        for (Frame f : frames) {
            for (int i = 0; i < height; i++) {
                System.arraycopy(f.image[i], 0, ret, i * j * height, width);
            }
            j++;
        }
        
        return (new State(fa.computeFeatures(ret)));
    }

    @Override
    public Object clone() {
        try {
            FrameHistory obj = (FrameHistory) super.clone();

            obj.frames = new LinkedList<Frame>();
            // Copy over the frames; we do not clone them
            for (Frame screen : this.frames) {
                obj.frames.add(screen);
            }
            return obj;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
