package sm.base.util;

/**
 * User: Oleg
 * Date: Jun 16, 2004
 * Time: 1:31:06 PM
 * Description: this utility class is intended to provide simple tracing
 */
public final class clTracer {
    private static final String c_sTracePrefix = "TRACE >> ";

    public static void straceln(final String str) {
        System.out.println(c_sTracePrefix + str);
    }

    public static void strace(final String str) {
        System.out.print(c_sTracePrefix + str);
    }

}
