package sm.base.md;

import sm.base.data.ifVectorData;
import sm.base.util.clCancelException;
import sm.base.util.ifInfoCallBack;

/**
 * User: Oleg
 * Date: Jun 18, 2004
 * Time: 1:00:43 PM
 * Description: The class implements the n-paired partial description subsystem
 * func(n inputs) - 1 output
 */
public class clPartDescription extends clBaseSystem implements ifPartDescriptor {
    protected clPartDescription(final ifVectorData[] input,
                                final ifVectorData output,
                                final ifFunction func,
                                final ifInfoCallBack cb) throws clCancelException {
        // apply here function expansion:
        super(func.buildMatrix(input), output, cb);
    }

    /**
     * Method for comparison different partial descriptions between each other
     */
    public final int compare(final Object o1, final Object o2) {

        if (!(o1 instanceof ifPartDescriptor) ||
            !(o2 instanceof ifPartDescriptor)) {
            return 0;
        }
        final ifPartDescriptor po1 = (ifPartDescriptor) o1;
        final ifPartDescriptor po2 = (ifPartDescriptor) o2;

        int res = 0;
        if (po1.getModelEstimation() < po2.getModelEstimation()) {
            res = -1;
        }
        if (po1.getModelEstimation() > po2.getModelEstimation()) {
            res = 1;
        }
        return res;
    }

    public String toString() {
        return "Partial descr. " + getClass().getName();
    }
}
