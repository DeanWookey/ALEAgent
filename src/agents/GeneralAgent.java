package agents;

import ale.io.Actions;
import ale.io.RLData;
import ale.screen.ScreenMatrix;
import image.Utils;
import java.awt.image.BufferedImage;
import rl.domain.State;
import rl.functionapproximation.BasisFunction;
import rl.functionapproximation.TransformBasis;
import rl.functionapproximation.Transformation2D;
import rl.memory.Frame;
import rl.memory.StateManager;

/**
 *
 * @author itchallengeuser
 */
public abstract class GeneralAgent extends Agent {

    final StateManager stateManager;

    boolean clipReward = true;

    final int imageWidth;
    final int imageHeight;

    private int lastAction;
    private State lastState;
    private final int numFramesPerState;

    public GeneralAgent(boolean gui, String game, String pipeBasename) {
        this(gui, game, pipeBasename, 82, 82, 1);
    }

    public GeneralAgent(boolean gui, String game, String pipeBasename, int imageWidth, int imageHeight, int numFramesPerState) {
        super(gui, game, pipeBasename);
        this.imageHeight = imageHeight;
        this.imageWidth = imageWidth;

        this.numFramesPerState = numFramesPerState;
        stateManager = getStateManager();
    }

    protected StateManager getStateManager() {
        return new StateManager(numFramesPerState, imageWidth, imageHeight);
    }

    public int getNumFeatures() {
        return stateManager.getNumFeatures();
    }

    /**
     * Starts an episode and returns the agent's first action.
     *
     * @param s The initial state of the environment.
     * @return The first action to take
     */
    public abstract int startEpisode(State s);

    /**
     * Returns the agent's next action in state currentState. For convenience,
     * the previous state and previous action are also provided.
     *
     * @param previousState The previous state of the environment.
     * @param previousAction The last action taken
     * @param currentState The current state of the environement.
     * @param reward The reward obtained for moving from previousState to
     * currentState with action previousAction.
     * @return The next action to take.
     */
    public abstract int step(State previousState, int previousAction, State currentState, double reward);

    /**
     * Ends the episode.
     *
     * @param previousState The previous state.
     * @param previousAction The last action taken.
     * @param reward The final reward received for taking action previousAction
     * in state previousState.
     */
    public abstract void endEpisode(State previousState, int previousAction, double reward);

    @Override
    public void rlStep(ScreenMatrix image, RLData rlData) {
        stateManager.addFrame(new Frame(convertImage(image)));

        // Obtain the feature vector from frame history
        State s = stateManager.getState();
        s.setTerminal(rlData.isTerminal);

        if (firstStep) {
            lastAction = startEpisode(s);
            learnerAction = lastAction;
            lastState = s;
            firstStep = false;
        } else {
            boolean terminal = rlData.isTerminal;
            double reward = rlData.reward;

            //restrict reward to -1,0,1
            if (clipReward) {
                reward = clipReward(reward);
            }
            // Regular RL step
            if (!terminal) {
                lastAction = step(lastState, lastAction, s, reward);
                learnerAction = lastAction;
                lastState = s;
            } // When we receive the terminal signal, we disregard the screen data
            //  and instead transit to the 'null state'
            else {
                endEpisode(lastState, lastAction, reward);
                stateManager.clear();
                lastState = null;
                lastAction = 0;
                // As a sanity check we set learnerAction; this is overriden by the reset
                learnerAction = Actions.map("player_a_noop");

                // We will want to reset, since we have reached the end of the episode
                requestReset = true;

                // Print the episode number
                System.err.println("Episode " + episodeNumber + " finished... " + episodeReward);

                if (training) {
                    if (numFrames >= numTrainingFrames) {
                        System.err.println("Finished training after " + numTrainingFrames + " frames over " + episodeNumber + " episodes");
                        training = false;
                        System.err.println("Starting evaluation...");
                    }
                } else {
                    cumulativeReward += episodeReward;
                }

                episodeReward = 0;

                episodeNumber++;
                if (training) {
                    numTrainingEpisodes++;
                }

                // End evaluation
                if (!training && (episodeNumber - numTrainingEpisodes) > numEvaluationEpisodes) {
                    System.err.println(numEvaluationEpisodes + " episodes, terminating...");
                }
            }
        }
    }

    private double[][] convertImage(ScreenMatrix image) {
        // Colourise (NTSC)
        // TODO: map NTSC to grayscale in one step to improve performance
        int[][] mat = converter.convert(image.matrix);

        // Crop the 210x160 image to 160x160
        // We remove the mostly blank top and bottommost sections
        mat = Utils.crop(mat, 0, 33, 160, 160);

        // Grayscale
        Utils.grayscale(mat);

        // Scale down to 84x84
        BufferedImage bi = Utils.scale(Utils.matrixToImage(mat), imageWidth, imageHeight);
        //double[][] img = Utils.imageToDoubleMatrix(bi);

        // Scale pixel values onto [0,1]
        //Utils.scalePixelValues(img, 0, 0xFFFFFF);
        double[][] img = Utils.scalePixelValues(bi, 0, 0xFFFFFF);

        //double[][] img = Utils.imageIntToMatrixDouble(bi);
        return img;
    }

    private double clipReward(double reward) {
        if (reward < 0) {
            reward = -1;
        } else if (reward > 0) {
            reward = 1;
        }
        return reward;
    }

    public boolean isClipReward() {
        return clipReward;
    }

    public void setClipReward(boolean clipReward) {
        this.clipReward = clipReward;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public int getNumFramesPerState() {
        return numFramesPerState;
    }

}
