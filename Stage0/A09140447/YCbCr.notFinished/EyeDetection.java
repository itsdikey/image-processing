import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.plugin.filter.PlugInFilter;
import java.awt.Color;
import java.awt.color.ColorSpace;
import ij.*;
public class EyeDetection implements PlugInFilter {

   
    int[][] mNeighbourhood = {
        {0,0,1,1,1,1,1,1,1,0,0},
        {0,1,1,1,1,1,1,1,1,1,0},
        {1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1},
        {0,1,1,1,1,1,1,1,1,1,0},
        {0,0,1,1,1,1,1,1,1,0,0}
    };

    double[][] mHeight = {
        {0,0,0,0.6248,1.2497,1.8745,1.2497,0.6248,0,0,0},
        {0,0.6248,1.2497,1.8745,2.4994,2.4994,2.4994,1.8745,1.2497,0.6248,0},
        { 0,1.2497,1.8745,2.4994,3.1242,3.1242,3.1242,2.4994,1.8745,1.2497,0},
        { 0.6248,1.8745,2.4994,3.1242,3.7491,3.7491,3.7491,3.1242,2.4994,1.8745,0.6248},
        {1.2497,2.4994,3.1242,3.7491,4.3739,4.3739,4.3739,3.7491,3.1242,2.4994,1.2497},
        { 1.8745,2.4994,3.1242,3.7491,4.3739,4.9987,4.3739,3.7491,3.1242,2.4994,1.8745},
        {1.2497,2.4994,3.1242,3.7491,4.3739,4.3739,4.3739,3.7491,3.1242,2.4994,1.2497},
        { 0.6248,1.8745,2.4994,3.1242,3.7491,3.7491,3.7491,3.1242,2.4994,1.8745,0.6248},
        { 0,1.2497,1.8745,2.4994,3.1242,3.1242,3.1242,2.4994,1.8745,1.2497,0},
        {0,0.6248,1.2497,1.8745,2.4994,2.4994,2.4994,1.8745,1.2497,0.6248,0},
        {0,0,0,0.6248,1.2497,1.8745,1.2497,0.6248,0,0,0},
    };


    public int setup(String args, ImagePlus im) {
        return DOES_RGB;
    }

    public void run(ImageProcessor ip) {
        int width = ip.getWidth(), height = ip.getHeight();
        Color color;
        float[][][] imageAsYCBCR = new float[height][width][3];
        for (int row = 0; row < height; row++)
            for (int col = 0; col < width; col++) {
                color = new Color(ip.getPixel(col, row));
               
                int[] ycbcr = GetColorToYCbCr(color);
                float y = ((float)ycbcr[0]/255);
                float cb = ((float)ycbcr[1]/255);
                float cr = ((float)ycbcr[2]/255);
                
                imageAsYCBCR[row][col][0] = y;
                imageAsYCBCR[row][col][1] = cb;
                imageAsYCBCR[row][col][2] = cr;

              
            }

            for (int row = 0; row < height; row++)
            {
                for (int col = 0; col < width; col++) {

                    float y =  imageAsYCBCR[row][col][0];
                    float cb = imageAsYCBCR[row][col][1];
                    float cr = imageAsYCBCR[row][col][2];
    
                    double eyeMapCoordinate = (cb*cb+(1-cr)*(1-cr)+(cb/cr))/3;
    
                    double dilation = dilate(row,col, imageAsYCBCR, width, height);
                    double erosion = erosion(row,col,imageAsYCBCR, width, height);
                    double eyeMapL = dilation/erosion;
                    Color colorC = new Color((float)eyeMapCoordinate, (float)eyeMapCoordinate, (float)eyeMapCoordinate);
                    Color colorL = new Color((float)eyeMapL, (float)eyeMapL, (float)eyeMapL);
                    int result = colorC.getRGB()&colorL.getRGB();
                    ip.putPixel(col, row, result);
                }
            }
            
            
    }

    double dilate(int row,int col, float[][][] imageAsYCBCR, int width, int height)
    {
        double max = 0;
        for(int i = row-5; i<=row+5; i++)
        {
            for(int j=col-5; j<=col+5; j++)
            {
                if(i<0 || i>=height || j>=width || j<0)
                continue;
                int l = 5 + i - row;
                int k = 5 + j - col;
                if(mNeighbourhood[l][k]==0)
                continue;
                double target = imageAsYCBCR[i][j][0]*255 + mHeight[l][k];
                if(target>max)
                    max = target;
                
            }
        }

        return Math.min(max, 255)/255;
    }


    double erosion(int row,int col, float[][][] imageAsYCBCR, int width, int height)
    {
        double min = 0;
        for(int i = row-5; i<=row+5; i++)
        {
            for(int j=col-5; j<=col+5; j++)
            {
                if(i<0 || i>=height || j>=width || j<0)
                continue;
                int l = 5 + i - row;
                int k = 5 + j - col;
                if(mNeighbourhood[l][k]==0)
                continue;
                double target = imageAsYCBCR[i][j][0]*255 - mHeight[l][k];
                if(target<min)
                min = target;
                
            }
        }

        return Math.max(min, 255)/255;
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
