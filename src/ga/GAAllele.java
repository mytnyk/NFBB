package ga;

/**
 * User: Oleg
 * Date: Jul 1, 2004
 * Time: 1:32:53 PM
 * Description:
 * Here we define a interface of alleles.  An allele is a possible value for a gene
 * and an allele set is a list of possible values (I use 'set' because it doesn't
 * imply the specific implementation of the container class).
 */

interface GAAllele {
    interface Type {
        int ENUMERATED = 1;
        int BOUNDED = 2;
        int DISCRETIZED = 3;
    }

    interface BoundType {
        int NONE = 0;
        int INCLUSIVE = 1;
        int EXCLUSIVE = 2;
    }
}