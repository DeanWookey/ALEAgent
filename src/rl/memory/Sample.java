package rl.memory;

import rl.domain.State;

/**
 *
 * @author Craig
 */
public class Sample {
    public State state;
    public int action;
    public double reward;
    public State nextState;
    
    public Sample(State state, int action, double reward, State nextState) {
        this.state = state;
        this.action = action;
        this.reward = reward;
        this.nextState = nextState;
    }
    
    public void replace(State state, int action, double reward, State nextState) {
        this.state.replace(state.getState(), state.isTerminal());
        this.action = action;
        this.reward = reward;
        this.nextState.replace(nextState.getState(), nextState.isTerminal());
    }
}
