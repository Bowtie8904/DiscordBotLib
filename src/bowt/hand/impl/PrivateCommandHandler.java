package bowt.hand.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import bowt.cmnd.Command;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.hand.intf.CommandHandler;
import bowt.util.perm.UserPermissions;

/**
 * A default {@link CommandHandler} implementation which is meant for commands in 
 * private channels.
 * 
 * <h1>Example of usage</h1>
 * 
 * <pre>
 * PrivateCommandHandler handler = new PrivateCommandHandler();
 * 
 * Command cmd = new BaseCommand(new String[]{"help", "h"}, 
 *                              UserPermissions.USER);
 * 
 * handler.addCommand(cmd);
 * </pre>
 * <p>
 * This code achieves that the handler will execute the help command whenever 
 * someone types 'prefix-h' or 'prefix-help' in a privatechat towards the bot account.
 * </p>
 * 
 * @author &#8904
 */
public class PrivateCommandHandler implements CommandHandler
{
    /** A {@link Map} which contains all {@link Command}s this instance is handling. */
    protected Map<String, Command> commands;
    
    protected Map<Long, Map<Command, Integer>> overrides;

    /**
     * Creates a new instance that will handle {@link Command}s.
     * 
     * <p>
     * <b>Note</b>
     * <br>
     * An instance from this constructor should only be used to handle commands from
     * private channels.
     * <br><br>
     * Use {@link #GuildCommandHandler(GuildObject)} to handle commands in normal
     * guild channels.
     * </p>
     * 
     * @see #GuildCommandHandler(GuildObject)
     * @see DefaultPrivateCommandHandler
     */
    public PrivateCommandHandler()
    {
        this.commands = new HashMap<String, Command>();
        this.overrides = new ConcurrentHashMap<>();
    }
    
    /**
     * Adds a {@link Command} to this instances {@link #commands} map.
     * 
     * @param command The {@link Command} that should be added.
     * @return This {@link CommandHandler} instance.
     */
    public CommandHandler addCommand(Command command)
    {
        for (String expression : command.getValidExpressions())
        {
            this.commands.put(expression, command);
        }
        return this;
    }
    
    /**
     * Gets all to this handler registered {@link Command}s.
     * 
     * @return A list with the commands.
     */
    public List<Command> getCommands()
    {
        return new ArrayList<Command>(this.commands.values());
    }
    
    public Command getCommand(String expression)
    {
        return this.commands.get(expression);
    }
    
    /**
     * Sets the registered {@link Command}s for this instance.
     * 
     * @param commands The commands to be set.
     */
    public void setCommands(List<Command> commands)
    {
        for (Command command : commands)
        {
            addCommand(command);
        }
    }

    /**
     * Checks if the message contained a valid command and if the user has a high enough 
     * permission level to execute it. If everything checks out the command will be executed.
     * 
     * @see bowt.hand.intf.CommandHandler#dispatch(bowt.evnt.impl.CommandEvent)
     */
    @Override
    public boolean dispatch(CommandEvent event)
    {
        Command command = this.commands.get(event.getCommand());
        
        if (command != null
                && command.isValidPermission(UserPermissions.getPermissionLevel(event.getMessage().getAuthor()), null))
        {
            command.execute(event);
            return true;
        }
        return false;
    }
}