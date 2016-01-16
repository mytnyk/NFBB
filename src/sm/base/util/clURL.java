package sm.base.util;

/**
 * Created by IntelliJ IDEA.
 * User: Mytnyk
 * Date: 16/2/2007
 * Time: 14:15:07
 * To change this template use File | Settings | File Templates.
 */
public final class clURL {
    public static boolean isURL(final String str) {
        return str.toLowerCase().startsWith("http");
    }
}
