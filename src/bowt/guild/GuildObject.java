package bowt.guild;

import java.util.ArrayList;
import java.util.List;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import bowt.hand.intf.CommandHandler;

/**
 * A class which represents a Discord guild.
 * 
 * @author &#8904
 */
public class GuildObject
{
    /**
     * A list containing the users in this guild which have permission to command the bot.
     */
    protected List<String> masters;

    /** A list containing the users that function as bot admins on this guild. */
    protected List<String> owners;

    /** The {@link IGuild} object this instance is representing. */
    protected final IGuild guild;

    /** The standard handler for commands. */
    protected CommandHandler commandHandler;

    /** The command prefix for this guild. */
    protected String prefix;

    /**
     * Creates a new {@link GuildObject} instance.
     * 
     * @param guild
     *            The guild this object should represent.
     */
    public GuildObject(IGuild guild)
    {
        this.guild = guild;
        this.masters = new ArrayList<>();
        this.owners = new ArrayList<>();
    }

    /**
     * Gets the String ID of the represented {@link #guild}.
     * 
     * @return The String ID of this instances {@link #guild}.
     */
    public String getStringID()
    {
        return this.guild.getStringID();
    }

    /**
     * Gets the long ID of the represented {@link #guild}.
     * 
     * @return The long ID of this instances {@link #guild}.
     */
    public long getLongID()
    {
        return this.guild.getLongID();
    }

    /**
     * Gets the represented {@link #guild}.
     * 
     * @return This instances {@link #guild}.
     */
    public IGuild getGuild()
    {
        return this.guild;
    }

    /**
     * Sets a new command prefix for this guild.
     * 
     * @param prefix
     *            The new prefix.
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
    }

    /**
     * Gets the command prefix for this guild.
     * 
     * @return The prefix.
     */
    public String getPrefix()
    {
        return this.prefix;
    }

    /**
     * Gets this instances {@link #owners}.
     * 
     * @return This instances {@link #owners}.
     */
    public List<String> getOwners()
    {
        return this.owners;
    }

    /**
     * Sets this instances {@link #owners}.
     * 
     * @param masters
     *            The {@link List} which should be set as {@link #owners}.
     */
    public void setOwners(List<String> owners)
    {
        this.owners = owners;
    }

    /**
     * Adds the given user to this instances {@link #owners} if it is not yet contained.
     * 
     * @param master
     *            The {@link IUser} that should be added.
     * @return true if the user was not yet contained and successfully added.
     */
    public boolean addOwner(IUser owner)
    {
        if (this.owners.contains(owner.getStringID()))
        {
            return false;
        }
        this.masters.remove(owner.getStringID());
        return this.owners.add(owner.getStringID());
    }

    public boolean addOwner(String id)
    {
        if (this.owners.contains(id))
        {
            return false;
        }
        this.masters.remove(id);
        return this.owners.add(id);
    }

    /**
     * Removes the given {@link IUser} from this instances {@link #owners}.
     * 
     * @param master
     *            The {@link IUser} that should be removed.
     * @return true if the given user was contained and successfully removed.
     */
    public boolean removeOwner(IUser owner)
    {
        return this.owners.remove(owner.getStringID());
    }

    /**
     * Checks if the given user is an owner in this guild.
     * 
     * @param user
     *            The {@link IUser} whichs master permissions should be checked.
     * @return true if the given user is contained in {@link #owners}.
     */
    public boolean isOwner(IUser user)
    {
        return this.owners.contains(user.getStringID());
    }

    /**
     * Gets this instances {@link #masters}.
     * 
     * @return This instances {@link #masters}.
     */
    public List<String> getMasters()
    {
        return this.masters;
    }

    /**
     * Sets this instances {@link #masters}.
     * 
     * @param masters
     *            The {@link List} which should be set as {@link #masters}.
     */
    public void setMasters(List<String> masters)
    {
        this.masters = masters;
    }

    /**
     * Adds the given user to this instances {@link #masters} if it is not yet contained.
     * 
     * @param master
     *            The {@link IUser} that should be added.
     * @return true if the user was not yet contained and successfully added.
     */
    public boolean addMaster(IUser master)
    {
        if (this.masters.contains(master.getStringID()) || this.owners.contains(master.getStringID()))
        {
            return false;
        }
        return this.masters.add(master.getStringID());
    }

    public boolean addMaster(String id)
    {
        if (this.masters.contains(id) || this.owners.contains(id))
        {
            return false;
        }
        return this.masters.add(id);
    }

    /**
     * Removes the given {@link IUser} from this instances {@link #masters}.
     * 
     * @param master
     *            The {@link IUser} that should be removed.
     * @return true if the given user was contained and successfully removed.
     */
    public boolean removeMaster(IUser master)
    {
        return this.masters.remove(master.getStringID());
    }

    /**
     * Checks if the given user is a master in this guild.
     * 
     * @param user
     *            The {@link IUser} whichs master permissions should be checked.
     * @return true if the given user is contained in {@link #masters}.
     */
    public boolean isMaster(IUser user)
    {
        return this.masters.contains(user.getStringID());
    }

    /**
     * Gets the currently set {@link #commandHandler} for this instance.
     * 
     * @return The CommandHandler.
     */
    public CommandHandler getCommandHandler()
    {
        return this.commandHandler;
    }

    /**
     * Sets the {@link #commandHandler} for this instance.
     * 
     * @param commandHandler
     *            The {@link CommandHandler} of this instance.
     */
    public void setCommandHandler(CommandHandler commandHandler)
    {
        this.commandHandler = commandHandler;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (this.getStringID().equals(((GuildObject)obj).getStringID()))
        {
            return true;
        }
        return false;
    }
}