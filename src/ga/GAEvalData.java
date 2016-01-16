package ga;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 12:02:42 PM
 * DESCRIPTION:
 * This is the basic interface for the object that contains evaluation data.  It
 * can be used with genomes and/or populations in combination with their
 * respective evaluation methods.
 * ----------------------------------------------------------------------------
 */

abstract class GAEvalData {
    GAEvalData() {
    }

    final void delete() {
    }

    final GAEvalData assign(final GAEvalData orig) {
        if (orig != this) {
            copy(orig);
        }
        return this;
    }

    public abstract Object clone();

    abstract void copy(final GAEvalData c);
}


