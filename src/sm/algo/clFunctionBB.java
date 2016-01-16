package sm.algo;

import sm.algo.bp.clBiiBernsteinPolyPack;
import sm.algo.bp.clPolynomialException;
import sm.algo.bp.clUniBernsteinPolyPack;
import sm.algo.bp.ifBernsteinPack;
import sm.algo.cast.clBiiCasteljau;
import sm.algo.cast.clCasteljauException;
import sm.algo.cast.clUniCasteljau;
import sm.algo.cast.ifCasteljau;
import sm.base.data.clMatrixData;
import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.util.clCancelException;
import sm.base.util.ifProgressCallBack;
import sm.appl.StringResources;

/**
 * User: Oleg
 * Date: Jun 21, 2004
 * Time: 2:52:30 PM
 * Description: this class describes the bezier bernstein view of model
 * and creates corresponding matrix data from a set of vectors X1, X2, ..., XN
 * where Xi - is a vector.
 * e.g. for bivariates:
 * y1 = f(x11, x21)
 * y2 = f(x12, x22)
 * .   .   .
 * yM = f(x1M, x2M)
 * for univariates:
 * y1 = f(x11)
 * y2 = f(x12)
 * .   .   .
 * yM = f(x1M)
 * where f - (uni)bi-variate bezier-bernstein polynomial functions
 */
class clFunctionBB implements ifBBFunction {
    private final ifProgressCallBack m_cFeedBack;
    private final ifKnotCallBack m_cKnotFeedBack;
    private final int m_iPolyOrder;
    private final boolean m_bFastMethod;
    private final int m_iFunctionID;

    // the members below are transient for each call of buildMatrix
    private double[] m_dMin = null;
    private double[] m_dMax = null;
    private ifBernsteinPack m_cBernsteinPack = null;
    private ifCasteljau m_cCasteljau = null;
    private int m_iArgs = 0;
    private int m_iCols = 0;
    private int m_iRows = 0;
    private ifVectorData[] m_cVectorData = null;
    private double[] m_vbar = null;
    private double[] m_vpoly = null;
    private double[] m_vec = null;

    /**
     * @param iPolyOrder  - dimension of bernstein polynomials (usually 1,2,3,4,5)
     * @param bFastMethod - using fast method optimization
     * @param pcb         - callback for user break
     * @param kcb         - callback for knots handling
     * @param iIndex      - just specific value for this function to identify it in algorithm
     */
    clFunctionBB(final int iPolyOrder,
                 final boolean bFastMethod,
                 final ifProgressCallBack pcb,
                 final ifKnotCallBack kcb,
                 final int iIndex) {
        m_cFeedBack = pcb;
        m_cKnotFeedBack = kcb;
        m_iPolyOrder = iPolyOrder;
        m_bFastMethod = bFastMethod;
        m_iFunctionID = iIndex;
    }

    /**
     * @param vd - input set of vectors. vd[i] = Xi, iRows == M, iCols = dimension of bernstein polynomials
     * @return - expanded matrix for bezier-bernstein model
     * @throws clCancelException - provides a machinery for the process interruption
     */
    public ifMatrixData buildMatrix(final ifVectorData[] vd) throws clCancelException {
        m_cVectorData = vd;

        m_iArgs = m_cVectorData.length;
        if (m_iArgs == 2) {   // valid only fo iArgs = 1,2 (uni-, bi- variates)
            m_cBernsteinPack = new clBiiBernsteinPolyPack(m_iPolyOrder);
            m_cCasteljau = new clBiiCasteljau(m_iPolyOrder);
        } else if (m_iArgs == 1) {
            m_cBernsteinPack = new clUniBernsteinPolyPack(m_iPolyOrder);
            m_cCasteljau = new clUniCasteljau(m_iPolyOrder);
        } else {
            throw new clCancelException("Not supported: " + m_iArgs);
        }
        m_iCols = m_cBernsteinPack.getPolyDim();
        m_iRows = m_cVectorData[0].getArraySize();
        final ifMatrixData cMatrixData = new clMatrixData(m_iRows, m_iCols);
        m_vbar = new double[m_iArgs];
        m_vpoly = new double[m_iCols];
        m_vec = new double[m_iArgs];

        // determine here m_dMin and m_dMax vars! and set uniform positions!
        setKnots();  // this may be overriden to support different knot distributions

        if (m_cKnotFeedBack != null) {
            m_cKnotFeedBack.knotCallback(m_iFunctionID, new clMatrixData(m_cCasteljau.getPredeterminiedKnots()));
        }

        // this may be overriden to do e.g. optimization:
        buildOnKnots(m_cCasteljau.getPredeterminiedKnots(), cMatrixData);

        return cMatrixData;
    }

