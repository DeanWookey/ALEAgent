/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.domain;

/**
 *
 * @author Craig Bester
 */
public class ScaledState extends State {
    
    public ScaledState(double stateVars[], double minValues[], double maxValues[]) {
        super(new double[stateVars.length]);
        double scaledState[] = this.getState();
        for (int i = 0; i < stateVars.length; i++) {
            scaledState[i] = (stateVars[i] - minValues[i])
                    / (maxValues[i] - minValues[i]);
        }
        this.vars = scaledState;
    }
    
    public ScaledState(double stateVars[], double minValues[], double maxValues[], boolean isTerminal) {
        super(new double[stateVars.length],isTerminal);
        double scaledState[] = this.getState();
        for (int i = 0; i < stateVars.length; i++) {
            scaledState[i] = (stateVars[i] - minValues[i])
                    / (maxValues[i] - minValues[i]);
        }
        this.vars = scaledState;
    }
    
    public ScaledState(double stateVars[][], double minValues[], double maxValues[], boolean isTerminal) {
        super(stateVars,isTerminal);
        for (int i = 0; i < vars.length; i++) {
            vars[i] = (vars[i] - minValues[i])
                    / (maxValues[i] - minValues[i]);
        }
    }

}
