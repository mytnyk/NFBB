package sm.base.pm;

import sm.base.data.ifMatrixData;

import java.io.IOException;

/**
 * User: Oleg
 * Date: Jun 16, 2004
 * Time: 5:45:02 PM
 * Description: this basic interface is intended to handle data with ports
 */
public interface ifPortHandler {
    String getPortDescription();

    void setPortDescription(String s);

    /**
     * Gets the i-Data from source (port)
     */
    ifMatrixData getData(int i) throws IOException;

    /**
     * Gets the array if descriptions of Data from source (port)
     */
    String[] getDataDesc();

}
