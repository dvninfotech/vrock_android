package com.vrockk.view.search

import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.vrockk.R
import com.vrockk.adapter.ViewPagerAdapter
import com.vrockk.base.BaseActivity
import com.vrockk.interfaces.AppTextWatcher
import com.vrockk.utils.Constant.Companion.ALL
import com.vrockk.utils.Constant.Companion.HASHTAGS
import com.vrockk.utils.Constant.Companion.POST
import com.vrockk.utils.Constant.Companion.SONG
import com.vrockk.utils.Constant.Companion.USER
import com.vrockk.viewmodels.SearchViewModel
import kotlinx.android.synthetic.main.activity_search_main.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class SearchMainActivity : BaseActivity() {
    companion object {
        const val TAG = "SearchMainActivity"

        val SEARCH_TYPING_OFFSET = TimeUnit.MILLISECONDS.toMillis(1000)
        const val ITEMS_PER_PAGE = 50
    }

    val searchViewModel by viewModel<SearchViewModel>()

    private val fragments = ArrayList<Fragment>()
    private val titles = ArrayList<String>()
    private lateinit var viewPagerAdapter: ViewPagerAdapter

    private lateinit var fragmentAll: SearchAllTabFragment
    private lateinit var fragmentPosts: SearchOtherTabFragment
    private lateinit var fragmentSongs: SearchOtherTabFragment
    private lateinit var fragmentTags: SearchOtherTabFragment
    private lateinit var fragmentUsers: SearchOtherTabFragment

    var searchString: String = ""
    private val runnable: () -> Unit = {
        fragmentAll.requestSearch(searchString)
        fragmentPosts.requestSearch(searchString)
        fragmentSongs.requestSearch(searchString)
        fragmentTags.requestSearch(searchString)
        fragmentUsers.requestSearch(searchString)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_main)

        fragmentAll = SearchAllTabFragment()

        fragmentPosts = SearchOtherTabFragment(POST)
        fragmentSongs = SearchOtherTabFragment(SONG)
        fragmentTags = SearchOtherTabFragment(HASHTAGS)
        fragmentUsers = SearchOtherTabFragment(USER)

        fragments.add(fragmentAll)
        titles.add(ALL)
        fragments.add(fragmentUsers)
        titles.add(USER)
        fragments.add(fragmentSongs)
        titles.add(SONG)
        fragments.add(fragmentPosts)
        titles.add(POST)
        fragments.add(fragmentTags)
        titles.add(HASHTAGS)

        viewPager.offscreenPageLimit = 5
        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, fragments, titles)
        viewPager.adapter = viewPagerAdapter

        tabLayout.setupWithViewPager(viewPager)

        addListeners()
    }

    private fun addListeners() {
        searchViewModel.errorLiveData.observe(this, Observer {
            showToast(it.peekContent())
        })

        etSearch.addTextChangedListener(object : AppTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                val search = s.toString()

                if (search.isNotEmpty()) {
                    if (search.trim().isNotEmpty()) {
                        searchString = search.trim()
                        handler.removeCallbacks(runnable)
                        handler.postDelayed(runnable, SEARCH_TYPING_OFFSET)
                    } else {
                        etSearch.setText("")
                        buttonsLayout.visibility = View.GONE
                    }
                } else
                    buttonsLayout.visibility = View.GONE
            }
        })

        ibBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}