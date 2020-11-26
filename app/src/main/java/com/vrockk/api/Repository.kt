package com.vrockk.api

import com.google.gson.JsonElement
import com.vrockk.models.coins.order_complete.CompleteOrderRequest
import com.vrockk.models.home.home_page.HomeResponse
import com.vrockk.models.login.SocailLoginRequest
import com.vrockk.models.search.SearchModel
import com.vrockk.utils.Constant
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.Header
import retrofit2.http.PartMap

class Repository(private val apiCallInterface: ApiCallInterface) {

    fun redeem_coins(token: String, coins: Int, remaining_coins: Int): Observable<JsonElement> {
        return apiCallInterface.reedem_coin(token, coins, remaining_coins)
    }

    fun reportCommentToAdmin(
        token: String,
        comment: String,
        message: String,
        type: String
    ): Observable<JsonElement> {
        return apiCallInterface.reportToCommentAdmin(token, comment, message, type)
    }


    fun get_all_settings(): Observable<JsonElement> {
        return apiCallInterface.get_all_settings()
    }

    fun completeOrder(
        token: String,
        completeOrderRequest: CompleteOrderRequest
    ): Observable<JsonElement> {
        return apiCallInterface.complete_order(token, completeOrderRequest)
    }

    fun sendGift(
        token: String,
        user: String,
        coins: Int,
        coinType: String
    ): Observable<JsonElement> {
        return apiCallInterface.send_gift(token, user, coins, coinType)
    }

    fun reportToAdmin(
        token: String,
        post: String,
        message: String,
        type: String
    ): Observable<JsonElement> {
        return apiCallInterface.reportToAdmin(token, post, message, type)
    }

    fun get_static_pages(token: String, page: Int, count: Int): Observable<JsonElement> {
        return apiCallInterface.get_static_pages(token, page, count)
    }

