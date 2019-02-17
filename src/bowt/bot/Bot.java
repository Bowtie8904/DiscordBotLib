package bowt.bot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import bowt.bot.exc.BowtieClientException;
import bowt.cmnd.PrefixLoader;
import bowt.cons.Colors;
import bowt.cons.LibConstants;
import bowt.guild.GuildObject;
import bowt.hand.impl.BotReadyHandler;
import bowt.hand.impl.PresenceHandler;
import bowt.log.Logger;
import bowt.prop.Properties;
import bowt.util.perm.UserPermissions;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuffer.IRequest;

/**
 * A class which provides basic methods for a Discord bot.
 * 
 * <h1>Basic implementation</h1>
 * <p>
 * A basic implementation would look something like this:
 * </p>
 * 
 * <pre>
 * Bot bot = new Bot(&quot;your_bot_token&quot;, &quot;your_command_prefix&quot;);
 * bot.login();
 * </pre>
 * 
 * @author &#8904
 */
public class Bot
{
    /** The {@link IDiscordClient} instance used by this bot. */
    protected IDiscordClient client;

    /** The list containing the registered {@link GuildObject}s. */
    protected Map<Long, GuildObject> guilds;

    /** The list containing the registered user IDs with creator permissions. */
    protected List<String> creators;

    /** The list containing the registered user IDs with creator permissions. */
    protected String appOwner;

    /** The list containing all the user IDs that are banned from using commands. */
    protected List<String> bannedUsers;

    /** The application token. */
    protected String token;

    /**
     * The prefix used to identify commands.
     * <p>
     * The default is 'cmd-'
     * </p>
     */
    protected static String prefix = "cmd-";

    /** A loader which will set the prefix for each guild on startup. */
    protected PrefixLoader prefixLoader;

    protected PresenceHandler presenceHandler;

    /** The {@link Logger} which is used to log information. */
    public static Logger log = new Logger("logs/system_logs.log", TimeZone.getTimeZone("CET"));

    /** The {@link Logger} which is used to log error information. */
    public static Logger errorLog = new Logger("logs/error_logs.log", TimeZone.getTimeZone("CET"));

    /**
     * Indicates whether the bot has finished all preparations.
     * <p>
     * Set to true by the {@link BotReadyHandler}.
     */
    protected boolean isReady = false;

    /**
     * Creates a new {@link Bot} instance.
     * <p>
     * Call {@link #buildClient(String)} before calling Discord related methods.
     * </p>
     * 
     * @see #buildClient(String)
     * @see #Bot(String)
     * @see #Bot(String, String)
     */
    public Bot()
    {
        new LibConstants();
        log.registerSource(this);
        errorLog.registerSource(this);
        log.start();
        errorLog.start();
        this.guilds = new ConcurrentHashMap<Long, GuildObject>();
        this.creators = new ArrayList<>();
        this.bannedUsers = new ArrayList<>();
    }

    /**
     * Creates a new {@link Bot} instance and builds a {@link IDiscordClient} with the given token.
     * 
     * @param token
     *            The application token.
     * 
     * @see #Bot()
     * @see #Bot(String, String)
     */
    public Bot(String token)
    {
        this(token, "cmd-");
    }

    /**
     * Creates a new {@link Bot} instance and builds a {@link IDiscordClient} with the given token.
     * 
     * @param token
     *            The application token.
     * @param withRecShards
     *            If this is true, the client will be built with the recommended shard count.
     * 
     * @see #Bot()
     * @see #Bot(String, String)
     */
    public Bot(String token, boolean withRecShards)
    {
        this(token, "cmd-", withRecShards);
    }

    /**
     * Creates a new {@link Bot} instance and builds a {@link IDiscordClient} with the given token.
     * 
     * @param token
     *            The application token.
     * @param prefix
     *            The desired prefix for commands.
     * 
     * @see #Bot()
     * @see #Bot(String)
     */
    public Bot(String token, String prefix)
    {
        this(token, prefix, false);
    }

    /**
     * Creates a new {@link Bot} instance and builds a {@link IDiscordClient} with the given token.
     * 
     * @param token
     *            The application token.
     * @param prefix
     *            The desired prefix for commands.
     * @param withRecShards
     *            If this is true, the client will be built with the recommended shard count.
     * 
     * @see #Bot()
     * @see #Bot(String)
     */
    public Bot(String token, String prefix, boolean withRecShards)
    {
        new LibConstants();
        Bot.prefix = prefix;
        log.registerSource(this);
        errorLog.registerSource(this);
        log.start();
        errorLog.start();
        this.token = token;
        this.buildClient(token, withRecShards);
        this.guilds = new ConcurrentHashMap<Long, GuildObject>();
        this.creators = new ArrayList<>();
        this.bannedUsers = new ArrayList<>();
    }

    /**
     * Returns the application ID of the client.
     * 
     * @return
     */
    public String getID()
    {
        return this.client.getApplicationClientID();
    }

    /**
     * Builds a {@link IDiscordClient} with the given token and assigns it to {@link #client}.
     * <p>
     * This called automatically if you use {@link #Bot(String)} or {@link #Bot(String, String)}.
     * </p>
     * 
     * @param token
     *            The application token.
     */
    public void buildClient(String token)
    {
        this.token = token;
        ClientBuilder builder = new ClientBuilder()
                .withToken(token);
        try
        {
            this.client = builder.build();
            UserPermissions.setBot(this);
        }
        catch (DiscordException e)
        {
            errorLog.print(this, "Failed to build the client.");
            errorLog.print(this, e);
        }
    }

