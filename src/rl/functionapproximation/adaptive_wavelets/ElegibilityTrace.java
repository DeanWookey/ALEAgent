package rl.functionapproximation.adaptive_wavelets;

import java.io.Serializable;

/**
 *
 * @author dean
 */
public class ElegibilityTrace implements Serializable {
    double trace[];
   public ElegibilityTrace(double initialTrace[]) {
       this.trace = initialTrace;
   }

   public ElegibilityTrace(int terms) {
       this.trace = new double[terms];
   }

   public double[] getTrace() {
       return trace;
   }

   public void setTrace(double trace[]) {
       this.trace = trace;
   }
}
