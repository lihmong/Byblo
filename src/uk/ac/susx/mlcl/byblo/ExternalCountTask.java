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
package uk.ac.susx.mlcl.byblo;

import uk.ac.susx.mlcl.byblo.tasks.MergeTask;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.base.Objects;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.susx.mlcl.byblo.io.*;
import uk.ac.susx.mlcl.byblo.tasks.SortTask;
import uk.ac.susx.mlcl.lib.Checks;
import uk.ac.susx.mlcl.lib.Enumerator;
import uk.ac.susx.mlcl.lib.Enumerators;
import uk.ac.susx.mlcl.lib.io.*;
import uk.ac.susx.mlcl.lib.tasks.AbstractParallelCommandTask;
import uk.ac.susx.mlcl.lib.tasks.InputFileValidator;
import uk.ac.susx.mlcl.lib.tasks.OutputFileValidator;
import uk.ac.susx.mlcl.lib.tasks.Task;

/**
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk%gt;
 */
@Parameters(commandDescription = "Freqency count a structured input instance file.")
public class ExternalCountTask extends AbstractParallelCommandTask {

    private static final Log LOG = LogFactory.getLog(ExternalCountTask.class);

    private static final int DEFAULT_MAX_CHUNK_SIZE = ChunkTask.DEFAULT_MAX_CHUNK_SIZE;

    protected static final String KEY_TASK_TYPE = "KEY_TASK_TYPE";

    protected static final String KEY_DATA_TYPE = "KEY_DATA_TYPE";

    protected static final String KEY_SRC_FILE = "KEY_SRC_FILE";

    protected static final String KEY_SRC_FILE_A = "KEY_SRC_FILE_A";

    protected static final String KEY_SRC_FILE_B = "KEY_SRC_FILE_B";

    protected static final String KEY_DST_FILE = "KEY_DST_FILE";

    protected static final String VALUE_TASK_TYPE_DELETE = "VALUE_TASK_TYPE_DELETE";

    protected static final String VALUE_TASK_TYPE_COUNT = "VALUE_TASK_TYPE_COUNT";

    protected static final String VALUE_TASK_TYPE_MERGE = "VALUE_TASK_TYPE_MERGE";

    protected static final String VALUE_TASK_TYPE_SORT = "VALUE_TASK_TYPE_SORT";

    protected static final String VALUE_DATA_TYPE_INPUT = "VALUE_DATA_TYPE_INPUT";

    protected static final String VALUE_DATA_TYPE_ENTRIES = "VALUE_DATA_TYPE_ENTRIES";

    protected static final String VALUE_DATA_TYPE_FEATURES = "VALUE_DATA_TYPE_FEATURES";

    protected static final String VALUE_DATA_TYPE_EVENTS = "VALUE_DATA_TYPE_EVENTS";

    private static final boolean DEBUG = true;

    @Parameter(names = {"-C", "--chunk-size"},
    description = "Number of lines per work unit. Lrger value increase performance and memory usage.")
    private int maxChunkSize = DEFAULT_MAX_CHUNK_SIZE;

    @Parameter(names = {"-i", "--input"},
    required = true,
    description = "Input instances file",
    validateWith = InputFileValidator.class)
    private File inputFile;

    @Parameter(names = {"-oef", "--output-entry-features"},
    required = true,
    description = "Output entry-feature frequencies file",
    validateWith = OutputFileValidator.class)
    private File entryFeaturesFile = null;

    @Parameter(names = {"-oe", "--output-entries"},
    required = true,
    description = "Output entry frequencies file",
    validateWith = OutputFileValidator.class)
    private File entriesFile = null;

    @Parameter(names = {"-of", "--output-features"},
    required = true,
    description = "Output feature frequencies file",
    validateWith = OutputFileValidator.class)
    private File featuresFile = null;

    @Parameter(names = {"-T", "--temporary-directory"},
    description = "Directory used for holding temporary files.")
    private TempFileFactory tempFileFactory = new TempFileFactory();

    @Parameter(names = {"-c", "--charset"},
    description = "Character encoding to use for reading and writing files.")
    private Charset charset = Files.DEFAULT_CHARSET;

    @ParametersDelegate
    protected TwoIndexDeligate indexDeligate = new TwoIndexDeligate();

    private Queue<File> mergeEntryQueue;

