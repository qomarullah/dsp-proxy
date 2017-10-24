package com.tsel.dsp.handler;

import org.apache.log4j.Logger;

public class Timer implements Runnable
{
	public static Logger logger = Logger.getLogger(Timer.class);

    long idle;
    long idle_max;
    Object o;
	String PN;

	public Timer(long i, String PN)
    {
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
                Thread.sleep(1000);
            }
            catch(Exception e)
			{
				logger.fatal(PN + "|Timer|Run: " + e.getMessage(), e);
			}
            
			synchronized(o)
            {
                idle = idle + (long)1000;
                //if(idle_max > (long)1800000) //0x1b7740
				if(idle > (long)900000)
                    idle = 0L;
            }
			//logger.debug(PN + "|Timer|getCurrent: " + getCurrent());
        }
    }
}