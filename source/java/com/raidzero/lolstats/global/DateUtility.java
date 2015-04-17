package com.raidzero.lolstats.global;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by raidzero on 4/10/15.
 */
public class DateUtility {
    private static final String tag = "DateUtility";

    /**
     * this gets a timestamp 15 minutes ago.
     * I think 15 minutes is a good range for games to be played
     * @return long unix epoch mins ago
     */
    public static long getTimestamp() {
        Calendar c = Calendar.getInstance();
        int minute = c.get(Calendar.MINUTE);

        // is it divisible by 5?
        int minsPast = minute % 5;
        if (minsPast == 0) {
            // already a 5 minute interval. subtract 15 mins
            minute -= 15;
        } else {
            // get last valid minute and then subtract 15 from that
            minute -= (minsPast + 15);
        }

        // force minutes & seconds back
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        //return (c.getTime().getTime()) / 1000; // dont care about millisecond

        /**
         * well, apparently there are no more URF matches. so that awesome stuff up there is worthless.
         * starting at 1428917100; pick one of 3500 random timestamps in the past and return it
         */

        long timestamp = 1428917100;
        int random = new Random().nextInt(3500);

        return timestamp - (random * 5 * 60);
    }
}
