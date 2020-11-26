package com.vrockk.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrockk.api.Event
import com.vrockk.api.Repository
import com.vrockk.api.parsers.FeedsParser
import com.vrockk.models.feeds.FeedsModel
import com.vrockk.utils.Variables
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class FeedsViewModel(private val repository: Repository) : ViewModel() {

    lateinit var feedsModelDisposable: Disposable
    lateinit var feedsModelUnRadixDisposable: Disposable
    lateinit var preFetchedFeedsModelDisposable: Disposable
    lateinit var formeFeedsModelDisposable: Disposable

    private val feedsModelLiveData = MutableLiveData<Event<FeedsModel>>()
    private val feedsModelUnRadixLiveData = MutableLiveData<Event<FeedsModel>>()
    private val preFetchedFeedsModelLiveData = MutableLiveData<Event<FeedsModel>>()
    private val formeFeedsModelLiveData = MutableLiveData<Event<FeedsModel>>()
    val errorLiveData = MutableLiveData<Event<String>>()

    fun getFeedsModelLiveData(): MutableLiveData<Event<FeedsModel>> {
        return feedsModelLiveData
    }

    fun getFeedsModelUnRadixKeyLiveData(): MutableLiveData<Event<FeedsModel>> {
        return feedsModelUnRadixLiveData
    }

    fun getPreFetchedFeedsModelLiveData(): MutableLiveData<Event<FeedsModel>> {
        return preFetchedFeedsModelLiveData
    }

    fun getFormeFeedsModelLiveData(): MutableLiveData<Event<FeedsModel>> {
        return formeFeedsModelLiveData
    }

    fun getFeeds(
        page: Int,
        count: Int,
        userId: String,
        latitude: Double,
        longitude: Double,
        songId: String,
        postId: String
    ) {
        viewModelScope.launch {
            feedsModelDisposable =
                repository.getHomeFeeds(
                    page, count, userId, latitude, longitude, songId, postId
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
                    .subscribe(
                        { feedsModelLiveData.postValue(Event(FeedsParser.parseFeeds(it)) as Event<FeedsModel>?) },
                        { errorLiveData.postValue(Event(it.message) as Event<String>?) }
                    )
//        }
        }
    }

    fun preFetchFeeds(
        page: Int,
        count: Int,
        userId: String,
        latitude: Double,
        longitude: Double,
        songId: String,
        postId: String
    ) {
        viewModelScope.launch {
            preFetchedFeedsModelDisposable =
                repository.getHomeFeedsRadixed(
                    page, count, userId, latitude, longitude, songId, postId,
                    Variables.HOME_PAGE_RADIX_POST_KEY
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
                    .subscribe(
                        { preFetchedFeedsModelLiveData.postValue(Event(FeedsParser.parseFeeds(it)) as Event<FeedsModel>?) },
                        { errorLiveData.postValue(Event(it.message) as Event<String>?) }
                    )
        }
    }

    fun getV2Feeds(
        page: Int,
        count: Int,
        userId: String,
        latitude: Double,
        longitude: Double,
        songId: String,
        postId: String
    ) {
        viewModelScope.launch {
            feedsModelDisposable =
                repository.getHomeFeedsRadixed(
                    page, count, userId, latitude, longitude, songId, postId,
                    Variables.HOME_PAGE_RADIX_POST_KEY
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
                    .subscribe(
                        { feedsModelLiveData.postValue(Event(FeedsParser.parseFeeds(it)) as Event<FeedsModel>?) },
                        { errorLiveData.postValue(Event(it.message) as Event<String>?) }
                    )
        }
    }

    fun getV2HomeFeedsUnRadix(
        page: Int,
        count: Int,
        userId: String,
        latitude: Double,
        longitude: Double,
        songId: String,
        postId: String
    ) {
        viewModelScope.launch {
            feedsModelUnRadixDisposable =
                repository.getHomeFeedsRadixed(
                    page, count, userId, latitude, longitude, songId, postId, ""
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
                    .subscribe(
                        { feedsModelUnRadixLiveData.postValue(Event(FeedsParser.parseFeeds(it)) as Event<FeedsModel>?) },
                        { errorLiveData.postValue(Event(it.message) as Event<String>?) }
                    )
        }
//        }
    }

    fun getForMeFeeds(token:String, page:Int, count:Int, songId:String) {
        viewModelScope.launch {
            formeFeedsModelDisposable = repository.getFollowingFeeds(token,page,count,songId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { formeFeedsModelLiveData.postValue(Event(FeedsParser.parseFeeds(it)) as Event<FeedsModel>?) },
                    { errorLiveData.postValue(Event(it.message) as Event<String>?) }
                )
        }
    }

    override fun onCleared() {
        try {
            preFetchedFeedsModelDisposable.dispose()
            feedsModelDisposable.dispose()
            feedsModelUnRadixDisposable.dispose()
            formeFeedsModelDisposable.dispose()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}