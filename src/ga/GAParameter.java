package ga;

/**
 * User: Oleg
 * Date: Jul 5, 2004
 * Time: 2:21:12 PM
 * Description:
 * ----------------------------------------------------------------------------
 * This object is used for naming the parameters.  We associate a fullname, a
 * short name, and a value with each parameter.
 * ----------------------------------------------------------------------------
 * Definition of the parameter object and a container list for it.  I did this
 * as a separate list implementation because I don't want all of the overhead of
 * a fullblown list.  The parameter list is a special purpose, stripped down list
 * implementation.
 * ----------------------------------------------------------------------------
 */

final class GAParameter {
    static final int BOOLEAN = 0;
    static final int CHAR = 1;
    static final int STRING = 2;
    static final int INT = 3;
    static final int FLOAT = 4;
    static final int DOUBLE = 5;
    static final int POINTER = 6;

    private String fname;
    private String sname;

    static final class Value {
        int ival;
        boolean bval;
        char cval;
        String sval;
        float fval;
        double dval;
        Object pval;
    }

    private final Value val = new Value();

    private int t;

    GAParameter(final String fn, final String sn, final int tp, final Object v) {
        if (fn != null) {
            fname = fn;
        } else {
            fname = null;
        }

        if (sn != null) {
            sname = sn;
        } else {
            sname = null;
        }

        t = tp;
        setvalue(v);
    }

    GAParameter(final GAParameter orig) {
        fname = sname = null;
        copy(orig);
    }

    GAParameter assing(final GAParameter orig) {
        copy(orig);
        return this;
    }

    void delete() {
    }

    private void copy(final GAParameter orig) {
        if (orig == this) {
            return;
        }

        if (orig.fname != null) {
            fname = orig.fname;
        } else {
            fname = null;
        }
        if (orig.sname != null) {
            sname = orig.sname;
        } else {
            sname = null;
        }
        t = orig.t;
        setvalue(orig.value()); // do this directly...
    }

    String fullname() {
        return fname;
    }

    String shrtname() {
        return sname;
    }

    Object value() {
        return getvalue();
    }

    Object value(final Object v) {
        setvalue(v);
        //return t == STRING ? val.sval : t == POINTER ? val.pval : val;
        return getvalue();
    }

    private Object getvalue() {
        //return t == STRING ? val.sval : t == POINTER ? val.pval : val;
        Object ret = null;
        switch (t) {
            case BOOLEAN:
                ret = new TObjBool(val.bval);
                break;
            case INT:
                ret = new TObjInt(val.ival);
                break;
            case CHAR:
                ret = new TObjChar(val.cval);
                break;
            case STRING:
                ret = val.sval;
                break;
            case FLOAT:
                ret = new TObjFloat(val.fval);
                break;
            case DOUBLE:
                ret = new TObjDouble(val.dval);
                break;
            case POINTER:
            default:
                ret = val.pval;
                break;
        }
        return ret;
    }

    int type() {
        return t;
    }

    private void setvalue(final Object v) {
        switch (t) {
            case BOOLEAN:
                val.bval = ((TObjBool) v).getValue();
                break;
            case INT:
                val.ival = ((TObjInt) v).getValue();
                break;
            case CHAR:
                val.cval = ((TObjChar) v).getValue();
                break;
            case STRING:
                if (v != val.sval) {
                    val.sval = (String) v;
                }
                break;
            case FLOAT:
                val.fval = ((TObjFloat) v).getValue();
                break;
            case DOUBLE:
                val.dval = ((TObjDouble) v).getValue();
                break;
            case POINTER:
            default:
                val.pval = v;
                break;
        }
    }
}