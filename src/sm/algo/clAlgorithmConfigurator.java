package sm.algo;

/**
 * User: Oleg
 * Date: 18/7/2004
 * Time: 12:10:46
 * Description: This class should provide appropriate configurations
 * for all kinds of available algorithms.
 * There are two main kinds of algorithms:
 * 1) polynomial multirow GMDH
 * 2) Harris alg.
 * <p/>
 * There are different expansion functions for each kind of alg.:
 * 1) linear
 * 2) parabolic
 * 3a) bezier-bernstein parital expansion (only bivariate)
 * 3b) bezier-bernstein full expansion    (bivariate and univariate)
 * <p/>
 * There are some optimizations to alg.:
 * 1) without any opt.
 * 2) fast method on knots
 * 3) GA A-opt. on knots
 * 4) GA D-opt. on knots
 * 5) GA PD-opt. on knots
 * <p/>
 * Also there some kind of partial descriptions (PD) view :
 * 1) only paired variables
 * 2) single and paired variables
 */
final class clAlgorithmConfigurator {
    private static final String[] m_sAlgorithms = {"PM GMDH",
                                                   "Ch. Harris"};
    private static final String[] m_sExpFunctions = {"linear",
                                                     "parabolic",
                                                     "BB partial",
                                                     "BB full"};
    private static final String[] m_sOptimizations = {"no opt.",
                                                      "fast method",
                                                      "GA A-opt",
                                                      "GA D-opt",
                                                      "GA PD-opt"};
    private static final String[] m_sPDViews = {"only paired",
                                                "single & paired"};
    private static final String[][] m_sAvailable = {
        {"PM GMDH", "linear", "no opt.", "only paired"},
        {"PM GMDH", "parabolic", "no opt.", "only paired"},
        {"PM GMDH", "BB partial", "no opt.", "only paired"},
        {"PM GMDH", "BB partial", "fast method", "only paired"},
        {"PM GMDH", "BB partial", "GA A-opt", "only paired"},
        {"PM GMDH", "BB partial", "GA D-opt", "only paired"},
        {"PM GMDH", "BB partial", "GA PD-opt", "only paired"},
        {"Ch. Harris", "BB partial", "no opt.", "only paired"},
        {"Ch. Harris", "BB partial", "fast method", "only paired"},
        {"Ch. Harris", "BB partial", "GA A-opt", "only paired"},
        {"Ch. Harris", "BB partial", "GA D-opt", "only paired"},
        {"Ch. Harris", "BB partial", "GA PD-opt", "only paired"},
    };

    public static final boolean isAvailable(final String sAlgorithm,
                                            final String sExpFunction,
                                            final String sOptimization,
                                            final String sPDView) {
        for (int i = 0; i < m_sAvailable.length; i++) {
            if (m_sAvailable[i][0] == sAlgorithm &&
                m_sAvailable[i][1] == sExpFunction &&
                m_sAvailable[i][2] == sOptimization &&
                m_sAvailable[i][3] == sPDView) {
                return true;
            }
        }
        return false;
    }

    clAlgorithmConfigurator(final String sAlgorithm,
                            final String sExpFunction,
                            final String sOptimization,
                            final String sPDView) {

        /* * Description: GMDH with Linear model view for representation of
* partial descriptions in rows*/
        //new clFunctionLinear()
        /* * Description: GMDH with Parabolic model view for representation of
 * partial descriptions in rows*/  /*
                           new clFunctionParabolic()


        bFullExpansion ?
                new clFunctionFullBB(iPolyDegree, false, null, cb, iIndex) :
                new clFunctionBB(iPolyDegree, false, null, cb, iIndex)


        bFullExpansion ?
                                    new clFunctionFullBB(iPolyDegree, iOptimization == 1, cb, cb, iIndex) :
                                    iOptimization < 2 ?
                                    new clFunctionBB(iPolyDegree, iOptimization == 1, cb, cb, iIndex) :
                                    new clFunctionBBGAOpt(iPolyDegree, iOptimization, cb, cb, iIndex)
                                         */

    }

}
