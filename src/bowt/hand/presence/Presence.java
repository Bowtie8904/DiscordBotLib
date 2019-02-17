package bowt.hand.presence;

import org.json.JSONObject;

import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;
import bowt.bot.Bot;
import bowt.json.JSONBuilder;
import bowt.json.Jsonable;

/**
 * @author &#8904
 *
 */
public class Presence implements Jsonable
{
    private StatusType status;
    private ActivityType activity;
    private String text;
    private String url;

    public Presence(StatusType status, ActivityType activity, String text, String url)
    {
        this.status = status;
        this.activity = activity;
        this.text = text;
        this.url = url;
    }

    public Presence(StatusType status, ActivityType activity, String text)
    {
        this.status = status;
        this.activity = activity;
        this.text = text;
    }

    public Presence(StatusType status, String text, String url)
    {
        this.activity = ActivityType.STREAMING;
        this.status = status;
        this.text = text;
        this.url = url;
    }

    public StatusType getStatus()
    {
        return this.status;
    }

    public ActivityType getActivity()
    {
        return this.activity;
    }

    public String getText()
    {
        return this.text;
    }

    public String getURL()
    {
        return this.url;
    }

    public void apply(Bot bot)
    {
        if (this.activity == ActivityType.STREAMING)
        {
            bot.setStreaming(this.text, this.status, this.url);
        }
        else
        {
            bot.setPlayingText(this.text, this.status, this.activity);
        }
    }

    /**
     * @see bowt.util.json.Jsonable#toJSON()
     */
    @Override
    public JSONObject toJSON()
    {
        return new JSONBuilder()
                .put("Status", this.status.toString())
                .put("Activity", this.activity.toString())
                .put("Text", this.text)
                .put("URL", this.url)
                .toJSON();
    }
}