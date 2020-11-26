package com.vrockk.view.settings

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.widget.CheckBox
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.facebook.login.LoginManager
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.SettingsAdapter
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.interfaces.ItemClickListener
import com.vrockk.models.settings.SettingsModel
import com.vrockk.utils.Constant
import com.vrockk.utils.PreferenceHelper
import com.vrockk.view.following.BlockUserListActivity
import com.vrockk.view.login.LoginActivity
import com.vrockk.viewmodels.viewmodels.SettingsViewModel
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.my_custom_alert.*
import org.koin.android.viewmodel.ext.android.viewModel

class SettingsActivity : BaseActivity(), ItemClickListener {

    val settingsViewModel by viewModel<SettingsViewModel>()
    lateinit var settingsAdapter: SettingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        handleClickEvents()
        setUpSettingsRv()

        tvLogout.setOnClickListener {
            showLogoutPopup()
        }

        settingsViewModel.accountDisableDeleteResponse().observe(this, Observer {
            when (it.status) {
                Status.LOADING -> {
                    showProgress("")
                }
                Status.SUCCESS -> {
                    hideProgress()
                    clearPrefs()
                    startActivity(Intent(this@SettingsActivity, LoginActivity::class.java))
                    finishAffinity()

                    try {
                        LoginManager.getInstance().logOut();
                    } catch (e: Exception) {

                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    showSnackbar(it.error?.localizedMessage ?: "")
                }
            }
        })
    }

    private fun handleClickEvents() {
        tvBack.setOnClickListener {
            finish()
        }
    }

    private fun setUpSettingsRv() {
        val settingsModels: ArrayList<SettingsModel> = ArrayList()
        settingsModels.add(SettingsModel(getString(R.string.refertofriend), Constant.SETTINGS_REFER_FRIEND))
        settingsModels.add(SettingsModel(getString(R.string.rewardpoints), Constant.SETTINGS_REWARD_POINTS))
        settingsModels.add(SettingsModel(getString(R.string.block_list), Constant.SETTINGS_BLOCK_LIST))
        settingsModels.add(SettingsModel(getString(R.string.notification), Constant.SETTINGS_NOTIFICATIONS))

        if (!isSocialLogIn()) {
            settingsModels.add(SettingsModel(getString(R.string.changepassword), Constant.SETTINGS_CHANGE_PASSWORD))
        }

        settingsModels.add(SettingsModel(getString(R.string.contactus), Constant.SETTINGS_CONTACT_US))
        settingsModels.add(SettingsModel(getString(R.string.termsandcondition), Constant.SETTINGS_TERMS_POLICIES))
        settingsModels.add(SettingsModel(getString(R.string.privacypolicy), Constant.SETTINGS_PRIVACY_POLICY))
        settingsModels.add(SettingsModel(getString(R.string.aboutapp), Constant.SETTINGS_ABOUT_APP))

        if (isLoggedIn()) {
            settingsModels.add(SettingsModel(getString(R.string.disable_account), Constant.SETTINGS_DISABLE_ACCOUNT))
            settingsModels.add(SettingsModel(getString(R.string.delete_account), Constant.SETTINGS_DELETE_ACCOUNT))
        }

        rvSettings.setHasFixedSize(true)
        rvSettings.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        settingsAdapter = SettingsAdapter(this, settingsModels, this)
        rvSettings.adapter = settingsAdapter

    }

