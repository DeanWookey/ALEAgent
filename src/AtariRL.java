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

import ale.ActionSet;
import ale.gui.AbstractUI;
import ale.gui.AgentGUI;
import ale.io.ALEPipes;
import ale.io.Actions;
import ale.io.RLData;
import ale.screen.NTSCPalette;
import ale.screen.ScreenConverter;
import ale.screen.ScreenMatrix;
import image.CosineTransform;
import image.FourierTransform;
import image.Utils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import rl.agents.QLambda;
import rl.agents.QLambdaReplay;
import rl.agents.QLearner;
import rl.agents.QReplay;
import rl.agents.QTarget;
import rl.agents.QTargetReplay;
import rl.agents.RLAgent;
import rl.agents.SarsaLambda;
import rl.domain.State;
import rl.functionapproximation.Basis;
import rl.functionapproximation.FourierMultiframeBasis;
import rl.functionapproximation.LinearBasis;
import rl.memory.Frame;
import rl.memory.FrameHistory;
import rl.memory.FrameHistory_Fourier;


/*
 * This is an Arcade Learning Environment reinforcement learning agent.
 *  It is based on the Java agent distributed with the ALE package written
 *  by Marc G. Bellemare under the GNU General Public License.
 *
 * 
 * @author Craig Bester
 */
public class AtariRL {

    ALEPipes io;
    RLAgent learner;
    ActionSet actionSet;
    AbstractUI ui;
    ScreenConverter converter;
    //FrameHistory history;
    FrameHistory_Fourier history;

    String namedPipesBasename;
    String gameName;
    boolean firstStep;
    boolean requestReset;
    boolean useGUI;
    boolean training;
    double cumulativeReward;
    double episodeReward;
    int learnerAction;
    int episodeNumber;
    int numFrames;
    int numTrainingEpisodes;

    final int imageSize = 84;
    final int framesPerState = 4;
    int numEvaluationEpisodes = 30;
    int numTrainingFrames = 10000;
    int numRandomReductionFrames = 2000;

    int order = 5;
    //double alpha = 1;
    double alpha = 0.014 / (int)Math.pow(order+1,2); // should be #frames*#basis_functions?
    //double alpha = 0.014 / (imageSize * imageSize);
    //double alpha = 0.00025; // DeepMind
    //double gamma = 0.99; //Sarsa
    double gamma = 0.95; //Q-learning
    double lambda = 0.95;
    double epsilonStart = 1.0;
    double epsilonEnd = 0.1;
    double epsilonEvaluation = 0.05;
    

    public AtariRL(boolean gui, String game, String pipesBasename) {
        requestReset = true;
        firstStep = true;
        training = (numTrainingFrames > 0);
        cumulativeReward = 0;
        episodeNumber = 1;
        numTrainingEpisodes = 1;
        numFrames = 0;

        useGUI = gui;
        gameName = game;
        if (gameName == null) {
            gameName = "default";
        }
        actionSet = new ActionSet(gameName);
        converter = new ScreenConverter(new NTSCPalette());
        //history = new FrameHistory(framesPerState);
        history = new FrameHistory_Fourier(framesPerState,new FourierTransform(imageSize,imageSize,order));

        init();
        initLearner();
    }

    public final void initLearner() {

        Basis[] functionApproximators = new Basis[actionSet.numActions];
        
        
        //Basis test = new FourierMultiframeBasis(framesPerState,imageSize,imageSize,order);
        
        
        for (int i = 0; i < actionSet.numActions; i++) {
            //functionApproximators[i] = new LinearBasis(test.getNumFunctions());
            //functionApproximators[i].setShrink(test.getShrink());
            
            //functionApproximators[i] = new LinearBasis(numFrames * imageSize * imageSize);
            functionApproximators[i] = new FourierMultiframeBasis(framesPerState,imageSize,imageSize,order);
        
            // Random weight initialisation - DeepMind (bounds used unclear)
            //functionApproximators[i].randomiseWeights();
            // Randomised weights sometimes causes learning divergence
        }
        
        
        //learner = new SarsaLambda(actionSet.numActions, functionApproximators[0].getNumFunctions(), functionApproximators);
        //learner = new QLambda(actionSet.numActions,functionApproximators[0].getNumFunctions(),functionApproximators);
        //learner = new QLearner(actionSet.numActions,functionApproximators[0].getNumFunctions(),functionApproximators);
        learner = new QReplay(actionSet.numActions,functionApproximators[0].getNumFunctions(),functionApproximators);
        //learner = new QTarget(actionSet.numActions,functionApproximators[0].getNumFunctions(),functionApproximators);
        //learner = new QTargetReplay(actionSet.numActions,functionApproximators[0].getNumFunctions(),functionApproximators);
        
        learner.setAlpha(alpha);
        learner.setGamma(gamma);
        learner.setLambda(lambda);
        learner.setEpsilon(epsilonStart);
    }

    public final void init() {
        if (useGUI) {
            // Create the GUI
            ui = new AgentGUI();
        } else {
            ui = null;
        }

        io = null;
        try {
            // Initialize the pipes; use named pipes if requested
            if (namedPipesBasename != null) {
                io = new ALEPipes(namedPipesBasename + "out", namedPipesBasename + "in");
            } else {
                io = new ALEPipes();
            }

            // Determine which information to request from ALE
            io.setUpdateScreen(wantsScreenData());
            io.setUpdateRam(wantsRamData());
            io.setUpdateRL(wantsRLData());
            io.initPipes();
        } catch (IOException e) {
            System.err.println("Could not initialize pipes: " + e.getMessage());
            System.exit(-1);
        }
    }

