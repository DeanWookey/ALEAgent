/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.memory;

import rl.domain.State;

/** Holds a transformed game image.
 *
 * @author Craig Bester
 */
public class Frame {
    double[][] image;
    
    public Frame(double[][] image) {
        this.image = image;
    }
    
    public final double[] toArray() {
        int height = image.length;
        int width = image[0].length;
        double[] ret = new double[height*width];
        for(int i = 0; i < height; i++) {
            System.arraycopy(image[i], 0, ret, i*height, width);
        }
        return ret;
    }
}
