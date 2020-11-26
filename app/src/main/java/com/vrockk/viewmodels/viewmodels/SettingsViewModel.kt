package com.vrockk.viewmodels.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vrockk.api.ApiResponse
import com.vrockk.api.Repository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class SettingsViewModel(private val repository: Repository) : ViewModel(){

    lateinit var disposable : Disposable

    private val accountDisableDeleteResponseLiveData = MutableLiveData<ApiResponse>()
    private val shortenUrlResponseLiveData = MutableLiveData<ApiResponse>()

    fun accountDisableDeleteResponse(): MutableLiveData<ApiResponse> {
        return accountDisableDeleteResponseLiveData
    }

    fun shortenUrlResponseLiveData(): MutableLiveData<ApiResponse> {
        return shortenUrlResponseLiveData
    }

    fun disableDeleteAccount(authorization: String,
                             permanentDelete:Boolean,
                             deleteContents:Boolean) {
        disposable = repository.disableDeleteAccount(authorization, permanentDelete, deleteContents)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { d -> accountDisableDeleteResponseLiveData.setValue(ApiResponse.loading()) }
            .subscribe(
                { result -> accountDisableDeleteResponseLiveData.setValue(ApiResponse.success(result)) },
                { throwable -> accountDisableDeleteResponseLiveData.setValue(ApiResponse.error(throwable)) }
            )
    }

    fun shortenDeepLinkUrl(longUrl:String) {
        disposable = repository.shortenDeepLinkUrl(longUrl)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { d -> shortenUrlResponseLiveData.setValue(ApiResponse.loading()) }
            .subscribe(
                { result -> shortenUrlResponseLiveData.setValue(ApiResponse.success(result)) },
                { throwable -> shortenUrlResponseLiveData.setValue(ApiResponse.error(throwable)) }
            )
    }

    override fun onCleared() {
        try {
            disposable.dispose()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
}