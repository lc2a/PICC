package com.hp.picc.utils;

import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import com.hp.picc.model.Printer;


public final class CMYKFileProcessor {
	
	public static final int C = 0;
	public static final int M = 1;
	public static final int Y = 2;
	public static final int K = 3;
	public static final boolean ACTUAL_SIZE = true;
	public static final boolean GRAY_SCALE = true;
	public static final int DEFAULT = -1;
	public static final int A0 = 0;
	public static final int A1 = 1;
	public static final int A2 = 2;
	public static final int A3 = 3;
	public static final int A4 = 4;
	public static final int A5 = 5;
	public static final int A6 = 6;
	public static final int A7 = 7;
	public static final int A8 = 8;
	public static final int A9 = 9;
	public static final int A10 = 10;
	
	private	static final int WIDTH = 0;
	private static final int HEIGHT = 1;
	private static final String DEFAULT_ICC_FILE = "icc/adobe/CMYK/WebCoatedFOGRA28.icc";
	private static final float DEFAULT_PPI = 96f; // User Window's standard
	private static final float DEFAULT_WI = 8.3f; // A4 Width in Inches
	private static final float DEFAULT_HI = 11.7f; // A4 Height in Inches
	private static final int DEFAULT_COVERAGE_PER_COLOR = 5;
	
	// Paper Size
	private static final float [][] PAPER_SIZE ={
		new float [] {(float)33.1, (float)46.8}, //A0
		new float [] {(float)23.4, (float)33.1}, //A1
		new float [] {(float)16.5, (float)23.4}, //A2
		new float [] {(float)11.7, (float)16.5}, //A3
		new float [] {(float)8.3, (float)11.7}, //A4
		new float [] {(float)5.8, (float)8.3}, //A5
		new float [] {(float)4.1, (float)5.8}, //A6
		new float [] {(float)2.9, (float)4.1}, //A7
		new float [] {(float)2.0, (float)2.9}, //A8
		new float [] {(float)1.5, (float)2.0}, //A9
		new float [] {(float)1.0, (float)1.5}, //A10
	};
			
		
	private BufferedImage image = null;
	private BufferedImage filteredImage = null;
	private BigDecimal[] CMYK;
	private BigDecimal PPI;
	private int pixels;
	private BigDecimal totalInkCostPerA4;
	private BigDecimal totalInkCostPerA4_grayScale;
	
	private BigDecimal inkCost = new BigDecimal(0);
	private BigDecimal inkCost_GrayScale = new BigDecimal(0);

	
	/*
	 * Constructor
	 */
	public CMYKFileProcessor(File imgFile, String iccPath, boolean actual_size, int paper_size, int ppi, Printer yield, boolean grayScale) throws Exception{
		try{
			
			// 1) Register Image File
			if(imgFile != null)
				registerImageFile(imgFile);
			
			// 2) Register ICC
			if(iccPath == null || iccPath.isEmpty())
				iccPath = DEFAULT_ICC_FILE;
			
			filteredImage = addSpecificICCProfile(image, iccPath);

			// 3) Calculate CMYK Value
			CMYK = getCMYKPercentageOfImage();			
			
			// 4) Calculate Print PPI based on A4
			PPI = calculatePPIForPrint(image.getWidth(), image.getHeight(), DEFAULT_WI, DEFAULT_HI);
			//System.out.println(PPI);
			// 5) Calculate Total Ink Cost for A4
			calculateTotalInkCostPerA4(yield);
			
			// 6) Calculate Total Ink Cost based on Params!			
			calculateActualInkCostPerPage(ppi, paper_size, actual_size, grayScale);

			
			
		}catch(Exception ex){
			ex.printStackTrace();
			throw ex;
		}
	}
	
