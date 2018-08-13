package org.tools4j.eventsourcing.queue;

import org.agrona.concurrent.UnsafeBuffer;
import org.tools4j.eventsourcing.api.*;
import org.tools4j.eventsourcing.appender.IndexedAppender;
import org.tools4j.eventsourcing.appender.MultiPayloadAppender;
import org.tools4j.mmap.region.api.RegionRingFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

public class DefaultIndexedTransactionalQueue implements IndexedTransactionalQueue {
    private final Transaction appender;
    private final IndexedPollerFactory pollerFactory;

    public DefaultIndexedTransactionalQueue(final String directory,
                                            final String filePrefix,
                                            final boolean clearFiles,
                                            final RegionRingFactory regionRingFactory,
                                            final int regionSize,
                                            final int regionRingSize,
                                            final int regionsToMapAhead,
                                            final long maxFileSize,
                                            final int encodingBufferSize) throws IOException {

        this.appender = new MultiPayloadAppender(
                            new IndexedAppender(
                                RegionAccessorSupplier.forReadWrite(
                                    directory,
                                    filePrefix,
                                    clearFiles,
                                    regionRingFactory,
                                    regionSize,
                                    regionRingSize,
                                    regionsToMapAhead,
                                    maxFileSize)),
                new UnsafeBuffer(ByteBuffer.allocateDirect(encodingBufferSize)));

        this.pollerFactory = new DefaultIndexedPollerFactory(
                directory,
                filePrefix,
                regionRingFactory,
                regionSize,
                regionRingSize,
                regionsToMapAhead);
    }

    @Override
    public Transaction appender() {
        return this.appender;
    }

    @Override
    public Poller createPoller(final Poller.IndexPredicate skipPredicate,
                               final Poller.IndexPredicate pausePredicate,
                               final Poller.IndexConsumer beforeIndexHandler,
                               final Poller.IndexConsumer afterIndexHandler) throws IOException {
        return pollerFactory.createPoller(
                skipPredicate,
                pausePredicate,
                beforeIndexHandler,
                afterIndexHandler);
    }
}