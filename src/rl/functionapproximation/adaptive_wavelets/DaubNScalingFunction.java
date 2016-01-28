/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation.adaptive_wavelets;

import java.util.ArrayList;
import rl.domain.State;
//import org.mathIT.approximation.Wavelets;
/**
 *
 * @author Dean
 */
public class DaubNScalingFunction implements SeriesFunction {
    private final int scale;
    private final int translation;
    private final int dimension;
    private final int order;
    private final double multiplier;
    
    double oldstateval = -1;
    double oldval = 0;
    private double activation;
    private int arrayIndex;
    private double supportStart;
    private double supportEnd;
    
    public DaubNScalingFunction(int scale, int translation, int dimension, int order) {
        this.scale = scale;
        this.multiplier = Math.pow(2,scale);
        this.translation = translation;
        this.dimension = dimension;
        this.order = order;
        this.supportStart = translation/multiplier;
        this.supportEnd = (order-1+translation)/multiplier;
    }

    @Override
    public double getValue(State s) {
        if (s.getState()[dimension] == oldstateval) {
            return oldval;
        }
        double t = multiplier*s.getState()[dimension] - translation;
        double ans = Wavelets.phi(order, t);
        oldstateval = s.getState()[dimension];
        oldval = ans;
        return ans;
    }
    
    

    @Override
    public double getShrink() {
        return 1;
    }

    @Override
    public String getBasisString() {
        return "0," + dimension + "," + scale + "," + translation + "," + order;
    }


    @Override
    public ArrayList<Integer> getDimensions() {
        ArrayList<Integer> temp = new ArrayList<Integer>();
        temp.add(dimension);
        return temp;
    }

        @Override
    public double getActivation() {
        return activation;
    }

    @Override
    public void setActivation(double activation) {
        this.activation = activation;
    }

    @Override
    public void decreaseActivation(double amount) {
        this.activation = this.activation - amount;
        this.activation = Math.max(0, activation);
    }
        @Override
    public int getArrayIndex() {
        return this.arrayIndex;
    }

    @Override
    public void setArrayIndex(int index) {
        this.arrayIndex = index;
    }
    
        @Override
    public boolean equals(Object o) {
        if (o instanceof DaubNScalingFunction) {
            DaubNScalingFunction d = (DaubNScalingFunction)o;
            if (this.scale == d.scale && this.translation == d.translation && this.dimension == d.dimension && this.order == d.order) {
                return true;
            }
           
        }
        return false;

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + this.scale;
        hash = 59 * hash + this.translation;
        hash = 59 * hash + this.dimension;
        hash = 59 * hash + this.order;
        return hash;
    }

    @Override
    public DaubNScalingFunction[] getChildren(int dim) {
        DaubNScalingFunction[] children = new DaubNScalingFunction[2];
        children[0] = new DaubNScalingFunction(scale+1, 2*translation, dimension, order);
        children[1] = new DaubNScalingFunction(scale+1, 2*translation+1, dimension, order);
        return children;
    }

    @Override
    public boolean isSupported(State s) {
        return (s.getState()[dimension] >= supportStart && s.getState()[dimension] <= supportEnd);
    }
    @Override
    public double getSupportStart() {
        return supportStart;
    }
    @Override
    public double getSupportEnd() {
        return supportEnd;
    }
    
    public double getChildWeight(int dimension) {
        return 0;
    }

    @Override
    public double getTranslation() {
        return translation;
        }

    @Override
    public double getScale() {
        return scale;
    }

    @Override
    public double getNormalValue(State s) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

