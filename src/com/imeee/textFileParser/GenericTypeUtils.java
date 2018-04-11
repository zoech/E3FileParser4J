package com.imeee.textFileParser;

public class GenericTypeUtils {
public static Class<?> getGenericClass(Class<?> clz){
		
		Class<?> genericClz = null;
		if(int.class == clz) {
			genericClz = Integer.class;
		} else if(long.class == clz) {
			genericClz = Long.class;
		} else if(double.class == clz) {
			genericClz = Double.class;
		} else if(float.class == clz) {
			genericClz = Float.class;
		} else if(boolean.class == clz) {
			genericClz = Boolean.class;
		} else if(char.class == clz) {
			genericClz = Character.class;
		} else if(byte.class == clz){
			genericClz = Byte.class;
		} else if(short.class == clz) {
			genericClz = Short.class;
		} else {
			genericClz = clz;
		}
		
		return genericClz;
	}
}
