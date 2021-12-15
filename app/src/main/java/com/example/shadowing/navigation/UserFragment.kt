package com.example.shadowing.navigation


import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.shadowing.LoginActivity
import com.example.shadowing.MainActivity
import com.example.shadowing.R
import com.example.shadowing.models.User
import com.example.shadowing.navigation.model.AlarmDTO
import com.example.shadowing.navigation.model.ContentDTO
import com.example.shadowing.navigation.model.FollowDTO
import com.example.shadowing.navigation.util.FcmPush
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.*
import kotlinx.android.synthetic.main.fragment_user.view.*


class UserFragment : Fragment() {
    var reference: DatabaseReference? = null

    var intent: Intent? = null

    var fragmentView: View? = null
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null
    var currentUserUid: String? = null


    companion object {
        var PICK_PROFILE_FROM_ALBUM = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //R.layout.frament_user 화면
        fragmentView =
            LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)
        // mainactivity에서 bundle에 putString으로 넣어놓은 값을 가져오기
        uid = arguments?.getString("destinationUid")
        // 파이어베이스
        firestore = FirebaseFirestore.getInstance()

        //간단히 생각해 데이터 값 참조 하면 됌
        // 일단 text 변경 성공  왜 된건지는 모르겠음;;




//        reference = FirebaseDatabase.getInstance().getReference("Users")
//
//        val user: User? = getValue(User::class.java)
//
        val rootRef = FirebaseDatabase.getInstance().reference
        val usersRef = rootRef.child("Users")

        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (ds in dataSnapshot.children) {
                    val user: User? = ds.getValue(User::class.java)

                    account_username.text = user?.username.toString()
                    Log.d(
                        "result",
                        "User name: " + user?.username.toString()
                    )
//                    fragmentView?.account_username?.text = user?.username?.toString()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, databaseError.message) //Don't ignore errors!
            }
        }
        usersRef.addListenerForSingleValueEvent(valueEventListener)

        currentUserUid = auth?.currentUser?.uid

        // uid -> bundle을 통해서 전달 받은 값, currnetUserUid -> 현재 유저
        // 사실상 무조건 참
        if (uid == currentUserUid) {
            //MyPage
                // 텍스트 변경
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
            fragmentView?.account_btn_follow_signout?.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }
        } else {
            //OtherUserPage
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
            var mainactivity = (activity as MainActivity)

        }

//        reference!!.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val user: User? = dataSnapshot.getValue(User::class.java)
//                fragmentView?.account_username?.text = user?.username
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })


        //btn Follow_signout버튼 클릭시?
        fragmentView?.account_btn_follow_signout?.setOnClickListener {
            requestFollow()
        }

        fragmentView?.account_reyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_reyclerview?.layoutManager = GridLayoutManager(requireActivity(), 3)


        fragmentView?.account_iv_profile?.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }
        getProfileImage()
        getFollowerAndFollowing()
        return fragmentView
    }

    fun getFollowerAndFollowing() {
        firestore?.collection("users")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot == null) return@addSnapshotListener
                var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
                if (followDTO?.followingCount != null) {
                    fragmentView?.account_tv_following_count?.text =
                        followDTO?.followingCount?.toString()
                }
                if (followDTO?.followerCount != null) {
                    fragmentView?.account_tv_follower_count?.text =
                        followDTO?.followerCount?.toString()
                    if (followDTO?.followers?.containsKey(currentUserUid!!)) {
                        fragmentView?.account_btn_follow_signout?.text =
                            getString(R.string.follow_cancel)
                        fragmentView?.account_btn_follow_signout?.background
                            ?.setColorFilter(
                                ContextCompat.getColor(requireActivity(), R.color.colorLightGray),
                                PorterDuff.Mode.MULTIPLY
                            )
                    } else {
                        if (uid != currentUserUid) {
                            fragmentView?.account_btn_follow_signout?.text =
                                getString(R.string.follow)
                            fragmentView?.account_btn_follow_signout?.background?.colorFilter = null
                        }

                    }
                }
            }
    }

    fun requestFollow() {
        //Save data to my account
        // 변수 tsDocFollowing 안에 현재 유저 users라는 문서에 파이어베이스 스토리지 참조
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction { transaction ->
            // 코틀린에서 !! 연산자의 의미는 Null Pointer Exception이 필요한 경우에 사용할 수 잇다는 의미
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followings[uid!!] = true

                transaction.set(tsDocFollowing, followDTO!!)
                return@runTransaction
            }

            // 팔로우 관련 코드
            if (followDTO.followings.containsKey(uid)) {
                //It remove following third person when a third person follow me
                followDTO?.followingCount = followDTO?.followingCount - 1
                followDTO?.followings.remove(uid)
            } else {
                //It add following third person when a third person do not follow me
                followDTO?.followingCount = followDTO?.followingCount + 1
                followDTO?.followings[uid!!] = true
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }

        //Save data to third account

        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)
                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }

            if (followDTO!!.followers.containsKey(currentUserUid!!)) {
                //It cancel my follower when I follow a third person
                followDTO!!.followerCount = followDTO!!.followerCount - 1
                followDTO!!.followers.remove(currentUserUid!!)
            } else {
                //It add my follower when I don't follow a third person
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO!!.followers[currentUserUid!!] = true
                followerAlarm(uid!!)
            }
            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction
        }
    }

    //알림 발생
    fun followerAlarm(destinationUid: String) {
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        //유저 이름으로 바꿀 예정
        alarmDTO.userId = auth?.currentUser?.email
        alarmDTO.uid = auth?.currentUser?.uid
        alarmDTO.kind = 2
        alarmDTO.timestamp = System.currentTimeMillis()
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

        var message = auth?.currentUser?.email + getString(R.string.alarm_follow)
        //Okhttp를 이용하여 알림 푸시
        FcmPush.instance.sendMessage(destinationUid, "테스터최강성", message)
    }

    fun getProfileImage() {
        firestore?.collection("profileImages")?.document(uid!!)
            ?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                if (documentSnapshot == null) return@addSnapshotListener
                if (documentSnapshot.data != null) {
                    var url = documentSnapshot?.data!!["image"]
                    // 프로필 화면 바꾸기
                    Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop())
                        .into(fragmentView?.account_iv_profile!!)
                }
            }
    }

    // RecyclerView
    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()

        init {
            firestore?.collection("images")?.whereEqualTo("uid", uid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    //Sometimes, This code return null of querySnapshot when it signout
                    if (querySnapshot == null) return@addSnapshotListener

                    //Get data
                    for (snapshot in querySnapshot.documents) {
                        contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    }
                    fragmentView?.account_tv_post_count?.text = contentDTOs.size.toString()
                    notifyDataSetChanged()
                }
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3

            var imageview = ImageView(p0.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageview)
        }

        inner class CustomViewHolder(var imageview: ImageView) :
            RecyclerView.ViewHolder(imageview) {

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
            var imageview = (p0 as CustomViewHolder).imageview
            Glide.with(p0.itemView.context).load(contentDTOs[p1].imageUrl)
                .apply(RequestOptions().centerCrop()).into(imageview)
        }

    }
}