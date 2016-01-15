/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package image;

import java.awt.image.BufferedImage;

/**
 *
 * @author Craig Bester
 */
public class HaarTransform {
    public static double[][] dwt(BufferedImage img) {
        return(dwt(Utils.imageToMatrix(img)));
    }
    
    public static double[][] dwt(int[][] img) {
        int width = img[0].length;
        int height = img.length;
        double[][] output = new double[height][width];

        // Rows
        // This function assumes that input.length=2^n, n>1
        for(int row = 0; row < height; row++) {
            for (int length = output[0].length >> 1;  ; length >>= 1) {
                // length = input.length / 2^n, WITH n INCREASING to log_2(input.length)
                for (int i = 0; i < length; ++i) {
                    int sum = img[row][i * 2] + img[row][i * 2 + 1];
                    int difference = img[row][i * 2] - img[row][i * 2 + 1];
                    output[row][i] = sum;
                    output[row][length + i] = difference;
                }
                
                if(length == 1) break;

                //Swap arrays to do next iteration
                //System.arraycopy(output[row], 0, img[row], 0, length << 1);
                for(int m = 0; m < length << 1; m++) {
                    img[row][m] = (int)output[row][m];
                }
            }
        }
        // Columns
        for(int col = 0; col < width; col++) {
            for (int length = img.length >> 1; ; length >>= 1) {
                // length = input.length / 2^n, WITH n INCREASING to log_2(input.length)
                for (int i = 0; i < length; ++i) {
                    int sum = img[i * 2][col] + img[i * 2 + 1][col];
                    int difference = img[i * 2][col] - img[i * 2 + 1][col];
                    output[i][col] = sum;
                    output[length + i][col] = difference;
                }
                
                if(length == 1) break;

                //Swap arrays to do next iteration
                for(int m = 0; m < length << 1; m++) {
                    img[m][col] = (int)output[m][col];
                }
            }
        }
        return output;
    }
    
    public static double[] dwt(int[] input) {
        // This function assumes that input.length=2^n, n>1
        double[] output = new double[input.length];

        for (int length = input.length >> 1; ; length >>= 1) {
            // length = input.length / 2^n, WITH n INCREASING to log_2(input.length)
            for (int i = 0; i < length; ++i) {
                int sum = input[i * 2] + input[i * 2 + 1];
                int difference = input[i * 2] - input[i * 2 + 1];
                output[i] = sum;
                output[length + i] = difference;
            }
            if (length == 1) {
                return output;
            }

            //Swap arrays to do next iteration
            System.arraycopy(output, 0, input, 0, length << 1);
        }
    }
}
