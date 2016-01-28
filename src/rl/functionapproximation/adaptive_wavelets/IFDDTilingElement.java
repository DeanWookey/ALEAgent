/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rl.functionapproximation.adaptive_wavelets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import rl.domain.State;
/**
 *
 * @author micha_000
 */
public class IFDDTilingElement implements SeriesFunction {
    private SeriesFunction[] bf;
    private int arrayIndex;
    private int dimensions;
    private ArrayList<IFDDTilingElement> descendents;
    private ArrayList<IFDDTilingElement>  parent;
    private double relevance;
    private int activationCount;
    
    private ArrayList<IFDDTilingElement> exceptions;
    
    private double[][] support;
    private double activation;
    private boolean[] dims;
    private double shrink=1;
    
    public IFDDTilingElement() {
    }
    
    public IFDDTilingElement(SeriesFunction[] bf) {
        this.bf = bf;
        this.dimensions = bf.length;
        descendents = new ArrayList();
        parent = new ArrayList();
        dims = new boolean[dimensions];
        support = new double[dimensions][2];
        for(int i=0; i<dimensions; i++) {
            if (bf[i]!=null) {
            dims[this.bf[i].getDimensions().get(0)] = true;
            support[i][0] = bf[i].getSupportStart();
            support[i][1] = bf[i].getSupportEnd();
            } else {
                support[i][0] = 0;
                support[i][1] = 1;
            }
        }
        exceptions = new ArrayList();
    }
    
    
    public IFDDTilingElement(SeriesFunction bf, int dimensions) {
        this.dimensions = dimensions;
        dims = new boolean[dimensions];
        descendents = new ArrayList();
        parent = new ArrayList();
        this.bf = new SeriesFunction[dimensions];
        this.bf[bf.getDimensions().get(0)] = bf;  // how are the others defined?
        dims[bf.getDimensions().get(0)] = true;
        support = new double[dimensions][2];
        for(int i=0; i<dimensions; i++) {
            if (dims[i]) {
                support[i][0] = bf.getSupportStart();
                support[i][1] = bf.getSupportEnd();
            }
            else {
                support[i][0] = 0;
                support[i][1] = 1;
            }
        }
        exceptions = new ArrayList();
    }
    
    public double getRelevance() {
        return relevance;
    }
    
    public void setRelevance(double rel) {
        relevance = rel;
    }
    
    public int getActivationCount() {
        return activationCount;
    }
    
    public void setActivationCount(int rel) {
        activationCount = rel;
    }
    
    public void addException(IFDDTilingElement bf) {
        exceptions.add(bf);
    }
    
    @Override
    public double getValue(State s) {
        if (exceptionsSupported(s)) return 0;
        double value = 1;
        for(int i=0; i<dimensions; i++) {
            //if (s.getState()[i] < support[0][i] || s.getState()[i] > support[1][i]) return 0;
            if (dims[i]) value = value*bf[i].getValue(s);
        }
        return value;
    }

    
    @Override
    public double getShrink() {
        return shrink;
    }
    
    public void setShrink(double shrink) {
        this.shrink = shrink;
    }
    
    
     @Override
    public ArrayList<Integer> getDimensions() {
         ArrayList<Integer> dim = new ArrayList();
         for(int i=0; i<dimensions; i++) {
             if (dims[i]) dim.add(i);
         }
        return dim;
    }
     
    public boolean[] getDims() {
        return dims;
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
    public String getBasisString() {
        String s = "";
        for(int i=0;i<dimensions;i++){
            if (dims[i]) s = s + " " + bf[i].getBasisString();
        }
        return s;
    }

    public SeriesFunction[] getBFs() {
        return bf.clone();
    }

    public void setBF(SeriesFunction b, int dim) {
        dims[dim] = true;
        bf[dim] = b;
        support[dim][0] = b.getSupportStart();
        support[dim][1] = b.getSupportEnd();
    }
    
    public SeriesFunction getBF(int dim) {
        return bf[dim];
    }
          
    public ArrayList<IFDDTilingElement> getParent() {
        return parent;
    }
    
    public void addParent(IFDDTilingElement parent) {
        this.parent.add(parent);
    }
            
    public ArrayList<IFDDTilingElement> getDescendents() {
        return descendents;
    }
    
    public void addDescendents(IFDDTilingElement desc) {
        descendents.add(desc);
    }
    
   
    public boolean equals(Object o) {
        if (o instanceof IFDDTilingElement) {
            IFDDTilingElement t = (IFDDTilingElement) o;
            SeriesFunction[] bfs2 = t.getBFs();
            if (bf.length != bfs2.length) {
                return false;
            } else {
                return (t.getBasisString().equalsIgnoreCase(this.getBasisString()));
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for(int i=0;i<dimensions;i++){
            if (bf[i] != null) hash += bf[i].hashCode();
        }
        return hash;
    }
    
    public boolean isLazy(int dimension) {
        return (bf[dimension]==null);
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
    public boolean isSupported(State s) {
        if (exceptionsSupported(s)) return false;
        for (int i = 0; i < dimensions; i++) {
            if (dims[i]) {
                if (!bf[i].isSupported(s)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean exceptionsSupported(State s) {
        for (IFDDTilingElement exception : exceptions) {
            if (exception.isSupported(s)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public double getSupportStart() {
        return 0;
    }
    @Override
    public double getSupportEnd() {
        return 0;
    }
    
    public double[][] getSupport() {
        return support;
    }
    

    @Override
    public double getTranslation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getScale() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getChildWeight(int dimension) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SeriesFunction[] getChildren(int dimension) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getNormalValue(State s) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
