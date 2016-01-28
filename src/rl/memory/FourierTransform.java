/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.memory;

import rl.memory.Frame;

/**
 *
 * @author Craig Bester
 */
public class FourierTransform extends Transform{

    // Static to save memory
    //  So don't use several bases with different parameters or everything dies
    static double[][] cosine;
    static int[][] coefficients; //same for every frame

    final int numDimensions;
    final int numFeatures;
    final int height;
    final int width;
    final int order;

    public FourierTransform(int height, int width, int order) {
        this.numDimensions = 2;
        this.numFeatures = (int) Math.pow(order + 1, numDimensions);

        this.order = order;
        this.height = height;
        this.width = width;

        computeFourierCoefficients();
        precalculateCosine();
    }

    public double[] transform(Frame fimg) {
        //int xco, yco;
        double[] phi = new double[numFeatures];
        double[][] img = fimg.image;
        /*for (int k = 0; k < numFeatures; k++) {
         // have to reset old value when overwriting
         phi[k] = 0;

         xco = coefficients[k][1];
         yco = coefficients[k][0];
         for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
         // x,y already scaled in cosine pre-calculation
         phi[k] += img[y][x] * cosine[yco * y][xco * x];
         }
         }
         }*/

        for (int yco = 0; yco < order; yco++) {
            for (int xco = 0; xco < order; xco++) {
                // have to reset old value when overwriting
                int index = yco * xco + xco;
                phi[index] = 0;
                double euclidNormSquared = 0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        // x,y already scaled in cosine pre-calculation
                        phi[index] += img[y][x] * cosine[yco * y][xco * x];
                        euclidNormSquared += cosine[yco* y][xco * x]*cosine[yco* y][xco * x];
                    }
                }

                // Divide by norm for numerical stability in gradient descent?
                // Normalisation is actually standard (is it shrink?), 
                //  it's just that in orthonormal bases the integral amount to 1
                //  , so it makes no difference
                if(euclidNormSquared>0) phi[index]/=euclidNormSquared;
            }
        }

        return phi;
    }

    /**
     * Since we use discrete, bounded indices as input, we can pre-calculate all
     * cosine values (may take a while but probably worth it)
     */
    private void precalculateCosine() {
        if (cosine == null) {
            cosine = new double[order * height][order * width];
            for (int i = 0; i < numFeatures; i++) {
                int xco = coefficients[i][1];
                int yco = coefficients[i][0];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        double xs = ((double) x / width);
                        double ys = ((double) y / height);
                        cosine[yco * y][xco * x] = Math.cos(Math.PI * (yco * ys + xco * xs));
                        //System.err.println("cos(pi*("+yco+"*"+ys+"+"+xco+"*"+xs+")="+cosine[yco*y][xco*x]);
                    }
                }
            }
        }
    }

    private void computeFourierCoefficients() {
        if (coefficients == null) {
            coefficients = new int[numFeatures][numDimensions];
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
}
