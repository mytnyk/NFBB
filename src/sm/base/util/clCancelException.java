package sm.base.util;

/**
 * User: Oleg
 * Date: Jun 24, 2004
 * Time: 2:28:16 PM
 * Description: Exception class for interruption processes
 */
public final class clCancelException extends Exception {
    public clCancelException(final String s) {
        super(s);
    }
}
