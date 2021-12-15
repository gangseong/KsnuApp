package com.example.shadowing

// 안드로이드 4대 컴포넌트 (Activity, Service, Broadcast Receiver, Content Provider)
// 이 컴포넌트들을 앱에서 사용하려면 매니페스트 파일에 등록해야함

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.shadowing.navigation.*
import com.google.android.gms.tasks.Task

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.item_detail.*
import kotlinx.android.synthetic.main.item_detail.view.*
import kotlinx.android.synthetic.main.user_item.*


//바텀네비게이션 -> 프래그먼트에서 불러와 onNavigationItemSelectedListener를 설정
class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    // 파이어베이스 유저
    var auth: FirebaseAuth? = null
    // 파이어베이스 데이터베이스
    var reference: DatabaseReference? = null


    // 바텀네비게이션
    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when(p0.itemId){
            // 네비게이션 id action_account 클릭시
            R.id.action_account ->{

                // 코틀린 파일 UserFragment
                var userFragment = UserFragment()
                //액티비티에서 프래그먼트에 데이터를 전달해주기 위해 사용
                var bundle = Bundle()
                //현재 로그인 유저
                var uid = FirebaseAuth.getInstance().currentUser?.uid

                //프래그먼트에 데이터 전달
                bundle.putString("destinationUid",uid)

                //arguments에 destinatnionUid에 Uid값을 넘긴다
                userFragment.arguments = bundle

                //FragmentManager는 앱 프래그먼트에서 작업을 추가, 삭제 또는 교체하고 백 스택에 추가하는 등의 작업을 실행하는 클래스입니다.
                supportFragmentManager.beginTransaction().replace(R.id.main_content,userFragment).commit()
                // listener를 호출 가능하게 한다.
                return true
            }

            R.id.action_home ->{
                var detailViewFragment = DetailViewFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,detailViewFragment).commit()
                return true
            }

            R.id.action_search ->{
                var gridFragment = GridFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,gridFragment).commit()
                return true
            }

            R.id.action_add_photo ->{

                if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    startActivity(Intent(this,AddPhotoActivity::class.java))
                }
                return true
            }
            R.id.action_favorite_alarm ->{
                var alarmFragment = AlarmFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main_content,alarmFragment).commit()
                return true
            }
        }
        return false
    }

    fun registerPushToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
                task ->
            val token = task.result
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            val map = mutableMapOf<String,Any>()
            map["pushToken"] = token!!

            FirebaseFirestore.getInstance().collection("pushtokens").document(uid!!).set(map)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottom_navigation.setOnNavigationItemSelectedListener(this)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)

        //Set default screen
        bottom_navigation.selectedItemId = R.id.action_home
        registerPushToken()
        auth = FirebaseAuth.getInstance()

        // 사진 등록 안한 사람만 뜨게
//        if(account_iv_profile == null) {
//            val myCustomDialog = MyCustomDialog(this)
//            myCustomDialog.show()
//        }


    }

//    override fun onStop() {
//        super.onStop()
//        FcmPush.instance.sendMessage("cUNXx5WcdyUEqAbcuESBZlWjl9v1","hi","bye")
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == UserFragment.PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK){
            var imageUri = data?.data
            var uid = FirebaseAuth.getInstance().currentUser?.uid
            var storageRef = FirebaseStorage.getInstance().reference.child("userProfileImages").child(uid!!)
            storageRef.putFile(imageUri!!).continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
                return@continueWithTask storageRef.downloadUrl
            }.addOnSuccessListener { uri ->
                var map = HashMap<String,Any>()
                map["image"] = uri.toString()
                FirebaseFirestore.getInstance().collection("profileImages").document(uid).set(map)
            }
        }
    }
}
