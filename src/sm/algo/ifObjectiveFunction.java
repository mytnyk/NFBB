package sm.algo;

import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 14, 2004
 * Time: 4:39:37 PM
 * Description: simple interface for objective function calculation with
 * two-dimensional double array as argument.
 */
interface ifObjectiveFunction {
    double getObjectiveFunctionValue(final double[][] vArgs) throws clCancelException;
}
