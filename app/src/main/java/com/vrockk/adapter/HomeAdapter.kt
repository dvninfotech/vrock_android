package com.vrockk.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.facebook.FacebookSdk.getApplicationContext
import com.squareup.picasso.Picasso
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.base.BaseActivity
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.home.home_page.Data
import com.vrockk.posts_play.PostRelatedToSongActivity
import com.vrockk.view.home.HomeFragment
import com.vrockk.view.search.SearchHashtagActivity
import kotlinx.android.synthetic.main.adapter_home.view.*

class HomeAdapter(
    val context: Context,
    val dataList: ArrayList<Data>,
    val homeFragment: HomeFragment,
    val itemClickListener: ItemClickListernerWithType
) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

//    private var videoUrl = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.adapter_home, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        homeFragment.stopFeed()

        val data = dataList[position]

        if (VrockkApplication.isLoggedIn()) {
            if (VrockkApplication.user_obj!!._id == data.userId._id) {
                holder.imgProfileFollow.visibility = View.GONE
                holder.imgProfileFollowDone.visibility = View.GONE
                holder.tvReport.visibility = View.GONE
                holder.tvGift.visibility = View.GONE

            } else {
                holder.tvReport.visibility = View.VISIBLE
                holder.tvGift.visibility = View.VISIBLE
            }
        }

        if (data.isFollowing) {
            if (VrockkApplication.isLoggedIn()) {
                if (VrockkApplication.user_obj?._id != data.userId._id) {
                    holder.imgProfileFollow.visibility = View.GONE
                    holder.imgProfileFollowDone.visibility = View.VISIBLE
                }
            }
        } else {
            if (VrockkApplication.isLoggedIn()) {
                if (VrockkApplication.user_obj!!._id != data.userId._id) {
                    holder.imgProfileFollowDone.visibility = View.GONE
                    holder.imgProfileFollow.visibility = View.VISIBLE
                }
            }
        }

        holder.setIsRecyclable(false)

        holder.tvName.text = "@${data.userId.userName}"
        holder.tvDescription.text = data.description
        holder.tvComment.text = "${data.totalComments}"
        holder.tvLike.text = "${data.totalLikes}"

        if (data.song != null) {
            holder.tvMusic.text = data.song?.description?.name?.replace(".mp3", "")
        } else {
            holder.tvMusic.visibility = View.GONE
            holder.songIconLayout.visibility = View.GONE
        }

        if (data.thumbnail.isNotEmpty())
            Picasso.get().load(data.thumbnail).into(holder.ivPlaceholder)

        if (data.userId.profilePic.isNotEmpty()) {
            Picasso.get().load(data.userId.profilePic)
                .placeholder(R.drawable.user_placeholder).error(R.drawable.user_placeholder)
                .into(holder.ivProfile!!)
        } else {
            Picasso.get().load(R.drawable.user_placeholder).into(holder.ivProfile!!)
        }

        holder.tvLike.setCompoundDrawablesWithIntrinsicBounds(
            0,
            if (data.isLiked) R.drawable.star_red else R.drawable.star_home,
            0, 0
        )

        if (homeFragment.playingFeedListIndex == position)
            homeFragment.changeFeed(holder, data.post)
        else
            homeFragment.stopFeed()

        holder.itemView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (homeFragment.isPlaying()) {
                    homeFragment.pauseFeed()
                    holder.itemView.playPause.visibility = View.VISIBLE
                    return false

                } else if (!homeFragment.isPlaying()) {
                    homeFragment.playFeed()
                    holder.itemView.playPause.visibility = View.GONE
                    return false
                } else
                    v?.performClick()
                return true
            }
        })

        holder.ivProfile.setOnClickListener {
            itemClickListener.onItemClicked(position, "other")
        }

        holder.tvDescription.setOnHashtagClickListener { view, text ->
            homeFragment.pauseFeed()
            context.startActivity(
                Intent(context, SearchHashtagActivity::class.java)
                    .putExtra("hashTag", text.toString())
            )
        }

        holder.tvLike.setOnClickListener {
            if (VrockkApplication.isLoggedIn()) {
                val isLiked = data.isLiked
                val totalLikes = data.totalLikes
                data.isLiked = !isLiked
                data.totalLikes =
                    if (data.isLiked) totalLikes + 1 else (if (totalLikes == 0) 0 else totalLikes - 1)
                holder.tvLike.text = "${data.totalLikes}"
                holder.tvLike.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    if (data.isLiked) R.drawable.star_red else R.drawable.star_home,
                    0, 0
                )
                homeFragment.postLike(data._id)
            } else
                (context as BaseActivity).showLoginPopup()
        }

        holder.tvComment.setOnClickListener {
            itemClickListener.onItemClicked(position, "comment")
        }

        holder.imgProfileFollow.setOnClickListener {
            if (VrockkApplication.isLoggedIn()) {
                holder.imgProfileFollowDone.visibility = View.VISIBLE
                data.isFollowing = true
                homeFragment.followUnFollow(data.userId._id)
            } else
                (context as BaseActivity).showLoginPopup()
        }

        holder.imgSongClick.setOnClickListener {
            if (data.song != null) {
                homeFragment.pauseFeed()
                context.startActivity(
                    Intent(context, PostRelatedToSongActivity::class.java)
                        .putExtra("songId", data.song?.id)
                        .putExtra("uploadedBy", data.song?.uploadedBy)
                        .putExtra("song", data.song?.song)
                        .putExtra("songName", data.song?.description?.name)
                )
            }
        }

        holder.tvMusic.setOnClickListener {
            if (data.song != null) {
                homeFragment.pauseFeed()
                context.startActivity(
                    Intent(context, PostRelatedToSongActivity::class.java)
                        .putExtra("songId", data.song?.id)
                        .putExtra("uploadedBy", data.song?.uploadedBy)
                        .putExtra("song", data.song?.song)
                        .putExtra("songName", data.song?.description?.name)
                )
            }
        }

        holder.ivClose.setOnClickListener {
            holder.tvMore.visibility = View.VISIBLE
            holder.linearRange.visibility = View.GONE
        }

        holder.tvMore.setOnClickListener {
            holder.tvMore.visibility = View.INVISIBLE
            holder.linearRange.visibility = View.VISIBLE
            val aniRotate: Animation =
                AnimationUtils.loadAnimation(context, R.anim.slide_right_to_left)
            holder.linearRange.startAnimation(aniRotate)
        }

        holder.tvDuet.setOnClickListener {
            holder.tvMore.visibility = View.VISIBLE
            holder.linearRange.visibility = View.GONE

            if (VrockkApplication.isLoggedIn()) {
                itemClickListener.onItemClicked(position, "duet")
            } else
                (context as BaseActivity).showLoginPopup()
        }
        holder.tvShare.setOnClickListener {
            if (VrockkApplication.isLoggedIn()) {
                itemClickListener.onItemClicked(position, "share")
            } else
                (context as BaseActivity).showLoginPopup()
        }

        holder.tvGift.setOnClickListener {
            if (VrockkApplication.isLoggedIn()) {
                itemClickListener.onItemClicked(position, "home")
            } else
                (context as BaseActivity).showLoginPopup()
        }

        holder.tvReport.setOnClickListener {
            holder.tvMore.visibility = View.VISIBLE
            holder.linearRange.visibility = View.GONE
            if (VrockkApplication.isLoggedIn()) {
                (homeFragment as HomeFragment).showReportDialog(data._id)
            } else
                (context as BaseActivity).showLoginPopup()
        }

        holder.imgSongClick.startAnimation(
            AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise)
        )
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivPlaceholder = itemView.ivPlaceholder
        var layout_movie_wrapper = itemView.layout_movie_wrapper
        var tvName = itemView.tvName
        var tvDescription = itemView.tvDescription
        var tvComment = itemView.tvComment
        var tvLike = itemView.tvLike
        var tvMusic = itemView.tvMusic

        var ivProfile = itemView.ivProfile
        var imgProfileFollow = itemView.imgProfileFollow
        var imgProfileFollowDone = itemView.imgProfileFollowDone
        var playPause = itemView.playPause
        var determinateBar = itemView.determinateBar
        var vv_postVideo = itemView.vv_postVideo
        var imgSongClick = itemView.imgSongClick
        var songIconLayout = itemView.songIconLayout
        var tvShare = itemView.tvShare
        var tvGift = itemView.tvGift
        var tvReport = itemView.tvReport
        var rlAddFollow = itemView.rlAddFollow

        var linearRange = itemView.linearRange
        var ivClose = itemView.ivClose
        var tvDuet = itemView.tvDuet
        var tvMore = itemView.tvMore
    }
}