package bowt.cons;

import bowt.bot.Bot;

/**
 * @author &#8904
 *
 */
public class LibConstants
{
    public static final String VERSION = "3.4.1";
    public static final String LAST_UPDATE = "25.07.2018";

    static
    {
        Bot.log.setPrefix("[Lib]");
        Bot.log.printEmpty();
        Bot.log.print("Bowtie Bot Lib v" + VERSION);
        Bot.log.print("Last updated " + LAST_UPDATE);
        Bot.log.setPrefix("");
    }
}