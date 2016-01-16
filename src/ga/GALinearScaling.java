package ga;

import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 12:03:34 PM
 * Description:
 * ----------------------------------------------------------------------------
 * LinearScaling
 * <p/>
 * This scaling object does linear scaling as described in goldberg pp 122-124.
 * ----------------------------------------------------------------------------
 */

final class GALinearScaling extends GAScalingScheme {
    private float c;  // linear scaling multiplier

    public String className() {
        return "GALinearScaling";
    }

    public int classID() {
        return LinearScaling;
    }

    GALinearScaling() {
        this(gaDefLinearScalingMultiplier);
    }

    private GALinearScaling(final float fm) {
        multiplier(fm);
    }

    private GALinearScaling(final GALinearScaling arg) {
        copy(arg);
    }

    GAScalingScheme assign(final GAScalingScheme arg) {
        copy(arg);
        return this;
    }

    void delete() {
    }

    public Object clone() {
        return new GALinearScaling(this);
    }

    /**
     * Scale the objective scores in the population.  We assume that the raw
     * evaluation has already taken place (the calculation of the objective scores
     * of the genomes in the population).  We must use a precision higher than that
     * of the genome scores so that we don't lose any information.
     */
    void evaluate(final GAPopulation p) throws clCancelException {
        // Here we calculate the slope and intercept using the multiplier and objective
        // score ranges...

        final double pmin = p.min();
        final double pmax = p.max();
        final double pave = p.ave();

        final double delta;
        final double a;
        final double b;
        if (pave == pmax) {   // no scaling - population is all the same
            a = 1.0;
            b = 0.0;
        } else if (pmin > ((double) c * pave - pmax) / ((double) c - 1.0)) {
            delta = pmax - pave;
            a = ((double) c - 1.0) * pave / delta;
            b = pave * (pmax - (double) c * pave) / delta;
        } else {    // stretch to make min be 0
            delta = pave - pmin;
            a = pave / delta;
            b = -pmin * pave / delta;
        }

        // and now we calculate the scaled scaled values.  Negative scores are not
        // allowed with this kind of scaling, so check for negative values.  If we get
        // a negative value, dump an error message then set all of the scores to 0.

        for (int i = 0; i < p.size(); i++) {
            double f = p.individual(i).score();
            if (f < 0.0) {
                GAError.GAErr(className(), "evaluate", GAError.gaErrNegFitness);
                for (int ii = 0; ii < p.size(); ii++) {
                    p.individual(ii).fitness((float) 0.0);
                }
                return;
            }
            f = f * a + b;
            if (f < 0) {
                f = 0.0; // truncate if necessary (only due to roundoff error)
            }
            p.individual(i).fitness((float) f);       // might lose information here!
        }

    }

    void copy(final GAScalingScheme arg) {
        if (arg != this) {
            super.copy(arg);
            c = ((GALinearScaling) arg).c;
        }
    }

    private float multiplier(final float fm) {
        if (fm <= 1.0) {
            GAError.GAErr(className(), "multiplier", GAError.gaErrBadLinearScalingMult);
            return c;
        }
        return c = fm;
    }

    float multiplier() {
        return c;
    }

}

