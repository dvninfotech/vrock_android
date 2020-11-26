package com.vrockk.viewmodels


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vrockk.api.ApiResponse
import com.vrockk.api.Repository
import com.vrockk.models.login.SocailLoginRequest
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.RequestBody
import retrofit2.http.PartMap

class SocailLoginViewModel(private val repository: Repository) : ViewModel(){

    lateinit var disposable : Disposable

    private val responseLiveData = MutableLiveData<ApiResponse>()

    fun socailLoginResponse(): MutableLiveData<ApiResponse> {
        return responseLiveData
    }

    fun socail_login(socailLoginRequest: SocailLoginRequest) {
        disposable = repository.socail_login(socailLoginRequest)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
            .subscribe(
                { result -> responseLiveData.setValue(ApiResponse.success(result)) },
                { throwable -> responseLiveData.setValue(ApiResponse.error(throwable)) }
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