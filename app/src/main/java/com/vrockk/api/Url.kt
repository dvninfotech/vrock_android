package com.vrockk.api

// for testing  .. local url
//const val BASE_URL = "https://appgrowthcompany.com:3090"

const val BASE_URL = "https://api.vrockk.mobi" // live url
//const val BASE_URL = "http://52.14.41.120:3090" //staging
const val IMAGE_BASE_URL = ""
const val SIGNUP = "/api/user/signup"
const val PROFILE_SETUP = "/api/user/profileSetup"
const val VERIFY_OTP = "/api/user/emailOtpVerify"
const val VERIFY_PHONE_OTP = "/api/user/verifyPhoneOTP"
const val LOGIN ="/api/user/login"
const val RESEND_OTP = "/api/user/resendOtp"
const val CHANGE_PASSWORD = "/api/user/changePassword"
const val SOCIAL_LOGIN = "/api/user/socialLogin"
const val FORGOT_PASSWORD = "/api/user/forgotPassword"
const val RESET_PASSWORD = "/api/user/resetPassword"
const val UPLOAD_POST = "/api/user/uploadPost"
const val UPDATE_PROFILE = "/api/user/updateProfile"

const val HOME = "/api/user/getHomePage"
//const val HOME_V2 = "/api/user/v2/getHomePage"
const val HOME_V2 = "/api/user/v3/getHomePage"
const val GET_FOLLOWING = "/api/user/getFollowingPage"
const val FOR_YOU = "/api/user/posts/forYou"
const val LIKE_POST= "/api/user/likePost"
const val COMMENT_POST = "/api/user/commentPost"
const val GET_COMMENTS = "/api/user/getComments"
const val FOLLOW_UNFOLLOW = "/api/user/followUser"

//const val PROFILE_PAGE = "/api/user/getProfilePage"
const val PROFILE_PAGE = "/api/user/v2/getProfilePage"
const val GET_LIKES_POST = "/api/user/getLikedPosts"
const val GET_FOLLOWERS = "/api/user/getFollowersList"
const val GET_FOLLOWING_LIST = "/api/user/getFollowingList"

//const val GET_USER_PROFILE = "/api/user/getUserProfile"
const val GET_USER_PROFILE = "/api/user/v2/getUserProfile"
const val POST_FAVOURITE_PROFILE = "/api/user/favoriteProfile"
const val GET_FAVOURITE_PROFILE = "/api/user/getFavoriteProfiles"

const val GET_SEARCH_PAGE = "/api/user/getSearchPage"
const val SEARCH = "/api/user/search"
const val SEARCH_HASHTAGS = "/api/user/search/hashTags"
const val SEARCH_POSTS = "/api/user/search/posts"
const val SEARCH_SONGS = "/api/user/search/songs"
const val SEARCH_USERS = "/api/user/search/users"
const val GET_HASHTAGS = "/api/user/getHashtagPosts"
const val BLOCK = "/api/user/block"
const val GETBLOCKUSER = "/api/user/getBlockedUsers"
const val ADD_VIEW_TO_POST = "/api/user/addViewToPost"

const val GETALLSONG = "/api/admin/getAllSongs"
const val UPLOAD_SONG = "/api/admin/song"
const val DELETE_COMMON = "/api/common/delete"

const val GET_HASH_TAGS_ = "/api/user/getHashTags"
const val GET_NOTIFICATION_LIST = "/api/user/notificationList"
const val GET_STAIC_PAGES = "/api/admin/getAllStaticPages"
const val REPORT = "/api/user/reportToAdmin"
const val CONTACT_US = "/api/user/contactUs"

const val GET_GIFTS = "/api/user/getAllGifts"
const val SEND_GIFT = "/api/user/sendGift"
const val ORDER_COMPLETE = "/api/user/completeOrder/google"
const val GET_ALL_SETTINGS = "/api/admin/setting"
const val REDEEM_COIN = "/api/user/createPayout"

const val DISABLE_DELETE_ACCOUNT = "/api/user/deleteAccount"
const val SHORTEN_DEEP_LINK_URL = "/api/user/shortUrl"



