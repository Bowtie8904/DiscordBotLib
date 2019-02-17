package bowt.hand.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import bowt.bot.Bot;
import bowt.log.Logger;
import bowt.thread.Threads;

/**
 * <p>
 * This basic implementation will send the text of a given {@link Logger}'s logfile at a set interval
 * into a specified logchannel.
 * </p>
 * <p>
 * This class can be extended and its {@link #send()} method can be overriden to customize its behavior.
 * </p>
 * 
 * <h1>Example of usage:</h1>
 * 
 * <pre>
 * ChannelLogger channelLogger = new ChannelLogger("myLogFile.txt", yourBotInstance);
 * channelLogger.setLogChannel("channel id");
 * channelLogger.getLogger().print("Hello World!");
 * channelLogger.start();
 * </pre>
 * 
 * @author &#8904
 */
public class ChannelLogger
{
    /** The default delay in milliseconds that is used if no other has been set. <p>Set to 10 minutes.</p> */
    public static final long DEFAULT_DELAY = 600000;
    
    /** The delay which determines the waiting time between log sending. */
    private long delay;
    
    /** The {@link Logger} instance that holds the file whichs content will be sent. */
    private Logger log;
    
    /** The {@link Bot} instance that should be used to send the logs to a channel. */
    private Bot bot;
    
    /** The channel into which the log messages will be sent. */
    private IChannel logChannel;
    
    /** Contains Strings which are used to filter out certain lines. */
    private List<String> filterTexts = new ArrayList<String>();
    
    
    /**
     * Creates a new instance with the {@link #DEFAULT_DELAY}, a default {@link Logger} and 
     * the given {@link Bot} instance.
     * 
     * @param bot The bot which will send the logs to the channel.
     */
    public ChannelLogger(Bot bot)
    {
        this(new Logger(), DEFAULT_DELAY, bot);
    }
    
    /**
     * Creates a new instance with the {@link #DEFAULT_DELAY}, a given {@link Logger} and 
     * the given {@link Bot} instance.
     * 
     * @param log The logger that holds the file whichs content will be sent.
     * @param bot The bot which will send the logs to the channel.
     */
    public ChannelLogger(Logger log, Bot bot)
    {
        this(log, DEFAULT_DELAY, bot);
    }
    
    /**
     * Creates a new instance with the {@link #DEFAULT_DELAY}, a given path for a {@link Logger} and 
     * the given {@link Bot} instance.
     * 
     * @param logPath The path to the logfile.
     * @param bot The bot which will send the logs to the channel.
     */
    public ChannelLogger(String logPath, Bot bot)
    {
        this(new Logger(logPath), DEFAULT_DELAY, bot);
    }
    
    /**
     * Creates a new instance with a given delay in milliseconds, a given path for a {@link Logger} and 
     * the given {@link Bot} instance.
     * 
     * @param logPath The path to the logfile.
     * @param delay The waiting time between log messages in milliseconds.
     * @param bot The bot which will send the logs to the channel.
     */
    public ChannelLogger(String logPath, long delay, Bot bot)
    {
        this(new Logger(logPath), delay, bot);
    }
    
    /**
     * Creates a new instance with a given delay in milliseconds, a given {@link Logger} and 
     * the given {@link Bot} instance.
     * 
     * @param log The logger that holds the file whichs content will be sent.
     * @param delay The waiting time between log messages in milliseconds.
     * @param bot The bot which will send the logs to the channel.
     */
    public ChannelLogger(Logger log, long delay, Bot bot)
    {
        this.delay = delay;
        this.log = log;
        this.bot = bot;
    }
    
    /**
     * Sets the {@link IChannel} in which the bot can send log messages.
     * 
     * @param logChannel The log channel.
     */
    public void setLogChannel(IChannel logChannel)
    {
        this.logChannel = logChannel;
    }
    
    /**
     * Sets the {@link IChannel} in which the bot can send log messages.
     * 
     * @param id The id of the log channel.
     * @return true if the channel was found, false otherwise.
     */
    public boolean setLogChannel(String id)
    {
        if (id == null)
        {
            return false;
        }
        IChannel nullCheck = this.bot.getClient().getChannelByID(Long.parseLong(id));
        if (nullCheck == null)
        {
            return false;
        }
        this.logChannel = nullCheck;
        return true;
    }
    
    /**
     * Gets the {@link IChannel} in which the bot can send log messages.
     * 
     * @return The channel.
     */
    public IChannel getLogChannel()
    {
        return this.logChannel;
    }
    
    /**
     * Gets the {@link Logger} that holds the logfile.
     * 
     * @return The logger.
     */
    public Logger getLogger()
    {
        return this.log;
    }
    
    /**
     * Gets the {@link Bot} that s sending the logs to the channel.
     * 
     * @return The bot.
     */
    public Bot getBot()
    {
        return this.bot;
    }
    
    /**
     * Adds the given text as a filter.
     * 
     * Any lines from the logfile that contains a filter will not be sent to the channel.
     * 
     * @param filter The filter text to be added.
     */
    public void addFilterText(String filter)
    {
        this.filterTexts.add(filter);
    }
    
    /**
     * Initializes the {@link #timer} and schedules a task which will execute the {@link #send()} method
     * at the set interval (delay).
     */
    public void start()
    {
        Threads.schedulerPool.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                send();
            }
        }, this.delay, this.delay, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Clears the assigned logfile.
     */
    public void clearFile()
    {
        try
        {
            new PrintWriter(this.log.getLoggerFile().getPath()).close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * <p>
     * This default implementation of the send method will make the bot send the content of the logfile
     * into the set log channel. While doing this it considers the character limit for Discord messages
     * and will distribute the log lines onto multiple messages if necessary. 
     * </p>
     * <p>
     * After sending the messages, the logfile will be cleared to ensure that no line will be sent twice.
     * </p>
     */
    public void send()
    {
        List<StringBuilder> builders = new ArrayList<StringBuilder>();
        try (BufferedReader br = new BufferedReader(
                                        new InputStreamReader(
                                                new FileInputStream(
                                                        this.log.getLoggerFile()),"UTF-8")))
        {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null)
            {
                if (!containsFilter(line))
                {
                    if (sb.length() + line.length() >= IMessage.MAX_MESSAGE_LENGTH - 6 && sb.length() != 0)
                    {
                        builders.add(sb);
                        sb = new StringBuilder();
                    }
                    sb.append(line + System.lineSeparator());
                } 
            }
            if (sb.length() != 0)
            {
                builders.add(sb);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        for (StringBuilder sb : builders)
        {
            this.bot.sendPlainMessage("```"+sb.toString()+"```", this.logChannel);
        }
        clearFile();
    }
    
    /**
     * Checks if the given String contains any of the Strings from the filterTexts list.
     * 
     * @param line The line to check.
     * @return true if the line contains any, false otherwise.
     */
    private boolean containsFilter(String line)
    {
        for (String filter : this.filterTexts)
        {
            if (line.toLowerCase().contains(filter.toLowerCase()))
            {
                return true;
            }
        }
        return false;
    }
}