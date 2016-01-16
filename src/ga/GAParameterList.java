package ga;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 12:04:49 PM
 * Description:
 * ----------------------------------------------------------------------------
 * The parameter list is implemented as an array, but has the interface of a
 * list.  Don't ask.  You can traverse through the list to get the parameters that
 * you need.  Be sure to check the type before you try to extract the value for
 * any specific parameter in the list.
 * ----------------------------------------------------------------------------
 */
public final class GAParameterList {
    private static final int PRM_CHUNKSIZE = 10;
    static final int BUFSIZE = 1024;     // size of buffer for reading pairs
    static final int MAX_PAIRS = 5000;   // max number of name-value pairs in stream
    static final int NAMESIZE = 128;     // max length of name in name-value pair

    private int n;
    private int N;
    private int cur;
    private GAParameter[] p;

    /**
     * The default parameter list is empty.
     */
    public GAParameterList() {
        N = n = cur = 0;
        p = null;
    }

    GAParameterList(final GAParameterList list) {
        N = list.N;
        n = list.n;
        cur = list.cur;
        p = new GAParameter[N];
        for (int i = 0; i < n; i++) {
            p[i] = new GAParameter(list.p[i]);
        }
    }

    /**
     * This is a rather stupid (operator=)(assing) implementation.  Instead of doing a copy
     * we just nuke everything the reallocate new stuff.  If this were called a lot
     * then we could end up with some fragmentation this way (rather than just
     * doing copies on already-allocated memory).
     */
    GAParameterList assing(final GAParameterList list) {
        if (list == this) {
            return this;
        }

        int i;

        for (i = 0; i < n; i++) {
            p[i].delete();
        }
        /*delete [] p;*/

        N = list.N;
        n = list.n;
        cur = list.cur;
        p = new GAParameter[N];
        for (i = 0; i < n; i++) {
            p[i] = new GAParameter(list.p[i]);
        }

        return this;
    }

    void delete() {
        for (int i = 0; i < n; i++) {
            p[i].delete();
        }
        //delete [] p;
    }

    int size() {
        return n;
    }

    public int get(final String name, Object value) {
        int status = 1;
        for (int i = 0; i < n; i++) {
            if (name == p[i].fullname() ||
                name == p[i].shrtname()) {
                value = p[i].value();
                status = 0;
            }
        }
        return status;
    }

    /**
     * Set the specified parameter (if we have it).  If we don't recognize the name
     * (ie it has not been added to the list) then we return the error code.
     */
    public int set(final String name, final Object v) {
        boolean found = false;
        for (int i = 0; i < n && !found; i++) {
            if (name == p[i].fullname() ||
                name == p[i].shrtname()) {
                p[i].value(v);
                found = true;
            }
        }
        return found ? 0 : -1;
    }

    public int set(final String s, final int v) {
        return set(s, new TObjInt(v));
    }

    public int set(final String s, final char v) {
        return set(s, new TObjChar(v));
    }

    public int set(final String s, final String v) {
        return set(s, (Object) v);
    }

    /**
     * Must do a special case for double/float.  Any floats that get passed to this
     * routine will be cast to doubles, but then we need to force them back to a
     * float if FLOAT is the type that is expected.  Kind of sucks, eh?
     * We could check the parameter type against the type here, but we don't.
     * (could do it for all of the 'set' members).  Maybe in a later release.
     */

    public int set(final String name, final double v) {
        boolean found = false;
        for (int i = 0; i < n; i++) {
            if (name == p[i].fullname() ||
                name == p[i].shrtname()) {
                if (p[i].type() == GAParameter.FLOAT) {
                    final float fval = (float) v;
                    p[i].value(new TObjFloat(fval));
                } else if (p[i].type() == GAParameter.DOUBLE) {
                    p[i].value(new TObjDouble(v));
                } else {
                    GAError.GAErr("GAParameterList", "set", GAError.gaErrBadTypeIndicator);
                }
                found = true;
            }
        }
        return found ? 0 : -1;
    }

    /**
     * Add the item to the list if it does not already exist.  Return 0 if the add
     * was OK, -1 if there was a problem.
     */
    int add(final String fn, final String sn, final int t, final Object v) {
        final int status = -1;
        if (n == N) {
            N += PRM_CHUNKSIZE;
            final GAParameter[] tmp = p;
            p = new GAParameter[N];
            if (n > 0) {
                System.arraycopy(tmp, 0, p, 0, n);
            }
            //delete[] tmp;
        }
        boolean found = false;
        for (int i = 0; i < n && !found; i++) {
            if (fn == p[i].fullname() &&
                sn == p[i].shrtname()) {
                found = true;
            }
        }
        if (!found) {
            cur = n;
            p[n++] = new GAParameter(fn, sn, t, v);
        }
        return status;
    }

