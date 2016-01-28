/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.memory;

import rl.memory.Frame;

/**
 *
 * @author Craig Bester
 */
public abstract class Transform {
    public abstract double[] transform(Frame fimg);
}
