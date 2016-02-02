
package rl.functionapproximation;

import rl.domain.State;

/**
 *
 * @author itchallengeuser
 */
public class FourierBasis extends BasisFunction {
    private final double[] c;
    double shrink = 1;
    
    public FourierBasis(double c[]) {
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
        double a = 0;
        for (int i = 0; i < c.length; i++) {
            a += c[i]*s.getState()[i];
        }
        return Math.cos(Math.PI*a);
    }

    @Override
    public double getShrink() {
        return shrink;
    }
    
    
    
}