    /**
     * Builds a {@link IDiscordClient} with the given token and assigns it to {@link #client}.
     * 
     * If withRecShards is true, the client will be built with the recommended shard count.
     * 
     * @param token
     *            The application token.
     * @param withRecShard
     *            If this is true, the client will be built with the recommended shard count.
     */
    public void buildClient(String token, boolean withRecShards)
    {
        this.token = token;
        ClientBuilder builder = new ClientBuilder()
                .withToken(token);
        if (withRecShards)
        {
            builder.withRecommendedShardCount();
        }
        try
        {
            this.client = builder.build();
            UserPermissions.setBot(this);
        }
        catch (DiscordException e)
        {
            errorLog.print(this, "Failed to build the client.");
            errorLog.print(this, e);
        }
    }

    /**
     * Stops the old presencehandler and sets the given one.
     * 
     * <p>
     * The new handler will be started.
     * </p>
     * 
     * @param presenceHandler
     */
    public void setPresenceHandler(PresenceHandler presenceHandler)
    {
        if (this.presenceHandler != null)
        {
            this.presenceHandler.stop();
        }

        this.presenceHandler = presenceHandler;
        this.presenceHandler.start();
    }

    public PresenceHandler getPresenceHandler()
    {
        return this.presenceHandler;
    }

    /**
     * Sets the PrefixLoader which will be used to load the prefix of each guild.
     * 
     * @param loader
     *            The loader instance.
     */
    public void setPrefixLoader(PrefixLoader loader)
    {
        this.prefixLoader = loader;
    }

    /**
     * Logs the {@link #client} into Discord.
     * 
     * <p>
     * You have to call {@link #buildClient(String)} first.
     * </p>
     * 
     * @throws BowtieClientException
     *             If the client is null.
     */
    public void login() throws BowtieClientException
    {
        log.print(this, "Trying to log into Discord.");
        if (this.client != null)
        {
            this.client.login();
            log.print(this, "Logged in.");
        }
        else
        {
            throw new BowtieClientException("The client has to be built first.");
        }
    }

    /**
     * Logs the {@link #client} out of Discord.
     * 
     * @throws BowtieClientException
     *             If the client is null.
     */
    public void logout() throws BowtieClientException
    {
        log.print(this, "Trying to log out of Discord.");
        checkClient();
        if (this.client.isLoggedIn())
        {
            if (this.presenceHandler != null)
            {
                this.presenceHandler.stop();
            }
            this.client.logout();
            log.print(this, "Offline.");
        }
        else
        {
            throw new BowtieClientException("Attempted to log out a not logged in client.");
        }

    }

    /**
     * Gets the Logger instance for this class.
     * 
     * @return The logger.
     */
    public Logger getLogger()
    {
        return log;
    }

    /**
     * Sets the Logger instance for this class.
     * 
     * @param log
     *            The logger.
     */
    public void setLogger(Logger log)
    {
        Bot.log = log;
    }

    /**
     * Returns the currently set prefix for commands.
     * 
     * @return The prefix.
     */
    public static String getPrefix()
    {
        return Bot.prefix;
    }

    /**
     * Sets the prefix for commands.
     * 
     * @param prefix
     *            The desired prefix.
     */
    public static void setPrefix(String prefix)
    {
        Bot.prefix = prefix;
    }

    /**
     * Gets the {@link #client} of this bot.
     * 
     * @return The client.
     */
    public IDiscordClient getClient()
    {
        return this.client;
    }

    /**
     * Indicates whether the {@link #client} has been built with {@link #buildClient(String)} yet.
     * 
     * @return true if the client has been built.
     */
    public boolean isClientBuilt()
    {
        return this.client != null;
    }

    /**
     * Checks if the {@link #client} has been built yet and throws an exception if it has not.
     * 
     * @throws BowtieClientException
     *             If the client has not been built yet.
     */
    protected void checkClient() throws BowtieClientException
    {
        if (!isClientBuilt())
        {
            throw new BowtieClientException("The client has to be built first.");
        }
    }

    /**
     * Changes the presence of the bot to online, playing and shows the given text.
     * 
     * @param text
     *            The text that should be displayed.
     */
    public void setPlayingText(String text)
    {
        try
        {
            checkClient();
        }
        catch (BowtieClientException e)
        {
            errorLog.print(this, e);
            return;
        }
        RequestBuffer.request(() ->
        {
            try
            {
                this.client.changePresence(StatusType.ONLINE, ActivityType.PLAYING, text);
            }
            catch (RateLimitException rte)
            {
                throw rte;
            }
            catch (Exception e)
            {

            }
        }).get();
    }

    public void setPlayingText(String text, StatusType status, ActivityType activity)
    {
        try
        {
            checkClient();
        }
        catch (BowtieClientException e)
        {
            errorLog.print(this, e);
            return;
        }
        RequestBuffer.request(() ->
        {
            try
            {
                this.client.changePresence(status, activity, text);
            }
            catch (RateLimitException rte)
            {
                throw rte;
            }
            catch (Exception e)
            {

            }
        }).get();
    }

    public void setStreaming(String text, StatusType status, String url)
    {
        try
        {
            checkClient();
        }
        catch (BowtieClientException e)
        {
            errorLog.print(this, e);
            return;
        }
        RequestBuffer.request(() ->
        {
            try
            {
                this.client.changeStreamingPresence(status, text, url);
            }
            catch (RateLimitException rte)
            {
                throw rte;
            }
            catch (Exception e)
            {

            }
        }).get();
    }

    /**
     * Registers the listeners from the array.
     */
    public void addListeners(IListener[] listeners)
    {
        EventDispatcher dispatcher = this.client.getDispatcher();
        for (IListener listener : listeners)
        {
            dispatcher.registerListener(listener);
        }
    }

