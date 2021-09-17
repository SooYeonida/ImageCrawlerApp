package com.yoni.imagecrawlerapp

import android.content.Context
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object NetworkDialog {
    lateinit var dialog : AlertDialog.Builder

    fun initDialog(context: Context){
        dialog = AlertDialog.Builder(context)
        dialog.setTitle("네트워크 문제")
        dialog.setMessage("네트워크 문제로 데이터를 받아올 수 없습니다. 재시도 하시겠습니까?")
        val listener = DialogInterface()
        dialog.setPositiveButton("재시도",listener)
        dialog.setNegativeButton("닫기", listener)
    }

    fun show(){
        CoroutineScope(Dispatchers.Main).launch {
            dialog.show()
        }
    }

    class DialogInterface :android.content.DialogInterface.OnClickListener {
        override fun onClick(p0: android.content.DialogInterface?, p1: Int) {
            when (p1) {
                android.content.DialogInterface.BUTTON_POSITIVE -> {
                    ImageUrlParser.parseImageUrl()
                }
            }
        }
    }
}
