/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rl.functionapproximation.adaptive_wavelets;

/**
 *
 * @author mitch
 */
public class BSpline {
    public static double Phi(double x, int order) {
        if (order == 0) {
            if (x>0 && x<=1) return 1;
        } else if (order == 1) {
            if (x>0 && x<= 1) return x;
            if (x>1 && x<= 2) return 2-x;
        } else if (order == 2) {
            if (x>0 && x<= 1) return (0.5*Math.pow(x, 2))/0.75;
            if (x>1 && x<= 2) return (0.75 - Math.pow((x-1.5),2))/0.75;
            if (x>2 && x<= 3) return (0.5*Math.pow(x-3, 2))/0.75;
        } else if (order == 3) {
            if (x>0 && x<= 1) return (Math.pow(x, 3)/6.0)*(3/2.0);
            if (x>1 && x<= 2) return (2/3.0 -x*Math.pow(x-2,2)/2)*(3/2.0);
            if (x>2 && x<= 3) return (-22/3.0+0.5*x*(20+x*(x-8)))*(3/2.0);
            if (x>3 && x<= 4) return (-Math.pow(x-4, 3)/6.0)*(3/2.0);
        } else if (order == 4) {
            if (x>0 && x<= 1) return Math.pow(x, 4)/24.0;
            if (x>1 && x<= 2) return (-5+2*x*(10+x*(-15-2*x*(x-5))))/24.0;
            if (x>2 && x<= 3) return (155+6*x*(x-5)*(10+x*(x-5)))/24.0;
            if (x>3 && x<= 4) return (-655+2*x*(390+x*(-165-2*x*(x-15))))/24.0;
            if (x>4 && x<= 5) return Math.pow(x-5, 4)/24.0;
        }
        return 0;
    }
    
    public static double normalPhi(double x, int order) {
        if (order == 0) {
            if (x>0 && x<=1) return 1;
        } else if (order == 1) {
            if (x>0 && x<= 1) return 1.22474*x;
            if (x>1 && x<= 2) return 1.22474*(2-x);
        } else if (order == 2) {
            if (x>0 && x<= 1) return 1.3484*(0.5*Math.pow(x, 2));
            if (x>1 && x<= 2) return 1.3484*(0.75 - Math.pow((x-1.5),2));
            if (x>2 && x<= 3) return 1.3484*(0.5*Math.pow(x-3, 2));
        } else if (order == 3) {
            if (x>0 && x<= 1) return 1.44433*(Math.pow(x, 3)/6.0);
            if (x>1 && x<= 2) return 1.44433*(2/3.0 -x*Math.pow(x-2,2)/2);
            if (x>2 && x<= 3) return 1.44433*(-22/3.0+0.5*x*(20+x*(x-8)));
            if (x>3 && x<= 4) return 1.44433*(-Math.pow(x-4, 3)/6.0);
        } else if (order == 4) {
            if (x>0 && x<= 1) return 1.52425*Math.pow(x, 4)/24.0;
            if (x>1 && x<= 2) return 1.52425*(-5+2*x*(10+x*(-15-2*x*(x-5))))/24.0;
            if (x>2 && x<= 3) return 1.52425*(155+6*x*(x-5)*(10+x*(x-5)))/24.0;
            if (x>3 && x<= 4) return 1.52425*(-655+2*x*(390+x*(-165-2*x*(x-15))))/24.0;
            if (x>4 && x<= 5) return 1.52425*Math.pow(x-5, 4)/24.0;
        }
        return 0;
    }
    
    public static double Psi(double x, int order) {
        if (order == 0) {
            if (x>0 && x<= 0.5) return 1;
            if (x>0.5 && x<= 1) return -1;
        } else if (order == 1) {
            return 0;
        } else if (order == 2) {
            if (x>0 && x<= 0.5) return Math.pow(x,2)/240.0;
            if (x>0.5 && x<= 1) return (-8 + (32 - 31*x)*x)/240.0;
            if (x>1 && x<= 1.5) return (229 - 442*x + 206*Math.pow(x,2))/(240.0);
            if (x>1.5 && x<= 2) return (-1643 + 2054*x - 626*Math.pow(x,2))/(240.0);
            if (x>2 && x<= 2.5) return (-5 + 2*x)*(-339 + 176*x)/(80.0);
            if (x>2.5 && x<= 3) return (-5 + 2*x)*(-541 + 176* x)/(-80.0);
            if (x>3 && x<= 3.5) return  (7023 - 4206* x + 626 *Math.pow(x, 2))/240.0;
            if (x>3.5 && x<= 4) return (-3169 + 2* (809 - 103 *x)* x)/(240.0);
            if (x>4 && x<= 4.5) return (623 + x *(-278 + 31 *x))/(240.0);
            if (x>4.5 && x<= 5) return Math.pow(x-5, 2)/(-240.0);
        } else if (order == 3) {
            return 0;
        } else if (order == 4) {
            return 0;
        }
        return 0;
    }
}
