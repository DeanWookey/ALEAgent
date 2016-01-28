/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation.adaptive_wavelets;

import java.util.ArrayList;

/**
 *
 * @author dean
 */
public interface WiFDDBasisFunction extends BasisFunction {
    public ArrayList<Integer> getDimensions();
    public double getActivation();
    public void setActivation(double activation);
    public void decreaseActivation(double activation);
    public void setArrayIndex(int index);
    public int getArrayIndex();
}
