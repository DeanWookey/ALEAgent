package rl.functionapproximation;

import java.util.ArrayList;
import rl.domain.State;

/**
 *
 * @author Dean
 */
public class HaarWavelet extends WaveletFunction {

    private double activation = 1;
    private int arrayIndex;

    public HaarWavelet(int scale, int translation, int dimension) {
        super(scale,translation,dimension);
    }

    @Override
    public double getValue(State s) {
        double t = multiplier * s.getState()[dimension] - translation;
        if (t >= 0 && t < 0.5) {
            return 1;
        } else if (t >= 0.5 && t < 1) {
            return -1;
        } else {
            return 0;
        }
    }

    @Override
    public double getShrink() {
        return 1;
    }

    @Override
    public String getBasisString() {
        return "Haar mother (d=" + dimension + ",s=" + scale + ",t=" + translation + ")";
    }

    public ArrayList<Integer> getDimensions() {
        ArrayList<Integer> temp = new ArrayList<Integer>();
        temp.add(dimension);
        return temp;
    }

    public double getActivation() {
        return activation;
    }

    public void setActivation(double activation) {
        this.activation = activation;
    }

    public void decreaseActivation(double amount) {
        this.activation = this.activation - amount;
        this.activation = Math.max(0, activation);
    }

    public int getArrayIndex() {
        return this.arrayIndex;
    }

    public void setArrayIndex(int index) {
        this.arrayIndex = index;
    }

    public boolean equals(Object o) {
        if (o instanceof HaarWavelet) {
            HaarWavelet d = (HaarWavelet)o;
            if (this.scale == d.scale && this.translation == d.translation && this.dimension == d.dimension) {
                return true;
            }
           
        }
        return false;

    }

    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.scale;
        hash = 53 * hash + this.translation;
        hash = 53 * hash + this.dimension;
        return hash;
    }

}
