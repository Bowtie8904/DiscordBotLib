package bowt.hand.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import bowt.bot.Bot;
import bowt.hand.intf.DynamicUpdateHandler;
import bowt.hand.presence.Presence;
import bowt.thread.Threads;

/**
 * A handler which will change the playing text of the bot at a set interval.
 * 
 * @author &#8904
 */
public class PresenceHandler
{
    private List<Presence> presences;
    private Bot bot;
    private long delay;
    private DynamicUpdateHandler updater;
    private int currentIndex = 0;
    private ScheduledFuture<?> future;

    /**
     * Creates a new instance without any lines and with a delay of 30 seconds.
     * 
     * @param bot
     *            The bot whichs playing text should be uppdated.
     */
    public PresenceHandler(Bot bot)
    {
        this.bot = bot;
        setDynamicUpdateHandler(() ->
        {
        });
        this.delay = 30000;
        this.presences = new ArrayList<>();
    }

    /**
     * Creates a new instance with a set of lines and with a delay of 30 seconds.
     * 
     * @param bot
     *            The bot whichs playing text should be uppdated.
     * @param presences
     *            The presences to loop through.
     */
    public PresenceHandler(Bot bot, List<Presence> presences)
    {
        this.bot = bot;
        setDynamicUpdateHandler(() ->
        {
        });
        this.delay = 30000;
        this.presences = presences;
    }

    /**
     * Creates a new instance with a set of lines and with a given delay.
     * 
     * @param bot
     *            The bot whichs playing text should be uppdated.
     * @param presences
     *            The presences to loop through.
     * @param delay
     *            The delay between changing presences in milliseconds.
     */
    public PresenceHandler(Bot bot, List<Presence> presences, long delay)
    {
        this.bot = bot;
        setDynamicUpdateHandler(() ->
        {
        });
        this.delay = delay;
        this.presences = presences;
    }

    /**
     * Creates a new instance without any lines and with a given delay.
     * 
     * @param bot
     *            The bot whichs playing text should be uppdated.
     * @param delay
     *            The delay between changing presences in milliseconds.
     */
    public PresenceHandler(Bot bot, long delay)
    {
        this.bot = bot;
        setDynamicUpdateHandler(() ->
        {
        });
        this.delay = delay;
        this.presences = new ArrayList<>();
    }

    /**
     * Sety the presences that this handler will loop through.
     * 
     * @param texts
     *            The presences
     */
    public void setPresences(Presence[] texts)
    {
        for (Presence text : texts)
        {
            this.presences.add(text);
        }
    }

    /**
     * Sety the presences that this handler will loop through.
     * 
     * @param texts
     *            The presences
     */
    public void setPresences(List<Presence> texts)
    {
        this.presences = texts;
    }

    public List<Presence> getPresences()
    {
        return this.presences;
    }

    public void setDynamicUpdateHandler(DynamicUpdateHandler updater)
    {
        this.updater = updater;
    }

    public void setDelay(long delay)
    {
        this.delay = delay;
    }

    public long getDelay()
    {
        return this.delay;
    }

    /**
     * Starts to loop through the set playingtexts.
     * <p>
     * This will call #update() of the set {@link #updater} before changing the playingtext at every interval.
     * </p>
     */
    public void start()
    {
        this.future = Threads.schedulerPool.scheduleWithFixedDelay(new Runnable()
        {
            @Override
            public void run()
            {
                updater.update();
                if (!presences.isEmpty() && currentIndex < presences.size())
                {
                    presences.get(currentIndex).apply(bot);
                    currentIndex ++ ;
                }
                else
                {
                    currentIndex = 0;
                    presences.get(currentIndex).apply(bot);
                    currentIndex ++ ;
                }
            }
        }, 1000, this.delay, TimeUnit.MILLISECONDS);
    }

    public void stop()
    {
        if (this.future != null)
        {
            this.future.cancel(true);
        }
    }
}