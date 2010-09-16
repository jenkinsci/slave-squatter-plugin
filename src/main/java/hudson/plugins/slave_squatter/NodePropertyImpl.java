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

import hudson.Extension;
import hudson.Util;
import hudson.model.Node;
import hudson.model.Saveable;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.util.DescribableList;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.List;

import static hudson.Util.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class NodePropertyImpl extends NodeProperty<Node> {
    private final DescribableList<SlaveSquatter,SlaveSquatterDescriptor> squatters =
            new DescribableList<SlaveSquatter,SlaveSquatterDescriptor>(Saveable.NOOP);

    @DataBoundConstructor
    public NodePropertyImpl(List<? extends SlaveSquatter> squatters) throws IOException {
        this.squatters.replaceBy(fixNull(squatters));
    }

    public DescribableList<SlaveSquatter,SlaveSquatterDescriptor> getSquatters() {
        return squatters;
    }

    public int sizeOfReservation(long timestamp) {
        int r=0;
        for (SlaveSquatter s : squatters)
            r += s.sizeOfReservation(timestamp);
        return r;
    }

    public long timeOfNextChange(long timestamp) {
        long t = Long.MAX_VALUE;
        for (SlaveSquatter s : squatters)
            t = Math.min(t,s.timeOfNextChange(timestamp));
        return t;
    }

    @Extension
    public static class DescriptorImpl extends NodePropertyDescriptor {
        @Override
        public String getDisplayName() {
            return "External Slave Reservation";
        }

        public List<SlaveSquatterDescriptor> getSquatterDescriptors() {
            return SlaveSquatterDescriptor.all();
        }
    }
}
