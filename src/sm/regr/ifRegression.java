package sm.regr;

import sm.base.data.ifMatrixData;

/**
 * User: Oleg
 * Date: Jun 17, 2004
 * Time: 4:01:29 PM
 * Description: this interface is intended to build the regression
 * Y = AX
 */
public interface ifRegression {
    ifMatrixData getRegressors();

    void buildRegression();

    double getVariance();

    ifMatrixData getEstimatedOutput();
}
