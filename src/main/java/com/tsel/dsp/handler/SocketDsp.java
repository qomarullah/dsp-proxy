package com.tsel.dsp.handler;

import java.io.*;
import java.net.*;
import java.util.Properties;

import javax.sql.DataSource;
import org.apache.log4j.Logger;

import com.tsel.dsp.App;

public class SocketDsp
{
	public static Logger logger = Logger.getLogger(SocketDsp.class);

	Utility UTIL;
	String PN;
	
	Socket sc;
	InetSocketAddress inet;
	InputStream in;
	OutputStream out;
	BufferedWriter wr;
	BufferedReader rd;
	String request;

	String data;
	
	DataSource dataSource;
	Timer tim;
	TimerReadTimeOut TRTO;
	
	String host;
	int port;
	String local_ip;
	String local_port;
	
	int thread;
	String eli;
	String dsp_user;
	String dsp_pwd;
	String rtt;
	
	boolean isRead=false;
	Utility U;
	EnquiryLinkDsp el;
	private String session;
	private boolean login;
	Properties prop;
	
	public SocketDsp()
	{
		
		 this.prop = App.prop;
		 eli = prop.getProperty(String.valueOf("dsp.enquiry.interval"));
		 host = prop.getProperty(String.valueOf("dsp.host"));
		 port = Integer.parseInt(prop.getProperty(String.valueOf("dsp.port")));
		 this.dsp_user = prop.getProperty(String.valueOf("dsp.user"));
		 this.dsp_pwd = prop.getProperty(String.valueOf("dsp.pwd"));
		 rtt = prop.getProperty(String.valueOf("dsp.rto"));
		 
		 PN = "DSP";
   	     local_port = prop.getProperty(String.valueOf("server.port"));
		 
		 InetAddress ip;
		  try {

			ip = InetAddress.getLocalHost();
			local_ip=ip.getHostAddress();
			logger.debug(PN + "|DSP|Get Current IP = " + ip.getHostAddress());

		  } catch (UnknownHostException e) {

			  logger.fatal(PN + "|DSP|Get Current IP=" + e.getMessage(), e);
		  }
			 
		 try
		{
			
			Timer tim = new Timer(Long.parseLong(eli), PN);
			TimerReadTimeOut trto = new TimerReadTimeOut(Long.parseLong(rtt), U, PN);
			this.CONNECT();
			
			if(this.isConnect()){
				this.BIND(dsp_user,dsp_pwd);
				U.SetBS(true);
			}
			this.el=new EnquiryLinkDsp(this, eli, dsp_user, dsp_pwd, tim,  trto, rtt, U);
			
			logger.info(PN + "|DSP|DSP()=Initialized");
		}
		catch(Exception e)
		{
			logger.fatal(PN + "|DSP|DSP()=" + e.getMessage(), e);
		}
	}

	public void CONNECT()
	{
		try
		{
			sc = new Socket();
			inet = new InetSocketAddress(host, port);
			sc.connect(inet, 10000);
			sc.setKeepAlive(true);
			
			in = sc.getInputStream();
			out = sc.getOutputStream();
			logger.info(PN + "|DSP|CONNECT(" + host + ", " + port + ")=Connected");
		}
		catch(Exception e)
		{
			logger.fatal(PN + "|DSP|CONNECT(" + host + ", " + port + ")=" + e.getMessage(), e);
		}
	}

