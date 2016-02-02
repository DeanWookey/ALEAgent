/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import ale.io.Actions;
import ale.io.RLData;
import ale.screen.ScreenMatrix;
import image.Utils;
import java.awt.image.BufferedImage;
import rl.domain.State;
import rl.functionapproximation.DaubFullBasis;
import rl.functionapproximation.FullFourierBasis;
import rl.functionapproximation.adaptive_wavelets.ATCext;
import rl.functionapproximation.adaptive_wavelets.AdaptiveBasis;
import rl.functionapproximation.adaptive_wavelets.FunctionApproximator;
import rl.functionapproximation.adaptive_wavelets.IFDD;
import rl.functionapproximation.adaptive_wavelets.IFDDTiling;
import rl.functionapproximation.adaptive_wavelets.IFDDext;
import rl.functionapproximation.adaptive_wavelets.IFDDplus;
import rl.functionapproximation.adaptive_wavelets.QApproximator;
import rl.functionapproximation.adaptive_wavelets.Sample;
import rl.functionapproximation.adaptive_wavelets.SarsaLambdaAlphaScale;
import rl.memory.Frame;
import rl.memory.FrameHistory;
import rl.memory.FrameHistory_Transform2;

/**
 *
 * @author Craig Bester
 */
public class AdaptiveWaveletAgent extends Agent {

    QApproximator q;
    SarsaLambdaAlphaScale learner;
    FrameHistory_Transform2 history;

    final int imageSize = 28;
    final int framesPerState = 4;


    double epsilonStart = 0.05;
    double epsilonEnd = 0.05;
    double epsilonEvaluation = 0.01;

    //Parameters
    int numExperiments = 1;
    int adaptiveEpisodes = 20;
    int totalEpisodes = 20;
    
    double alpha = 1;
    double gamma = 0.95;
    double lambda = 0.95;
    double epsilon = 0.01;

    // --------Play with these parameters--------------------------------
    double combineTol = 0.2; // below this it will combine two functions
    double splitTol = 500; // past this it will split and create two functions

    //FA Parameters
    int order = 2;
    int baseScale = 0;
    int numTiles = order + (int) Math.pow(2, baseScale);
    //Methods: select one
    boolean mMAWB = true;
    boolean mIFDDext = false;
    boolean mATCext = false;
    boolean mtMAWB = false;
    boolean mtIFDDext = false;
    boolean mtATCext = false;
    boolean mIFDD = false;
    boolean mIFDDplus = false;
    double tempMAWBStats[][] = new double[totalEpisodes][2];
    String method = "Fixed";
    
    int ifddadd, atcadd;
    int tempNewTerms;
    
    boolean combine;
    boolean split;
    String type;
    
    int maxTerms = 1000;
    int terms;
    double totalReward;
    Sample samp;
    double results[][][] = new double[numExperiments][totalEpisodes][2];
    double currVal;
    State lastState;
    
    int experimentNumber = 0;
    private FullFourierBasis transformBasis;
    private int numDimensions;

    public AdaptiveWaveletAgent(boolean gui, String game, String pipesBasename) {
        super(gui, game, pipesBasename);
        
        if (mMAWB) {
            System.err.println("Multiscale Adaptive Wavelet method using BSplines");
            method = "MAWB";
        }
        if (mtMAWB) {
            System.err.println("Multiscale Adaptive Wavelet method using Tiling");
            method = "MAWB";
        }
        if (mIFDDext) {
            System.err.println("IFDD+ extension method using BSplines");
            method = "IFDDext";
        }
        if (mATCext) {
            System.err.println("ATC extension method using BSplines");
            method = "ATC";
        }
        if (mtIFDDext) {
            System.err.println("IFDD+ extension method using Tiling");
            method = "IFDDext";
        }
        if (mtATCext) {
            System.err.println("ATC extension method using Tiling");
            method = "ATC";
        }
        if (mIFDD) {
            System.err.println("IFDD method using Tiling");
            method = "IFDD";
        }
        if (mIFDDplus) {
            System.err.println("IFDD+ method using Tiling");
            method = "IFDD+";
        }
        combine = (mMAWB || mIFDDext || mtMAWB || mtIFDDext);
        split = (mMAWB || mtMAWB || mATCext || mtATCext);
        
        if (mMAWB || mIFDDext || mATCext) {
            type = "bspline";          
            System.err.println(type + ", order " + order + ", baseScale " + baseScale);
        } else {
            type = "tiling";
            System.err.println(type + ", numTiles " + numTiles);
        }
        
        System.err.println("Arcade Learning Environment" + ", gamma " + gamma + ", epsilon " + epsilon + ", lambda " + lambda);
        if (combine || mIFDD || mIFDDplus) {
            System.err.println("combining tolerance " + combineTol);
        }
        if (split) {
            System.err.println("splitting tolerance " + splitTol);
        }
                initLearner();
        System.err.println("Dimensions: " + numDimensions);
        System.err.println("Actions: " + actionSet.numActions);
        
        // Setup learner

    }

