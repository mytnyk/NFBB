package ga;

import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 5:24:56 PM
 * Description: the steady-state genetic algorithm class
 */
public final class GASteadyStateGA extends GAGeneticAlgorithm {
    private static final int USE_PREPL = 0;
    private static final int USE_NREPL = 1;
    private GAPopulation tmpPop;  // temporary population for replacements
    private float pRepl;// percentage of population to replace each gen
    private int nRepl;    // how many of each population to replace
    private short which;// 0 if prepl, 1 if nrepl

    public String className() {
        return "GASteadyStateGA";
    }

    public int classID() {
        return SteadyStateGA;
    }

    public static GAParameterList registerDefaultParameters(final GAParameterList p) {
        GAGeneticAlgorithm.registerDefaultParameters(p);
        final int ival = 1;
        p.add(GADefs.gaNnReplacement, GADefs.gaSNnReplacement, GAParameter.INT, new TObjInt(ival));
        p.add(GADefs.gaNpReplacement, GADefs.gaSNpReplacement, GAParameter.FLOAT, new TObjFloat(gaDefPRepl));
        p.set(GADefs.gaNscoreFrequency, GAStatistics.gaDefScoreFrequency2);
        return p;
    }


    public GASteadyStateGA(final GAGenome c) throws clCancelException {
        super(c);
        pRepl = gaDefPRepl;
        params.add(GADefs.gaNpReplacement, GADefs.gaSNpReplacement, GAParameter.FLOAT, new TObjFloat(pRepl));

        final float n = pRepl * (float) pop.size() < 1 ? 1 : pRepl * (float) pop.size();
        tmpPop = new GAPopulation(pop.individual(0), (int) n);

        nRepl = tmpPop.size();
        params.add(GADefs.gaNnReplacement, GADefs.gaSNnReplacement, GAParameter.INT, new TObjInt(nRepl));
        stats.scoreFrequency(GAStatistics.gaDefScoreFrequency2);
        params.set(GADefs.gaNscoreFrequency, GAStatistics.gaDefScoreFrequency2);

        which = USE_PREPL;
    }

    GASteadyStateGA(final GAPopulation p) throws clCancelException {
        super(p);
        pRepl = gaDefPRepl;
        params.add(GADefs.gaNpReplacement, GADefs.gaSNpReplacement, GAParameter.FLOAT, new TObjFloat(pRepl));

        final float n = pRepl * (float) pop.size() < 1 ? 1 : pRepl * (float) pop.size();
        tmpPop = new GAPopulation(pop.individual(0), (int) n);

        nRepl = tmpPop.size();
        params.add(GADefs.gaNnReplacement, GADefs.gaSNnReplacement, GAParameter.INT, new TObjInt(nRepl));
        stats.scoreFrequency(GAStatistics.gaDefScoreFrequency2);
        params.set(GADefs.gaNscoreFrequency, GAStatistics.gaDefScoreFrequency2);

        which = USE_PREPL;
    }

    GASteadyStateGA(final GASteadyStateGA ga) throws clCancelException {
        super(ga);
        tmpPop = null;
        copy(ga);
    }

    GASteadyStateGA assign(final GASteadyStateGA ga) throws clCancelException {
        if (ga != this) {
            copy(ga);
        }
        return this;
    }

    void delete() {
        tmpPop.delete();
    }

    void copy(final GAGeneticAlgorithm g) throws clCancelException {
        super.copy(g);
        final GASteadyStateGA ga = (GASteadyStateGA) g;
        pRepl = ga.pRepl;
        nRepl = ga.nRepl;
        if (tmpPop != null) {
            tmpPop.copy(ga.tmpPop);
        } else {
            tmpPop = (GAPopulation) ga.tmpPop.clone();
        }
        tmpPop.geneticAlgorithm(this);

        which = ga.which;
    }

    /**
     * For initialization we set the random seed, check for stupid errors, init the
     * population, reset the statistics, and that's it.
     * If we don't get a seed then we set it ourselves.  If we do get one, then
     * we use it as the random seed.
     */
    void initialize(final int seed) throws clCancelException {
        GARandom.GARandomSeed(seed);
        pop.initialize();
        pop.evaluate(true);
        stats.reset(pop);
        if (scross == null) {
            GAError.GAErr(className(), "initialize", GAError.gaErrNoSexualMating);
        }
    }

