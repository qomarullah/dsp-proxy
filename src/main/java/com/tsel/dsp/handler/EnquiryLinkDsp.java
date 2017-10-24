package com.tsel.dsp.handler;


import org.apache.log4j.Logger;

public class EnquiryLinkDsp implements Runnable
{
	public static Logger logger = Logger.getLogger(EnquiryLinkDsp.class);

	SocketDsp D;
	String ELI;
	Timer timer;
	String session;
	TimerReadTimeOut TRTO;
	String RTT;
	Utility UTIL;
	
	String user;
	String pwd;
	boolean jalan=false;
	
	public EnquiryLinkDsp(SocketDsp D, String ELI, String user, String pwd, Timer timer, TimerReadTimeOut TRTO, String RTT, Utility UTIL)
	{
		this.D = D;
		this.ELI = ELI;
		this.timer = timer;
		this.TRTO = TRTO;
		this.RTT = RTT;
		this.UTIL = UTIL;
		this.user = user;
		this.pwd = pwd;

		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
	}

	public boolean setJalan(boolean bol){
		return this.jalan=bol;
	}
	public void stop() {
		this.jalan = false; // Add preferred synchronization here.
	    Thread.interrupted();
	}
	public void run()
	{
		long elval = Long.parseLong(ELI);
		long eRTT = Long.parseLong(RTT);
		int counter_rto=0;
		//System.out.println("ENQUIRY");
		try
		{
			while(true)
			{
				try
				{
					Thread.sleep(1000);
					if(this.timer.getCurrent() >= elval)
					{
						if(UTIL.isBindSuccess())
						{
							//if(this.jalan){
								
								this.D.EnquiryLink();
								timer.reset();
											
							/*}else{
								try {
									Thread.sleep(1000);
									logger.debug("enquiry_sleep................................");
								} catch (Exception e) {
									// TODO: handle exception
									logger.debug("error_enquiry_sleep................................");
								}
							}
							*/
							
						}else
							timer.reset();
					}

					if(TRTO.getCurrent() >= eRTT || counter_rto > 5)
					{
						if(!UTIL.isBindSuccess())
						{
							TRTO.reset();
							this.D.RECONNECT();
							if(this.D.isConnect())
								this.D.BIND(user,pwd);
							counter_rto =0;
							
						}
						else
							this.TRTO.reset();
						
						counter_rto++;
						
					}
					
					
				}
				catch(Exception e)
				{
					logger.fatal("EnquiryLink_0: " + e.getMessage(), e);
				}
			}
		}
		catch(Exception e)
		{
			logger.fatal("EnquiryLink_1: " + e.getMessage(), e);
		}
	}
}