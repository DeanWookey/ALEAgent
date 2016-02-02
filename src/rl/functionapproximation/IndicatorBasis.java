package rl.functionapproximation;

import rl.domain.State;

/**
 *
 * @author itchallengeuser
 */
public class IndicatorBasis extends TransformBasis {

    public IndicatorBasis(int numFrames, int frameWidth, int frameHeight) {
        super(numFrames, frameWidth, frameHeight);
        this.numFeatures = numFrames*frameWidth*frameHeight;
        weights = new double[numFeatures];
    }

        public IndicatorBasis(int numFrames, int frameWidth, int frameHeight, int numFeatures) {
        super(numFrames, frameWidth, frameHeight);
        this.numFeatures = numFeatures;
        weights = new double[numFeatures];
    }


    public int getNumFeatures() {
        return numFeatures;
    }

    @Override
    public double getValue(State s) {
        double Q = 0;
        for (int i = 0; i < numFeatures; i++) {
            Q += weights[i] * s.getState()[i];
        }
        return Q;
    }

    @Override
    public double getValue(double[] phi) {
        double Q = 0;
        for (int i = 0; i < numFeatures; i++) {
            Q += weights[i] * phi[i];
        }
        return Q;
    }

 



    @Override
    public void updateWeights(double[] deltaW) {
        for (int i = 0; i < numFeatures; i++) {
            weights[i] += deltaW[i];
        }
    }

    @Override
    public double[] computeFeatures(State s) {
        return s.getState();
    }

    @Override
    public BasisFunction[] getBasisFunctions() {
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
