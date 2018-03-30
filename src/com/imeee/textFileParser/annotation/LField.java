package com.imeee.textFileParser.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Eee
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME) 
public @interface LField {
	
	/**
	 * 此成员在文件中对应域的顺序,不能小于0且不能大于LLine中定义的行域数 fieldsCnt
	 * @return
	 */
	int order() default -1;
	
	/**
	 * 此成员在文件中对应的域的字符串最大长度，小于0则不检查
	 * @return
	 */
	int length() default -1;
	
	String format() default "";
	
	boolean nullable() default true;

}
