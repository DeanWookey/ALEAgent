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
public abstract class BasisFunction {
    
    public abstract String getBasisString();
    public abstract double getValue(State s);
    
    public double getShrink() {
        return 1;
    }
}
