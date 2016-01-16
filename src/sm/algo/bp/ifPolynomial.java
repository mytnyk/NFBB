package sm.algo.bp;

/**
 * User: Oleg
 * Date: 20/6/2004
 * Time: 12:21:38
 * Description: Base interface for univariate & bivariate Bernstein polynomial
 */
interface ifPolynomial {
    double getValue(double[] v) throws clPolynomialException;
}
