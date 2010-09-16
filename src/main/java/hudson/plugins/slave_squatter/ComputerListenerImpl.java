package hudson.plugins.slave_squatter;

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.TaskListener;
import hudson.slaves.ComputerListener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Re-run maintenance if the node configuration changes.
 *
 * @author Kohsuke Kawaguchi
 */
@Extension
public class ComputerListenerImpl extends ComputerListener {
    @Override
    public void onOnline(Computer c, TaskListener listener) throws IOException, InterruptedException {
        // so that we can reserve executors right away
        maintain();
    }

    @Override
    public void onConfigurationChange() {
        maintain();
    }

    private void maintain() {
        try {
            Maintenance.get().doRun();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to rerun the squatter maintenance",e);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ComputerListenerImpl.class.getName());
}
