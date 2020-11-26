package com.vrockk.view.feeds.trial1

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vrockk.interfaces.OnFeedActionListener
import com.vrockk.interfaces.OnMediaEventChangeListener
import com.vrockk.models.feeds.FeedDataModel

class FeedsAdapter(
    p_context: Context,
    p_feedDataModels: ArrayList<FeedDataModel>,
    p_onMediaEventChangeListener: OnMediaEventChangeListener,
    p_onFeedActionListener: OnFeedActionListener
) : RecyclerView.Adapter<FeedsAdapter.ViewHolder>() {
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}/*{

    var context: Context = p_context
    var feedDataModels: ArrayList<FeedDataModel> = p_feedDataModels
    var onMediaEventChangeListener: OnMediaEventChangeListener = p_onMediaEventChangeListener
    var onFeedActionListener: OnFeedActionListener = p_onFeedActionListener

    var currentPlayingIndex = 0

    fun setPlayingIndex(playingIndex: Int) {
        this.currentPlayingIndex = playingIndex
    }

    fun getPlayingIndex(): Int {
        return currentPlayingIndex
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.adapter_feeds, parent, false),
            context, feedDataModels,
            onMediaEventChangeListener, onFeedActionListener
        )
    }

    override fun getItemCount(): Int {
        return feedDataModels.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feedDataModel = feedDataModels[position]

        if (feedDataModel.thumbnail.isNotEmpty())
            Picasso.get().load(feedDataModel.thumbnail).into(holder.ivThumbnail)

        if (position == currentPlayingIndex) {
            holder.ivPlayPause.visibility = View.GONE
            holder.playerView.player?.addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            holder.playerView.visibility = View.INVISIBLE
                            holder.progressBar.visibility = View.VISIBLE
                        }
                        Player.STATE_READY -> {
                            holder.progressBar.visibility = View.GONE
                            holder.playerView.visibility = View.VISIBLE
                        }
                    }
                }
            })
            PlayerUtils.assignMediaItemAndPrepare(feedDataModel.post)
        } else {
            holder.ivPlayPause.visibility = View.VISIBLE
            holder.playerView.visibility = View.INVISIBLE
            holder.progressBar.visibility = View.GONE
        }

        holder.tvName.text = "@${feedDataModel.userModel.userName}"
        holder.tvDescription.text = feedDataModel.description
        holder.tvComment.text = "${feedDataModel.totalComments}"
        holder.tvLikes.text = "${feedDataModel.totalLikes}"

        if (feedDataModel.song != null) {
            holder.tvSongName.text = feedDataModel.song?.descriptionModel?.name?.replace(".mp3", "")
            holder.musicLayout.visibility = View.VISIBLE
        } else {
            holder.tvSongName.visibility = View.GONE
            holder.musicLayout.visibility = View.GONE
        }

        if (VrockkApplication.isLoggedIn()) {
            if (VrockkApplication.user_obj!!._id == feedDataModel.userModel._id) {
                holder.ivFollowUser.visibility = View.GONE
                holder.ivFollowingUser.visibility = View.GONE
                holder.tvReport.visibility = View.GONE
                holder.tvGift.visibility = View.GONE

            } else {
                holder.tvReport.visibility = View.VISIBLE
                holder.tvGift.visibility = View.VISIBLE
            }
        }

        if (feedDataModel.userModel.profilePic.isNotEmpty()) {
            Picasso.get().load(feedDataModel.userModel.profilePic)
                .placeholder(R.drawable.user_placeholder).error(R.drawable.user_placeholder)
                .into(holder.ivUserProfile!!)
        } else {
            Picasso.get().load(R.drawable.user_placeholder).into(holder.ivUserProfile!!)
        }

        if (feedDataModel.isFollowing) {
            if (VrockkApplication.isLoggedIn()) {
                if (VrockkApplication.user_obj?._id != feedDataModel.userModel._id) {
                    holder.ivFollowUser.visibility = View.GONE
                    holder.ivFollowingUser.visibility = View.VISIBLE
                }
            }
        } else {
            if (VrockkApplication.isLoggedIn()) {
                if (VrockkApplication.user_obj!!._id != feedDataModel.userModel._id) {
                    holder.ivFollowingUser.visibility = View.GONE
                    holder.ivFollowUser.visibility = View.VISIBLE
                }
            }
        }

        holder.tvLikes.setCompoundDrawablesWithIntrinsicBounds(
            0,
            if (feedDataModel.isLiked) R.drawable.star_red else R.drawable.star_home,
            0, 0
        )

        holder.ivSong.startAnimation(
            AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise)
        )
    }

    fun resetPlayer() {
        PlayerUtils.release()
        currentPlayingIndex = 0
    }

    fun resumeCurrentFeed() {
        PlayerUtils.resume()
    }

    fun pauseCurrentFeed() {
        PlayerUtils.pause()
    }

    class ViewHolder(
        itemView: View, context: Context, feedDataModels: ArrayList<FeedDataModel>,
        onMediaEventChangeListener: OnMediaEventChangeListener,
        onFeedActionListener: OnFeedActionListener
    ) : RecyclerView.ViewHolder(itemView) {
        var movieWrapper: MovieWrapperView = itemView.movieWrapper
        var tvName: TextView = itemView.tvName
        var tvDescription: SocialTextView = itemView.tvDescription
        var tvComment: TextView = itemView.tvComment
        var tvLikes: TextView = itemView.tvLikes

        var rlUser: RelativeLayout = itemView.rlUser
        var ivUserProfile: CircleImageView = itemView.ivUserProfile
        var ivFollowUser: ImageView = itemView.ivFollowUser
        var ivFollowingUser: ImageView = itemView.ivFollowingUser

        var tvShare: TextView = itemView.tvShare
        var tvGift: TextView = itemView.tvGift

        var moreLayout: LinearLayout = itemView.moreLayout
        var tvMore: TextView = itemView.tvMore
        var tvDuet: TextView = itemView.tvDuet
        var tvReport: TextView = itemView.tvReport
        var ivClose: ImageView = itemView.ivClose

        var ivPlayPause: ImageView = itemView.ivPlayPause
        var progressBar: ProgressBar = itemView.progressBar
        var ivThumbnail: ImageView = itemView.ivThumbnail
        var playerView: PlayerView = itemView.playerView
//        var playerFrame: SurfaceView = itemView.playerFrame

        var musicLayout: ConstraintLayout = itemView.musicLayout
        var ivSong: CircleImageView = itemView.ivSong
        var tvSongName: TextView = itemView.tvSongName

        init {
            setIsRecyclable(false)

            playerView.player = PlayerUtils.getMyExoplayer(context)

            itemView.setOnClickListener {
                if (PlayerUtils.isPlaying()) {
                    PlayerUtils.pause()
                    ivPlayPause.visibility = View.VISIBLE
                } else {
                    PlayerUtils.resume()
                    ivPlayPause.visibility = View.GONE
                }
            }

            ivUserProfile.setOnClickListener {
                onFeedActionListener.onViewUserRequested(
                    adapterPosition,
                    feedDataModels[adapterPosition]
                )
            }

            tvDescription.setOnHashtagClickListener { view, text ->
//                feedsFragment.pauseFeed()
                context.startActivity(
                    Intent(context, SearchHashtagActivity::class.java)
                        .putExtra("hashTag", text.toString())
                )
            }

            ivFollowUser.setOnClickListener {
                if (VrockkApplication.isLoggedIn()) {
                    onFeedActionListener.onFollowChangeRequested(
                        adapterPosition,
                        feedDataModels[adapterPosition]
                    )
                } else
                    (context as BaseActivity).showLoginPopup()
            }

            tvLikes.setOnClickListener {
                if (VrockkApplication.isLoggedIn()) {
                    onFeedActionListener.onLikesChanged(
                        adapterPosition,
                        feedDataModels[adapterPosition]
                    )
                } else
                    (context as BaseActivity).showLoginPopup()
            }

            tvComment.setOnClickListener {
                onFeedActionListener.onCommentRequested(
                    adapterPosition,
                    feedDataModels[adapterPosition]
                )
            }

            tvGift.setOnClickListener {
                if (VrockkApplication.isLoggedIn()) {
                    onFeedActionListener.onGiftClicked(
                        adapterPosition,
                        feedDataModels[adapterPosition]
                    )
                } else
                    (context as BaseActivity).showLoginPopup()
            }

            tvShare.setOnClickListener {
                if (VrockkApplication.isLoggedIn()) {
                    onFeedActionListener.onShareClicked(
                        adapterPosition,
                        feedDataModels[adapterPosition]
                    )
                } else
                    (context as BaseActivity).showLoginPopup()
            }

            tvMore.setOnClickListener {
                tvMore.visibility = View.GONE
                moreLayout.visibility = View.VISIBLE

                moreLayout.startAnimation(
                    AnimationUtils
                        .loadAnimation(context, R.anim.slide_right_to_left)
                )
            }

            tvDuet.setOnClickListener {
                moreLayout.visibility = View.GONE
                tvMore.visibility = View.VISIBLE

                if (VrockkApplication.isLoggedIn()) {
                    onFeedActionListener.onDuetClicked(
                        adapterPosition,
                        feedDataModels[adapterPosition]
                    )
                } else
                    (context as BaseActivity).showLoginPopup()
            }

            tvReport.setOnClickListener {
                moreLayout.visibility = View.GONE
                tvMore.visibility = View.VISIBLE

                if (VrockkApplication.isLoggedIn()) {
                    onFeedActionListener.onMoreClicked(
                        adapterPosition,
                        feedDataModels[adapterPosition]
                    )
                } else
                    (context as BaseActivity).showLoginPopup()
            }

            ivClose.setOnClickListener {
                moreLayout.visibility = View.GONE
                tvMore.visibility = View.VISIBLE
            }

            tvSongName.setOnClickListener {
                val feedDataModel = feedDataModels[adapterPosition]
                if (feedDataModel.song != null) {
//                    feedsFragment.pauseFeed()
                    context.startActivity(
                        Intent(context, PostRelatedToSongActivity::class.java)
                            .putExtra("songId", feedDataModel.song?.id)
                            .putExtra("uploadedBy", feedDataModel.song?.uploadedBy)
                            .putExtra("song", feedDataModel.song?.song)
                            .putExtra("songName", feedDataModel.song?.descriptionModel?.name)
                    )
                }
            }

            ivSong.setOnClickListener {
                val feedDataModel = feedDataModels[adapterPosition]
                if (feedDataModel.song != null) {
//                    feedsFragment.pauseFeed()
                    context.startActivity(
                        Intent(context, PostRelatedToSongActivity::class.java)
                            .putExtra("songId", feedDataModel.song?.id)
                            .putExtra("uploadedBy", feedDataModel.song?.uploadedBy)
                            .putExtra("song", feedDataModel.song?.song)
                            .putExtra("songName", feedDataModel.song?.descriptionModel?.name)
                    )
                }
            }
        }
    }
}*/