/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.learners;

import rl.functionapproximation.Basis;
import rl.domain.State;

/**
 *
 * @author Craig Bester
 */
public abstract class Learner {
    double alpha;
    double gamma;
    double lambda;
    double epsilon;
    
    int numActions;
    int numFeatures;
    int stepNumber;
    
    int lastAction;
    State lastState;
    
    Basis[] FA;
    
    public abstract int agent_start(State s);
    public abstract int agent_step(double reward, State s);
    public abstract void agent_end(double reward);
    
    public Learner(int numActions, int numFeatures) {
        this.stepNumber = 0;
        this.numActions = numActions;
        this.numFeatures = numFeatures;
    }
    
    public Learner(int numActions, int numFeatures, Basis[] functionApproximators) {
        this.stepNumber = 0;
        this.numActions = numActions;
        this.numFeatures = numFeatures;
        this.FA = functionApproximators;
    }
    
    public final void setAlpha(double a) {
        alpha = a;
    }
    
    public final void setGamma(double g) {
        gamma = g;
    }
    
    public final void setLambda(double l) {
        lambda = l;
    }
    
    public final void setEpsilon(double e) {
        epsilon = e;
    }
}
