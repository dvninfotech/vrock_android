package com.vrockk.viewmodels


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vrockk.api.Event
import com.vrockk.api.Repository
import com.vrockk.api.parsers.HomeParser
import com.vrockk.models.home.home_page.HomeResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class GetVideoSearchViewModel(private val repository: Repository) : ViewModel() {

    lateinit var disposable: Disposable

    private val responseLiveData = MutableLiveData<Event<HomeResponse>>()

    val errorMessage = MutableLiveData<Event<String>>()

    fun homeResponse(): MutableLiveData<Event<HomeResponse>> {
        return responseLiveData
    }

    fun getHomePageWithoutSong(page: Int, count: Int, userId: String, postId: String) {
        disposable = repository.getHomePageWithoutSong(page, count, userId, postId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            //  .doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
            .subscribe(
                { result -> responseLiveData.postValue(Event(result)) },
                { throwable -> errorMessage.postValue(Event(throwable.message) as Event<String>?) }
            )
    }

    fun getHomePageV2WithoutSongRadixedWithoutPostKey(
        page: Int,
        count: Int,
        userId: String,
        postId: String
    ) {
        disposable = repository.getHomePageWithoutSongRadixed(page, count, userId, postId, "")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            //  .doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
            .subscribe(
                { responseLiveData.postValue(Event(HomeParser.parseResponse(it)) as Event<HomeResponse>?) },
                { errorMessage.postValue(Event(it.message) as Event<String>?) }
            )
    }

    override fun onCleared() {
        try {
            disposable.dispose()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}