package me.ghost.printapi.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    /**
     * Rounds all numbers in a string
     * @param str A string with numbers in it
     * @return The supplied string with all numbers rounded
     * @author GhostTypes
     */
    public static String roundString(String str) {
        String regex = "\\d+(\\.\\d+)?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            double originalNumber = Double.parseDouble(matcher.group());
            long roundedNumber = Math.round(originalNumber);
            matcher.appendReplacement(result, String.valueOf(roundedNumber));
        }
        matcher.appendTail(result);
        return result.toString();
    }

}
