package app;

import org.jetbrains.annotations.Contract;

import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Vladimir on 03/01/18.
 **/
public class OtherFunction {
    public static String ColorToHexString(Color color) {
        return "#" + Integer.toHexString(color.getRGB()).substring(2);
    }

    @Deprecated
    public static Period GetPeriodByNumber(Date firstDate, int searchPeriod) {
        Calendar c = Calendar.getInstance();
        Date tempDate = new Date(firstDate.getTime());
        c.setTime(tempDate);
        final long DAY = 1000*60*60*24;
        tempDate.setTime(tempDate.getTime() + DAY*(2 - c.get(Calendar.DAY_OF_WEEK)) + DAY*7*(searchPeriod - 1));
        return new Period(tempDate, new Date(tempDate.getTime() + DAY*4), 5);

    }
}

