package sm.algo.cast;

/**
 * User: Oleg
 * Date: Jul 16, 2004
 * Time: 12:26:19 PM
 * Description: this interface provides the mechanism for
 * handling the knots in BB approach.
 */
public interface ifKnotsHandling {
    void setKnotsUniformly(double[] vBMin, double[] vBMax) throws clCasteljauException;

    void setPredeterminiedKnots(double[][] vKnots) throws clCasteljauException;

    double[][] getPredeterminiedKnots();
}
