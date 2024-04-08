package com.qs.minigridv5.utilities;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import com.qs.minigridv5.misc.C;

import java.nio.ByteBuffer;

import static com.qs.minigridv5.misc.Helpers.getTimeInSecs;

public class MyMuxer {

    private final MediaMuxer muxer;

    // buffers
    private final int                   bufferSize = 2048 * 1024;
    private final ByteBuffer            buffer     = ByteBuffer.allocate(bufferSize);
    private final MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    private boolean muxerStarted = false;


    public MyMuxer(final String outputFile) throws Exception {

        muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

    }

    public MyMuxer(final String outputFile, boolean webm) throws Exception {

        muxer = new MediaMuxer(outputFile, webm ? MediaMuxer.OutputFormat.MUXER_OUTPUT_WEBM : MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

    }

    public void start() {
        muxer.start();
        muxerStarted = true;
    }

    public void stopAndRelease() {
        if(muxerStarted) {
            muxer.stop();
            muxer.release();
        }
    }

    public int addTrack(MediaFormat mediaFormat) {
        return muxer.addTrack(mediaFormat);
    }

    /**
     * Adds media samples from the specified video file to the muxer at a specified time and returns the last sample's
     * presentation time
     *
     * @param file            input file to read samples from
     * @param in_track        audio or video track of the input file to extract data from
     * @param out_track       muxer track to write the samples to
     * @param timeOffset      time from where to start writing the samples in the muxer
     * @param sampleTimeLimit time limit beyond which the samples will be ignored i.e. not read and written to the muxer. Set -1 if no limit is required
     * @return the last sample's presentation time
     * @throws Exception
     */
    public long addSamples(
            String file,
            int in_track,
            int out_track,
            long timeOffset,
            long sampleTimeLimit
    ) throws Exception {

        final MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(file);
        extractor.selectTrack(in_track);

        long currentSamplePresentationTime  = 0;
        long previousSamplePresentationTime = 0;
        long presentTimeDiff                = 0;

        while ((bufferInfo.size = extractor.readSampleData(buffer, 0)) > 0) {

            currentSamplePresentationTime = extractor.getSampleTime();
            presentTimeDiff = currentSamplePresentationTime - previousSamplePresentationTime;


            if (sampleTimeLimit > 0) {
                if ((timeOffset + currentSamplePresentationTime) > sampleTimeLimit) {
                    buffer.clear();
                    Log.i(C.T, "audio samples ignored");
                    break;
                }
            }


            bufferInfo.offset = 0;
            bufferInfo.presentationTimeUs = currentSamplePresentationTime + timeOffset;
            bufferInfo.flags = extractor.getSampleFlags();
            muxer.writeSampleData(out_track, buffer, bufferInfo);
            extractor.advance();
            previousSamplePresentationTime = currentSamplePresentationTime;

            buffer.clear();

        }

        Log.e(
                C.T,
                "track: " + out_track + " | " +
                        getTimeInSecs(timeOffset) + "  ->  " + getTimeInSecs(currentSamplePresentationTime + timeOffset) +
                        " | [ " + timeOffset + "  ->  " + (currentSamplePresentationTime + timeOffset) + " ]" +
                        " | diff: " + getTimeInSecs(presentTimeDiff) +
                        " | " + file
        );

        extractor.release();
        return currentSamplePresentationTime + presentTimeDiff;

    }

}