	private void calculateActualInkCostPerPage(int ppi, int paper_size, boolean actual_size, boolean grayScale){
		float currentInkCost = 0.0f;
		if(actual_size){				
			ppi = (int) DEFAULT_PPI;
			if(grayScale)
				currentInkCost = totalInkCostPerA4_grayScale.floatValue() * ((float)ppi/PPI.floatValue());
			else
				currentInkCost = totalInkCostPerA4.floatValue() * ((float)ppi/PPI.floatValue()); // Everything Default
		}else{
			float currentW = PAPER_SIZE[paper_size][WIDTH];
			float currentH = PAPER_SIZE[paper_size][HEIGHT];
			if(grayScale)
				currentInkCost = totalInkCostPerA4_grayScale.floatValue() * (currentW/DEFAULT_WI) * (currentH/DEFAULT_HI) * ((float)ppi/PPI.floatValue());
			else
				currentInkCost = totalInkCostPerA4.floatValue() * (currentW/DEFAULT_WI) * (currentH/DEFAULT_HI) * ((float)ppi/PPI.floatValue());
		}
				
		BigDecimal cic = new BigDecimal(currentInkCost);	
		if(grayScale)
			inkCost_GrayScale = cic.setScale(2, BigDecimal.ROUND_HALF_UP);  
		else
			inkCost = cic.setScale(2, BigDecimal.ROUND_HALF_UP);  
		//System.out.println("Final Ink Cost will be $" + inkCost);
	}
	
