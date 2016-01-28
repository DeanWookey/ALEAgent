/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.memory;

import java.awt.image.BufferedImage;
import rl.domain.State;
import rl.functionapproximation.BasisFunction;
import rl.functionapproximation.CombinationFunction;
import rl.functionapproximation.DaubNScalingFunction;
import rl.functionapproximation.DaubNWavelet;
import rl.functionapproximation.HaarScalingFunction;
import rl.functionapproximation.HaarWavelet;
import rl.memory.Frame;

/**
 *
 * @author Craig Bester
 */
public class DaubTransform extends Transform{
    private final int maxScale;
    private final int baseScale;
    private final int dimensions;
    private final int order;
    
    final int height;
    final int width;
    int numFeatures;
    
    private static BasisFunction[] terms;
    private static double[][][] waveletCache;

    public DaubTransform(int height, int width, int baseScale, int maxScale, int order) {
        this.order = order;
        this.dimensions = 2;
        this.numFeatures = calculateNumTerms(baseScale, maxScale, dimensions, order);
        this.height = height;
        this.width = width;
        
        this.maxScale = maxScale;
        this.baseScale = baseScale;
        initialiseTerms();
        initialiseCache();
        
        // random weight initialisation, prevent incorrect feature correlation?
        //for(int i = 0; i < numFeatures; i++) weights[i] = random.nextDouble();
        //Arrays.fill(weights, 0);
        
        // Optimistic?
        //Arrays.fill(weights,0.25);
    }
    
    public double[] transform(Frame fimg) {
        // avoid extra memory
        //if(phi == null) phi = new double[numFeatures];
        double[] phi = new double[numFeatures];
        double[][] img = fimg.getImage();
        
        for (int k = 0; k < numFeatures; k++) {
            phi[k] = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    phi[k] += img[y][x] * waveletCache[k][y][x];
                }
            }
        }
        
        return phi;
    }
    

    public static int calculateNumTerms(int baseScale, int maxScale, int dimensions, int order) {
        int numPhi = (int)Math.pow(2,baseScale) + order - 2;
        if (baseScale > maxScale) return (int)Math.pow(numPhi, dimensions) + numPhi*dimensions;
        int numPsi = (int)Math.pow(2, maxScale+1) + (maxScale-baseScale+2)*(order-2) - numPhi;
        return (int) Math.pow(numPsi,dimensions) + (int)Math.pow(numPhi, dimensions) + (numPhi+numPsi)*dimensions;
    }

    private void initialiseTerms() {
        if (terms == null) {
            terms = new BasisFunction[numFeatures];
            //System.out.println(terms.length);
            //intialise scaling functions
            int pos = 0;
            for (int i = 0; i < dimensions; i++) {
                int start = pos;
                for (int j = -order + 2; j < Math.pow(2, baseScale); j++) {
                    DaubNScalingFunction curr = new DaubNScalingFunction(baseScale, j, i, order);
                    terms[pos] = curr;
                    pos++;
                    for (int k = 0; k < start; k++) { //add all combinations in
                        terms[pos] = new CombinationFunction(curr, terms[k]);
                        pos++;
                    }
                }
            }
            if (baseScale <= maxScale) {
                int waveletStart = pos;
                for (int i = 0; i < dimensions; i++) {
                    int start = pos;
                    for (int l = baseScale; l <= maxScale; l++) { //go through all scales
                        for (int j = -order + 2; j < Math.pow(2, l); j++) { // go through all translations
                            DaubNWavelet curr = new DaubNWavelet(l, j, i, order);
                            terms[pos] = curr;
                            pos++;
                            for (int k = waveletStart; k < start; k++) { //add all combinations in
                                terms[pos] = new CombinationFunction(curr, terms[k]);
                                pos++;
                            }
                        }
                    }
                }
            }
        }
        /*
         System.out.println(pos);
         System.out.println(terms.length);
         for (int i = 0; i < terms.length; i++) {
         System.out.println(terms[i].getBasisString());
         }
         * 
         */
    }
    
    private void initialiseCache() {
        if(waveletCache == null) {
             waveletCache = new double[numFeatures][height][width];
            //System.out.println(terms.length);
            //intialise scaling functions
            for(int i = 0; i < numFeatures; i++) {
                //System.err.println(terms[i].getBasisString()+":");
                for(int y = 0; y < height; y++) {
                    for(int x = 0; x < width; x++) {
                        double ys = ((double) y)/(height-1);
                        double xs = ((double) x)/(width-1);
                        //waveletCache[i][y][x] = terms[i].getValue(new State(new double[]{y,x}));
                        waveletCache[i][y][x] = terms[i].getValue(new State(new double[]{ys,xs}));
                        //System.err.println(y+","+x+": "+waveletCache[i][y][x]);
                    }
                }
            }
        }
        /*
        System.out.println(pos);
        System.out.println(terms.length);
        for (int i = 0; i < terms.length; i++) {
            System.out.println(terms[i].getBasisString());
        }
         * 
         */
    }
}
