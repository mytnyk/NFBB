package sm.gene;

import java.io.IOException;

/**
 * User: Oleg
 * Date: 14/11/2004
 * Time: 11:19:13
 * Description: common interface for data sets
 */
public interface ifDataSet {

    void write2File(final String name) throws IOException;

    double getInput(final int t);

    double getOutput(final int t);
}
