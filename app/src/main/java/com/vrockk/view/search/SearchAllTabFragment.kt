package com.vrockk.view.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.vrockk.R
import com.vrockk.api.Event
import com.vrockk.base.BaseFragment
import com.vrockk.interfaces.ItemClickListernerWithType
import com.vrockk.models.search.SearchAllModel
import com.vrockk.posts_play.PostRelatedToSongActivity
import com.vrockk.utils.Constant
import com.vrockk.view.feeds.TrendingFeedsFragment
import com.vrockk.view.posts_play.PostsPlayAcivity
import com.vrockk.view.profile.OtherProfileActivity
import com.vrockk.viewmodels.SearchViewModel
import kotlinx.android.synthetic.main.fragment_search_tab.*
import kotlinx.android.synthetic.main.layout_loader.*

class SearchAllTabFragment(): BaseFragment(), ItemClickListernerWithType {
    companion object {
        const val TAG = "SearchAllTabFragment"
    }

//    private val searchViewModel by viewModel<SearchViewModel>()
    private lateinit var searchViewModel: SearchViewModel

    private var tabItems = ArrayList<String>()

    private val allModel = SearchAllModel()
    private lateinit var searchAllTabAdapter: SearchAllTabAdapter

    private var searchKey: String = ""
    private var page: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchViewModel = ViewModelProvider(activity!!).get(SearchViewModel::class.java)
        initProgress(contentLoadingProgressBar)

        rvSearch.layoutManager = LinearLayoutManager(context)
        searchAllTabAdapter = SearchAllTabAdapter(context!!, tabItems, allModel, this)
        rvSearch.adapter = searchAllTabAdapter

        srlSearch.setOnRefreshListener {
            requestSearch(searchKey)
        }

        searchViewModel.getAllSearchModelLiveData()
            .observe(activity!!, Observer<Event<SearchAllModel>> { response ->
                response.getContentIfNotHandled()?.let {
                    hideProgress()
                    srlSearch.isRefreshing = false
                    handleSearchResponse(it)
                }
            })
    }

    private fun handleSearchResponse(allModel: SearchAllModel) {
        tabItems.clear()

        try {
            if (allModel.userDataModels.isNotEmpty()) {
                tabItems.add(Constant.USER)
                this.allModel.userDataModels.clear()
                this.allModel.userDataModels.addAll(allModel.userDataModels)
            }
            if (allModel.songsDataModels.isNotEmpty()) {
                tabItems.add(Constant.SONG)
                this.allModel.songsDataModels.clear()
                this.allModel.songsDataModels.addAll(allModel.songsDataModels)
            }
            if (allModel.postsDataModels.isNotEmpty()) {
                tabItems.add(Constant.POST)
                this.allModel.postsDataModels.clear()
                this.allModel.postsDataModels.addAll(allModel.postsDataModels)
            }
            if (allModel.hashTagsDataModels.isNotEmpty()) {
                tabItems.add(Constant.HASHTAGS)
                this.allModel.hashTagsDataModels.clear()
                this.allModel.hashTagsDataModels.addAll(allModel.hashTagsDataModels)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        hideProgress()
        srlSearch.isRefreshing = false
        searchAllTabAdapter./*setTabItems(tabItems)*/notifyDataSetChanged()
    }

    override fun onItemClicked(position: Int, type: String) {
        if (type == Constant.USER) {
            TrendingFeedsFragment.otherUserProfileId = allModel.userDataModels[position].id

            startActivity(
                Intent(context, OtherProfileActivity::class.java)
                    .putExtra("_id", allModel.userDataModels[position].id)
            )

        } else if (type == Constant.SONG) {
            if (allModel.songsDataModels.isNotEmpty()) {
                val songDataModel = allModel.songsDataModels[position]
                startActivity(
                    Intent(context, PostRelatedToSongActivity::class.java)
                        .putExtra("songId", songDataModel._id)
                        .putExtra("song", songDataModel.song)
                        .putExtra("uploadedBy", songDataModel.uploadedBy)
                        .putExtra("songName", songDataModel.description)
                )
            }
        } else if (type == Constant.POST) {
            startActivity(
                Intent(context, PostsPlayAcivity::class.java)
                    .putExtra("position", position)
                    .putExtra("search", "")
                    .putExtra("latitude", getMyLatitude())
                    .putExtra("longitude", getMyLongitude())
                    .putExtra("postId", allModel.postsDataModels[position]._id)
            )
        } else {
            startActivity(
                Intent(context, SearchHashtagActivity::class.java)
                    .putExtra("hashTag", allModel.hashTagsDataModels[position].name)
            )
        }
        activity!!.finish()
    }

    fun requestSearch(search: String) {
        searchKey = search
        page = 1
    }
}