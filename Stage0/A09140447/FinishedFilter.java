import ij.process.ImageProcessor;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.*;
import java.awt.Color;
import ij.ImagePlus;
public class FinishedFilter implements PlugInFilter {


    float[] mConvolutionMatrix = 
    {0.0076831f,0.00768312f,0.00768312f,0.00768312f,0.00768312f,
    0.00815656f,0.00815656f,0.00815656f,0.00815656f,0.00815656f,
    0.00083208f,0.00083208f,0.00083208f,0.00083208f,0.00083208f,
    0.00815656f,0.00815656f,0.00815656f,0.00815656f,0.00815656f,
    0.0076831f,0.00768312f,0.00768312f,0.00768312f,0.00768312f};


    public double bottom0(int x) {
        return 0.9848 * x - 6.7474;
    }

    public double top0(int x) {
        return -0.0009 * x * x + 1.1917 * x - 4.0146;
    }


    public double bottom1(int x) {
        return -0.0009 * x * x + 1.1917 * x - 4.0146;
    }

    public double top1(int x) {
        return -0.0011 * x * x + 1.2262 * x + 4.0264;
    }

    public double bottom2(int x) {
        return -0.0011 * x * x + 1.2262 * x + 4.0264;
    }

    public double top2(int x) {
        return -0.0013 * x * x + 1.2608 * x + 12.067;
    }
    public double bottom3(int x) {
        return -0.0013 * x * x + 1.2608 * x + 12.067;
    }

    public double top3(int x) {
        return -0.0026 * x * x + 1.5713 * x + 14.8;
    }
        

    public int setup(String args, ImagePlus im) {
        return DOES_RGB;
    }

    public void run(ImageProcessor ip) {
        ImageProcessor ip2 = ip.bin(1);
        
        int width = ip.getWidth(), height = ip.getHeight(), pixel, r, g, b;
        double rb;
        Color color;
        int[][] binaryLayer0 = new int[height][width];
        int[][] binaryLayer1 = new int[height][width];
        int[][] binaryLayer2 = new int[height][width];
        int[][] binaryLayer3 = new int[height][width];
        //ip.convolve(mConvolutionMatrix,5,5);
        ip2.smooth();
        ip2.blurGaussian(4);

        ImageProcessor ipForBL3 = ip2.bin(1);
        for (int row = 0; row < height; row++)
        {
            for (int col = 0; col < width; col++) {
                color = new Color(ip2.getPixel(col, row));
                r = color.getRed();
                g = color.getGreen();
                b = color.getBlue();
                rb = (r + b) / 2.;

                if (b < g && g < r && rb >= bottom0(g) && rb <= top0(g))
                    binaryLayer0[row][col] = 0;
                else
                    binaryLayer0[row][col] = 1; 

                if (b < g && g < r && rb >= bottom1(g) && rb <= top1(g))
                    binaryLayer1[row][col] = 0;
                else
                    binaryLayer1[row][col] = 1; 

                if (b < g && g < r && rb >= bottom2(g) && rb <= top2(g))
                    binaryLayer2[row][col] = 0;
                else
                    binaryLayer2[row][col] = 1; 

                if (b < g && g < r && rb >= bottom3(g) && rb <= top3(g))
                {
                    binaryLayer3[row][col] = 0;
                    ipForBL3.putPixel(col,row,0);
                } 
                else
                {
                    binaryLayer3[row][col] = 1;
                    ipForBL3.putPixel(col,row,16777215);
                }
                     

                
            }
        }

        int moment00 = 0;
        int sumX = 0;
        int sumY = 0;


        int moment00_2 = 0;
        int sumX_2 = 0;
        int sumY_2 = 0;
        for (int row = 0; row < height; row++)
        {
            for (int col = 0; col < width; col++) {
                moment00+=binaryLayer0[row][col];
                sumX += col * binaryLayer0[row][col];
                sumY += row * binaryLayer0[row][col];

                moment00_2+=binaryLayer1[row][col];
                sumX_2 += col * binaryLayer1[row][col];
                sumY_2 += row * binaryLayer1[row][col];
            }
        }

        int centerX = sumX / moment00;
        int centerY = sumY/ moment00;

        int centerX_2 = sumX_2 / moment00_2;
        int centerY_2 = sumY_2/ moment00_2;

        int centerXAverage = (centerX + centerX_2) / 2;
        int centerYAverage = (centerY + centerY_2) / 2;


        RankFilters rf = new RankFilters();
        int neighborHood = (int)(Math.sqrt(moment00)*0.04f);
        rf.rank(ipForBL3,neighborHood,RankFilters.MIN);

        ImageProcessor lipLayer = ip.bin(1);

        for (int row = 0; row < height; row++)
        {
            for (int col = 0; col < width; col++)
            {
                if(ipForBL3.getPixel(col,row)==0 && binaryLayer1[row][col]!=0)
                {
                    lipLayer.putPixel(col,row, 0);
                }
                else
                {
                    lipLayer.putPixel(col,row, 16777215);
                }
            }
        }

        rf.rank(lipLayer,neighborHood*0.3,RankFilters.MEDIAN);
        
        
        int lipMoment00 = 0;
        int lipSumX = 0;
        int lipSumY = 0;
        
        int lowestX=width, lowestY = height;
        int highestX = 0, highestY = 0;

        for (int row = 0; row < height; row++)
        {
            for (int col = 0; col < width; col++)
            {
                
                if(lipLayer.getPixel(col,row)==0)
                {
                     lipMoment00++;
                     lipSumX += col;
                     lipSumY += row;
                     
                     if(lowestX>col)
                        lowestX = col;
                     if(lowestY>row)
                        lowestY = row;
                     if(highestX<col)
                        highestX = col;
                     if(highestY<row)
                        highestY = row;
                }
            }
        }

        int lipCenterX = (int)(lipSumX/(float)lipMoment00);
        int lipCenterY = (int)(lipSumY/(float)lipMoment00);

        int radius = 2*(int)Math.sqrt(lipMoment00/Math.PI);

        ip.drawOval(lipCenterX-radius/2,lipCenterY-radius/2,radius,radius);


        ip.drawRect(lowestX,lowestY,highestX-lowestX,highestY-lowestY);

    }
}