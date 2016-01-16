package sm.algo.cast;

/**
 * User: Oleg
 * Date: 20/6/2004
 * Time: 18:12:48
 * Description: interface to Casteljau algorithm.
 */
public interface ifCasteljau extends ifKnotsHandling {
    // inverse procedure using backpropagation
    void mapUsingBackProp(double[] vec, double[] vs) throws clCasteljauException;

    // inverse procedure using new fast access alg. due to spec. knots!
    void mapUsingFastAccess(double[] vec, double[] vs) throws clCasteljauException;
}
