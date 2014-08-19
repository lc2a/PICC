package com.hp.hpic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;

import javax.imageio.ImageIO;

public class Test {
	
	// Price refer from  amazon -- for fairness
	
	// HP 564 XL on HP Photo Smart 7250
	public static Yield generatePSYield(){
		Yield yield = new Yield();
		// set Yield Pages - http://www.hp.com/pageyield/us/en/PSB8500/index.html
		yield.setBlackYieldPage(800);
		yield.setCyanYieldPage(750);
		yield.setMagentaYieldPage(750);
		yield.setYellowYieldPage(750);
		
		// set Yield Prices
		yield.setBlackInkPrice((float) 28.85); // HP 564XL CN684WN#140 Ink Cartridge 
		yield.setCyanInkPrice((float) 15.3); // HP 564XL High Capacity XL Cartridges Combo Pack (1 Cyan, 1 Magenta, 1 Yellow) 49.5/3
		yield.setMagentaInkPrice((float) 15.3);
		yield.setYellowInkPrice((float) 15.3);
		
		return yield;
	}
	
	// Canon PIXMA MG8220 on CLI-25BK and CLI-226 (Color)
	public static Yield generateMGYield(){
		Yield yield = new Yield();
				
		// set Yield Pages - http://www.usa.canon.com/CUSA/assets/app/pdf/PrintYield/PrintYield-MG8220.pdf
		yield.setBlackYieldPage(311);
		yield.setCyanYieldPage(478);
		yield.setMagentaYieldPage(447);
		yield.setYellowYieldPage(466);
		
		// set Yield Prices
		yield.setBlackInkPrice((float) 15.99); //Canon 4530B001 PGI-225
		yield.setCyanInkPrice((float) 13.33); // Canon CLI-226 3 Color Value Pack Ink, Genuine Ink 39.99/3
		yield.setMagentaInkPrice((float) 13.33);
		yield.setYellowInkPrice((float) 13.33);
		
		return yield;
	}
	
	// Epson WorkForce WF-3520 on Epson 126 (High-capacity Black, Cyan, Magenta, Yellow)
	public static Yield generateWFYield(){
		Yield yield = new Yield();
		
		// set Yield Pages
		yield.setBlackYieldPage(385);
		yield.setCyanYieldPage(470);
		yield.setMagentaYieldPage(470);
		yield.setYellowYieldPage(470);
		
		// set Yield Prices
		yield.setBlackInkPrice((float) 18.99); // Epson DURABrite T126120 Ultra 126 High-capacity Inkjet Cartridge
		yield.setCyanInkPrice((float) 15.99); // Epson DURABrite T126520 Ultra 126 High-capacity Inkjet Cartridge Color Multipack -Cyan/Magenta/Yellow - 47.99/3
		yield.setMagentaInkPrice((float) 15.99);
		yield.setYellowInkPrice((float) 15.99);
		
		return yield;
	}
	
