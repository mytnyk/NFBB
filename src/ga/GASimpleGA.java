package ga;

import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 12:28:27 PM
 * Description: simple genetic algorithm class.
 */
public final class GASimpleGA extends GAGeneticAlgorithm {
    private GAPopulation oldPop;  // current and old populations
    private boolean el;           // are we elitist?

    public String className() {
        return "GASimpleGA";
    }

    public int classID() {
        return SimpleGA;
    }

    static GAParameterList registerDefaultParameters(final GAParameterList p) {
        GAGeneticAlgorithm.registerDefaultParameters(p);
        p.add(GADefs.gaNelitism, GADefs.gaSNelitism,
              GAParameter.BOOLEAN, new TObjBool(gaDefElitism));
        return p;
    }


    public GASimpleGA(final GAGenome c) throws clCancelException {
        super(c);
        oldPop = (GAPopulation) pop.clone();
        el = true;
        params.add(GADefs.gaNelitism, GADefs.gaSNelitism, GAParameter.BOOLEAN, new TObjBool(el));
    }

    GASimpleGA(final GAPopulation p) throws clCancelException {
        super(p);
        oldPop = (GAPopulation) pop.clone();

        el = true;
        params.add(GADefs.gaNelitism, GADefs.gaSNelitism, GAParameter.BOOLEAN, new TObjBool(el));
    }

    GASimpleGA(final GASimpleGA ga) throws clCancelException {
        super(ga);
        oldPop = null;
        copy(ga);
    }

    GASimpleGA assign(final GASimpleGA ga) throws clCancelException {
        if (ga != this) {
            copy(ga);
        }
        return this;
    }

    void delete() {
        oldPop.delete();
    }

    void copy(final GAGeneticAlgorithm g) throws clCancelException {
        super.copy(g);
        final GASimpleGA ga = (GASimpleGA) g;
        el = ga.el;
        if (oldPop != null) {
            oldPop.copy(ga.oldPop);
        } else {
            oldPop = (GAPopulation) ga.oldPop.clone();
        }
        oldPop.geneticAlgorithm(this);
    }

    /**
     * Initialize the population, set the random seed as needed, do a few stupidity
     * checks, reset the stats.  We must initialize the old pop because there is no
     * guarantee that each individual will get initialized during the course of our
     * operator++ operations.  We do not evaluate the old pop because that will
     * happen as-needed later on.
     */
    void initialize(final int seed) throws clCancelException {
        GARandom.GARandomSeed(seed);
        pop.initialize();
        pop.evaluate(true); // the old pop will get it when the pops switch
        stats.reset(pop);
        if (scross == null) {
            GAError.GAErr(className(), "initialize", GAError.gaErrNoSexualMating);
        }
    }

