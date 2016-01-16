package ga;

/**
 * User: Oleg
 * Date: Jul 1, 2004
 * Time: 2:43:48 PM
 * Description:
 * These are the indices for all of the error messages used in the library.
 */

interface GAErrorIndex {
// general errors
    int gaErrReadError = 0;
    int gaErrWriteError = 1;
    int gaErrUnexpectedEOF = 2;
    int gaErrBadProbValue = 3;
    int gaErrObjectTypeMismatch = 4;
    int gaErrOpUndef = 5;
    int gaErrRefsRemain = 6;

// errors for the GA objects
    int gaErrNeedRS = 7;
    int gaErrBadRS = 8;
    int gaErrBadCS = 9;
    int gaErrBadPRepl = 10;
    int gaErrBadNRepl = 11;
    int gaErrBadPopIndex = 12;
    int gaErrNoIndividuals = 13;
    int gaErrBadPopSize = 14;
    int gaErrNoSexualMating = 15;
    int gaErrNoAsexualMating = 16;

// errors for the genome and crossover objects
    int gaErrSameBehavReqd = 17;
    int gaErrSameLengthReqd = 18;
    int gaErrBadParentLength = 19;
    int gaErrBadResizeBehaviour = 20;
    int gaErrBadPhenotypeID = 21;
    int gaErrBadPhenotypeValue = 22;
    int gaErrBadBndsDim = 23;

// scaling scheme error messages
    int gaErrBadLinearScalingMult = 24;
    int gaErrBadSigmaTruncationMult = 25;
    int gaErrNegFitness = 26;
    int gaErrPowerNegFitness = 27;
    int gaErrBadSharingCutoff = 28;

// miscellaneous error messages from various data objects
    int gaErrNoAlleleIndex = 29;
    int gaErrBinStrTooLong = 30;
    int gaErrDataLost = 31;
    int gaErrBadWhereIndicator = 32;
    int gaErrBadTypeIndicator = 33;
    int gaErrBadTreeLinks = 34;
    int gaErrCannotSwapAncestors = 35;
    int gaErrCannotInsertIntoSelf = 36;
    int gaErrCannotInsertOnNilNode = 37;
    int gaErrCannotInsertWithSiblings = 38;
    int gaErrCannotInsertBeforeRoot = 39;
    int gaErrCannotInsertAfterRoot = 40;
}