    private Queue<File> mergeFeaturesQueue;

    private Queue<File> mergeEntryFeatureQueue;

    public ExternalCountTask(
            final File instancesFile, final File entryFeaturesFile,
            final File entriesFile, final File featuresFile, Charset charset,
            int maxChunkSize) {
        this(instancesFile, entryFeaturesFile, entriesFile, featuresFile);
        setCharset(charset);
        setMaxChunkSize(maxChunkSize);
    }

    public ExternalCountTask(
            final File instancesFile, final File entryFeaturesFile,
            final File entriesFile, final File featuresFile) {
        setInstancesFile(instancesFile);
        setEntryFeaturesFile(entryFeaturesFile);
        setEntriesFile(entriesFile);
        setFeaturesFile(featuresFile);
    }

    public ExternalCountTask() {
        super();
    }

    public TempFileFactory getTempFileFactory() {
        return tempFileFactory;
    }

    public void setTempFileFactory(TempFileFactory tempFileFactory) {
        Checks.checkNotNull("tempFileFactory", tempFileFactory);
        this.tempFileFactory = tempFileFactory;
    }

    public final Charset getCharset() {
        return charset;
    }

    public final void setCharset(Charset charset) {
        Checks.checkNotNull(charset);
        this.charset = charset;
    }

    public final int getMaxChunkSize() {
        return maxChunkSize;
    }

    public final void setMaxChunkSize(int maxChunkSize) {
        if (maxChunkSize < 1)
            throw new IllegalArgumentException("maxChunkSize < 1");
        this.maxChunkSize = maxChunkSize;
    }

    public final File getFeaturesFile() {
        return featuresFile;
    }

    public final void setFeaturesFile(final File contextsFile)
            throws NullPointerException {
        if (contextsFile == null)
            throw new NullPointerException("contextsFile is null");
        this.featuresFile = contextsFile;
    }

    public final File getEntryFeaturesFile() {
        return entryFeaturesFile;
    }

    public final void setEntryFeaturesFile(final File featuresFile)
            throws NullPointerException {
        if (featuresFile == null)
            throw new NullPointerException("featuresFile is null");
        this.entryFeaturesFile = featuresFile;
    }

    public final File getEntriesFile() {
        return entriesFile;
    }

    public final void setEntriesFile(final File entriesFile)
            throws NullPointerException {
        if (entriesFile == null)
            throw new NullPointerException("entriesFile is null");
        this.entriesFile = entriesFile;
    }

    public File getInputFile() {
        return inputFile;
    }

    public final void setInstancesFile(final File inputFile)
            throws NullPointerException {
        if (inputFile == null)
            throw new NullPointerException("sourceFile is null");
        this.inputFile = inputFile;
    }

    @Override
    protected void initialiseTask() throws Exception {
        super.initialiseTask();
        checkState();
    }

    @Override
    protected void runTask() throws Exception {
        if (LOG.isInfoEnabled())
            LOG.info("Running external count on \"" + inputFile + "\".");

        map();
        reduce();
        finish();

        if (LOG.isInfoEnabled())
            LOG.info("Completed external count");

    }

    protected void map() throws Exception {

        mergeEntryQueue = new ArrayDeque<File>();
        mergeFeaturesQueue = new ArrayDeque<File>();
        mergeEntryFeatureQueue = new ArrayDeque<File>();

        BlockingQueue<File> chunkQueue = new ArrayBlockingQueue<File>(2);

        ChunkTask chunkTask = new ChunkTask(getInputFile(), getCharset(),
                                            getMaxChunkSize());
        chunkTask.setDstFileQueue(chunkQueue);
        chunkTask.setChunkFileFactory(tempFileFactory);
        Future<ChunkTask> chunkFuture = submitTask(chunkTask);

        // Immidiately poll the chunk task so we can start handling other
        // completed tasks
        if (!getFutureQueue().poll().equals(chunkFuture))
            throw new AssertionError("Expecting ChunkTask on future queue.");

        while (!chunkFuture.isDone() || !chunkQueue.isEmpty()) {

            if (!getFutureQueue().isEmpty() && getFutureQueue().peek().isDone()) {

                handleCompletedTask(getFutureQueue().poll().get());

            } else if (!chunkQueue.isEmpty()) {

                File chunk = chunkQueue.take();

                File chunk_entriesFile = tempFileFactory.createFile("cnt.ent.",
                                                                    "");
                File chunk_featuresFile = tempFileFactory.createFile("cnt.feat.",
                                                                     "");
                File chunk_entryFeaturesFile = tempFileFactory.createFile(
                        "cnt.evnt.", "");

                submitCountTask(chunk, chunk_entriesFile, chunk_featuresFile, chunk_entryFeaturesFile);

            }

            // XXX: Nasty hack to stop it tight looping when both queues are empty
            Thread.sleep(1);
        }
        chunkTask.throwException();
    }