	public void RECONNECT()
	{
		try
		{
			if(sc.isConnected())
			{
				try
				{
					out.close();
					out = null;
					in.close();
					in = null;
					sc.close();
					sc = null;
					logger.info(PN + "|DSP|RECONNECT(" + host + ", " + port + ")=Connection_Closed_for_Reconnect");
				}
				catch(Exception e)
				{
					logger.fatal(PN + "|DSP|RECONNECT(" + host + ", " + port + ")=" + e.getMessage(), e);
				}
				
			}
			else
				logger.info(PN + "|DSP|RECONNECT(" + host + ", " + port + ")=Connection_Lost");
			
			Thread.sleep(3000);
			sc = new Socket();
			inet = new InetSocketAddress(host, port);
			sc.connect(inet, 10000);
			sc.setKeepAlive(true);
			in = sc.getInputStream();
			out = sc.getOutputStream();
			
			logger.info(PN + "|DSP|RECONNECT(" + host + ", " + port + ")=Reconnected");
		
		}
		catch(Exception e)
		{
			logger.fatal(PN + "|DSP|RECONNECT(" + host + ", " + port + ")=" + e.getMessage(), e);
		}
	}

	private String setSession(String session){
		return this.session=session;
	}
	private boolean setLogin(boolean bol){
		return this.login=bol;
	}
	public void BIND(String user, String pwd)
	{
		try
		{
			this.setLogin(true);
			logger.debug("login_running.........................");
			
			data = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body><LGI><HLRSN>1</HLRSN><OPNAME>[user]</OPNAME><PWD>[pwd]</PWD></LGI></soapenv:Body></soapenv:Envelope>";
		    data=data.replaceAll("\\[user\\]", user).replaceAll("\\[pwd\\]",pwd);
			data +="\n\n";
			
		    String header = "POST " + "/"+ " HTTP/1.1\r\n";
			header += "HOST: "+ local_ip + ":" + local_port + "\r\n";
			header += "Content-Length: " + data.length() + "\r\n";
			header += "Content-Type: text/xml;charset=UTF-8\r\n";
			header += "\r\n";
	   
			
			wr = new BufferedWriter(new OutputStreamWriter(sc.getOutputStream(), "UTF8"));
			
			wr.write(header);
			wr.write(data);
			wr.flush();

			logger.info(PN + "|SUBMIT_SM|OUT|" + data );
			   
		    Reader rd = new InputStreamReader(sc.getInputStream());
		    String line="";
		    
		    StringBuilder sb = new StringBuilder();
		    char[] chars = new char[10*1024];
		    int len;
		    if((len = rd.read(chars))>=0) {
		    	sb.append(chars, 0, len);
		    	logger.info(PN + "|RESPONSE|" + sb);
		    }
		    
		    line = sb.toString();
		    String[] arrayHeader = line.split("\n");
		    for (int i = 0; i < arrayHeader.length; ++i) {
			    if(arrayHeader[i].startsWith("Location")){
			    	String[] param = arrayHeader[i].split("/");
			     	if (param[3].length()>0) {
			         this.session = param[3].trim(); 
			         setSession(this.session);
			        }	
			    }
		    }
		    logger.info(PN + "|DSP|LOGIN|GET_SESSION|" + session);
		  
			//wr.close();
		    //rd.close();
			this.setLogin(false);
			logger.debug("login_end.........................");
			

		}
		catch(Exception e)
		{
			logger.fatal(PN + "|DSP|LOGIN=" + e.getMessage(), e);
			this.setLogin(false);

		}
	}


