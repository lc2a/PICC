package com.hp.picc.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.hp.picc.model.Printer;
import com.hp.picc.utils.CMYKFileProcessor;

@Controller
@RequestMapping(value="/cmyk")
public class CMYKController implements ServletContextAware {

	@Autowired
    private JdbcTemplate mysqlJdbcTemplate;
	
	private ServletContext servletContext;
    public void setServletContext(ServletContext servletCtx){
       this.servletContext=servletCtx;
    }
    
	@RequestMapping(value = "/")
	public String index() {
		return "redirect:/pages/index.html";
	}

	@RequestMapping(value = "/upload/image", method = RequestMethod.POST)
	@ResponseBody
	public String getImageCalcResult(
			@RequestParam("img") MultipartFile img_file,
			@RequestParam(value = "actual", required=true) String actual_size,
			@RequestParam(value = "paper", required=true) int paper_size,
			@RequestParam(value = "ppi", required=true) int ppi,
			@RequestParam(value = "gray", required=true) String grayScale,
			@RequestParam(value = "printer", required=true) String printer_id) {

		Gson gson = new Gson();
		Map<String, Object> map = new HashMap<String, Object>();
		
		String fileName = null;
    	if (!img_file.isEmpty()) {
            try {
                fileName = img_file.getOriginalFilename();
                
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHHHmmss_SSSSSS");
                String formattedDate = sdf.format(date);
                fileName = formattedDate + "_" + fileName;
                
                String uploadFolder = servletContext.getRealPath("/");
                uploadFolder = uploadFolder.substring(0, uploadFolder.length() - 1);
                int pos = uploadFolder.lastIndexOf("\\");
                uploadFolder = uploadFolder.substring(0, pos);
                
                File imgFile = new File(uploadFolder + "/upload/img/" + fileName);
                
                byte[] bytes = img_file.getBytes();
                BufferedOutputStream buffStream = 
                        new BufferedOutputStream(new FileOutputStream(imgFile));
                buffStream.write(bytes);
                buffStream.close();
                
                Printer printer = null;
                String sql = "SELECT * FROM printer WHERE id = ?";
                List<Map<String, Object>> rows = mysqlJdbcTemplate.queryForList(sql, new Object[]{new Integer(printer_id)});
                for (Map<String, Object> row : rows) {
                	printer = new Printer();
                    printer.setId((Integer)row.get("id"));
                    printer.setBrand((String)row.get("brand"));
                    printer.setType((String)row.get("type"));
                    printer.setTitle((String)row.get("title"));
                    printer.setImage((String)row.get("image"));
                    printer.setPrice((Float)row.get("price"));
                    printer.setUrl((String)row.get("url"));
                    printer.setIccFile((String)row.get("icc_file"));
                    printer.setFunctions((String)row.get("functions"));
                    printer.setPrintSpeedBlack((Float)row.get("print_speed_black"));
                    printer.setPrintSpeedColor((Float)row.get("print_speed_color"));
                    printer.setMaxInputCapacity((Integer)row.get("max_input_capacity"));
                    printer.setMaxMonthlyDutyCycle((Integer)row.get("max_monthly_duty_cycle"));
                    printer.setAutoDuplex((String)row.get("auto_duplex"));
                    printer.setInkCyanPrice((Float)row.get("ink_cyan_price"));
                    printer.setInkCyanYield((Integer)row.get("ink_cyan_yield"));
                    printer.setInkMagentaPrice((Float)row.get("ink_magenta_price"));
                    printer.setInkMagentaYield((Integer)row.get("ink_magenta_yield"));
                    printer.setInkYellowPrice((Float)row.get("ink_yellow_price"));
                    printer.setInkYellowYield((Integer)row.get("ink_yellow_yield"));
                    printer.setInkBlackPrice((Float)row.get("ink_black_price"));
                    printer.setInkBlackYield((Integer)row.get("ink_black_yield"));
                }
        			
                if(printer == null) {
                	map.put("error", "Printer not found ("+printer_id+")");
                	
                } else {
	                CMYKFileProcessor processor = new CMYKFileProcessor(imgFile, uploadFolder + "/upload/icc/" + printer.getIccFile(), 
	                		toBoolean(actual_size), paper_size, ppi, printer, toBoolean(grayScale));
	                
	                map.put("printer", printer);
	                
	                Map<String, Object> processorMap = new HashMap<String, Object>();
	                BigDecimal [] cmyk = processor.getCMYK();
	                processorMap.put("cyan", cmyk[CMYKFileProcessor.C]);
	                processorMap.put("magenta", cmyk[CMYKFileProcessor.M]);
	                processorMap.put("yellow", cmyk[CMYKFileProcessor.Y]);
	                processorMap.put("black", cmyk[CMYKFileProcessor.K]);
	                
	                BigDecimal totalCostPerPage;
	    			if(toBoolean(grayScale))
	    				totalCostPerPage = processor.getInkCost_GrayScale();
	    			else
	    				totalCostPerPage = processor.getInkCost();
	    			
	    			processorMap.put("tcpp", totalCostPerPage);
	                map.put("result", processorMap);
	                
	                Map<String, Object> optionsMap = new HashMap<String, Object>();
	                optionsMap.put("actual_size", actual_size);
	                optionsMap.put("paper_size", paper_size);
	                optionsMap.put("ppi", ppi);
	                optionsMap.put("printer_id", printer_id);
	                optionsMap.put("grayscale", grayScale);
	                map.put("options", optionsMap);
                }
                
            } catch (Exception e) {
            	map.put("error", "Failed to upload "+fileName+": " + e.getMessage());
            }
        } else {
        	map.put("error", "Unable to upload. File is empty.");
        }
    	
		return gson.toJson(map);
	}
	
	public static boolean toBoolean(String s) {
	    return ((s != null) && s.equalsIgnoreCase("yes"));
	}
}
