package hudson.plugins.slave_squatter;

import hudson.model.Queue.Executable;

/**
 * @author Kohsuke Kawaguchi
 */
public class ReservationExecutable implements Executable {
    private final ReservationTask parent;

    public ReservationExecutable(ReservationTask parent) {
        this.parent = parent;
    }

    public ReservationTask getParent() {
        return parent;
    }

    public synchronized void run() {
        try {
            while (true) {
                wait();
            }
        } catch (InterruptedException e) {
            // block forever until interrupted
        }
    }
}
