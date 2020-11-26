package com.vrockk.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.daasuu.gpuv.player.GPUPlayerView
import com.facebook.FacebookSdk
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Log
import com.google.android.exoplayer2.util.Util
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.api.IMAGE_BASE_URL
import com.vrockk.base.BaseActivity
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.home.home_page.Data
import com.vrockk.posts_play.PostRelatedToSongActivity
import com.vrockk.view.posts_play.PostsPlayAcivity
import com.vrockk.view.posts_play.PostsPlayAcivity.Companion.index
import com.vrockk.view.posts_play.PostsPlayAcivity.Companion.isHomeVisible
import com.vrockk.view.posts_play.PostsPlayAcivity.Companion.player
import com.vrockk.view.search.SearchHashtagActivity
import kotlinx.android.synthetic.main.post_play_adapter.view.*

class PostPlayAdapter(
    val context: Context,
    val list: ArrayList<Data>,
    val instanceObj: Activity,
    val ItemClickListernerWithType: ItemClickListernerWithType
) : RecyclerView.Adapter<PostPlayAdapter.ViewHolder>() {

    private var STREAM_URL_MP4_VOD_LONG = ""
    private var gpuPlayerView: GPUPlayerView? = null

    private var progressDialog: ProgressDialog? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view: View =
            LayoutInflater.from(context).inflate(R.layout.post_play_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.tvGift.setOnClickListener {
            if (VrockkApplication.user_obj != null) {
                ItemClickListernerWithType.onItemClicked(position, "home")
            } else {
                (context as BaseActivity).showLoginPopup()
            }

        }

        if (VrockkApplication.user_obj != null) {
            if (VrockkApplication.user_obj!!._id.equals(list[position].userId._id)) {
                holder.imgProfileFollow.visibility = View.GONE
                holder.imgProfileFollowDone.visibility = View.GONE
                holder.tvReport.visibility = View.GONE
                holder.tvGift.visibility = View.GONE
            } else {
                holder.tvReport.visibility = View.VISIBLE
                holder.tvGift.visibility = View.VISIBLE
            }
        }

        holder.ivProfile.setOnClickListener {
            if (VrockkApplication.user_obj != null) {

                ItemClickListernerWithType.onItemClicked(position, "other")

            } else {
                (context as BaseActivity).showLoginPopup()
            }
        }

//        if( VrockkApplication.user_obj!=null){
//            if(list[position].userId._id == VrockkApplication.user_obj!!._id){
//                holder.tvFollow.visibility = View.GONE
//            }else{
//                holder.tvFollow.visibility = View.VISIBLE
//            }
//        }

        if (index == position) {
            holder.itemView.playPause.visibility =
                /*if (player!!.isPlaying) View.VISIBLE else*/ View.GONE

            if (list[position].post.contains("media.vrockk")) {
                Glide.with(context).load(list.get(position).post).into(holder.ivPlaceholder!!)
            } else {
                val videourl: String = IMAGE_BASE_URL + list.get(position).post
                Glide.with(context).load(videourl).into(holder.ivPlaceholder!!)
            }

            if (player != null) {
                player?.stop()
                player = null
            }

            if (list[position].post.contains("media.vrockk")) {
                STREAM_URL_MP4_VOD_LONG = list[position].post
            } else {
                STREAM_URL_MP4_VOD_LONG = IMAGE_BASE_URL + list[position].post
            }
            setUpSimpleExoPlayer(holder)

            holder.tvName.setOnClickListener {
                if (VrockkApplication.user_obj != null) {
                    ItemClickListernerWithType.onItemClicked(position, "other")

                } else {
                    (context as BaseActivity).showLoginPopup()
                }
            }
        } else {
            holder.itemView.playPause.visibility = View.GONE
            player!!.stop()
        }

        if (isHomeVisible) {

        } else {
            player!!.stop()
        }


        holder.tvName.text = "@" + list[position].userId.userName
        holder.tvDescription.text = "" + list[position].description
        holder.tvMusic.text = "" + list[position].originalName
        holder.tvComment.text = "" + list[position].totalComments
        holder.tvLike.text = "" + list[position].totalLikes

        var profileImageUrl: String = list[position].userId.profilePic
        Glide.with(context).load(profileImageUrl)
            .placeholder(context.resources.getDrawable(R.drawable.user_placeholder))
            .error(context.resources.getDrawable(R.drawable.user_placeholder))
            .into(holder.ivProfile!!)

        if (list[position].isLiked) {
            holder.tvLike.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.star_red, 0, 0);
        } else {
            holder.tvLike.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.star_home, 0, 0);
        }

        if (list[position].isFollowing) {
            if (VrockkApplication.user_obj != null) {
                if (!VrockkApplication.user_obj?._id.equals(list[position].userId._id)) {
//            holder.tvFollow.text = "Following"
                    holder.imgProfileFollow.visibility = View.GONE
                    holder.imgProfileFollowDone.visibility = View.VISIBLE
                }
            }
        } else {
            if (VrockkApplication.user_obj != null) {
                if (!VrockkApplication.user_obj!!._id.equals(list[position].userId._id)) {
                    //holder.tvFollow.text = "Follow"
                    holder.imgProfileFollowDone.visibility = View.GONE
                    holder.imgProfileFollow.visibility = View.VISIBLE
                }
            }
        }


        holder.tvDescription.setOnHashtagClickListener { view, text ->
            context.startActivity(
                Intent(context, SearchHashtagActivity::class.java)
                    .putExtra("hashTag", text.toString())
            )
        }
        holder.tvLike.setOnClickListener {

            if (VrockkApplication.user_obj != null) {
                if (list[position].isLiked) {
                    if (holder.tvLike.text.toString().toInt() == 0) {
                    } else {
                        var like: Int = 0
                        like = holder.tvLike.text.toString().toInt() - 1
                        holder.tvLike.text = "" + like

                        list.get(position).totalLikes = like

                    }

                    list.get(position).isLiked = false
                    holder.tvLike.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        R.drawable.star_home,
                        0,
                        0
                    );
                    (instanceObj as PostsPlayAcivity).postLike(list.get(position)._id)
                } else {

                    var like: Int = 0
                    like = holder.tvLike.text.toString().toInt() + 1
                    holder.tvLike.text = "" + like

                    list.get(position).totalLikes = like


                    list.get(position).isLiked = true
                    holder.tvLike.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        R.drawable.star_red,
                        0,
                        0
                    );
                    (instanceObj as PostsPlayAcivity).postLike(list.get(position)._id)
                }
            } else {
                (context as BaseActivity).showLoginPopup()
            }

        }

        holder.tvComment.setOnClickListener {
            ItemClickListernerWithType.onItemClicked(position, "comment")
        }

