package rife.template;

public class TemplateFactoryFilters {
    public static final String PREFIX_CONFIG = "config:";
    public static final String PREFIX_L10N = "l10n:";
    public static final String PREFIX_LANG = "lang:";
    public static final String PREFIX_RENDER = "render:";

    public static final String TAG_CONFIG = "^" + PREFIX_CONFIG + "\\s*(.*)\\s*$";
    public static final String TAG_L10N = "^" + PREFIX_L10N + "\\s*([^:]*)(?::([^:]*))?\\s*$";
    public static final String TAG_LANG = "(?s)^(" + PREFIX_LANG + ".*):\\s*(\\w*)\\s*$";
    public static final String TAG_RENDER = "^" + PREFIX_RENDER + "\\s*(.*?)\\s*(:[^:]*)?$";
}
