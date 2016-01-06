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
import rl.memory.Memory;
import rl.memory.Sample;

/**
 *
 * @author Craig
 */
public class QTargetReplay extends RLAgent {

    Random random;
    Basis[] targetQ;
    Memory memory;

    int numUpdates;
    
    final int updateFrequency = 4;
    final int batchSize = 32;
    final int replaySize = 10000;
    final int targetUpdateFrequency = 1000;

    public QTargetReplay(int numActions, int numFeatures) {
        super(numActions, numFeatures);
        random = new Random();
        FA = new LinearBasis[numActions];
        targetQ = new LinearBasis[numActions];
        for (int i = 0; i < numActions; i++) {
            FA[i] = new LinearBasis(numFeatures);
            targetQ[i] = (LinearBasis) ((LinearBasis) FA[i]).clone();
        }
        memory = new Memory(replaySize);
        numUpdates = 0;
    }

    public QTargetReplay(int numActions, int numFeatures, Basis[] functionApproximators) {
        super(numActions, numFeatures, functionApproximators);
        targetQ = new Basis[numActions];
        for (int i = 0; i < numActions; i++) {
            targetQ[i] = (Basis) ((Basis) FA[i]).clone();
        }
        random = new Random();
        memory = new Memory(replaySize);
        numUpdates = 0;
    }

    @Override
    public int agent_start(State s) {
        lastAction = getAction(s);
        lastState = s;
        return lastAction;
    }

    @Override
    public int agent_step(double reward, State s) {
        int action = getAction(s);

        stepNumber++;

        addSample(lastState, lastAction, reward, s, action);
        lastState.replace(s); //will throw null exception if agent_start not called at least once before agent_step
        lastAction = action;
        return action;
    }

    @Override
    public void agent_end(double reward) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int getAction(State s) {
        int action;
        if ((random.nextFloat() < epsilon)) {
            action = random.nextInt(numActions);
        } else {
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

    private void updateTargetQ() {
        for (int i = 0; i < numActions; i++) {
            targetQ[i].setWeights(FA[i].getWeights());
        }
    }

    public void addSample(State currState, int move, double reward, State newState, int nextMove) {
        memory.addSample(currState, move, reward, newState);
        if (stepNumber >= batchSize && stepNumber % updateFrequency == 0) {
            batchUpdate();
        }
    }

    public void batchUpdate() {
        ArrayList<Sample> ls = memory.getRandomBatch(batchSize);
        ls.stream().forEach((s) -> {
            updateQ(s);
        });
    }

    public void updateQ(Sample s) {
        State currState = s.state;
        int move = s.action;
        double reward = s.reward;
        State newState = s.nextState;
        //System.err.println("LastQ:"+FA[move].getValue(currState));
        int nextMove = greedyMove(newState);

        double[] phi_t = FA[move].computeFeatures(currState);
        double[] phi_tp = null;
        if ((!newState.isTerminal()) && (nextMove != -1)) {
            phi_tp = FA[nextMove].computeFeatures(newState);
        }

        // Alpha scaling
        // Formula?
        // ea = SUM(gamma*f(i)*(dQ/dw) - f(i)*(dQ/dw))  ??
        // Where Q = SUM(w(i)*f(i))
        double[] shrink = FA[move].getShrink();
        double epsilon_alpha = 0.0;
        if ((!newState.isTerminal()) && (nextMove != -1)) {
            double[] nextShrink = FA[nextMove].getShrink();
            for (int k = 0; k < FA[move].getNumFunctions(); k++) {
                epsilon_alpha += gamma * phi_tp[k] * phi_tp[k] / nextShrink[k] - phi_t[k] * phi_t[k] / shrink[k];
            }
        } else {
            // terminal state - calculate only from last move
            for (int k = 0; k < FA[move].getNumFunctions(); k++) {
                epsilon_alpha += -1.0 * phi_t[k] * phi_t[k] / shrink[k];
            }
        }
        // epsilon_alpha < 0, we have good bounds 
        if (epsilon_alpha < 0.0) {
            alpha = Math.min(Math.abs(1.0 / epsilon_alpha), alpha);
        }

        // calculate temporal difference error
        //double delta = reward - FA[move].getValue(currState);
        double delta = reward - FA[move].getValue(phi_t);
        // not end of episode -> update delta with next estimated value
        if ((!newState.isTerminal()) && (nextMove != -1)) {
            delta += gamma * targetQ[nextMove].getValue(phi_tp);
        }

        // Check for divergence
        if (java.lang.Double.isNaN(delta)) {
            System.err.println("Function approximation divergence");
            System.exit(1);
        }

        // Update weights
        if ((!newState.isTerminal()) && (nextMove != -1)) {
            // Should only the concerned basis functions be updated?

            double[] deltaW = new double[FA[move].getNumFunctions()];
            for (int i = 0; i < FA[move].getNumFunctions(); i++) {
                deltaW[i] = (alpha / shrink[i]) * delta * (phi_t[i] - gamma * phi_tp[i]);
            }
            /*double[] deltaW2 = new double[FA[nextMove].getNumFunctions()];
             for (int i = 0; i < FA[nextMove].getNumFunctions(); i++) {
             deltaW2[i] = alpha / FA[nextMove].getShrink()[i] * delta * (phi[i] - gamma*phi2[i]);
             }*/

            // Update weights
            FA[move].updateWeights(deltaW);
            //FA[nextMove].updateWeights(deltaW2);
        } else {
            // Terminal state
            double[] deltaW = new double[FA[move].getNumFunctions()];
            for (int i = 0; i < FA[move].getNumFunctions(); i++) {
                deltaW[i] = (alpha / shrink[i]) * delta * phi_t[i];
            }
            // Update weights
            FA[move].updateWeights(deltaW);
        }
        
        numUpdates++;
        if (numUpdates % targetUpdateFrequency == 0) {
            updateTargetQ();
        }
    }

}
