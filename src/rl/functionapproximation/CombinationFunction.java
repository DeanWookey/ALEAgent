/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation;

import java.util.ArrayList;
import rl.domain.State;

/**
 *
 * @author Craig Bester
 */
public class CombinationFunction extends BasisFunction {
    private BasisFunction bf1;
    private BasisFunction bf2;

    public CombinationFunction(BasisFunction bf1, BasisFunction bf2) {
        this.bf1 = bf1;
        this.bf2 = bf2;
    }
    
    @Override
    public String getBasisString() {
        return bf1.getBasisString() + " " + bf2.getBasisString();
    }

    @Override
    public double getValue(State s) {
        return bf1.getValue(s) * bf2.getValue(s);
    }

    @Override
    public double getShrink() {
        return 1;
    }

}
