/*
 * The MIT License
 *
 * Copyright (c) 2010, InfraDNA, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.slave_squatter.squatters;

import antlr.ANTLRException;
import hudson.Extension;
import hudson.plugins.slave_squatter.SlaveSquatter;
import hudson.plugins.slave_squatter.SlaveSquatterDescriptor;
import hudson.scheduler.CronTab;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Reserves a slave with a cron-like syntax that specifies the start of the reservation,
 * duration, and the size of the reservation.
 *
 * @author Kohsuke Kawaguchi
 */
public class CronSquatter extends SlaveSquatter {

    public final String format;
    private transient List<Entry> entries;

    public static final class Entry {
        public final CronTab cron;
        /**
         * Duration of the reservation, in milliseconds.
         */
        public final long duration;
        /**
         * How many executors do we reserve?
         */
        public final int size;

        public Entry(int size, CronTab cron, long duration) {
            this.cron = cron;
            this.duration = duration;
            this.size = size;
        }

        public int sizeOfReservation(long timestamp) {
            long start = cron.floor(timestamp).getTimeInMillis();
            if (start<=timestamp && timestamp<start+duration)
                return size;
            return 0;
        }

        public long timeOfNextChange(long timestamp) {
            long end = cron.floor(timestamp).getTimeInMillis()+duration;
            long start = cron.ceil(timestamp).getTimeInMillis();

            if (timestamp<end)  return Math.min(end,start);
            return start;
        }
    }

    @DataBoundConstructor
    public CronSquatter(String format) {
        this.format = format;
        readResolve();
    }

    private Object readResolve() {
        entries = new ArrayList<Entry>();
        int lineNumber = 0;
        for (String line : format.split("\\r?\\n")) {
            lineNumber++;
            line = line.trim();
            if(line.length()==0 || line.startsWith("#"))
                continue;   // ignorable line

            String[] tokens = line.split(":");
            if (tokens.length!=3)
                throw new IllegalArgumentException("3 tokens separated by ':' are expected, but found "+tokens.length+" in "+line);

            try {
                entries.add(new Entry(
                        Integer.parseInt(tokens[0].trim()),
                        new CronTab(tokens[1].trim(),lineNumber),
                        Long.parseLong(tokens[2].trim())*60*1000));
            } catch (ANTLRException e) {
                throw new IllegalArgumentException(hudson.scheduler.Messages.CronTabList_InvalidInput(line,e.toString()),e);
            }
        }
        return this;
    }

    @Override
    public int sizeOfReservation(long timestamp) {
        int r=0;
        for (Entry e : entries)
            r += e.sizeOfReservation(timestamp);
        return r;
    }

    @Override
    public long timeOfNextChange(long timestamp) {
        long l = Long.MAX_VALUE;
        for (Entry e : entries)
            l = Math.min(l,e.timeOfNextChange(timestamp));
        return l;
    }

    @Extension
    public static class DescriptorImpl extends SlaveSquatterDescriptor {
        public DescriptorImpl() {
        }

        @Override
        public String getDisplayName() {
            return Messages.CronSquatter_DisplayName();
        }
    }
}
