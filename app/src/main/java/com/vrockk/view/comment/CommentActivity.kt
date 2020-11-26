package com.vrockk.view.comment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.vrockk.R
import com.vrockk.VrockkApplication
import com.vrockk.adapter.CommentAdapter
import com.vrockk.api.ApiResponse
import com.vrockk.api.BASE_URL
import com.vrockk.api.Status
import com.vrockk.base.BaseActivity
import com.vrockk.models.comments.comment_post.CommentPostResponse
import com.vrockk.models.comments.get_comments.GetCommentsResponse
import com.vrockk.utils.Utils
import com.vrockk.view.duet.DuetMainActivity
import com.vrockk.viewmodels.CommentPostViewModel
import com.vrockk.viewmodels.GetCommentsViewModel
import com.vrockk.viewmodels.ReportViewModel
import com.vrockk.viewmodels.viewmodels.ReportCommentViewModel
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.activity_comment.tvTitle
import kotlinx.android.synthetic.main.activity_viewprofile.*
import kotlinx.android.synthetic.main.dialog_confirmation_report.*
import kotlinx.android.synthetic.main.dialog_report.*
import kotlinx.android.synthetic.main.dialog_share.*
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*


class CommentActivity : BaseActivity() {

    lateinit var commentAdapter: CommentAdapter

    val commentPostViewModel by viewModel<CommentPostViewModel>()

    val getCommentsViewModel by viewModel<GetCommentsViewModel>()

    private val reportViewModel by viewModel<ReportCommentViewModel>()

    var _id:String = ""

    var _token:String = ""

    var position:Int = 0

    var listSize : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _id = intent.getStringExtra("_id")?:""
        _token ="SEC "+ VrockkApplication.user_obj!!.authToken

        commentPostViewModel.commentPostResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(1,response)
        })

        getCommentsViewModel.getCommentsResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(2,response)
        })

        reportViewModel.reportResponse().observe(this, Observer<ApiResponse> { response ->
            this.dataResponse(3,response)
        })

        setContentView(R.layout.activity_comment)
        init()

        getChatHistory()

    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun dataResponse(type:Int , response: ApiResponse) {
        when (response.status) {
            Status.LOADING -> {
                showProgress("")
            }
            Status.SUCCESS -> {
                hideProgress()

                renderResponse(type,response)
            }
            Status.ERROR -> {
                hideProgress()
                Log.e("comment_error", Gson().toJson(response.error))
                //showCustomToastMessage(this, "Something went wrong!")
            }
        }
    }

    private fun renderResponse(type:Int ,response: ApiResponse) {
        if(response != null){

            if (type == 1)
            {
                Log.e("post_comment_response: ", Gson().toJson(response))
                val data: String = Utils.toJson(response.data)
                val gson1 = Gson();
                val postCommentResponse = gson1.fromJson(data, CommentPostResponse::class.java)

                if (postCommentResponse.success)
                {
//                  showSnackbar(""+postCommentResponse.message)
                    etComment.setText("")
                    getChatHistory()
                }
                else
                {
                    showSnackbar(""+postCommentResponse.message)
                }
            }
             if (type == 2)
            {
                Log.e("get comments response: ", Gson().toJson(response))
                val data: String = Utils.toJson(response.data)
                val gson1 = Gson();
                val commentResponse = gson1.fromJson(data, GetCommentsResponse::class.java)

                if (commentResponse.success) {
                    listSize = commentResponse.data.size
                    Collections.reverse(commentResponse.data)
                    commentAdapter = CommentAdapter(this,commentResponse.data)
                    rvComment.adapter = commentAdapter
                    rvComment.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
                    rvComment.scrollToPosition(commentResponse.data.size - 1);
                } else {
                    showSnackbar(""+resources.getString(R.string.report_send_succesfully))
                }
            }
            if (type == 3)
            {
                showSnackbar(""+resources.getString(R.string.report_send_succesfully))
            }

        }
    }

    override fun onBackPressed() {
        val ii = Intent()
        ii.putExtra("count",listSize)
        if(intent.hasExtra("position"))
            ii.putExtra("position",intent.getIntExtra("position",0))
        setResult(Activity.RESULT_OK,ii)
        finish()
    }

    fun init()
    {
//      hideStatusBar()
        ibBack.setOnClickListener {
            val ii = Intent()
            ii.putExtra("count",listSize)
            if(intent.hasExtra("position"))
                ii.putExtra("position",intent.getIntExtra("position",0))
            setResult(Activity.RESULT_OK,ii)
            finish()
        }

        ibSend.setOnClickListener {
            callPostApi()
        }

        etComment.addTextChangedListener(MyTextWatcher(etComment));

    }


    ////**** Text Watcher..

    class MyTextWatcher(editText: EditText): TextWatcher {
        private val editText:EditText
        init{
            this.editText = editText
        }
        override fun beforeTextChanged(s:CharSequence, start:Int, count:Int, after:Int) {
        }
        override fun onTextChanged(s:CharSequence, start:Int, before:Int, count:Int) {

            val text = editText.getText().toString()
            if (text.startsWith(" "))
            {
                editText.setText(text.trim({ it <= ' ' }))
            }

        }
        override fun afterTextChanged(s: Editable) {

        }
    }

    fun callPostApi()
    {
        if (etComment.text.toString().equals(""))
        {
            showSnackbar("Please add comment")
        }
        else
        {
            commentPostViewModel.commentPost(_token,_id,etComment.text.toString())
        }
    }


    fun getChatHistory()
    {
        getCommentsViewModel.getCommentsPost(_token,1,"100",_id)
    }

    fun showReportDialog(_id: String)
    {
        val dialog1 = Dialog(this)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setContentView(R.layout.dialog_report)
        dialog1.show()

        dialog1.tvSpam.setOnClickListener {

            showConfirmReportDialog(_id,"it's spam",dialog1)
            dialog1.dismiss()
        }

        dialog1.tvInformation.setOnClickListener {

            showConfirmReportDialog(_id,"False Information",dialog1)
            dialog1.dismiss()
        }


        dialog1.tvBullying.setOnClickListener {

            showConfirmReportDialog(_id,"Bullying and harassment",dialog1)
            dialog1.dismiss()
        }

        dialog1.tvJust.setOnClickListener {

            showConfirmReportDialog(_id,"I just don't Like",dialog1)
            dialog1.dismiss()
        }

        dialog1.tvInpropriate.setOnClickListener {
            showConfirmReportDialog(_id,"Inappropriate Content",dialog1)
            dialog1.dismiss()
        }

    }

    fun showConfirmReportDialog(
        _id: String,
        message: String,
        dialog: Dialog
    ) {
        val dialog1 = Dialog(this)
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog1.setContentView(R.layout.dialog_confirmation_report)
        dialog1.show()

        dialog1.tvTitle.text = ""+resources.getString(R.string.are_you_sure_you_want_to_report_this_comment)

        dialog1.tvNo.setOnClickListener {
            dialog1.dismiss()
        }

        dialog1.tvYes.setOnClickListener {

            reportViewModel.hitReport("SEC "+VrockkApplication.user_obj!!.authToken
                ,_id
                ,message
                ,"Comment")

            dialog1.dismiss()
            dialog.dismiss()

        }

    }


}