    /**
     * Evolve a new generation of genomes.  When we start this routine, pop
     * contains the current generation.  When we finish, pop contains the new
     * generation and oldPop contains the (no longer) current generation.  The
     * previous old generation is lost.  We don't deallocate any memory, we just
     * reset the contents of the genomes.
     * The selection routine must return a pointer to a genome from the old
     * population.
     */
    void step() throws clCancelException {
        int i, mut, c1, c2;
        GAGenome mom, dad;          // tmp holders for selected genomes
        final GAPopulation tmppop;        // Swap the old population with the new pop.
        tmppop = oldPop;            // When we finish the ++ we want the newly
        oldPop = pop;               // generated population to be current (for
        pop = tmppop;               // references to it from member functions).
        // Generate the individuals in the temporary population from individuals in
        // the main population.
        for (i = 0; i < pop.size() - 1; i += 2) { // takes care of odd population
            mom = oldPop.select();
            dad = oldPop.select();
            stats.numsel += 2;            // keep track of number of selections
            c1 = c2 = 0;
            if (GARandom.GAFlipCoin(pCrossover())) {
                stats.numcro += scross.sexualCrossover(mom, dad,
                                                       pop.individual(i), pop.individual(i + 1));
                c1 = c2 = 1;
            } else {
                pop.individual(i).copy(mom);
                pop.individual(i + 1).copy(dad);
            }
            stats.nummut += mut = pop.individual(i).mutate(pMutation());
            if (mut > 0) {
                c1 = 1;
            }
            stats.nummut += mut = pop.individual(i + 1).mutate(pMutation());
            if (mut > 0) {
                c2 = 1;
            }
            stats.numeval += c1 + c2;
        }
        if (pop.size() % 2 != 0) {    // do the remaining population member
            mom = oldPop.select();
            dad = oldPop.select();
            stats.numsel += 2;        // keep track of number of selections
            c1 = 0;
            if (GARandom.GAFlipCoin(pCrossover())) {
                stats.numcro += scross.sexualCrossover(mom, dad, pop.individual(i), null);
                c1 = 1;
            } else {
                if (GARandom.GARandomBit() != 0) {
                    pop.individual(i).copy(mom);
                } else {
                    pop.individual(i).copy(dad);
                }
            }
            stats.nummut += mut = pop.individual(i).mutate(pMutation());
            if (mut > 0) {
                c1 = 1;
            }

            stats.numeval += c1;
        }
        stats.numrep += pop.size();
        pop.evaluate(true); // get info about current pop for next time
        // If we are supposed to be elitist, carry the best individual from the old
        // population into the current population.  Be sure to check whether we are
        // supposed to minimize or maximize.
        if (minimaxi() == GAGeneticAlgorithm.MAXIMIZE) {
            if (el && oldPop.best().score() > pop.best().score()) {
                oldPop.replace(pop.replace(oldPop.best(), GAPopulation.Replacement.WORST),
                               GAPopulation.Replacement.BEST);
            }
        } else {
            if (el && oldPop.best().score() < pop.best().score()) {
                oldPop.replace(pop.replace(oldPop.best(), GAPopulation.Replacement.WORST),
                               GAPopulation.Replacement.BEST);
            }
        }
        stats.update(pop);      // update the statistics by one generation
    }

    GASimpleGA increment() throws clCancelException {
        step();
        return this;
    }

    protected int setptr(final String name, final Object value) throws clCancelException {
        int status = super.setptr(name, value);

        if (name == GADefs.gaNelitism ||
            name == GADefs.gaSNelitism) {
            el = ((TObjBool) value).getValue() ? true : false;
            status = 0;
        }
        return status;
    }

    int get(final String name, Object value) {
        int status = super.get(name, value);

        if (name == GADefs.gaNelitism ||
            name == GADefs.gaSNelitism) {
            value = new TObjBool(el ? true : false);
            status = 0;
        }
        return status;
    }

    boolean elitist() {
        return el;
    }

    boolean elitist(final boolean flag) {
        params.set(GADefs.gaNelitism, new TObjBool(flag));
        return el = flag;
    }

    int minimaxi() {
        return minmax;
    }

    protected int minimaxi(final int m) {
        super.minimaxi(m);
        if (m == MINIMIZE) {
            oldPop.order(GAPopulation.SortOrder.LOW_IS_BEST);
        } else {
            oldPop.order(GAPopulation.SortOrder.HIGH_IS_BEST);
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
        oldPop.copy((GAPopulation) pop.clone());
        oldPop.geneticAlgorithm(this);

        return pop;
    }

    int populationSize() {
        return pop.size();
    }

    public int populationSize(final int n) throws clCancelException {
        super.populationSize(n);
        oldPop.size(n);
        return n;
    }

    GAScalingScheme scaling() {
        return pop.scaling();
    }

    public GAScalingScheme scaling(final GAScalingScheme s) {
        oldPop.scaling(s);
        return super.scaling(s);
    }

    GASelectionScheme selector() {
        return pop.selector();
    }

    GASelectionScheme selector(final GASelectionScheme s) {
        oldPop.selector(s);
        return super.selector(s);
    }

    void objectiveFunction(final Evaluator f) {
        super.objectiveFunction(f);
        for (int i = 0; i < pop.size(); i++) {
            oldPop.individual(i).evaluator(f);
        }
    }

    void objectiveData(final GAEvalData v) {
        super.objectiveData(v);
        for (int i = 0; i < pop.size(); i++) {
            pop.individual(i).evalData(v);
        }
    }
}
