package com.imeee.textFileParser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import com.imeee.textFileParser.annotation.LField;
import com.imeee.textFileParser.annotation.LLine;

/**
 * 文本解释类，将一个文本按照类定义的规则转换为Bean对象
 * 
 * @author Eee
 *
 * @param <T>
 * T类型声明中必须用 LLine 标注，并且相关成员用LField标注起来。
 */
public class TextLineFileParser<T> {
	
	/**
	 * 目标文件的编码 
	 */
	private String charSet;
	
	private BufferedReader bufferReader;
	
	private List<FieldHelper> fieldHelpers;
	
	/**
	 * 文件中每条记录解释后含有的域数，用以检查文本记录合法性
	 */
	private int fieldsCnt;
	
	/**
	 * 每条记录中，每个域的分割符，使用正则表达
	 */
	private String fieldSep;
	
	/**
	 * 解释的目标文本路径
	 */
	private String filePath;
	
	private String bufferedStr;
	
	private Class<T> Tclass;
	
	
	/**
	 * 当前解释的行数
	 */
	private int curLine;
	
	/**
	 * 某个域默认的最大长度，如果设置大于0,则可以覆盖LField注解中的定义
	 */
	private int defaultFieldLength = -1;
	
	
	/**
	 * 记录上次操作的错误信息
	 */
	private String message = "";
	
	
	public TextLineFileParser(Class<T> clz) throws Exception {
		this(clz, "");
	}
	
	
	
	public TextLineFileParser(Class<T> clz, String path) throws Exception {
		//super(path);
		this.filePath = path;
		
		
		// 获取实体类 T 的注解内容
		this.Tclass = clz;
		LLine lLine = clz.getAnnotation(LLine.class);
		if(lLine == null){
			throw new Exception("class " + this.Tclass.getName() + " is not compatible!");
		}
		
		this.fieldSep = lLine.fieldSep();
		this.fieldsCnt = lLine.fieldsCnt();
		
		if(this.fieldsCnt < 0) {
			throw new Exception("LLine annotation for class " + this.Tclass.getName() + " fieldsCnt must greater than 0!");
		}
		
		// 获取 T 实体类成员注解的信息
		this.initFieldHelper();
		
	}
	
	
	/**
	 * 将文本内容解释为一个对象列表。
	 * 
	 * @return
	 * 解释文件得到的对象列表，此方法会重置本解释器
	 * 
	 * @throws Exception
	 */
	public List<T> parseWholeFile() throws Exception{
		List<T> res = new ArrayList<T>();
		
		if(!this.valdateWholeFile()){
			throw new Exception("file format is not correct");
		}
		
		while(this.hasNext()){
			if(!this.validateNext()){
				throw new Exception("file record invalid");
			}
			T t = this.next();
			if(t == null){
				throw new Exception("parsing error");
			}
			res.add(t);
		}
		
		
		return res;
	}

