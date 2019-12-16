import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.plugin.filter.PlugInFilter;
import java.awt.Color;
import java.awt.color.ColorSpace;

public class YCbCrConverter implements PlugInFilter {
    public int setup(String args, ImagePlus im) {
        return DOES_RGB;
    }

    public void run(ImageProcessor ip) {
        int width = ip.getWidth(), height = ip.getHeight();
        Color color;
        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++) {
                color = new Color(ip.getPixel(col, row));
               
                int[] ycbcr = GetColorToYCbCr(color);
                color = GetColorFromYCbCr((ycbcr[0]), (ycbcr[1]), (ycbcr[2]));
                ip.putPixel(col, row, color.getRGB()); // WHITE
            }
    }

    Color GetColorFromYCbCr(int y, int cb, int cr) {
        double Y = (double) y;
        double Cb = (double) cb;
        double Cr = (double) cr;

        int r = (int) (Y + 1.40200 * (Cr - 0x80));
        int g = (int) (Y - 0.34414 * (Cb - 0x80) - 0.71414 * (Cr - 0x80));
        int b = (int) (Y + 1.77200 * (Cb - 0x80));

        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return new Color(r,g,b);
    }

    int[] GetColorToYCbCr(Color color)
    {
        int[] value = new int[3];
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int y = 16 +(((r<<6)+(r<<1)+(g<<7)+g+(b<<4)+b)>>8);
        int cb = 128+((-((r<<5)+(r<<2)+(r<<1))-((g<<6)+(g<<3)+(g<<1))+(b<<7)-(b<<4))>>8);
        int cr = 128+(((r<<7)-(r<<4)-((g<<6)+(g<<5)-(g<<1))-((b<<4)+(b<<1)))>>8);
        value[0] = y;
        value[1] = cb;
        value[2] = cr;
        return value;
    }
}
