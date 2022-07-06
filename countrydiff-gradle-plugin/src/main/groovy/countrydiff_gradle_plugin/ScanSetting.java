package countrydiff_gradle_plugin;

import java.util.ArrayList;

public class ScanSetting {
    public static final String PLUGIN_NAME = "countrydiff_gradle_plugin";
    public static final String GENERATE_TO_CLASS_NAME = "com/example/countrydiff_api/CountryDiff";
    public static final String GENERATE_TO_CLASS_FILE_NAME = GENERATE_TO_CLASS_NAME + ".class";
    public static final String GENERATE_TO_METHOD_NAME = "addTransformMap";
    static final String COUNTRY_DIFF_CLASS_PACKAGE_NAME = "com/example/myapplication";
    private static final String INTERFACE_PACKAGE_NAME = "com/example/countrydiff_annotation/";
    public static final String REGISTER_METHOD_NAME = "registerMap";
    public String interfaceName = "";
    public ArrayList<String> classList = new ArrayList<>();

    public ScanSetting(String interfaceName) {
        this.interfaceName = INTERFACE_PACKAGE_NAME + interfaceName;
    }

}