    /**
     * When you remove a parameter from the list, the iterator is left pointing
     * at the same location.  If the item was the last in the list, then the
     * iterator moves to the new last item in the list.
     * Return 0 if everything was OK, -1 if error.
     */
    int remove() {
        int status = -1;
        if (cur > n) {
            return status;
        }
        p[cur].delete();
        // TODO: implement smth like this:
        // memmove( & (p[cur]), &(p[cur + 1]), (n - cur - 1) * sizeof(GAParameter *));
        n--;
        if (cur > n) {
            cur = n;
        }
        status = 0;
        return status;
    }

    GAParameter getAt(final int i) {
        return p[i];
    }

    GAParameter next() {
        return p[(cur > n ? cur = 0 : ++cur)];
    }

    GAParameter prev() {
        return p[(cur == 0 ? cur = n - 1 : --cur)];
    }

    GAParameter current() {
        return p[cur];
    }

    GAParameter first() {
        return p[cur = 0];
    }

    GAParameter last() {
        return p[cur = n - 1];
    }

    GAParameter operator(final String name) {
        for (int i = 0; i < n; i++) {
            if (name == p[i].fullname() || name == p[i].shrtname()) {
                return p[i];
            }
        }
        return null;
    }

    /**
     * Parse the arglist for any recognized arguments.  If we find a string we
     * know, then we set the value.  If we find a string we don't know then we
     * don't do anything unless the flag is set.  If the flag is set then we
     * complain about unknown arguments.
     * You should set up the list before you do the parsing in order to grab
     * arguments from the arglist.
     * When we encounter names we know with valid values, we put the list into
     * order as we get the new values.  When we recognize a pair, we pull the
     * parameter from the list, set its value, then stick it at the end of the
     * parameter list.  So if something turns up more than once, we'll remove-
     * then-append it more than once and the ordering from argv will be properly
     * maintained.
     * We assume that argv[0] is the name of the program, so we don't barf on
     * it if it is not a recognized name.
     */
    public int parse(int argc, final String[] argv, final boolean flag) {
        int nfound = 0;
        if (n == 0) {
            return nfound;
        }

        final String[] argvout = new String[argc];
        int argcu = argc - 1;
        int argcl = 0;

        for (int i = 0; i < argc; i++) {
            boolean found = false;
            // Loop through all of the parameters to see if we got a match.  If there is
            // no value for the name, complain.  Otherwise, set the value.
            for (int j = 0; j < n && !found; j++) {
                if (p[j].shrtname() == argv[i] ||
                    p[j].fullname() == argv[i]) {
                    found = true;
                    argvout[argcu] = argv[i];
                    argcu--;
                    if (++i >= argc) {
                        GAError.GAErr("GAParameterList", "parse", argv[0], argv[i - 1], " needs a value");
                    } else {
                        final int ival;
                        final float fval;
                        final double dval;

                        switch (p[j].type()) {
                            case GAParameter.BOOLEAN:
                                if (argv[i] == "true" ||
                                    argv[i] == "True" ||
                                    argv[i] == "TRUE" ||
                                    argv[i] == "t" ||
                                    argv[i] == "T") {
                                    ival = 1;
                                } else {
                                    ival = 0;
                                }

                                set(argv[i - 1], ival);
                                nfound += 1;
                                break;
                            case GAParameter.INT:
                                ival = Integer.parseInt(argv[i]);
                                set(argv[i - 1], ival);
                                nfound += 1;
                                break;
                            case GAParameter.CHAR:
                            case GAParameter.STRING:
                                set(argv[i - 1], argv[i]);
                                nfound += 1;
                                break;
                            case GAParameter.FLOAT:
                                fval = Float.parseFloat(argv[i]);
                                set(argv[i - 1], fval);
                                nfound += 1;
                                break;
                            case GAParameter.DOUBLE:
                                dval = Double.parseDouble(argv[i]);
                                set(argv[i - 1], dval);
                                nfound += 1;
                                break;
                            case GAParameter.POINTER:
                            default:
                                break;
                        }
                        // Move this parameter to the front of the list
                        if (j < n - 1) {
                            final GAParameter tmpptr = p[j];
                            // TODO: implement smth like this:
                            // memmove(  & (p[j]), &(p[j + 1]), (n - j - 1) * sizeof(GAParameter *));
                            p[n - 1] = tmpptr;
                        }
                        // Now update the argv array and argc count to indicate we understood this one
                        argvout[argcu] = argv[i];
                        argcu--;
                        continue;
                    }
                }
            }
            if (!found) {
                if (flag && i != 0) {
                    String _gaerrbuf1 = "\0";
                    _gaerrbuf1 += "unrecognized name ";
                    _gaerrbuf1 += argv[i];
                    GAError.GAErr("GAParameterList", "parse", "GAParameterList", "parse", _gaerrbuf1);
                }
                argvout[argcl] = argv[i];
                argcl++;
            }
        }
        System.arraycopy(argvout, 0, argv, 0, argc);
        argc = argcl;
        //delete[] argvout;
        return nfound;
    }

}
