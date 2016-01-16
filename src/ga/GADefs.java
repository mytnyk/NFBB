package ga;

/**
 * User: Oleg
 * Date: Jul 7, 2004
 * Time: 10:24:33 AM
 * Description:
 * When specifying parameters for a GAlib object, you can use the fullname (the
 * name used in parameters data files) or the short name (the name typically
 * used on the command line).  When specifying parameters in your code you can
 * use a string, or use the predefined variables below.
 */
public interface GADefs {

    String gaNnGenerations = "number_of_generations";
    String gaSNnGenerations = "ngen";
    String gaNpConvergence = "convergence_percentage";
    String gaSNpConvergence = "pconv";
    String gaNnConvergence = "generations_to_convergence";
    String gaSNnConvergence = "nconv";
    String gaNpCrossover = "crossover_probability";
    String gaSNpCrossover = "pcross";
    String gaNpMutation = "mutation_probability";
    String gaSNpMutation = "pmut";
    String gaNpopulationSize = "population_size";
    String gaSNpopulationSize = "popsize";
    String gaNnPopulations = "number_of_populations";
    String gaSNnPopulations = "npop";
    String gaNpReplacement = "replacement_percentage";
    String gaSNpReplacement = "prepl";
    String gaNnReplacement = "replacement_number";
    String gaSNnReplacement = "nrepl";
    String gaNnBestGenomes = "number_of_best";
    String gaSNnBestGenomes = "nbest";
    String gaNscoreFrequency = "score_frequency";
    String gaSNscoreFrequency = "sfreq";
    String gaNflushFrequency = "flush_frequency";
    String gaSNflushFrequency = "ffreq";
    String gaNscoreFilename = "score_filename";
    String gaSNscoreFilename = "sfile";
    String gaNselectScores = "select_scores";
    String gaSNselectScores = "sscores";
    String gaNelitism = "elitism";
    String gaSNelitism = "el";
    String gaNnOffspring = "number_of_offspring";
    String gaSNnOffspring = "noffspr";
    String gaNrecordDiversity = "record_diversity";
    String gaSNrecordDiversity = "recdiv";
    String gaNpMigration = "migration_percentage";
    String gaSNpMigration = "pmig";
    String gaNnMigration = "migration_number";
    String gaSNnMigration = "nmig";
    String gaNminimaxi = "minimaxi";
    String gaSNminimaxi = "mm";
}
