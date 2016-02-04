package transform;

import java.util.Random;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import rl.domain.State;
import rl.functionapproximation.BSplineBasis;
import rl.functionapproximation.BasisFunction;
import rl.functionapproximation.ExactFullFourierBasis;
import rl.functionapproximation.FullFourierBasis;
import rl.functionapproximation.FullPolynomialBasis;
import rl.functionapproximation.Transformation2D;

/**
 *
 * @author itchallengeuser
 */
public class TransformTest {

    public TransformTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testForwardTransform() {
        int imWidth = 4;
        int imHeight = 4;
        double arr[][] = randomArray(imWidth, imHeight);
        BSplineBasis fb = new BSplineBasis(1, imWidth, imHeight, 3, -1, 1, false);
        System.out.println(fb.getBasisFunctions().length);
        //FullPolynomialBasis fb = new FullPolynomialBasis(1, imWidth, imHeight, imWidth-1);
        Transformation2D transform = new Transformation2D(imWidth, imHeight, fb.getBasisFunctions());
        double result[] = transform.transform(arr);
        double untransformed[][] = untransform(imWidth, imHeight, result, transform.getBasisFunctions());
        assertTrue(compare(arr, untransformed, 0.0000000001));
    }

    public double[][] untransform(int width, int height, double transformedSpace[], BasisFunction basisFunctions[]) {
        double result[][] = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < basisFunctions.length; k++) {
                    result[i][j] += transformedSpace[k] * basisFunctions[k].getValue(new State((double) i / (width - 1), (double) j / (height - 1)));
                }
            }
        }
        return result;
    }

    public boolean compare(double arr[][], double arr2[][], double tolerance) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                if (Math.abs(arr[i][j] - arr2[i][j]) > tolerance) {
                    return false;
                }
            }
        }
        return true;
    }

    public double[][] randomArray(int xSize, int ySize) {
        Random r = new Random();
        double arr[][] = new double[xSize][ySize];
        for (int i = 0; i < xSize; i++) {
            for (int j = 0; j < ySize; j++) {
                arr[i][j] = r.nextDouble() * 10;
            }
        }
        return arr;
    }

    public double[] unroll(double arr[][]) {
        double result[] = new double[arr.length * arr[0].length];
        for (int i = 0; i < arr.length; i++) {
            System.arraycopy(arr[i], 0, result, i * arr[i].length, arr[i].length);
        }
        return result;
    }

    /**
     * public double[][] roll(double arr[], int width, int height) { double
     * result[][] = new double[width][height]; * for (int i = 0; i < height;
     * i++) { System.arraycopy(arr, ) } }
     */
}
