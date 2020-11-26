package com.vrockk.view.dashboard

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.vrockk.R
import com.vrockk.adapter.ViewPagerAdapter
import com.vrockk.base.BaseFragment
import com.vrockk.interfaces.AppPageChangeListener
import com.vrockk.player.media.MyMultiPlayer
import com.vrockk.view.feeds.ForYouFeedsFragment
import com.vrockk.view.feeds.TrendingFeedsFragment
import kotlinx.android.synthetic.main.fragment_posts.*

class PostsFragment : BaseFragment() {
    companion object {
        const val FEEDS_TYPE_TRENDING = "trending"
        const val FEEDS_TYPE_FOR_YOU = "foryou"
        const val FEEDS_TYPE_LIVE = "live"

        const val ITEMS_PER_PAGE_COUNT = 10
        const val RELOAD_OFFSET = 8

        var currentFeedsType = FEEDS_TYPE_TRENDING
        var isPostDisplaying = false
    }

    lateinit var typefaceRegular: Typeface
    lateinit var typefaceBlack: Typeface

//    var trendingFeedsFragment: TrendingFeedsFragment4? = null
    var trendingFeedsFragment: TrendingFeedsFragment? = null
    var forYouFeedsFragment: ForYouFeedsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        typefaceRegular = Typeface.createFromAsset(activity!!.assets, "roboto_regular.ttf")
        typefaceBlack = Typeface.createFromAsset(activity!!.assets, "roboto_black.ttf")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clHeadertop.visibility = if (isLoggedIn()) View.VISIBLE else View.GONE

        tvLive.setOnClickListener {
            showSnackbar("Go live as soon as you have 5,000 followers")
        }

        tvTranding.setOnClickListener {
            if (currentFeedsType == FEEDS_TYPE_TRENDING)
                return@setOnClickListener

            tvForYou.typeface = typefaceRegular
            tvTranding.typeface = typefaceBlack

            trendingFeedsFragment?.preparePlayers()
            ForYouFeedsFragment.isFragmentVisible = false
            TrendingFeedsFragment.isFragmentVisible = true

            currentFeedsType = FEEDS_TYPE_TRENDING
            vpPosts.currentItem = 0
            trendingFeedsFragment!!.playOrLoad()
        }

        tvForYou.setOnClickListener {
            if (currentFeedsType == FEEDS_TYPE_FOR_YOU)
                return@setOnClickListener

            tvForYou.typeface = typefaceBlack
            tvTranding.typeface = typefaceRegular

            forYouFeedsFragment?.preparePlayers()
            TrendingFeedsFragment.isFragmentVisible = false
            ForYouFeedsFragment.isFragmentVisible = true

            currentFeedsType = FEEDS_TYPE_FOR_YOU
            vpPosts.currentItem = 1
            forYouFeedsFragment!!.playOrLoad()
        }

//        trendingFeedsFragment = TrendingFeedsFragment4()
        trendingFeedsFragment = TrendingFeedsFragment()
        forYouFeedsFragment = ForYouFeedsFragment()

        val fragments = ArrayList<Fragment>()
        val titles = ArrayList<String>()
        fragments.add(trendingFeedsFragment!!)
        titles.add("Trending")
        if (isLoggedIn()) {
            fragments.add(forYouFeedsFragment!!)
            titles.add("For You")
        }
        vpPosts.addOnPageChangeListener(object: AppPageChangeListener() {
            override fun onPageSelected(position: Int) {
                if (position == 0)
                    tvTranding.performClick()
                else
                    tvForYou.performClick()
            }
        })
        vpPosts.adapter = ViewPagerAdapter(childFragmentManager, fragments, titles)
    }

    fun onVisibilityChanged(isVisible: Boolean) {
        TrendingFeedsFragment.isFragmentVisible = isVisible
        ForYouFeedsFragment.isFragmentVisible = isVisible
    }

    override fun onTabSwitched() {
        if (trendingFeedsFragment == null || forYouFeedsFragment ==  null)
            return

        if (currentFeedsType == FEEDS_TYPE_TRENDING)
            trendingFeedsFragment!!.onTabSwitched()
        else
            forYouFeedsFragment!!.onTabSwitched()
    }

    fun playFeed() {
        if (trendingFeedsFragment == null || forYouFeedsFragment ==  null)
            return

        if (currentFeedsType == FEEDS_TYPE_TRENDING)
            trendingFeedsFragment!!.playFeed()
        else
            forYouFeedsFragment!!.playFeed()
    }

    fun pauseFeed() {
        if (trendingFeedsFragment == null || forYouFeedsFragment ==  null)
            return

        if (currentFeedsType == FEEDS_TYPE_TRENDING)
            trendingFeedsFragment!!.pauseFeed()
        else
            forYouFeedsFragment!!.pauseFeed()
    }

    fun destroyMyPlayer() {
        MyMultiPlayer.destroy()
    }
}