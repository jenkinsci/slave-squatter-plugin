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