	private void calculateTotalInkCostPerA4(Printer yield){
		float cyanTimes = CMYK[C].floatValue()/DEFAULT_COVERAGE_PER_COLOR;
		float cyanPages = yield.getInkCyanYield()/cyanTimes;
		float cyanInkCost = yield.getInkCyanPrice()/cyanPages;
		//System.out.println("Cyan Ink Cost Per A4: " + cyanInkCost);
		float magentaTimes = CMYK[M].floatValue()/DEFAULT_COVERAGE_PER_COLOR;
		float magentaPages = yield.getInkMagentaYield()/magentaTimes;
		float magentaInkCost = yield.getInkMagentaPrice()/magentaPages;
		//System.out.println("Magenta Ink Cost Per A4: " + magentaInkCost);
		float yellowTimes = CMYK[Y].floatValue()/DEFAULT_COVERAGE_PER_COLOR;
		float yellowPages = yield.getInkYellowYield()/yellowTimes;
		float yellowInkCost = yield.getInkYellowPrice()/yellowPages;
		//System.out.println("Yellow Ink Cost Per A4: " + yellowInkCost);
		float blackTimes = CMYK[K].floatValue()/DEFAULT_COVERAGE_PER_COLOR;
		float blackPages = yield.getInkBlackYield()/blackTimes;
		float blackInkCost = yield.getInkBlackPrice()/blackPages;
		BigDecimal grayBD = new BigDecimal(Float.toString(blackInkCost));
		grayBD = grayBD.setScale(2, BigDecimal.ROUND_HALF_UP);  
		totalInkCostPerA4_grayScale = grayBD;
		//System.out.println("Total Ink Cost Per A4 for gray scale: $" + totalInkCostPerA4_grayScale);
		//System.out.println("Black Ink Cost Per A4: " + blackInkCost);
		
		float totalCostPerA4Page = cyanInkCost+magentaInkCost+yellowInkCost+blackInkCost;
		BigDecimal bd = new BigDecimal(Float.toString(totalCostPerA4Page));
		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);  
		totalInkCostPerA4 = bd;
		//System.out.println("Total Ink Cost Per A4: $" + totalInkCostPerA4);		
	}
	
	private BigDecimal calculatePPIForPrint(int pX, int pY, float wI, float hI){
		double x = Math.pow(pX,2);
		double y = Math.pow(pY,2);
		double pixelD = Math.sqrt(x+y);
		//System.out.println(pixelD);
		
		double xI = Math.pow(wI,2);
		double yI = Math.pow(hI,2);
		double paperD = Math.sqrt(xI+yI);
		//System.out.println(paperD);
		
		double ppi = pixelD/paperD;
		
		BigDecimal bd = new BigDecimal(Double.toString(ppi));
		bd = bd.setScale(0, BigDecimal.ROUND_HALF_UP);  
		return bd;
	}
	
	private BigDecimal getPercentage(float d) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);       
        return bd;
    }
	
	private void registerImageFile(File imgFile){
		try{
			ImageInputStream iis = new FileImageInputStream(imgFile);
			
			for (Iterator<ImageReader> i = ImageIO.getImageReaders(iis); 
			        image == null && i.hasNext(); ) {
			        ImageReader r = i.next();
			        try {
			            r.setInput(iis);
			            image = r.read(0);
			        } catch (IOException e) {}
			 }
		} catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private BufferedImage addSpecificICCProfile(BufferedImage image, String ICCPath){
		BufferedImage filteredImage = null;
		try{
			ICC_Profile icc_profile = ICC_Profile.getInstance(ICCPath);
			ColorConvertOp cco = new ColorConvertOp(image.getColorModel().getColorSpace(), new ICC_ColorSpace(icc_profile), null);			
			filteredImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());		    
			cco.filter(image, filteredImage);	
		}catch (Exception ex){
			ex.printStackTrace();
		}
	
		return filteredImage;		
	}
	
	private BigDecimal[] getCMYKPercentageOfImage(){
	    Raster imgRaster = filteredImage.getRaster();
	    int pixelCount = 0;
	    float finalC=0.0f, finalM=0.0f, finalY=0.0f, finalK=0.0f;
	    for(int x = imgRaster.getMinX(); x < imgRaster.getWidth(); ++x){
	    	for(int y = imgRaster.getMinY(); y < imgRaster.getHeight(); ++y){
	    		float[] p = imgRaster.getPixel(x, y, (float[])null);
	    		float[] cmyk = getCMYKValue(p);
	    		finalC += cmyk[0];
	    		finalM += cmyk[1];
	    		finalY += cmyk[2];
	    		finalK += cmyk[3];
	    		pixelCount += 1;		    		
	    	}
	    }	    	  
	    //System.out.println("Total Pixel Count: " + pixelCount);
	    pixels = pixelCount;
	    //System.out.println("Total C :" + finalC + ", Total M :" + finalM + ", Total Y :" + finalY + ", Total K :" + finalK);
	    float percentC,percentM,percentY,percentK;
	    percentC = finalC/pixelCount*100;
	    percentM = finalM/pixelCount*100;
	    percentY = finalY/pixelCount*100;
	    percentK = finalK/pixelCount*100;
	    // System.out.println("C% :" + percentC + ", M% :" + percentM + ", Y% :" + percentY + ", K% :" + percentK);
	    //CMYK = new BigDecimal[]{getPercentage(percentC),getPercentage(percentM),getPercentage(percentY),getPercentage(percentK)};
	    return new BigDecimal[]{getPercentage(percentC),getPercentage(percentM),getPercentage(percentY),getPercentage(percentK)};
	}
	
	private float[] getCMYKValue(float[] p){
		float R,G,B;
    	//System.out.println("R = " + p[0] + ", G = " + p[1] + ", B = " + p[2]);
    	R = p[0]/255f;
    	G = p[1]/255f;
    	B = p[2]/255f;
    	float C, M, Y, K;
    	K = 1-Math.max(Math.max(R, G), B);
    	//System.out.println("K = " + K);
    	C = (1-R-K)/(1-K);
    	//System.out.println("C = " + C);
    	M = (1-G-K)/(1-K);
    	//System.out.println("M = " + M);
    	Y = (1-B-K)/(1-K);
    	//System.out.println("Y = " + Y);
    	return new float[]{C, M, Y, K};
	}
	
	public BigDecimal[] getCMYK() {
		return CMYK;
	}
	
	public int getPixels() {
		return pixels;
	}

	public BigDecimal getInkCost() {
		return inkCost;
	}

	public BigDecimal getInkCost_GrayScale() {
		return inkCost_GrayScale;
	}
	
}