    suspend fun getNotifications(token: String, page: Int, count: Int): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.getNotificationList(token, page, count)
        }
    }

    fun get_hash_tags(): Observable<JsonElement> {
        return apiCallInterface.get_hash_tags()
    }


    suspend fun blockUser(
        token: String,
        profileId: String
    ): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.postBlock(token, profileId)
        }
    }

    fun blockUserList(token: String): Observable<JsonElement> {
        return apiCallInterface.getBlockListUser(token)
    }

    suspend fun getUserProfile(
        userId: String,
        page: Int,
        count: Int,
        user: String,
        postId: String
    ): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.getUserProfile(userId, page, count, user, postId)
        }
    }

    suspend fun postFavouriteProfile(
        token: String,
        profileId: String
    ): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.postFavouriteProfile(token, profileId)
        }
    }

    fun get_favourite_profile(token: String): Observable<JsonElement> {
        return apiCallInterface.getFavouriteProfile(token)
    }


    fun get_following_list(
        token: String,
        user: String,
        userId: String,
        search: String,
        page: Int,
        count: Int
    ): Observable<JsonElement> {
        return apiCallInterface.get_following_list(token, user, userId, search, page, count)
    }

    fun getFollowers(
        token: String,
        user: String,
        userId: String,
        search: String,
        page: Int,
        count: Int
    ): Observable<JsonElement> {
        return apiCallInterface.getFollowers(token, user, userId, search, page, count)
    }

    fun getLikesPost(
        token: String
    ): Observable<JsonElement> {
        return apiCallInterface.getLikesPost(token)
    }

    fun getGifts(
        token: String,
        page: Int,
        count: Int
    ): Observable<JsonElement> {
        return apiCallInterface.getGifts(token, page, count)
    }

    suspend fun getProfilePage(
        token: String,
        page: Int,
        count: Int,
        postId: String
    ): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.getProfilePage(token, page, count, postId)
        }
    }

    suspend fun getHomeFeeds(
        page: Int,
        count: Int,
        userId: String,
        latitude: Double,
        longitude: Double,
        songId: String,
        postId: String
    ): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.getHomePage(
                page,
                count,
                userId,
                latitude,
                longitude,
                songId,
                postId
            )
        }
    }

    suspend fun getHomeFeedsRadixed(
        page: Int,
        count: Int,
        userId: String,
        latitude: Double,
        longitude: Double,
        songId: String,
        postId: String,
        postKey: String
    ): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.getHomePageRadixed(
                page,
                count,
                userId,
                latitude,
                longitude,
                songId,
                postId,
                postKey
            )
        }
    }

    fun getHomePageWithoutSong(
        page: Int,
        count: Int,
        userId: String,
        postId: String
    ): Observable<HomeResponse> {
        return apiCallInterface.getHomePageWithoutSong(page, count, userId, postId)
    }

    fun getHomePageWithoutSongRadixed(
        page: Int,
        count: Int,
        userId: String,
        postId: String,
        postKey: String
    ): Observable<JsonElement> {
        return apiCallInterface.getHomePageWithoutSongRadixed(page, count, userId, postId, postKey)
    }

    suspend fun getFollowingFeeds(
        token: String,
        page: Int,
        count: Int,
        songId: String
    ): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.getFollowingFeeds(token, page, count)
        }
    }

    suspend fun getForYouFeeds(
        token: String,
        page: Int,
        count: Int,
        userId: String
    ): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.getForYouFeeds(token, page, count, userId)
        }
    }

    fun likePost(token: String, postId: String): Observable<JsonElement> {
        return apiCallInterface.likePost(token, postId)
    }

    fun commentPost(
        token: String,
        postId: String,
        comment: String
    ): Observable<JsonElement> {
        return apiCallInterface.commentPost(token, postId, comment)
    }

    fun getComments(
        token: String, page: Int,
        count: String,
        postId: String
    ): Observable<JsonElement> {
        return apiCallInterface.getComments(token, page, count, postId)
    }

    suspend fun followUnfollow(
        @Header("Authorization") token: String,
        @Field("following") postId: String
    ): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.followUnfollow(token, postId)
        }
    }


    //////////////


    fun update_profile_setup(
        token: String,
        para: HashMap<String, RequestBody>
    ): Observable<JsonElement> {
        return apiCallInterface.update_profile(token, para)
    }

    fun reset_password(
        password: String?,
        email: String?,
        phone: String?,
        countrycode: String?
    ): Observable<JsonElement> {
        return apiCallInterface.reset_password(password, email, phone, countrycode)
    }

    fun forgot_password(
        email: String?,
        phone: String?,
        countrycode: String?
    ): Observable<JsonElement> {
        return apiCallInterface.forgot_password(email, phone, countrycode)
    }

    fun profile_setup(para: HashMap<String, RequestBody>): Observable<JsonElement> {
        return apiCallInterface.profile_setup(para)
    }

    fun signUpApi(
        email: String?,
        phone: String?,
        countrycode: String?
    ): Observable<JsonElement> {
        return apiCallInterface.signUp(email, phone, countrycode)
    }

    fun verifyOtpApi(
        email: String?,
        otp: String?,
        phone: String?,
        countrycode: String?
    ): Observable<JsonElement> {
        return apiCallInterface.verify_otp(email, otp, phone, countrycode)
    }

    fun verifyPhoneOtpApi(
        phone: String,
        countryCode: String,
        deviceType: String,
        deviceId: String,
        otp: String
    ): Observable<JsonElement> {
        return apiCallInterface.verify_phone_otp(phone, countryCode, deviceType, deviceId, otp)
    }

    fun login(
        email: String?,
        password: String?,
        phone: String?,
        countrycode: String?,
        deviceType: String,
        deviceId: String
    ): Observable<JsonElement> {
        return apiCallInterface.login(email, password, phone, countrycode, deviceType, deviceId)
    }

    fun resendOtp(
        email: String?,
        otp: String?,
        phone: String?,
        countrycode: String?
    ): Observable<JsonElement> {
        return apiCallInterface.resend_otp(email, otp, phone, countrycode)
    }

    fun changePassword(
        token: String,
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Observable<JsonElement> {
        return apiCallInterface.change_password(
            token,
            currentPassword,
            newPassword,
            confirmPassword
        )
    }

    suspend fun getSearchPage(userId: String, page: Int, count: Int): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.getSearchPage(userId, page, count)
        }
    }

    fun addViewToPost(token: String, postId: String): Observable<JsonElement> {
        return apiCallInterface.addViewToPost(token, postId)
    }

    suspend fun search(
        userid: String,
        page: Int,
        search: String,
        count: Int,
        type: String
    ): Observable<SearchModel> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.search(userid, page, search, count, type)
        }
    }

    suspend fun searchNew(
        token: String,
        page: Int,
        search: String,
        type: String
    ): Observable<JsonElement> {
        when (type) {
            Constant.HASHTAGS -> {
                return withContext(Dispatchers.IO) {
                    apiCallInterface.searchHashtags(token, page, search)
                }
            }
            Constant.SONG -> {
                return withContext(Dispatchers.IO) {
                    apiCallInterface.searchSongs(token, page, search)
                }
            }
            Constant.USER -> {
                return withContext(Dispatchers.IO) {
                    apiCallInterface.searchUsers(token, page, search)
                }
            }
            else -> {
                return withContext(Dispatchers.IO) {
                    apiCallInterface.searchPosts(token, page, search)
                }
            }
        }
    }

    suspend fun getAllSongs(token: String, page: Int, search: String, count: Int): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.getAllSongs(token, page, search, count)
        }
    }

    suspend fun getHashTags(
        page: Int,
        count: Int,
        hashtag: String,
        postId: String,
        userId: String
    ): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.getHashTags(page, count, hashtag, postId, userId)
        }
    }

    suspend fun contactUs(name: String, email: String, message: String): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.contactUs(name, email, message)
        }
    }

    fun deleteCommon(header: String, id: String, model: String): Observable<JsonElement> {
        return apiCallInterface.deleteCommon(header, id, model)
    }

    fun socail_login(socailLoginRequest: SocailLoginRequest): Observable<JsonElement> {
        return apiCallInterface.socail_login(socailLoginRequest)
    }


    fun uploadSong(token: String, para: HashMap<String, RequestBody>): Observable<JsonElement> {
        return apiCallInterface.updloadSong(token, para)
    }


    suspend fun uploadPost(
        header: String,
        @PartMap para: HashMap<String, RequestBody>
    ): Observable<JsonElement> {
        return withContext(Dispatchers.IO) {
            apiCallInterface.uploadPost(header, para)
        }
    }

    fun disableDeleteAccount(
        authorization: String,
        permanentDelete: Boolean,
        deleteContent: Boolean
    ): Observable<JsonElement> {
        return apiCallInterface.disableDeleteAccount(authorization, permanentDelete, deleteContent)
    }

    fun shortenDeepLinkUrl(
        longUrl: String
    ): Observable<JsonElement> {
        return apiCallInterface.shortenDeepLinkUrl(longUrl)
    }
}