//        holder.tvFollow.setOnClickListener {
//
//            Log.e("call","follow status: "+list.get(position).isFollowing)
//
//            if (list.get(position).isFollowing) {
//                if(VrockkApplication.user_obj != null){
//                    holder.tvFollow.text = "Follow"
//                    list.get(position).isFollowing = false
//                }
//
//                (instanceObj as PostsPlayAcivity).followUnfollow(list.get(position).userId._id)
//            }
//            else {
//                if(VrockkApplication.user_obj != null){
//                holder.tvFollow.text = "Following"
//                list.get(position).isFollowing = true
//                }
//                (instanceObj as PostsPlayAcivity).followUnfollow(list.get(position).userId._id)
//            }
//        }

        holder.imgProfileFollow.setOnClickListener {

            Log.e("call", "follow status: " + list.get(position).isFollowing)
            if (VrockkApplication.user_obj != null) {
//                holder.tvFollow.text = "Following"
                holder.imgProfileFollowDone.visibility = View.VISIBLE
                list.get(position).isFollowing = true
                (instanceObj as PostsPlayAcivity).followUnfollow(list.get(position).userId._id)
            } else {
                (context as BaseActivity).showLoginPopup()
            }
        }

        if (list[position].song != null) {
            holder.tvMusic.text = "" + list[position].song?.description?.name?.replace(".mp3", "")
        } else {
            holder.tvMusic.visibility = View.GONE
            holder.songIconLayout.visibility = View.GONE
        }
