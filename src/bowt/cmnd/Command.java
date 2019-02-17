package bowt.cmnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sx.blah.discord.api.internal.json.objects.EmbedObject;
import bowt.evnt.impl.CommandEvent;
import bowt.guild.GuildObject;
import bowt.hand.intf.CommandHandler;

/**
 * A class which represents a command.
 * 
 * <h1>Basic implementation</h1>
 * 
 * <p>
 * A basic implementation would look something like this: <br>
 * (Assuming that 'BaseCommand' extends this class)
 * </p>
 * 
 * <pre>
 * GuildCommandHandler handler = new GuildCommandHandler(exampleGuildObject);
 * 
 * Command cmd = new BaseCommand(new String[]
 * {
 *         &quot;help&quot;, &quot;h&quot;
 * },
 *         UserPermissions.USER);
 * 
 * handler.addCommand(cmd);
 * </pre>
 * <p>
 * This code achieves that the handler will execute the help command whenever someone types 'prefix-h' or 'prefix-help'
 * in a chat on 'exampleGuildObject'.
 * </p>
 * 
 * @author &#8904
 */
public abstract class Command
{
    /** Used to indicate that the permission level was reverted to the default. */
    public static final int DEFAULT_PERMISSION = 1;

    /** Used to indicate that the permission level was changed to a new level. */
    public static final int NEW_PERMISSION = 2;

    /** Used to indicate that the permission level could not be overriden. */
    public static final int CANT_OVERRIDE = -1;

    /** A list which contains all the strings that are considered valid for this command. */
    protected List<String> validExpressions;

    protected Map<Long, Integer> overrides;

    protected Map<Long, Boolean> cooldowns;

    /** The default permission level that is needed to execute this command. */
    protected final int defaultPermission;

    /** Indicates whether the permission level of this command may be overriden. */
    protected boolean canOverridePermission;

    protected Map<String, String> aliases;

    /**
     * Creates a new {@link Command} instance.
     * 
     * @param validExpressions
     *            The expressions that should be considered valid.
     * @param permission
     *            The permission level needed to execute this command.
     */
    public Command(String[] validExpressions, int permission)
    {
        this.validExpressions = new ArrayList<String>();
        for (String command : validExpressions)
        {
            this.validExpressions.add(command);
        }
        this.defaultPermission = permission;
        this.canOverridePermission = true;
        this.overrides = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        this.aliases = new ConcurrentHashMap<>();
    }

    /**
     * Creates a new {@link Command} instance.
     * 
     * @param validExpressions
     *            The expressions that should be considered valid.
     * @param permission
     *            The permission level needed to execute this command.
     */
    public Command(List<String> validExpressions, int permission)
    {
        this.validExpressions = validExpressions;
        this.defaultPermission = permission;
        this.canOverridePermission = true;
        this.overrides = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        this.aliases = new ConcurrentHashMap<>();
    }

    /**
     * Creates a new {@link Command} instance.
     * 
     * @param validExpressions
     *            The expressions that should be considered valid.
     * @param permission
     *            The permission level needed to execute this command.
     * @param canOverride
     *            true if the permission level may be overriden.
     */
    public Command(String[] validExpressions, int permission, boolean canOverride)
    {
        this.validExpressions = new ArrayList<String>();
        for (String command : validExpressions)
        {
            this.validExpressions.add(command);
        }
        this.defaultPermission = permission;
        this.canOverridePermission = canOverride;
        this.overrides = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
        this.aliases = new ConcurrentHashMap<>();
    }

    /**
     * Creates a new {@link Command} instance.
     * 
     * @param validExpressions
     *            The expressions that should be considered valid.
     * @param permission
     *            The permission level needed to execute this command.
     * @param canOverride
     *            true if the permission level may be overriden.
     */
    public Command(List<String> validExpressions, int permission, boolean canOverride)
    {
        this.validExpressions = validExpressions;
        this.defaultPermission = permission;
        this.canOverridePermission = canOverride;
        this.overrides = new ConcurrentHashMap<>();
        this.cooldowns = new ConcurrentHashMap<>();
    }