    /**
     * Evolve a new generation of genomes.  A steady-state GA has no 'old'
     * and 'new' populations - we pick from the current population and replace its
     * members with the new ones we create.  We replace the worst members of the
     * preceeding population.  If a genome in the tmp population is worse than
     * one in the main population, the genome in the main population will be
     * replaced regardless of its better score.
     */
    void step() throws clCancelException {
        int i, mut, c1, c2;
        GAGenome mom, dad;   // tmp holders for selected genomes
        // Generate the individuals in the temporary population from individuals in
        // the main population.
        for (i = 0; i < tmpPop.size() - 1; i += 2) {    // takes care of odd population
            mom = pop.select();
            dad = pop.select();
            stats.numsel += 2;  // keep track of number of selections

            c1 = c2 = 0;
            if (GARandom.GAFlipCoin(pCrossover())) {
                stats.numcro += scross.sexualCrossover(mom, dad, tmpPop.individual(i),
                                                       tmpPop.individual(i + 1));
                c1 = c2 = 1;
            } else {
                tmpPop.individual(i).copy(mom);
                tmpPop.individual(i + 1).copy(dad);
            }
            stats.nummut += mut = tmpPop.individual(i).mutate(pMutation());
            if (mut > 0) {
                c1 = 1;
            }
            stats.nummut += mut = tmpPop.individual(i + 1).mutate(pMutation());
            if (mut > 0) {
                c2 = 1;
            }

            stats.numeval += c1 + c2;
        }
        if (tmpPop.size() % 2 != 0) {   // do the remaining population member
            mom = pop.select();
            dad = pop.select();
            stats.numsel += 2;          // keep track of number of selections

            c1 = 0;
            if (GARandom.GAFlipCoin(pCrossover())) {
                stats.numcro += scross.sexualCrossover(mom, dad,
                                                       tmpPop.individual(i), null);
                c1 = 1;
            } else {
                if (GARandom.GARandomBit() != 0) {
                    tmpPop.individual(i).copy(mom);
                } else {
                    tmpPop.individual(i).copy(dad);
                }
            }
            stats.nummut += mut = tmpPop.individual(i).mutate(pMutation());
            if (mut > 0) {
                c1 = 1;
            }
            stats.numeval += c1;
        }
// Replace the worst genomes in the main population with all of the individuals
// we just created.  Notice that we invoke the population's add member with a
// genome pointer rather than reference.  This way we don't force a clone of
// the genome - we just let the population take over.  Then we take it back by
// doing a remove then a replace in the tmp population.

        for (i = 0; i < tmpPop.size(); i++) {
            pop.add(tmpPop.individual(i));
        }
        pop.evaluate(); // get info about current pop for next time
        pop.scale();    // remind the population to do its scaling

// the individuals in tmpPop are all owned by pop, but tmpPop does not know
// that.  so we use replace to take the individuals from the pop and stick
// them back into tmpPop
        for (i = 0; i < tmpPop.size(); i++) {
            tmpPop.replace(pop.remove(GAPopulation.Replacement.WORST, GAPopulation.SortBasis.SCALED), i);
        }
        stats.numrep += tmpPop.size();
        stats.update(pop); // update the statistics by one generation
    }

    GASteadyStateGA increment() throws clCancelException {
        step();
        return this;
    }

    protected int setptr(final String name, final Object value) throws clCancelException {
        int status = super.setptr(name, value);
        if (name == GADefs.gaNpReplacement ||
            name == GADefs.gaSNpReplacement) {
            pReplacement(((TObjFloat) value).getValue());
            status = 0;
        } else if (name == GADefs.gaNnReplacement ||
                   name == GADefs.gaSNnReplacement) {
            nReplacement(((TObjInt) value).getValue());
            status = 0;
        }
        return status;
    }

    int get(final String name, Object value) {
        int status = super.get(name, value);
        if (name == GADefs.gaNpReplacement ||
            name == GADefs.gaSNpReplacement) {
            value = new TObjFloat(pRepl);
            status = 0;
        } else if (name == GADefs.gaNnReplacement ||
                   name == GADefs.gaSNnReplacement) {
            value = new TObjInt(nRepl);
            status = 0;
        }
        return status;
    }

