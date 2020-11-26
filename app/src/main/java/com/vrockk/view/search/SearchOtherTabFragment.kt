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
import com.vrockk.models.search.SearchHashTagModel
import com.vrockk.models.search.SearchPostModel
import com.vrockk.models.search.SearchSongModel
import com.vrockk.models.search.SearchUserModel
import com.vrockk.posts_play.PostRelatedToSongActivity
import com.vrockk.utils.Constant
import com.vrockk.view.feeds.TrendingFeedsFragment
import com.vrockk.view.posts_play.PostsPlayAcivity
import com.vrockk.view.profile.OtherProfileActivity
import com.vrockk.viewmodels.SearchViewModel
import kotlinx.android.synthetic.main.fragment_search_tab.*
import kotlinx.android.synthetic.main.layout_loader.*

class SearchOtherTabFragment(
    var searchType: String = ""
) : BaseFragment(), ItemClickListernerWithType {
    companion object {
        const val TAG = "SearchOtherTabFragment"
    }

//    private val searchViewModel by viewModel<SearchViewModel>()
    private lateinit var searchViewModel: SearchViewModel

    private lateinit var searchOtherTabAdapter: SearchOtherTabAdapter
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
        searchOtherTabAdapter = SearchOtherTabAdapter(context!!, searchType, this)
        rvSearch.adapter = searchOtherTabAdapter

        srlSearch.setOnRefreshListener {
            requestSearch(searchKey)
        }

        if (searchType == Constant.USER) {
            searchViewModel.getUsersSearchModelLiveData()
                .observe(activity!!, Observer<Event<SearchUserModel>> { response ->
                    response.getContentIfNotHandled()?.let {
                        hideProgress()
                        srlSearch.isRefreshing = false
                        if (it.success) {
                            searchOtherTabAdapter.setUserDataModels(it.dataModels)
                        }
                    }
                })
        } else if (searchType == Constant.SONG) {
            searchViewModel.getSongsSearchModelLiveData()
                .observe(activity!!, Observer<Event<SearchSongModel>> { response ->
                    response.getContentIfNotHandled()?.let {
                        hideProgress()
                        srlSearch.isRefreshing = false
                        if (it.success) {
                            searchOtherTabAdapter.setSongDataModels(it.dataModels)
                        }
                    }
                })
        } else if (searchType == Constant.POST) {
            searchViewModel.getPostsSearchModelLiveData()
                .observe(activity!!, Observer<Event<SearchPostModel>> { response ->
                    response.getContentIfNotHandled()?.let {
                        hideProgress()
                        srlSearch.isRefreshing = false
                        if (it.success) {
                            searchOtherTabAdapter.setPostDataModels(it.dataModels)
                        }
                    }
                })
        } else if (searchType == Constant.HASHTAGS) {
            searchViewModel.getTagsSearchModelLiveData()
                .observe(activity!!, Observer<Event<SearchHashTagModel>> { response ->
                    response.getContentIfNotHandled()?.let {
                        hideProgress()
                        srlSearch.isRefreshing = false
                        if (it.success) {
                            searchOtherTabAdapter.setHashTagDataModels(it.dataModels)
                        }
                    }
                })
        }
    }

    override fun onItemClicked(position: Int, type: String) {
        if (type == Constant.USER) {
            val userDataModel = searchOtherTabAdapter.getUserDataModel(position)
            TrendingFeedsFragment.otherUserProfileId = userDataModel.id
            startActivity(
                Intent(context, OtherProfileActivity::class.java)
                    .putExtra("_id", userDataModel.id)
            )

        } else if (type == Constant.SONG) {
            val songDataModel = searchOtherTabAdapter.getSongDataModel(position)
            startActivity(
                Intent(context, PostRelatedToSongActivity::class.java)
                    .putExtra("songId", songDataModel._id)
                    .putExtra("song", songDataModel.song)
                    .putExtra("uploadedBy", songDataModel.uploadedBy)
                    .putExtra("songName", songDataModel.description)
            )
        } else if (type == Constant.POST) {
            startActivity(
                Intent(context, PostsPlayAcivity::class.java)
                    .putExtra("position", position)
                    .putExtra("search", "")
                    .putExtra("latitude", getMyLatitude())
                    .putExtra("longitude", getMyLongitude())
                    .putExtra("postId", searchOtherTabAdapter.getPostDataModel(position)._id)
            )
        } else {
            startActivity(
                Intent(context, SearchHashtagActivity::class.java)
                    .putExtra("hashTag", searchOtherTabAdapter.getHashTagDataModel(position).name)
            )
        }
        activity!!.finish()
    }

    fun requestSearch(search: String) {
        searchKey = search
        page = 1
        hitSearchApiRequest()
    }

    private fun hitSearchApiRequest() {
        if (searchKey.contains("@"))
            searchKey = searchKey.replace("@", "")
//        if (searchKey.contains("#"))
//            searchKey = searchKey.replace("#", "")

        showProgress()
        searchViewModel.search(
            getMyAuthToken(),
            page,
            searchKey,
            searchType
        )
    }
}