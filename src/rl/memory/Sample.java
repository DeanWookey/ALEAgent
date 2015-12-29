package rl.memory;

import rl.domain.State;

/**
 *
 * @author Craig
 */
public class Sample {
    public final State state;
    public final int action;
    public final double reward;
    public final State nextState;
    
    public Sample(State state, int action, double reward, State nextState) {
        this.state = state;
        this.action = action;
        this.reward = reward;
        this.nextState = nextState;
    }
}
