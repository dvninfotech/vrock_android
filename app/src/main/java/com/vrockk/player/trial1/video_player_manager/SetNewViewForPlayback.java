package com.vrockk.player.trial1.video_player_manager;

import com.vrockk.player.trial1.video_player_manager.manager.VideoPlayerManagerCallback;
import com.vrockk.player.trial1.video_player_manager.meta.MetaData;
import com.vrockk.player.trial1.video_player_manager.player_messages.PlayerMessage;
import com.vrockk.player.trial1.video_player_manager.ui.VideoPlayerView;

public class SetNewViewForPlayback extends PlayerMessage {

    private final MetaData mCurrentItemMetaData;
    private final VideoPlayerView mCurrentPlayer;
    private final VideoPlayerManagerCallback mCallback;

    public SetNewViewForPlayback(MetaData currentItemMetaData, VideoPlayerView videoPlayerView, VideoPlayerManagerCallback callback) {
        super(videoPlayerView, callback);
        mCurrentItemMetaData = currentItemMetaData;
        mCurrentPlayer = videoPlayerView;
        mCallback = callback;
    }

    @Override
    public String toString() {
        return SetNewViewForPlayback.class.getSimpleName() + ", mCurrentPlayer " + mCurrentPlayer;
    }

    @Override
    protected void performAction(VideoPlayerView currentPlayer) {
        mCallback.setCurrentItem(mCurrentItemMetaData, mCurrentPlayer);
    }

    @Override
    protected com.vrockk.player.trial1.video_player_manager.PlayerMessageState stateBefore() {
        return com.vrockk.player.trial1.video_player_manager.PlayerMessageState.SETTING_NEW_PLAYER;
    }

    @Override
    protected com.vrockk.player.trial1.video_player_manager.PlayerMessageState stateAfter() {
        return PlayerMessageState.IDLE;
    }
}
