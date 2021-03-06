/*
 * Copyright (c) 2010-2013, University of Sussex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of the University of Sussex nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.mlcl.byblo.tasks;

import org.junit.Test;
import uk.ac.susx.mlcl.TestConstants;
import uk.ac.susx.mlcl.byblo.Tools;
import uk.ac.susx.mlcl.lib.test.ExitTrapper;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * These are old test that are pretty much deprecated, but they are maintained because it's generally a bad idea to
 * remove tests if you don't have to.
 *
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class AllPairsTaskTest {

    @Test
    public void testMainRun() throws Exception {
        try {
            ExitTrapper.enableExistTrapping();
            Tools.main(new String[]{"allpairs",
                    "-i", TestConstants.TEST_FRUIT_EVENTS.toString(),
                    "-if", TestConstants.TEST_FRUIT_FEATURES.toString(),
                    "-o", new File(TestConstants.TEST_OUTPUT_DIR, "bnc-gramrels-fruit.out").toString()});
        } finally {
            ExitTrapper.disableExitTrapping();
        }
    }

    @Test
    public void testMainRun_Indexed() throws Exception {
        try {
            ExitTrapper.enableExistTrapping();
            Tools.main(new String[]{"allpairs",
                    "-i", TestConstants.TEST_FRUIT_SKIP_INDEXED_EVENTS.toString(),
                    "-if", TestConstants.TEST_FRUIT_SKIP_INDEXED_FEATURES.toString(),
                    "-o", new File(TestConstants.TEST_OUTPUT_DIR, "bnc-gramrels-fruit.indexed.out").toString(),
                    "--enumerated-entries",
                    "--enumerated-features"});
        } finally {
            ExitTrapper.disableExitTrapping();
        }
    }

    @Test
    public void testExitStatus() throws Exception {
        try {
            ExitTrapper.enableExistTrapping();
            Tools.main(new String[]{"allpairs"});
        } catch (ExitTrapper.ExitException ex) {
            assertTrue("Expecting non-zero exit status.", ex.getStatus() != 0);
        } finally {
            ExitTrapper.disableExitTrapping();
        }
    }

}
