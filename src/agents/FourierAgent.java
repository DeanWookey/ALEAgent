/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import ale.io.Actions;
import ale.io.RLData;
import ale.screen.ScreenMatrix;
import image.FourierTransform;
import image.Utils;
import java.awt.image.BufferedImage;
import rl.domain.State;
import rl.functionapproximation.Basis;
import rl.functionapproximation.FourierMultiframeBasis;
import rl.learners.QReplay;
import rl.memory.Frame;
import rl.memory.FrameHistory_Fourier;

/**
 *
 * @author Craig Bester
 */
public class FourierAgent extends Agent {

    //FrameHistory history;

    FrameHistory_Fourier history;

    final int imageSize = 84;
    final int framesPerState = 4;

    final int order = 83;
    //double alpha = 1;
    double alpha = 0.014 / (int) Math.pow(order + 1, 2); // should be #frames*#basis_functions?
    //double alpha = 0.014 / (imageSize * imageSize);
    //double alpha = 0.00025; // DeepMind
    //double gamma = 0.99; //Sarsa
    double gamma = 0.95; //Q-learning
    double lambda = 0.95;
    double epsilonStart = 1.0;
    double epsilonEnd = 0.1;
    double epsilonEvaluation = 0.05;

    public FourierAgent(boolean gui, String game, String pipesBasename) {
        super(gui, game, pipesBasename);
        initLearner();
    }

    @Override
    public final void initLearner() {
        //history = new FrameHistory(framesPerState);
        history = new FrameHistory_Fourier(framesPerState, new FourierTransform(imageSize, imageSize, order));

        Basis[] functionApproximators = new Basis[actionSet.numActions];

        //Basis test = new FourierMultiframeBasis(framesPerState,imageSize,imageSize,order);
        for (int i = 0; i < actionSet.numActions; i++) {
            //functionApproximators[i] = new LinearBasis(test.getNumFunctions());
            //functionApproximators[i].setShrink(test.getShrink());

            //functionApproximators[i] = new LinearBasis(numFrames * imageSize * imageSize);
            functionApproximators[i] = new FourierMultiframeBasis(framesPerState, imageSize, imageSize, order);

            // Random weight initialisation - DeepMind (bounds used unclear)
            functionApproximators[i].randomiseWeights();
            // Randomised weights sometimes causes learning divergence
        }

        //learner = new SarsaLambda(actionSet.numActions, functionApproximators[0].getNumFunctions(), functionApproximators);
        //learner = new QLambda(actionSet.numActions,functionApproximators[0].getNumFunctions(),functionApproximators);
        //learner = new QLearner(actionSet.numActions,functionApproximators[0].getNumFunctions(),functionApproximators);
        learner = new QReplay(actionSet.numActions, functionApproximators[0].getNumFunctions(), functionApproximators);
        //learner = new QTarget(actionSet.numActions,functionApproximators[0].getNumFunctions(),functionApproximators);
        //learner = new QTargetReplay(actionSet.numActions,functionApproximators[0].getNumFunctions(),functionApproximators);

        learner.setAlpha(alpha);
        learner.setGamma(gamma);
        learner.setLambda(lambda);
        learner.setEpsilon(epsilonStart);
    }

    @Override
    public void rlStep(ScreenMatrix image, RLData rlData) {

        history.addFrame(new Frame(convertImage(image)));

        // Obtain the feature vector from frame history
        //State s = history.getState();
        State s = history.getState();
        s.setTerminal(rlData.isTerminal);

        if (firstStep) {
            // On the first step, no reward is computed
            learnerAction = learner.agent_start(s);

            firstStep = false;
        } else {
            boolean terminal = rlData.isTerminal;
            double reward = rlData.reward;

            //restrict reward to -1,0,1
            reward = clippedReward(reward);
            // Regular RL step
            if (!terminal) // we don't immediately receive the new screen, so the agent has to rely on its previous data
            {
                learnerAction = learner.agent_step(reward, s);
            } // When we receive the terminal signal, we disregard the screen data
            //  and instead transit to the 'null state'
            else {
                episodeEnd(reward);
            }
        }

        reduceEpsilon();
    }

    /*
     * Perform an end-of-episode learning step
     */
    protected void episodeEnd(double reward) {
        learner.agent_end(reward);
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
        BufferedImage bi = Utils.scale(Utils.matrixToImage(mat), 84, 84);
        //double[][] img = Utils.imageToDoubleMatrix(bi);

        // Cosine transform
        //double[][] img = CosineTransform.transform(bi);
        // Scale pixel values onto [0,1]
        //Utils.scalePixelValues(img, 0, 0xFFFFFF);
        double[][] img = Utils.scalePixelValues(bi, 0, 0xFFFFFF);

        //double[][] img = Utils.imageIntToMatrixDouble(bi);
        return img;
    }

    private double clippedReward(double reward) {
        if (reward < 0) {
            reward = -1;
        } else if (reward > 0) {
            reward = 1;
        }
        return reward;
    }

    public void reduceEpsilon() {
        if (numFrames < numRandomReductionFrames) {
            learner.setEpsilon((1 - (double) numFrames / (double) numRandomReductionFrames) * (epsilonStart - epsilonEnd) + epsilonEnd);
        } else {
            if (training) {
                learner.setEpsilon(epsilonEnd);
            } else {
                learner.setEpsilon(epsilonEvaluation);
            }
        }
    }
}
