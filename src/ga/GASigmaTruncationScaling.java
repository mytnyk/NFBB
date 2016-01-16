package ga;

import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 3:14:27 PM
 * Description:
 * ----------------------------------------------------------------------------
 * SigmaTruncationScaling
 * <p/>
 * This scaling object does sigma truncation as defined in goldberg p124.
 * ----------------------------------------------------------------------------
 */

public final class GASigmaTruncationScaling extends GAScalingScheme {
    private float c;  // std deviation multiplier

    public String className() {
        return "GASigmaTruncationScaling";
    }

    public int classID() {
        return SigmaTruncationScaling;
    }

    public GASigmaTruncationScaling() {
        this(gaDefSigmaTruncationMultiplier);
    }

    private GASigmaTruncationScaling(final float m) {
        multiplier(m);
    }

    private GASigmaTruncationScaling(final GASigmaTruncationScaling arg) {
        copy(arg);
    }

    GAScalingScheme assign(final GAScalingScheme arg) {
        copy(arg);
        return this;
    }

    void delete() {
    }

    public Object clone() {
        return new GASigmaTruncationScaling(this);
    }

    /**
     * ----------------------------------------------------------------------------
     * SigmaTruncationScaling
     * <p/>
     * This is an implementation of the sigma truncation scaling method descibed in
     * goldberg p 124.  If the scaled fitness is less than zero, we arbitrarily set
     * it to zero (thus the truncation part of 'sigma truncation').
     * ----------------------------------------------------------------------------
     */
    void evaluate(final GAPopulation p) throws clCancelException {
        for (int i = 0; i < p.size(); i++) {
            double f = (double) p.individual(i).score() - (double) p.ave();
            f += (double) c * (double) p.dev();
            if (f < 0) {
                f = 0.0;
            }
            p.individual(i).fitness((float) f);       // might lose information here!
        }
    }

    void copy(final GAScalingScheme arg) {
        if (arg != this) {
            super.copy(arg);
            c = ((GASigmaTruncationScaling) arg).c;
        }
    }

    /**
     * Set the multiplier for this selection type.  It should be greater than or
     * equal to zero.
     */
    private float multiplier(final float fm) {
        if (fm < 0.0) {
            GAError.GAErr(className(), "multiplier", GAError.gaErrBadSigmaTruncationMult);
            return c;
        }
        return c = fm;
    }

    float multiplier() {
        return c;
    }

}
