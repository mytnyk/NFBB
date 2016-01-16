package sm.base.md;

import sm.base.data.clMatrixData;
import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.util.clCancelException;
import sm.base.util.ifInfoCallBack;
import sm.regr.clMatrix;
import sm.regr.clRegrLSM;
import sm.regr.clRegrLSMEx;
import sm.regr.ifRegression;

/**
 * User: Oleg
 * Date: Jun 17, 2004
 * Time: 5:48:54 PM
 * Description: base class for system identification
 */
public class clBaseSystem implements ifSystem {
    // the following should be the same for all subsystems!
    private static double m_sPercOfTestSamples = c_dDefaultPercentOfTestSamples;

    private String m_sSystemDescription = "Unknown";
    protected double m_dModelFitness = 0.0;
    protected ifVectorData m_dEstimatedOutput = null;
    protected int m_dNumberOfParamters = 0;

    protected final ifInfoCallBack m_cInfoCallBack;
    protected final ifMatrixData m_dInput;
    protected final ifVectorData m_dOutput;

    public clBaseSystem(final ifMatrixData dInput,
                        final ifVectorData dOutput,
                        final ifInfoCallBack cb) {
        m_dInput = dInput;
        m_dOutput = dOutput;
        m_cInfoCallBack = cb;
        m_dNumberOfParamters = m_dInput.getCols();
    }

    public final String getSystemDescription() {
        return m_sSystemDescription;
    }

    public final void setSystemDescription(final String s) {
        m_sSystemDescription = s;
    }

    public final void setRateOfTestSamples(final double dPercOfTestSamples) {
        m_sPercOfTestSamples = dPercOfTestSamples;
    }

    public void identifySystem() throws clCancelException {
        if (m_cInfoCallBack != null) {
            m_cInfoCallBack.infoCallback("Start LSM.");
        }
        final ifRegression ls;
        if (m_sPercOfTestSamples == 0) {
            ls = new clRegrLSM(m_dInput, new clMatrixData(m_dOutput));
        } else {
            ls = new clRegrLSMEx(m_dInput, new clMatrixData(m_dOutput), m_sPercOfTestSamples);
        }

        ls.buildRegression();
        final ifMatrixData eo = ls.getEstimatedOutput();
        m_dEstimatedOutput = (new clMatrix(eo).transpose()).getVectorPtr(0);
        m_dModelFitness = ls.getVariance();
        if (m_cInfoCallBack != null) {
            m_cInfoCallBack.infoCallback("Finish LSM.");
        }
        // clTracer.straceln("Fitness: " + m_dModelFitness);
    }

    public final double getModelEstimation() {
        return m_dModelFitness;
    }

    public int getNumberOfParamters() {
        return m_dNumberOfParamters;
    }

    public final ifVectorData getEstimatedOutput() {
        // override with system description
        m_dEstimatedOutput.setDataDescription(m_sSystemDescription);
        return m_dEstimatedOutput;
    }

    public String toString() {
        return "system";
    }
}
