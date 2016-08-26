/*
        MIT License

        Copyright (c) 2016 Emmanuel Mathi-Amorim

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.
*/

package com.mathi_amorim.emmanuel.metrictime;

import java.util.Calendar;

public class MetricTimeConverter {
    public static MetricTime convertToMetricTime(Calendar date) {
        int milliseconds = (date.get(Calendar.HOUR_OF_DAY) * 3600000) + (date.get(Calendar.MINUTE) * 60000) + (date.get(Calendar.SECOND) * 1000) + (date.get(Calendar.MILLISECOND));
        double metricMilliseconds = milliseconds / 0.864d;

        MetricTime time = new MetricTime();

        time.hours = (int)Math.floor(metricMilliseconds / 10000000);
        time.minutes = (int)Math.floor((metricMilliseconds % 10000000) / 100000);
        time.seconds = (int)Math.floor((metricMilliseconds % 100000) / 1000);
        time.milliseconds = (int)Math.floor(metricMilliseconds % 1000);

        return time;
    }

    public static Calendar convertTo24HourTime(MetricTime time) {
        return Calendar.getInstance(); //TODO finish method
    }

    public static MetricTime currentMetricTime() {
        return convertToMetricTime(Calendar.getInstance());
    }
}
