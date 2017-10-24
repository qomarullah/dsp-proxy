package com.tsel.dsp.handler;

import org.apache.log4j.Logger;

public class TimerReadTimeOut implements Runnable
{
	public static Logger logger = Logger.getLogger(TimerReadTimeOut.class);

    long idle;
    long idle_max;
    Object o;
	Utility UTIL;
	String PN;

	public TimerReadTimeOut(long i, Utility UTIL, String PN)
    {
		this.UTIL = UTIL;
		this.PN = PN;
        idle = 0L;
        idle_max = 0L;
        o = new Object();
        idle_max = i;
		Thread tt = new Thread(this);
		tt.setDaemon(true);
		tt.start();
    }

    public void reset()
    {
        synchronized(o)
        {
            idle = 0L;
        }
    }

    public long getCurrent()
    {
        return idle;
    }

    public void run()
    {
        while(true)
        {
            try
            {
                Thread.sleep(500000);
            }
            catch(Exception e)
			{
				logger.fatal(PN + "|TimerReadTimeOut|Run: " + e.getMessage(), e);
			}
            
			synchronized(o)
            {
                idle = idle + (long)1000;
                //if(idle_max > (long)1800000) //0x1b7740
				if(idle > (long)900000)
                    idle = 0L;
            }
			//logger.debug(PN + "|TimerReadTimeOut|getCurrent: " + getCurrent());
        }
    }
}