	public String SUBMITSM(String data) throws Exception
	{
		String resp="-1";
		try
		{
			if(this.el.jalan){
				resp="";
				logger.debug("enquiry_running.................");
				
				return resp;
			}
			if(this.login){
				resp="";
				logger.debug("login_running.................");
				
				return resp;
			}
			
			this.setLogin(false);
			this.el.setJalan(false);
			this.el.timer.reset();	
			
			data +="\r\n";
			wr = new BufferedWriter(new OutputStreamWriter(sc.getOutputStream(), "UTF8"));
		    String header = "POST " + "/"+this.session + " HTTP/1.1\r\n";
			header += "HOST: "+ local_ip + ":" + local_port + "\r\n";
			header += "Content-Length: " + data.length() + "\r\n";
			header += "Content-Type: text/xml;charset=UTF-8\r\n";
			header += "\r\n";
	    
			
			wr.write(header);
			wr.write(data);
		    wr.flush();

		    logger.info(PN + "|SUBMIT_SM|OUT|" + header + data );
		    rd = new BufferedReader(new InputStreamReader(sc.getInputStream()));
		    StringBuilder sb = new StringBuilder();
		    char[] chars = new char[10*1024];
		    int len;
	        if((len = rd.read(chars))>=0) {
		    	sb.append(chars, 0, len);
		    }
		    resp = sb.toString();
		    
		    logger.info(PN + "|SUBMIT_SM|RESP|" + resp );
		   	
		}
		catch(Exception e)
		{
			logger.fatal(PN + "|DSP|FATAL|SUBMITSM|OUT|" + data + "|" + e.getMessage(), e);
			this.RECONNECT();
			if(this.isConnect())
				this.BIND(dsp_user,dsp_pwd);
		}
		
		return resp;
	}

	public void EnquiryLink() throws Exception
	{
		String resp="-1";
		try
		{
			this.el.setJalan(true);
			
			logger.debug("enquiry_running.........................");
			data="";
			wr = new BufferedWriter(new OutputStreamWriter(sc.getOutputStream(), "UTF8"));
		
			String header = "POST " + "/"+this.session + " HTTP/1.1\r\n";
			header += "SOAPAction: Notification" + "\r\n";
			header += "HOST: "+ local_ip + ":" + local_port + "\r\n";
			header += "Content-Length: " + 0 + "\r\n";
			header += "Content-Type: text/xml;charset=UTF-8\r\n";
			header += "\r\n";
	    
	    
			wr.write(header);
			wr.write(data);
			wr.flush();
		   
		    logger.info(PN + "|DSP|ENQUIRY_LINK|SESSION|" + session);
		    logger.info(PN + "|DSP|ENQUIRY_LINK|SUBMIT|" + header+""+data);
		    
			//Thread.sleep(1000);
			InputStreamReader in = new InputStreamReader(sc.getInputStream());
			
		    rd = new BufferedReader(in);
		    StringBuilder sb = new StringBuilder();
		    char[] chars = new char[4*1024];
		    int len;
			if((len = rd.read(chars))>=0) {
		    	sb.append(chars, 0, len);
		    }
	        //wr.close();
		    //rd.close();
		    resp = sb.toString();
		
		    /*if(sc.isConnected()){
		    	logger.info(PN + "|DSP|ENQUIRY_LINK|STILL_CONNECT");
				
		    }*/	    
			
			logger.info(PN + "|DSP|ENQUIRY_LINK|RESPONSE|" + resp);
		    
			if(resp.indexOf("Error")!=-1 || resp.indexOf("5004")!=-1){
		    	 logger.info(PN + "|DSP|ENQUIRY_LINK|RELOGIN..."+resp+"|"+dsp_user+"|"+dsp_pwd);
		    	 this.BIND(dsp_user, dsp_pwd);
		    	 Thread.sleep(1000);
			    	
		    }
		    
		    this.el.setJalan(false);
			logger.debug("enquiry_end.................");
			
			
		}
		catch(Exception e)
		{
			logger.fatal(PN + "|DSP|EnquiryLink(" + data + ")=" + e.getMessage(), e);
			this.RECONNECT();
			if(this.isConnect())
				this.BIND(dsp_user,dsp_pwd);
			
			 this.el.setJalan(false);
				
		}
	}
	
	public boolean isConnect() throws Exception
	{
		return sc.isConnected();
	}

	public void write(byte b[]) throws Exception
    {
        out.write(b);
    }

    public void write(int i) throws Exception
    {
        out.write(i);
    }

    public void flush() throws Exception
    {
        out.flush();
    }

    public int read() throws Exception
    {
        int i = in.read();
        return i;
    }

	public void read(byte b[]) throws Exception
    {
        in.read(b);
    }
}