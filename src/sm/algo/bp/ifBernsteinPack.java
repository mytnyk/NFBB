package sm.algo.bp;

/**
 * User: Oleg
 * Date: Jun 21, 2004
 * Time: 1:40:17 PM
 * Description: Base interface for univariate & bivariate Bernstein polynomial pack
 */
public interface ifBernsteinPack {
    void getVectorOnBarycentric(double[] v, double[] vpoly) throws clPolynomialException;

    int getPolyDim();
}
