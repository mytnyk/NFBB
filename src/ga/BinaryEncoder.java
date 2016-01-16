package ga;

/**
 * User: Oleg
 * Date: Jul 7, 2004
 * Time: 4:36:20 PM
 * Description:
 * The encoder converts a decimal value into a binary string.  The decoder
 * converts a string of bits into a decimal value.  Both types of functions
 * return an error code to indicate whether or not the conversion was
 * successful.  The caller must make sure that sufficient space is available
 * for the arguments.  The encoder will set the value to whatever it was able
 * to encode, so be sure to check the return status and make your value such
 * that you can check it if you get a non-zero return code.
 */

interface BinaryEncoder {
    int encode(float[] val, short[] binstr, int offset, int nbits, float minval, float maxval);
}