    public void addAlias(String guildID, String alias)
    {
        this.aliases.put(guildID, alias.toLowerCase());
    }

    public String getAlias(String guildID)
    {
        String alias = this.aliases.get(guildID);

        return alias;
    }

    /**
     * Checks if the given String is a valid command expression.
     * 
     * @param expression
     *            The string that should be checked.
     * @return true is the string is valid.
     */
    public boolean isValidExpression(String expression)
    {
        if (this.validExpressions.contains(expression))
        {
            return true;
        }
        return false;
    }

    /**
     * Checks if the given permission level is high enough to execute this command.
     * 
     * @param permission
     *            The permission level that should be checked.
     * @return true if the permission level is high enough.
     */
    public boolean isValidPermission(int permission, GuildObject guild)
    {
        if (guild == null)
        {
            return permission >= this.defaultPermission;
        }
        Integer permissionOverride = overrides.get(guild.getLongID());
        if (permissionOverride == null)
        {
            permissionOverride = this.defaultPermission;
        }
        return permission >= permissionOverride;
    }

    /**
     * Gets the default permission level.
     * 
     * @return The permission level.
     */
    public int getDefaultPermission()
    {
        return this.defaultPermission;
    }

    /**
     * Gets the overriden permission level.
     * 
     * @return The permission level.
     */
    public int getPermissionOverride(GuildObject guild)
    {
        Integer override = overrides.get(guild.getLongID());
        return override == null ? this.defaultPermission : override;
    }

    /**
     * Overrides the permission level for this command.
     * 
     * @param permission
     *            The desired permission level.
     * @return <ul>
     *         <li>{@link #CANT_OVERRIDE} If the permission level can't be overriden.</li>
     *         <li>{@link #DEFAULT_PERMISSION} If the permission level was changed to the default.</li>
     *         <li>{@link #NEW_PERMISSION} If the permission level was changed to a new level.</li>
     *         </ul>
     */
    public synchronized int overridePermission(int permission, GuildObject guild)
    {
        if (!this.canOverridePermission)
        {
            return CANT_OVERRIDE;
        }
        if (this.defaultPermission == permission)
        {
            overrides.remove(guild.getLongID());
            return DEFAULT_PERMISSION;
        }
        overrides.put(guild.getLongID(), permission);
        return NEW_PERMISSION;
    }

    /**
     * Indicates whether the permission level of this command can be overriden.
     * 
     * @return true if the permission level can be overriden.
     */
    public boolean canOverride()
    {
        return this.canOverridePermission;
    }

    /**
     * Returns a list containing all valid expressions for this command.
     * 
     * @return The list with the valid expressions.
     */
    public List<String> getValidExpressions()
    {
        return this.validExpressions;
    }

    /**
     * Sets {@link #onCooldown}.
     * 
     * <p>
     * True indicates that this command is currently on cooldown.
     * </p>
     * 
     * @param onCooldown
     *            true or false.
     */
    public synchronized void setOnCooldown(boolean onCooldown, GuildObject guild)
    {
        if (onCooldown)
        {
            cooldowns.put(guild.getLongID(), true);
        }
        else
        {
            cooldowns.remove(guild.getLongID());
        }
    }

    /**
     * Indicates whether this command is currently on cooldown.
     * 
     * @return true if this command is on cooldown.
     */
    public boolean isOnCooldown(GuildObject guild)
    {
        return cooldowns.containsKey(guild.getLongID());
    }

    /**
     * Defines the action that should be performed when this command is called.
     * 
     * @param event
     *            The {@link CommandEvent} dispatched by the {@link CommandHandler}.
     */
    public abstract void execute(CommandEvent event);

    /**
     * Defines a description of the command.
     * 
     * @return The help embed.
     */
    public abstract EmbedObject getHelp(GuildObject guild);
}