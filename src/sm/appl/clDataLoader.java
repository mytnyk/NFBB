package sm.appl;

import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.pm.clPlain2DTwinPort;
import sm.base.pm.ifPortHandler;
import sm.base.util.clCancelException;
import sm.base.util.clURL;
import sm.regr.clMatrix;

import java.io.IOException;
import java.net.URL;

/**
 * User: Oleg
 * Date: Jun 23, 2004
 * Time: 2:06:08 PM
 * Description: simple class for loading data
 */
final class clDataLoader implements ifDataLoader {
    private boolean m_bDataLoaded = false;
    private ifMatrixData m_dInput = null;
    private ifVectorData[] m_dInputV = null;
    private ifVectorData m_dOutput = null;
    private int m_iInputs = 0;
    private clCancelException m_cCancel = null;

    private final Thread m_cLoaderThread;
    private final String m_sourceData;

    clDataLoader(final String sourceData) {
        m_sourceData = sourceData;
        m_cLoaderThread = new Thread(this, "Data Loader thread.");
    }

    public int getNubmerOfInputs() {
        return m_iInputs;
    }

    public boolean isDataLoaded() {
        return m_bDataLoaded;
    }

    public ifMatrixData getInput() {
        return m_dInput;
    }

    public ifVectorData getInput(final int i) {
        return m_dInputV[i];
    }

    public ifVectorData getOutput() {
        return m_dOutput;
    }

    public void start() {
        m_cLoaderThread.start();
    }

    public synchronized void run() {
        if (m_bDataLoaded) {
            m_cCancel = new clCancelException("Data has already been loaded!");
            notify();
            return;
        }
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        final ifMatrixData xm;
        final ifMatrixData ym;
        m_cCancel = null;
        final String[] sDataDesc;
        try {
            final ifPortHandler p;
            String correct_filename = clPlain2DTwinPort.CorrectFileName(m_sourceData);
            if (clURL.isURL(correct_filename))
                p = new clPlain2DTwinPort(new URL(correct_filename));
            else
                p = new clPlain2DTwinPort(correct_filename);
            xm = p.getData(0);
            ym = p.getData(1);
            sDataDesc = p.getDataDesc();
        } catch (IOException e) {
            m_cCancel = new clCancelException("Can't load data: " + e.getMessage());
            notify();
            return;
        }
        // main workflow:
        // much more convinient to operate with transparent matrices
        m_dInput = (new clMatrix(xm)).transpose();
        m_iInputs = m_dInput.getRows();
        m_dInputV = m_dInput.getVArrayPtr();
        for (int i = 0; i < m_iInputs; i++) {
            m_dInputV[i].setDataDescription(sDataDesc[i]);
        }

        m_dOutput = (new clMatrix(ym)).transpose().getVectorPtr(0);
        m_dOutput.setDataDescription(sDataDesc[m_iInputs]);
        // actually should not be here
        m_dInput.normalize(0.0, 1.0);
        m_dOutput.normalize(0.0, 1.0);

        m_bDataLoaded = true;
        notify();
    }

    public clCancelException getCancelled() {
        return m_cCancel;
    }
}
