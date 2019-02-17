package bowt.hand.impl;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import bowt.bot.Bot;
import bowt.log.Logger;
import bowt.thread.Threads;

/**
 * A handler class which will call its {@link #handle} method when the bot is offline.
 * 
 * @author &#8904
 */
public class OfflineHandler
{
    private static final long DEFAULT_DELAY = 60000;
    protected Logger log;
    private long delay;
    protected Bot bot;
    private boolean active = false;
    private boolean shouldLog = true;
    
    public OfflineHandler(Bot bot, Logger log, long delay)
    {
        this.bot = bot;
        this.log = log;
        this.delay = delay;
        this.log.setPrefix("[OfflineHandler]");
    }
    
    public OfflineHandler(Bot bot, String logpath, long delay)
    {
        this(bot, new Logger(logpath, TimeZone.getTimeZone("CET")), delay);
    }
    
    public OfflineHandler(Bot bot, String logpath)
    {
        this(bot, new Logger(logpath, TimeZone.getTimeZone("CET")), DEFAULT_DELAY);
    }
    
    public OfflineHandler(Bot bot, Logger log)
    {
        this(bot, log, DEFAULT_DELAY);
    }
    
    public OfflineHandler(Bot bot, long delay)
    {
        this(bot, new Logger("logs/alive_check.log", TimeZone.getTimeZone("CET")), delay);
    }
    
    public OfflineHandler(Bot bot)
    {
        this(bot, new Logger("logs/alive_check.log", TimeZone.getTimeZone("CET")), DEFAULT_DELAY);
    }
    
    public boolean shouldLog()
    {
        return this.shouldLog;
    }
    
    public void setShouldLog(boolean shouldLog)
    {
        this.shouldLog = shouldLog;
    }
    
    public Logger getLogger()
    {
        return this.log;
    }
    
    public void start()
    {
        this.active = true;
        Threads.schedulerPool.scheduleWithFixedDelay(new Runnable(){
            @Override
            public void run()
            {
                if (active)
                {
                    if (bot.getClient().isLoggedIn())
                    {
                        if (shouldLog)
                        {
                            log.print(this, "Alive"); 
                        }
                    }
                    else
                    {
                        handle();
                    }
                }
            }
        }, DEFAULT_DELAY, delay, TimeUnit.MILLISECONDS);
    }
    
    public void stop()
    {
        active = false;
    }
    
    public void handle()
    {
        
    }
}