    /**
     * The main program loop. In turn, we will obtain a new screen from ALE,
     * pass it on to the agent and send back an action (which may be a reset
     * request).
     */
    public void run() {
        boolean done = false;

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

        // Output average evaluation score
        double avg = cumulativeReward / numEvaluationEpisodes;
        System.err.println("Achieved an average of " + avg + " over " + numEvaluationEpisodes + " episodes");
    }

    public boolean shouldTerminate() {
        // Terminate when we are told to do so by the outside world
        //return (io.wantsTerminate() || episodeNumber > maxNumEpisodes);
        return (io.wantsTerminate() || (!training && (episodeNumber - numTrainingEpisodes) > numEvaluationEpisodes));
    }

    protected void updateImage(ScreenMatrix currentScreen) {
        // Convert the screen matrix to an image
        BufferedImage img = converter.convert(currentScreen);

        // Provide the new image to the UI
        ui.updateFrameCount();
        ui.setImage(img);
        ui.refresh();
    }

    public int selectAction() {
        // If reset is requested, send it
        if (requestReset) {
            //System.err.println("RESET RESET RESET");
            firstStep = true;
            requestReset = false;
            return Actions.map("system_reset");
        } // Otherwise send back the action taken by the learner (see rlStep())
        else {
            return actionSet.get(learnerAction);
        }
    }

    public void observe(ScreenMatrix image, RLData rlData) {
        // Display reward information via messages
        if (useGUI && rlData.reward != 0) {
            ui.addMessage("Reward: " + rlData.reward);
        }
        // Also print out 'game over' when we received the terminal bit

        history.addFrame(new Frame(convertImage(image)));

        numFrames++;
        
        rlStep(rlData);
        //rlStep(image,rlData);
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

    public void rlStep(RLData rlData) {
    //public void rlStep(ScreenMatrix image, RLData rlData) {

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

    public void reduceEpsilon() {
        if (numFrames < numRandomReductionFrames) {
            learner.setEpsilon((1 - (double) numFrames / (double) numRandomReductionFrames) * (epsilonStart - epsilonEnd) + epsilonEnd);
        } else {
            if(training) {
                learner.setEpsilon(epsilonEnd);
            } else {
                learner.setEpsilon(epsilonEvaluation);
            }
        }
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
    
    public void setEvaluationEpisodes(int numEpisodes) {
        this.numEvaluationEpisodes = numEpisodes;
    }

    public void setTrainingFrames(int numFrames) {
        this.numTrainingFrames = numFrames;
        this.training = (numFrames > 0);
    }

    public void setRandomReductionFrames(int numFrames) {
        this.numRandomReductionFrames = numFrames;
    }

    public boolean wantsRamData() {
        return false;
    }

    public boolean wantsRLData() {
        return true;
    }

    public boolean wantsScreenData() {
        return true;
    }

    /**
     * Prints out command-line usage text.
     *
     */
    public static void printUsage() {
        System.err.println("Invalid argument.");
        System.err.println("Usage: java RLAgent [-nogui] [-named_pipes filename] [-game gamename]\n");
        System.err.println("Example: java RLAgent -named_pipes /tmp/ale_fifo_ -game breakout");
        System.err.println("  Will start an agent that communicates with ALE via named pipes \n"
                + "  /tmp/ale_fifo_in and /tmp/ale_fifo_out and uses the minimal action set for breakout");
    }

    /**
     * Main class for running the RL agent.
     *
     * @param args
     */
    public static void main(String[] args) {
        // Parameters; default values
        boolean useGUI = true;
        String gameName = null;
        String namedPipesName = null;
        int trainingFrames = -1;
        int randomFrames = -1;
        int evaluationEpisodes = -1;

        // Parse arguments
        int argIndex = 0;

        boolean doneParsing = (args.length == 0);

        // Loop through the list of arguments
        while (!doneParsing) {
            // -nogui: do not display the Java GUI
            if (args[argIndex].equals("-nogui")) {
                useGUI = false;
                argIndex++;
            } // -named_pipes <basename>: use to communicate with ALE via named pipes
            //  (instead of stdin/out)
            else if (args[argIndex].equals("-named_pipes") && (argIndex + 1) < args.length) {
                namedPipesName = args[argIndex + 1];

                argIndex += 2;
            } // -game <game_rom_name>: allows the agent to use the minimal action set of the specified game if available
            else if (args[argIndex].equals("-game") && (argIndex + 1) < args.length) {
                gameName = args[argIndex + 1];

                argIndex += 2;
            } // -training <frames>: sets the number of training frames
            else if (args[argIndex].equals("-training") && (argIndex + 1) < args.length) {
                trainingFrames = Integer.parseInt(args[argIndex + 1]);

                argIndex += 2;
            } // -random <frames>: sets the number of frames over which to reduce epsilon
            else if (args[argIndex].equals("-random") && (argIndex + 1) < args.length) {
                randomFrames = Integer.parseInt(args[argIndex + 1]);

                argIndex += 2;
            } // -evaluation <episodes>: sets the number of episodes over which to evaluate the agent
            else if (args[argIndex].equals("-evaluation") && (argIndex + 1) < args.length) {
                evaluationEpisodes = Integer.parseInt(args[argIndex + 1]);

                argIndex += 2;
            } // If the argument is unrecognized, exit
            else {
                printUsage();
                System.exit(-1);
            }

            // Once we have parsed all arguments, stop
            if (argIndex >= args.length) {
                doneParsing = true;
            }
        }

        AtariRL ar = new AtariRL(useGUI, gameName, namedPipesName);
        if (trainingFrames >= 0) {
            ar.setTrainingFrames(trainingFrames);
        }
        if (randomFrames >= 0) {
            ar.setRandomReductionFrames(randomFrames);
        }
        if (evaluationEpisodes >= 0) {
            ar.setEvaluationEpisodes(evaluationEpisodes);
        }

        ar.run();
    }
}
