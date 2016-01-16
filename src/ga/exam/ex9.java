package ga.exam;

import ga.*;
import sm.base.util.clCancelException;

/**
 * User: Oleg
 * Date: Jul 7, 2004
 * Time: 5:12:40 PM
 * Description:
 * ----------------------------------------------------------------------------
 * ex9.C
 * <p/>
 * DESCRIPTION:
 * Sample program that illustrates how to use a GA to find the maximum value
 * of a continuous function in two variables.  This program uses a binary-to-
 * decimal genome.
 * ----------------------------------------------------------------------------
 */
public final class ex9 implements Evaluator {
    public static void main(final String[] args) throws clCancelException {
        final ex9 inst = new ex9();
        inst.test(args);
    }

    private void test(final String[] args) throws clCancelException {

        System.out.println("Example 9");
        System.out.println("This program finds the maximum value in the function");
        System.out.println("  y = - x1^2 - x2^2");
        System.out.println("with the constraints");
        System.out.println("     -5 <= x1 <= 5");
        System.out.println("     -5 <= x2 <= 5");
        System.out.println();
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
        final int popsize = 30;
        final int ngen = 100;
        final float pmut = (float) 0.01;
        final float pcross = (float) 0.6;
// Create a phenotype for two variables.  The number of bits you can use to
// represent any number is limited by the type of computer you are using.  In
// this case, we use 16 bits to represent a floating point number whose value
// can range from -5 to 5, inclusive.  The bounds on x1 and x2 can be applied
// here and/or in the objective function.
        final GABin2DecPhenotype map = new GABin2DecPhenotype();
        map.add(16, -5, 5);
        map.add(16, -5, 5);
// Create the template genome using the phenotype map we just made.
        GABin2DecGenome genome = new GABin2DecGenome(map, this);

// Now create the GA using the genome and run it.  We'll use sigma truncation
// scaling so that we can handle negative objective scores.

        final GASimpleGA ga = new GASimpleGA(genome);
        final GASigmaTruncationScaling scaling = new GASigmaTruncationScaling();
        ga.populationSize(popsize);
        ga.nGenerations(ngen);
        ga.pMutation(pmut);
        ga.pCrossover(pcross);
        ga.scaling(scaling);
        ga.scoreFilename("bog.dat");
        ga.scoreFrequency(10);
        ga.flushFrequency(50);
        ga.evolve(seed);
// Dump the results of the GA to the screen.
        genome = (GABin2DecGenome) ga.statistics().bestIndividual();
        System.out.println("the ga found an optimum at the point (");
        System.out.println(genome.phenotype(0) + ", " + genome.phenotype(1) + ')');
    }

    /**
     * This objective function tries to maximize the value of the function
     * <p/>
     * y = -(x1*x1 + x2*x2)
     */
    public float evaluator(final GAGenome c) {
        final GABin2DecGenome genome = (GABin2DecGenome) c;
        float y;
        y = -genome.phenotype(0) * genome.phenotype(0);
        y -= genome.phenotype(1) * genome.phenotype(1);
        return y;
    }
}
