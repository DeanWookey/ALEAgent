/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import ale.io.Actions;
import ale.io.RLData;
import ale.screen.ScreenMatrix;
import rl.memory.DaubTransform;
import rl.memory.FourierTransform;
import rl.memory.HaarTransform;
import image.Utils;
import java.awt.image.BufferedImage;
import rl.domain.State;
import rl.functionapproximation.BSplineBasis;
import rl.functionapproximation.Basis;
import rl.functionapproximation.DaubBasis;
import rl.functionapproximation.DaubFullBasis;
import rl.functionapproximation.FourierMultiframeBasis;
import rl.functionapproximation.FullFourierBasis;
import rl.functionapproximation.HaarBasis;
import rl.functionapproximation.HaarBasis;
import rl.functionapproximation.IndicatorBasis;
import rl.functionapproximation.TransformBasis;
import rl.functionapproximation.WaveletTensorBasis;
import rl.learners.Learner;
import rl.learners.QLambda;
import rl.learners.QReplay;
import rl.learners.SarsaLambda;
import rl.memory.BSplineTransform;
import rl.memory.DaubFullTransform;
import rl.memory.Frame;
import rl.memory.FrameHistory;
import rl.memory.FrameHistory_Transform;
import rl.memory.FrameHistory_Transform2;

/**
 *
 * @author Craig Bester
 */
public class FixedWaveletAgent extends Agent {
    //FrameHistory history;
    FrameHistory_Transform2 history;
    Learner learner;

    final int imageSize = 84;
    final int framesPerState = 4;
    final int numPixels = framesPerState*imageSize*imageSize;

    // Function approximator parameters
    
    // Haar
    //final int baseScale = 2; final int maxScale = 4; final boolean normalise = false;
    // Daub
    //final int baseScale = 2; final int maxScale = -1; final int order = 4; final boolean normalise = false;
    // Daub Multiresolution
    final int baseScale = 2; final int maxScale = 3; final int order = 4; final boolean normalise = false;
    // BSpline
    //final int baseScale = 2; final int maxScale = -1; final int order = 2; final boolean normalise = false;
    
    
    boolean clipReward = true;
    //double alpha = 0.014 / (int) Math.pow(order + 1, 2); // should be #frames*#basis_functions?
    //double alpha = 0.014 / (imageSize * imageSize);
    //double alpha = 0.00025; // DeepMind
    double gamma = 0.95; double alpha = 1; //Sarsa
    //double gamma = 0.95; double alpha = 1; //Sarsa
    //double gamma = 0.95; //Q-learning
    //double lambda = 0.99; 
    double lambda = 0.95; 
    double epsilonStart = 0.05;
    double epsilonEnd = 0.05;
    double epsilonEvaluation = 0.01;
    private TransformBasis transformBasis;

    public FixedWaveletAgent(boolean gui, String game, String pipesBasename) {
        super(gui, game, pipesBasename);
        
        System.err.println("Fixed wavelet basis" + ", gamma " + gamma +  ", lambda " + lambda);
        System.err.println("Pixels: " + numPixels);
        System.err.println("Actions: " + numActions);
        
        initLearner();
    }

    @Override
    public final void initLearner() {
        //history = new FrameHistory(framesPerState);
        //history = new FrameHistory_Transform(framesPerState, new HaarTransform(imageSize, imageSize, baseScale,maxScale));
        //history = new FrameHistory_Transform(framesPerState, new DaubTransform(imageSize, imageSize, baseScale,maxScale,order));
        //history = new FrameHistory_Transform(framesPerState, new BSplineTransform(imageSize, imageSize, baseScale,maxScale,order));
        //history = new FrameHistory_Transform(framesPerState, new DaubFullTransform(imageSize, imageSize, baseScale,maxScale,order));
        //transforms 1 frame to another basis
        transformBasis = new DaubFullBasis(1, imageSize, imageSize, baseScale, maxScale, order, normalise);
        //transformBasis = new IndicatorBasis(1, imageSize, imageSize);
        //transformBasis = new FullFourierBasis(1, imageSize, imageSize, 10);
        history = new FrameHistory_Transform2(framesPerState, transformBasis);
        Basis[] functionApproximators = new Basis[numActions];

        //Basis test = new FourierMultiframeBasis(framesPerState,imageSize,imageSize,order);
        int numTerms = 0;
        for (int i = 0; i < actionSet.numActions; i++) {
            //functionApproximators[i] = new HaarFullCrossTilingBasis(framesPerState, imageSize, imageSize, baseScale, maxScale, normalise);
            //functionApproximators[i] = new DaubBasis(framesPerState, imageSize, imageSize, baseScale, maxScale, order, normalise);
            //functionApproximators[i] = new WaveletTensorBasis(framesPerState, imageSize, imageSize, baseScale, maxScale, order, normalise);
            //functionApproximators[i] = new BSplineBasis(framesPerState, imageSize, imageSize, baseScale, maxScale, order, normalise);
            //functionApproximators[i] = new DaubFullBasis(framesPerState, imageSize, imageSize, baseScale, maxScale, order, normalise);
            functionApproximators[i] = new IndicatorBasis(framesPerState, imageSize, imageSize, transformBasis.getNumFeatures()*framesPerState);
            // Random weight initialisation - DeepMind (bounds used unclear)
            //functionApproximators[i].randomiseWeights();
            // Randomised weights sometimes causes divergence with experience replay
            
            numTerms += functionApproximators[i].getNumFunctions();
        }
        System.err.println("Terms per frame = " + functionApproximators[0].getNumFunctions()/framesPerState);
        System.err.println("Terms per action = " + functionApproximators[0].getNumFunctions());
        System.err.println("Total number of terms = " + numTerms);

        learner = new SarsaLambda(numActions, functionApproximators[0].getNumFunctions(), functionApproximators);
        //learner = new QLambda(actionSet.numActions,functionApproximators[0].getNumFunctions(),functionApproximators);
        //learner = new QLearner(actionSet.numActions,functionApproximators[0].getNumFunctions(),functionApproximators);
        //learner = new QReplay(actionSet.numActions, functionApproximators[0].getNumFunctions(), functionApproximators);
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
            if(clipReward)
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
        BufferedImage bi = Utils.scale(Utils.matrixToImage(mat), imageSize, imageSize);
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
