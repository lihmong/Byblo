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
package uk.ac.susx.mlcl.lib.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.base.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.CheckReturnValue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Parameters(commandDescription = "Delete a file.")
public class FileDeleteCommand extends AbstractCommand {

    private static final Log LOG = LogFactory.getLog(FileDeleteCommand.class);

    @Parameter(names = {"-f", "--file"},
            description = "File to deleted",
            validateWith = InputFileValidator.class, required = true)
    private File file = null;

    public FileDeleteCommand(File file) {
        setFile(file);
    }

    public FileDeleteCommand() {
    }

    @Override
    @CheckReturnValue
    public boolean runCommand() {
        if (LOG.isInfoEnabled())
            LOG.info("Deleting file \"" + getFile() + "\".");
        if (file == null)
            throw new NullPointerException("file is null");
        if (!file.exists())
            throw new RuntimeException(new FileNotFoundException("Unable to delete file because it "
                    + "doesn't exist: \"" + file + "\""));

        if (!file.delete())
            throw new RuntimeException(new IOException("Unable to delete file: \"" + file + "\""));

        return true;
    }

    public final File getFile() {
        return file;
    }

    final void setFile(final File file)
            throws NullPointerException {
        if (file == null)
            throw new NullPointerException("file is null");
        this.file = file;
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().add("file", file);
    }
}