    override fun onItemClicked(position: Int) {
        when (settingsAdapter.getSettingsType(position)) {
            Constant.SETTINGS_REFER_FRIEND -> navigate(ReferToFriendActivity::class.java)
            Constant.SETTINGS_REWARD_POINTS -> navigate(RewardActivity::class.java)
            Constant.SETTINGS_BLOCK_LIST -> navigate(BlockUserListActivity::class.java)
            Constant.SETTINGS_NOTIFICATIONS -> navigate(NotificationActivity::class.java)

            Constant.SETTINGS_CHANGE_PASSWORD -> navigate(ChangePasswordActivity::class.java)

            Constant.SETTINGS_CONTACT_US -> navigate(ContactUsActivity::class.java)
            Constant.SETTINGS_TERMS_POLICIES -> {
                val i = Intent(this, Terms_Privacy_About_Activity::class.java)
                i.putExtra("type", "terms-and-conditions")
                startActivity(i)
            }
            Constant.SETTINGS_PRIVACY_POLICY -> {
                val i = Intent(this, Terms_Privacy_About_Activity::class.java)
                i.putExtra("type", "privacy-policy")
                startActivity(i)
            }
            Constant.SETTINGS_ABOUT_APP -> {
                val i = Intent(this, Terms_Privacy_About_Activity::class.java)
                i.putExtra("type", "about-us")
                startActivity(i)
            }
            Constant.SETTINGS_DISABLE_ACCOUNT -> showDisableDeletePopup(true)
            Constant.SETTINGS_DELETE_ACCOUNT -> showDisableDeletePopup(false)
        }
    }

    private fun showDisableDeletePopup(disablePopup: Boolean) {
        val cbDeleteContents = LayoutInflater.from(this).inflate(R.layout.disable_delete_checkbox, null) as CheckBox
        cbDeleteContents.isChecked = false

        val myCustomDlg = Dialog(this)
        myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        myCustomDlg.setContentView(R.layout.my_custom_alert)
        myCustomDlg.tvTitleCustom.text = resources.getString(R.string.confirm)
        myCustomDlg.tvAlertMessage.text = getString(
            when (disablePopup) {
                true -> R.string.are_you_sure_you_want_to_disable_your_account
                false -> R.string.are_you_sure_you_want_to_delete_your_account
            }
        )
        myCustomDlg.positiveBtn.text = getString(R.string.yes)
        myCustomDlg.noBtn.text = resources.getString(R.string.close)

        if (!disablePopup){
            myCustomDlg.extraContainer.addView(cbDeleteContents)
        }
        myCustomDlg.show()

        myCustomDlg.noBtn.setOnClickListener {
            myCustomDlg.dismiss()
        }

        myCustomDlg.positiveBtn.setOnClickListener {
            myCustomDlg.dismiss()
            settingsViewModel.disableDeleteAccount("SEC "+ VrockkApplication.user_obj!!.authToken,
                !disablePopup, cbDeleteContents.isChecked)
        }
    }

    private fun showLogoutPopup() {
        val myCustomDlg = Dialog(this)
        myCustomDlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        myCustomDlg.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        myCustomDlg.setContentView(R.layout.my_custom_alert)
        myCustomDlg.tvTitleCustom.text = resources.getString(R.string.confirm)
        myCustomDlg.tvAlertMessage.text = resources.getString(R.string.are_you_sure_you_want_to_logout)
        myCustomDlg.positiveBtn.text = resources.getString(R.string.logout)
        myCustomDlg.noBtn.text = resources.getString(R.string.close)
        myCustomDlg.show()

        myCustomDlg.noBtn.setOnClickListener {
            myCustomDlg.dismiss()
        }
        myCustomDlg.positiveBtn.setOnClickListener {
            myCustomDlg.dismiss()
            val editor = VrockkApplication.prefs.edit()

            clearPrefs()

            startActivity(Intent(this@SettingsActivity, LoginActivity::class.java))
            finishAffinity()

            try {
                LoginManager.getInstance().logOut();
            } catch (e: Exception) {

            }
        }
    }

    private fun clearPrefs() {
        val editor = VrockkApplication.prefs.edit()

        val sharedPreference = PreferenceHelper.defaultPrefs(this@SettingsActivity)
        val deviceToken = sharedPreference.getString(PreferenceHelper.Key.FCMTOKEN, "")
        editor!!.remove(PreferenceHelper.Key.REGISTEREDUSER)
        editor.putString(PreferenceHelper.Key.FCMTOKEN, deviceToken)
        editor.apply()
        VrockkApplication.user_obj = null
    }
}