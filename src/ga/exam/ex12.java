package ga.exam;

import ga.*;
import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 7, 2004
 * Time: 10:04:10 AM
 * Description: ...
 * ----------------------------------------------------------------------------
 * ex12
 * <p/>
 * DESCRIPTION:
 * This example shows how to use an order-based string genome.
 * ----------------------------------------------------------------------------
 */
public final class ex12 implements Evaluator, GADefs {
    public static void main(final String[] args) throws clCancelException {
        final ex12 inst = new ex12();
        inst.test(args);
    }

    private void test(final String[] args) throws clCancelException {

        System.out.println("Example 12");
        System.out.println("This program illustrates the use of order-based strings.");
        System.out.println("The string in this problem contains 26 letters, a to z.");
        System.out.println("It tries to put them in alphabetic order.");
// See if we've been given a seed to use (for testing purposes).  When you
// specify a random seed, the evolution will be exactly the same each time
// you use that seed number.
        for (int ii = 1; ii < args.length; ii++) {
            if (args[ii++] == "seed") {
                GARandom.GARandomSeed(Integer.parseInt(args[ii]));
            }
        }
// Set the default values of the parameters then parse for command line changes
        int i;
        final GAParameterList params = new GAParameterList();
        GASteadyStateGA.registerDefaultParameters(params);
        params.set(gaNpopulationSize, 25);  // population size
        params.set(gaNpCrossover, 0.9);     // probability of crossover
        params.set(gaNpMutation, 0.01);     // probability of mutation
        params.set(gaNnGenerations, 4000);  // number of generations
        params.parse(args.length, args, false);

// Now create the GA and run it.  We first create a genome with the
// operators we want.  Since we're using a template genome, we must assign
// all three operators.  We use the order-based crossover site when we assign
// the crossover operator.

        //typedef GAAlleleSet<char> GAStringAlleleSet;

        final GAAlleleSet alleles = new GAAlleleSet();
        char a = 'a';
        for (i = 0; i < 26; i++) {
            alleles.add(new TObjChar(a++));
        }

        //typedef GA1DArrayAlleleGenome<char> GAStringGenome;
        GA1DArrayAlleleGenome genome = new GA1DArrayAlleleGenome(26, alleles, this);
        genome.initializer(new AlphabetInitializer());
        genome.mutator(new GA1DArrayGenome.SwapMutator());

        final GASteadyStateGA ga = new GASteadyStateGA(genome);
        ga.parameters(params);
        ga.crossover(new GA1DArrayGenome.PartialMatchCrossover());
        ga.evolve();

        genome = (GA1DArrayAlleleGenome) ga.statistics().bestIndividual();
        System.out.println("the ga generated the following string (objective score is ");
        System.out.println(genome.score() + "):\n" + genome + '\n' + genome.className());
    }

    /**
     * ----------------------------------------------------------------------------
     * AlphabetInitializer
     * This initializer creates a string genome with the letters a-z as its
     * elements.  Once we have assigned all the values, we randomize the string.
     * ----------------------------------------------------------------------------
     */
    final class AlphabetInitializer implements Initializer {

        public void initializer(final GAGenome c) {
            final GA1DArrayAlleleGenome genome = (GA1DArrayAlleleGenome) c;
            int i;
            char a = 'a';
            for (i = 0; i < genome.size(); i++) {
                genome.gene(25 - i, new TObjChar(a++));
            }

            for (i = 0; i < genome.size(); i++) {
                if (GARandom.GARandomBit() != 0) {
                    genome.swap(i, GARandom.GARandomInt(0, genome.size() - 1));
                }
            }
        }
    }

    /**
     * ----------------------------------------------------------------------------
     * Objective function
     * The objective function gives one point for every number in the correct
     * position.  We're trying to get a sequence of numbers from n to 0 in descending
     * order.
     * ----------------------------------------------------------------------------
     */
    public float evaluator(final GAGenome c) {
        final GA1DArrayAlleleGenome genome = (GA1DArrayAlleleGenome) c;
        float score = 0;
        char a = 'a';
        for (int i = 0; i < genome.size(); i++) {
            score += ((TObjChar) genome.gene(i)).getValue() == a++ ? 1 : 0;
        }
        return score;
    }
}
