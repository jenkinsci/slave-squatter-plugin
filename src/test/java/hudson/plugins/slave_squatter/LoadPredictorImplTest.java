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

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.plugins.slave_squatter.squatters.CronSquatter;
import hudson.util.TimeUnit2;
import org.jvnet.hudson.test.HudsonTestCase;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Future;

/**
 * @author Kohsuke Kawaguchi
 */
public class LoadPredictorImplTest extends HudsonTestCase {
    public void testFoo() throws Exception {
        Date now = new Date();
        // make a 15min reservation for 2 slaves 10 mins from now  
        hudson.getNodeProperties().add(new NodePropertyImpl(Arrays.asList(
                new CronSquatter("2 : "+(now.getMinutes()+10)%60+" * * * * : 15")
        )));

        // this build should succeed, given that it has no estimate
        FreeStyleProject p = createFreeStyleProject();
        assertTrue(p.getEstimatedDuration()<0);
        assertBuildStatusSuccess(p.scheduleBuild2(0));

        // another one should succeed as well, since we have a very short ETA
        assertTrue(p.getEstimatedDuration()>0);
        FreeStyleBuild b = assertBuildStatusSuccess(p.scheduleBuild2(0));

        // now artificially inflate the build execution time to make a longer ETA
        Field f = Run.class.getDeclaredField("duration");
        f.setAccessible(true);
        long THREE_HOURS = TimeUnit2.HOURS.toMillis(3);
        f.set(b, THREE_HOURS);

        // build should block because it collides with the upcoming reservation
        Future<FreeStyleBuild> task = p.scheduleBuild2(0);
        Thread.sleep(3000);
        assertFalse("build should be blocked", task.isDone());

        // remove the property and we should be building
        hudson.getNodeProperties().clear();
        hudson.getQueue().scheduleMaintenance(); // emulates the effect of saving configuration
        assertBuildStatusSuccess(task);
    }
}
