package com.vrockk.player.trial1.video_player_manager.manager;

import com.vrockk.player.trial1.video_player_manager.meta.MetaData;
import com.vrockk.player.trial1.video_player_manager.ui.VideoPlayerView;

/**
 * This is basic interface for Items in Adapter of the list. Regardless of is it {@link android.widget.ListView}
 * or {@link android.support.v7.widget.RecyclerView}
 */
public interface VideoItem {
    void playNewVideo(MetaData currentItemMetaData, VideoPlayerView player, VideoPlayerManager<MetaData> videoPlayerManager);
    void stopPlayback(VideoPlayerManager videoPlayerManager);
}
