package rl.memory;

import java.util.LinkedList;
import rl.domain.State;
import rl.functionapproximation.TransformBasis;
import rl.functionapproximation.Transformation2D;

/**
 *
 * @author Craig Bester
 */
public class StateManager {

    /**
     * The list of recent frames
     */
    protected LinkedList<double[]> frames;

    /**
     * The maximum length of history we need to keep Should be same as images
     * per state (essential)
     */
    protected int maxLength;

    private final Transformation2D transform;
    private int imageWidth;
    private int imageHeight;

    /**
     * Create a new StateManager which needs to keep no more than the last
     * 'stateLength' states.
     *
     * @param stateLength How many previous states to concatenate into one
     * state.
     */
    public StateManager(int stateLength, int imageWidth, int imageHeight) {
        this(stateLength, null);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    /**
     * Create a new StateManager which needs to keep no more than the last
     * 'stateLength' states.
     *
     * @param stateLength How many previous states to concatenate into one state
     * @param basis The transformation to apply to each state. If null, the raw
     * state will be used.
     */
    public StateManager(int stateLength, Transformation2D basis) {
        this.maxLength = stateLength;
        this.transform = basis;
        frames = new LinkedList<>();
    }

    public int getNumFeatures() {
        if (transform != null) {
            return transform.getBasisFunctions().length * maxLength;
        } else {
            return imageWidth * imageHeight * maxLength;
        }
    }

    public void clear() {
        frames.clear();
    }

    /**
     * Append a new frame to the end of the history.
     *
     * @param frame
     */
    public void addFrame(Frame frame) {
        if (transform != null) {
            frames.addLast(transform.transform(frame.toArray()));
        } else {
            frames.addLast(frame.toArray());
        }
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
            int repeat = maxLength - frames.size();
            for (; j < repeat; j++) {
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
