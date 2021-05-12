package common;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class IdFormatter {
    public static DecimalFormat df = new DecimalFormat("0.000000000");
    static{
        df.setRoundingMode(RoundingMode.DOWN);
    }
}
