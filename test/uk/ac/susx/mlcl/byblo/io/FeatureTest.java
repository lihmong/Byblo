/*
 * Copyright (c) 2010-2012, University of Sussex
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
package uk.ac.susx.mlcl.byblo.io;

import uk.ac.susx.mlcl.lib.io.IOUtil;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import static uk.ac.susx.mlcl.TestConstants.*;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.SimpleEnumerator;
import uk.ac.susx.mlcl.lib.io.TSVSink;
import uk.ac.susx.mlcl.lib.io.TSVSource;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class FeatureTest {

    private void copyF(File a, File b, boolean compact) throws FileNotFoundException, IOException {
        WeightedTokenSource aSrc = new WeightedTokenSource(new TSVSource(a, DEFAULT_CHARSET),
                new SimpleEnumerator<String>());
        WeightedTokenSink bSink = new WeightedTokenSink(new TSVSink(b, DEFAULT_CHARSET),
                aSrc.getEnumerator());
        bSink.setCompactFormatEnabled(compact);

        IOUtil.copy(aSrc, bSink);
        bSink.close();
    }

    @Test
    public void testFeaturesConversion() throws FileNotFoundException, IOException {
        File a = TEST_FRUIT_FEATURES;
        File b = new File(TEST_OUTPUT_DIR,
                TEST_FRUIT_FEATURES.getName() + ".compact");
        File c = new File(TEST_OUTPUT_DIR,
                TEST_FRUIT_FEATURES.getName() + ".verbose");

        copyF(a, b, true);

        assertTrue("Compact copy is smaller that verbose source.",
                b.length() <= a.length());

        copyF(b, c, false);


        assertTrue("Verbose copy is smaller that compact source.",
                c.length() >= b.length());
        assertTrue("Double converted file is not equal to origion.",
                Files.equal(a, c));
    }
    
    
    
    @Test
    public void testFeaturesEnumeratorConversion() throws FileNotFoundException, IOException {
        File a = TEST_FRUIT_FEATURES;
        File b = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_FEATURES.getName() + ".enum");
        File c = new File(TEST_OUTPUT_DIR,
                          TEST_FRUIT_FEATURES.getName() + ".str");

        Enumerator<String> idx = new SimpleEnumerator<String>();

        {
            WeightedTokenSource aSrc = new WeightedTokenSource(new TSVSource(a, DEFAULT_CHARSET), idx);
            WeightedTokenSink bSink = new WeightedTokenSink(new TSVSink(b, DEFAULT_CHARSET));
            IOUtil.copy(aSrc, bSink);
            bSink.close();
        }

        assertTrue("Compact copy is smaller that verbose source.",
                   b.length() <= a.length());

        {
            WeightedTokenSource bSrc = new WeightedTokenSource(new TSVSource(b, DEFAULT_CHARSET));
            WeightedTokenSink cSink = new WeightedTokenSink(new TSVSink(c, DEFAULT_CHARSET), idx);
            IOUtil.copy(bSrc, cSink);
            cSink.close();
        }

        assertTrue("Verbose copy is smaller that compact source.",
                   c.length() >= b.length());
        assertTrue("Double converted file is not equal to origion.",
                   Files.equal(a, c));
    }
}
