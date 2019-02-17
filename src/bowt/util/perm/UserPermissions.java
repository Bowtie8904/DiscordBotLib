package bowt.util.perm;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import bowt.bot.Bot;
import bowt.guild.GuildObject;

/**
 * A utility class which contains methods to check for user permissions.
 * 
 * <p>
 * You have to set a {@link Bot} instance by calling {@link #setBot(Bot)}.
 * </p>
 * 
 * @author &#8904
 */
public final class UserPermissions
{
    /**
     * Permission level for creators.
     * <p>
     * This level represents users that have developed the bot.
     * </p>
     */
    public static final int CREATOR = 5;

    /**
     * Permission level for owners of their own bot account.
     */
    public static final int APP_OWNER = 4;

    /**
     * Permission level for owners.
     * <p>
     * This level represents guild owners and bot admins.
     * </p>
     */
    public static final int OWNER = 3;

    /**
     * Permission level for masters.
     * <p>
     * This level represents users that are authorized as masters.
     * </p>
     */
    public static final int MASTER = 2;

    /**
     * Permission level for normal users.
     * <p>
     * This level represents normal users without any special rights.
     * </p>
     */
    public static final int USER = 1;

    /** Permission level for users with no rights. */
    public static final int NONE = 0;

    private static Map<String, Bot> bots = new ConcurrentHashMap<>();

    /**
     * Sets the {@link Bot} which guilds should be checked for permissions.
     * 
     * @param bot
     *            The bot for which this class will be used.
     */
    public static synchronized void setBot(Bot bot)
    {
        bots.put(bot.getClient().getApplicationClientID(), bot);
    }

    /**
     * Returns the name of the permission which is represented by the given level.
     * 
     * @param permissionLevel
     *            The permission level.
     * @return <ul>
     *         <li>0 = NONE</li>
     *         <li>1 = USER</li>
     *         <li>2 = MASTER</li>
     *         <li>3 = OWNER</li>
     *         <li>4 = APP_OWNER</li>
     *         <li>5 = CREATOR</li>
     *         </ul>
     */
    public static String getPermissionString(int permissionLevel)
    {
        String permissionString = "UNKNOWN_PERMISSION_LEVEL";
        switch (permissionLevel)
        {
        case NONE:
            permissionString = "NONE";
            break;
        case USER:
            permissionString = "USER";
            break;
        case MASTER:
            permissionString = "MASTER";
            break;
        case OWNER:
            permissionString = "OWNER";
            break;
        case APP_OWNER:
            permissionString = "APP_OWNER";
            break;
        case CREATOR:
            permissionString = "CREATOR";
            break;
        }
        return permissionString;
    }

    /**
     * Returns the level of the permission which is represented by the given name.
     * 
     * @param permission
     *            The permission name. (Not case sensitive)
     * @return <ul>
     *         <li>0 = NONE</li>
     *         <li>1 = USER</li>
     *         <li>2 = MASTER</li>
     *         <li>3 = OWNER</li>
     *         <li>4 = APP_OWNER</li>
     *         <li>5 = CREATOR</li>
     *         </ul>
     */
    public static int getPermissionLevelForString(String permission)
    {
        int level = -1;
        switch (permission.toLowerCase())
        {
        case "none":
            level = NONE;
            break;
        case "user":
            level = USER;
            break;
        case "master":
            level = MASTER;
            break;
        case "owner":
            level = OWNER;
            break;
        case "app_owner":
            level = APP_OWNER;
            break;
        case "creator":
            level = CREATOR;
            break;
        }
        return level;
    }

    /**
     * Gets the permission level of the given user on that guild.
     * 
     * @param user
     * @param guild
     * @return <ul>
     *         <li>{@link UserPermissions#CREATOR}</li>
     *         <li>{@link UserPermissions#APP_OWNER}</li>
     *         <li>{@link UserPermissions#OWNER}</li>
     *         <li>{@link UserPermissions#MASTER}</li>
     *         <li>{@link UserPermissions#USER}</li>
     *         <li>{@link UserPermissions#NONE}</li>
     *         </ul>
     */
    public static int getPermissionLevel(IUser user, GuildObject guild)
    {
        String clientID = user.getClient().getApplicationClientID();

        if (bots.get(clientID).isBanned(user))
        {
            return NONE;
        }
        else if (bots.get(clientID).isCreator(user))
        {
            return CREATOR;
        }
        else if (bots.get(clientID).isAppOwner(user))
        {
            return APP_OWNER;
        }
        else if (guild.isOwner(user))
        {
            return OWNER;
        }
        else if (guild.isMaster(user))
        {
            return MASTER;
        }
        return USER;
    }

    /**
     * Gets the permission level of the given user.
     * <p>
     * <b>Note</b> that this will return {@link UserPermissions#MASTER} or {@link UserPermissions#OWNER} if the user is
     * a master or owner on any registered guild. This method should only be used for private message handling.
     * </p>
     * 
     * @param user
     * @return <ul>
     *         <li>{@link UserPermissions#CREATOR}</li>
     *         <li>{@link UserPermissions#APP_OWNER}</li>
     *         <li>{@link UserPermissions#OWNER}</li>
     *         <li>{@link UserPermissions#MASTER}</li>
     *         <li>{@link UserPermissions#USER}</li>
     *         <li>{@link UserPermissions#NONE}</li>
     *         </ul>
     */
    public static int getPermissionLevel(IUser user)
    {
        String clientID = user.getClient().getApplicationClientID();

        if (bots.get(clientID).isBanned(user))
        {
            return NONE;
        }
        else if (bots.get(clientID).isCreator(user))
        {
            return CREATOR;
        }
        else if (bots.get(clientID).isAppOwner(user))
        {
            return APP_OWNER;
        }
        else if (bots.get(clientID).isOwner(user))
        {
            return OWNER;
        }
        else if (bots.get(clientID).isMaster(user))
        {
            return MASTER;
        }
        return USER;
    }

    public static int getHighestRolePosition(IUser user, IGuild guild)
    {
        List<IRole> roles = user.getRolesForGuild(guild);
        int highest = -1;

        for (IRole role : roles)
        {
            if (role.getPosition() > highest)
            {
                highest = role.getPosition();
            }
        }
        return highest;
    }

    /**
     * Returns a list of all the {@link Permissions} that are contained in 'permissionsNeeded' but not in
     * 'permissionsHave'.
     * 
     * @param permissionsHave
     *            The permissions that are present.
     * @param permissionsNeeded
     *            The permissions that are needed.
     * @return The list of missing permissions.
     */
    public static List<Permissions> getMissingPermissions(EnumSet<Permissions> permissionsHave,
            EnumSet<Permissions> permissionsNeeded)
    {
        List<Permissions> missing = new ArrayList<Permissions>();
        if (permissionsHave.containsAll(permissionsNeeded))
        {
            return missing;
        }
        for (Permissions perm : permissionsNeeded)
        {
            if (!permissionsHave.contains(perm))
            {
                missing.add(perm);
            }
        }
        return missing;
    }

    /**
     * Returns a list of all the {@link Permissions} that are contained in 'permissionsNeeded' but not in
     * 'permissionsHave'.
     * 
     * @param permissionsHave
     *            The permissions that are present.
     * @param permissionsNeeded
     *            The code for the permissions that are needed.
     * @return The list of missing permissions.
     */
    public static List<Permissions> getMissingPermissions(EnumSet<Permissions> permissionsHave,
            int permissionsNeeded)
    {
        return getMissingPermissions(permissionsHave, Permissions.getAllowedPermissionsForNumber(permissionsNeeded));
    }
}