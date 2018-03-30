package com.imeee.textFileParser;

import java.lang.reflect.Field;

import com.imeee.textFileParser.annotation.LField;

public class FieldHelper {
	private LField meta;
    private Field field;
    private String name;
    private Class<?> type;
    
    
    
	public FieldHelper(LField meta, Field field) {
		super();
		this.meta = meta;
		this.field = field;
		this.name = field.getName();
		this.type = field.getType();
	}
	
	
	public LField getMeta() {
		return meta;
	}
	public void setMeta(LField meta) {
		this.meta = meta;
	}
	public Field getField() {
		return field;
	}
	public void setField(Field field) {
		this.field = field;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Class<?> getType() {
		return type;
	}
	public void setType(Class<?> type) {
		this.type = type;
	}
    
    
    
}