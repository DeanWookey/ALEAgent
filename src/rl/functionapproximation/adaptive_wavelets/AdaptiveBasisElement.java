/*
 * To change this template, choose Tools | Templates
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
 * @author mitch
 */
public class AdaptiveBasisElement implements SeriesFunction {
    private SeriesFunction[] bf;
    private int arrayIndex;
    private int dimensions;
    private ArrayList<AdaptiveBasisElement> descendents;
    private ArrayList<AdaptiveBasisElement>  parent;
    private AdaptiveBasisElement splitParent;
    private AdaptiveBasisElement[][] splitchildren;
    private double relevance;
    private int activationCount;
    private double subweight;
    private int splitDim;
    private double lowestErr;
    
    private final double relDecay = 0.99;
    private double obsErr;
    private double absErr;
    
    private double[][] support;
    private double activation;
    private double[] childActivation;
    private boolean[] dims;
    private double shrink=1;
    private double relevanceNormalisation;
    private double[] childRelevanceNormalisation;
    
    private double testAbsErr;
    private double testObsErr;
    private double testRel;
    private double testDecayAbsErr;
    private double testDecayObsErr;
    private double testDecayRel;
    private double testActive;
    
    
    
    public AdaptiveBasisElement() {
    }
    
    public AdaptiveBasisElement(SeriesFunction[] bf) {
        this.bf = bf;
        this.dimensions = bf.length;
        descendents = new ArrayList();
        parent = new ArrayList();
        dims = new boolean[dimensions];
        childActivation = new double[dimensions];
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
        setRelevanceNormalisation();
        this.lowestErr=10000;
        this.splitchildren = new AdaptiveBasisElement[dimensions][];
        this.childRelevanceNormalisation = new double[dimensions];
        setShrink();
    }
    
    
    public AdaptiveBasisElement(SeriesFunction bf, int dimensions) {
        this.dimensions = dimensions;
        dims = new boolean[dimensions];
        childActivation = new double[dimensions];
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
        setRelevanceNormalisation();
        this.splitchildren = new AdaptiveBasisElement[dimensions][];
        this.lowestErr=10000;        
        this.childRelevanceNormalisation = new double[dimensions];
        setShrink();
    }
    
    public void setSplitDimension(int dim) {
        splitDim = dim;
    }
    
    public int getSplitDimension() {
        return splitDim;
    }
    
    public void setRelevanceNormalisation() {
        relevanceNormalisation=1;
        for (int i=0; i<support.length; i++) {
            if (dims[i]) relevanceNormalisation = relevanceNormalisation*(Math.min(support[i][1], 1) - Math.max(support[i][0], 0));///(support[i][1] - support[i][0]);
        }
    }
    
    public double getRelevanceNormalisation() {
        return relevanceNormalisation;
    }
    
    public boolean isLowest(double delta) {
        double err=Math.abs(delta);
        if (lowestErr==10000) {
            lowestErr=err;
            return false;
        }
        if (err<lowestErr) {
            lowestErr=err;
            return true;
        }
        //System.out.println("Not the lowest error");
        return false;
    }
    
    public void resetError() {
        lowestErr = 10000;
    }
    
    public double getRelevance() {
        if (this.getActivationCount()==0) return 0;
        return (this.getActivationCount()-1)*this.getRelevanceNormalisation()*Math.abs(relevance/this.getActivationCount());
        // sample-attenuated decayed relevance
    }
    
    public double getObsErr() {
        if (this.getActivationCount()==0) return 0;
        return this.getRelevanceNormalisation()*Math.abs(obsErr/this.getActivationCount());
    }
    
    public double getAbsErr() {
        if (this.getActivationCount()==0) return 0;
        return absErr/this.getActivationCount();
    }
    
    public void getRelevanceReport() {
        System.err.println("Normalisation: "+getRelevanceNormalisation()+", Relevance: "+relevance+", Activation Count: "+getActivationCount());
        //return this.getRelevanceNormalisation()*Math.abs(relevance/this.getActivationCount());
    }
    
