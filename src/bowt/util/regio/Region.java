package bowt.util.regio;

import java.util.TimeZone;

import sx.blah.discord.handle.obj.IGuild;

/**
 * A utility class to get a fitting {@link TimeZone} for the region of a {@link IGuild}.
 * 
 * @author &#8904
 */
public final class Region
{
    /**
     * Gets a {@link TimeZone} based on its region.
     * <p>
     * Valid regions are:
     * <ul>
     *  <li>Central Europe</li>
     *  <li>Western Europe</li>
     *  <li>Brazil</li>
     *  <li>Hong Kong</li>
     *  <li>Russia</li>
     *  <li>Singapore</li>
     *  <li>Sydney</li>
     *  <li>US Central</li>
     *  <li>US East</li>
     *  <li>US West</li>
     *  <li>Us South</li>
     * </ul>
     * </p>
     * 
     * <p>
     * If you are unsure which region you need take a look at {@link #getTimeZone(IGuild)}.
     * </p>
     * 
     * @param region The name of the region for which you want the timezone.
     * @return A fitting timezone object or a UTC timezone if the given region was not recognized.
     * 
     * @see #getTimeZone(IGuild)
     */
    public static TimeZone getTimeZone(String region)
    {
        switch (region)
        {
            case "Central Europe":
                return TimeZone.getTimeZone("CET");
            case "Western Europe":
                return TimeZone.getTimeZone("WET");
            case "Brazil":
                return TimeZone.getTimeZone("ACT");
            case "Hong Kong":
                return TimeZone.getTimeZone("Asia/Hong_Kong");
            case "Russia":
                return TimeZone.getTimeZone("Europe/Moscow");
            case "Singapore":
                return TimeZone.getTimeZone("Asia/Singapore");
            case "Sydney":
                return TimeZone.getTimeZone("Australia/Sydney");
            case "US Central":
                return TimeZone.getTimeZone("CST");
            case "US East":
                return TimeZone.getTimeZone("EST");
            case "US West":
                return TimeZone.getTimeZone("PST");
            case "US South":
                return TimeZone.getTimeZone("EST");
            default:
                return TimeZone.getTimeZone("UTC");
        }
    }
    
    /**
     * Gets the {@link TimeZone} of the given {@link IGuild} based on its region.
     * 
     * @param guild The discord guild for which the timezone should be found.
     * @return A fitting timezone object or a UTC timezone if the region of the guild was not recognized.
     * 
     * @see #getTimeZone(String)
     */
    public static TimeZone getTimeZone(IGuild guild)
    {
        return Region.getTimeZone(guild.getRegion().getName());
    }
}
