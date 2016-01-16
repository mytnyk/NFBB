package ga.exam;

import ga.*;
import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 8, 2004
 * Time: 11:28:42 AM
 * Description:
 * ----------------------------------------------------------------------------
 * ex1.C
 * Example program for the SimpleGA class and 2DBinaryStringGenome class.
 * This program tries to fill the 2Dgenome with alternating 1s and 0s.
 * This example uses the default crossover (single point), default mutator
 * (uniform random bit flip), and default initializer (uniform random) for the
 * 2D genome.
 * Notice that one-point crossover is not necessarily the best kind of crossover
 * to use if you want to generate a 'good' genome with this kind of objective
 * function.  But it does work.
 * ----------------------------------------------------------------------------
 */
public final class ex1 implements Evaluator {

    public static void main(final String[] args) throws clCancelException {
        final ex1 inst = new ex1();
        inst.test(args);
    }

    private void test(final String[] args) throws clCancelException {
        System.out.println("Example 1");
        System.out.println("This program tries to fill a 2DBinaryStringGenome with");
        System.out.println("alternating 1s and 0s using a SimpleGA");
// See if we've been given a seed to use (for testing purposes).  When you
// specify a random seed, the evolution will be exactly the same each time
// you use that seed number.
        int seed = 0;
        for (int i = 1; i < args.length; i++) {
            if (args[i++] == "seed") {
                seed = Integer.parseInt(args[i]);
            }
        }
// Declare variables for the GA parameters and set them to some default values.
        final int width = 10;
        final int height = 5;
        final int popsize = 30;
        final int ngen = 400;
        final float pmut = (float) 0.001;
        final float pcross = (float) 0.9;

// Now create the GA and run it.  First we create a genome of the type that
// we want to use in the GA.  The ga doesn't operate on this genome in the
// optimization - it just uses it to clone a population of genomes.
        GARandom.GARandomSeed(seed);
        GA2DBinaryStringGenome genome = new GA2DBinaryStringGenome(width, height, this);

// Now that we have the genome, we create the genetic algorithm and set
// its parameters - number of generations, mutation probability, and crossover
// probability.  And finally we tell it to evolve itself.

        final GASimpleGA ga = new GASimpleGA(genome);
        ga.populationSize(popsize);
        ga.nGenerations(ngen);
        ga.pMutation(pmut);
        ga.pCrossover(pcross);
        ga.evolve();

// Now we print out the best genome that the GA found.
        genome = (GA2DBinaryStringGenome) ga.statistics().bestIndividual();
        System.out.println("The GA found:\n" + ga.statistics().bestIndividual());

// That's it!
    }


    /**
     * This is the objective function.  All it does is check for alternating 0s and
     * 1s.  If the gene is odd and contains a 1, the fitness is incremented by 1.
     * If the gene is even and contains a 0, the fitness is incremented by 1.  No
     * penalties are assigned.
     * We have to do the cast because a plain, generic GAGenome doesn't have
     * the members that a GA2DBinaryStringGenome has.  And it's ok to cast it
     * because we know that we will only get GA2DBinaryStringGenomes and
     * nothing else.
     */
    public float evaluator(final GAGenome g) {
        final GA2DBinaryStringGenome genome = (GA2DBinaryStringGenome) g;
        float score = (float) 0.0;
        int count = 0;
        for (int i = 0; i < genome.width(); i++) {
            for (int j = 0; j < genome.height(); j++) {
                if (genome.gene(i, j) == 0 && count % 2 == 0) {
                    score += 1.0;
                }
                if (genome.gene(i, j) == 1 && count % 2 != 0) {
                    score += 1.0;
                }
                count++;
            }
        }
        return score;
    }
}
