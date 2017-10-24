package com.tsel.dsp.handler;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.tsel.dsp.App;

public class SocketTask implements Callable<String> {
	
	static Logger log = Logger.getLogger(SocketTask.class);
    private String workDetails;
    SocketDsp sd;
    static int retry;
    static int retry_sleep;
    
    static String dsp_user;
    static String dsp_pwd;
    static Utility U;
    static Properties prop;
    
    private static final ThreadLocal<SocketDsp> threadLocal = 
           new ThreadLocal<SocketDsp>(){
    	@Override
        protected SocketDsp initialValue(){
    		 SocketDsp sd = new SocketDsp();
    		 prop = App.prop;
        	 retry = Integer.parseInt(prop.getProperty("dsp.retry"));
             retry_sleep = Integer.parseInt(prop.getProperty("dsp.retry.sleep"));
             dsp_user = prop.getProperty("dsp.user");
     		 dsp_pwd = prop.getProperty("dsp.pwd");
     		
        	return sd;
        	
        }           
    };

    public SocketTask(String payload){     
    	this.workDetails = payload;
    	  
     }

    public  String call() throws Exception {
        //return the thread name executing this callable task
        SocketDsp sd = getSocket(); //gets from threadlocal
        String response="NOK";
         try {
             	response = sd.SUBMITSM(this.workDetails);
             	if(response.equals("") || (response.indexOf("Content-Length: 0")!=-1) || (response.indexOf("Session ID invalid")!=-1) || (response.indexOf("Redirect")!=-1)){
             		int i = 0;
             		while(i < retry){
     					
     					if(response.indexOf("Session ID invalid")!=-1){
     						this.sd.BIND(dsp_user,dsp_pwd);
     						log.debug("RETRY login because invalid session--------------------------------"+i);
     					}
     					
     					log.debug("RETRY sleep waiting stream--------------------------------"+i);
     					Thread.sleep(retry_sleep);
     					
     					if(!response.equals("") && (response.indexOf("Content-Length: 0")==-1) && (response.indexOf("Session ID invalid")==-1) && (response.indexOf("Redirect")==-1)){
     						log.debug("BREAK retry in -------------------------------"+i);
     						break;
     					}else{
     						response = sd.SUBMITSM(this.workDetails);
     					}
     					
     					i++;
     				}
     			}
             	
             //Thread.sleep(15000);
             //if((data.indexOf("5004")==-1) && (data.indexOf("Redirect")==-1) && !data.equals(""))
			//response="NOK";	
        	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.fatal("DSP|FATAL|RECONNECT|CALL|" + e.getMessage(), e);
			sd.RECONNECT();
			Thread.sleep(2000);
			if(sd.isConnect())
				sd.BIND(dsp_user,dsp_pwd);

		}
		return response;
		
    }
   
    public static SocketDsp getSocket(){
        return threadLocal.get();
    }
    

}