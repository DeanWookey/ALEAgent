package rl.learners;

import java.util.Random;
import rl.domain.State;

/**
 *
 * @author Craig Bester
 */
public class RandomAgent extends Learner{
    protected Random random;

    public RandomAgent(int numActions) {
        super(numActions,0);
        this.numActions = numActions;
        random = new Random();
    }

    @Override
    public int agent_start(State s) {
        return getAction();
    }

    @Override
    public int agent_step(double reward, State s) {
        return getAction();
    }

    @Override
    public void agent_end(double reward) {
        
    }
    
    int getAction() {
        return random.nextInt(numActions);
    }
    
}
