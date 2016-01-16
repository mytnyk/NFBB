package ga;

import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 5, 2004
 * Time: 3:58:10 PM
 * Description:
 * ----------------------------------------------------------------------------
 * Scaling
 * <p/>
 * The scaling object is used to scale the objective scores of a population to
 * avoid clustering and premature convergence (among other things).  See golberg
 * for more about the theory.  This is basically just a container for any data
 * that the scaling object might need to do its thing.  The simplest scalings
 * don't store any data.
 * ----------------------------------------------------------------------------
 */

abstract class GAScalingScheme implements GAID {
    static final float gaDefLinearScalingMultiplier = (float) 1.2;
    static final float gaDefSigmaTruncationMultiplier = (float) 2.0;
    static final float gaDefPowerScalingFactor = (float) 1.0005;
    static final float gaDefSharingCutoff = (float) 1.0;

    public final boolean sameClass(final GAID b) {
        return classID() == b.classID();
    }

    public String className() {
        return "GAScalingScheme";
    }

    public int classID() {
        return Scaling;
    }

    GAScalingScheme() {
    }

    GAScalingScheme(final GAScalingScheme s) {
        copy(s);
    }

    GAScalingScheme assign(final GAScalingScheme s) {
        copy(s);
        return this;
    }

    abstract void delete();

    public abstract Object clone();

    void copy(final GAScalingScheme s) {
    }

    abstract void evaluate(final GAPopulation p) throws clCancelException;
}


