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
package hudson.plugins.slave_squatter;

import hudson.model.AbstractDescribableImpl;

import java.util.Date;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class SlaveSquatter extends AbstractDescribableImpl<SlaveSquatter> {
    /**
     * Returns the number of executors that should be reserved at the specified time.
     * <p>
     * The timestamp is the same format as {@link Date#getTime()}. The precision is 1 minute,
     * and thus the caller should set the second and millisecond portion to 00.000.
     */
    public abstract int sizeOfReservation(long timestamp);

    /**
     * Given the timestamp, return the nearest future timestamp (including itself --- the ceil semantics)
     * when the size of the reservation changes.
     *
     * <p>
     * Hudson uses this information to figure out the scheduling that takes the future capacity change
     * into account.
     */
    public abstract long timeOfNextChange(long timestamp);

    
}
