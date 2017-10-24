package com.tsel.dsp.handler;

import java.util.*;
import java.text.*;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

public class Utility
{
	public static Logger logger = Logger.getLogger(Utility.class);

	Properties prop;
	java.util.Date skr;
	SimpleDateFormat formatter;
	String tdate;
	String OP;
	boolean isBS;
	String session;
	String PN;
	DataSource Ds;
	 
	public static HashMap<String, String> hashConfig = new HashMap<String, String>();
	
	public Utility()
	{
		
	}
	

	public String GetTrxID(String OP, String MSISDN)
	{
		skr = new java.util.Date();
		formatter = new SimpleDateFormat("yyyyMMddHHmmssS");
		tdate = formatter.format(skr);
		return MSISDN + tdate;
	}
	private static long counter = 10000;

	public static String generateTxid(String prefix, String msisdn) {
		SimpleDateFormat sdfTxid = new SimpleDateFormat("yyMMddHHmmss");
		String dt = sdfTxid.format(new Date());
		String suffix = msisdn.substring(msisdn.length() - 5, msisdn.length());
		synchronized (sdfTxid) {
			counter++;
			if (counter > 99999) {
				counter = 10000;
			}
		}
		return prefix + dt + suffix + String.valueOf(counter);
	}

	public void SetBS(boolean B_BS)
	{
		isBS = B_BS;
	}
	
	public boolean isBindSuccess()
	{
		return isBS;
	}
}