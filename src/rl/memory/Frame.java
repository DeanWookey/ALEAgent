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
    public double[][] image;
    int height;
    int width;
    
    public Frame(double[][] image) {
        this.image = image;
        this.height = image.length;
        this.width = image[0].length;
    }
    
    public int getHeight() {
        return height;
    }
    
    public int getWidth() {
        return width;
    }
    
    public double[][] getImage() {
        return image;
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
