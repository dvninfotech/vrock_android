package com.vrockk.viewmodels.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrockk.VrockkApplication
import com.vrockk.api.Event
import com.vrockk.api.Repository
import com.vrockk.api.parsers.HomeParser
import com.vrockk.models.home.home_page.HomeResponse
import com.vrockk.player.cache.FeedsPreferences
import com.vrockk.utils.Variables
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class HomePageViewModel(private val repository: Repository) : ViewModel() {

    lateinit var disposable: Disposable

    private val responseLiveData = MutableLiveData<Event<HomeResponse>>()
    private val responseWithoutPostKeyLiveData = MutableLiveData<Event<HomeResponse>>()
    private val preFetchedResponseLiveData = MutableLiveData<Event<HomeResponse>>()
    val errorLiveData = MutableLiveData<Event<String>>()

    fun getResponseLiveData(): MutableLiveData<Event<HomeResponse>> {
        return responseLiveData
    }

    fun getResponseWithoutPostKeyLiveData(): MutableLiveData<Event<HomeResponse>> {
        return responseWithoutPostKeyLiveData
    }

    fun getPreFetchedResponseLiveData(): MutableLiveData<Event<HomeResponse>> {
        return preFetchedResponseLiveData
    }

    fun getHomeFeeds(
        page: Int,
        count: Int,
        userId: String,
        latitude: Double,
        longitude: Double,
        songId: String,
        postId: String
    ) {
        viewModelScope.launch {
            disposable =
                repository.getHomeFeeds(
                    page, count, userId, latitude, longitude, songId, postId
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
                    .subscribe(
                        { responseLiveData.postValue(Event(HomeParser.parseResponse(it)) as Event<HomeResponse>?) },
                        { errorLiveData.postValue(Event(it.message) as Event<String>?) }
                    )
//        }
        }
    }

    fun preFetchHomeFeeds(
        page: Int,
        count: Int,
        userId: String,
        latitude: Double,
        longitude: Double,
        songId: String,
        postId: String
    ) {
        try {
            val homeResponse = HomeParser.parseResponse(FeedsPreferences.getTrendingFeeds(VrockkApplication.context))
                ?: throw Exception("null saved trending feeds")
            preFetchedResponseLiveData.postValue(
                Event(homeResponse) as Event<HomeResponse>?
            )
        } catch (e: Exception) {
            e.printStackTrace()

            viewModelScope.launch {
                disposable =
                    repository.getHomeFeedsRadixed(
                        page, count, userId, latitude, longitude, songId, postId,
                        Variables.HOME_PAGE_RADIX_POST_KEY
                    )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
                        .subscribe({
                            preFetchedResponseLiveData.postValue(
                                Event(
                                    HomeParser.parseResponse(it)
                                ) as Event<HomeResponse>?
                            )
                        },
                            { errorLiveData.postValue(Event(it.message) as Event<String>?) }
                        )
            }
        }
    }

    fun getV2HomeFeeds(
        page: Int,
        count: Int,
        userId: String,
        latitude: Double,
        longitude: Double,
        songId: String,
        postId: String
    ) {
        viewModelScope.launch {
            disposable =
                repository.getHomeFeedsRadixed(
                    page, count, userId, latitude, longitude, songId, postId,
                    Variables.HOME_PAGE_RADIX_POST_KEY
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
                    .subscribe({
//                        FeedsPreferences.saveTrendingJson(VrockkApplication.context, it)
                        responseLiveData.postValue(Event(HomeParser.parseResponse(it)) as Event<HomeResponse>?)
                    },
                        { errorLiveData.postValue(Event(it.message) as Event<String>?) }
                    )
        }
    }

    fun getV2HomeFeedsWithoutPostKey(
        page: Int,
        count: Int,
        userId: String,
        latitude: Double,
        longitude: Double,
        songId: String,
        postId: String
    ) {
        viewModelScope.launch {
            disposable =
                repository.getHomeFeedsRadixed(
                    page, count, userId, latitude, longitude, songId, postId, ""
                )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
//                .doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
                    .subscribe(
                        { responseWithoutPostKeyLiveData.postValue(Event(HomeParser.parseResponse(it)) as Event<HomeResponse>?) },
                        { errorLiveData.postValue(Event(it.message) as Event<String>?) }
                    )
        }
//        }
    }

    override fun onCleared() {
        try {
            disposable.dispose()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}