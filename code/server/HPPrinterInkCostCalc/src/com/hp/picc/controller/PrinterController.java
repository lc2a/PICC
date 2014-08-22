package com.hp.picc.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hp.picc.model.Printer;

@Controller
@RequestMapping(value = "/printer")
public class PrinterController {
	
	@Autowired
    private JdbcTemplate mysqlJdbcTemplate;
	
	
	@RequestMapping(value = "/query")
	@ResponseBody
	public List<Printer> query(@RequestParam(value = "id", required=false) String id,
			@RequestParam(value = "brand", required=false) String brand,
			@RequestParam(value = "title", required=false) String title) {
		
		List<Printer> printers = new ArrayList<Printer>();
		List<Map<String, Object>> rows = null;
		
		String sql = "SELECT * FROM printer";
		if(id != null && !"".equals(id)) {
			sql += " WHERE id = ?";
			rows = mysqlJdbcTemplate.queryForList(sql, new Object[]{new Integer(id)});
			
		}else if(brand != null && !"".equals(brand)) {
			sql += " WHERE upper(brand) LIKE ?";
			rows = mysqlJdbcTemplate.queryForList(sql, new Object[]{new String("%"+brand.toUpperCase()+"%")});
			
		}else if(title != null && !"".equals(title)) {
			sql += " WHERE upper(title) LIKE ?";
			rows = mysqlJdbcTemplate.queryForList(sql, new Object[]{new String("%"+title.toUpperCase()+"%")});
		} else {
			rows = mysqlJdbcTemplate.queryForList(sql);
		}

        for (Map<String, Object> row : rows) {
            
            Printer printer = new Printer();
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
            
            printers.add(printer);
        }
		
		return printers;
	}

}
