package com.yoni.imagecrawlerapp.Network

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.yoni.imagecrawlerapp.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NetworkDialog {
    lateinit var mContext: Context
    lateinit var mDialog : AlertDialog.Builder

    fun initDialog(context: Context){
        mContext = context
        mDialog = AlertDialog.Builder(context)
        mDialog.setTitle("네트워크 문제")
        mDialog.setMessage("네트워크 문제로 데이터를 받아올 수 없습니다. 재시도 하시겠습니까?")
        val listener = DialogInterface()
        mDialog.setPositiveButton("재시도",listener)
        mDialog.setNegativeButton("닫기", listener)
    }

    fun show(){
        CoroutineScope(Dispatchers.Main).launch {
            mDialog.show()
        }
    }

    class DialogInterface :android.content.DialogInterface.OnClickListener {
        override fun onClick(p0: android.content.DialogInterface?, p1: Int) {
            when (p1) {
                android.content.DialogInterface.BUTTON_POSITIVE -> {
                    //재시도
                    (mContext as MainActivity).loadImage()
                }
            }
        }
    }
}
