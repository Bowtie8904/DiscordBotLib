package bowt.hand.impl;

import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import bowt.bot.Bot;

/**
 * A base handler for the {@link ReadyEvent}.
 * 
 * <p>
 * An implementation of this class will listen to the {@link ReadyEvent} of the {@link Bot} that has been set. <br>
 * <br>
 * Once the event has been triggered the creators are loaded from the property file, the code in {@link #prepare()} will
 * be executed and {@link Bot#setReady(boolean)} is set to true.
 * </p>
 * 
 * @author &#8904
 */
public abstract class BotReadyHandler implements IListener<ReadyEvent>
{
    /** The {@link Bot} object whichs {@link ReadyEvent} should be listened for. */
    protected Bot bot;

    /**
     * Creates a new instance for the given {@link Bot}.
     * 
     * @param bot
     *            The bot for whichs {@link ReadyEvent} should be listened.
     */
    public BotReadyHandler(Bot bot)
    {
        this.bot = bot;
    }

    /**
     * Loads the bot creators, calls {@link #prepare()} and marks the bot as ready.
     * 
     * @see sx.blah.discord.api.events.IListener#handle(sx.blah.discord.api.events.Event)
     */
    @Override
    public void handle(ReadyEvent event)
    {
        this.bot.loadCreators();
        prepare();
        this.bot.setAppOwner();
        this.bot.setReady(true);
    }

    protected abstract void prepare();
}