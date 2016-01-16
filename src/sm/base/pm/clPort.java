package sm.base.pm;

import sm.base.data.ifMatrixData;

import java.io.IOException;
import java.util.List;

/**
 * User: Oleg
 * Date: Jun 17, 2004
 * Time: 2:29:37 PM
 * Description: the basic class is intended to handle port data
 */
class clPort implements ifPortHandler {
    /**
     * The list of objects read
     */
    List m_listData = null;
    String[] m_listDataDesc = null;
    private String m_sPortDescription = "Unknown";

    public final String getPortDescription() {
        return m_sPortDescription;
    }

    public final void setPortDescription(final String s) {
        m_sPortDescription = s;
    }

    public final ifMatrixData getData(final int i) throws IOException {
        final List li = m_listData;
        if (i >= li.size()) {
            throw new IOException("Not enough data to retreive! total = " + li.size());
        }

        return (ifMatrixData) li.get(i);
    }

    public final String[] getDataDesc() {
        return m_listDataDesc;
    }

    public String toString() {
        return "Simple port class " + getClass().getName();
    }
}
