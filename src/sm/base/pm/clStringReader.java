package sm.base.pm;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Oleg
 * Date: 26.02.2006
 * Time: 0:05:31
 * To change this template use File | Settings | File Templates.
 */
public final class clStringReader {
    private final Vector m_sStrings = new Vector();

    public clStringReader(final BufferedReader reader) throws IOException {
        String line;
        int j = 0;
        while ((line = reader.readLine()) != null) {
            m_sStrings.add(j++, line);
        }
    }

    public Vector getStrings() {
        return m_sStrings;
    }

    public String toString() {
        return "Simple string reader";
    }
}
