/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation.adaptive_wavelets;

import java.util.ArrayList;
import rl.functionapproximation.adaptive_wavelets.Sample;
import rl.domain.State;

/**
 *
 * @author mitch
 */
public class IFDDext {
    // a set of tools to assist with wifdd
    
    public static void addFeature(QApproximator q, AdaptiveBasisElement newFeature, int a, State s) {
        //if (a==-0) System.out.println("Adding "+newFeature.getBasisString());
        if (q.getBasisIndex(newFeature, a) == -1) {
            ((AdaptiveBasis) q.FAs[a]).addConjunctions(newFeature, s);
            q.addBasis(newFeature, a);
            //((AdaptiveBasis) q.FAs[a]).addSplits(newFeature);
        }
    }
    
    
    public static void update(QApproximator q, Sample samp, double conjtau, double gamma) {
        //returns AdaptiveBasisElement with relevance above tau, if any.
        double delta;
        //ArrayList<AdaptiveBasisElement> featuresToAdd = new ArrayList();
        delta = samp.reward - (q.valueAt(samp.s, samp.action) - gamma * q.valueAt(samp.nextState, samp.nextAction));
        AdaptiveBasisElement highestBf = null;
        AdaptiveBasisElement bf;
        double highest = 0;
        double rel;
        
        //ArrayList<AdaptiveBasisElement> conjuncts = ((AdaptiveBasis) q.FAs[samp.action]).getConjunctions();
        ArrayList<AdaptiveBasisElement> conjuncts = ((AdaptiveBasis) q.FAs[samp.action]).getSupportedConjunctions(samp.s);
        for (int i = conjuncts.size() - 1; i >= 0; i--) {
            bf = conjuncts.get(i);
            bf.updateActivationCount(delta, samp.s);
            bf.updateDecayRelevance(delta, samp.s);
            rel = bf.getRelevance();
            if (rel > conjtau && rel > highest) {
                highestBf = bf;
                highest = rel;

            }
        }
        if (highestBf!=null) {
                //conjuncts.remove(toRemove);
                ((AdaptiveBasis) q.FAs[samp.action]).removeConjunctions(highestBf);
                //System.out.println(" Combining");
                addFeature(q, highestBf, samp.action, samp.s);
            }
        //else System.out.println(" No change");
    }
   
    
    
}