    protected void reduce() throws Exception {
        while (!getFutureQueue().isEmpty()) {
            Task task = getFutureQueue().poll().get();
            handleCompletedTask(task);
        }
    }

    protected void handleCompletedTask(Task task) throws Exception {
        while (task.isExceptionThrown())
            task.throwException();

        final Properties p = task.getProperties();
        final String taskType = p.getProperty(KEY_TASK_TYPE);
        final String dataType = p.getProperty(KEY_DATA_TYPE);


        if (taskType.equals(VALUE_TASK_TYPE_DELETE)) {
            // not a sausage
        } else if (taskType.equals(VALUE_TASK_TYPE_COUNT)) {
            final CountTask countTask = (CountTask) task;
            submitSortEntriesTask(countTask.getEntriesFile());
            submitSortFeaturesTask(countTask.getFeaturesFile());
            submitSortEventsTask(countTask.getEntryFeaturesFile());

            if (!DEBUG && !this.getInputFile().equals(countTask.getInputFile()))
                submitDeleteTask(countTask.getInputFile());

        } else if (taskType.equals(VALUE_TASK_TYPE_SORT)) {

            final File src = new File(p.getProperty(KEY_SRC_FILE));
            final File dst = new File(p.getProperty(KEY_DST_FILE));

            if (dataType.equals(VALUE_DATA_TYPE_ENTRIES))
                submitMergeEntriesTask(dst);
            else if (dataType.equals(VALUE_DATA_TYPE_FEATURES))
                submitMergeFeaturesTask(dst);
            else if (dataType.equals(VALUE_DATA_TYPE_EVENTS))
                submitMergeEventsTask(dst);
            else
                throw new AssertionError();

            if (!DEBUG && !src.equals(dst))
                submitDeleteTask(src);

        } else if (taskType.equals(VALUE_TASK_TYPE_MERGE)) {

            final File srca = new File(p.getProperty(KEY_SRC_FILE_A));
            final File srcb = new File(p.getProperty(KEY_SRC_FILE_B));
            final File dst = new File(p.getProperty(KEY_DST_FILE));

            if (dataType.equals(VALUE_DATA_TYPE_ENTRIES))
                submitMergeEntriesTask(dst);
            else if (dataType.equals(VALUE_DATA_TYPE_FEATURES))
                submitMergeFeaturesTask(dst);
            else if (dataType.equals(VALUE_DATA_TYPE_EVENTS))
                submitMergeEventsTask(dst);
            else
                throw new AssertionError();

            if (!DEBUG) {
                submitDeleteTask(srca);
                submitDeleteTask(srcb);
            }

        } else {
            throw new AssertionError();
        }
    }

    protected void finish() throws Exception {
        checkState();

        File finalMerge;

        finalMerge = mergeEntryQueue.poll();
        if (finalMerge == null)
            throw new AssertionError(
                    "The entry merge queue is empty but final copy has not been completed.");
        new CopyCommand(finalMerge, getEntriesFile()).runCommand();
        if (!DEBUG)
            new DeleteTask(finalMerge).runTask();

        finalMerge = mergeEntryFeatureQueue.poll();
        if (finalMerge == null)
            throw new AssertionError(
                    "The entry/feature merge queue is empty but final copy has not been completed.");
        new CopyCommand(finalMerge, getEntryFeaturesFile()).runCommand();
        if (!DEBUG)
            new DeleteTask(finalMerge).runTask();

        finalMerge = mergeFeaturesQueue.poll();
        if (finalMerge == null)
            throw new AssertionError(
                    "The feature merge queue is empty but final copy has not been completed.");
        new CopyCommand(finalMerge, getFeaturesFile()).runCommand();
        if (!DEBUG)
            new DeleteTask(finalMerge).runTask();
    }

