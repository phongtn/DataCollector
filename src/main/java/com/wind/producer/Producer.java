package com.wind.producer;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.producer.HollowProducer;
import com.wind.producer.datamodel.Movie;
import com.wind.producer.datamodel.SourceDataRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Producer {

    public static final String SCRATCH_DIR = System.getProperty("java.io.tmpdir");
    private static final long MIN_TIME_BETWEEN_CYCLES = 10000;
    static final Logger logger = LoggerFactory.getLogger(Producer.class);

    public static void restoreIfAvailable(HollowProducer producer,
                                          HollowConsumer.BlobRetriever retriever,
                                          HollowConsumer.AnnouncementWatcher unpinnableAnnouncementWatcher) {

        System.out.println("ATTEMPTING TO RESTORE PRIOR STATE...");
        long latestVersion = unpinnableAnnouncementWatcher.getLatestVersion();
        if (latestVersion != HollowConsumer.AnnouncementWatcher.NO_ANNOUNCEMENT_AVAILABLE) {
            producer.restore(latestVersion, retriever);
            System.out.println("RESTORED " + latestVersion);
        } else {
            System.out.println("RESTORE NOT AVAILABLE");
        }
    }

    public static void cycleForever(HollowProducer producer) {
        final SourceDataRetriever sourceDataRetriever = new SourceDataRetriever();

        long lastCycleTime = Long.MIN_VALUE;
        while (true) {
            waitForMinCycleTime(lastCycleTime);
            lastCycleTime = System.currentTimeMillis();
            producer.runCycle(writeState -> {
                for (Movie movie : sourceDataRetriever.retrieveAllMovies()) {
                    writeState.add(movie);  /// <-- this is thread-safe, and can be done in parallel
                }
            });
        }
    }

    private static void waitForMinCycleTime(long lastCycleTime) {
        long targetNextCycleTime = lastCycleTime + MIN_TIME_BETWEEN_CYCLES;

        while (System.currentTimeMillis() < targetNextCycleTime) {
            try {
                Thread.sleep(targetNextCycleTime - System.currentTimeMillis());
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
}
