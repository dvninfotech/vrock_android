package com.vrockk.koin_di

import com.vrockk.viewmodels.*
import com.vrockk.viewmodels.viewmodels.*
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val viewModelModule : Module = module {

    viewModel {  SignUpViewModel(get()) }
    viewModel {  LoginViewModel(get()) }
    viewModel {  EmailOtpVerficationViewModel(get()) }
    viewModel {  ResendOtpViewModel(get()) }
    viewModel {  ChangePasswordViewModel(get()) }
    viewModel {  SocailLoginViewModel(get()) }
    viewModel {  ProfileSetupViewModel(get()) }
    viewModel {  PhoneOtpVerificationVeiwModel(get()) }
    viewModel {  ForgotPasswordViewModel(get()) }
    viewModel {  ResetPasswordViewModel(get()) }
    viewModel {  UpdateProfileViewModel(get()) }
    viewModel {  CommonViewModel(get()) }

    viewModel {  HomePageViewModel(get()) }
    viewModel {  FollowingViewModel(get()) }
    viewModel {  LikePostViewModel(get()) }
    viewModel {  FollowUnfollowViewModel(get()) }
    viewModel {  CommentPostViewModel(get()) }
    viewModel {  GetCommentsViewModel(get()) }

    viewModel {  ProfilePageViewModel(get()) }
    viewModel {  GetLikesPostViewModel(get()) }

    viewModel {  GetFollowersViewModel(get()) }
    viewModel {  GetFollowingListViewModel(get()) }

    viewModel {  GetUserProfileViewModel(get()) }
    viewModel {  PostFavouriteProfileViewModel(get()) }
    viewModel {  GetFavouriteProfileViewModel(get()) }

    viewModel {  UserBlockViewModel(get()) }
    viewModel {  UserBlockListViewModel(get()) }
    viewModel {  SearchViewModel(get()) }
    viewModel {  UploadSongViewModel(get()) }
    viewModel {  GetVideoSearchViewModel(get()) }
    viewModel {  AddViewToPostViewModel(get()) }
    viewModel {  DeletePostViewModel(get()) }

    viewModel {  Get_HashTags_ViewModel(get()) }
    viewModel {  NotificatioViewModel(get()) }

    viewModel {  GetStaticPagesViewModel(get()) }
    viewModel { ReportViewModel(get()) }
    viewModel { GiftsViewModel(get()) }
    viewModel { SendGiftViewModel(get()) }
    viewModel { CompleteOrderViewModel(get()) }
    viewModel { GetAllSettingsViewModel(get()) }
    viewModel { ReportCommentViewModel(get()) }
    viewModel { RedeemCoinsViewModel(get()) }

    viewModel { SettingsViewModel(get()) }
    viewModel { FeedsViewModel(get()) }
    viewModel { ForYouViewModel(get()) }
}