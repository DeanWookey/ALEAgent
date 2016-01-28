/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rl.functionapproximation.adaptive_wavelets;

import java.util.ArrayList;
import rl.domain.State;

/**
 *
 * @author micha_000
 */
public class SmartIndex {
//    a helper class that indexes wavelet tiles by translation and dimension
    // we'll start with a set scale, and add support for multiple scales later
    // 
    private int dimensions;
    private int scale;
    private int support;
    private int translations;
    private int transOffset;
    private ArrayList<AdaptiveBasisElement>[][] index;
    private int size;
    
    public SmartIndex() {
        
    }
    
    public SmartIndex(int dimensions, int scale, int support) {
        translations = (int)Math.pow(2, scale) + support -1;
        transOffset = support - 1;
        this.dimensions = dimensions;
        this.scale = scale;
        this.support = support;
        index = new ArrayList[dimensions][translations];
        for (int i=0; i<dimensions; i++) {
            for (int j=0; j<translations; j++) {
                index[i][j] = new ArrayList<AdaptiveBasisElement>();
            }
        }
        size = 0;
    }
    
    public void add(AdaptiveBasisElement bf) {
        boolean[] dims = bf.getDims();
        int i=0;
        while (!dims[i]) i++;
        index[i][(int)bf.getBF(i).getTranslation()+transOffset].add(bf);
        size++;
        //System.out.println(bf.getBasisString()+" added at dimension "+i+", position "+((int)bf.getBF(i).getTranslation()+transOffset)+" with offset "+transOffset);
    }
    
    public ArrayList activated(State s) {
        ArrayList activated = new ArrayList<AdaptiveBasisElement>();
        ArrayList<AdaptiveBasisElement> cands;
        double[] x = s.getState();
        //System.out.print("State: [");
        //for (Double xx : x) System.out.print(xx+", ");
        //System.out.println("]");
        //System.out.println("Offset: "+transOffset+", Support: "+support+", Scale: "+scale);
        for (int d=0; d<dimensions; d++) {
            for (int k = (int)Math.floor(-support+Math.pow(2, scale)*x[d]+1); k<= Math.ceil(Math.pow(2, scale)*x[d]-1); k++) {
                cands = index[d][k+transOffset];
                for (AdaptiveBasisElement bf : cands) {
                    if (Math.abs(bf.getValue(s)) > Math.pow(10, -10)) {
                        activated.add(bf);
                        //System.out.println(bf.getBasisString()+" activated");
                    }
                }
            }
        }
        return activated;
    }
    
    
    public void remove(AdaptiveBasisElement bf) {
        boolean success;
        boolean[] dims = bf.getDims();
        int i=0;
        while (!dims[i]) i++;
        if (!index[i][(int)bf.getBF(i).getTranslation()+transOffset].remove(bf)) System.err.println("Removal failed.");
        size--;
    }
    
    public ArrayList getAll() {
        ArrayList all = new ArrayList<AdaptiveBasisElement>();
        for (int d=0; d<index.length; d++) {
            for (int k=0; k<index[d].length; k++) {
                all.addAll(index[d][k]);
            }
        }
        return all;
    }
    
    public boolean present(AdaptiveBasisElement bf) {
        boolean success;
        boolean[] dims = bf.getDims();
        int i=0;
        while (!dims[i]) i++;
        //System.out.print("Matching "+bf.getBasisString()+" against ");
        //for (AdaptiveBasisElement bfCheck : index[i][(int)bf.getBF(i).getTranslation()+transOffset]) {
        //    System.out.print(bfCheck.getBasisString()+"; ");
        //}
        //System.out.println(index[i][(int)bf.getBF(i).getTranslation()+transOffset].contains(bf));
        return index[i][(int)bf.getBF(i).getTranslation()+transOffset].contains(bf);
    }
    
    public int size() {
        return size;
    }
}