    public void setRelevance(double rel) {
        relevance = rel;
    }
    
    public void setAbsErr(double err) {
        absErr = err;
    }
    
    public void setObsErr(double err) {
        obsErr = err;
    }
    
    public int getActivationCount() {
        return activationCount;
    }
    
    public void setActivationCount(int rel) {
        activationCount = rel;
    }
    
    public double getSubWeight() {
        return subweight;
    }
    
    public void setSubWeight(double rel) {
        subweight = rel;
    }
    
    

    @Override
    public double getValue(State s) {
        double value = 1;
        for(int i=0; i<dimensions; i++) {
            //if (s.getState()[i] < support[0][i] || s.getState()[i] > support[1][i]) return 0;
            if (dims[i]) value = value*bf[i].getValue(s);
        }
        return value;
    }

    @Override
    public double getNormalValue(State s) {
        double value = 1;
        for(int i=0; i<dimensions; i++) {
            //if (s.getState()[i] < support[0][i] || s.getState()[i] > support[1][i]) return 0;
            if (dims[i]) value = value*bf[i].getNormalValue(s);
        }
        return value;
    }
    
    public double getSubweightValueAt(State s) {
        double value = 1;
        for(int i=0; i<dimensions; i++) {
            if (s.getState()[i] < support[0][i] || s.getState()[i] > support[1][i]) return 0;
            if (dims[i]) value = value*bf[i].getValue(s);
        }
        return subweight*value;
    }
    
    @Override
    public double getShrink() {
        //return shrink;
        return 1;
    }
    
    public void setShrink() {
        double s = 1;
        for (Integer d : getDimensions()) {
            // s = s * bf[d].getShrink();
            if (s<bf[d].getShrink()) {
                s=bf[d].getShrink();
            }
        }
        this.shrink = s;
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
        this.setRelevanceNormalisation();
        setShrink();
    }
    
    public SeriesFunction getBF(int dim) {
        return bf[dim];
    }
          
    public ArrayList<AdaptiveBasisElement> getParent() {
        return parent;
    }
    
    public void addParent(AdaptiveBasisElement parent) {
        this.parent.add(parent);
    }
            
    public ArrayList<AdaptiveBasisElement> getDescendents() {
        return descendents;
    }
    
    public void addDescendents(AdaptiveBasisElement desc) {
        descendents.add(desc);
    }
    
    
    public AdaptiveBasisElement getSplitParents() {
        return splitParent;
    }
    
    public void setSplitParent(AdaptiveBasisElement desc) {
        splitParent = desc;
    }
   
