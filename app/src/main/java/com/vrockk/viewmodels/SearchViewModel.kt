package com.vrockk.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrockk.api.Event
import com.vrockk.api.Repository
import com.vrockk.api.parsers.SearchParser
import com.vrockk.models.search.*
import com.vrockk.utils.Constant
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class SearchViewModel(private val repository: Repository) : ViewModel() {

    lateinit var allSearchDisposable: Disposable
    lateinit var userSearchDisposable: Disposable
    lateinit var songSearchDisposable: Disposable
    lateinit var postsSearchDisposable: Disposable
    lateinit var tagsSearchDisposable: Disposable

    private val allModel = SearchAllModel()

    private val allSearchModelLiveData = MutableLiveData<Event<SearchAllModel>>()
    private val tagsSearchModelLiveData = MutableLiveData<Event<SearchHashTagModel>>()
    private val postsSearchModelLiveData = MutableLiveData<Event<SearchPostModel>>()
    private val songsSearchModelLiveData = MutableLiveData<Event<SearchSongModel>>()
    private val usersSearchModelLiveData = MutableLiveData<Event<SearchUserModel>>()

    val errorLiveData = MutableLiveData<Event<String>>()

    fun getAllSearchModelLiveData(): MutableLiveData<Event<SearchAllModel>> {
        return allSearchModelLiveData
    }

    fun getTagsSearchModelLiveData(): MutableLiveData<Event<SearchHashTagModel>> {
        return tagsSearchModelLiveData
    }

    fun getPostsSearchModelLiveData(): MutableLiveData<Event<SearchPostModel>> {
        return postsSearchModelLiveData
    }

    fun getSongsSearchModelLiveData(): MutableLiveData<Event<SearchSongModel>> {
        return songsSearchModelLiveData
    }

    fun getUsersSearchModelLiveData(): MutableLiveData<Event<SearchUserModel>> {
        return usersSearchModelLiveData
    }

    fun search(token: String, page: Int, search: String, type: String) {
        if (type == Constant.HASHTAGS)
            searchTags(token, page, search)
        if (type == Constant.SONG)
            searchSongs(token, page, search)
        if (type == Constant.USER)
            searchUsers(token, page, search)
        if (type == Constant.POST)
            searchPosts(token, page, search)
    }

    private fun searchTags(token: String, page: Int, search: String) {
        viewModelScope.launch {
            tagsSearchDisposable = repository.searchNew(token, page, search, Constant.HASHTAGS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                //.doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
                .subscribe(
                    {
                        run {
                            val tagsModel = SearchParser.parseSearchHashTags(it)
                            if (page == 1)
                                allModel.hashTagsDataModels.clear()
                            allModel.hashTagsDataModels.addAll(tagsModel!!.dataModels)
                            allSearchModelLiveData.postValue(Event(allModel))
                            tagsSearchModelLiveData.postValue(Event(tagsModel))
                        }
                    },
                    { errorLiveData.postValue(Event(it.message) as Event<String>) }
                )
        }
    }

    private fun searchPosts(token: String, page: Int, search: String) {
        viewModelScope.launch {
            postsSearchDisposable = repository.searchNew(token, page, search, Constant.POST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                //.doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
                .subscribe(
                    {
                        run {
                            val postModel = SearchParser.parseSearchPosts(it)
                            if (page == 1)
                                allModel.postsDataModels.clear()
                            allModel.postsDataModels.addAll(postModel!!.dataModels)
                            allSearchModelLiveData.postValue(Event(allModel))
                            postsSearchModelLiveData.postValue(Event(postModel))
                        }
                    },
                    { errorLiveData.postValue(Event(it.message) as Event<String>) }
                )
        }
    }

    private fun searchSongs(token: String, page: Int, search: String) {
        viewModelScope.launch {
            songSearchDisposable = repository.searchNew(token, page, search, Constant.SONG)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                //.doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
                .subscribe(
                    {
                        run {
                            val songModel = SearchParser.parseSearchSongs(it)
                            if (page == 1)
                                allModel.songsDataModels.clear()
                            allModel.songsDataModels.addAll(songModel!!.dataModels)
                            allSearchModelLiveData.postValue(Event(allModel))
                            songsSearchModelLiveData.postValue(Event(songModel))
                        }
                    },
                    { errorLiveData.postValue(Event(it.message) as Event<String>) }
                )
        }
    }

    private fun searchUsers(token: String, page: Int, search: String) {
        viewModelScope.launch {
            userSearchDisposable = repository.searchNew(token, page, search, Constant.USER)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                //.doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
                .subscribe(
                    {
                        run {
                            val userModel = SearchParser.parseSearchUsers(it)
                            if (page == 1)
                                allModel.userDataModels.clear()
                            allModel.userDataModels.addAll(userModel!!.dataModels)
                            allSearchModelLiveData.postValue(Event(allModel))
                            usersSearchModelLiveData.postValue(Event(userModel))
                        }
                    },
                    { errorLiveData.postValue(Event(it.message) as Event<String>) }
                )
        }
    }

    override fun onCleared() {
        try {
            allSearchDisposable.dispose()
            userSearchDisposable.dispose()
            songSearchDisposable.dispose()
            postsSearchDisposable.dispose()
            tagsSearchDisposable.dispose()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}