    public void buildOnKnots(final double[][] vKnots, final ifMatrixData md) throws clCancelException {
        try {
            m_cCasteljau.setPredeterminiedKnots(vKnots); // actually unnecessary
            for (int i = 0; i < m_iRows; i++) {
                boolean bContinue = true;
                if (m_cFeedBack != null) {
                    final int p = 100 * i / m_iRows;
                    bContinue = m_cFeedBack.progressCallback(p, StringResources.get(StringResources.progressflow) + p);
                }

                if (!bContinue) {
                    throw new clCancelException(StringResources.get(StringResources.callbackbreak));
                }
                // load current point (x1,x2,..xN) into temporary vector m_vec
                for (int j = 0; j < m_iArgs; j++) {
                    m_vec[j] = m_cVectorData[j].getValue(i);
                }
                // find barycentric coordinates
                if (m_bFastMethod) {
                    m_cCasteljau.mapUsingFastAccess(m_vec, m_vbar);
                } else {
                    m_cCasteljau.mapUsingBackProp(m_vec, m_vbar);
                }
                // calculate polynomial coef. values for the given barycentric m_vbar
                m_cBernsteinPack.getVectorOnBarycentric(m_vbar, m_vpoly);
                // put this coefs. into resulting matrix
                for (int j = 0; j < m_iCols; j++) {
                    md.setValue(i, j, m_vpoly[j]);
                }
            }
            if (m_cFeedBack != null) {
                m_cFeedBack.progressCallback(100, "100%");
            }
            m_cKnotFeedBack.refresh();
        } catch (clPolynomialException e) {
            throw new clCancelException("Polynomial error!" + e);
        } catch (clCasteljauException e) {
            throw new clCancelException("Casteljau error!" + e);
        }
    }

    /**
     * Setting knots with uniform distribution.
     * Your should override this function in order to set knots in some other way.
     * BUT remember than you MAY NOT USE m_bFastMethod = true, i.e. fast building of knots
     */
    private void setKnots() throws clCancelException {
        computeBounds();
        try { // set uniform knots distribution
            m_cCasteljau.setKnotsUniformly(m_dMin, m_dMax);
        } catch (clCasteljauException e) {
            throw new clCancelException("Knots are invalid!" + e);
        }
    }

    /**
     * This one determines m_dMin m_dMax vector values of input data
     */
    private void computeBounds() {
        m_dMin = new double[m_iArgs];
        m_dMax = new double[m_iArgs];
        for (int j = 0; j < m_iArgs; j++) {
            m_dMin[j] = m_dMax[j] = m_cVectorData[j].getValue(0);
            for (int i = 1; i < m_iRows; i++) {
                final double d = m_cVectorData[j].getValue(i);
                if (m_dMin[j] > d) {
                    m_dMin[j] = d;
                } else if (m_dMax[j] < d) {
                    m_dMax[j] = d;
                }
            }
        }
    }

    public final void setKnotsUniformly(final double[] vBMin, final double[] vBMax) throws clCasteljauException {
        // set uniform knots distribution
        m_cCasteljau.setKnotsUniformly(vBMin, vBMax);
    }

    public final void setPredeterminiedKnots(final double[][] vKnots) throws clCasteljauException {
        m_cCasteljau.setPredeterminiedKnots(vKnots);
    }

    public final double[][] getPredeterminiedKnots() {
        return m_cCasteljau.getPredeterminiedKnots();
    }

    public String toString() {
        return "B-B partial";
    }
}
