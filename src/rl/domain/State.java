/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.domain;

import java.util.Arrays;
import rl.memory.Frame;

/**
 *
 * @author Craig Bester
 */
public class State {

    //Uses several cropped and grayscaled game frames as a State

    //protected double[] arr;
    //protected Frame[] vars;
    protected double[] vars;
    
    protected int numDimensions;
    protected boolean isTerminal;
    private boolean replaced;

   /* public State(Frame[] images) {
        this.vars = images;
        
        this.numDimensions = vars.length*vars[0].getHeight()*vars[0].getWidth();
        this.isTerminal = false;
        
        replaced = false;
    }

    public State(Frame[] images, boolean isTerminal) {
        this.vars = images;
        this.numDimensions = vars.length;
        this.isTerminal = isTerminal;
        
        replaced = false;
    }

    public void replace(Frame[] vars, boolean isTerminal) {
        this.vars = vars;
        this.isTerminal = isTerminal;
        replaced = true;
    }*/
    
    public State(double[] vars) {
        this.vars = vars;
        
        this.numDimensions = vars.length;
        this.isTerminal = false;
        
        replaced = false;
    }

    public State(double[] vars, boolean isTerminal) {
        this.vars = vars;
        this.numDimensions = vars.length;
        this.isTerminal = isTerminal;
        
        replaced = false;
    }

    public void replace(double[] vars, boolean isTerminal) {
        //this.vars = vars;
        
        //overwrite instead of reference since garbage collector
        System.arraycopy(vars, 0, this.vars, 0, numDimensions);
        this.isTerminal = isTerminal;
        replaced = true;
    }
    
    public void replace(State s) {
        //this.vars = vars;
        
        //overwrite instead of reference since garbage collector
        System.arraycopy(s.vars, 0, this.vars, 0, numDimensions);
        this.isTerminal = s.isTerminal();
        replaced = true;
    }

    /*// Cannot simply return a double array since we want to have a basis per frame
    public Frame[] getState() {
        return vars;
    }*/
    
    public double[] getState() {
        return vars;
    }
    
    /*public double[] getArray() {
        if(arr==null) {
            arr = new double[numDimensions];
            int height = vars[0].getHeight();
            int width = vars[0].getWidth();

            int j = 0;
            for (Frame f : vars) {
                for (int i = 0; i < height; i++) {
                    System.arraycopy(f.image[i], 0, arr, i * j * height, width);
                }
                j++;
            }
            replaced=false;
        }
        else if(replaced) {
            int height = vars[0].getHeight();
            int width = vars[0].getWidth();

            int j = 0;
            for (Frame f : vars) {
                for (int i = 0; i < height; i++) {
                    System.arraycopy(f.image[i], 0, arr, i * j * height, width);
                }
                j++;
            }
            replaced=false;
        }
        return arr;
    }*/

    public int getStateLength() {
        return numDimensions;
    }

    public void setTerminal(boolean terminal) {
        isTerminal = terminal;
    }

    public boolean isTerminal() {
        return isTerminal;
    }
    
}
