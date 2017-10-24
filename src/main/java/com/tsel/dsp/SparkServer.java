package com.tsel.dsp;


import static spark.Spark.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tsel.dsp.handler.SocketTask;



public class SparkServer {

	private final static Logger log = LogManager.getLogger(SparkServer.class);
	
	  
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}
	public SparkServer(int port, int minThreads, int maxThreads, int timeOutMillis, ExecutorService threadPool){
		
		
		port(port);
		threadPool(maxThreads, minThreads, timeOutMillis);
		get("/hello", (req, res) -> "Bismillahirrahmanirrahim");
		
		get("/", (request, response) -> {
			String resp="NotValidMethod";
			
		    return resp;
		});
		
		post("/", (request, response) -> {
			String resp="";
			
			 String payload = request.body().replaceAll("\r\n", "");
			 Callable<String> callable = new SocketTask(payload);
		     Future<String> future = threadPool.submit(callable);
		      
		     try {
		    	  resp = future.get();
		    	  //log.debug("masuk--------------------------------------------");
		    	     
		       }catch (InterruptedException ef){
		        	ef.printStackTrace();
		        }catch (ExecutionException e) {
		          e.printStackTrace();
		      }
		    
		    log.info("payload:"+payload+"|"+resp);
		    return resp;
		});
		
		
		
	}
	
	
}