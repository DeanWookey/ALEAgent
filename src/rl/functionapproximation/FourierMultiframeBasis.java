/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation;

import java.util.Arrays;
import rl.domain.State;
import rl.memory.Frame;

/**
 *
 * @author Craig Bester
 */
public class FourierMultiframeBasis extends Basis {
    
    // Static to save memory
    //  So don't use several bases with different parameters or everything dies
    static double[][] cosine;
    static int[][] coefficients; //same for every frame
    
    final int numDimensions;
    final int numFunctionsPerFrame;
    final int numFrames;
    final int height;
    final int width;
    final int order;
    
    public FourierMultiframeBasis(int numFrames, int height, int width, int order) {
        this.numDimensions = 2;
        this.numFunctionsPerFrame = (int)Math.pow(order+1,numDimensions);
        this.numFrames = numFrames;
        this.numFeatures = numFrames*numFunctionsPerFrame;
        this.weights = new double[numFeatures];
        
        this.order = order;
        this.height = height;
        this.width = width;
        
        
        computeFourierCoefficients();
        precalculateCosine();
        initialiseShrink();
    }

    public FourierMultiframeBasis(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /** Since we use discrete, bounded indices as input, we can pre-calculate
     *   all cosine values (may take a while but probably worth it)
     */
    private void precalculateCosine() {
        if(cosine == null) {
            cosine = new double[order*height][order*width];
            // SHOULD WE SCALE x AND y? Probably
            for(int i = 0; i < numFunctionsPerFrame; i++) {
                int xco = coefficients[i][1];
                int yco = coefficients[i][0];
                for(int y = 0; y < height; y++) {
                    for(int x = 0; x < width; x++) {
                        double xs = ((double)x/width);
                        //double xs = x;
                        double ys = ((double)y/height);
                        //double ys = y;
                        cosine[yco*y][xco*x] = Math.cos(Math.PI*(yco*ys+xco*xs));
                        //System.err.println("cos(pi*("+yco+"*"+ys+"+"+xco+"*"+xs+")="+cosine[yco*y][xco*x]);
                    }
                }
            }
        }
    }

    @Override
    public double getValue(State s) {
        phi = computeFeatures(s);
        double Q = 0;
        for(int i = 0; i < numFeatures; i++) {
            Q += weights[i]*phi[i];
        }
        
        return Q;
    }

    double[] phi;
    @Override
    public double[] computeFeatures(State s) {
        // avoid extra memory
        //if(phi == null) phi = new double[numFeatures];
        double[] phi = new double[numFeatures];
        double[] vars = s.getState();
        if (vars.length == numFeatures) {
            return vars;
        } else {
            int xco, yco, index, imgindex;
            for (int f = 0; f < numFrames; f++) {
                for (int k = 0; k < numFunctionsPerFrame; k++) {
                    index = f * numFunctionsPerFrame + k;
                    // have to reset old value when overwriting
                    phi[index] = 0;

                    xco = coefficients[k][1];
                    yco = coefficients[k][0];
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            imgindex = f * height * width + y * width + x;
                            // x,y already scaled in cosine pre-calculation
                            phi[index] += vars[imgindex] * cosine[yco * y][xco * x];
                        }
                    }
                }
            }
        }
        
        
        /*Frame[] farr = s.getState();
        for(int f = 0; f < numFrames; f++) {
            double[][] img = farr[f].image;
            for(int k = 0; k < numFunctionsPerFrame; k++) {
                int index = f*numFunctionsPerFrame + k;
                
                // have to reset old value when overwriting
                phi[index] = 0; 
                
                int xco = coefficients[k][1];
                int yco = coefficients[k][0];
                for(int y = 0; y < height; y++) {
                    for(int x = 0; x < width; x++) {
                        // x,y already scaled in cosine pre-calculation
                        phi[index] += img[y][x]*cosine[yco*y][xco*x];
                    }
                }
            }
        }*/
        
        return phi;
    }
    
    public double[] computeFeatures(double[] vars) {
        // avoid extra memory
        double[] phi = new double[numFeatures];
        
        int xco,yco,index,imgindex;
        for(int f = 0; f < numFrames; f++) {
            for(int k = 0; k < numFunctionsPerFrame; k++) {
                index = f*numFunctionsPerFrame + k;
                // have to reset old value when overwriting
                phi[index] = 0; 
 
                xco = coefficients[k][1];
                yco = coefficients[k][0];
                for(int y = 0; y < height; y++) {
                    for(int x = 0; x < width; x++) {
                        imgindex = f*height*width+y*width+x;
                        // x,y already scaled in cosine pre-calculation
                        phi[index] += vars[imgindex]*cosine[yco*y][xco*x];
                    }
                }
            }
        }
        
        return phi;
    }

    @Override
    public void updateWeights(double[] deltaW) {
        for (int i = 0; i < numFeatures; i++) {
            weights[i] += deltaW[i]; // shrinking should be done by the learner?
        }
    }
    
    @Override
    public double[] getShrink() {
        return shrink;
    }
    
    private void initialiseShrink() {
        shrink = new double[numFeatures];
        
        for(int f = 0; f < numFunctionsPerFrame; f++) {
            double d = 0;

            //computer norm of coefficient vector for each basis function
            for (int i = 0; i < numDimensions; i++) {
                d += (coefficients[f][i] * coefficients[f][i]);
            }
            d = Math.sqrt(d);
            if (d == 0.0) {
                d = 1.0;
            }
            
            shrink[f] = d;
        }
        
        // Since every frame has the same coefficients, copy across the shrink
        for(int i = 1; i < numFrames; i++) {
            System.arraycopy(shrink, 0, shrink, i*numFunctionsPerFrame, numFunctionsPerFrame);
        }
    }
    
    private void computeFourierCoefficients() {
        if(coefficients == null) {
            coefficients = new int[numFunctionsPerFrame][numDimensions];
            int pos = 0;
            int c[] = new int[numDimensions];
            for (int j = 0; j < numDimensions; j++) {
                c[j] = 0;
            }
            do {
                System.arraycopy(c, 0, coefficients[pos], 0, numDimensions);
                pos++;
                // Iterate c
                Iterate(c, numDimensions, order);
            } while (c[0] <= order);
        }
        
        /*for(int i = 0; i < coefficients.length; i++) {
            for(int j = 0; j < coefficients[0].length; j++) {
                System.err.print(coefficients[i][j]+",");
            }
            System.err.println();
        }*/
    }

    private void Iterate(int[] c, int pos, int Degree) {
        (c[pos - 1])++;
        if (c[pos - 1] > Degree) {
            if (pos > 1) {
                c[pos - 1] = 0;
                Iterate(c, pos - 1, Degree);
            }
        }
    }

    @Override
    public double getValue(double[] phi) {
        double Q = 0;
        for(int i = 0; i < numFeatures; i++) {
            Q += weights[i]*phi[i];
        }
        
        return Q;
    }
    
    @Override
    public Object clone() {
        FourierMultiframeBasis obj = (FourierMultiframeBasis) super.clone();
        obj.weights = new double[numFeatures];
        System.arraycopy(this.weights, 0, obj.weights, 0, numFeatures);
        return obj;
    }
}


