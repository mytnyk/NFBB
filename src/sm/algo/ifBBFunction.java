package sm.algo;

import sm.algo.cast.ifKnotsHandling;
import sm.base.data.ifMatrixData;
import sm.base.md.ifFunction;
import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 16, 2004
 * Time: 1:04:12 PM
 * Description: this interface describes the BB view of model
 * with ability to handle the knots
 */
public interface ifBBFunction extends ifFunction, ifKnotsHandling {
    /**
     * This is the main chain for building the expansion matrix using
     * polynomial functions based on predetermined knots.
     */
    void buildOnKnots(double[][] vKnots, ifMatrixData md) throws clCancelException;
}
