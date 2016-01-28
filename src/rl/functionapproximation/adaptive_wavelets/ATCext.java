/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation.adaptive_wavelets;

import java.util.ArrayList;
import rl.functionapproximation.adaptive_wavelets.Sample;

/**
 *
 * @author mitch
 */
public class ATCext {
    // a set of tools to assist with widdd
//    public static void makeSplits(AdaptiveBasis basisFunctions) {
//        basisFunctions.populateSplits();
//    }

    public static void updateRelevanceByChildren(QApproximator q, Sample samp, double gamma) {
        double delta = samp.reward - (q.valueAt(samp.s, samp.action) - gamma * q.valueAt(samp.nextState, samp.nextAction));
        AdaptiveBasisElement bf;
        boolean[] dims;

        for (int t = 0; t < q.FAs[samp.action].terms.length; t++) {
            bf = (AdaptiveBasisElement) q.FAs[samp.action].terms[t];
            if (bf.isSupported(samp.s)) {
                dims = bf.getDims();
                //bf.setActivationCount(bf.getActivationCount() + 1);
                for (int d = 0; d < dims.length; d++) {
                    if (dims[d]) {
                        bf.updateChildRelevance(d, delta, samp.s);
                        //bf.setChildActivation(bf.getChildActivation(d) + delta * bf.getValueAtChildSum(samp.s, d), d);
                    }
                }
            }
        }
    }

    public static void updateAndSplitNew(QApproximator q, Sample samp, double splittau, double gamma) {
        //returns AdaptiveBasisElement with relevance above tau, if any.
        double delta = samp.reward - (q.valueAt(samp.s, samp.action) - gamma * q.valueAt(samp.nextState, samp.nextAction));
        AdaptiveBasisElement bf;
        for (int t = 0; t < q.FAs[samp.action].terms.length; t++) {
            bf = (AdaptiveBasisElement) q.FAs[samp.action].terms[t];
            if (bf.isSupported(samp.s)) {
                bf.updateDecayParentErrors(delta, samp.s);
            }
        }
        AdaptiveBasisElement highestBf = null;
        double highest = 0;
        double rel;
        int highestDim = -1;
        BasisFunction[] terms = q.FAs[samp.action].terms;
        for (int i = terms.length - 1; i >= 0; i--) {
            bf = (AdaptiveBasisElement) terms[i];
            if (bf.isSupported(samp.s)) {
                rel = bf.getErrorRatio();
                if (rel > splittau && rel > highest) {
                    highestBf = bf;
                    highest = rel;
                }
            }
        }
        if (highestBf != null) {
            highest = 0;
            for (Integer d : highestBf.getDimensions()) {
                rel = highestBf.getChildRelevance(d);
                if (rel > highest) {
                    highestDim = d;
                    highest = rel;
                }
            }
            if (highestDim != -1) performSplit(q, highestBf, highestDim, samp.action);
        }
    }
    
    public static void updateAndSplit(QApproximator q, Sample samp, double splittau, double gamma) {
        //returns AdaptiveBasisElement with relevance above tau, if any.
        updateRelevanceByChildren(q, samp, gamma);
        AdaptiveBasisElement highestBf = null;
        AdaptiveBasisElement bf;
        double highest = 0;
        double rel;
        int highestDim = 0;
        BasisFunction[] terms = q.FAs[samp.action].terms;
        for (int i = terms.length - 1; i >= 0; i--) {
            bf = (AdaptiveBasisElement) terms[i];
            if (bf.isSupported(samp.s)) {
                for (Integer d : bf.getDimensions()) {
                    //rel = bf.getRelevanceNormalisation() * Math.abs(bf.getChildActivation(d)) / bf.getActivationCount(); // a change from getActivationCount
                    //System.out.println(bf.getBasisString());
                    rel = bf.getChildRelevance(d);
                    if (rel > splittau && rel > highest) {
                        //System.out.println("BF: "+bf.getBasisString()+" has relevance "+rel+" in dimension "+d);
                        //bf.getRelevanceReport();
                        highestBf = bf;
                        highestDim = d;
                        highest = rel;
                    }
                }
            }
        }
        if (highestBf != null) {
            //System.out.println(" Splitting");
            performSplit(q, highestBf, highestDim, samp.action);

        }  //else System.out.println(" No change");
    }

    public static void performSplit(QApproximator q, AdaptiveBasisElement bf, int dim, int action) {
        //System.out.println("Splitting "+bf.getBasisString()+" dimension " +dim+" action "+action+", with weight "+q.getWeights()[q.getBasisIndex(bf, action)]);
        //((AdaptiveBasis)q.FAs[action]).removeSplits(bf);
        int index;
        double m, tr;
        AdaptiveBasisElement[] children = bf.getChildren(dim);
        tr = q.traces.getLast().getTrace()[q.getBasisIndex(bf, action)];
        for (int i = 0; i < children.length; i++) {
            index = q.getBasisIndex(children[i], action);
            m = children[i].getChildWeight(dim);
            if (index == -1) {
                q.addBasis(children[i], action, m * q.getWeights()[q.getBasisIndex(bf, action)]);
                //System.out.println("Child "+i+" initialised to weight "+(m*q.getWeights()[q.getBasisIndex(bf, action)]));
                //q.addBasis(children[i], action, m * q.getWeights()[q.getBasisIndex(bf, action)], m*tr);
                //((AdaptiveBasis)q.FAs[action]).addSplits(children[i]); 

            } else {
                //System.out.println("Child "+children[i].getBasisString()+" found with weight "+ q.weights[index]);
                q.weights[index] += m * q.getWeights()[q.getBasisIndex(bf, action)];
            }
            //else System.out.println("Child "+children[i].getBasisString()+" found at "+ q.getBasisIndex(children[i], action)+" for action "+action);
        }
        //System.out.println("Removing a bf after adding "+ children.length+ " children");
        if (children.length > 0) {
            q.removeBasis(bf, action);
        }
    }

}
