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
package uk.ac.susx.mlcl.byblo.commands;

import com.beust.jcommander.ParametersDelegate;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumerating;
import uk.ac.susx.mlcl.byblo.enumerators.SingleEnumeratingDelegate;
import uk.ac.susx.mlcl.byblo.io.BybloIO;
import uk.ac.susx.mlcl.byblo.io.Token;
import uk.ac.susx.mlcl.byblo.io.WeightSumReducerObjectSink;
import uk.ac.susx.mlcl.byblo.io.Weighted;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.MemoryUsage;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.SeekableObjectSource;
import uk.ac.susx.mlcl.lib.io.Tell;

import javax.annotation.CheckReturnValue;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class ExternalSortEntriesCommand extends AbstractExternalSortCommand<Weighted<Token>> {

    private static final long serialVersionUID = 1L;

    @ParametersDelegate
    private SingleEnumerating indexDelegate = new SingleEnumeratingDelegate();

    public ExternalSortEntriesCommand(
            File sourceFile, File destinationFile, Charset charset,
            SingleEnumerating indexDelegate) {
        super(sourceFile, destinationFile, charset);
        setIndexDelegate(indexDelegate);
    }

    public ExternalSortEntriesCommand() {
    }

    @Override
    @CheckReturnValue
    public boolean runCommand() {
        try {
            boolean result = super.runCommand();
            indexDelegate.saveEnumerator();
            indexDelegate.closeEnumerator();
            return result;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    protected ObjectSink<Weighted<Token>> openSink(File file) throws IOException {
        return new WeightSumReducerObjectSink<Token>(BybloIO.openEntriesSink(file, getCharset(), indexDelegate));
    }

    @Override
    protected long getBytesPerObject() {
        return new MemoryUsage().add(new Weighted<Token>(new Token(1), 1)).getInstanceSizeBytes();
    }

    @Override
    protected SeekableObjectSource<Weighted<Token>, Tell> openSource(File file) throws IOException {
        return BybloIO.openEntriesSource(file, getCharset(), indexDelegate);
    }

    public final SingleEnumerating getIndexDelegate() {
        return indexDelegate;
    }

    final void setIndexDelegate(SingleEnumerating indexDelegate) {
        Checks.checkNotNull("indexDelegate", indexDelegate);
        this.indexDelegate = indexDelegate;
    }
}