//        holder.itemView.setOnTouchListener(object : View.OnTouchListener {
//            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                if (player != null && player!!.isPlaying) {
//                    player!!.playWhenReady = false
//                    holder.itemView.playPause.visibility = View.VISIBLE
//                    android.util.Log.e("TOUCH", " stop ")
//                    return false
//                } else if (player != null && !player!!.isPlaying) {
//                    player!!.playWhenReady = true
//                    holder.itemView.playPause.visibility = View.GONE
//                    android.util.Log.e("TOUCH", " play ")
//                    return false
//                }
//
//                return true
//            }
//
//        })
        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (player != null && player!!.isPlaying) {
                    player!!.playWhenReady = false
                    holder.itemView.playPause.visibility = View.VISIBLE
                } else if (player != null && !player!!.isPlaying) {
                    player!!.playWhenReady = true
                    holder.itemView.playPause.visibility = View.GONE
                }
            }
        })

        holder.imgSongClick.setOnClickListener {
            if (list[position].song != null) {
                context.startActivity(
                    Intent(context, PostRelatedToSongActivity::class.java)
                        .putExtra("songId", list[position].song?.id)
                        .putExtra("uploadedBy", list[position].song?.uploadedBy)
                        .putExtra("song", list[position].song?.song)
                        .putExtra("songName", list[position].song?.description?.name)
                )
            }
        }

        holder.tvMusic.setOnClickListener {
            if (list[position].song != null) {
                context.startActivity(
                    Intent(context, PostRelatedToSongActivity::class.java)
                        .putExtra("songId", list[position].song?.id)
                        .putExtra("uploadedBy", list[position].song?.uploadedBy)
                        .putExtra("song", list[position].song?.song)
                        .putExtra("songName", list[position].song?.description?.name)
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
            if (VrockkApplication.user_obj != null) {
                ItemClickListernerWithType.onItemClicked(position, "duet")
            } else {
                (context as BaseActivity).showLoginPopup()
            }

        }
        holder.tvShare.setOnClickListener {
//            holder.tvMore.visibility = View.VISIBLE
//            holder.linearRange.visibility = View.GONE
            if (VrockkApplication.user_obj != null) {
                ItemClickListernerWithType.onItemClicked(position, "share")
            } else {
                (context as BaseActivity).showLoginPopup()
            }

        }

        holder.tvReport.setOnClickListener {
            holder.tvMore.visibility = View.VISIBLE
            holder.linearRange.visibility = View.GONE
            if (VrockkApplication.user_obj != null) {
                (context as PostsPlayAcivity).showReportDialog(list.get(position)._id)
            } else {
                (context as BaseActivity).showLoginPopup()
            }
        }

        val aniRotate: Animation =
            AnimationUtils.loadAnimation(
                FacebookSdk.getApplicationContext(),
                R.anim.rotate_clockwise
            )
        holder.imgSongClick.startAnimation(aniRotate)


    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var ivPlaceholder = itemView.ivPlaceholder

        var layout_movie_wrapper = itemView.layout_movie_wrapper
        var vv_postVideo = itemView.vv_postVideo
        var tvName = itemView.tvName
        var tvDescription = itemView.tvDescription
        var tvComment = itemView.tvComment
        var tvLike = itemView.tvLike
        var tvMusic = itemView.tvMusic

        //        var tvFollow = itemView.tvFollow
        var ivProfile = itemView.ivProfile
        var imgProfileFollow = itemView.imgProfileFollow
        var imgProfileFollowDone = itemView.imgProfileFollowDone
        var imgSongClick = itemView.imgSongClick
        var determinateBar = itemView.determinateBar
        var tvShare = itemView.tvShare
        var tvGift = itemView.tvGift
        var songIconLayout = itemView.songIconLayout
        var tvReport = itemView.tvReport
        var rlAddFollow = itemView.rlAddFollow

        var linearRange = itemView.linearRange
        var ivClose = itemView.ivClose
        var tvDuet = itemView.tvDuet
        var tvMore = itemView.tvMore
    }

    fun initPlayer(holder: ViewHolder) {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                2000,
                50000,
                1500,
                2000
            )
            .createDefaultLoadControl()
        player = SimpleExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .build()
        holder.vv_postVideo.player = player
        holder.vv_postVideo.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)



        player?.repeatMode = Player.REPEAT_MODE_ALL

    }

    private fun setUpSimpleExoPlayer(holder: ViewHolder) {

        if (player == null)
            initPlayer(holder)

        if (player != null)
            player!!.playWhenReady = false

        player!!.repeatMode = Player.REPEAT_MODE_ONE

        player!!.apply {
            val userAgent =
                Util.getUserAgent(context, context.resources.getString(R.string.app_name))
//            val proxyServer: HttpProxyCacheServer =
//                HttpProxyCacheServer.Builder(context).maxCacheSize(1024 * 1024 * 1024).build()
//            val proxyURL = proxyServer.getProxyUrl(STREAM_URL_MP4_VOD_LONG)
            val mediaSource = ProgressiveMediaSource.Factory(
                DefaultDataSourceFactory(context, userAgent),
                DefaultExtractorsFactory()
            ).createMediaSource(Uri.parse(STREAM_URL_MP4_VOD_LONG))
            prepare(mediaSource!!, true, false)
            player!!.playWhenReady = true
        }


        player!!.addListener(object : Player.DefaultEventListener() {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_IDLE -> {
                        holder.determinateBar.visibility = View.GONE
                        holder.itemView.playPause.visibility =
                            if (player!!.isPlaying) View.GONE else View.VISIBLE
                    }
                    Player.STATE_BUFFERING -> {
                        holder.determinateBar.visibility = View.VISIBLE
                    }
                    Player.STATE_READY -> {
                        holder.determinateBar.visibility = View.GONE
                        holder.vv_postVideo.visibility = View.VISIBLE
                        holder.itemView.playPause.visibility =
                            if (player!!.isPlaying) View.GONE else View.VISIBLE
                    }
                    Player.STATE_ENDED -> {
                    }
                }
            }
        })
    }
}