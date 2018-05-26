package ru.kazantsev.template.domain;


import ru.kazantsev.template.util.AndroidSystemUtils;

/**
 * Created by Rufim on 07.01.2015.
 */
public class Constants {

    public static class App {
        public static final int CORES = AndroidSystemUtils.getNumberOfCores();
        public static boolean USE_MOXY = false;
    }

    public static class Assets {
        public static final String ROBOTO_FONT_PATH = "fonts/roboto/Roboto-Regular.ttf";
        public static final String DROID_SANS_FONT_PATH = "fonts/droidsans/DroidSans.ttf";
        public static final String ROBOTO_Capture_it = "fonts/Capture-it/Capture_it.ttf";
    }

    public static class ArgsName {
        public static final String LAST_FRAGMENT_TAG = "last_fragment_tag";
        public static final String LAST_FRAGMENT = "last_fragment";
        public static final String FRAGMENT_CACHE = "fragment_cache";
        public static final String FRAGMENT_CLASS = "fragment_class";
        public static final String FRAGMENT_ARGS = "fragment_args";
        public static final String FRAGMENT_EXCEPTION = "fragment_exception";
        public static final String MESSAGE = "message";
        public static final String RESOURCE_ID = "resource_id";
        public static final String CONFIG_CHANGE = "config_change";
    }

    public static class Pattern {
        public static final String TIME_PATTERN = "HH:mm";
        public static final String DATA_PATTERN = "dd-MM-yyyy";
        public static final String DATA_PATTERN_INVERT = "yyyy-MM-dd";
        public static final String REVERSE_DATA_PATTERN = "MM-dd-yyyy";
        public static final String DATA_TIME_PATTERN = "dd-MM-yyyy HH:mm";
        public static final String DATA_ISO_8601_24H_FULL_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    }

    public static class Preferences {
        public static final String PREF_COOKIES = "pref_cookies";
    }

}
