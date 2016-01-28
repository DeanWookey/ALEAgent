package rl.functionapproximation.adaptive_wavelets;

import java.util.ArrayList;
import rl.domain.State;

/**
 *
 * @author mitch
 */
public interface SeriesFunction extends WiFDDBasisFunction {
    public double getChildWeight(int dimension);
    public SeriesFunction[] getChildren(int dimension);
    public boolean isSupported(State s);
    public double getSupportStart();
    public double getSupportEnd();
    public double getTranslation();
    public double getScale();
    public double getNormalValue(State s);
}
