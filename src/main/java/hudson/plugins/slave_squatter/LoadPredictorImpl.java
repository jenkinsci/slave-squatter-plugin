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
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.queue.FutureLoad;
import hudson.model.queue.LoadPredictor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Puts slave reservations into the future load prediction.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class LoadPredictorImpl extends LoadPredictor {
    @Override
    public Iterable<FutureLoad> predict(Computer computer, long start, long end) {
        Node n = computer.getNode();
        if (n==null)    return Collections.emptyList();

        NodePropertyImpl p = n.getNodeProperties().get(NodePropertyImpl.class);
        if (p==null)    return Collections.emptyList();

        int cnt=0; // a safety bent to avoid taking too much time
        List<FutureLoad> r = new ArrayList<FutureLoad>();
        for (long t=start; t<end && cnt<256; cnt++) {
            int sz = p.sizeOfReservation(t);
            long t2 = p.timeOfNextChange(t+1);
            if (sz>0)
                r.add(new FutureLoad(t,t2-t,sz));
            t = t2;
        }
        return r;
    }
}
