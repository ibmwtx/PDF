package com.ibm.websphere.dtx.m4pdf;

import java.util.HashSet;

public class NameFactory {

	private static NameFactory instance = null;
	
	private HashSet<String> hashTable = new HashSet<String>();
	private int iUniqueInstance = 1;
	
	private NameFactory() 
	{
		
	}
	public static NameFactory getItsInstance() {
		
		if (instance == null)
			instance = new NameFactory();
		
		return instance;
	}
	
	public boolean hasString(String value) {
		
		if (hashTable.contains(value)) {
			return true;
		}
		
		hashTable.add(value);
		
		return false;
	}
	public int getUniqueInstance() {
		// TODO Auto-generated method stub
		return iUniqueInstance++;
	}
}
