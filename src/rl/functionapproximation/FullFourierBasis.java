package rl.functionapproximation;

import java.util.Arrays;

/**
 *
 * @author itchallengeuser
 */
public class FullFourierBasis extends TransformBasis {

    BasisFunction bfs[];
    int dimensions;

    public FullFourierBasis(int numFrames, int frameWidth, int frameHeight, int order) {
        this(numFrames, frameWidth, frameHeight, order, 2);
    }

    public FullFourierBasis(int numFrames, int frameWidth, int frameHeight, int order, int dimensions) {
        super(numFrames, frameWidth, frameHeight);
        this.numFeatures = (int) Math.pow(order + 1, dimensions);
        bfs = new BasisFunction[numFeatures];

        int c[] = new int[dimensions];
        for (int i = 0; i < bfs.length; i++) {
            double doubleC[] = new double[dimensions];
            for (int j = 0; j < doubleC.length; j++) {
                doubleC[j] = c[j];
            }
            bfs[i] = new FourierBasis(doubleC);
            iterate(c, c.length-1, order);
        }
    }

    @Override
    public BasisFunction[] getBasisFunctions() {
        return bfs;
    }

    @Override
    public int getNumFeatures() {
        return bfs.length;
    }

    private void iterate(int[] c, int pos, int degree) {
        if (pos < 0) {
            return;
        }
        (c[pos])++;
        if (c[pos] > degree) {
            c[pos] = 0;
            iterate(c, pos--, degree);
        }
    }
}


