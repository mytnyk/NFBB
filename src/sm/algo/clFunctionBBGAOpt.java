package sm.algo;

import sm.base.data.ifMatrixData;
import sm.base.util.clCancelException;
import sm.base.util.ifProgressCallBack;
import sm.regr.clMatrix;
import sm.regr.ifMatrixTransform;


/**
 * User: Oleg
 * Date: Jul 14, 2004
 * Time: 3:38:55 PM
 * Description: class for GA optimizations of the knot positions
 * in bezier bernstein modeling.
 */
final class clFunctionBBGAOpt extends clFunctionBB {
    private final ifProgressCallBack m_cUpperFeedBack;
    private final int m_iOptimization; // 1 - A-opt, 2 - D-opt

    private ifMatrixData m_cMatrixData = null; // temporary buffer!

    /**
     * @param iPolyOrder    - dimension of bernstein polynomials (usually 1,2,3,4,5)
     * @param iOptimization - the optimization kind that should be applied
     * @param pcb           - callback for user break
     * @param kcb           - callback for knots
     * @param iIndex        - just specific value for this function to identify it in algorithm
     */
    clFunctionBBGAOpt(final int iPolyOrder,
                      final int iOptimization,
                      final ifProgressCallBack pcb,
                      final ifKnotCallBack kcb,
                      final int iIndex) {
        super(iPolyOrder, false, null, kcb, iIndex); // fast method is unavailable for this kind of optim.
        // intersept progress feedback by this function to provide high level feedback
        m_cUpperFeedBack = pcb;
        m_iOptimization = iOptimization;
    }

    /**
     * This function is overriden in order to intersept standard behaviour
     * of this function and provide the GA optimization
     *
     * @param vKnots - given knots, also return the optimal knot set
     * @param md     - return the optimal matrix data
     * @throws clCancelException - available fo user break
     */
    public void buildOnKnots(final double[][] vKnots, final ifMatrixData md) throws clCancelException {

        final ifObjectiveFunction optimizer;

        if (m_iOptimization == 2) {
            optimizer = new clAOptimizator(); // estimate covariance trace : tr(cCurr*cCurr_1)
        } else if (m_iOptimization == 3) {
            optimizer = new clDOptimizator(); // estimate covariance det : det(cCurr*cCurr_1)
        } else {
            throw new clCancelException("Not supported optimization  - parameter = " + m_iOptimization);
        }

        final clGAOptimizer cGAOptimizer = new clGAOptimizer(vKnots, optimizer, m_cUpperFeedBack);

        m_cMatrixData = md;
        // get optimal knot set! into vKnots
        cGAOptimizer.optimizeDataSet();
        // build the final optimal matrix with optimal knot set!
        super.buildOnKnots(vKnots, md);
        // clTracer.straceln("Best opt = " + optimizer.getObjectiveFunctionValue(vKnots));
        if (m_cUpperFeedBack != null) {
            m_cUpperFeedBack.progressCallback(100, "100%");
        }
    }

    /**
     * @param vKnots - given knots
     * @return - covariance matrix built on these knots
     * @throws clCancelException - may be interrupted by user
     */
    private ifMatrixTransform buildCovOnKnots(final double[][] vKnots) throws clCancelException {

        super.buildOnKnots(vKnots, m_cMatrixData);
        return new clMatrix(m_cMatrixData).getCovariance();
    }

    public String toString() {
        return "B-B GA Opt";
    }

    /**
     * Objective function - trace of covariance matrix build on knots by BB
     */
    private final class clAOptimizator implements ifObjectiveFunction {
        public double getObjectiveFunctionValue(final double[][] vArgs) throws clCancelException {

            final double y = buildCovOnKnots(vArgs).trace();
            return y;
        }
    }

    /**
     * Objective function - determinant of covariance matrix build on knots by BB
     */
    private final class clDOptimizator implements ifObjectiveFunction {
        public double getObjectiveFunctionValue(final double[][] vArgs) throws clCancelException {

            final double y = buildCovOnKnots(vArgs).absdet();
            return y;
        }
    }

}