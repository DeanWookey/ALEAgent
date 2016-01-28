/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation;

import java.util.ArrayList;
import rl.domain.State;

/**
 *
 * @author mitch
 */
public class BSplinePhi extends BasisFunction {
    private final int scale;
    private final int translation;
    private final int dimension;
    private final double multiplier;
    private final int order;
    
    double oldstateval = -1;
    double oldval = 0;
    private double activation;
    private int arrayIndex;
    double supportStart;
    double supportEnd;
    double childWeight;
    int maxScale=7; //previously 7 for non-pinball runs
    
    public BSplinePhi(int scale, int translation, int dimension, int order) {
        this.scale = scale;
        this.multiplier = Math.pow(2,scale);
        this.translation = translation;
        this.dimension = dimension;
        this.order = order;
        this.supportStart = translation/multiplier;
        this.supportEnd = (order+1+translation)/multiplier;
        this.childWeight=1;
    }
    
    public BSplinePhi(int scale, int translation, int dimension, int order, double childWeight) {
        this.scale = scale;
        this.multiplier = Math.pow(2,scale);
        this.translation = translation;
        this.dimension = dimension;
        this.order = order;
        this.supportStart = translation/multiplier;
        this.supportEnd = (order+1+translation)/multiplier;
        this.childWeight=childWeight;
    }
    

    @Override
    public double getValue(State s) {
        //if (s.getState()[dimension] == oldstateval) {
       //     return oldval;
        //}
        double t = multiplier*s.getState()[dimension] - translation;
        //double ans = Math.sqrt(multiplier)*BSpline.Phi(t, order);
        double ans = BSpline.Phi(t, order);
        //oldstateval = s.getState()[dimension];
        //oldval = ans;
        return ans;
    }
    
    public double getNormalValue(State s) {
        double t = multiplier*s.getState()[dimension] - translation;
        //double ans = Math.sqrt(multiplier)*BSpline.Phi(t, order);
        return  Math.sqrt(multiplier)*BSpline.normalPhi(t, order);
    }
    
    
    @Override
    public double getShrink() {
        //return Math.sqrt(multiplier);
        return 1;
    }
    
    

    @Override
    public String getBasisString() {
        return ("Phi (0," + dimension + "," + scale + "," + translation+")");
    }
    
    private double[] makeChildWeights(int order) {
        double weights[] = new double[order+2];
        if (order==0) {
            weights[0] = 1;
            weights[1] = 1;
        } else if (order ==1) {
            weights[0] = 0.5;
            weights[1] = 1;
            weights[2] = 0.5;
        } else if (order==2) {
            weights[0] = 0.25;
            weights[1] = 0.75;
            weights[2] = 0.75;
            weights[3] = 0.25;
        } else if (order ==3) {
            weights[0] = 0.125;
            weights[1] = 0.5;
            weights[2] = 0.75;
            weights[3] = 0.5;
            weights[4] = 0.125;
        } else if (order == 4) {
            weights[0] = 0.0625;
            weights[1] = 0.3125;
            weights[2] = 0.625;
            weights[3] = 0.625;
            weights[4] = 0.3125;
            weights[5] = 0.0625;
        }
        return weights;
    }
    
    public BSplinePhi[] getChildren(int dim) {
        double[] newWeight;
        ArrayList<BSplinePhi> children = new ArrayList();
        //double sqr = Math.sqrt(2);
        BSplinePhi[] child;
        newWeight = makeChildWeights(order);
        for (int i=0; i<order+2; i++) {
            if (scale + 1 <= maxScale) {
                children.add(new BSplinePhi(scale + 1, 2 * translation + i, dimension, order, newWeight[i]));
                //children.add(new BSplinePhi(scale + 1, 2 * translation + i, dimension, order, newWeight[i]/sqr)); // remove sqr if functions arent value-normalised
            }
        }
        child = new BSplinePhi[children.size()];
        for (int i = 0; i < child.length; i++) {
            child[i] = children.get(i);
        }
        return child;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + this.scale;
        hash = 59 * hash + this.translation;
        hash = 59 * hash + this.dimension;
        return hash;
    }
    
    public double getSupportStart() {
        return supportStart;
    }
    
    public double getSupportEnd() {
        return supportEnd;
    }
    
    public double getChildWeight(int dimension) {
        return childWeight;
    }

}
