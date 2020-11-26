package com.vrockk.common;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeAgo {

    public static final Map<String, Long> times = new LinkedHashMap<>();

    static {
        times.put("year", TimeUnit.DAYS.toMillis(365));
        times.put("month", TimeUnit.DAYS.toMillis(30));
        times.put("week", TimeUnit.DAYS.toMillis(7));
        times.put("day", TimeUnit.DAYS.toMillis(1));
        times.put("hour", TimeUnit.HOURS.toMillis(1));
        times.put("minute", TimeUnit.MINUTES.toMillis(1));
        times.put("second", TimeUnit.SECONDS.toMillis(1));
    }

    public static String toRelative(long duration, int maxLevel) {
        StringBuilder res = new StringBuilder();
        int level = 0;
        for (Map.Entry<String, Long> time : times.entrySet()){
            long timeDelta = duration / time.getValue();
            if (timeDelta > 0){
                res.append(timeDelta)
                        .append(" ")
                        .append(time.getKey())
                        .append(timeDelta > 1 ? "s" : "")
                        .append(", ");
                duration -= time.getValue() * timeDelta;
                level++;
            }
            if (level == maxLevel){
                break;
            }
        }
        if ("".equals(res.toString())) {
            return "0 seconds ago";
        } else {
            res.setLength(res.length() - 2);
            res.append(" ago");
            return res.toString();
        }
    }

    public static String toRelative(long duration) {
        return toRelative(duration, times.size());
    }

    public static String toRelative(Date start, Date end){
        assert start.after(end);
        return toRelative(end.getTime() - start.getTime());
    }

    public static String toRelative(Date start, Date end, int level){
        assert start.after(end);
        return toRelative(end.getTime() - start.getTime(), level);
    }


    public static  String toSpecificFormat(String duration)
    {
        try
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            Date past = format.parse(duration);
            Date now = new Date();
            long seconds=TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
            long minutes=TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());
            long hours=TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());
            long days=TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime());
//
//          System.out.println(TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime()) + " milliseconds ago");
//          System.out.println(TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime()) + " minutes ago");
//          System.out.println(TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime()) + " hours ago");
//          System.out.println(TimeUnit.MILLISECONDS.toDays(now.getTime() - past.getTime()) + " days ago");
         if(seconds<60)
            {
                return  seconds+" seconds ago";
            }
            else if(minutes<60)
        {
            return minutes+" minutes ago";
        }
        else if(hours<24)
        {
            return hours+" hours ago";
        }
        else
        {
            return days+" days ago";
        }
        }
        catch (Exception j){
            j.printStackTrace();
        }
        return  "";
    }

}
