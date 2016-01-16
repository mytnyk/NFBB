package sm.appl;

import sm.algo.*;
import sm.base.data.ifVectorData;
import sm.base.md.ifSystem;
import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jun 25, 2004
 * Time: 10:29:52 AM
 * Description: simple class for data identification
 */
final class clSystemModeler implements ifSystemModeler {
    private ifSystem m_cSystem = null;
    private Thread m_cModelerThread = null;
    private clCancelException m_cCancel = null;
    private final ifFullCallBack m_cCallBack;
    private final ifDataLoader m_cDataLoader;
    private int m_iIndex = 0;

    clSystemModeler(final ifDataLoader cDataLoader, final ifFullCallBack cb/*progress*/) {
        m_cDataLoader = cDataLoader;
        m_cCallBack = cb;
    }

    public void start(final String sAlgorithmKind,
                      final double dTestRate,
                      final int iPolyDegree,
                      final int iOptimization,
                      final boolean bFullExpansion) {
        if (sAlgorithmKind.equals(clMethodKind.enGMDHLinear.getName())) {
            m_cSystem = clGMDHAlgorithm.createGMDHLinear(m_cDataLoader.getInput(),
                                                         m_cDataLoader.getOutput(),
                                                         m_cCallBack);
        } else if (sAlgorithmKind.equals(clMethodKind.enGMDHParabolic.getName())) {
            m_cSystem = clGMDHAlgorithm.createGMDHParabolic(m_cDataLoader.getInput(),
                                                            m_cDataLoader.getOutput(),
                                                            m_cCallBack);
        } else if (sAlgorithmKind.equals(clMethodKind.enGMDHBB.getName())) {
            m_cSystem = clGMDHAlgorithm.createGMDHBB(m_cDataLoader.getInput(),
                                                     m_cDataLoader.getOutput(),
                                                     iPolyDegree,
                                                     iOptimization,
                                                     bFullExpansion,
                                                     m_cCallBack,
                                                     m_iIndex);
            m_iIndex++;
        } else if (sAlgorithmKind.equals(clMethodKind.enHarrisBB.getName())) {
            m_cSystem = clHarrisAlgorithm.createGMDHBB(m_cDataLoader.getInput(),
                                                       m_cDataLoader.getOutput(),
                                                       iPolyDegree,
                                                       iOptimization,
                                                       bFullExpansion,
                                                       m_cCallBack,
                                                       m_iIndex);
            m_iIndex++;
        } else if (sAlgorithmKind.equals(clMethodKind.enHarrisOptimal.getName())) {
            m_cSystem = new clHarrisBBOpt(m_cDataLoader.getInput(),
                                          m_cDataLoader.getOutput(),
                                          iPolyDegree,
                                          bFullExpansion,
                                          m_cCallBack,
                                          m_iIndex);
            m_iIndex++;
        } else if (sAlgorithmKind.equals(clMethodKind.enGMDHOptimal.getName())) {
            m_cSystem = new clGMDHBBOpt(m_cDataLoader.getInput(),
                                        m_cDataLoader.getOutput(),
                                        iPolyDegree,
                                        bFullExpansion,
                                        m_cCallBack,
                                        m_iIndex);
            m_iIndex++;
        }

        m_cSystem.setRateOfTestSamples(dTestRate);

        m_cModelerThread = new Thread(this, "System modeler thread.");
        m_cModelerThread.start();
    }

    public synchronized void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        m_cCancel = null;
        try {
            if (m_cSystem == null) {
                throw new clCancelException("The system is empty!");
            }
            m_cSystem.identifySystem();
        } catch (clCancelException e) {
            m_cCancel = e;
        }
        notify();
    }

    public double getModelEstimation() {
        return m_cSystem.getModelEstimation();
    }

    public double getQualityCriterion() {
        int params = m_cSystem.getNumberOfParamters();
        double emprisk = m_cSystem.getModelEstimation();
        int N = m_cDataLoader.getInput().getCols();
        // AIC (Akaike):
        // double aic = N*Math.log(emprisk)+2*params;
        // BIC (Bayesian):
        double bic = N*Math.log(emprisk)+Math.log(N)*params;
        return bic;
    }

    public synchronized ifVectorData getEstimatedOutput() {
        return m_cSystem.getEstimatedOutput();
    }

    public clCancelException getCancelled() {
        return m_cCancel;
    }
}
