package bowt.hand.intf;

import bowt.cmnd.Command;
import bowt.evnt.impl.CommandEvent;
import bowt.hand.impl.GuildCommandHandler;

/**
 * A functional interface for command handlers.
 * 
 * <p>
 * 
 * It implements the {@link #dispatch(CommandEvent)} method.
 * </p>
 * 
 * {@link GuildCommandHandler} and {@link PrivateCommandhandler} are basic
 * implementations of this interface which can easily be used in combination 
 * with {@link Command}s.
 * 
 * @author &#8904
 */
public interface CommandHandler 
{
    /**
     * Defines how {@link CommandEvent}s should be dispatched.
     * @param event The {@link CommandEvent} which should be dispatched.
     * @return Should return true if the command was successfully dispatched.
     */
    public boolean dispatch(CommandEvent event);
}