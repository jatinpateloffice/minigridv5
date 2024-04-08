package com.qs.minigridv5.entities;

import android.text.Html;
import android.text.Spanned;
import com.crashlytics.android.Crashlytics;
import com.qs.minigridv5.misc.Helpers;

import java.io.File;
import java.util.ArrayList;

public class Scene {

    public static final int SELFIE                 = 1;
    public static final int NON_SELFIE             = 2;
    public static final int MIN_SELFIE_CLIP_LENGTH = 5;

    public final int           ID;
    public final MovieTemplate movieTemplate;
    public final String        title;
    public final int           type;// selfie or non-selfie
    public       boolean       complete = false;

    // Video Component
    public final String            video_prompt_text;
    public final long              video_length;
    public final ArrayList<String> videoClips;
    public       long              remainingVideoClipLengthMs;

    // Audio Component
    public final String            audio_header;
    public final String            audio_text;
    public final long max_audio_length;
    private String audioFile;


    public Scene(final int ID, final MovieTemplate movieTemplate, ScenePackage cp) {

        this.ID = ID;
        this.movieTemplate = movieTemplate;
        this.title = cp.title;
        this.video_prompt_text = cp.video_text;
        String tmpAudioText = cp.audio_texts[0].trim();
        if (tmpAudioText.endsWith("<br/>")) {
            tmpAudioText = tmpAudioText.substring(0, tmpAudioText.lastIndexOf("<br/>"));
        }
        this.audio_text = tmpAudioText;
        this.type = cp.type;
        this.video_length = cp.video_length * 1000;
        this.audio_header = cp.audio_header;
        this.max_audio_length = cp.max_audio_length;
        this.videoClips = new ArrayList<>();
        this.audioFile = null;
        remainingVideoClipLengthMs = this.video_length;


    }

    /**
     * adds a video file to the array, recalculates remaining clip length
     *
     * @param videoName
     */
    public void addVideo(String videoName) {
        if (type == SELFIE) {// this is to avoid duplicate video file names
            videoClips.clear();
        }

        videoClips.add(videoName);

        if (movieTemplate.project.thumbnail == null) {
            try {
                movieTemplate.project.thumbnail = Helpers.createThumbnailAtTime(videoName, 1);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }


        // update remaining remaining time of the Category
        recalculateRemainingVideoLength();

    }

    public void removeVideoClip(String videoName) {

        final File file = new File(videoName);
        if (file.delete()) {

            videoClips.remove(videoName);

            recalculateRemainingVideoLength();

            updateCompletionStatus();
        }

    }

    public void setAudio(String audioFileName) {

        this.audioFile = audioFileName;

    }

    public String getAudioFile(){
        return this.audioFile;
    }

    public void removeAudioFile(){

        final File file = new File(this.audioFile);
        if (file.delete()) {
            this.audioFile = null;
        }

    }

    public void recalculateRemainingVideoLength() {

        remainingVideoClipLengthMs = this.video_length;
        for (String video : videoClips) {
            final long videoLength = Helpers.calculateClipLength(video);
            remainingVideoClipLengthMs -= videoLength;
        }
        if (remainingVideoClipLengthMs < 1000) {
            remainingVideoClipLengthMs = 0;
        }
    }

    public void updateCompletionStatus() {

        if (allVideoClipsPresent()) {

            if (type == SELFIE) {

                complete = true;

            } else {

                // if not a selfie video
                // check if you have the required audio file
                complete = audioClipPresent();

            }

        } else {
            complete = false;
        }

    }

    public boolean audioClipPresent() {
        return audioFile != null;
    }

    public boolean allVideoClipsPresent() {
        if (type == SELFIE) {
            return videoClips.size() > 0;
        } else {
            return remainingVideoClipLengthMs <= 0;
        }
    }

    public int getNoofVideoClipsPresent() {

        return videoClips.size();

    }

    @Override
    public String toString() {
        String string = title;
        string += ", remaining: " + remainingVideoClipLengthMs;
        return string;
    }

    /**
     * Gets the video length of all the present video files for the category
     *
     * @return length of video files in milli seconds
     */
    public long calculateAllVideosLength() {

        long length = 0;
        for (String videoFile : videoClips) {
            length += Helpers.calculateClipLength(videoFile);
        }
        return length;
    }

    /**
     * updates category remaining clip length time and
     * category completion status
     */
    @Deprecated
    public void update() {

        remainingVideoClipLengthMs = video_length;

        recalculateRemainingVideoLength();


        updateCompletionStatus();

    }

    public boolean isSelfieScene(){
        return this.type == SELFIE;
    }

    public Spanned getHtmlVideoText() {
        return Html.fromHtml(video_prompt_text);
    }

    public Spanned getHtmlAudioText() {
        return Html.fromHtml(audio_text);
    }

    public static class ScenePackage {

        public String   title;
        public String   video_text;
        public String[] audio_texts;
        public int      type;
        public long     video_length;
        public String   audio_header;
        public long     max_audio_length;

        public ScenePackage(String title, int type, String video_text, String audio_header, String[] audio_texts, long video_length, long max_audio_length) {
            this.title = title;
            this.type = type;
            this.video_text = video_text;
            this.audio_texts = audio_texts;
            this.video_length = video_length;
            this.audio_header = audio_header;
            this.max_audio_length = max_audio_length;
        }
    }

}
