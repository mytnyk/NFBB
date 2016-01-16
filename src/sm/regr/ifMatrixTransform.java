package sm.regr;

import sm.base.data.ifMatrixData;

/**
 * User: Oleg
 * Date: Jun 16, 2004
 * Time: 3:04:19 PM
 * Description: this interface is intended to handle some 2D matrix transformations
 */
public interface ifMatrixTransform extends ifMatrixData {
    ifMatrixTransform transpose();

    ifMatrixTransform copy();

    ifMatrixTransform product(ifMatrixTransform right);

    ifMatrixTransform subtract(ifMatrixTransform right);

    void invert();

    double getVariance();

    ifMatrixTransform getCovariance();

    double trace();

    double absdet();
}
