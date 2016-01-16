package sm.algo.bp;

import sm.base.util.clMathEx;

/**
 * User: Oleg
 * Date: 20/6/2004
 * Time: 16:08:03
 * Description: class implements bivariate polynomials
 */
public final class clBiiBernsteinPolyPack extends clBernsteinPolyPack {

    private final class clBiiBernsteinPoly extends clBernsteinPoly {
        private clBiiBernsteinPoly(final int[] vItem) {
            super(vItem); // use the same ptr
            /*if (vItem.length != 3 || m_iOrder < vItem[0] + vItem[2]) {
                throw new clPolynomialException("Wrong bivariate polynomial construction!");
            } */
        }

        public double getValue(final double[] vec) throws clPolynomialException {
            if (vec.length != 2) {
                throw new clPolynomialException("Wrong bivariate polynomial argument!");
            }

            final double v = vec[0];
            final double u = vec[1];
            final double w = 1.0 - u - v;

            if (u < 0 || u > 1 || v < 0 || v > 1 || w < 0 || w > 1) {
                throw new clPolynomialException("Wrong bivariate polynomial argument!");
            }

            final int m = m_iOrder;
            final int i = m_vInstance[0];
            final int j = m_vInstance[1];
            final int k = m_vInstance[2];

            return (double) (clMathEx.fact(m) / (clMathEx.fact(i) * clMathEx.fact(j) * clMathEx.fact(k)))
                   * clMathEx.pow(u, i) * clMathEx.pow(v, j) * clMathEx.pow(w, k);
        }

        public String toString() {
            return "Bivariate bernstein polynomial " + getClass().getName();
        }
    }

    public clBiiBernsteinPolyPack(final int iOrder) {
        super(iOrder, (iOrder + 1) * (iOrder + 2) >> 1); //only for bivariate

        m_cPoly = new clBiiBernsteinPoly[m_iDim];

        final int[][] vInstance = new int[m_iDim][3];
        int iCounter = 0;
        for (int i = 0; i <= m_iOrder; i++) {
            for (int j = 0; j <= m_iOrder; j++) {
                for (int k = 0; k <= m_iOrder; k++) {
                    if (i + j + k == m_iOrder) {   //  addData new polynomial to the biv pack
                        vInstance[iCounter][0] = i;
                        vInstance[iCounter][1] = j;
                        vInstance[iCounter][2] = k;
                        iCounter += 1;
                    }
                }
            }
        }
        // initialize here the polynomials:
        for (int i = 0; i < m_iDim; i++) {// addData new polynomial to the pack
            m_cPoly[i] = new clBiiBernsteinPoly(vInstance[i]);
        }
    }

    public String toString() {
        return "Bivariate bernstein polynomials pack " + getClass().getName();
    }
}
