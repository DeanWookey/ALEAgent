package rl.functionapproximation.adaptive_wavelets;

import java.io.Serializable;
import rl.domain.State;

public interface BasisFunction extends Serializable {
  public double getValue(State s);
  public double getShrink(); //most basis functions can just return 1 here.
  public String getBasisString();
}