    @Override
    public boolean equals(Object o) {
        if (o instanceof AdaptiveBasisElement) {
            AdaptiveBasisElement t = (AdaptiveBasisElement) o;
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
    
    private State getParentMidpoint(){
        State s;
        double x[] = new double[this.dimensions];
        for (int i=0; i<dimensions; i++) {
            x[i] = (support[i][1] + support[i][0])/2;
        }
        s = new State(x);
        return s;
    }
    
    @Override
    public AdaptiveBasisElement[] getChildren(int dimension) {
        AdaptiveBasisElement[] children;
        //System.out.println("help: dimension "+dimension+ " of "+ this.getBasisString());
        if (this.splitchildren[dimension] != null) return splitchildren[dimension];
        if (dims[dimension]==false) return null;
        SeriesFunction[] childbfs = this.getBF(dimension).getChildren(dimension);
        children = new AdaptiveBasisElement[childbfs.length];
        for(int i=0; i<childbfs.length; i++) {
            children[i] = new AdaptiveBasisElement(this.bf.clone()); 
            children[i].setBF(childbfs[i], dimension);
            children[i].setSplitParent(this);
            children[i].setSplitDimension(dimension);
            children[i].setRelevance(0);
            children[i].setActivationCount(0);
        }
        this.splitchildren[dimension] = children;
        //double val = 0;
        //State mid = getParentMidpoint();
        //for (AdaptiveBasisElement children1 : children) {
        //    val += children1.getValue(mid);
        //}
        //childRelevanceNormalisation[dimension] = this.getValue(mid)/val;
        return children;
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
        for (int i = 0; i < dimensions; i++) {
            if (dims[i] && !bf[i].isSupported(s)) {
                return false;
            }
        }
        return true;
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
    
    
    public double getChildWeight(int dim) {
        return bf[dim].getChildWeight(dim);
    }
    
    public double getChildActivation(int dim) {
        return childActivation[dim];
    }
    
    public double getChildRelevance (int dim) {
        double sum = 0;
        for (AdaptiveBasisElement child : getChildren(dim)) {
            sum += child.getRelevance();
        }
        if (getChildren(dim).length == 0) return 0;
        return sum/getChildren(dim).length;
    }
    
    public void updateActivationCount(double delta, State s) {
        if (this.isSupported(s)) {
            this.setActivationCount(this.getActivationCount()+1);
        }
    }
    
    public void updateAbsErr(double delta, State s) {
        if (this.isSupported(s)) {
            this.setAbsErr(absErr+Math.abs(delta));
        }
    }
    
    public void updateObsErr(double delta, State s) {
        if (this.isSupported(s)) {
            this.setObsErr(obsErr+this.getNormalValue(s)*Math.abs(delta));
        }
    }
    
    
    public void updateDecayAbsErr(double delta, State s) {
        if (this.isSupported(s)) {
            //if (this.getActivationCount() == 1)  this.setAbsErr(Math.abs(delta));
            this.setAbsErr(relDecay*absErr+(1-relDecay)*Math.abs(delta));
        }
    }
    
    public void updateDecayObsErr(double delta, State s) {
        if (this.isSupported(s)) {
            //if (this.getActivationCount() == 1) this.setObsErr(this.getNormalValue(s)*Math.abs(delta));
            this.setObsErr(relDecay*obsErr+this.getNormalValue(s)*Math.abs(delta));
        }
    }
    
    public void updateRelevance(double delta, State s) {
        if (this.isSupported(s)) {
            //this.setActivationCount(this.getActivationCount()+1);
            this.setRelevance(relevance+this.getNormalValue(s)*delta);
        }
    }
    
    
    
    public void updateDecayRelevance(double delta, State s) {
        if (this.isSupported(s)) {
            //this.setActivationCount(this.getActivationCount()+1);
            //if (this.getActivationCount() == 1) this.setRelevance(this.getNormalValue(s)*delta);
            this.setRelevance(relDecay*relevance+(1-relDecay)*this.getNormalValue(s)*delta);
        }
    }
    
    public void updateChildRelevance (int dim, double delta, State s) {
        for (AdaptiveBasisElement child : getChildren(dim)) {
            if (child.isSupported(s)) {
                child.updateRelevance(delta, s);
            }
        }
    }
    
    public void updateChildDecayRelevance (int dim, double delta, State s) {
        for (AdaptiveBasisElement child : getChildren(dim)) {
            if (child.isSupported(s)) {
                child.updateDecayRelevance(delta, s);
            }
        }
    }
    
    public void updateParentChildDecayRelevance(double delta, State s) {
        this.updateActivationCount(delta, s);
        this.updateDecayRelevance(delta, s);
        for (Integer d : this.getDimensions()) {
            for (AdaptiveBasisElement child : getChildren(d)) {
                if (child.isSupported(s)) {
                    child.updateActivationCount(delta, s);
                    child.updateDecayRelevance(delta, s);
                }
            }
        }
    }
    
    public void updateDecayErrors(double delta, State s) {
        this.updateDecayRelevance(delta, s);
        //this.updateDecayAbsErr(delta, s);
        this.updateDecayObsErr(delta, s);
    }
    
    public void updateDecayParentErrors(double delta, State s) {
        this.updateParentChildDecayRelevance(delta, s);
        this.updateDecayObsErr(delta, s);
        //this.updateDecayAbsErr(delta, s);
    }
    
    public double getErrorRatio() {
        if (this.getActivationCount()==0) return 0;
        return (this.getActivationCount()-1)*this.getRelevanceNormalisation()*(obsErr - Math.abs(relevance))/this.getActivationCount(); // note: this has changed, and would need to take into account activation, etc, 
    }
    
    public double getErrorRatioNew() {
        if (this.getActivationCount()==0) return 0;
        return this.getChildRelevance(splitDim) - this.getRelevance();
    }
    
    public void setChildActivation(double val, int dim) {
        childActivation[dim] = val;
    }
    
    public double getValueAtChildSum(State s, int dim) {
        double val=0;
        AdaptiveBasisElement[] children = getChildren(dim);
        if (children!= null) { // subtract parent value
            for (AdaptiveBasisElement children1 : children) {
                val += children1.getValue(s);
            }
            val = val*childRelevanceNormalisation[dim] - getValue(s);  //parent subtraction added
        }
        return val; // zero if no children in that dim
    }

    @Override
    public double getTranslation() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getScale() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    public void testUpdateAllErrs(double delta, State s) {
        if (isSupported(s)) {
            testActive += 1;
//            if (testActive == 1) {
//                testDecayAbsErr = Math.abs(delta);
//                testDecayObsErr = Math.abs(delta * this.getNormalValue(s));
//                testDecayRel = delta * this.getNormalValue(s);
//            } else {
                testDecayAbsErr = relDecay * testDecayAbsErr + (1 - relDecay) * Math.abs(delta);
                testDecayObsErr = relDecay * testDecayObsErr + (1 - relDecay) * Math.abs(delta * this.getNormalValue(s));
                testDecayRel = relDecay * testDecayRel + (1 - relDecay) * delta * this.getNormalValue(s);
            //}
            testAbsErr = testAbsErr + Math.abs(delta);
            testObsErr = testObsErr + Math.abs(delta * this.getNormalValue(s));
            testRel = testRel + delta * this.getNormalValue(s);
        }
    }
    
    public double[] testGetAllErrs() {
        //change to required stats
        double[] results = new double[13];
        if (testActive == 0) {
            results[1] = 0;
            results[2] = 0;
            results[3] = Math.abs(testDecayRel*this.getRelevanceNormalisation());
            results[4] = 0;
            results[5] = 0;
            results[6] = 0;
            results[7] = testDecayObsErr*this.getRelevanceNormalisation() - results[3];
            results[8] = 0;
            results[9] = 0;
            results[10] = 0;
            results[11] = testDecayAbsErr*this.getRelevanceNormalisation() - results[3];
            results[12] = 0;       
            return results;
        }
        results[1] = Math.abs(testRel*this.getRelevanceNormalisation()/testActive); // relevance;
        results[2] = Math.abs((testActive-1)*testRel*this.getRelevanceNormalisation()/Math.pow(testActive,2)); //sample attenuated rel
        results[3] = Math.abs(testDecayRel*this.getRelevanceNormalisation());
        results[4] = Math.abs((testActive-1)*testDecayRel*this.getRelevanceNormalisation()/testActive); // sample attenuated decayed rel;
        
        //derived stat: obs err - rel
        results[5] = testObsErr*this.getRelevanceNormalisation()/testActive - results[1];
        results[6] = (testActive-1)*testObsErr*this.getRelevanceNormalisation()/Math.pow(testActive,2) - results[2];
        results[7] = testDecayObsErr*this.getRelevanceNormalisation() - results[3];
        results[8] = (testActive-1)*testDecayObsErr*this.getRelevanceNormalisation()/testActive - results[4];
        results[9] = testAbsErr*this.getRelevanceNormalisation()/testActive - results[1];
        results[10] = (testActive-1)*testAbsErr*this.getRelevanceNormalisation()/Math.pow(testActive,2) - results[2];
        results[11] = testDecayAbsErr*this.getRelevanceNormalisation() - results[3];
        results[12] = (testActive-1)*testDecayAbsErr*this.getRelevanceNormalisation()/testActive - results[4];
        return results;
    }
}
