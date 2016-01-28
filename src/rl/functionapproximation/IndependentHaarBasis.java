/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation;

import rl.domain.State;

/**
 *
 * @author Craig Bester
 */
public class IndependentHaarBasis extends Basis {
    
    private final int maxScale;
    private final int baseScale;
    private final int dimensions;
    
    private WaveletFunction[] terms;

    public IndependentHaarBasis(int baseScale, int maxScale, int dimensions) {
        numFeatures = calculateNumTerms(baseScale,maxScale,dimensions);
        weights = new double[numFeatures];
        
        this.maxScale = maxScale;
        this.baseScale = baseScale;
        this.dimensions = dimensions;
        initialiseTerms();
        
        // random weight initialisation, prevent incorrect feature correlation?
        //for(int i = 0; i < numFeatures; i++) weights[i] = random.nextDouble();
        //Arrays.fill(weights, 0);
        
        // Optimistic?
        //Arrays.fill(weights,0.25);
    }
    
    public static int calculateNumTerms(int baseScale, int maxScale, int dimensions) {

        int num = (int)Math.pow(2, baseScale)*dimensions;
        int num2 = 0;
        for (int i = baseScale; i <= maxScale; i++) {
            num2 += Math.pow(2, i);
        }
        num += num2*dimensions; //-1 same as above 
        return num;
    }
    
    private void initialiseTerms() {
        terms = new WaveletFunction[numFeatures];
        System.err.println(terms.length);
        //intialise scaling functions
        
        int pos = 0;
        for (int i = 0; i < dimensions; i++) {
            for (int j = 0; j < Math.pow(2, baseScale); j++) {
                HaarScalingFunction curr = new HaarScalingFunction(baseScale, j, i);
                terms[pos] = curr;
                pos++;
            }
        }

        for (int i = 0; i < dimensions; i++) {
            for (int l = baseScale; l <= maxScale; l++) { //go through all scales
                for (int j = 0; j < Math.pow(2, l); j++) { // go through all translations
                    HaarWavelet curr = new HaarWavelet(l, j, i);
                    terms[pos] = curr;
                    pos++;
                }
            }
        }
        /*
        System.out.println(pos);
        System.out.println(terms.length);
        for (int i = 0; i < terms.length; i++) {
            System.out.println(terms[i].getBasisString());
        }
         * */
    }

    @Override
    public double getValue(State s) {
        double Q = 0;
        double[] vars = s.getState();
        // Q = Sum(wi*xi)
        for(int i = 0; i < numFeatures; i++) {
            Q += weights[i]*vars[i];
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
        // features (dQ/dW) are simply the state variables
        double[] phi = s.getState();//.clone(); // precaution to prevent changes
        return phi;
    }

    @Override
    public double getValue(double[] phi) {
        double Q = 0;
        // Q = Sum(wi*xi)
        for(int i = 0; i < numFeatures; i++) {
            Q += weights[i]*phi[i];
        }
        return Q;
    }
}
