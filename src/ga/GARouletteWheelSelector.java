package ga;

import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 12:06:02 PM
 * Description:
 * ----------------------------------------------------------------------------
 * Roulette wheel uses a fitness-proportional algorithm for selecting
 * individuals.
 * ----------------------------------------------------------------------------
 */

final class GARouletteWheelSelector extends GASelectionScheme {

    private int n;
    private float[] psum;

    public String className() {
        return "GARouletteWheelSelector";
    }

    public int classID() {
        return RouletteWheelSelection;
    }

    GARouletteWheelSelector() {
        this(SCALED);
    }

    private GARouletteWheelSelector(final int w) {
        super(w);
        psum = null;
        n = 0;
    }

    GARouletteWheelSelector(final GARouletteWheelSelector orig) {
        super(0);
        psum = null;
        n = 0;
        copy(orig);
    }

    GASelectionScheme assign(final GASelectionScheme orig) {
        if (orig != this) {
            copy(orig);
        }
        return this;
    }

    void delete() {
        //delete[] psum;
    }

    public Object clone() {
        return new GARouletteWheelSelector();
    }

    void copy(final GASelectionScheme orig) {
        super.copy(orig);
        final GARouletteWheelSelector sel = (GARouletteWheelSelector) orig;
        //delete[] psum;
        n = sel.n;
        psum = new float[n];
        System.arraycopy(sel.psum, 0, psum, 0, n);
    }

    /**
     * This selection routine is straight out of Goldberg's Genetic Algorithms
     * book (with the added restriction of not allowing zero scores - Goldberg
     * does not address this degenerate case).  We look through the members of the
     * population using a weighted roulette wheel.  Likliehood of selection is
     * proportionate to the fitness score.
     * This is a binary search method (using cached partial sums).  It assumes
     * that the genomes are in order from best (0th) to worst (n-1).
     */
    GAGenome select() {
        final float cutoff;
        int i, upper, lower;

        cutoff = GARandom.GARandomFloat();
        lower = 0;
        upper = pop.size() - 1;
        while (upper >= lower) {
            i = lower + (upper - lower >> 1);
            if (psum[i] > cutoff) {
                upper = i - 1;
            } else {
                lower = i + 1;
            }
        }
        lower = (int) GAUtils.GAMin(pop.size() - 1, lower);
        lower = (int) GAUtils.GAMax(0, lower);

        return pop.individual(lower,
                              which == SCALED ? GAPopulation.SortBasis.SCALED : GAPopulation.SortBasis.RAW);

    }

    /**
     * Update our list of partial sums.  Use the appropriate fitness/objective
     * scores as determined by the flag.  The delete/alloc assumes that the pop
     * size won't be changing a great deal.
     * Our selector requires that the population is sorted (it uses a binary
     * search to go faster) so we force the sort here.
     */
    void update() throws clCancelException {
        if (pop.size() != n) {
            //delete [] psum;
            n = pop.size();
            psum = new float[n];
        }

        int i;
        if (which == GASelectionScheme.RAW) {
            if (pop.max() == pop.min()) {
                for (i = 0; i < n; i++) {
                    psum[i] = (float) (i + 1) / (float) n;	// equal likelihoods
                }
            } else if (pop.max() > 0 && pop.min() >= 0 ||
                       pop.max() <= 0 && pop.min() < 0) {
                pop.sort(false, GAPopulation.SortBasis.RAW);
                if (pop.order() == GAPopulation.SortOrder.HIGH_IS_BEST) {
                    psum[0] = pop.individual(0, GAPopulation.SortBasis.RAW).score();
                    for (i = 1; i < n; i++) {
                        psum[i] = pop.individual(i, GAPopulation.SortBasis.RAW).score() + psum[i - 1];
                    }
                    for (i = 0; i < n; i++) {
                        psum[i] /= psum[n - 1];
                    }
                } else {
                    psum[0] = -pop.individual(0, GAPopulation.SortBasis.RAW).score()
                              + pop.max() + pop.min();
                    for (i = 1; i < n; i++) {
                        psum[i] = -pop.individual(i, GAPopulation.SortBasis.RAW).score()
                                  + pop.max() + pop.min() + psum[i - 1];
                    }
                    for (i = 0; i < n; i++) {
                        psum[i] /= psum[n - 1];
                    }
                }
            } else {
                GAError.GAErr(className(), "update - objective",
                              "objective scores are not strictly negative or strictly positive",
                              "this selection method cannot be used with these scores", "");
            }
        } else {
            if (pop.fitmax() == pop.fitmin()) {
                for (i = 0; i < n; i++) {
                    psum[i] = (float) (i + 1) / (float) n;	// equal likelihoods
                }
            } else if (pop.fitmax() > 0 && pop.fitmin() >= 0 ||
                       pop.fitmax() <= 0 && pop.fitmin() < 0) {
                pop.sort(false, GAPopulation.SortBasis.SCALED);
                if (pop.order() == GAPopulation.SortOrder.HIGH_IS_BEST) {
                    psum[0] = pop.individual(0, GAPopulation.SortBasis.SCALED).fitness();
                    for (i = 1; i < n; i++) {
                        psum[i] = pop.individual(i, GAPopulation.SortBasis.SCALED).fitness()
                                  + psum[i - 1];
                    }
                    for (i = 0; i < n; i++) {
                        psum[i] /= psum[n - 1];
                    }
                } else {
                    psum[0] = -pop.individual(0, GAPopulation.SortBasis.SCALED).fitness()
                              + pop.fitmax() + pop.fitmin();
                    for (i = 1; i < n; i++) {
                        psum[i] = -pop.individual(i, GAPopulation.SortBasis.SCALED).fitness() +
                                  pop.fitmax() + pop.fitmin() + psum[i - 1];
                    }
                    for (i = 0; i < n; i++) {
                        psum[i] /= psum[n - 1];
                    }
                }
            } else {
                GAError.GAErr(className(), "update - fitness",
                              "fitness scores are not strictly negative or strictly positive",
                              "this selection method cannot be used with these scores", "");
            }
        }
    }
}


