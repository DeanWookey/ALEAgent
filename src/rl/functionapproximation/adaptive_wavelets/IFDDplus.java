/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation.adaptive_wavelets;

import java.util.ArrayList;
import rl.functionapproximation.adaptive_wavelets.Sample;
import rl.domain.State;

/**
 *
 * @author micha_000
 */
public class IFDDplus {

    // ifdd+, a slight modification of the evaluation function
    public static void addFeature(QApproximator q, IFDDTilingElement newFeature, int a, double alpha, State s) {
        //System.out.println("Action "+a);
        ArrayList<IFDDTilingElement> parents = newFeature.getParent();
        if (q.getBasisIndex(newFeature, a) == -1) {
            ((IFDDTiling) q.FAs[a]).modifyParents(newFeature);
            ((IFDDTiling) q.FAs[a]).addConjunctions(newFeature, s);
            newFeature.setShrink(alpha);
            double weight = q.getWeights()[q.getBasisIndex(parents.get(0),a)] + q.getWeights()[q.getBasisIndex(parents.get(1), a)];
            q.addBasis(newFeature, a, weight);
            //((IFDDTiling) q.FAs[a]).addSplits(newFeature);
        } else {
            System.err.println("Conjunction "+newFeature.getBasisString()+" already exists!");
        }
    }

    public static void update(QApproximator q, Sample samp, double conjtau, double gamma, double alpha) {
        //returns all IFDDTilingElement with relevance above tau, if any.
        double delta;
        delta = samp.reward - (q.valueAt(samp.s, samp.action) - gamma * q.valueAt(samp.nextState, samp.nextAction));
        ArrayList<IFDDTilingElement> toAdd = new ArrayList();
        IFDDTilingElement bf;
        double rel;

        ArrayList<IFDDTilingElement> conjuncts = ((IFDDTiling) q.FAs[samp.action]).getConjunctions();
        for (int i = conjuncts.size() - 1; i >= 0; i--) {
            bf = conjuncts.get(i);
            bf.setActivationCount(bf.getActivationCount() + 1);
            bf.setRelevance(bf.getRelevance() + delta);
            rel = Math.abs(bf.getRelevance() / Math.sqrt(bf.getActivationCount()));
            if (rel > conjtau) {
                toAdd.add(bf);
            }
        }
        if (toAdd.size() != 0) {
            for (int i = 0; i < toAdd.size(); i++) {
                ((IFDDTiling) q.FAs[samp.action]).removeConjunctions(toAdd.get(i));
            }
            for (int i = 0; i < toAdd.size(); i++) {
                //System.out.println(" Combining");
                addFeature(q, toAdd.get(i), samp.action, alpha, samp.s);
            }

        }
        //else System.out.println(" No change");
    }
}
