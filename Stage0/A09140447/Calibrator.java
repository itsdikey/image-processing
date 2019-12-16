import ij.*;
import java.awt.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.*;
import java.awt.Color;
import java.lang.Math;

public class Calibrator implements PlugInFilter {
    ImageProcessor mReferenceImageProcessor;
   

    public int setup (String args, ImagePlus im) {
        return DOES_RGB;
    }

    public void run (ImageProcessor ip) {
        mReferenceImageProcessor = IJ.openImage().getProcessor();
        double[][] referenceCumulativeHistogram = new double[3][256];
        calculateHistogram(mReferenceImageProcessor, referenceCumulativeHistogram);
        equalizeChannels(ip, referenceCumulativeHistogram);
    }

    private void equalizeChannels(ImageProcessor ip, double[][] referenceCumulativeHistogram) {

        
        double[][] originalCumulativeHistogram = new double[3][256];
        calculateHistogram(ip, originalCumulativeHistogram);

        int width = ip.getWidth();
        int height = ip.getHeight();
        int[][] colorMap = new int[3][256];

        // calculate mappings
        for (int i = 0; i < 256; i++) {
            int calibratedRedColor = mapValue(referenceCumulativeHistogram[0], originalCumulativeHistogram[0][i]);
            int calibratedGreenColor = mapValue(referenceCumulativeHistogram[1], originalCumulativeHistogram[1][i]);
            int calibratedBlueColor = mapValue(referenceCumulativeHistogram[2], originalCumulativeHistogram[2][i]);

            colorMap[0][i] = calibratedRedColor;
            colorMap[1][i] = calibratedGreenColor;
            colorMap[2][i] = calibratedBlueColor;
        }

        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                Color color = new Color(ip.getPixel(col, row));
                ip.putPixel(col, row, calibrateColor(color, colorMap).getRGB());
            }
        }
    }

    private Color calibrateColor(Color orgColor, int[][] colorMap)
    {
        return new Color(colorMap[0][orgColor.getRed()],
        colorMap[1][orgColor.getGreen()],
        colorMap[2][orgColor.getBlue()]);
    }

    private int mapValue(double[] referenceHistogram, double value) {
        double minDistance = Math.abs(referenceHistogram[0] - value);
        int minIndex = 0;
       for(int i = 0; i<256; i++)
       {
            double distance = Math.abs(referenceHistogram[i] - value);
            if(distance<minDistance)
            {
                minDistance = distance;
                minIndex = i;
            }
       }
       return minIndex;
    }

    private void calculateHistogram(ImageProcessor ip, double[][] histogram) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        Color color;
        for (int col = 0; col < width; col++) 
        {
            for (int row = 0; row < height; row++) 
            {
                color = new Color(ip.getPixel(col, row));
                histogram[0][color.getRed()] ++;
                histogram[1][color.getGreen()] ++;
                histogram[2][color.getBlue()] ++;
            }
        }

        
        int area = width * height;
        histogram[0][0] /= area;
        histogram[1][0] /= area;
        histogram[2][0] /= area;
        for (int i = 1; i < 256; i++) {
                histogram[0][i] = (histogram[0][i] / area) + histogram[0][i-1];
                histogram[1][i] = (histogram[1][i] / area) + histogram[1][i-1];
                histogram[2][i] = (histogram[2][i] / area) + histogram[2][i-1];
        }
    }

}