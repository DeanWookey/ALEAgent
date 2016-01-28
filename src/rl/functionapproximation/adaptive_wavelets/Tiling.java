/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation.adaptive_wavelets;

import java.util.ArrayList;
import rl.domain.State;

/**
 *
 * @author mitch
 */
public class Tiling implements SeriesFunction {
    private final int translation;
    private final int dimension;
    private final double multiplier;
    
    double oldstateval = -1;
    double oldval = 0;
    private double activation;
    private int arrayIndex;
    double supportStart;
    double supportEnd;
    double shrink;
    
    // switch this entire mess over to running on support start / support end? a
    //allow modification of these when conjunctions are added.
    private double childWeight;
    
    
    // tiling is 1 over support, and zero otherwise. support is of length 1/multiplier
    public Tiling(double scale, int translation, int dimension) {
        this.multiplier = scale;
        this.translation = translation;
        this.dimension = dimension;
        this.supportStart = translation/multiplier;
        this.supportEnd = (1+translation)/multiplier;
        this.shrink=1;
    }
    
    

    @Override
    public double getValue(State s) {
        if (s.getState()[dimension] == oldstateval) {
            return oldval;
        }
        double ans = 0;
        if (isSupported(s)) ans = 1;
        oldstateval = s.getState()[dimension];
        oldval = ans;
        return ans;
    }
    
    @Override
    public double getNormalValue(State s) {
        double ans = 0;
        if (isSupported(s)) ans = 1/Math.sqrt(supportEnd-supportStart);
        return ans;
    }
            

    @Override
    public double getShrink() {
        return shrink;
    }

    @Override
    public String getBasisString() {
        return "0," + dimension + "," + supportStart + "," + supportEnd;
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
    public boolean isSupported(State s) {
        return (s.getState()[dimension] >= supportStart && s.getState()[dimension] < supportEnd);
    }
    
    @Override
    public Tiling[] getChildren(int dim) {
        Tiling[] children = new Tiling[2];
        children[0] = new Tiling(multiplier*2, 2*translation, dimension);
        children[1] = new Tiling(multiplier*2, 2*translation+1, dimension);
        return children;
    }
   

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (int)this.multiplier;
        hash = 59 * hash + (int)this.translation;
        hash = 59 * hash + this.dimension;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tiling other = (Tiling) obj;
        if (Double.doubleToLongBits(this.supportStart) != Double.doubleToLongBits(other.supportStart)) {
            return false;
        }
        if (Double.doubleToLongBits(this.supportEnd) != Double.doubleToLongBits(other.supportEnd)) {
            return false;
        }
        return true;
    }
    
    @Override
    public double getSupportStart() {
        return supportStart;
    }
    
    @Override
    public double getSupportEnd() {
        return supportEnd;
    }
    
    public void setSupportStart(double s) {
        supportStart = s;
    }
    
    public void setSupportEnd(double s) {
        supportEnd = s;
    }
    
    
    public double getChildWeight(int dim) {
        return 1;
    }

    @Override
    public double getTranslation() {
        return translation;
    }

    @Override
    public double getScale() {
        return multiplier;
    }
}
