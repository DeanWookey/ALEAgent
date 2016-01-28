package rl.functionapproximation.adaptive_wavelets;

/**
 * A full tiling of haar wavelets and scaling functions up to some order
 * @author Dean
 */
public class IndependentHaarMathIT extends FunctionApproximator {

    private final int maxScale;
    private final int baseScale;
    private final int dimensions;

    public IndependentHaarMathIT(int baseScale, int maxScale, int dimensions) {
        super(calculateNumTerms(baseScale, maxScale, dimensions), dimensions);
        this.maxScale = maxScale;
        this.baseScale = baseScale;
        this.dimensions = dimensions;
        initialiseTerms();
    }

    public static int calculateNumTerms(int baseScale, int maxScale, int dimensions) {

        int num = (int)Math.pow(2, baseScale)*dimensions;
        int num2 = 0;
        for (int i = baseScale; i <= maxScale; i++) {
            num2 += Math.pow(2, i);
        }
        num += num2*dimensions; //-1 same as above 
        return num;
    }

    public void initialiseTerms() {
        super.initialiseTerms();
        BasisFunction terms[] = getTerms();
        System.err.println(terms.length);
        //intialise scaling functions
        int pos = 0;
        for (int i = 0; i < dimensions; i++) {
            int start = pos;
            for (int j = 0; j < Math.pow(2, baseScale); j++) {
                DaubNScalingFunction curr = new DaubNScalingFunction(baseScale, j, i, 2);
                terms[pos] = curr;
                pos++;
            }
        }

        int waveletStart = pos;
        for (int i = 0; i < dimensions; i++) {
            int start = pos;
            for (int l = baseScale; l <= maxScale; l++) { //go through all scales
                for (int j = 0; j < Math.pow(2, l); j++) { // go through all translations
                    DaubNWavelet curr = new DaubNWavelet(l, j, i,2);
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
         * */
    }
}
