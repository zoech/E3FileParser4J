package com.imeee.examples;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.imeee.textFileParser.TextLineFileParser;
import com.imeee.textFileParser.annotation.LField;
import com.imeee.textFileParser.annotation.LLine;

/**
 * @author Eee
 *
 */
@LLine(fieldsCnt=4, fieldSep="\\|")
public class Ebean {
	@LField(order=0,length=50)
	private int id;
	
	@LField(order=1,format="\\d+")
	private String key;
	
	@LField(order=2)
	private Date date;
	
	@LField(order=3)
	private Double amt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public Double getAmt() {
		return amt;
	}

	public void setAmt(Double amt) {
		this.amt = amt;
	}

	public static void main(String[] args) throws Exception {
		parseTest();
	}
	
	
	public static void methodInvokeTest() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method valueOfMethod = Double.class.getMethod("valueOf", String.class);
		
		if(valueOfMethod != null) {
			valueOfMethod.invoke(null, "20.8");
		}
	}
	
	public static void parseTest() throws Exception {
//		String s = "xxx?bbb?cc";
//		String[] x = s.split("?");
//		
//		for(String k : x) {
//			System.out.println(k);
//		}
		
		
		
		TextLineFileParser<Ebean> parser = new TextLineFileParser<Ebean>(Ebean.class, "E:\\tmp\\tmp_20180330\\parsing.txt");
		
		parser.initial();
//		
		if(!parser.valdateWholeFile()) {
			System.out.println("--------");
			System.out.println(parser.getMessage());
		}
		
		List<Ebean> ll = parser.parseWholeFile();
		
		parser.reset();
//		
//		for(Ebean x : ll) {
//			System.out.println(x.getId());
//			System.out.println(x.getKey());
//			System.out.println(x.getDate());
//			System.out.println(x.getAmt());
//		}
		
		//parser.finalizing();
		
		
	
		
		while(parser.hasNext() && parser.validateNext()) {
			Ebean x = parser.next();
			System.out.println(x.getId());
			System.out.println(x.getKey());
			System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(x.getDate()));
			System.out.println(x.getAmt());
		}
	}
	
}
