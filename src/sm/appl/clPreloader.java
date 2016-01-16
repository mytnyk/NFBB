package sm.appl;

import sm.base.pm.clStringReader;
import sm.base.util.clURL;

import java.io.*;
import java.net.URL;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Oleg
 * Date: 26.02.2006
 * Time: 0:39:44
 * To change this template use File | Settings | File Templates.
 */
final class clPreloader {
    private Vector m_sDataSets = new Vector();

    clPreloader(final String path) {
        try {
            final InputStream is;
            if (clURL.isURL(path))
                is = (new URL(path)).openStream();
            else
                is = new FileInputStream(path);
            
            final InputStreamReader isr = new InputStreamReader(is);
            final BufferedReader reader = new BufferedReader(isr);

            m_sDataSets = (new clStringReader(reader)).getStrings();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public Vector getDataSets() {
        return m_sDataSets;
    }

    public String toString() {
        return "Preloader of available data sets";
    }
}
