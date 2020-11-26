package com.vrockk.view.dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.MyViewPagerAdapter
import com.vrockk.adapter.SearchParentAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.base.BaseFragment
import com.vrockk.models.search_page.HashtagsItem
import com.vrockk.models.search_page.SearchPageModel
import com.vrockk.models.search_page.SliderItem
import com.vrockk.utils.Utils
import com.vrockk.view.home.HomeFragment
import com.vrockk.view.search.SearchMainActivity
import com.vrockk.viewmodels.viewmodels.CommonViewModel
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.layout_loader.*
import org.koin.android.viewmodel.ext.android.viewModel

class SearchFragment : BaseFragment() {
    lateinit var searchAdapter: SearchParentAdapter
    lateinit var viewPagerAdapter: MyViewPagerAdapter

    private var postsItemList = ArrayList<HashtagsItem>()
    private var sliderItemList = ArrayList<SliderItem>()

    private val commonViewModel by viewModel<CommonViewModel>()

    var pageCount: Int = 1

    // var totalPages:Int = 1
    var pagesFromApi: Double = 0.00

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initProgress(contentLoadingProgressBar)

        searchAdapter = SearchParentAdapter(context!!, postsItemList)
        rvSearch.layoutManager =
            LinearLayoutManager(activity!!, LinearLayoutManager.VERTICAL, false)
        rvSearch.adapter = searchAdapter

        viewPagerAdapter = MyViewPagerAdapter(activity!!, sliderItemList)
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager, true)

        addListeners()
        addScrollListeners()
        observerApi()
    }

    private fun addListeners() {
        btnSearch.setOnClickListener {
            startActivityForResult(Intent(activity!!, SearchMainActivity::class.java), 10)
            activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun observerApi() {
        commonViewModel.searchPageResponseLiveData().observe(activity!!, Observer<ApiResponse> { response ->
            this.searchData(response)
        })
    }

    override fun onTabSwitched() {
        if (postsItemList.size == 0) {
            if (isLoggedIn())
                commonViewModel.getSearchPage(VrockkApplication.user_obj?._id!!, pageCount, 15)
            else
                commonViewModel.getSearchPage("", pageCount, 15)
        }
    }

    private fun searchData(response: ApiResponse?) {
        when (response!!.status) {
            Status.LOADING -> {
                if (pageCount == 1)
                    showProgress()
            }
            Status.SUCCESS -> {
                hideProgress()
                renderResponse(response)
            }
            Status.ERROR -> {
                hideProgress()
            }
        }
    }

    private fun renderResponse(response: ApiResponse) {
        val data: String = Utils.toJson(response.data)
        val gson1 = Gson();
        val model = gson1.fromJson(data, SearchPageModel::class.java)
        if (model.success!!) {

            sliderItemList.clear()
            sliderItemList.addAll(model.data?.slider!!)
            viewPagerAdapter.notifyDataSetChanged()

            if (postsItemList.size == 0) {
                postsItemList.clear()
                postsItemList.addAll(model?.data?.hashtags!!)
                searchAdapter.notifyDataSetChanged()
            } else {
                postsItemList.addAll(model?.data?.hashtags!!)
                //hashTagsListTemp.addAll(hashTagsList)
                searchAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 10) {
            var data = data?.getStringExtra("id")
            DashboardActivity.classType = "other_profile"
            HomeFragment.otherUserProfileId = data!!
            (context as BaseActivity).navigateFinishAffinity(DashboardActivity::class.java)
        }
    }


    private fun addScrollListeners() {
        nestedScrollview.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            val nestedScrollView = checkNotNull(v) {
                return@setOnScrollChangeListener
            }
            val lastChild = nestedScrollView.getChildAt(nestedScrollView.childCount - 1)
            if (lastChild != null) {
                if ((scrollY >= (lastChild.measuredHeight - nestedScrollView.measuredHeight)) && scrollY > oldScrollY) {
                    pageCount++

                    if (VrockkApplication.user_obj != null)
                        commonViewModel.getSearchPage(
                            VrockkApplication.user_obj?._id!!,
                            pageCount,
                            15
                        )
                    else
                        commonViewModel.getSearchPage("", pageCount, 15)
                }
            }
        }
    }

}