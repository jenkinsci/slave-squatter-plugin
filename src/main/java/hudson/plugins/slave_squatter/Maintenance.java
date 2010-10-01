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
import hudson.model.Executor;
import hudson.model.Hudson;
import hudson.model.Node;
import hudson.model.PeriodicWork;
import hudson.model.Queue;
import hudson.util.TimeUnit2;

/**
 * Runs every so often to adjust the reservation.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class Maintenance extends PeriodicWork {
    private boolean inprogress;

    @Override
    public long getRecurrencePeriod() {
        return TimeUnit2.SECONDS.toMillis(15);
    }

    @Override
    protected void doRun() throws Exception {
        // if a maintenance is already in progress, skip.
        synchronized (this) {
            if (inprogress)     return;
            inprogress = true;
        }
        try {
            _run();
        } finally {
            synchronized (this) {
                inprogress = false;
            }
        }
    }

    private void _run() {
        long now = System.currentTimeMillis();
        Hudson h = Hudson.getInstance();
        Queue q = h.getQueue();

        // kill of any reservations that hanged around in the queue
        synchronized (q) {
            for (Queue.Item i : q.getItems()) {
                if (i.task instanceof ReservationTask) {
                    q.cancel(i.task);
                }
            }
        }

        // adjust the number of reservations
        for (Computer c : h.getComputers()) {
            Node n = c.getNode();
            if (n == null) continue;
            NodePropertyImpl p = n.getNodeProperties().get(NodePropertyImpl.class);
            if (p == null) continue;

            int r = p.sizeOfReservation(c,now);
            int current=0;
            int idle=0;
            for (Executor e : c.getExecutors()) {
                if (e.getCurrentExecutable() instanceof ReservationExecutable) {
                    current++;
                    if (r<current)
                        e.interrupt(); // shouldn't be reserved any longer
                } else if (e.isIdle()) {
                    idle++;
                }
            }

            for (int i=current; i<r && idle>0; i++,idle--) {
                q.schedule(new ReservationTask(n),0);
            }
        }
    }

    public static Maintenance get() {
        return PeriodicWork.all().get(Maintenance.class);
    }
}
