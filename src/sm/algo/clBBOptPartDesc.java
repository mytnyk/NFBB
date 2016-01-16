package sm.algo;

import sm.base.data.ifVectorData;
import sm.base.md.clPartDescription;
import sm.base.util.clCancelException;
import sm.base.util.ifProgressCallBack;

/**
 * User: Oleg
 * Date: Jul 16, 2004
 * Time: 12:51:44 PM
 * Description: This class is developed to provide the GA optimization
 * using fitness of partial systems
 */
final class clBBOptPartDesc extends clPartDescription implements ifObjectiveFunction {
    private final ifProgressCallBack m_cUpperFeedBack;
    private final ifBBFunction m_cKnotHandler;

    clBBOptPartDesc(final ifVectorData[] input,
                           final ifVectorData output,
                           final ifBBFunction func,
                           final ifFullCallBack cb) throws clCancelException {
        super(input, output, func, null); // disable the lower callbacking
        m_cUpperFeedBack = cb;
        m_cKnotHandler = func;
    }

    public void identifySystem() throws clCancelException {
        // get standard knots distribution
        final double[][] vKnots = m_cKnotHandler.getPredeterminiedKnots();
        // set this distribution as initial for optimizer
        final clGAOptimizer cGAOptimizer = new clGAOptimizer(vKnots, this, m_cUpperFeedBack);
        // run optimizer
        cGAOptimizer.optimizeDataSet();
        // here we have an optimal vKnots - so set the knots position and
        // estimate final optimal system
        m_cKnotHandler.buildOnKnots(vKnots, m_dInput);
        // than do the final system identification
        super.identifySystem();
    }

    public double getObjectiveFunctionValue(final double[][] vArgs) throws clCancelException {
        // build the model based on arguments (knots)
        // so set current knots:
        m_cKnotHandler.buildOnKnots(vArgs, m_dInput);
        // than identify system with specified input
        super.identifySystem();
        // send estimation of the model (m_dModelFitness) as objective function value
        return m_dModelFitness;
    }

    public String toString() {
        return "Opt partial descr. " + getClass().getName();
    }
}