	public static void main(String[] args) throws IOException {
		try{
			
			//Create Test Data: Using Default ICC, Print Actual Size, Paper Size is A4, PPI is 200, Color
			File imgFile = new File("images/img1.jpg");
			boolean actual_size = !CMYKFileProcessor.ACTUAL_SIZE;
			int paper_size = CMYKFileProcessor.A4;
			int ppi = 300;
			boolean grayScale = !CMYKFileProcessor.GRAY_SCALE;
			
			String iccPath = "icc/hp/x520/RR UltraPro Satin 3.0 HP 7520.icm";
			CMYKFileProcessor processor = new CMYKFileProcessor(imgFile, iccPath, actual_size, paper_size, ppi, generatePSYield(), grayScale);
			
			// Show Printing Specification
			System.out.println("Image Specification: " + 
					"Image Name: ["+imgFile.getName()+"], " +
					"ICC Path: ["+iccPath+"], " +
					"is Actual Size: ["+actual_size+"], " +
					"Paper Size: ["+paper_size+"], " +
					"PPI: ["+ppi+"], " +
					"is Gray Scale: ["+grayScale+"]");
			
			// Show CMYK Value
			BigDecimal [] cmyk = processor.getCMYK();

			if(cmyk != null && cmyk.length > 0){
				System.out.println("CMYK Values:\nC :" + cmyk[CMYKFileProcessor.C] + "%, M :" 
						+ cmyk[CMYKFileProcessor.M] + "%, Y :" 
						+ cmyk[CMYKFileProcessor.Y] + "%, K :" 
						+ cmyk[CMYKFileProcessor.K] + "%");
			}
			
			// Show Total Cost
			BigDecimal totalCostPerPage;
			if(grayScale)
				totalCostPerPage = processor.getInkCost_GrayScale();
			else
				totalCostPerPage = processor.getInkCost();
			
			System.out.println("Total Ink Cost Per Page: $" + totalCostPerPage);


			
			iccPath = "icc/epson/workforce/RR UltraPro Satin 3.0 Ep WF Series.icm";
			processor = new CMYKFileProcessor(imgFile, iccPath, actual_size, paper_size, ppi, generateWFYield(), grayScale);
			
			// Show Printing Specification
			System.out.println("Image Specification: " + 
					"Image Name: ["+imgFile.getName()+"], " +
					"ICC Path: ["+iccPath+"], " +
					"is Actual Size: ["+actual_size+"], " +
					"Paper Size: ["+paper_size+"], " +
					"PPI: ["+ppi+"], " +
					"is Gray Scale: ["+grayScale+"]");
			
			// Show CMYK Value
			cmyk = processor.getCMYK();

			if(cmyk != null && cmyk.length > 0){
				System.out.println("CMYK Values:\nC :" + cmyk[CMYKFileProcessor.C] + "%, M :" 
						+ cmyk[CMYKFileProcessor.M] + "%, Y :" 
						+ cmyk[CMYKFileProcessor.Y] + "%, K :" 
						+ cmyk[CMYKFileProcessor.K] + "%");
			}
			
			// Show Total Cost
			if(grayScale)
				totalCostPerPage = processor.getInkCost_GrayScale();
			else
				totalCostPerPage = processor.getInkCost();
			
			System.out.println("Total Ink Cost Per Page: $" + totalCostPerPage);
			
			iccPath = "icc/canon/mgX/RR UltraPro Satin 3.0 Can iP4920.icm";
			processor = new CMYKFileProcessor(imgFile, iccPath, actual_size, paper_size, ppi, generateMGYield(), grayScale);
			
			// Show Printing Specification
			System.out.println("Image Specification: " + 
					"Image Name: ["+imgFile.getName()+"], " +
					"ICC Path: ["+iccPath+"], " +
					"is Actual Size: ["+actual_size+"], " +
					"Paper Size: ["+paper_size+"], " +
					"PPI: ["+ppi+"], " +
					"is Gray Scale: ["+grayScale+"]");
			
			// Show CMYK Value
			cmyk = processor.getCMYK();

			if(cmyk != null && cmyk.length > 0){
				System.out.println("CMYK Values:\nC :" + cmyk[CMYKFileProcessor.C] + "%, M :" 
						+ cmyk[CMYKFileProcessor.M] + "%, Y :" 
						+ cmyk[CMYKFileProcessor.Y] + "%, K :" 
						+ cmyk[CMYKFileProcessor.K] + "%");
			}
			
			// Show Total Cost
			if(grayScale)
				totalCostPerPage = processor.getInkCost_GrayScale();
			else
				totalCostPerPage = processor.getInkCost();
			
			System.out.println("Total Ink Cost Per Page: $" + totalCostPerPage);			

		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	private static void createFile(BufferedImage image, File imgFile, String newFileNames){
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, getFileExtention(imgFile.getName()), baos);
			String outputPath = imgFile.getParent();
			if(!outputPath.isEmpty()){
				outputPath = outputPath + "/" + newFileNames + "." + getFileExtention(imgFile.getName());
			}
		    OutputStream os = new FileOutputStream(outputPath);
		    os.write(baos.toByteArray());
		    os.flush();
	        os.close();
		    baos.flush();
		    baos.close();
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	private static String getFileExtention(String fileName){
		String extension = "";

		int i = fileName.lastIndexOf('.');
		int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (i > p) {
		    extension = fileName.substring(i+1);
		}
		return extension;
	}
}