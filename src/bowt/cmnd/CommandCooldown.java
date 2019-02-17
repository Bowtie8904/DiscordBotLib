package bowt.cmnd;

import java.util.concurrent.TimeUnit;

import bowt.guild.GuildObject;
import bowt.thread.Threads;

/**
 * Class which handles a cooldown for a single {@link Command}.
 * 
 * <h1>Basic implementation</h1>
 * 
 * <p>
 * A normal implementation would look something like this:
 * <br><br>
 * Assuming that BasicCommand extends {@link Command}.
 * </p>
 * 
 * <pre>
 * Command command = new BasicCommand(new String[]{"help"}, UserPermissions.USER);
 * 
 * new CommandCooldown(command, 5000).startTimer();
 * </pre>
 * <p>
 * This will make the command unusable for 5 seconds.
 * 
 * @author &#8904
 */
public class CommandCooldown 
{
    private Command command;
    private long cooldown;
    private GuildObject guild;
    
    /**
     * Creates a new instance for the given command with the given cooldown.
     * 
     * @param command The command for which this instance handles the cooldown.
     * @param cooldown The wanted cooldown is milliseconds.
     */
    public CommandCooldown(Command command, long cooldown, GuildObject guild)
    {
        this.command = command;
        this.cooldown = cooldown;
        this.guild = guild;
    }
    
    /**
     * Gets the {@link Command} that this instance is handling the cooldown for.
     * 
     * @return The command.
     */
    public Command getCommand()
    {
        return this.command;
    }
    
    /**
     * Starts the timer for the set command.
     * <p>
     * This will set {@link Command#onCooldown} to true
     * and change it back to false once the timer is finished.
     * </p>
     */
    public void startTimer()
    {
        this.command.setOnCooldown(true, guild);
        Threads.schedulerPool.schedule(new Runnable()
        {
            @Override
            public void run()
            {
                command.setOnCooldown(false, guild);
            }
        }, this.cooldown, TimeUnit.MILLISECONDS);
    }
}