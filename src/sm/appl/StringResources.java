package sm.appl;

public final class StringResources {

    public static String loaddata = "*load data";
    public static String algorithm = "*algorithm";
    public static String testrate = "*test rate";
    public static String polydegree = "*poly degree";
    public static String fullexp = "*full expansion";
    public static String withoutopt = "*without opt.";
    public static String fastopt = "*fast build";
    public static String Aopt = "*A-optimal";
    public static String Dopt = "*D-optimal";
    public static String identify = "*IDENTIFY";
    public static String cancel = "*CANCEL";
    public static String finished = "*Finished";
    public static String identfinished = "*Identification finished.";
    public static String dataset = "*DATASET";
    public static String knotset = "*KNOTSET";
    public static String curvenumber = "*curve:";
    public static String showcurve = "*SHOW";
    public static String clearcurve = "*CLEAR";
    public static String showsubscripts = "*show subscripts";
    public static String showtestrate = "*show test rate";
    public static String appletname = "*Bezier Bernstein neurofuzzy modeling tool";
    public static String modelquality = "*BIC";
    public static String MSEestimation = "*VAR";
    public static String loadingdata = "*Loading data...";
    public static String dataloaded = "*Data was loaded.";
    public static String identificationprocess = "*Identification process...Please wait...";
    public static String usingknots = "*Using knots:";
    public static String callbackbreak = "*Callback break!";
    public static String progressflow = "*Progress: ";

    public static String g_locale = "en";
    public static String get(String name) {
        if (g_locale.equals("en"))
        {
            return getEnglish(name);
        }
        else if (g_locale.equals("uk"))
        {
            return getUkrainian(name);
        }
        else
        {
            return name;
        }
    }

    private static String getEnglish(String name) {
        final String[][] content = {
      { loaddata, "load data" },
      { algorithm , "algorithm:" } ,
      { testrate, "test rate:" } ,
      { polydegree  , "poly degree"},
      { fullexp  , "full expansion" },
      { withoutopt  , "without opt."},
      { fastopt  , "fast build"},
      { Aopt  , "A-optimal"},
      { Dopt  , "D-optimal"},
      { identify  , "IDENTIFY"},
      { cancel  , "CANCEL"},
      { finished  , "Finished"},
      { identfinished  , "Identification finished."},
      { dataset  , "DATASET"},
      { knotset  , "KNOTSET"},
      { curvenumber  , "curve:"},
      { showcurve  , "SHOW"},
      { clearcurve  , "CLEAR"},
      { showsubscripts  , "show subscripts"},
      { showtestrate  , "show test rate"},
      { appletname  , "Bezier Bernstein neurofuzzy modeling tool"},
      { modelquality  , "BIC"},
      { MSEestimation  , "VAR"},
      { loadingdata  , "Loading data..."},
      { dataloaded  , "Data was loaded."},
      { identificationprocess  , "Identification process... Please wait..."},
      { usingknots  , "Using knots:"},
      { callbackbreak  , "Callback break!"},
      { progressflow  , "Progress: "},
        };
        for (int k = 0;  k < content.length; k++) {
        if (content[k][0] == name)
            return content[k][1];
        }
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    private static String getUkrainian(String name) {
        final String[][] content = {
      { loaddata, "�����������" },
      { algorithm , "��������:" } ,
      { testrate, "����:" } ,
      { polydegree  , "������:"},
      { fullexp  , "���� �������" },
      { withoutopt  , "�������"},
      { fastopt  , "�������"},
      { Aopt  , "�-���."},
      { Dopt  , "D-���."},
      { identify  , "������Բ������"},
      { cancel  , "²�̲����"},
      { finished  , "100%"},
      { identfinished  , "������������� ��������"},
      { dataset  , "�������"},
      { knotset  , "��������Ͳ �����"},
      { curvenumber  , "�����:"},
      { showcurve  , "��������"},
      { clearcurve  , "������"},
      { showsubscripts  , "�������� ������"},
      { showtestrate  , "�������� �������� ����������"},
      { appletname  , "����������� ����� � ���� ����������"},
      { modelquality  , "���"},
      { MSEestimation  , "����"},
      { loadingdata  , "������������ �����..."},
      { dataloaded  , "��� ����������."},
      { identificationprocess  , "������ �������������... ���������..."},
      { usingknots  , "��������� �����:"},
      { callbackbreak  , "�������� ������������!"},
      { progressflow  , "���������: "},
        };
        for (int k = 0;  k < content.length; k++) {
        if (content[k][0] == name)
            return content[k][1];
        }
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

}
