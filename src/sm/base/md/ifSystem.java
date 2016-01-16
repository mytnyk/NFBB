package sm.base.md;

import sm.base.data.ifVectorData;
import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jun 17, 2004
 * Time: 5:37:16 PM
 * Description: this interface is intended to handle all operations
 * concerning system identification
 */
public interface ifSystem {
    String getSystemDescription();

    void setSystemDescription(String s);

    void identifySystem() throws clCancelException;

    double getModelEstimation();

    int getNumberOfParamters();

    ifVectorData getEstimatedOutput();

    void setRateOfTestSamples(double dPercOfTestSamples);

    double c_dDefaultPercentOfTestSamples = 0.5;

}
