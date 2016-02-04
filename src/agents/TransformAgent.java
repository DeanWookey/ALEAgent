package agents;

import rl.functionapproximation.BasisFunction;
import rl.functionapproximation.Transformation2D;
import rl.memory.StateManager;

/**
 *
 * @author itchallengeuser
 */
public abstract class TransformAgent extends GeneralAgent {

    public TransformAgent(boolean gui, String game, String pipeBasename, int imageWidth, int imageHeight, int numFramesPerState) {
        super(gui, game, pipeBasename, imageWidth, imageHeight, numFramesPerState);
    }

    @Override
    protected StateManager getStateManager() {
        return new StateManager(getNumFramesPerState(), new Transformation2D(getImageWidth(), getImageHeight(), getTransformBasis()));
    }
    
    
    
    protected abstract BasisFunction[] getTransformBasis();
    
}
