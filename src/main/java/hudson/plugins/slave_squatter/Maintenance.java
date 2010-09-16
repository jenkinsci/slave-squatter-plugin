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

            int r = p.sizeOfReservation(now);
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
