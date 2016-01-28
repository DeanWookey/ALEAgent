/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.memory;

import java.util.LinkedList;
import rl.domain.State;

/**
 *
 * @author Craig Bester
 */
public class FrameHistory_Transform {
    /**
     * The list of recent frames
     */
    protected LinkedList<double[]> frames;

    /**
     * The maximum length of history we need to keep Should be same as images
     * per state (essential)
     */
    protected int maxLength;
    
    private Transform transform;

    /**
     * Create a new FrameHistory which needs to keep no more than the last
     * 'maxLength' frames.
     *
     * @param stateLength
     */
    public FrameHistory_Transform(int stateLength, Transform transform) {
        this.maxLength = stateLength;
        this.transform = transform;
        frames = new LinkedList<>();
    }

    /**
     * Append a new frame to the end of the history.
     *
     * @param frame
     */
    public void addFrame(Frame frame) {
        frames.addLast(transform.transform(frame));
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