    protected void submitCountTask(File in, File outEntries, File outFeatures, File outEvents) {
        CountTask ct = new CountTask();
        ct.setInstancesFile(in);
        ct.setEntriesFile(outEntries);
        ct.setFeaturesFile(outFeatures);
        ct.setEntryFeaturesFile(outEvents);
        ct.setCharset(getCharset());
        ct.indexDeligate.setPreindexedTokens1(indexDeligate.isPreindexedTokens1());
        ct.indexDeligate.setPreindexedTokens2(indexDeligate.isPreindexedTokens2());
        ct.indexDeligate.setIndex1(indexDeligate.getIndex1());
        ct.indexDeligate.setIndex2(indexDeligate.getIndex2());

        ct.getProperties().setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_COUNT);
        submitTask(ct);
    }

    protected void submitDeleteTask(File file) {
        final DeleteTask deleteTask = new DeleteTask(file);
        deleteTask.getProperties().setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_DELETE);
        submitTask(deleteTask);
    }

    private void submitSortEntriesTask(File file) throws IOException {
        File srcFile = file;
        File dstFile = tempFileFactory.createFile("mrg.ent.", "");

        Source<Weighted<Token>> srcA = new WeightedTokenSource(
                new TSVSource(srcFile, getCharset()),
                indexDeligate.getDecoder1());

        Sink<Weighted<Token>> snk = new WeightSumReducerSink<Token>(
                new WeightedTokenSink(
                new TSVSink(dstFile, getCharset()),
                indexDeligate.getEncoder1()));

        SortTask<Weighted<Token>> task =
                new SortTask<Weighted<Token>>(srcA, snk);
        task.setComparator(Weighted.recordOrder(Token.indexOrder()));

        task.getProperties().setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_SORT);
        task.getProperties().setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_ENTRIES);
        task.getProperties().setProperty(KEY_SRC_FILE, srcFile.toString());
        task.getProperties().setProperty(KEY_DST_FILE, dstFile.toString());
        submitTask(task);
    }

    private void submitSortFeaturesTask(File file) throws IOException {
        File srcFile = file;
        File dstFile = tempFileFactory.createFile("mrg.feat.", "");

        Source<Weighted<Token>> srcA = new WeightedTokenSource(
                new TSVSource(srcFile, getCharset()),
                indexDeligate.getDecoder2());

        Sink<Weighted<Token>> snk = new WeightSumReducerSink<Token>(
                new WeightedTokenSink(
                new TSVSink(dstFile, getCharset()),
                indexDeligate.getEncoder2()));

        SortTask<Weighted<Token>> task =
                new SortTask<Weighted<Token>>(srcA, snk);
        task.setComparator(Weighted.recordOrder(Token.indexOrder()));

        task.getProperties().setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_SORT);
        task.getProperties().setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_FEATURES);
        task.getProperties().setProperty(KEY_SRC_FILE, srcFile.toString());
        task.getProperties().setProperty(KEY_DST_FILE, dstFile.toString());
        submitTask(task);
    }

    private void submitSortEventsTask(File file) throws IOException {
        File srcFile = file;
        File dstFile = tempFileFactory.createFile("mrg.feat.", "");

        Source<Weighted<TokenPair>> srcA = new WeightedTokenPairSource(
                new TSVSource(srcFile, getCharset()),
                indexDeligate.getDecoder1(), indexDeligate.getDecoder2());

        Sink<Weighted<TokenPair>> snk = new WeightSumReducerSink<TokenPair>(
                new WeightedTokenPairSink(
                new TSVSink(dstFile, getCharset()),
                indexDeligate.getEncoder1(), indexDeligate.getEncoder2()));

        SortTask<Weighted<TokenPair>> task =
                new SortTask<Weighted<TokenPair>>(srcA, snk);
        task.setComparator(Weighted.recordOrder(TokenPair.indexOrder()));

        task.getProperties().setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_SORT);
        task.getProperties().setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_EVENTS);
        task.getProperties().setProperty(KEY_SRC_FILE, srcFile.toString());
        task.getProperties().setProperty(KEY_DST_FILE, dstFile.toString());
        submitTask(task);
    }

    private void submitMergeEntriesTask(File dst) throws IOException {
        mergeEntryQueue.add(dst);
        if (mergeEntryQueue.size() >= 2) {
            File srcFileA = mergeEntryQueue.poll();
            File srcFileB = mergeEntryQueue.poll();
            File dstFile = tempFileFactory.createFile("mrg.ent.", "");

            Source<Weighted<Token>> srcA = new WeightedTokenSource(
                    new TSVSource(srcFileA, getCharset()),
                    indexDeligate.getDecoder1());

            Source<Weighted<Token>> srcB = new WeightedTokenSource(
                    new TSVSource(srcFileB, getCharset()),
                    indexDeligate.getDecoder1());

            Sink<Weighted<Token>> snk = new WeightSumReducerSink<Token>(
                    new WeightedTokenSink(
                    new TSVSink(dstFile, getCharset()),
                    indexDeligate.getEncoder1()));

            MergeTask<Weighted<Token>> task =
                    new MergeTask<Weighted<Token>>(srcA, srcB, snk);
            task.setComparator(Weighted.recordOrder(Token.indexOrder()));

            task.getProperties().setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_MERGE);
            task.getProperties().setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_ENTRIES);
            task.getProperties().setProperty(KEY_SRC_FILE_A, srcFileA.toString());
            task.getProperties().setProperty(KEY_SRC_FILE_B, srcFileB.toString());
            task.getProperties().setProperty(KEY_DST_FILE, dstFile.toString());


            submitTask(task);
        }
    }

    private void submitMergeFeaturesTask(File dst) throws IOException {
        mergeFeaturesQueue.add(dst);
        if (mergeFeaturesQueue.size() >= 2) {
            File srcFileA = mergeFeaturesQueue.poll();
            File srcFileB = mergeFeaturesQueue.poll();
            File dstFile = tempFileFactory.createFile("mrg.feat.", "");

            Source<Weighted<Token>> srcA = new WeightedTokenSource(
                    new TSVSource(srcFileA, getCharset()),
                    indexDeligate.getDecoder2());

            Source<Weighted<Token>> srcB = new WeightedTokenSource(
                    new TSVSource(srcFileB, getCharset()),
                    indexDeligate.getDecoder2());

            Sink<Weighted<Token>> snk = new WeightSumReducerSink<Token>(
                    new WeightedTokenSink(
                    new TSVSink(dstFile, getCharset()),
                    indexDeligate.getEncoder2()));

            MergeTask<Weighted<Token>> task =
                    new MergeTask<Weighted<Token>>(srcA, srcB, snk);
            task.setComparator(Weighted.recordOrder(Token.indexOrder()));

            task.getProperties().setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_MERGE);
            task.getProperties().setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_FEATURES);
            task.getProperties().setProperty(KEY_SRC_FILE_A, srcFileA.toString());
            task.getProperties().setProperty(KEY_SRC_FILE_B, srcFileB.toString());
            task.getProperties().setProperty(KEY_DST_FILE, dstFile.toString());

            submitTask(task);
        }
    }

    private void submitMergeEventsTask(File dst) throws IOException {
        mergeEntryFeatureQueue.add(dst);
        if (mergeEntryFeatureQueue.size() >= 2) {
            File srcFileA = mergeEntryFeatureQueue.poll();
            File srcFileB = mergeEntryFeatureQueue.poll();
            File dstFile = tempFileFactory.createFile("mrg.evnt.", "");

            Source<Weighted<TokenPair>> srcA = new WeightedTokenPairSource(
                    new TSVSource(srcFileA, getCharset()),
                    indexDeligate.getDecoder1(), indexDeligate.getDecoder2());

            Source<Weighted<TokenPair>> srcB = new WeightedTokenPairSource(
                    new TSVSource(srcFileB, getCharset()),
                    indexDeligate.getDecoder1(), indexDeligate.getDecoder2());

            Sink<Weighted<TokenPair>> snk = new WeightSumReducerSink<TokenPair>(
                    new WeightedTokenPairSink(
                    new TSVSink(dstFile, getCharset()),
                    indexDeligate.getEncoder1(), indexDeligate.getEncoder2()));

            MergeTask<Weighted<TokenPair>> task =
                    new MergeTask<Weighted<TokenPair>>(srcA, srcB, snk);
            task.setComparator(Weighted.recordOrder(TokenPair.indexOrder()));


            task.getProperties().setProperty(KEY_TASK_TYPE, VALUE_TASK_TYPE_MERGE);
            task.getProperties().setProperty(KEY_DATA_TYPE, VALUE_DATA_TYPE_EVENTS);
            task.getProperties().setProperty(KEY_SRC_FILE_A, srcFileA.toString());
            task.getProperties().setProperty(KEY_SRC_FILE_B, srcFileB.toString());
            task.getProperties().setProperty(KEY_DST_FILE, dstFile.toString());


            submitTask(task);
        }
    }

    /**
     * Method that performance a number of sanity checks on the parameterisation
     * of this class. It is necessary to do this because the the class can be
     * instantiated via a null constructor when run from the command line.
     *
     * @throws NullPointerException
     * @throws IllegalStateException
     * @throws FileNotFoundException
     */
    private void checkState() throws NullPointerException, IllegalStateException, FileNotFoundException {
        // Check non of the parameters are null
        if (inputFile == null)
            throw new NullPointerException("inputFile is null");
        if (entryFeaturesFile == null)
            throw new NullPointerException("entryFeaturesFile is null");
        if (featuresFile == null)
            throw new NullPointerException("featuresFile is null");
        if (entriesFile == null)
            throw new NullPointerException("entriesFile is null");
        if (charset == null)
            throw new NullPointerException("charset is null");

        // Check that no two files are the same
        if (inputFile.equals(entryFeaturesFile))
            throw new IllegalStateException("inputFile == featuresFile");
        if (inputFile.equals(featuresFile))
            throw new IllegalStateException("inputFile == contextsFile");
        if (inputFile.equals(entriesFile))
            throw new IllegalStateException("inputFile == entriesFile");
        if (entryFeaturesFile.equals(featuresFile))
            throw new IllegalStateException("entryFeaturesFile == featuresFile");
        if (entryFeaturesFile.equals(entriesFile))
            throw new IllegalStateException("entryFeaturesFile == entriesFile");
        if (featuresFile.equals(entriesFile))
            throw new IllegalStateException("featuresFile == entriesFile");


        // Check that the instances file exists and is readable
        if (!inputFile.exists())
            throw new FileNotFoundException(
                    "instances file does not exist: " + inputFile);
        if (!inputFile.isFile())
            throw new IllegalStateException(
                    "instances file is not a normal data file: " + inputFile);
        if (!inputFile.canRead())
            throw new IllegalStateException(
                    "instances file is not readable: " + inputFile);

        // For each output file, check that either it exists and it writeable,
        // or that it does not exist but is creatable
        if (entriesFile.exists() && (!entriesFile.isFile() || !entriesFile.canWrite()))
            throw new IllegalStateException(
                    "entries file exists but is not writable: " + entriesFile);
        if (!entriesFile.exists() && !entriesFile.getAbsoluteFile().
                getParentFile().
                canWrite()) {
            throw new IllegalStateException(
                    "entries file does not exists and can not be reated: " + entriesFile);
        }
        if (featuresFile.exists() && (!featuresFile.isFile() || !featuresFile.canWrite()))
            throw new IllegalStateException(
                    "features file exists but is not writable: " + featuresFile);
        if (!featuresFile.exists() && !featuresFile.getAbsoluteFile().
                getParentFile().
                canWrite()) {
            throw new IllegalStateException(
                    "features file does not exists and can not be reated: " + featuresFile);
        }
        if (entryFeaturesFile.exists() && (!entryFeaturesFile.isFile() || !entryFeaturesFile.canWrite()))
            throw new IllegalStateException(
                    "entry-features file exists but is not writable: " + entryFeaturesFile);
        if (!entryFeaturesFile.exists() && !entryFeaturesFile.getAbsoluteFile().
                getParentFile().
                canWrite()) {
            throw new IllegalStateException(
                    "entry-features file does not exists and can not be reated: " + entryFeaturesFile);
        }
    }

    @Override
    protected Objects.ToStringHelper toStringHelper() {
        return super.toStringHelper().
                add("in", inputFile).
                add("entriesOut", entriesFile).
                add("featuresOut", featuresFile).
                add("eventsOut", entryFeaturesFile).
                add("tempDir", tempFileFactory).
                add("charset", charset);
    }

}
