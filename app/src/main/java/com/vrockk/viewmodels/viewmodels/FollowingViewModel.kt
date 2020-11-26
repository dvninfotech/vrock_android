package com.vrockk.viewmodels


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrockk.api.Event
import com.vrockk.api.Repository
import com.vrockk.api.parsers.HomeParser
import com.vrockk.models.home.home_page.HomeResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch


class FollowingViewModel(private val repository: Repository) : ViewModel(){

    lateinit var disposable : Disposable

    private val responseLiveData = MutableLiveData<Event<HomeResponse>>()
    val errorMessage = MutableLiveData<Event<String>>()

    fun getFollowingResponse(): MutableLiveData<Event<HomeResponse>> {
        return responseLiveData
    }

    fun getFollowingFeeds(token:String, page:Int, count:Int, songId:String) {
        viewModelScope.launch {
            disposable = repository.getFollowingFeeds(token,page,count,songId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    responseLiveData.postValue(Event(HomeParser.parseResponse(it)) as Event<HomeResponse>?)
                },
                    { errorMessage.postValue(Event(it.message) as Event<String>?) }
                )
        }
    }

    override fun onCleared() {
        try {
            disposable.dispose()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}