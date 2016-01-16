package sm.algo;

import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.md.clBaseAlgorithm;
import sm.base.md.clBaseSystem;
import sm.base.md.ifRowHandling;
import sm.base.md.ifSystem;
import sm.base.pm.clPlain2DTwinPort;
import sm.base.pm.ifPortHandler;
import sm.base.util.clCancelException;
import sm.base.util.clMathEx;
import sm.base.util.clTracer;
import sm.regr.clMatrix;
import sm.regr.ifMatrixTransform;

import java.io.IOException;

/**
 * User: Oleg
 * Date: 20/6/2004
 * Time: 17:07:52
 * Description: the class implements Bezier Bernstein ident. algorithm
 */
public class clHarrisAlgorithm extends clBaseAlgorithm {

    protected clHarrisAlgorithm(final ifMatrixData dInput,
                                final ifVectorData dOutput,
                                final ifBBFunction func,
                                final ifFullCallBack cb) {
        super(dInput, dOutput, func, cb);
        setSystemDescription("Harris " + func.toString());
    }

    public final void identifySystem() throws clCancelException {
        clTracer.straceln("--------------------------Harris Algorithm--------------------------");
        // if we have more than 2 inputs we must build the rows
        ifSystem finalSystem;
        if (m_dInput.getRows() <= 2)
        {
            finalSystem = new clBaseSystem(m_cFunction.buildMatrix(m_dInput.getVArrayPtr()), m_dOutput, m_cInfoCallBack);
            m_dNumberOfParamters = finalSystem.getNumberOfParamters();
        }
        else
        {
            // row building:
            final ifRowHandling r = createRow(m_dInput, null); /*first row does not have a parent*/
            m_dNumberOfParamters = r.getNumberOfParamters();
            r.identifyRow();
            finalSystem = r.getLastRow();
        }
        // set system output from the final row!
        finalSystem.identifySystem();
        m_dEstimatedOutput = finalSystem.getEstimatedOutput();
        m_dModelFitness = finalSystem.getModelEstimation();
        clTracer.straceln("Final estimation: " + clMathEx.formatDouble(m_dModelFitness, 4));
        if (m_cInfoCallBack != null) m_cInfoCallBack.infoCallback("");
    }


    /**
     * That is more standard Harris BB
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
        return new clHarrisAlgorithm(input, output, bFullExpansion ?
                                                    new clFunctionFullBB(iPolyDegree, iOptimization == 1, cb, cb,
                                                                         iIndex) :
                                                    (iOptimization < 2 ?
                                                     new clFunctionBB(iPolyDegree, iOptimization == 1, cb, cb, iIndex) :
                                                     new clFunctionBBGAOpt(iPolyDegree, iOptimization, cb, cb, iIndex))
                                     , cb);
    }


    // just for debugging!!!
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

            final ifSystem is = new clHarrisAlgorithm(x, y0, new clFunctionBB(2, false, null, null, 0), null);
            is.identifySystem();

        } catch (IOException e) {
            clTracer.straceln("io error: " + e);
        } catch (clCancelException e) {
            clTracer.straceln("user break! " + e);
        }

    }

    public String toString() {
        return "Harris " + super.toString();
    }
}
