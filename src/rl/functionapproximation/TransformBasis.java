package rl.functionapproximation;

import rl.domain.State;

/**
 *
 * @author Dean
 */
public abstract class TransformBasis extends Basis {

    private final int numFrames;
    private final int frameWidth;
    private final int frameHeight;
    private double[][][] transformCache;

    public TransformBasis(int numFrames, int frameWidth, int frameHeight) {
        this.numFrames = numFrames;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    public abstract BasisFunction[] getBasisFunctions();

    public abstract int getNumFeatures();

    public int getNumFeaturesPerFrame() {
        return getNumFeatures() / numFrames;
    }

    @Override
    public double getValue(State s) {
        double[] phi = computeFeatures(s);
        double Q = 0;
        for (int i = 0; i < numFeatures; i++) {
            Q += weights[i] * phi[i];
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
    public double[] computeFeatures(State s) {
        double[] phi = new double[getNumFeatures()];
        double[] vars = s.getState();
        int pixelsPerFrame = getFrameHeight() * getFrameWidth();
        for (int j = 0; j < getNumFeatures(); j++) {
            int frameNum = j / getNumFeaturesPerFrame();
            int frameOffset = frameNum * pixelsPerFrame;
            double normalisation = 0;
            for (int y = 0; y < getFrameHeight(); y++) {
                for (int x = 0; x < getFrameWidth(); x++) {
                    int imgIndex = frameOffset + y * getFrameWidth() + x;
                    // x,y already scaled in cosine pre-calculation
                    phi[j] += vars[imgIndex] * getBasisValue(j, x, y);
                    normalisation += getBasisValue(j, x, y);
                }
            }
            phi[j] = phi[j] / normalisation;
        }
        return phi;
    }

    public double getBasisValue(int basisFunctionIndex, int x, int y) {
        if (transformCache == null) {
            transformCache = new double[getNumFeatures()][getFrameWidth()][getFrameHeight()];
            for (int i = 0; i < getNumFeatures(); i++) {
                for (int k = 0; k < getFrameWidth(); k++) {
                    for (int l = 0; l < getFrameHeight(); l++) {
                        transformCache[i][k][l] = getBasisFunctions()[basisFunctionIndex].getValue(new State(new double[]{(double) x / (getFrameWidth() - 1), (double) y / (getFrameHeight() - 1)}));
                    }
                }
            }
        }
        return transformCache[basisFunctionIndex][x][y];
    }

    @Override
    public void updateWeights(double[] deltaW) {
        for (int i = 0; i < numFeatures; i++) {
            weights[i] += deltaW[i];
        }
    }

    public int getNumFrames() {
        return numFrames;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

}
