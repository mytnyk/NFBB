package sm.regr;

import sm.base.data.clMatrixData;
import sm.base.data.ifMatrixData;

/**
 * User: Oleg
 * Date: Jun 16, 2004
 * Time: 4:33:02 PM
 * Description: this class implements matrix operations with 2D data
 */
public final class clMatrix extends clMatrixData implements ifMatrixTransform {
    private static final boolean m_bDebug = false;

    /**
     * @param md - initializing matrice with Matrix Data
     */
    public clMatrix(final ifMatrixData md) {
        super(md);
    }

    private clMatrix(final int iRows, final int iCols) {
        super(iRows, iCols);
    }

    public ifMatrixTransform transpose() {
        final ifMatrixTransform tr = new clMatrix(getCols(), getRows());
        final double[][] ds = get2DArrayPtr();
        for (int i = 0; i < tr.getRows(); i++) {
            for (int j = 0; j < tr.getCols(); j++) {
                tr.setValue(i, j, ds[j][i]);
            }
        }
        return tr;
    }

    public ifMatrixTransform copy() {
        final ifMatrixTransform co = new clMatrix(getCols(), getRows());
        final double[][] ds = get2DArrayPtr();
        for (int i = 0; i < co.getRows(); i++) {
            for (int j = 0; j < co.getCols(); j++) {
                co.setValue(i, j, ds[i][j]);
            }
        }
        return co;
    }

    public ifMatrixTransform product(final ifMatrixTransform right) {
        final ifMatrixTransform pr = new clMatrix(getRows(), right.getCols());
        final double[][] dl = get2DArrayPtr();
        final double[][] dr = right.get2DArrayPtr();
        for (int i = 0; i < pr.getRows(); i++) {
            for (int j = 0; j < pr.getCols(); j++) {
                double sum = 0.0;
                for (int k = 0; k < getCols(); k++) {
                    sum += dl[i][k] * dr[k][j];
                }
                pr.setValue(i, j, sum);
            }
        }
        return pr;
    }

    public ifMatrixTransform subtract(final ifMatrixTransform right) {
        final ifMatrixTransform sb = new clMatrix(getRows(), right.getCols());
        final double[][] dl = get2DArrayPtr();
        final double[][] dr = right.get2DArrayPtr();
        for (int i = 0; i < sb.getRows(); i++) {
            for (int j = 0; j < sb.getCols(); j++) {
                sb.setValue(i, j, dl[i][j] - dr[i][j]);
            }
        }
        return sb;
    }

    public void invert() {
        final int size = getRows(); // only for square matrix
        int i;
        int j;
        int k;
        final double[][] data = get2DArrayPtr();
        if (size <= 0) {
            return;  // sanity check
        }
        if (size == 1) {
            return;  // must be of dimension >= 2
        }
        for (i = 1; i < size; i++) {
            data[0][i] /= data[0][0]; // normalize row 0
        }
        for (i = 1; i < size; i++) {
            for (j = i; j < size; j++) { // do a column of L
                double sum = 0.0;
                for (k = 0; k < i; k++) {
                    sum += data[j][k] * data[k][i];
                }
                data[j][i] -= sum;
            }
            if (i == size - 1) {
                continue;
            }
            for (j = i + 1; j < size; j++) {  // do a row of U
                double sum = 0.0;
                for (k = 0; k < i; k++) {
                    sum += data[i][k] * data[k][j];
                }
                data[i][j] = (data[i][j] - sum) / data[i][i];
            }
        }
        for (i = 0; i < size; i++)  // invert L
        {
            for (j = i; j < size; j++) {
                double x = 1.0;
                if (i != j) {
                    x = 0.0;
                    for (k = i; k < j; k++) {
                        x -= data[j][k] * data[k][i];
                    }
                }
                data[j][i] = x / data[j][j];
            }
        }
        for (i = 0; i < size; i++)   // invert U
        {
            for (j = i; j < size; j++) {
                if (i == j) {
                    continue;
                }
                double sum = 0.0;
                for (k = i; k < j; k++) {
                    sum += data[k][j] * (i == k ? 1.0 : data[i][k]);
                }
                data[i][j] = -sum;
            }
        }
        for (i = 0; i < size; i++)   // final inversion
        {
            for (j = 0; j < size; j++) {
                double sum = 0.0;
                for (k = i > j ? i : j; k < size; k++) {
                    sum += (j == k ? 1.0 : data[j][k]) * data[k][i];
                }
                data[j][i] = sum;
            }
        }
    }

