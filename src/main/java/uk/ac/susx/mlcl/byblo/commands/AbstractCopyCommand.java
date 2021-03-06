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

import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.lib.MiscUtil;
import uk.ac.susx.mlcl.lib.commands.AbstractCommand;
import uk.ac.susx.mlcl.lib.commands.FilePipeDelegate;
import uk.ac.susx.mlcl.lib.events.ProgressDelegate;
import uk.ac.susx.mlcl.lib.events.ProgressEvent;
import uk.ac.susx.mlcl.lib.events.ProgressListener;
import uk.ac.susx.mlcl.lib.events.ProgressReporting;
import uk.ac.susx.mlcl.lib.io.Files;
import uk.ac.susx.mlcl.lib.io.ObjectSink;
import uk.ac.susx.mlcl.lib.io.ObjectSource;
import uk.ac.susx.mlcl.lib.tasks.ObjectPipeTask;

import javax.annotation.CheckReturnValue;
import java.io.*;
import java.nio.charset.Charset;

/**
 * Abstract super class for all tasks that require copying data from one file to
 * another.
 * <p/>
 *
 * @param <T>
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Parameters(commandDescription = "Sort a file.")
public abstract class AbstractCopyCommand<T> extends AbstractCommand
        implements ProgressReporting {

    private static final Log LOG = LogFactory.getLog(AbstractCopyCommand.class);

    private final ProgressDelegate progress = new ProgressDelegate(this, true);

    @ParametersDelegate
    private FilePipeDelegate filesDelegate = new FilePipeDelegate();

    public AbstractCopyCommand(File sourceFile, File destinationFile, Charset charset) {
        filesDelegate = new FilePipeDelegate(sourceFile, destinationFile, charset);
    }

    public AbstractCopyCommand(File sourceFile, File destinationFile) {
        this(sourceFile, destinationFile, Files.DEFAULT_CHARSET);
    }

    public AbstractCopyCommand() {
    }

    public FilePipeDelegate getFilesDelegate() {
        return filesDelegate;
    }

    public final void setCharset(Charset charset) {
        filesDelegate.setCharset(charset);
    }

    final Charset getCharset() {
        return filesDelegate.getCharset();
    }

    public final void setSourceFile(File sourceFile) throws NullPointerException {
        filesDelegate.setSourceFile(sourceFile);
    }

    public final void setDestinationFile(File destinationFile) throws NullPointerException {
        filesDelegate.setDestinationFile(destinationFile);
    }

    public final File getSourceFile() {
        return filesDelegate.getSourceFile();
    }

    public final File getDestinationFile() {
        return filesDelegate.getDestinationFile();
    }

    @Override
    @CheckReturnValue
    public boolean runCommand() {
        try {
            progress.setState(State.PENDING);
            LOG.debug(MiscUtil.memoryInfoString());

            ObjectSource<T> src = openSource(getFilesDelegate().getSourceFile());
            ObjectSink<T> snk =
                    openSink(getFilesDelegate().getDestinationFile());

            ObjectPipeTask<T> task = new ObjectPipeTask<T>();
            task.setSource(src);
            task.setSink(snk);

            task.addProgressListener(new ProgressListener() {
                @Override
                public void progressChanged(ProgressEvent progressEvent) {
                    LOG.info(progressEvent.getSource().getProgressReport());
                }
            });

            task.run();

            while (task.isExceptionTrapped())
                task.throwTrappedException();

            if (src instanceof Closeable)
                ((Closeable) src).close();
            if (snk instanceof Flushable)
                ((Flushable) snk).flush();
            if (snk instanceof Closeable)
                ((Closeable) snk).close();

            progress.setState(State.COMPLETED);
            LOG.debug(MiscUtil.memoryInfoString());

            return true;

        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getName() {
        return "copy";
    }

    @Override
    public void removeProgressListener(ProgressListener progressListener) {
        progress.removeProgressListener(progressListener);
    }

    @Override
    public boolean isProgressPercentageSupported() {
        return progress.isProgressPercentageSupported();
    }

    @Override
    public State getState() {
        return progress.getState();
    }

    @Override
    public String getProgressReport() {
        return progress.getProgressReport();
    }

    @Override
    public int getProgressPercent() {
        return progress.getProgressPercent();
    }

    @Override
    public ProgressListener[] getProgressListeners() {
        return progress.getProgressListeners();
    }

    @Override
    public void addProgressListener(ProgressListener progressListener) {
        progress.addProgressListener(progressListener);
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("name", getName()).
                add("files", getFilesDelegate());
    }

    /**
     *
     * @param file
     * @return
     * @throws FileNotFoundException if the specified file did not exist
     * @throws IOException
     */
    protected abstract ObjectSource<T> openSource(File file) throws IOException;

    /**
     *
     * @param file
     * @return
     * @throws FileNotFoundException if the specified file did not exist
     * @throws IOException
     */
    protected abstract ObjectSink<T> openSink(File file) throws IOException;
}
