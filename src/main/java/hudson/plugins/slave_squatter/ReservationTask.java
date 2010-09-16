package hudson.plugins.slave_squatter;

import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Queue.Executable;
import hudson.model.Queue.TransientTask;
import hudson.model.ResourceList;
import hudson.model.queue.AbstractQueueTask;
import hudson.model.queue.CauseOfBlockage;
import org.acegisecurity.AccessDeniedException;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class ReservationTask extends AbstractQueueTask implements TransientTask {
    private final Node node;

    public ReservationTask(Node node) {
        this.node = node;
    }

    public boolean isBuildBlocked() {
        return false;
    }

    public String getWhyBlocked() {
        return null;
    }

    public CauseOfBlockage getCauseOfBlockage() {
        return null;
    }

    public String getName() {
        return "External reservation";
    }

    public String getFullDisplayName() {
        return "External reservation";
    }

    public void checkAbortPermission() {
        throw new AccessDeniedException("Reservation cannot be aborted");
    }

    public boolean hasAbortPermission() {
        return false;
    }

    public String getUrl() {
        return null; // TODO: maybe to the config page?
    }

    public boolean isConcurrentBuild() {
        return false;
    }

    public String getDisplayName() {
        return getFullDisplayName();
    }

    public Label getAssignedLabel() {
        return node.getSelfLabel();
    }

    public Node getLastBuiltOn() {
        return node;
    }

    public long getEstimatedDuration() {
        return -1;
    }

    public Executable createExecutable() throws IOException {
        return new ReservationExecutable(this);
    }

    public Object getSameNodeConstraint() {
        return null;
    }

    public ResourceList getResourceList() {
        return new ResourceList();
    }
}
