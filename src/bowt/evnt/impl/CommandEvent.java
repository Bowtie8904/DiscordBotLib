package bowt.evnt.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IMessage.Attachment;
import sx.blah.discord.handle.obj.IUser;
import bowt.bot.Bot;
import bowt.evnt.BotEvent;
import bowt.guild.GuildObject;

/**
 * A class to transmit command information.
 * 
 * @author &#8904
 */
public class CommandEvent implements BotEvent
{
    /** The {@link GuildObject} which caused this event. */
    private final GuildObject guildObject;

    private final IGuild guild;

    /** The message of the event. */
    private final IMessage message;

    /** The command used in the message that triggered this event. */
    private final String command;

    private final String fixedContent;

    private final String parameterlessContent;

    private Map<String, String> parameters;

    public static String parameterIndicator = "-";

    private final String oneWordParam = "-(\\w+)=(\\w+)";
    private final Pattern PARAMETER_ONE_WORD = Pattern.compile(oneWordParam);

    private final String textParam = "-(\\w+)=\"([^\"]+)\"";
    private final Pattern PARAMETER_TEXT = Pattern.compile(textParam);

    /**
     * Creates a new event instance.
     * 
     * @param guildObject
     *            The guildobject on which the event was caused.
     * @param message
     *            The message of the event.
     */
    public CommandEvent(GuildObject guildObject, IMessage message)
    {
        this.guildObject = guildObject;
        if (message == null)
        {
            this.guild = guildObject.getGuild();
            this.message = null;
            this.command = null;
            this.fixedContent = null;
            this.parameters = null;
            this.parameterlessContent = null;
        }
        else
        {
            this.guild = message.getGuild();
            this.message = message;
            this.command = message.getContent().toLowerCase().split(" ")[0].replace(guildObject.getPrefix(), "");
            this.fixedContent = fixCase(message.getContent());
            this.parameters = findParameters(this.fixedContent);
            this.parameterlessContent = this.fixedContent.replaceAll(textParam, "").replaceAll(oneWordParam, "");
        }
    }

    /**
     * Creates a new event instance.
     * 
     * @param message
     *            The message of the event.
     */
    public CommandEvent(IMessage message)
    {
        if (message == null)
        {
            this.message = null;
            this.guildObject = null;
            this.guild = null;
            this.command = null;
            this.fixedContent = null;
            this.parameters = null;
            this.parameterlessContent = null;
        }
        else
        {
            this.message = message;
            this.guildObject = null;
            this.guild = message.getGuild();
            this.command = message.getContent().toLowerCase().split(" ")[0].replace(Bot.getPrefix(), "");
            this.fixedContent = fixCase(message.getContent());
            this.parameters = findParameters(this.fixedContent);
            this.parameterlessContent = this.fixedContent.replaceAll(textParam, "").replaceAll(oneWordParam, "");
        }
    }

    public String getClientID()
    {
        return this.guild.getClient().getApplicationClientID();
    }

    private Map<String, String> findParameters(String text)
    {
        Map<String, String> params = new HashMap<>();

        Matcher oneWordMat = PARAMETER_ONE_WORD.matcher(text);

        while (oneWordMat.find())
        {
            params.put(oneWordMat.group(1), oneWordMat.group(2));
        }

        Matcher textMat = PARAMETER_TEXT.matcher(text);

        while (textMat.find())
        {
            params.put(textMat.group(1), textMat.group(2));
        }

        return params;
    }

    /**
     * Returns a list with all parts of the command text which started with the set parameter indicator (default is
     * '-').
     * 
     * @return The list of parameters.
     */
    public Map<String, String> getParameters()
    {
        return this.parameters;
    }

    /**
     * Returns the value of the given parameter or null if it doesnt have a value or the parameter is not present.
     * 
     * @param key
     *            The name of the parameter.
     * @return The value of the given parameter or null.
     */
    public String getParameter(String key)
    {
        return this.parameters.get(key);
    }

    /**
     * Gets the command word used in the message that triggered this event.
     * 
     * @return The command word.
     */
    public String getCommand()
    {
        return this.command;
    }

    /**
     * Returns the {@link GuildObject} on which this event was called.
     * 
     * <p>
     * <b>Note</b> <br>
     * This will return null if the event was called from a private channel.
     * </p>
     * 
     * @return the guildobject.
     */
    public GuildObject getGuildObject()
    {
        return guildObject;
    }

    public IGuild getGuild()
    {
        return guild;
    }

    /**
     * Returns the message of the event.
     * 
     * @return the message.
     */
    public IMessage getMessage()
    {
        return message;
    }

    /**
     * Returns the user that caused the event.
     * 
     * @return the user.
     */
    public IUser getAuthor()
    {
        return this.message.getAuthor();
    }

    /**
     * Returns the channel this event was caused on.
     * 
     * @return the channel.
     */
    public IChannel getChannel()
    {
        return this.message.getChannel();
    }

    /**
     * Returns the attachments of the message.
     * 
     * @return the attachments.
     */
    public List<Attachment> getAttachments()
    {
        return this.message.getAttachments();
    }

    /**
     * Returns the mentioned users in the message.
     * 
     * @return the mentioned users.
     */
    public List<IUser> getMentions()
    {
        return this.message.getMentions();
    }

    /**
     * Returns the content of the included message with a complete lower case prefix and command.
     * 
     * @return The fixed text.
     */
    public String getFixedContent()
    {
        return this.fixedContent;
    }

    /**
     * Returns the final content that is left after all parameters have been removed.
     * 
     * @return
     */
    public String getFinalContent()
    {
        return this.parameterlessContent;
    }

    private String fixCase(String text)
    {
        String[] parts = text.split(" ");
        parts[0] = parts[0].toLowerCase();

        StringBuilder sb = new StringBuilder();

        for (String part : parts)
        {
            sb.append(part + " ");
        }

        return sb.toString().trim();
    }
}