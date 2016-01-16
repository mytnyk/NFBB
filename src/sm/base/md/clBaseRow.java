package sm.base.md;

import sm.base.data.clMatrixData;
import sm.base.data.clVectorData;
import sm.base.data.ifMatrixData;
import sm.base.data.ifVectorData;
import sm.base.util.clCancelException;
import sm.base.util.clTracer;
import sm.base.util.ifInfoCallBack;
import sm.regr.clMatrix;

import java.util.Arrays;

/**
 * User: Oleg
 * Date: 19/6/2004
 * Time: 13:02:41
 * Description: The class represents the base row of data in any kind of algorithm
 */
public class clBaseRow extends clBaseSystem implements ifRowHandling {
    private final ifPartDescriptor[] m_cPartDesc;
    private final int m_iNumOfPD;
    private final int m_iNumOfVars;
    private final int m_iNumOfSamples;
    private final ifRowHandling m_cParentRow; // just for debugging multirows!!!
    private final int m_iRowNumber;
    protected final ifFunction m_cPDFunction;
    // whether to use part. desc. optimization or not
    //private final boolean m_bPartDescOpt;

    public clBaseRow(final ifMatrixData dInput,
                     final ifVectorData dOutput,
                     final ifFunction func,
                     final ifInfoCallBack cb,
                     final ifRowHandling parentRow) throws clCancelException {
        super(dInput, dOutput, cb);
        m_cPDFunction = func;
        m_cParentRow = parentRow;
        if (m_cParentRow == null) {
            m_iRowNumber = 1;
        } else {
            m_iRowNumber = 1 + m_cParentRow.getRowNumber();
        }

        m_iNumOfVars = m_dInput.getRows();
        m_iNumOfSamples = m_dInput.getCols();
        m_iNumOfPD = m_iNumOfVars * (m_iNumOfVars - 1) >> 1; // c2n(m_iNumOfVars);
        m_cPartDesc = new clPartDescription[m_iNumOfPD];

        int j = 0; // creation of PD
        for (int i = 0; i < m_iNumOfVars; i++) {
            for (int k = i + 1; k < m_iNumOfVars; k++) {
                final ifVectorData[] input = new clVectorData[2];
                input[0] = m_dInput.getVArrayPtr()[i];
                input[1] = m_dInput.getVArrayPtr()[k];
                m_cPartDesc[j] = createPartDescription(input);
                j += 1;
            }
        }

    }

    public boolean identifyRow() throws clCancelException {
        clTracer.straceln(m_iRowNumber + " row identification.");
        if (m_cInfoCallBack != null) {
            m_cInfoCallBack.infoCallback(m_iRowNumber + " row.");
        }
        int j = 0;
        for (int i = 0; i < m_iNumOfVars; i++) {
            for (int k = i + 1; k < m_iNumOfVars; k++) {
                if (m_cInfoCallBack != null) {
                    m_cInfoCallBack.infoCallback(m_iRowNumber + " row: " + (j + 1) + " model.");
                }
                m_cPartDesc[j].identifySystem();
                final double est = m_cPartDesc[j].getModelEstimation();
                j += 1;
                clTracer.straceln(j + " model estimation: " + est);
            }
        }
        // sort the array in ascending order
        Arrays.sort(m_cPartDesc, m_cPartDesc[0]);
        // set current subsystem output to the best one
        m_dEstimatedOutput = m_cPartDesc[0].getEstimatedOutput();
        m_dModelFitness = m_cPartDesc[0].getModelEstimation();

        // compare with the previuos models
        if (m_cParentRow != null && m_dModelFitness > m_cParentRow.getModelEstimation()) {
            clTracer.straceln(" WARNING: previous model is better!");
            return false;
        }
        return true;
    }


    public final int getNumberOfParamters() {
        int c = 0;
        for (int i = 0; i < m_iNumOfPD; i++) {
            c += m_cPartDesc[i].getNumberOfParamters();
        }
        return c;
    }

    /**
     * This one may be overriden to provide another part descriptions
     *
     * @param input - input vectors set
     * @return - part description subsystem instance
     */
    protected ifPartDescriptor createPartDescription(ifVectorData[] input) throws clCancelException {
        return new clPartDescription(input, m_dOutput, m_cPDFunction, m_cInfoCallBack);
    }

    public ifMatrixData getNextInput(final int reduceModels) throws clCancelException {
        // first we have to choose the set of the best models
        final int iFreedomOfChoice = m_iNumOfVars/*m_iNumOfPD*/ - reduceModels;

        if (iFreedomOfChoice <= 1) { // their should be at least two inputs!
            throw new clCancelException("Can't reduce models count anymore!");
        }

        final ifMatrixData dNextInput = new clMatrixData(iFreedomOfChoice, m_iNumOfSamples);

        for (int i = 0; i < iFreedomOfChoice; i++) {
            final ifVectorData pd = m_cPartDesc[i].getEstimatedOutput();
            // copy the row outputs to the next row :
            for (int j = 0; j < m_iNumOfSamples; j++) {
                dNextInput.setValue(i, j, pd.getValue(j));
            }
        }
        return dNextInput;
    }

    public int getRowNumber() {
        return m_iRowNumber;
    }

    public ifSystem getLastRow() throws clCancelException {
        final ifMatrixData dNextInput = (new clMatrix(getNextInput(0))).transpose();
        /* output & callback parse without changes to the final row */
        final ifSystem finalRow = new clBaseSystem(dNextInput, m_dOutput, m_cInfoCallBack);
        return finalRow;
    }

    public String toString() {
        return "Row " + getClass().getName();
    }
}
