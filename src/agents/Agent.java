/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import ale.ActionSet;
import ale.gui.AbstractUI;
import ale.gui.AgentGUI;
import ale.io.ALEPipes;
import ale.io.Actions;
import ale.io.RLData;
import ale.screen.NTSCPalette;
import ale.screen.ScreenConverter;
import ale.screen.ScreenMatrix;
import java.awt.image.BufferedImage;
import java.io.IOException;
import rl.learners.Learner;

/**
 *
 * @author Craig Bester
 */
public abstract class Agent {
    ALEPipes io;
    //Learner learner;
    ActionSet actionSet;
    AbstractUI ui;
    ScreenConverter converter;
    
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
    int numActions;
    int numFrames;
    int numTrainingEpisodes;
    
    int numEvaluationEpisodes = 30;
    int numTrainingFrames = 10000;
    int numRandomReductionFrames = 2000;
    
    
    public abstract void initLearner();
    public abstract void rlStep(ScreenMatrix image, RLData rlData);
    
    public Agent(boolean gui, String game, String pipesBasename) {
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

        numActions = actionSet.numActions;
        
        init();
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
    
    public void observe(ScreenMatrix image, RLData rlData) {
        // Display reward information via messages
        if (useGUI && rlData.reward != 0) {
            ui.addMessage("Reward: " + rlData.reward);
        }
        // Also print out 'game over' when we received the terminal bit

        numFrames++;
        
        rlStep(image,rlData);
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
    
}
