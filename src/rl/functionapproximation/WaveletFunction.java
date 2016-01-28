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
public abstract class WaveletFunction extends BasisFunction {
    protected final int scale;
    protected final int translation;
    protected final int dimension;
    protected final double multiplier;
    
    public WaveletFunction(int scale, int translation, int dimension) {
        this.scale = scale;
        this.multiplier = Math.pow(2, scale);
        this.translation = translation;
        this.dimension = dimension;
    }
    
    public abstract double getValue(State s);
    
    public double getTranslation() {
        return translation;
    }

    public double getScale() {
        return scale;
    }
    
    public double getShrink() {
        return 1;
    }
}
