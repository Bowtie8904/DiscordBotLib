package bowt.cons;

import java.awt.Color;
import java.util.Random;

/**
 * Color constants which are mainly used for embed message colors.
 * 
 * @author &#8904
 */
public final class Colors
{
    /** Light blue default color. */
    public static final Color DEFAULT = new Color(114, 137, 218);
    public static final Color RED = new Color(255, 0, 0);
    public static final Color GREEN = new Color(28, 209, 82);
    public static final Color BLUE = new Color(0, 0, 255);
    public static final Color YELLOW = new Color(197, 209, 28);
    public static final Color ORANGE = new Color(255, 154, 0);
    public static final Color PINK = new Color(255, 0, 255);
    public static final Color PURPLE = new Color(133, 32, 201);

    public static Color random()
    {
        Random random = new Random();

        int r = random.nextInt(256);

        try
        {
            Thread.sleep(random.nextInt(200));
        }
        catch (Exception e)
        {
        }

        int g = random.nextInt(256);

        try
        {
            Thread.sleep(random.nextInt(200));
        }
        catch (Exception e)
        {
        }

        int b = random.nextInt(256);

        return new Color(r, g, b);
    }
}