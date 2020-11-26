package com.vrockk.interfaces

import com.vrockk.models.feeds.FeedDataModel

interface OnFeedActionListener {
    fun onViewUserRequested(adapterPosition: Int, feedDataModel: FeedDataModel)

    fun onFollowChangeRequested(adapterPosition: Int, feedDataModel: FeedDataModel)
    fun onLikesChanged(adapterPosition: Int, feedDataModel: FeedDataModel)
    fun onCommentRequested(adapterPosition: Int, feedDataModel: FeedDataModel)

    fun onGiftClicked(adapterPosition: Int, feedDataModel: FeedDataModel)

    fun onShareClicked(adapterPosition: Int, feedDataModel: FeedDataModel)

    fun onMoreClicked(adapterPosition: Int, feedDataModel: FeedDataModel)
    fun onDuetClicked(adapterPosition: Int, feedDataModel: FeedDataModel)
    fun onReportClicked(adapterPosition: Int, feedDataModel: FeedDataModel)
}