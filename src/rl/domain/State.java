/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.domain;

import java.util.Arrays;

/**
 *
 * @author Craig Bester
 */
public class State implements Cloneable{
    //Use a cropped, grayscaled, cosine transformed game frame as a State
    protected double[] vars;
    protected int numDimensions;
    protected boolean isTerminal;
    
    public State(double[] img) {
        this.vars = img;
        this.numDimensions = vars.length;
        this.isTerminal = false;
    }
    
    public State(double[] img, boolean isTerminal) {
        this.vars = img;
        this.numDimensions = vars.length;
        this.isTerminal = isTerminal;
    }
    
    public State(double[][] img, boolean isTerminal) {
        this.vars = new double[img.length*img[0].length];
        for(int i = 0; i < img.length; i++) {
            System.arraycopy(img[i], 0, vars, i*img[0].length, img[0].length);
            /*for(int j = 0; j < img[0].length; j++) {
                vars[i*img[0].length + j] = img[i][j];
            }*/
        }
        this.numDimensions = vars.length;
        this.isTerminal = isTerminal;
    }
    
    public State(int[][] img, boolean isTerminal) {
        this.vars = new double[img.length*img[0].length];
        for(int i = 0; i < img.length; i++) {
            for(int j = 0; j < img[0].length; j++) {
                vars[i*img[0].length + j] = img[i][j];
            }
        }
        this.numDimensions = vars.length;
        this.isTerminal = isTerminal;
    }
    
    public double[] getState() {
        return vars;
    }
    
    public int getStateLength() {
        return numDimensions;
    } 
    
    public void setTerminal(boolean terminal) {
        isTerminal = terminal;
    }
    
    public boolean isTerminal() {
        return isTerminal;
    }
    
    @Override
    protected Object clone() {
        try {
            State s = (State)super.clone();
            s.vars = Arrays.copyOf(this.vars,vars.length);
            s.isTerminal = this.isTerminal;
            return s;
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
