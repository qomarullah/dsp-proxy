package com.tsel.dsp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/* Bismillahirrahmaanirrahiim
 * Author : qomarullah
 * Created-date : 2017/10/24
 * purpose : proxy dsp for scp
 * 
 */


public class App 
{
	private final static Logger log = LogManager.getLogger(App.class);
	public static Properties prop;
	public static String test="1";
	
    public static void main( String[] args )
    {
    	//setup properties
    	prop = new Properties();
    	if(args.length>0)
    		loadProperties(prop, args[0]);
    	else{
    		log.debug("properties not found");
    		System.exit(0);
    	}
    	
    	//setup log
        if(args.length>1)
    		BasicConfigurator.configure();
    	else
    		PropertyConfigurator.configure(args[0]);
    	
    	//setup server
    	int port=Integer.parseInt(prop.getProperty("server.port","9001"));
    	int maxThreads = Integer.parseInt(prop.getProperty("server.maxthread","200"));
    	int minThreads = Integer.parseInt(prop.getProperty("server.minthread","30"));
    	int timeOutMillis = Integer.parseInt(prop.getProperty("server.timeout","20000"));
    	int dsp_pool = Integer.parseInt(prop.getProperty("server.timeout","20000"));
    	
    	//threadPool
    	ExecutorService threadPool = Executors.newFixedThreadPool(dsp_pool, Executors.defaultThreadFactory());
		
    	//start server
    	new SparkServer(port, minThreads, maxThreads, timeOutMillis, threadPool);
    	//new TomcatServer(port, minThreads, maxThreads, timeOutMillis);
    }
    
    public static void loadProperties(Properties prop, String filename){

    	InputStream input = null;
    	try {

    		input = new FileInputStream(filename);
    		// load a properties file
    		prop.load(input);
    	} catch (IOException ex) {
    		ex.printStackTrace();
    	} finally {
    		if (input != null) {
    			try {
    				input.close();
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
    	}

    }
}
