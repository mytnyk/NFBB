package sm.algo.bp;

/**
 * User: Oleg
 * Date: 20/6/2004
 * Time: 15:02:09
 * Description: Base class for univariate & bivariate Bernstein polynomials
 */
class clBernsteinPolyPack implements ifBernsteinPack {
    /**
     * m_cPoly - array of addresses to clBernsteinPoly objects
     * m_iOrder - order of polynomails
     * m_iDim  - total number of polynomial in pack
     */
    ifPolynomial[] m_cPoly = null;
    final int m_iOrder;
    final int m_iDim;

    protected abstract class clBernsteinPoly implements ifPolynomial {
        /**
         * m_vInstance - polynomail ordinal number in pack
         */
        final int[] m_vInstance;

        protected clBernsteinPoly(final int[] vInstance) {
            m_vInstance = vInstance;
        }
    }

    protected clBernsteinPolyPack(final int iOrder, final int iDim) {
        m_iOrder = iOrder;
        m_iDim = iDim;
    }

    public final int getPolyDim() {
        return m_cPoly.length;
    }

    public final void getVectorOnBarycentric(final double[] v, final double[] vpoly) throws clPolynomialException {
        for (int i = 0; i < m_cPoly.length; i++) {
            vpoly[i] = m_cPoly[i].getValue(v);
        }
    }

    // just for debugging!!!
    public static void main(final String[] args) {
        new clUniBernsteinPolyPack(4);
        new clBiiBernsteinPolyPack(3);
    }
}

