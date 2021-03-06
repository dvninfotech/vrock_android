package com.vrockk.player.trial1.video_player_manager.player_messages;

import com.vrockk.player.trial1.video_player_manager.PlayerMessageState;
import com.vrockk.player.trial1.video_player_manager.manager.VideoPlayerManagerCallback;
import com.vrockk.player.trial1.video_player_manager.ui.VideoPlayerView;

/**
 * This is generic PlayerMessage for setDataSource
 */
public abstract class SetDataSourceMessage extends PlayerMessage {

    public SetDataSourceMessage(VideoPlayerView videoPlayerView, VideoPlayerManagerCallback callback) {
        super(videoPlayerView, callback);
    }

    @Override
    protected PlayerMessageState stateBefore() {
        return PlayerMessageState.SETTING_DATA_SOURCE;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return PlayerMessageState.DATA_SOURCE_SET;
    }
}