	/**
	 * 检查文件中是否还有下一条记录（不检查下条记录的文本是否为有效的记录)
	 * 
	 * @return
	 */
	public boolean hasNext() {
		// TODO Auto-generated method stub
		if(this.bufferedStr != null && !"".equals(this.bufferedStr)) {
			return true;
		}
		
		try {
			this.bufferedStr = this.bufferReader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(this.bufferedStr != null && !"".equals(this.bufferedStr)) {
			this.curLine ++;
			return true;
		}
		
		return false;
	}
	

	/**
	 * 获取文本中下一条记录
	 * 
	 * @return 
	 * 文本中下一条本文所解释得到的T对象
	 * 
	 * @throws Exception
	 * 没有下一条内容，或者下条记录文本不合法，则抛出异常
	 */
	public T next() throws Exception {
		// TODO Auto-generated method stub
		
		T resObj = this.Tclass.newInstance();
		
		return this.nextAs(resObj);
	}
	
	
	/**
	 * 将文本中下一条记录解释并将对应字段填入参数 resObj中。
	 * 为了与DAO框架兼容，可以用DAO框架新建一个对象后，将对象传入此方法进行调用，则返回的对象可以使用DAO存储到数据库中
	 * 
	 * @param resObj
	 * 传入的非空参数
	 * 
	 * @return
	 * @throws Exception
	 * 没有下一条内容，或者下条记录文本不合法，或者传入的参数resObj为null，则抛出异常
	 */
	public T nextAs(T resObj) throws Exception {
		if(resObj == null) {
			throw new java.lang.NullPointerException();
		}
		
		if(!this.hasNext()) {
			throw new Exception("end of file!");
		}
		
		String[] strTmp = this.bufferedStr.split(this.fieldSep);
		
		for(FieldHelper fh : this.fieldHelpers) {
			LField lF = fh.getMeta();
			int fIndex = lF.order();
			String targetStr = strTmp[fIndex].trim();
			
			Field fieldTmp = fh.getField();
			String fmt = lF.format();
			fieldTmp.setAccessible(true);
			if(String.class == fh.getType()) {
				fieldTmp.set(resObj, targetStr);
			} else if(int.class == fh.getType()) {
				int x = Integer.valueOf(targetStr);
				fieldTmp.set(resObj, x);
			} else if(long.class == fh.getType()) {
				long x = Long.valueOf(targetStr);
				fieldTmp.set(resObj, x);
			} else if(double.class == fh.getType()) {
				double x = Double.valueOf(targetStr);
				fieldTmp.set(resObj, x);
			} else if(float.class == fh.getType()) {
				float x = Float.valueOf(targetStr);
				fieldTmp.set(resObj, x);
			} else if(boolean.class == fh.getType()) {
				boolean x = Boolean.valueOf(targetStr);
				fieldTmp.set(resObj, x);
			} else if(Date.class == fh.getType()){
				if(fmt == null || "".equals(fmt)) {
					fmt = "yyyyMMdd";
				}
				
				SimpleDateFormat format = new SimpleDateFormat(fmt);
				Date x = format.parse(targetStr);
				fieldTmp.set(resObj, x);
				
			} else {
				
				Method valueOfMethod = fh.getType().getMethod("valueOf", String.class);
				
				if(valueOfMethod == null) {
					throw new Exception("unsupport field type : " + fh.getType().getName());
				}

				
				Object x = valueOfMethod.invoke(null, targetStr);
				fieldTmp.set(resObj, x);
				
			}
			
		}
		
		this.bufferedStr = null;
		
		return resObj;
	}

	
	/**
	 * 检查整个文件的合法性, 会重置至文件开头
	 * @return
	 * @throws Exception 
	 */
	public boolean valdateWholeFile() throws Exception {
		// TODO Auto-generated method stub
		this.reset();
		
		while(this.hasNext()) {
			if(!this.validateNext()) {
				return false;
			}
			this.ignoreNext();
		}
		
		this.reset();
		return true;
	}
	
	
	/**
	 * 重置此解释器，即回到文件头重新解释
	 * @throws Exception
	 */
	public void reset()  throws Exception {
		if(this.bufferReader != null) {
			try {
				this.bufferReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.bufferReader = null;
		}
		this.initializeInput();
		this.curLine = 0;
		this.bufferedStr = null;
	}

	
	/**
	 * 检查下一条内容的合法性，包括域数、T类型定义合法性和用户自定义的合法性（用户通过标注LField中的format来定义）
	 * @return
	 * 
	 * @throws Exception
	 * 如果没有下一条内容则抛出异常
	 */
	public boolean validateNext() throws Exception {
		// TODO Auto-generated method stub
		if(!this.hasNext()) {
			throw new Exception("end of file!");
		}
		
		String[] strTmp = this.bufferedStr.split(this.fieldSep);
		
		
		// 检查域数是否一样
		if(this.fieldsCnt != strTmp.length) {
			String msg = "fieldsCnt not correct. (file compare to declare, " 
					+ strTmp.length + " : " + this.fieldsCnt
					+ ")";
			System.out.println(msg);
			this.setMessage(msg);
			return false;
		}
		
		// 检查类型定义约束与字符串是否匹配
		if(!this.validateNextTypeConstraint()) {
			return false;
		}
//		for(FieldHelper fh : this.fieldHelpers) {
//			LField lF = fh.getMeta();
//			int fIndex = lF.order();
//			String targetStr = strTmp[fIndex].trim();
//			
//			if(!this.validateTypeConstraint(fh.getType(), targetStr)) {
//				String msg = "field \"" + fh.getName() + "\": type constraint validation failed";
//				this.setMessage(msg);
//				System.out.println(msg);
//				return false;
//			}
//		}
		
		
		// 检查用户自定义format与字符串是否匹配
		if(!this.validateNextUserField()) {
			return false;
		}
//		for(FieldHelper fh : this.fieldHelpers) {
//			LField lF = fh.getMeta();
//			int fIndex = lF.order();
//			String targetStr = strTmp[fIndex].trim();
//			
//			if(!validateUserLField(fh, targetStr)) {
//				String msg = "field \"" + fh.getName() + "\": user defined constraint validation failed";
//				this.setMessage(msg);
//				System.out.println(msg);
//				return false;
//			}
//			
//			
//		} // end of +++++ for(FieldHelper fh : this.fieldHelpers) { +++++
		
		this.setMessage("ok");
		return true;
	}
	
	
	public boolean validateNextTypeConstraint() throws Exception {
		
		if(!this.hasNext()) {
			throw new Exception("end of file!");
		}
		
		String[] strTmp = this.bufferedStr.split(this.fieldSep);
		for(FieldHelper fh : this.fieldHelpers) {
			LField lF = fh.getMeta();
			int fIndex = lF.order();
			String targetStr = strTmp[fIndex].trim();
			
			if(!this.validateTypeConstraint(fh.getType(), targetStr)) {
				String msg = "field \"" + fh.getName() + "\": type constraint validation failed";
				this.setMessage(msg);
				return false;
			}
		}
		
		return true;
	}
	
	
	
	/**
	 * 
	 * 检查基本的类型约束与某个字符串是否相匹配，即该字符串是否能正确定义 clz 类型的值
	 * 
	 * @param clz
	 * @param targetStr
	 * @return
	 * @throws Exception
	 * 
	 */
	private boolean validateTypeConstraint(Class<?> clz, String targetStr) throws Exception {
		
		if(clz == null) {
			throw new java.lang.NullPointerException("");
		}
		
		if(targetStr == null) {
			return false;
		}
		
		if(String.class == clz) {
			return true;
		} else if(int.class == clz) {
			
		} else if(long.class == clz) {
			
		} else if(double.class == clz) {
			
		} else if(float.class == clz) {
			
		} else if(boolean.class == clz) {
			
		} else if(Date.class == clz){
			return true;
		} else {
			
			Method valueOfMethod = clz.getMethod("valueOf", String.class);
			
			if(valueOfMethod == null) {
				throw new Exception("unsupport field type : " + clz.getName());
			}
			
			try {
				valueOfMethod.invoke(null, targetStr);
			} catch (Exception e) {
				e.printStackTrace();
				//System.out.println("field format not match");
				return false;
			}
			
		}
		
		return true;
	}
	
	
	
	/**
	 * 忽略下一个记录
	 */
	public void ignoreNext() throws Exception {
		
		if(!this.hasNext()) {
			throw new Exception("end of file!");
		}
		
		this.bufferedStr = null;
	}
	
	public boolean validateNextUserField() throws Exception {
		// 检查用户自定义format与字符串是否匹配
		
		if(!this.hasNext()) {
			throw new Exception("end of file!");
		}
		
		String[] strTmp = this.bufferedStr.split(this.fieldSep);
		
		for(FieldHelper fh : this.fieldHelpers) {
			LField lF = fh.getMeta();
			int fIndex = lF.order();
			String targetStr = strTmp[fIndex].trim();
			
			if(!validateUserLField(fh, targetStr)) {
				String msg = "field \"" + fh.getName() + "\": user defined constraint validation failed";
				this.setMessage(msg);
				return false;
			}
			
			
		}
		
		return true;
	}
	
	private  boolean validateUserLField(FieldHelper fh, String targetStr) {
		
		LField lF = fh.getMeta();
		
		if("".equals(targetStr)) {
			
			// 该域不能为空
			if(!lF.nullable()) {
				System.out.println("empty field");
				return false;
			}
		} else {
			
			// 检查长度
			int maxLen = this.defaultFieldLength > 0 ? this.defaultFieldLength : lF.length();
			if(maxLen > 0 && targetStr.length() > maxLen) {
				return false;
			}
			
			// 检查字段文本格式合法性
			String fmt = lF.format();
			
			if(fmt == null || "".equals(fmt)) {
				return true;
				
			} else {
				
				if(Date.class == fh.getType()){
					if(fmt == null || "".equals(fmt)) {
						fmt = "yyyyMMdd";
					}
					
					if(!this.validateDate(fmt, targetStr)) {
						return false;
					}
					
				} else {
					
					if(!fmt.startsWith("^")) {
						fmt = "^" + fmt;
					}
					
					if(!fmt.endsWith("$")) {
						fmt = fmt + "$";
					}
					
					if(!Pattern.matches(fmt, targetStr)) {
						//System.out.println("field format not match");
						return false;
					}
					
				}
			
			} // end of else of +++++ if(fmt == null || "".equals(fmt)) { ++++
			
		} // end of else of +++++ if("".equals(targetStr)) { +++++
		
		return true;
	}
	
	
	public void initial() throws Exception{
		this.curLine = 0;
		this.initFieldHelper();
		this.initializeInput();
	}
	
	public void finalizing() throws IOException {
		if(this.bufferReader != null) {
			try {
				this.bufferReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.bufferReader = null;
		}
		
		this.curLine = 0;
		this.bufferReader.close();
		this.bufferReader = null;
		this.fieldHelpers = null;
	}
	
	protected void finalize() {
		try {
			this.finalizing();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 初始化输入BufferedWritter
	 * @return
	 * @throws Exception
	 */
	public boolean initializeInput() throws Exception {
		// TODO Auto-generated method stub
		if(this.bufferReader == null){
			if(this.filePath == null){
				throw new Exception("no input source!");
			}
			
			FileInputStream fi = new FileInputStream(this.filePath);
			InputStreamReader isr = new InputStreamReader(fi, this.charSet == null ? "UTF-8" : this.charSet);
			this.bufferReader = new BufferedReader(isr);
			
			if(this.bufferReader == null){
				throw new Exception("initialize the bufferReader error");
			}
		}
		return true;
	}
	
	
	
	protected void initFieldHelper() throws Exception{
		if(this.fieldHelpers != null){
			return;
		}
		
		this.fieldHelpers = new ArrayList<FieldHelper>();
		
		Field[] fls = this.Tclass.getDeclaredFields();
		
		for(Field f : fls){
			LField lField = f.getAnnotation(LField.class);
			
			if(lField != null){
				if(lField.order() < 0 || lField.order() > this.fieldsCnt) {
					throw new Exception(this.Tclass.getName() + " LField order() greater than LLine fieldsCnt");
				}
				this.fieldHelpers.add(new FieldHelper(lField, f));
			}
			
		}
	}

	
	

	public String getCharSet() {
		return this.charSet;
	}


	public void setCharSet(String charSet) {
		this.charSet = charSet;
	}


	public int getFieldsCnt() {
		return this.fieldsCnt;
	}


	public void setFieldsCnt(int fieldsCnt) {
		this.fieldsCnt = fieldsCnt;
	}


	public String getFilePath() {
		return this.filePath;
	}


	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}


	public String getFieldSep() {
		return fieldSep;
	}


	public void setFieldSep(String fieldSep) {
		this.fieldSep = fieldSep;
	}


	public int getDefaultFieldLength() {
		return defaultFieldLength;
	}
	

	public int getCurLine() {
		return curLine;
	}


	public void setCurLine(int curLine) {
		this.curLine = curLine;
	}

	
	/**
	 * 获取上次操作的错误信息, 此操作会清空上条报错信息
	 * @return
	 */
	public String getMessage() {
		if(this.message == null) {
			return "";
		}
		
		String msg = this.message;
		
		this.message = "";
		
		return msg;
	}

	private void setMessage(String message) {
		this.message = "[line " + this.curLine + "] " + message;
	}


	/**
	 * @param defaultFieldLength
	 * 某个域默认的最大长度，如果设置大于0,则可以覆盖LField注解中的定义
	 */
	public void setDefaultFieldLength(int defaultFieldLength) {
		this.defaultFieldLength = defaultFieldLength;
	}
	
	
	
	private boolean validateDate(String fmt, String src) {
		if(fmt == null || "".equals(fmt)) {
			throw new java.lang.NullPointerException("");
		}
		
		SimpleDateFormat format = new SimpleDateFormat(fmt);
		try {
			format.parse(src);
		} catch (ParseException e) {
			e.printStackTrace();
			//System.out.println("field format not match");
			return false;
		}
		
		return true;
	}

}
