package sm.algo.cast;

/**
 * User: Oleg
 * Date: 20/6/2004
 * Time: 18:02:32
 * Description: implements Casteljau algorithm.
 */
abstract class clCasteljau implements ifCasteljau {
    static final int m_iMaxIteration = 1000;
    static final double m_dEpsilon = 0.001;
    static final double m_dAlpha = 0.2;
    static final boolean m_bDebug = false;
    final int m_iOrder;
    final int m_iDim; // _iDim  - total number of knots in set
    double[][] m_pKnotB = null;      //  set of predetermined knots

    protected clCasteljau(final int iOrder, final int iDim) {
        m_iOrder = iOrder;
        m_iDim = iDim;
    }

    public final double[][] getPredeterminiedKnots() {
        return m_pKnotB;
    }

    // straight forward procedure
    protected abstract void buildKnots(double[] vec, double[] vX) throws clCasteljauException;

    // just for debugging!!!
    public static void main(final String[] args) throws clCasteljauException {
        /*
        ifCasteljau ck = new clUniCasteljau(4);
        final double [] min = {0.0};
        final double [] max = {1.0};
        ck.setKnotsUniformly(min, max);
        final double [] x = {0.7};
        final double [] d = {0.0};
        ck.mapUsingBackProp(x, d);
        ck = null;      */
        final ifCasteljau ck = new clBiiCasteljau(4);
        final double[] min = {0.0, 0.0};
        final double[] max = {1.0, 1.0};
        ck.setKnotsUniformly(min, max);
        final double[] x = {0.2, 0.9};
        final double[] dd = {0.0, 0.0};
        ck.mapUsingBackProp(x, dd);
    }

}

