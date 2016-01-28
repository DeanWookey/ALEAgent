package rl.functionapproximation.adaptive_wavelets;

/**
 * A full tiling of daub wavelets and scaling functions up to some order
 * @author Dean
 */
public class IndependentDaubFunctionApproximation extends FunctionApproximator {

    private final int maxScale;
    private final int baseScale;
    private final int dimensions;
    private final int order;

    public IndependentDaubFunctionApproximation(int baseScale, int maxScale, int dimensions, int order) {
        super(calculateNumTerms(baseScale, maxScale, dimensions, order), dimensions);
        this.maxScale = maxScale;
        this.baseScale = baseScale;
        this.dimensions = dimensions;
        this.order = order;
        initialiseTerms();
    }

    public static int calculateNumTerms(int baseScale, int maxScale, int dimensions, int order) {

        int num = (int)Math.pow(2, maxScale+1) + (maxScale-baseScale+2)*(order-2);
        return num*dimensions;
    }

    public void initialiseTerms() {
        super.initialiseTerms();
        BasisFunction terms[] = getTerms();
        //System.out.println(terms.length);
        //intialise scaling functions
        int pos = 0;
        for (int i = 0; i < dimensions; i++) {
            int start = pos;
            for (int j = -order+2; j < Math.pow(2, baseScale); j++) {
                DaubNScalingFunction curr = new DaubNScalingFunction(baseScale, j, i, order);
                terms[pos] = curr;
                pos++;
            }
        }

        int waveletStart = pos;
        for (int i = 0; i < dimensions; i++) {
            int start = pos;
            for (int l = baseScale; l <= maxScale; l++) { //go through all scales
                for (int j = -order+2; j < Math.pow(2, l); j++) { // go through all translations
                    DaubNWavelet curr = new DaubNWavelet(l, j, i, order);
                    //System.out.println(pos);
                    terms[pos] = curr;
                    pos++;
                }
            }
        }
        /*
        System.out.println(pos);
        System.out.println(terms.length);
        for (int i = 0; i < terms.length; i++) {
            System.out.println(terms[i].getBasisString());
        }
         * 
         */
         
    }
}
