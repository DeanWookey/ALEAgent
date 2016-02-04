
package rl.functionapproximation;

import rl.domain.State;

/**
 *
 * @author itchallengeuser
 */
public class PolynomialBasis extends BasisFunction {
    private final double[] c;
    double shrink = 1;
    
    public PolynomialBasis(double c[]) {
        this.c = c;
        double s = 0;
        for (int i = 0; i < c.length; i++) {
            s += Math.pow(c[i], 2);
        }
        shrink = Math.max(Math.sqrt(s),shrink);
    }

    @Override
    public String getBasisString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getValue(State s) {
        double a = 1;
        for (int i = 0; i < c.length; i++) {
            a *= Math.pow(s.getState()[i],c[i]);
        }
        return a;
    }

    @Override
    public double getShrink() {
        return shrink;
    }
    
    
    
}
