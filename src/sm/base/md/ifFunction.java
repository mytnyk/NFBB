package sm.base.md;

import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jun 18, 2004
 * Time: 1:49:21 PM
 * Description: this interface describes the abstract view of model
 * which is determined expansion function.
 */
public interface ifFunction {
    ifMatrixData buildMatrix(ifVectorData[] vd) throws clCancelException;

}
