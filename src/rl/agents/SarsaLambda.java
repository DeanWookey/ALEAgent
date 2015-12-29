/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.agents;

import java.util.ArrayList;
import java.util.Random;
import rl.domain.State;
import rl.functionapproximation.Basis;
import rl.functionapproximation.LinearBasis;

/**
 *
 * @author Craig
 */
public class SarsaLambda extends RLAgent{
    
    Random random;
    double[][] traces;

    public SarsaLambda(int numActions, int numFeatures) {
        super(numActions, numFeatures);
        random = new Random();
        FA = new LinearBasis[numActions];
        for(int i = 0; i < numActions; i++) {
            FA[i] = new LinearBasis(numFeatures);
        }
        traces = new double[numActions][numFeatures];
        resetTraces();
    }
    
    public SarsaLambda(int numActions, int numFeatures, Basis[] functionApproximators) {
        super(numActions, numFeatures,functionApproximators);
        random = new Random();
        traces = new double[numActions][numFeatures];
        resetTraces();
    }

    @Override
    public int agent_start(State s) {
        resetTraces();
        
        lastAction = getAction(s);
        lastState = s;
        return lastAction;
    }

    @Override
    public int agent_step(double reward, State s) {
        int action = getAction(s);
        addSample(lastState,lastAction,reward,s,action);
        lastState = s;
        lastAction = action;
        return action;
    }

    @Override
    public void agent_end(double reward) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public final void resetTraces() {
        for(int i = 0; i < numActions; i++) {
            for(int j = 0; j < numFeatures; j++) {
                traces[i][j] = 0;
            }
        }
    }

    public int getAction(State s) {
        int action;
        if ((random.nextFloat() < epsilon)) {
            action = random.nextInt(numActions);
        }
        else {
            action = greedyMove(s);
        }
        return action;
    }
    
    private int greedyMove(State s) {
        int bestMove = -1;
        double bestQ = Double.NEGATIVE_INFINITY;
        if (s.isTerminal()) {
            return bestMove;
        }
        ArrayList<Integer> ties = new ArrayList<>();
        for (int a = 0; a < numActions; a++) {
            double Qa = FA[a].getValue(s);
            //System.out.println(a + ": " + Va);
            if (Qa == bestQ && bestQ > Double.NEGATIVE_INFINITY) {
                ties.add(a);
            } else if (Qa > bestQ) {
                ties.clear();
                ties.add(a);
                bestQ = Qa;
                bestMove = a;
            }
        }
        if (ties.size() > 1) {
            bestMove = ties.get(random.nextInt(ties.size()));
        }
        if (bestMove == -1) {
            System.err.println("Diverged, no greedy move");
        }
        return bestMove;
    }

    public void addSample(State currState, int move, double reward, State newState, int nextMove) {
        //System.err.println("LastQ:"+FA[move].getValue(currState));
        
        // Decay traces
        for (int a = 0; a < numActions; a++) {
            for (int j = 0; j < FA[a].getNumFunctions(); j++) {
                traces[a][j] = traces[a][j] * gamma * lambda;
            }
        }
        
        // Update traces
        double[] phi = FA[move].computeFeatures(currState);
        for (int k = 0; k < FA[move].getNumFunctions(); k++) {
            traces[move][k] += phi[k];
        }
        
        // Alpha scaling
        double epsilon_alpha = 0.0;
        double[] shrink = FA[move].getShrink();
        if ((!newState.isTerminal()) && (nextMove != -1)) {
            double[] phi_tp = FA[nextMove].computeFeatures(newState);
            double[] phi_t = FA[move].computeFeatures(currState);
            double[] nextShrink = FA[nextMove].getShrink();
            for (int k = 0; k < FA[move].getNumFunctions(); k++) {
                epsilon_alpha += gamma * phi_tp[k] * (traces[nextMove][k]/nextShrink[k]) - phi_t[k] * (traces[move][k]/shrink[k]);
            }
        } else {
            // terminal state - calculate only from last move
            double[] phi_t = FA[move].computeFeatures(currState);
            for (int k = 0; k < FA[move].getNumFunctions(); k++) {
                epsilon_alpha += -1.0 * phi_t[k] * (traces[move][k]/shrink[k]);
            }
        }
        // epsilon_alpha < 0, we have good bounds 
        if (epsilon_alpha < 0.0) {
            alpha = Math.min(Math.abs(1.0 / epsilon_alpha), alpha);
        }
        
        
        // calculate temporal difference error
        double delta = reward - FA[move].getValue(currState);
        // not end of episode -> update delta with next estimated value
        if ((!newState.isTerminal()) && (nextMove != -1)) {
            delta += gamma * FA[nextMove].getValue(newState);
        }

        // Check for divergence
        if (java.lang.Double.isNaN(delta)) {
            System.err.println("Function approximation divergence");
            System.exit(1);
        }

        for (int a = 0; a < numActions; a++) {
            double[] deltaW = new double[FA[a].getNumFunctions()];
            for (int i = 0; i < FA[a].getNumFunctions(); i++) {
                deltaW[i] = alpha/FA[a].getShrink()[i] * delta * traces[a][i];
            }

            // Update weights
            FA[a].updateWeights(deltaW);
        }

    }

}
