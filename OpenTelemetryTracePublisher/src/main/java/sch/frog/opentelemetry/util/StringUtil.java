package sch.frog.opentelemetry.util;

public class StringUtil {

    public static boolean isBlank(String str){
        if (str != null && str.length() != 0) {
            for (int i = 0, len = str.length(); i < len; i++) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

}