    @Override
    public final void initLearner() {

        transformBasis = new FullFourierBasis(1, imageSize, imageSize, 10);
        history = new FrameHistory_Transform2(framesPerState, transformBasis);
        numDimensions = framesPerState * transformBasis.getNumFeatures();
        // Initialise function approximators
        int numActions = actionSet.numActions;
        FunctionApproximator FAs[] = new FunctionApproximator[numActions];
        for (int k = 0; k < numActions; k++) {
            if (mIFDD || mIFDDplus) {
                FAs[k] = new IFDDTiling(numTiles, numDimensions);
            } else {
                if (type.equalsIgnoreCase("tiling")) {
                    FAs[k] = new AdaptiveBasis(combine, split, type, numTiles, numDimensions, order);
                    System.err.println("Number of terms for "+k+": "+FAs[k].getNumTerms());
                } else {
                    FAs[k] = new AdaptiveBasis(combine, split, type, baseScale, numDimensions, order);
                }
            }
            /*if (i == 0) {
                System.out.println("Action " + k + " number of terms: " + FAs[k].getNumTerms());
            }*/
            System.err.println("Number of terms: " +FAs[k].getNumTerms());
        }
        
        // Initialise learner
        alpha = FAs[0].getTerms()[0].getShrink();
        q = new QApproximator(FAs);
        System.err.println("Total Number of terms: " +q.getNumTerms());
        learner = new SarsaLambdaAlphaScale(q, numActions, alpha, gamma, lambda, epsilon);
        //learner.setEpsilon(epsilonStart);
        
    }
    
