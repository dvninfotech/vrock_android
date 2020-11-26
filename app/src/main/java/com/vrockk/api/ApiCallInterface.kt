package com.vrockk.api

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.vrockk.models.coins.order_complete.CompleteOrderRequest
import com.vrockk.models.home.home_page.HomeResponse
import com.vrockk.models.login.SocailLoginRequest
import com.vrockk.models.search.SearchModel
import com.vrockk.models.upload_post.UploadPostResponse
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface ApiCallInterface {

    @FormUrlEncoded
    @POST(REDEEM_COIN)
    fun reedem_coin(
        @Header("Authorization") token: String,
        @Field("coins") coins: Int,
        @Field("remainingCoins") remaining_coins: Int
    ): Observable<JsonElement>

    @GET(GET_ALL_SETTINGS)
    fun get_all_settings(): Observable<JsonElement>

    @POST(ORDER_COMPLETE)
    fun complete_order(
        @Header("Authorization") token: String,
        @Body completeOrderRequest: CompleteOrderRequest
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(SEND_GIFT)
    fun send_gift(
        @Header("Authorization") token: String,
        @Field("user") user: String,
        @Field("coins") coins: Int,
        @Field("coinType") coinType: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(REPORT)
    fun reportToAdmin(
        @Header("Authorization") token: String,
        @Field("post") post: String,
        @Field("message") message: String,
        @Field("type") type: String
    ): Observable<JsonElement>

    @GET(GET_STAIC_PAGES)
    fun get_static_pages(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("count") count: Int
    ): Observable<JsonElement>


    @GET(GET_NOTIFICATION_LIST)
    fun getNotificationList(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("count") count: Int
    ): Observable<JsonElement>


    @GET(GET_HASH_TAGS_)
    fun get_hash_tags(): Observable<JsonElement>


    @FormUrlEncoded
    @POST(BLOCK)
    fun postBlock(
        @Header("Authorization") token: String,
        @Field("profileId") profileId: String
    ): Observable<JsonElement>

    @GET(GETBLOCKUSER)
    fun getBlockListUser(
        @Header("Authorization") token: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(GET_USER_PROFILE)
    fun getUserProfile(
        @Field("userId") userId: String,
        @Field("page") page: Int,
        @Field("count") count: Int,
        @Field("user") user: String,
        @Field("postId") postId: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(POST_FAVOURITE_PROFILE)
    fun postFavouriteProfile(
        @Header("Authorization") token: String,
        @Field("profileId") profileId: String
    ): Observable<JsonElement>


    @GET(GET_FAVOURITE_PROFILE)
    fun getFavouriteProfile(
        @Header("Authorization") token: String
    ): Observable<JsonElement>


    @FormUrlEncoded
    @POST(REPORT)
    fun reportToCommentAdmin(
        @Header("Authorization") token: String,
        @Field("comment") comment: String,
        @Field("message") message: String,
        @Field("type") type: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(GET_FOLLOWING_LIST)
    fun get_following_list(
        @Header("Authorization") token: String,
        @Field("user") user: String,
        @Field("userId") userId: String,
        @Field("search") search: String,
        @Field("page") page: Int,
        @Field("count") count: Int
    ): Observable<JsonElement>


    @FormUrlEncoded
    @POST(GET_FOLLOWERS)
    fun getFollowers(
        @Header("Authorization") token: String,
        @Field("user") user: String,
        @Field("userId") userId: String,
        @Field("search") search: String,
        @Field("page") page: Int,
        @Field("count") count: Int
    ): Observable<JsonElement>


    @GET(GET_LIKES_POST)
    fun getLikesPost(
        @Header("Authorization") token: String
    ): Observable<JsonElement>


    @GET(GET_GIFTS)
    fun getGifts(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("count") count: Int
    ): Observable<JsonElement>


    @FormUrlEncoded
    @POST(PROFILE_PAGE)
    fun getProfilePage(
        @Header("Authorization") token: String,
        @Field("page") page: Int,
        @Field("count") count: Int,
        @Field("postId") postId: String
    ): Observable<JsonElement>


    @FormUrlEncoded
    @POST(HOME)
    fun getHomePage(
        @Field("page") page: Int,
        @Field("count") count: Int,
        @Field("userId") userId: String,
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double,
        @Field("songId") songId: String,
        @Field("postId") postId: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(HOME_V2)
    fun getHomePageRadixed(
        @Field("page") page: Int,
        @Field("count") count: Int,
        @Field("userId") userId: String,
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double,
        @Field("songId") songId: String,
        @Field("postId") postId: String,
        @Field("postKey") postKey: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(HOME)
    fun getHomePageWithoutSong(
        @Field("page") page: Int,
        @Field("count") count: Int,
        @Field("userId") userId: String,
        @Field("postId") postId: String
    ): Observable<HomeResponse>

    @FormUrlEncoded
    @POST(HOME_V2)
    fun getHomePageWithoutSongRadixed(
        @Field("page") page: Int,
        @Field("count") count: Int,
        @Field("userId") userId: String,
        @Field("postId") postId: String,
        @Field("postKey") postKey: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(GET_FOLLOWING)
    fun getFollowingFeeds(
        @Header("Authorization") token: String,
        @Field("page") page: Int,
        @Field("count") count: Int
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(FOR_YOU)
    fun getForYouFeeds(
        @Header("Authorization") token: String,
        @Field("page") page: Int,
        @Field("count") count: Int,
        @Field("userId") userId: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(LIKE_POST)
    fun likePost(
        @Header("Authorization") token: String,
        @Field("postId") postId: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(COMMENT_POST)
    fun commentPost(
        @Header("Authorization") token: String,
        @Field("postId") postId: String,
        @Field("comment") comment: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(GET_COMMENTS)
    fun getComments(
        @Header("Authorization") token: String, @Field("page") page: Int,
        @Field("count") count: String,
        @Field("postId") postId: String
    ): Observable<JsonElement>


    @FormUrlEncoded
    @POST(FOLLOW_UNFOLLOW)
    fun followUnfollow(
        @Header("Authorization") token: String,
        @Field("following") postId: String
    ): Observable<JsonElement>


    //////

    @Multipart
    @PUT(UPDATE_PROFILE)
    fun update_profile(
        @Header("Authorization") token: String,
        @PartMap para: HashMap<String, RequestBody>
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(FORGOT_PASSWORD)
    fun forgot_password(
        @Field("email") email: String?,
        @Field("phone") phone: String?,
        @Field("countryCode") countrycode: String?
    ): Observable<JsonElement>


    @FormUrlEncoded
    @POST(RESET_PASSWORD)
    fun reset_password(
        @Field("password") password: String?,
        @Field("email") email: String?,
        @Field("phone") phone: String?,
        @Field("countryCode") countrycode: String?
    ): Observable<JsonElement>

    @Multipart
    @POST(PROFILE_SETUP)
    fun profile_setup(@PartMap para: HashMap<String, RequestBody>): Observable<JsonElement>

    @FormUrlEncoded
    @POST(SIGNUP)
    fun signUp(
        @Field("email") email: String?,
        @Field("phone") phone: String?,
        @Field("countryCode") countrycode: String?
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(VERIFY_OTP)
    fun verify_otp(
        @Field("email") email: String?,
        @Field("otp") otp: String?,
        @Field("phone") phone: String?,
        @Field("countryCode") countrycode: String?
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(VERIFY_PHONE_OTP)
    fun verify_phone_otp(
        @Field("phone") phone: String,
        @Field("countryCode") countryCode: String,
        @Field("deviceType") deviceType: String,
        @Field("deviceId") deviceId: String,
        @Field("otp") otp: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(LOGIN)
    fun login(
        @Field("email") email: String?,
        @Field("password") password: String?,
        @Field("phone") phone: String?,
        @Field("countryCode") countrycode: String?,
        @Field("deviceType") deviceType: String?,
        @Field("deviceId") deviceId: String?
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(RESEND_OTP)
    fun resend_otp(
        @Field("email") email: String?,
        @Field("otp") otp: String?,
        @Field("phone") phone: String?,
        @Field("countryCode") countrycode: String?
    ): Observable<JsonElement>

    @FormUrlEncoded
    @PUT(CHANGE_PASSWORD)
    fun change_password(
        @Header("Authorization") token: String,
        @Field("oldPassword") currentPassword: String,
        @Field("newPassword") newPassword: String,
        @Field("confirmPassword") confirmPassword: String
    ): Observable<JsonElement>


    @POST(SOCIAL_LOGIN)
    fun socail_login(@Body socailLoginRequest: SocailLoginRequest): Observable<JsonElement>

    @Multipart
    @POST(UPLOAD_POST)
    fun uploadPostRetrofit(
        @Header("Authorization") token: String,
        @PartMap para: HashMap<String, RequestBody>
    ): Call<UploadPostResponse>

    @Multipart
    @POST(UPLOAD_POST)
    fun uploadPost(
        @Header("Authorization") token: String,
        @PartMap para: HashMap<String, RequestBody>
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(GET_SEARCH_PAGE)
    fun getSearchPage(
        @Field("userId") userId: String,
        @Field("page") page: Int,
        @Field("count") count: Int
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(SEARCH)
    fun search(
        @Field("userId") userId: String,
        @Field("page") page: Int,
        @Field("search") search: String,
        @Field("count") count: Int,
        @Field("type") type: String
    ): Observable<SearchModel>

    @FormUrlEncoded
    @POST(SEARCH_HASHTAGS)
    fun searchHashtags(
        @Header("Authorization") token: String,
        @Field("page") page: Int,
        @Field("search") search: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(SEARCH_POSTS)
    fun searchPosts(
        @Header("Authorization") token: String,
        @Field("page") page: Int,
        @Field("search") search: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(SEARCH_SONGS)
    fun searchSongs(
        @Header("Authorization") token: String,
        @Field("page") page: Int,
        @Field("search") search: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(SEARCH_USERS)
    fun searchUsers(
        @Header("Authorization") token: String,
        @Field("page") page: Int,
        @Field("search") search: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(GETALLSONG)
    fun getAllSongs(
        @Header("Authorization") token: String,
        @Field("page") page: Int,
        @Field("search") search: String,
        @Field("count") count: Int
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(ADD_VIEW_TO_POST)
    fun addViewToPost(
        @Header("Authorization") token: String,
        @Field("postId") postId: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(GET_HASHTAGS)
    fun getHashTags(
        @Field("page") page: Int,
        @Field("count") count: Int,
        @Field("hashtag") hashtag: String,
        @Field("postId") postId: String,
        @Field("userId") userId: String
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(CONTACT_US)
    fun contactUs(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("message") message: String
    ): Observable<JsonElement>


    @FormUrlEncoded
    @POST(DELETE_COMMON)
    fun deleteCommon(
        @Header("Authorization") token: String,
        @Field("id") id: String,
        @Field("model") model: String
    ): Observable<JsonElement>


    @Multipart
    @POST(UPLOAD_SONG)
    fun updloadSong(
        @Header("Authorization") token: String,
        @PartMap para: HashMap<String, RequestBody>
    ): Observable<JsonElement>

    @Multipart
    @POST("/api/user/uploadPost")
    fun uploadPost(@Part file: MultipartBody.Part?): Call<JsonObject?>?

    @FormUrlEncoded
    @POST(DISABLE_DELETE_ACCOUNT)
    fun disableDeleteAccount(
        @Header("Authorization") authorization: String,
        @Field("parmanentDelete") permanentDelete: Boolean,
        @Field("deleteContent") deleteContent: Boolean
    ): Observable<JsonElement>

    @FormUrlEncoded
    @POST(SHORTEN_DEEP_LINK_URL)
    fun shortenDeepLinkUrl(
        @Field("longUrl") longUrl: String
    ): Observable<JsonElement>
}