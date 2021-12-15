package com.example.shadowing

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.example.shadowing.databinding.DialogTestBinding
import com.example.shadowing.navigation.AddPhotoActivity


class MyCustomDialog(context: Context): Dialog(context) {

    lateinit var dialogTestBinding: DialogTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        val dialogTestBinding = DialogTestBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        setContentView(dialogTestBinding.root)

        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setCanceledOnTouchOutside(false)


        dialogTestBinding.picturebtn.setOnClickListener {
            val intent = Intent(context, AddPhotoActivity::class.java)
            context.startActivity(intent)

            dismiss()
        }

    }

}