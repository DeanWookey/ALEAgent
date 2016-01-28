package rl.functionapproximation.adaptive_wavelets;

import rl.domain.State;

/**
 *
 * @author Dean
 */
public class Sample {
    public State s;
    public int action;
    public double reward;
    public double error;
    public State nextState;
    public int nextAction;
    public double ret;
    public Sample() {
        
    }
    
    public Sample(State s, int action, State nextState, int nextAction, double reward) {
        this.s = s;
        this.action = action;
        this.nextState = nextState;
        this.nextAction = nextAction;
        this.reward = reward;
    }
    
    public Sample(State s, int action, State nextState, int nextAction, double reward, double ret) {
        this.s = s;
        this.action = action;
        this.nextState = nextState;
        this.nextAction = nextAction;
        this.reward = reward;
        this.ret = ret;
    }
}
