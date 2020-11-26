package com.vrockk.viewmodels


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vrockk.api.ApiResponse
import com.vrockk.api.Repository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch


class UserBlockViewModel(private val repository: Repository) : ViewModel(){

    lateinit var disposable : Disposable

    private val responseLiveData = MutableLiveData<ApiResponse>()

    fun userBlockResponse(): MutableLiveData<ApiResponse> {
        return responseLiveData
    }

    fun userBlock(token:String,
                  profileId:String) {
        viewModelScope.launch {
            disposable = repository.blockUser(token, profileId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
                .subscribe(
                    { result -> responseLiveData.setValue(ApiResponse.success(result)) },
                    { throwable -> responseLiveData.setValue(ApiResponse.error(throwable)) }
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