package sm.algo;

import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.md.*;
import sm.base.pm.clPlain2DTwinPort;
import sm.base.pm.ifPortHandler;
import sm.base.util.clCancelException;
import sm.base.util.clMathEx;
import sm.base.util.clTracer;
import sm.base.util.ifInfoCallBack;
import sm.regr.clMatrix;
import sm.regr.ifMatrixTransform;

import java.io.IOException;

/**
 * User: Oleg
 * Date: Jun 17, 2004
 * Time: 5:53:15 PM
 * Description: implements GMDH algorithm for system identification
 * standard multirow polynomial GMDH algorithm intends on iterative creating
 * of rows
 */
public class clGMDHAlgorithm extends clBaseAlgorithm {

    protected clGMDHAlgorithm(final ifMatrixData input,
                              final ifVectorData output,
                              final ifFunction func,
                              final ifInfoCallBack cb) {
        super(input, output, func, cb);
        setSystemDescription("GMDH " + func.toString());
    }

    /**
     * This is the main procedure for system identification based on GMDH.
     *
     * @throws clCancelException - the identification process may be interrupted by callback
     */
    public final void identifySystem() throws clCancelException {
        clTracer.straceln("--------------------------GMDH Algorithm--------------------------");
        if (m_dInput.getRows() == 1)
        {   // do not need to create row mechanism
            ifSystem finalSystem;
            finalSystem = new clBaseSystem(m_cFunction.buildMatrix(m_dInput.getVArrayPtr()), m_dOutput, m_cInfoCallBack);
            finalSystem.identifySystem();
            m_dNumberOfParamters = finalSystem.getNumberOfParamters();
            m_dEstimatedOutput = finalSystem.getEstimatedOutput();
            m_dModelFitness = finalSystem.getModelEstimation();
        }
        else
        {
            // row building:
            ifRowHandling r = createRow(m_dInput, null); /*first row does not have a parent*/
            m_dNumberOfParamters = r.getNumberOfParamters();
            final int reduceModels = 1;
            do {
                if (r.identifyRow()) {
                    // set current estimated output from the best one or current row r
                    m_dEstimatedOutput = r.getEstimatedOutput();
                    m_dModelFitness = r.getModelEstimation();
                }
            } while ((r = buildNextRow(reduceModels, r)) != null);
        }
        if (m_cInfoCallBack != null) m_cInfoCallBack.infoCallback("");
    }

    /**
     * @param reduceModels - determines how much the next row will be reduced in inputs
     * @param parentRow    - the descriptor for the previous row
     * @return the next builded row
     */
    private ifRowHandling buildNextRow(final int reduceModels, final ifRowHandling parentRow) {
        ifRowHandling nextRow = null;
        try {
            final ifMatrixData dNextInput = parentRow.getNextInput(reduceModels);
            nextRow = createRow(dNextInput, parentRow);
        } catch (clCancelException e) {
            clTracer.straceln("Last row: " + e.getMessage());
            clTracer.straceln("Final estimation: " + clMathEx.formatDouble(m_dModelFitness, 4));
        }
        return nextRow;
    }

    public static ifSystem createGMDHLinear(final ifMatrixData input,
                                            final ifVectorData output,
                                            final ifInfoCallBack cb) {
        return new clGMDHAlgorithm(input, output, new clFunctionLinear(), cb);
    }

    public static ifSystem createGMDHParabolic(final ifMatrixData input,
                                               final ifVectorData output,
                                               final ifInfoCallBack cb) {
        return new clGMDHAlgorithm(input, output, new clFunctionParabolic(), cb);
    }

    /**
     * That is more standard BB GMDH
     *
     * @param input          - input data
     * @param output         - output vector data of the system
     * @param iPolyDegree    - degree of bernstein polynomials used
     * @param iOptimization  -  0 - none; 1 - fast; 2 - A-Opt; 3 - D-Opt
     * @param bFullExpansion - full or partial expansion of BB function
     * @param cb             - complex callback
     * @param iIndex         - alg. identification
     */
    public static ifSystem createGMDHBB(final ifMatrixData input,
                                        final ifVectorData output,
                                        final int iPolyDegree,
                                        final int iOptimization,
                                        final boolean bFullExpansion,
                                        final ifFullCallBack cb,
                                        final int iIndex) {
        return new clGMDHAlgorithm(input, output, bFullExpansion ?
                                                  new clFunctionFullBB(iPolyDegree, iOptimization == 1, cb, cb, iIndex) :
                                                  (iOptimization < 2 ?
                                                   new clFunctionBB(iPolyDegree, iOptimization == 1, cb, cb, iIndex) :
                                                   new clFunctionBBGAOpt(iPolyDegree, iOptimization, cb, cb, iIndex))
                                   , cb);
    }

    public static void main(final String[] args) {
        try {
            // read source:
            final ifPortHandler p = new clPlain2DTwinPort(args[0]);
            final ifMatrixData xm = p.getData(0);
            final ifMatrixData ym = p.getData(1);
            // main workflow:
            // much more convinient to operate with transparent matrices
            final ifMatrixTransform x = (new clMatrix(xm)).transpose();
            final ifMatrixTransform y = (new clMatrix(ym)).transpose();

            final ifVectorData y0 = y.getVectorPtr(0);
            // clFunctionParabolic, clFunctionLinear - for standard GMDH
            // clFunctionBB - for non-standard GMDH
            final ifSystem is = new clGMDHAlgorithm(x, y0, new clFunctionBB(2, false, null, null, 0), null);
            is.identifySystem();

        } catch (IOException e) {
            clTracer.straceln("io error: " + e);
        } catch (clCancelException e) {
            clTracer.straceln("user break: " + e);
        }
    }

    public String toString() {
        return "GMDH " + super.toString();
    }
}