    int minimaxi() {
        return minmax;
    }

    protected int minimaxi(final int m) {
        super.minimaxi(m);
        if (m == MINIMIZE) {
            tmpPop.order(GAPopulation.SortOrder.LOW_IS_BEST);
        } else {
            tmpPop.order(GAPopulation.SortOrder.HIGH_IS_BEST);
        }
        return minmax;
    }

    GAPopulation population() {
        return pop;
    }

    GAPopulation population(final GAPopulation p) throws clCancelException {
        if (p.size() < 1) {
            GAError.GAErr(className(), "population", GAError.gaErrNoIndividuals);
            return pop;
        }
        super.population(p);
        //delete tmpPop;
        if (which == USE_PREPL) {
            float n = pRepl * pop.size();
            if (n < 1) {
                n = (float) 1.0;
            }
            nRepl = (int) n;
            params.set(GADefs.gaNnReplacement, nRepl);
        } else {
            if (nRepl > pop.size()) {
                nRepl = pop.size();
            }
            if (nRepl < 1) {
                nRepl = 1;
            }
        }
        tmpPop = new GAPopulation(pop.individual(0), nRepl);
        tmpPop.geneticAlgorithm(this);
        return pop;
    }

    int populationSize() {
        return pop.size();
    }

    protected int populationSize(final int value) throws clCancelException {
        super.populationSize(value);
        if (which == USE_PREPL) {
            final float n = pRepl * (float) pop.size() < 1 ? 1 : pRepl * (float) pop.size();
            nRepl = (int) n;
            params.set(GADefs.gaNnReplacement, nRepl);
            tmpPop.size(nRepl);
        } else {                // if we're using nrepl, be sure in valid range
            if (nRepl > value) { // clip to new population size
                nRepl = value;
                tmpPop.size(nRepl);
            }
        }
        return value;
    }

    GAScalingScheme scaling() {
        return pop.scaling();
    }

    GAScalingScheme scaling(final GAScalingScheme s) {
        return super.scaling(s);
    }

    GASelectionScheme selector() {
        return pop.selector();
    }

    GASelectionScheme selector(final GASelectionScheme s) {
        return super.selector(s);
    }

    void objectiveFunction(final Evaluator f) {
        super.objectiveFunction(f);
        for (int i = 0; i < tmpPop.size(); i++) {
            tmpPop.individual(i).evaluator(f);
        }
    }

    void objectiveData(final GAEvalData v) {
        super.objectiveData(v);
        for (int i = 0; i < tmpPop.size(); i++) {
            tmpPop.individual(i).evalData(v);
        }
    }

    float pReplacement() {
        return pRepl;
    }

    private float pReplacement(final float value) throws clCancelException {
        if (value == pRepl) {
            return pRepl;
        }
        if (value <= 0 || value > 1) {
            GAError.GAErr(className(), "pReplacement", GAError.gaErrBadPRepl);
            params.set(GADefs.gaNpReplacement, pRepl);	// force it back
            return pRepl;
        }

        params.set(GADefs.gaNpReplacement, (double) value);
        pRepl = value;

        final float n = value * (float) pop.size() < 1 ? 1 : value * (float) pop.size();
        nRepl = (int) n;
        params.set(GADefs.gaNnReplacement, nRepl);

        which = USE_PREPL;

        tmpPop.size(nRepl);

        return pRepl;
    }

    int nReplacement() {
        return nRepl;
    }

    private int nReplacement(final int value) throws clCancelException {
        if (value == nRepl) {
            return nRepl;
        }
        if (value == 0 || value > (int) pop.size()) {
            GAError.GAErr(className(), "nReplacement", GAError.gaErrBadNRepl);
            params.set(GADefs.gaNnReplacement, nRepl); // force it back
            return nRepl;
        }
        params.set(GADefs.gaNnReplacement, value);
        nRepl = value;
        pRepl = (float) nRepl / (float) pop.size();
        params.set(GADefs.gaNpReplacement, (double) pRepl);
        which = USE_NREPL;
        tmpPop.size(nRepl);
        return nRepl;
    }
}
