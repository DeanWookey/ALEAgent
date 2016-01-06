package image;

import ale.screen.ScreenConverter;
import ale.screen.ScreenMatrix;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Craig
 */
public class Utils {
    
    public static double[][] imageIntToMatrixDouble(BufferedImage img) {
        final int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        final int width = img.getWidth();
        final int height = img.getHeight();
        double[][] result = new double[height][width];
        for (int i = 0, row = 0, col = 0; i < pixels.length; i++) {
            result[row][col] = pixels[i];
            col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }

        return result;
    }
    
    public static double[][] scalePixelValues(BufferedImage img, int minValue, int maxValue) {
        return scalePixelValues(imageToMatrix(img),minValue,maxValue);
    }
    
    public static double[][] scalePixelValues(int[][] img, int minValue, int maxValue) {
        int height = img.length;
        int width = img[0].length;
        double[][] ret = new double[height][width];
        
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                ret[i][j] = (img[i][j] - minValue)/(double)(maxValue - minValue);
            }
        }
        
        return ret;
    }
    
    public static void scalePixelValues(double[][] img, int minValue, int maxValue) {
        int height = img.length;
        int width = img[0].length;
        
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                img[i][j] = (img[i][j] - minValue)/(double)(maxValue - minValue);
            }
        }

    }

    public static int[][] imageToMatrix(BufferedImage img) {
        int type = img.getType();
        switch (type) {
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                return byteImageToMatrix(img);
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            case BufferedImage.TYPE_INT_BGR:
            case BufferedImage.TYPE_INT_RGB:
                return intImageToMatrix(img);
            default:
                return null;
        }
    }

    public static int[][] intImageToMatrix(BufferedImage img) {
        final int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        final int width = img.getWidth();
        final int height = img.getHeight();
        int[][] result = new int[height][width];
        for (int i = 0, row = 0, col = 0; i < pixels.length; i++) {
            result[row][col] = pixels[i];
            col++;
            if (col == width) {
                col = 0;
                row++;
            }
        }

        return result;
    }

    public static int[][] byteImageToMatrix(BufferedImage img) {
        final byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        final int width = img.getWidth();
        final int height = img.getHeight();
        final boolean hasAlphaChannel = img.getAlphaRaster() != null;

        int[][] result = new int[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
                int argb = 0;
                //argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }

    public static int[][] crop(int[][] image, int x, int y, int width, int height) {
        //System.out.println("Cropping a " + image.length + "x" + image[0].length + " image");

        //Order [column][row] - in keeping with ScreenMatrix
        int[][] ret = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int temp = image[i + x][j + y];
                ret[i][j] = temp;
            }
        }
        return ret;
    }

    public static BufferedImage crop(BufferedImage img, int x, int y, int width, int height) {
        //return img.getSubimage(0, 33, 160, 160);
        return img.getSubimage(x, y, width, height);
    }

    public static BufferedImage scale(BufferedImage img, int height, int width) {
        BufferedImage scaledImage = new BufferedImage(
                width,
                height,
                img.getType()
        );

        //DeepMind used some form of bilinear interpolation
        Graphics2D g = scaledImage.createGraphics();
        /*
        g.setRenderingHint(
         RenderingHints.KEY_INTERPOLATION,
         RenderingHints.VALUE_INTERPOLATION_BILINEAR
         );
        */
        g.drawImage(img, 0, 0, width, height, null);
        g.dispose();

        return scaledImage;
    }

    public static void grayscale(double[][] matrix) {
        //Order: [column][row];

        //Alpha included for generality
        int colour, alpha, red, green, blue, lum, gray;

        int h = matrix[0].length;
        int w = matrix.length;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                colour = (int) matrix[x][y];
                alpha = (colour >> 24) & 255;
                red = (colour >> 16) & 255;
                green = (colour >> 8) & 255;
                blue = (colour) & 255;

                //Average: (R+G+B)/3
                //CIE: 0.2126·R + 0.7152·G + 0.0722·B 
                //HSP: sqrt( 0.299*R^2 + 0.587*G^2 + 0.114*B^2 )
                //0.299·R + 0.587·G + 0.114·B - DeepMind used these weights
                //lum = (int)((red+green+blue)/3);
                //lum = (int)(0.2126*red+0.7152*green+0.0722*blue);
                //lum = (int)Math.sqrt(0.299*red*red+0.587*green*green+0.114*blue*blue);
                //lum = (int)(0.299*red+0.587*green+0.114*blue);
                //These weightings provide better visual distinction 
                // between shades of colour (at least visually)
                lum = (int) (0.2126 * red + 0.7152 * green + 0.0722 * blue);

                alpha = (alpha << 24);
                red = (lum << 16);
                green = (lum << 8);
                blue = lum;
                gray = alpha + red + green + blue;

                matrix[x][y] = gray;
            }
        }
    }

    public static void grayscale(int[][] matrix) {
        //Order: [column][row];

        //Alpha included for generality
        int colour, alpha, red, green, blue, lum, gray;

        int h = matrix[0].length;
        int w = matrix.length;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                colour = matrix[x][y];
                alpha = (colour >> 24) & 255;
                red = (colour >> 16) & 255;
                green = (colour >> 8) & 255;
                blue = (colour) & 255;

                //Average: (R+G+B)/3
                //CIE: 0.2126·R + 0.7152·G + 0.0722·B 
                //HSP: sqrt( 0.299*R^2 + 0.587*G^2 + 0.114*B^2 )
                //0.299·R + 0.587·G + 0.114·B - DeepMind used these weights
                //lum = (int)((red+green+blue)/3);
                //lum = (int)(0.2126*red+0.7152*green+0.0722*blue);
                //lum = (int)Math.sqrt(0.299*red*red+0.587*green*green+0.114*blue*blue);
                //lum = (int)(0.299*red+0.587*green+0.114*blue);
                //These weightings provide better visual distinction 
                // between shades of colour (at least visually)
                lum = (int) (0.2126 * red + 0.7152 * green + 0.0722 * blue);

                alpha = (alpha << 24);
                red = (lum << 16);
                green = (lum << 8);
                blue = lum;
                gray = alpha + red + green + blue;

                matrix[x][y] = gray;
            }
        }
    }

    public static void grayscale(BufferedImage img) {
        //Alpha included for generality
        int colour, alpha, red, green, blue, lum, gray;

        int h = img.getHeight();
        int w = img.getWidth();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                colour = img.getRGB(x, y);
                alpha = (colour >> 24) & 255;
                red = (colour >> 16) & 255;
                green = (colour >> 8) & 255;
                blue = (colour) & 255;

                //Average: (R+G+B)/3
                //CIE: 0.2126·R + 0.7152·G + 0.0722·B 
                //HSP: sqrt( 0.299*R^2 + 0.587*G^2 + 0.114*B^2 )
                //0.299·R + 0.587·G + 0.114·B - DeepMind used these weights
                //lum = (int)((red+green+blue)/3);
                //lum = (int)(0.2126*red+0.7152*green+0.0722*blue);
                //lum = (int)Math.sqrt(0.299*red*red+0.587*green*green+0.114*blue*blue);
                //lum = (int)(0.299*red+0.587*green+0.114*blue);
                //These weightings provide better visual distinction 
                // between shades of colour (at least visually)
                lum = (int) (0.2126 * red + 0.7152 * green + 0.0722 * blue);

                alpha = (alpha << 24);
                red = (lum << 16);
                green = (lum << 8);
                blue = lum;
                gray = alpha + red + green + blue;

                img.setRGB(x, y, gray);
            }
        }
    }
    
    public static BufferedImage matrixToImage(double[][] img) {
        int height = img[0].length;
        int width = img.length;
        //BufferedImage.TYPE_3BYTE_BGR required to cast raster to DataBufferByte
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                bi.setRGB(j, i, (int)img[i][j]);
            }
        }
        return bi;
    }

    public static BufferedImage matrixToImage(int[][] img) {
        int height = img[0].length;
        int width = img.length;
        //BufferedImage.TYPE_3BYTE_BGR required to cast raster to DataBufferByte
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                bi.setRGB(j, i, img[i][j]);
            }
        }
        return bi;
    }

    public static void savePNG(BufferedImage img, String name) throws IOException {
        ImageIO.write(img, "png", new File(name + ".png"));
    }

    public static void savePNG(double[][] img, String name) throws IOException {
        int height = img[0].length;
        int width = img.length;
        BufferedImage bi = new BufferedImage(width, height, TYPE_INT_RGB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                bi.setRGB(j, i, (int) img[i][j]);
            }
        }
        ImageIO.write(bi, "png", new File(name + ".png"));
    }
    
    public static void savePNG(int[][] img, String name) throws IOException {
        int height = img[0].length;
        int width = img.length;
        BufferedImage bi = new BufferedImage(width, height, TYPE_INT_RGB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                bi.setRGB(j, i, img[i][j]);
            }
        }
        ImageIO.write(bi, "png", new File(name + ".png"));
    }
}
