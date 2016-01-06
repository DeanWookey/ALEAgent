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
public class LinearBasis extends Basis {
    
    // Adding a constant drops performance
    
    public LinearBasis(int numDimensions) {
        numFeatures = numDimensions;
        weights = new double[numFeatures];
        
        // random weight initialisation, prevent incorrect feature correlation?
        //for(int i = 0; i < numFeatures; i++) weights[i] = random.nextDouble();
        //Arrays.fill(weights, 0);
        
        // Optimistic?
        //Arrays.fill(weights,0.25);
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
