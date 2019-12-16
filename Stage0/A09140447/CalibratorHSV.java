import ij.*;
import java.awt.*;
import ij.plugin.filter.*;
import ij.process.*;
import java.lang.*;
import ij.ImagePlus;
import java.util.*;

public class CalibratorHSV implements PlugInFilter {

    public class HSBHistogram
    {
        public TreeMap<Double, Double> histogramHue;
        public TreeMap<Double, Double> histogramSat;
        public TreeMap<Double, Double> histogramVal;
        public HSBHistogram()
        {
            histogramHue = new TreeMap<Double,Double>();
            histogramSat = new TreeMap<Double,Double>();
            histogramVal = new TreeMap<Double,Double>();
        }
    }

    HSBHistogram mOriginalHistogram = new HSBHistogram();
    HSBHistogram mReferenceHistogram = new HSBHistogram();

    public int setup (String args, ImagePlus im) {
        return DOES_RGB;
    }

    private double[] convertToHSB(Color color) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        return new double[]{(double)hsb[0], (double)hsb[1], (double)hsb[2]};
    }

    public void run (ImageProcessor ip) {
        ImageProcessor referenceImageProcessor = IJ.openImage().getProcessor();
        int referenceImageSize = referenceImageProcessor.getWidth() * referenceImageProcessor.getHeight();
        calculateHistogram(referenceImageProcessor, mReferenceHistogram);
        equalizeChannels(ip, mReferenceHistogram);
    }


    private void equalizeChannels(ImageProcessor ip, HSBHistogram targetHistogram) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        calculateHistogram(ip, mOriginalHistogram);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                double[] hsb = convertToHSB(new Color(ip.getPixel(col,row)));
                float hue = (float) mapValue(hsb[0],mOriginalHistogram.histogramHue,targetHistogram.histogramHue);
                float sat = (float) mapValue(hsb[1],mOriginalHistogram.histogramSat,targetHistogram.histogramSat);
                float br = (float) mapValue(hsb[2],mOriginalHistogram.histogramVal,targetHistogram.histogramVal);
                int rgb = Color.HSBtoRGB(hue, sat, br);
                ip.putPixel(col, row, rgb);
            }
        }
    }

    private double mapValue(double currentValue, TreeMap<Double,Double> originalHistogram, TreeMap<Double,Double> targetHistogram)
    {
        return closestIndexToValue(originalHistogram.get(currentValue), targetHistogram);
    }

  
    private void calculateHistogram(ImageProcessor ip, HSBHistogram targetHistogram) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        int imageSize = width * height;
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                double[] hsv = convertToHSB(new Color(ip.getPixel(col, row)));
                increaseHistogram(targetHistogram.histogramHue, hsv[0]);
                increaseHistogram(targetHistogram.histogramSat, hsv[1]);
                increaseHistogram(targetHistogram.histogramVal, hsv[2]);
            }
        }

        getCumulative(targetHistogram.histogramHue, imageSize);
        getCumulative(targetHistogram.histogramSat, imageSize);
        getCumulative(targetHistogram.histogramVal, imageSize);
    }

    private void increaseHistogram(TreeMap<Double, Double> histogram, double value) {
        if (!histogram.containsKey(value)) {
            histogram.put(value, 0.0);
        }
        histogram.put(value, histogram.get(value) + 1.0);
    }

    private void getCumulative(TreeMap<Double, Double> histogram, int imageSize) {
        double previousValue = 0.0;
        for (double key : histogram.keySet()) {
            histogram.put(key, (histogram.get(key) / imageSize) + previousValue);
            previousValue = histogram.get(key);
        }
    }

    private double closestIndexToValue(double value, TreeMap<Double, Double> histogram) {
        double minKey = 0.0;
        double minDistance = 100000000000d;
        for (double key : histogram.keySet()) {
            double candidate = Math.abs(histogram.get(key)-value);
           if(candidate<minDistance)
           {
               minDistance = candidate;
               minKey = key;
           }
        }
        return minKey;
    }
}