    public double getVariance() {
        double dNorm = 0.0;
        final double[][] data = get2DArrayPtr();
        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getCols(); j++) {
                dNorm += data[i][j] * data[i][j];
            }
        }
        dNorm = dNorm / (double) (getRows() * getCols());
        return dNorm;
    }

    public ifMatrixTransform getCovariance() {
        final ifMatrixTransform xTr = transpose();
        final ifMatrixTransform xPr = xTr.product(this); // get inform. matrix
        if (m_bDebug) {
            xPr.dumpData();
        }
        xPr.invert(); // get cov. matrix
        if (m_bDebug) {
            xPr.dumpData();
        }
        // here we have xPr - covariance matrix!!!
        return xPr;
    }

    public double trace() {
        // calculate the trace:
        double dTraceValue = 0.0;
        final double[][] data = get2DArrayPtr();
        for (int i = 0; i < getRows(); i++) {
            dTraceValue += data[i][i];
        }
        return dTraceValue;
    }

    public double absdet() {
        // does not work correctly!!! -> sometimes with or w/o "-" (opposite value)
        // correct with abs value!!!
        // fist copy the matrix
        final ifMatrixTransform cCopy = copy();
        final double[][] data = cCopy.get2DArrayPtr();
        final int size = getRows(); // only for square matrix
        int ipass, imx, icol, irow;
        double det, temp, pivot, factor = 0.0;
        // COMPUTATION //
        // THE CURRENT PIVOT ROW IS IPASS, FOR EACH PASS, FIRST FIND THE MAXIMUM
        // ELEMENT IN THE PIVOT COLUMN
        det = 1.0;
        for (ipass = 0; ipass < size; ipass++) {
            imx = ipass;
            for (irow = ipass; irow < size; irow++) {
                if (Math.abs(data[ipass][irow]) > Math.abs(data[ipass][imx])) {
                    imx = irow;
                }
            }
            // INTERCHANGE THE ELEMENTS OF ROW IPASS AND ROW IMX IN BOTH A AND AINV
            if (imx != ipass) {
                for (icol = 0; icol < size; icol++) {
                    if (icol >= ipass) {
                        temp = data[icol][ipass];
                        data[icol][ipass] = data[icol][imx];
                        data[icol][imx] = temp;
                    }
                }
            }
            // THE CURRENT PIVOT IS NOW data[IPASS][IPASS]  //
            // THE DETRMINANT IS THE PRODUCT OF THE PIVOT ELEMENTS

            pivot = data[ipass][ipass];

            det *= pivot; // pivot  -  not an eigen value!!!hm.. why?
            if (det == 0) return det;

            for (icol = 0; icol < size; icol++) {
                //NORMALIZING PIVOT ROW BY DIVIDING ACCROSS BY
                //THE PIVOT ELEMENT//
                if (icol >= ipass) data[icol][ipass] /= pivot;
            }
            for (irow = 0; irow < size; irow++) {
                // NOW REPLACE EACH ROW BY THE ROW PLUS A MULTIPLE OF THE PIVOT
                // ROW WITH A FACTOR CHOSEN SO THAT THE ELEMNT OF A ON THE
                // PIVOT COLUMN IS 0
                if (irow != ipass) factor = data[ipass][irow];
                for (icol = 0; icol < size; icol++) {
                    if (irow != ipass) data[icol][irow] -= factor * data[icol][ipass];
                }
            }
        }// for ipass
        return Math.abs(det);
    }

    public String toString() {
        return "Matrix handling class " + getClass().getName();
    }

}
