package sm.appl;

import sm.base.data.ifVectorData;
import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jun 25, 2004
 * Time: 4:12:47 PM
 * Description: provides modelling interface
 */
interface ifSystemModeler extends Runnable {
    void start(String sAlgorithmKind,
               double dTestRate,
               int iPolyDegree,
               int iOptimization,
               boolean bFullExpansion);

    ifVectorData getEstimatedOutput();

    double getModelEstimation();

    double getQualityCriterion();

    clCancelException getCancelled();
}
