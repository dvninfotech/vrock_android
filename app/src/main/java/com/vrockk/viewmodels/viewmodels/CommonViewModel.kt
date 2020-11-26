package com.vrockk.viewmodels.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.vrockk.api.ApiResponse
import com.vrockk.api.Event
import com.vrockk.api.Repository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import retrofit2.http.PartMap

class CommonViewModel(private val repository: Repository) : ViewModel() {

    lateinit var disposableUploadPost: Disposable
    lateinit var disposableSearchPage: Disposable
    lateinit var disposableAllSongs: Disposable
    lateinit var disposableHashTags: Disposable
    lateinit var disposableContactUs: Disposable

    private val uploadPostResponseLiveData = MutableLiveData<Event<ApiResponse>>()
    private val searchPageResponseLiveData = MutableLiveData<ApiResponse>()
    private val allSongsResponseLiveData = MutableLiveData<ApiResponse>()
    private val hashTagsResponseLiveData = MutableLiveData<ApiResponse>()
    private val contactUsResponseLiveData = MutableLiveData<ApiResponse>()

    fun uploadPostResponseLiveData(): MutableLiveData<Event<ApiResponse>> {
        return uploadPostResponseLiveData
    }

    fun searchPageResponseLiveData(): MutableLiveData<ApiResponse> {
        return searchPageResponseLiveData
    }

    fun allSongsResponseLiveData(): MutableLiveData<ApiResponse> {
        return allSongsResponseLiveData
    }

    fun hashTagsResponseLiveData(): MutableLiveData<ApiResponse> {
        return hashTagsResponseLiveData
    }

    fun contactUsResponseLiveData(): MutableLiveData<ApiResponse> {
        return contactUsResponseLiveData
    }

    fun uploadPost(header: String, @PartMap para: HashMap<String, RequestBody>) {
        viewModelScope.launch {
            disposableUploadPost = repository.uploadPost(header, para)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { d -> uploadPostResponseLiveData.setValue(Event(ApiResponse.loading())) }
                .subscribe(
                    { result -> uploadPostResponseLiveData.setValue(Event(ApiResponse.success(result))) },
                    { throwable -> uploadPostResponseLiveData.setValue(Event(ApiResponse.error(throwable))) }
                )
        }
    }

    fun getSearchPage(userId: String, page: Int, count: Int) {
        viewModelScope.launch {
            disposableSearchPage = repository.getSearchPage(userId, page, count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { d -> searchPageResponseLiveData.setValue(ApiResponse.loading()) }
                .subscribe(
                    { result -> searchPageResponseLiveData.setValue(ApiResponse.success(result)) },
                    { throwable -> searchPageResponseLiveData.setValue(ApiResponse.error(throwable)) }
                )
        }
    }

    /* fun search(page : Int ,search : String ,count: Int , type : String) {
         disposable = repository.search(page,search,count,type)
             .subscribeOn(Schedulers.io())
             .observeOn(AndroidSchedulers.mainThread())
             .doOnSubscribe { d -> responseLiveData.setValue(ApiResponse.loading()) }
             .subscribe(
                 { result -> responseLiveData.setValue(ApiResponse.success(result)) },
                 { throwable -> responseLiveData.setValue(ApiResponse.error(throwable)) }
             )
     }*/

    fun getAllSongs(token: String, page: Int, search: String, count: Int) {
        viewModelScope.launch {
            disposableAllSongs = repository.getAllSongs(token, page, search, count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { d -> allSongsResponseLiveData.setValue(ApiResponse.loading()) }
                .subscribe(
                    { result -> allSongsResponseLiveData.setValue(ApiResponse.success(result)) },
                    { throwable -> allSongsResponseLiveData.setValue(ApiResponse.error(throwable)) }
                )
        }
    }

    fun getHashTags(page: Int, count: Int, hashtag: String, postId: String, userId: String) {
        viewModelScope.launch {
            disposableHashTags = repository.getHashTags(page, count, hashtag, postId, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { d -> hashTagsResponseLiveData.setValue(ApiResponse.loading()) }
                .subscribe(
                    { result -> hashTagsResponseLiveData.setValue(ApiResponse.success(result)) },
                    { throwable -> hashTagsResponseLiveData.setValue(ApiResponse.error(throwable)) }
                )
        }
    }

    fun contactUs(name: String, email: String, message: String) {
        viewModelScope.launch {
            disposableContactUs = repository.contactUs(name, email, message)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { d -> contactUsResponseLiveData.setValue(ApiResponse.loading()) }
                .subscribe(
                    { result -> contactUsResponseLiveData.setValue(ApiResponse.success(result)) },
                    { throwable -> contactUsResponseLiveData.setValue(ApiResponse.error(throwable)) }
                )
        }
    }

    override fun onCleared() {
        try {
            disposableUploadPost.dispose()
            disposableSearchPage.dispose()
            disposableAllSongs.dispose()
            disposableHashTags.dispose()
            disposableContactUs.dispose()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}