package ga;

import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 5, 2004
 * Time: 4:03:30 PM
 * Description:
 * ----------------------------------------------------------------------------
 * The base class definition for the selector object defines the interface.
 * Any derived selector must define a clone member and a select member.  If you
 * add any special data members then you should also define a copy member.
 * Any selector can do its business based on fitness or objective scores.  The
 * base selector provides the mechanism for this.  Derived classes can use it if
 * they want to, or ignore it.
 * ----------------------------------------------------------------------------
 */
abstract class GASelectionScheme implements GAID {
    public final boolean sameClass(final GAID b) {
        return classID() == b.classID();
    }

    public String className() {
        return "GASelectionScheme";
    }

    public int classID() {
        return Selection;
    }

    GAPopulation pop;
    int which;      // should we use fitness or objective scores?

    static final int RAW = 0;
    static final int SCALED = 1;

    GASelectionScheme(final int w) {
        which = w;
    }

    GASelectionScheme(final GASelectionScheme orig) {
        copy(orig);
    }

    GASelectionScheme assign(final GASelectionScheme orig) {
        if (orig != this) {
            copy(orig);
        }
        return this;
    }

    abstract void delete();

    public abstract Object clone();

    void copy(final GASelectionScheme orig) {
        pop = orig.pop;
        which = orig.which;
    }

    final void assign(final GAPopulation p) {
        pop = p;
    }

    abstract void update() throws clCancelException;

    abstract GAGenome select();
}