    @Override
    public void run() {
        boolean done = false;
        learner.startEpisode();
        
        System.err.print("Experiment " + experimentNumber +" ");
        ifddadd = 0;
        atcadd = 0;
        
        // Loop until we're done
        while (!done) {
            // Obtain relevant data from ALE
            done = io.observe();
            // The I/O channel will return true once EOF is received
            if (done) {
                break;
            }

            // Obtain the screen matrix
            ScreenMatrix screen = io.getScreen();
            // send it to the ui
            if (useGUI) {
                updateImage(screen);
            }
            // ... and to the agent
            observe(screen, io.getRLData());

            
            episodeReward += io.getRLData().reward;

            // Request an action from the agent
            int action = selectAction();
            // Send it back to ALE
            done = io.act(action);
            
            

            // The agent also tells us when to terminate
            done |= shouldTerminate();
        }

        double average = 0;
        double ave[] = new double[totalEpisodes];
        double var[] = new double[totalEpisodes];
        double termsAve[] = new double[totalEpisodes];
        double termsVar[] = new double[totalEpisodes];
        String key = "'Domain, gamma, epsilon, numExperiments, method, bfType, order, basescale, combTol, splitTol, return, stats'";
        System.err.print("{"+key+", 'Arcade Learning Environment', "+gamma+", "+epsilon+", "+numExperiments+ ", ");
        System.err.print("'"+method+"', '"+type+"', "+order+", "+baseScale+", "+combineTol+", "+splitTol);
        System.err.print(",[");
        for (int f = 0; f < totalEpisodes; f++) {
            for (int g = 0; g < numExperiments; g++) {
                ave[f] += results[g][f][0] / ((double) numExperiments);
                termsAve[f] += results[g][f][1] / ((double) numExperiments);
            }

            for (int g = 1; g < numExperiments - 1; g++) {
                var[f] += Math.pow(results[g][f][0] - ave[f], 2) / (numExperiments - 1);
                termsVar[f] += Math.pow(results[g][f][1] - termsAve[f], 2) / (numExperiments - 1);
            }
            if (f < totalEpisodes - 1) {
                System.err.print(f + ", " + ave[f] + ", " + Math.sqrt(var[f]) + ", " + termsAve[f] + ", " + Math.sqrt(termsVar[f]) + ";");
            } else {
                System.err.print(f + ", " + ave[f] + ", " + Math.sqrt(var[f]) + ", " + termsAve[f] + ", " + Math.sqrt(termsVar[f]) + "]");
            }
            average += ave[f];
        }
        System.err.print(",[");
        for (int f = 0; f< totalEpisodes; f++) {
            tempMAWBStats[f][0] = tempMAWBStats[f][0]/(double)numExperiments;
            tempMAWBStats[f][1] = tempMAWBStats[f][1]/(double)numExperiments;
            if (f < totalEpisodes - 1) {
                System.err.print(f + ", " + tempMAWBStats[f][0] + ", " + tempMAWBStats[f][1]+";");
            } else {
                System.err.print(f + ", " + tempMAWBStats[f][0] + ", " + tempMAWBStats[f][1]+"]};");
            }
        }
        
        // Output average evaluation score
        System.err.println("Achieved an average of " + average + " over " + totalEpisodes + " episodes");
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
            lastState = s;
            learnerAction = learner.nextMove(s);

            firstStep = false;
        } else {
            boolean terminal = rlData.isTerminal;
            double reward = rlData.reward;

            //restrict reward to -1,0,1
            reward = clippedReward(reward);
            // Regular RL step
            if (!terminal) // we don't immediately receive the new screen, so the agent has to rely on previous data
            {
                int lastAction = learnerAction;
                learnerAction = learner.nextMove(s);
                learner.addSample(lastState,lastAction,reward,s,learnerAction);
                samp = new Sample(lastState,lastAction,s,learnerAction, reward);
                lastState = s;
                
                // Learning step
                terms = q.getNumTerms();
                currVal = q.valueAt(s, learnerAction);
                
                // Update wavelets
                if (episodeNumber <= adaptiveEpisodes) {//&& q.getNumTerms() < maxTerms) {
                    if (mIFDD) {
                        IFDD.update(q, samp, combineTol, gamma, 1);
                        tempNewTerms = q.getNumTerms();
                        tempMAWBStats[episodeNumber-1][0] += tempNewTerms - terms;
                        ifddadd += tempNewTerms - terms;
                    }
                    if (mIFDDplus) {
                        IFDDplus.update(q, samp, combineTol, gamma, 1);
                        tempNewTerms = q.getNumTerms();
                        tempMAWBStats[episodeNumber-1][0] += tempNewTerms - terms;
                        ifddadd += tempNewTerms - terms;
                    }
                    if (split && combine) {
                        if (maxTerms >= terms) {
                            //tempCheck = MAWB.updateAllByChildren(q, samp, combineTol, splitTol, gamma);
                            ATCext.updateAndSplitNew(q, samp, splitTol, gamma);
                            tempNewTerms = q.getNumTerms();
                            tempMAWBStats[episodeNumber-1][1] += tempNewTerms - terms;
                            atcadd += tempNewTerms - terms;
                            IFDDext.update(q, samp, combineTol, gamma);
                            terms = q.getNumTerms();
                            tempMAWBStats[episodeNumber-1][0] += terms - tempNewTerms;
                            ifddadd += terms - tempNewTerms;

                        }
                    } else {
                        if (split) {
                            if (maxTerms >= terms) {
                                ATCext.updateAndSplitNew(q, samp, splitTol, gamma);
                                tempNewTerms = q.getNumTerms();
                                tempMAWBStats[episodeNumber-1][1] += tempNewTerms - terms;
                                atcadd += tempNewTerms - terms;
                                //System.out.println(tempNewTerms-terms);
                            }
                        }
                        if (combine) {
                            if (maxTerms >= terms) {
                                IFDDext.update(q, samp, combineTol, gamma);
                                tempNewTerms = q.getNumTerms();
                                tempMAWBStats[episodeNumber-1][0] += tempNewTerms - terms;
                                ifddadd += tempNewTerms - terms;
                            }
                        }
                    }
                    if (Math.abs(currVal - q.valueAt(s, learnerAction)) > Math.pow(10, -4)) {
                        System.err.println("Warning! Q has changed! Diff: " + (currVal - q.valueAt(s, learnerAction))+" episode: "+episodeNumber);
                        //System.out.println(currState.getState()[0]+", "+currState.getState()[1]);
                    }
                }
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
        System.gc();
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
        
        // Store score
        results[experimentNumber][episodeNumber-1][0] = episodeReward;
        results[experimentNumber][episodeNumber-1][1] = q.getNumTerms();

        episodeReward = 0;
        episodeNumber++;
        
        // End experiment
        if(episodeNumber > totalEpisodes) {
            System.err.println(totalEpisodes + " episodes reached" + ": experiment " + experimentNumber + " finished...");
            System.err.println("Added " + ifddadd + " conjunctions and " + atcadd+ " splits, alpha is "+learner.getAlpha());
            episodeNumber = 1;
            
            numExperiments++;
            System.err.print("Experiment " + experimentNumber +" ");
            ifddadd = 0;
            atcadd = 0;
        }
        
        // Reset learner
        learner.startEpisode();

        // End evaluation
        if (shouldTerminate()) {
            System.err.println(numExperiments + " experiments, terminating...");
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
    
    @Override
    public boolean shouldTerminate() {
        // Terminate when we are told to do so by the outside world
        //return (io.wantsTerminate() || episodeNumber > maxNumEpisodes);
        return (io.wantsTerminate() || (experimentNumber>=numExperiments));
    }
    
}