    /**
     * Registers the listeners from the array.
     */
    public void addListeners(IListener[] listeners, ExecutorService threads)
    {
        EventDispatcher dispatcher = this.client.getDispatcher();
        for (IListener listener : listeners)
        {
            dispatcher.registerListener(threads, listener);
        }
    }

    /**
     * Indicates whether the bot is ready on all shards.
     * 
     * @return true if the bot is ready.
     */
    public boolean isReady()
    {
        return this.isReady;
    }

    /**
     * Sets {@link #isReady}.
     * 
     * <p>
     * True indicates that the bot is ready on all shards.
     * </p>
     * 
     * @param ready
     *            true or false.
     */
    public void setReady(boolean ready)
    {
        this.isReady = ready;
    }

    /**
     * Gets the number of guilds the {@link Bot#client} is connected to.
     * 
     * @return The number of guilds or -1 if the client has not been built yet.
     */
    public int getGuildCount()
    {
        try
        {
            checkClient();
        }
        catch (BowtieClientException e)
        {
            errorLog.print(this, e);
            return -1;
        }
        return this.client.getGuilds().size();
    }

    /**
     * Gets the number of users that are visible to the bot.
     * 
     * @return The number of users or -1 if the client has not been built yet.
     */
    public int getTotalUserCount()
    {
        try
        {
            checkClient();
        }
        catch (BowtieClientException e)
        {
            errorLog.print(this, e);
            return -1;
        }
        return this.client.getUsers().size();
    }

    /**
     * Gets the total number of masters registered to the {@link GuildObject}s.
     * 
     * @return The number of masters.
     */
    public int getTotalMasterCount()
    {
        List<GuildObject> guilds = this.getGuildObjects();
        int count = 0;
        for (GuildObject guild : guilds)
        {
            count += guild.getMasters().size();
        }
        return count;
    }

    /**
     * Gets the total number of owners registered to the {@link GuildObject}s.
     * 
     * @return The number of owners.
     */
    public int getTotalOwnerCount()
    {
        List<GuildObject> guilds = this.getGuildObjects();
        int count = 0;
        for (GuildObject guild : guilds)
        {
            count += guild.getOwners().size();
        }
        return count;
    }

    /**
     * Gets the number of creators that are registered to the bot.
     * 
     * @return The number of creators.
     */
    public int getTotalCreatorCount()
    {
        return this.creators.size();
    }

    /**
     * Creates a {@link GuildObject} for each connected guild and adds them to the {@link #guilds} map.
     * 
     * @return The Map of {@link GuildObject}s or null if the client has not been built yet.
     */
    public Map<Long, GuildObject> createGuildObjects()
    {
        try
        {
            checkClient();
        }
        catch (BowtieClientException e)
        {
            errorLog.print(this, e);
            return null;
        }
        List<IGuild> connectedGuilds = this.client.getGuilds();
        for (IGuild guild : connectedGuilds)
        {
            addGuildObject(new GuildObject(guild));
        }
        log.print(this, "Created " + this.guilds.size() + " guildobjects.");
        return this.guilds;
    }

    /**
     * Gets {@link #guilds} which contains all registered {@link GuildObject}s.
     * 
     * @return A {@link List} containing the registered {@link GuildObject}s.
     */
    public List<GuildObject> getGuildObjects()
    {
        return new ArrayList<GuildObject>(this.guilds.values());
    }

    /**
     * Sets the {@link #guilds}.
     * 
     * @param guilds
     *            A {@link Map} containing the {@link GuildObject}s.
     */
    public void setGuildObjects(Map<Long, GuildObject> guilds)
    {
        this.guilds = guilds;
    }

    /**
     * Adds a single {@link GuildObject} to the {@link #guilds} list.
     * 
     * @param guild
     *            The {@link GuildObect} which should be added.
     * @return true always
     */
    public boolean addGuildObject(GuildObject guild)
    {
        if (!this.guilds.containsKey(guild.getLongID()))
        {
            if (this.prefixLoader != null)
            {
                guild.setPrefix(this.prefixLoader.load(guild));
            }
            else
            {
                guild.setPrefix(Bot.prefix);
            }
            this.guilds.put(guild.getLongID(), guild);
            return true;
        }
        return false;
    }

    /**
     * Remooves the given {@link GuildObject} from the {@link #guilds} list.
     * 
     * @param guild
     *            The {@link GuildObect} which should be removed.
     * @return true if {@link #guilds} contained the given {@link GuildObject} and if it was successfully removed.
     */
    public boolean removeGuildObject(GuildObject guild)
    {
        if (this.guilds.containsKey(guild.getLongID()))
        {
            this.guilds.remove(guild.getLongID());
            return true;
        }
        return false;
    }

    /**
     * Searches the {@link #guilds} map for a {@link GuildObject} with the given ID and returns it.
     * 
     * @param guildID
     *            The String ID of the {@link GuildObject} that should be returned.
     * @return The {@link GuildObject} with the given ID or null if no element in {@link #guilds} has the given ID.
     */
    public GuildObject getGuildObjectByID(String guildID)
    {
        long key = -1;
        try
        {
            key = Long.parseLong(guildID);
        }
        catch (Exception e)
        {
        }

        return this.guilds.get(key);
    }

    /**
     * Gets {@link #bannedUsers} which contains all IDs of banned users.
     * 
     * @return {@link #bannedUsers}.
     */
    public List<String> getBannedUsers()
    {
        return this.bannedUsers;
    }

    /**
     * Checks if the given user is banned from using commands.
     * 
     * @param user
     *            The user which should be checked.
     * @return true if the user is banned.
     */
    public boolean isBanned(IUser user)
    {
        return this.bannedUsers.contains(user.getStringID());
    }

