package com.imeee.textFileParser.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME) 
public @interface LLine {
	
	
	/**
	 * 
	 * <p> 字段分割符，使用正则表达式表示，默认为空格分割
	 * 常用的如下：
	 * <li> 空格 "\\s+"
	 * <li> "|" "\\|"
	 * <li> "." "\\."
	 * <li> "\" "\\"
	 * 
	 * @return
	 * 
	 */
	String fieldSep() default "|";
	
	
	/**
	 * 文件中每行记录所拥有有的域数
	 * @return
	 */
	int fieldsCnt() default -1;
}