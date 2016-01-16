package ga;

/**
 * User: Oleg
 * Date: Jul 2, 2004
 * Time: 3:00:05 PM
 * Description:
 * This defines the identifiers for polymorphic classes.  The IDs are used
 * primarily for checking to be see if the two objects are the same type before
 * doing a copy, for example.  The name is often used when printing out error
 * messages so you'll know where things are happening.
 * I hate to derive so many classes from the same base class, especially when
 * the derived classes are completely unrelated.  But this is a convenient way to
 * enumerate the built-in classes, and they DO share the polymorphic behaviour
 * (even if they do NOT share any other attributes).
 * <p/>
 * TO DO:
 * I leave the id/classname implementation for backward compatibility.  Also,
 * as of fall98 there are still some systems that do not support RTTI (or environs
 * that do not want to use RTTI for some reason or another).
 * This whole thing will be replaced with a proper RTTI implementation as soon
 * as RTTI is stable on all the platforms (and as soon as I have time to do the
 * update).  So for now, I apologize for the 'hack'iness of this implementation.
 */
interface GAID {
    int BaseGA = 0;
    int SimpleGA = 1;
    int SteadyStateGA = 2;
    int IncrementalGA = 3;
    int DemeGA = 4;

    int Population = 10;

    int Scaling = 15;
    int NoScaling = 16;
    int LinearScaling = 17;
    int SigmaTruncationScaling = 18;
    int PowerLawScaling = 19;
    int Sharing = 20;

    int Selection = 40;
    int RankSelection = 41;
    int RouletteWheelSelection = 42;
    int TournamentSelection = 43;
    int UniformSelection = 44;
    int SRSSelection = 45;
    int DSSelection = 46;

    int Genome = 50;
    int BinaryStringGenome = 51;
    int BinaryStringGenome2D = 52;
    int BinaryStringGenome3D = 53;
    int Bin2DecGenome = 54;
    int ListGenome = 55;
    int TreeGenome = 56;
    int ArrayGenome = 57;
    int ArrayGenome2D = 58;
    int ArrayGenome3D = 59;
    int ArrayAlleleGenome = 60;
    int ArrayAlleleGenome2D = 61;
    int ArrayAlleleGenome3D = 62;
    int StringGenome = 63;
    int FloatGenome = 64;
    int IntGenome = 65;
    int DoubleGenome = 66;

    boolean sameClass(final GAID b);

    String className();

    int classID();
}
