package image;

import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;

/**
 * Applies a lossless, full 2-D Discrete Cosine Transform (DCTII) to an image.
 * Forward: http://www.mathworks.com/help/images/ref/dct2.html
 * Inverse: http://www.mathworks.com/help/images/ref/idct2.html
 * 
 * @author Craig Bester
 */
public class CosineTransform {
    /** Exploit the fact that image size is constant:
     *  -pre-calculate cosine values
     */
    private static final int size = 84;
    private static final double[] cosine;
    private static final double[] alpha;

    static {
        // pre-calculate cosine values since we have a fixed image size
        cosine = new double[size * (2 * size + 1)];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cosine[i * (2 * j + 1)] = Math.cos((Math.PI * i * (2 * j + 1)) / (2 * size));
            }
        }

        //hacky quick fix to avoid branching during calculation
        alpha = new double[size];
        alpha[0] = 1.0 / Math.sqrt(size);
        for (int i = 1; i < size; i++) {
            alpha[i] = Math.sqrt(2 / (double) size);
        }
    }
    
    public static double[][] transform(BufferedImage img) {
        return(transform(Utils.imageToMatrix(img)));
    }
    
    public static double[][] transform(int[][] img) {
        int width = img[0].length;
        int height = img.length;
        double[][] fspace = new double[height][width];
        double[][] temp = new double[height][width];
        // Rows
        for (int q = 0; q < width; q++) {
            for (int y = 0; y < height; y++) {
                double t = 0;
                for (int x = 0; x < width; x++) {
                    t += img[y][x] * cosine[q * (2 * x + 1)];
                }
                temp[y][q] = t;
            }
        }
        // Columns
        for (int p = 0; p < height; p++) {
            for (int q = 0; q < width; q++) {
                double t = 0;
                for (int y = 0; y < height; y++) {
                    t += temp[y][q] * cosine[p * (2 * y + 1)];
                }
                t = t * alpha[p] * alpha[q];
                fspace[p][q] = t;
            }
        }
        
        return fspace;
    }

    public static BufferedImage inverseTransform(double[][] fspace) {
        // Only meant to be used to ensure the transform works
        int width = fspace[0].length;
        int height = fspace.length;
        BufferedImage img = new BufferedImage(width, height, TYPE_INT_RGB);

        for (int m = 0; m < height; m++) {
            for (int n = 0; n < width; n++) {
                double t = 0;
                for (int p = 0; p < height; p++) {
                    for (int q = 0; q < width; q++) {
                        t += alpha[p] * alpha[q] * fspace[p][q] * cosine[p * (2 * m + 1)] * cosine[q * (2 * n + 1)];
                    }
                }

                img.setRGB(n, m, (int) t);
            }
        }

        return img;
    }
}
