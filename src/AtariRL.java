
import agents.Agent;
import agents.FourierAgent;
import agents.AdaptiveWaveletAgent;
import agents.FixedWaveletAgent;

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

/*
 * This is an Arcade Learning Environment reinforcement learning agent.
 *  It is based on the Java agent distributed with the ALE package written
 *  by Marc G. Bellemare under the GNU General Public License.
 *
 * 
 * @author Craig Bester
 */
public class AtariRL {

    /**
     * Prints out command-line usage text.
     *
     */
    public static void printUsage() {
        System.err.println("Invalid argument.");
        System.err.println("Usage: java -jar <RLAgent> [-nogui] [-named_pipes filename] [-game gamename]\n");
        System.err.println("Example: java -jar RLAgent.jar -named_pipes /tmp/ale_fifo_ -game breakout");
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

        // Select reinforcement learning agent:
        //Agent ar = new FourierAgent(useGUI, gameName, namedPipesName);
        //Agent ar = new FixedWaveletAgent(useGUI, gameName, namedPipesName);
        Agent ar = new AdaptiveWaveletAgent(useGUI, gameName, namedPipesName);
        
        // Set parameters
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
