package agents;

import rl.domain.State;
import rl.functionapproximation.BSplineBasis;
import rl.functionapproximation.Basis;
import rl.functionapproximation.BasisFunction;
import rl.functionapproximation.FullFourierBasis;
import rl.functionapproximation.IndicatorBasis;
import rl.learners.Learner;
import rl.learners.SarsaLambda;

/**
 *
 * @author itchallengeuser
 */
public class BSplineGeneralAgent extends TransformAgent {

    double lambda = 0.95;
    double epsilonStart = 0.05;
    double epsilonEnd = 0.05;
    double epsilonEvaluation = 0.01;
    double alpha = 1.0;
    double gamma = 0.95;
    Learner learner;

    public BSplineGeneralAgent(boolean gui, String game, String pipeBasename, int imageWidth, int imageHeight) {
        super(gui, game, pipeBasename, imageWidth, imageHeight, 4);
        Basis[] functionApproximators = new Basis[numActions];

        System.err.println("BSpline transform agent.");
        int numTerms = 0;
        for (int i = 0; i < actionSet.numActions; i++) {
            functionApproximators[i] = new IndicatorBasis(this.getNumFramesPerState(), getImageWidth(), getImageHeight(), getNumFeatures());
            numTerms += functionApproximators[i].getNumFunctions();
        }
        System.err.println("Terms per frame = " + functionApproximators[0].getNumFunctions() / 4);
        System.err.println("Terms per action = " + functionApproximators[0].getNumFunctions());
        System.err.println("Total number of terms = " + numTerms);

        learner = new SarsaLambda(numActions, functionApproximators[0].getNumFunctions(), functionApproximators);

        learner.setAlpha(alpha);
        learner.setGamma(gamma);
        learner.setLambda(lambda);
        learner.setEpsilon(epsilonStart);
    }

    @Override
    public int startEpisode(State s) {
        return learner.agent_start(s);
    }

    @Override
    public int step(State previousState, int previousAction, State currentState, double reward) {
        return learner.agent_step(reward, currentState);
    }

    @Override
    public void endEpisode(State previousState, int previousAction, double reward) {
        learner.agent_end(reward);
    }

    @Override
    protected BasisFunction[] getTransformBasis() {
        return new BSplineBasis(1, imageHeight, imageWidth, 2, -1, 20, false).getBasisFunctions();
    }

}