    /**
     * Adds the given user to the {@link #bannedUsers} list which stops him from using any command.
     * 
     * @param user
     *            The user which should be banned.
     * @return true if the user was not yet banned and was successfully added to {@link #bannedUsers}.
     */
    public boolean banUser(IUser user)
    {
        if (!this.bannedUsers.contains(user.getStringID()))
        {
            return this.bannedUsers.add(user.getStringID());
        }
        return false;
    }

    public boolean banUser(String id)
    {
        if (!this.bannedUsers.contains(id))
        {
            return this.bannedUsers.add(id);
        }
        return false;
    }

    /**
     * Removes the given user from {@link #bannedUsers} if he was contained.
     * 
     * @param user
     *            The user which should no longer be banned.
     * @return true if the user was successfully removed from {@link #bannedUsers}.
     */
    public boolean unbanUser(IUser user)
    {
        return this.bannedUsers.remove(user.getStringID());
    }

    /**
     * Checks if the given user is a owner on any of the registered guilds.
     * 
     * @param user
     *            The user which should be checked.
     * @return true if the given user is an owneer on any guild.
     */
    public boolean isOwner(IUser user)
    {
        for (GuildObject guild : getGuildObjects())
        {
            if (guild.isOwner(user))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the given user is a master on any of the registered guilds.
     * 
     * @param user
     *            The user which should be checked.
     * @return true if the given user is a master on any guild.
     */
    public boolean isMaster(IUser user)
    {
        for (GuildObject guild : getGuildObjects())
        {
            if (guild.isMaster(user))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets {@link #creators} which contains all registered creator IDs.
     * 
     * @return {@link #creators}.
     */
    public List<String> getCreators()
    {
        return this.creators;
    }

    /**
     * Checks if the given user is a registered creator.
     * 
     * @param user
     *            The user which should be checked.
     * @return true if the user is a creator.
     */
    public boolean isCreator(IUser user)
    {
        return this.creators.contains(user.getStringID());
    }

    public boolean isAppOwner(IUser user)
    {
        return this.appOwner.equals(user.getStringID());
    }

    public void setAppOwner()
    {
        this.appOwner = this.client.getApplicationOwner().getStringID();
    }

    /**
     * Adds the given user to the {@link #creators} list which enables him to use every command.
     * 
     * @param user
     *            The user which should be added as a creator.
     * @return true if the user was not yet a creator and was successfully added to {@link #creators}.
     */
    public boolean addCreator(IUser user)
    {
        if (!this.creators.contains(user.getStringID()))
        {
            return this.creators.add(user.getStringID());
        }
        return false;
    }

    /**
     * Adds the given id to the {@link #creators} list which enables the user to use every command.
     * 
     * @param id
     *            The user id which should be added as a creator.
     * @return true if the user was not yet a creator and was successfully added to {@link #creators}.
     */
    public boolean addCreator(String id)
    {
        if (!this.creators.contains(id))
        {
            return this.creators.add(id);
        }
        return false;
    }

    /**
     * Loads the ID's from the 'creators' field in the property file.
     * <p>
     * ID's loaded by this are treated as creators and are able to use any command on any guild.
     * </p>
     * 
     * @return The number of creators or -1 if the client has not been built yet.
     */
    public int loadCreators()
    {
        try
        {
            checkClient();
        }
        catch (BowtieClientException e)
        {
            errorLog.print(this, e);
            return -1;
        }
        String[] ids = Properties.getValueOf("creators").split(" ");
        for (String id : ids)
        {
            this.addCreator(id);
        }
        log.print(this, getCreators().size() > 1 || this.getCreators().size() == 0
                ? "Registered " + this.getCreators().size() + " creators."
                : "Registered " + this.getCreators().size() + " creator.");
        return this.getCreators().size();
    }

    /**
     * Removes the given user from {@link #creators} if he was contained.
     * 
     * @param user
     *            The user which should no longer be a creator.
     * @return true if the user was successfully removed from {@link #creators}.
     */
    public boolean removeCreator(IUser user)
    {
        return this.creators.remove(user.getStringID());
    }

    /**
     * Gets the application token the bot is using to log into Discord with.
     * 
     * @return The application token.
     */
    public String getToken()
    {
        return this.token;
    }

    public IMessage sendMessage(EmbedObject embed, IChannel channel)
    {
        try
        {
            checkClient();
        }
        catch (BowtieClientException e)
        {
            e.printStackTrace();
            return null;
        }

        try
        {
            return RequestBuffer.request(new IRequest<IMessage>()
            {
                @Override
                public IMessage request()
                {
                    IMessage msg = null;

                    try
                    {
                        msg = channel.sendMessage(embed);
                    }
                    catch (RateLimitException rte)
                    {
                        throw rte;
                    }
                    catch (MissingPermissionsException mpe)
                    {
                        return null;
                    }
                    catch (Exception e)
                    {
                        throw e;
                    }

                    return msg;
                }
            }).get();
        }
        catch (Exception e)
        {
            errorLog.print(this, e);
        }
        return null;
    }

    public IMessage sendMessage(String text, EmbedObject embed, IChannel channel)
    {
        try
        {
            checkClient();
        }
        catch (BowtieClientException e)
        {
            e.printStackTrace();
            return null;
        }

        try
        {
            return RequestBuffer.request(new IRequest<IMessage>()
            {
                @Override
                public IMessage request()
                {
                    IMessage msg = null;

                    try
                    {
                        msg = channel.sendMessage(text, embed);
                    }
                    catch (RateLimitException rte)
                    {
                        throw rte;
                    }
                    catch (MissingPermissionsException mpe)
                    {
                        return null;
                    }
                    catch (Exception e)
                    {

                    }

                    return msg;
                }
            }).get();
        }
        catch (Exception e)
        {
            errorLog.print(this, e);
        }
        return null;
    }

    /**
     * Sends an embedded message to the given channel.
     * 
     * @param message
     *            The text of the message.
     * @param channel
     *            The channel to which the message should be sent. Can be a private channel.
     * @param color
     *            The color of the embeded message.
     * @param icon
     *            The icon that will be displayed in the foootnote.
     * @param fast
     *            true if the message should be sent directly, false if it should be added to the {@link RequestBuffer}.
     *            Sending it directly can mean that message wont be sent at all if the rate limit is exceeded.
     * @return
     * 
     * @see #sendMessage(String, IChannel)
     * @see #sendMessage(String, IChannel, boolean)
     * @see #sendMessage(String, IChannel, Color)
     * @see #sendMessage(String, IChannel, String)
     * @see #sendMessage(String, IChannel, Color, boolean)
     * @see #sendMessage(String, IChannel, Color, String)
     * @see #sendMessage(String, IChannel, String, boolean)
     */
    public IMessage sendMessage(String message, IChannel channel, Color color, String icon, boolean fast)
    {
        try
        {
            checkClient();
        }
        catch (BowtieClientException e)
        {
            e.printStackTrace();
            return null;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.withDesc(message);
        builder.withColor(color);
        if (icon != null)
        {
            builder.withThumbnail(icon);
        }
        try
        {
            if (fast)
            {
                channel.sendMessage(builder.build());
            }
            else
            {
                return RequestBuffer.request(new IRequest<IMessage>()
                {
                    @Override
                    public IMessage request()
                    {
                        IMessage message = null;
                        try
                        {
                            message = channel.sendMessage(builder.build());
                        }
                        catch (RateLimitException rte)
                        {
                            throw rte;
                        }
                        catch (MissingPermissionsException mpe)
                        {
                            return null;
                        }
                        catch (Exception e)
                        {

                        }
                        return message;
                    }
                }).get();
            }
        }
        catch (Exception e)
        {
            errorLog.print(this, e);
        }
        return null;
    }

    /**
     * Sends an embedded message to the given channel.
     * 
     * @param message
     *            The text of the message.
     * @param channel
     *            The channel to which the message should be sent. Can be a private channel.
     * @param color
     *            The color of the embeded message.
     * @param fast
     *            true if the message should be sent directly, false if it should be added to the {@link RequestBuffer}.
     *            Sending it directly can mean that message wont be sent at all if the rate limit is exceeded.
     * 
     * @see #sendMessage(String, IChannel, Color, String, boolean)
     */
    public IMessage sendMessage(String message, IChannel channel, Color color, boolean fast)
    {
        return sendMessage(message, channel, color, null, fast);
    }

    /**
     * Sends an embedded message to the given channel.
     * 
     * @param message
     *            The text of the message.
     * @param channel
     *            The channel to which the message should be sent. Can be a private channel.
     * 
     * @see #sendMessage(String, IChannel, Color, String, boolean)
     */
    public IMessage sendMessage(String message, IChannel channel)
    {
        return sendMessage(message, channel, false);
    }

    /**
     * Sends an embedded message to the given channel.
     * 
     * @param message
     *            The text of the message.
     * @param channel
     *            The channel to which the message should be sent. Can be a private channel.
     * @param icon
     *            The icon that will be displayed in the foootnote.
     * 
     * @see #sendMessage(String, IChannel, Color, String, boolean)
     */
    public IMessage sendMessage(String message, IChannel channel, String icon)
    {
        return sendMessage(message, channel, Colors.DEFAULT, icon, false);
    }

    /**
     * Sends an embedded message to the given channel.
     * 
     * @param message
     *            The text of the message.
     * @param channel
     *            The channel to which the message should be sent. Can be a private channel.
     * @param color
     *            The color of the embeded message.
     * 
     * @see #sendMessage(String, IChannel, Color, String, boolean)
     */
    public IMessage sendMessage(String message, IChannel channel, Color color)
    {
        return sendMessage(message, channel, color, false);
    }

    /**
     * Sends an embedded message to the given channel.
     * 
     * @param message
     *            The text of the message.
     * @param channel
     *            The channel to which the message should be sent. Can be a private channel.
     * @param color
     *            The color of the embeded message.
     * @param icon
     *            The icon that will be displayed in the foootnote.
     * 
     * @see #sendMessage(String, IChannel, Color, String, boolean)
     */
    public IMessage sendMessage(String message, IChannel channel, Color color, String icon)
    {
        return sendMessage(message, channel, color, icon, false);
    }

    /**
     * Sends an embedded message to the given channel.
     * 
     * @param message
     *            The text of the message.
     * @param channel
     *            The channel to which the message should be sent. Can be a private channel.
     * @param fast
     *            true if the message should be sent directly, false if it should be added to the {@link RequestBuffer}.
     *            Sending it directly can mean that message wont be sent at all if the rate limit is exceeded.
     */
    public IMessage sendMessage(String message, IChannel channel, boolean fast)
    {
        return sendMessage(message, channel, Colors.DEFAULT, fast);
    }

    /**
     * Sends an embedded message to the given channel.
     * 
     * @param message
     *            The text of the message.
     * @param channel
     *            The channel to which the message should be sent. Can be a private channel.
     * @param icon
     *            The icon that will be displayed in the foootnote.
     * @param fast
     *            true if the message should be sent directly, false if it should be added to the {@link RequestBuffer}.
     *            Sending it directly can mean that message wont be sent at all if the rate limit is exceeded.
     * 
     * @see #sendMessage(String, IChannel, Color, String, boolean)
     */
    public IMessage sendMessage(String message, IChannel channel, String icon, boolean fast)
    {
        return sendMessage(message, channel, Colors.DEFAULT, icon, fast);
    }

    /**
     * Creates {@link EmbedObject} from the given messages with the given settings and returns them in a list.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param titles
     *            The titles for each message.
     * @param messages
     *            All the messages that should be listed.
     * @param color
     *            The color of the embeded messages.
     * @param icon
     *            The icon that will be displayed in the foootnotes.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * @return The list of EmbedObjects.
     */
    public List<EmbedObject> createListEmbeds(String title,
            List<String> titles,
            List<String> messages,
            Color color,
            String icon,
            int numberPerMsg,
            boolean inline)
    {
        if (titles.size() != messages.size())
        {
            throw new IllegalArgumentException("Not enough titles for the given messages.");
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setLenient(true);
        builder.withTitle(title);
        if (icon != null)
        {
            builder.withThumbnail(icon);
        }
        builder.withColor(color);
        List<EmbedObject> embedObjects = new ArrayList<EmbedObject>();
        for (int i = 0; i < messages.size(); i ++ )
        {
            boolean clear = false;
            if (numberPerMsg == -1)
            {
                if (builder.getTotalVisibleCharacters() + messages.get(i).length() + Integer.toString(i + 1).length()
                > EmbedBuilder.MAX_CHAR_LIMIT)
                {
                    clear = true;
                }
                else
                {
                    builder.appendField(titles.get(i), messages.get(i), inline);
                }
            }
            else
            {
                if (builder.getFieldCount() < numberPerMsg)
                {
                    builder.appendField(titles.get(i), messages.get(i), inline);
                }
                else
                {
                    clear = true;
                }
            }
            if (clear)
            {
                // builds the full embed
                embedObjects.add(builder.build());
                // resets the builder and continues adding messages to the "new" embed
                builder.withTitle("");
                if (icon != null)
                {
                    builder.withThumbnail(icon);
                }
                builder.withColor(color);
                builder.clearFields();
                builder.appendField(titles.get(i), messages.get(i), inline);
            }
        }
        if (inline)
        {
            // adds empty embeds to avoid weird shifting of the elements
            while (builder.getFieldCount() % 3 != 0)
            {
                builder.appendField("-", "-", true);
            }
        }
        // builds the last embed
        embedObjects.add(builder.build());

        return embedObjects;
    }

    /**
     * Creates {@link EmbedObject} from the given messages with the given settings and returns them in a list.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param titles
     *            The titles for each message.
     * @param messages
     *            All the messages that should be listed.
     * @param icon
     *            The icon that will be displayed in the foootnotes.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * @return The list of EmbedObjects.
     */
    public List<EmbedObject> createListEmbeds(String title,
            List<String> titles,
            List<String> messages,
            String icon,
            int numberPerMsg,
            boolean inline)
    {
        return createListEmbeds(title, titles, messages, Colors.DEFAULT, icon, numberPerMsg, inline);
    }

    /**
     * Creates {@link EmbedObject} from the given messages with the given settings and returns them in a list.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param titles
     *            The titles for each message.
     * @param messages
     *            All the messages that should be listed.
     * @param color
     *            The color of the embeded messages.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * @return The list of EmbedObjects.
     */
    public List<EmbedObject> createListEmbeds(String title,
            List<String> titles,
            List<String> messages,
            Color color,
            int numberPerMsg,
            boolean inline)
    {
        return createListEmbeds(title, titles, messages, color, null, numberPerMsg, inline);
    }

    /**
     * Creates {@link EmbedObject} from the given messages with the given settings and returns them in a list.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param titles
     *            The titles for each message.
     * @param messages
     *            All the messages that should be listed.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * @return The list of EmbedObjects.
     */
    public List<EmbedObject> createListEmbeds(String title,
            List<String> titles,
            List<String> messages,
            int numberPerMsg,
            boolean inline)
    {
        return createListEmbeds(title, titles, messages, Colors.DEFAULT, null, numberPerMsg, inline);
    }

    /**
     * Creates {@link EmbedObject} from the given messages with the given settings and returns them in a list.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param messages
     *            All the messages that should be listed.
     * @param color
     *            The color of the embeded messages.
     * @param icon
     *            The icon that will be displayed in the foootnotes.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * @return The list of EmbedObjects.
     */
    public List<EmbedObject> createListEmbeds(String title,
            List<String> messages,
            Color color,
            String icon,
            int numberPerMsg,
            boolean inline)
    {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setLenient(true);
        builder.withTitle(title);
        if (icon != null)
        {
            builder.withThumbnail(icon);
        }
        builder.withColor(color);
        List<EmbedObject> embedObjects = new ArrayList<EmbedObject>();
        for (int i = 0; i < messages.size(); i ++ )
        {
            boolean clear = false;
            if (numberPerMsg == -1)
            {
                if (builder.getTotalVisibleCharacters() + messages.get(i).length() + Integer.toString(i + 1).length()
                > EmbedBuilder.MAX_CHAR_LIMIT)
                {
                    clear = true;
                }
                else
                {
                    builder.appendField(Integer.toString(i + 1), messages.get(i), inline);
                }
            }
            else
            {
                if (builder.getFieldCount() < numberPerMsg)
                {
                    builder.appendField(Integer.toString(i + 1), messages.get(i), inline);
                }
                else
                {
                    clear = true;
                }
            }
            if (clear)
            {
                // builds the full embed
                embedObjects.add(builder.build());
                // resets the builder and continues adding messages to the "new" embed
                builder.withTitle("");
                if (icon != null)
                {
                    builder.withThumbnail(icon);
                }
                builder.withColor(color);
                builder.clearFields();
                builder.appendField(Integer.toString(i + 1), messages.get(i), inline);
            }
        }
        if (inline)
        {
            // adds empty embeds to avoid weird shifting of the elements
            while (builder.getFieldCount() % 3 != 0)
            {
                builder.appendField("-", "-", true);
            }
        }
        // builds the last embed
        embedObjects.add(builder.build());

        return embedObjects;
    }

    /**
     * Creates {@link EmbedObject} from the given messages with the given settings and returns them in a list.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param messages
     *            All the messages that should be listed.
     * @param icon
     *            The icon that will be displayed in the foootnotes.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * @return The list of EmbedObjects.
     */
    public List<EmbedObject> createListEmbeds(String title,
            List<String> messages,
            String icon,
            int numberPerMsg,
            boolean inline)
    {
        return createListEmbeds(title, messages, Colors.DEFAULT, icon, numberPerMsg, inline);
    }

    /**
     * Creates {@link EmbedObject} from the given messages with the given settings and returns them in a list.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param messages
     *            All the messages that should be listed.
     * @param color
     *            The color of the embeded messages.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * @return The list of EmbedObjects.
     */
    public List<EmbedObject> createListEmbeds(String title,
            List<String> messages,
            Color color,
            int numberPerMsg,
            boolean inline)
    {
        return createListEmbeds(title, messages, color, null, numberPerMsg, inline);
    }

    /**
     * Creates {@link EmbedObject} from the given messages with the given settings and returns them in a list.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param messages
     *            All the messages that should be listed.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * @return The list of EmbedObjects.
     */
    public List<EmbedObject> createListEmbeds(String title,
            List<String> messages,
            int numberPerMsg,
            boolean inline)
    {
        return createListEmbeds(title, messages, Colors.DEFAULT, null, numberPerMsg, inline);
    }

    /**
     * Sends an embedded message with the elements from the list to the given channel. If the list contains more
     * elements than 'numberPerMsg' this method will create another embedded message.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param titles
     *            The titles for each message.
     * @param messages
     *            All the messages that should be listed.
     * @param channel
     *            The channel to which the messages should be sent. Can be a private channel.
     * @param color
     *            The color of the embeded messages.
     * @param icon
     *            The icon that will be displayed in the foootnotes.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * 
     * @see #sendListMessage(String, List, IChannel, int, boolean)
     * @see #sendListMessage(String, List, IChannel, Color, int, boolean)
     * @see #sendListMessage(String, List, IChannel, String, int, boolean)
     */
    public void sendListMessage(String title,
            List<String> titles,
            List<String> messages,
            IChannel channel,
            Color color,
            String icon,
            int numberPerMsg,
            boolean inline)
    {
        try
        {
            checkClient();
        }
        catch (BowtieClientException e)
        {
            e.printStackTrace();
            return;
        }
        List<EmbedObject> embedObjects = createListEmbeds(title, titles, messages, color, icon, numberPerMsg, inline);

        try
        {
            for (EmbedObject embed : embedObjects)
            {
                RequestBuffer.request(new IRequest<IMessage>()
                {
                    @Override
                    public IMessage request()
                    {
                        IMessage message = null;
                        try
                        {
                            message = channel.sendMessage(embed);
                        }
                        catch (RateLimitException rte)
                        {
                            throw rte;
                        }
                        catch (MissingPermissionsException mpe)
                        {
                            return null;
                        }
                        catch (Exception e)
                        {

                        }
                        return message;
                    }
                }).get();
            }
        }
        catch (Exception e)
        {
            errorLog.print(this, e);
        }
    }

    /**
     * Sends an embedded message with the elements from the list to the given channel. If the list contains more
     * elements than 'numberPerMsg' this method will create another embedded message.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param titles
     *            The titles for each message.
     * @param messages
     *            All the messages that should be listed.
     * @param channel
     *            The channel to which the messages should be sent. Can be a private channel.
     * @param icon
     *            The icon that will be displayed in the foootnotes.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * 
     * @see #sendListMessage(String, List, IChannel, Color, String, int, boolean)
     */
    public void sendListMessage(String title,
            List<String> titles,
            List<String> messages,
            IChannel channel,
            String icon,
            int numberPerMsg,
            boolean inline)
    {
        sendListMessage(title, titles, messages, channel, Colors.DEFAULT, icon, numberPerMsg, inline);
    }

    /**
     * Sends an embedded message with the elements from the list to the given channel. If the list contains more
     * elements than 'numberPerMsg' this method will create another embedded message.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param titles
     *            The titles for each message.
     * @param messages
     *            All the messages that should be listed.
     * @param channel
     *            The channel to which the messages should be sent. Can be a private channel.
     * @param color
     *            The color of the embeded messages.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * 
     * @see #sendListMessage(String, List, IChannel, Color, String, int, boolean)
     */
    public void sendListMessage(String title,
            List<String> titles,
            List<String> messages,
            IChannel channel,
            Color color,
            int numberPerMsg,
            boolean inline)
    {
        sendListMessage(title, titles, messages, channel, color, null, numberPerMsg, inline);
    }

    /**
     * Sends an embedded message with the elements from the list. If the list contains more elements than 'numberPerMsg'
     * this method will create another embedded message.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param titles
     *            The titles for each message.
     * @param messages
     *            All the messages that should be listed.
     * @param channel
     *            The channel to which the messages should be sent. Can be a private channel.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * 
     * @see #sendListMessage(String, List, IChannel, Color, String, int, boolean)
     */
    public void sendListMessage(String title,
            List<String> titles,
            List<String> messages,
            IChannel channel,
            int numberPerMsg,
            boolean inline)
    {
        sendListMessage(title, titles, messages, channel, Colors.DEFAULT, null, numberPerMsg, inline);
    }

    /**
     * Sends an embedded message with the elements from the list to the given channel. If the list contains more
     * elements than 'numberPerMsg' this method will create another embedded message.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param messages
     *            All the messages that should be listed.
     * @param channel
     *            The channel to which the messages should be sent. Can be a private channel.
     * @param color
     *            The color of the embeded messages.
     * @param icon
     *            The icon that will be displayed in the foootnotes.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * 
     * @see #sendListMessage(String, List, IChannel, int, boolean)
     * @see #sendListMessage(String, List, IChannel, Color, int, boolean)
     * @see #sendListMessage(String, List, IChannel, String, int, boolean)
     */
    public void sendListMessage(String title,
            List<String> messages,
            IChannel channel,
            Color color,
            String icon,
            int numberPerMsg,
            boolean inline)
    {
        try
        {
            checkClient();
        }
        catch (BowtieClientException e)
        {
            e.printStackTrace();
            return;
        }
        List<EmbedObject> embedObjects = createListEmbeds(title, messages, color, icon, numberPerMsg, inline);

        try
        {
            for (EmbedObject embed : embedObjects)
            {
                RequestBuffer.request(new IRequest<IMessage>()
                {
                    @Override
                    public IMessage request()
                    {
                        IMessage message = null;
                        try
                        {
                            message = channel.sendMessage(embed);
                        }
                        catch (RateLimitException rte)
                        {
                            throw rte;
                        }
                        catch (MissingPermissionsException mpe)
                        {
                            return null;
                        }
                        catch (Exception e)
                        {

                        }
                        return message;
                    }
                }).get();
            }
        }
        catch (Exception e)
        {
            errorLog.print(this, e);
        }
    }

    /**
     * Sends an embedded message with the elements from the list to the given channel. If the list contains more
     * elements than 'numberPerMsg' this method will create another embedded message.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param messages
     *            All the messages that should be listed.
     * @param channel
     *            The channel to which the messages should be sent. Can be a private channel.
     * @param icon
     *            The icon that will be displayed in the foootnotes.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * 
     * @see #sendListMessage(String, List, IChannel, Color, String, int, boolean)
     */
    public void sendListMessage(String title,
            List<String> messages,
            IChannel channel,
            String icon,
            int numberPerMsg,
            boolean inline)
    {
        sendListMessage(title, messages, channel, Colors.DEFAULT, icon, numberPerMsg, inline);
    }

    /**
     * Sends an embedded message with the elements from the list to the given channel. If the list contains more
     * elements than 'numberPerMsg' this method will create another embedded message.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param messages
     *            All the messages that should be listed.
     * @param channel
     *            The channel to which the messages should be sent. Can be a private channel.
     * @param color
     *            The color of the embeded messages.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * 
     * @see #sendListMessage(String, List, IChannel, Color, String, int, boolean)
     */
    public void sendListMessage(String title,
            List<String> messages,
            IChannel channel,
            Color color,
            int numberPerMsg,
            boolean inline)
    {
        sendListMessage(title, messages, channel, color, null, numberPerMsg, inline);
    }

    /**
     * Sends an embedded message with the elements from the list. If the list contains more elements than 'numberPerMsg'
     * this method will create another embedded message.
     * 
     * @param title
     *            The text displayed on top of the first message.
     * @param messages
     *            All the messages that should be listed.
     * @param channel
     *            The channel to which the messages should be sent. Can be a private channel.
     * @param numberPerMsg
     *            The number of elements per message. Or -1 if the maximum number based on the amount of characters
     *            should be used.
     * @param inline
     *            Indicates whether multiple elements should be put in one line.
     * 
     * @see #sendListMessage(String, List, IChannel, Color, String, int, boolean)
     */
    public void sendListMessage(String title,
            List<String> messages,
            IChannel channel,
            int numberPerMsg,
            boolean inline)
    {
        sendListMessage(title, messages, channel, Colors.DEFAULT, null, numberPerMsg, inline);
    }

    /**
     * Sends a standard text message to the given channel.
     * 
     * @param message
     *            The text of the message.
     * @param channel
     *            The channel to which the message should be sent. Can be a private channel.
     * @param fast
     *            true if the message should be sent directly, false if it should be added to the {@link RequestBuffer}.
     *            Sending it directly can mean that message wont be sent at all if the rate limit is exceeded.
     * 
     * @see #sendPlainMessage(String, IChannel)
     */
    public void sendPlainMessage(String message, IChannel channel, boolean fast)
    {
        try
        {
            checkClient();
        }
        catch (BowtieClientException e)
        {
            e.printStackTrace();
            return;
        }
        try
        {
            if (fast)
            {
                channel.sendMessage(message);
            }
            else
            {
                RequestBuffer.request(new IRequest<IMessage>()
                {
                    @Override
                    public IMessage request()
                    {
                        IMessage msg = null;
                        try
                        {
                            msg = channel.sendMessage(message);
                        }
                        catch (RateLimitException rte)
                        {
                            throw rte;
                        }
                        catch (MissingPermissionsException mpe)
                        {
                            return null;
                        }
                        catch (Exception e)
                        {

                        }
                        return msg;
                    }
                }).get();
            }
        }
        catch (Exception e)
        {
            errorLog.print(this, e);
        }
    }

    /**
     * Sends a standard text message to the given channel.
     * 
     * @param message
     *            The text of the message.
     * @param channel
     *            The channel to which the message should be sent. Can be a private channel.
     * 
     * @see #sendPlainMessage(String, IChannel, boolean)
     */
    public void sendPlainMessage(String message, IChannel channel)
    {
        sendPlainMessage(message, channel, false);
    }
}