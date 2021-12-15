package com.example.shadowing.navigation.util

import com.example.shadowing.navigation.model.PushDTO

import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
// okhttp 라이브러리를 이용해서 서버에 post 요청 할 수 있다.
// REST API, HTTP 통신을 간편하게 구현할 수 있도록 다양한 기능을 제공해주는 java 라이브러리
// Retrofit은 Square라는 회사가 만든 OkHttp 라이브러리의 상위 구현체
import okhttp3.*
import java.io.IOException

// REST API란?
// REST 아키텍처의 제약조건을 준수하는 애플리케이션 프로그램밍 인터페이스
// 서버와 클라이언트 구조, Socket 통신과 다른게 양방향이 아닌 단방향 통신
// Request와 Response로 이루어짐

//파이어베이스 알람
class FcmPush {

    // MediaType은 파일이 어떤 형태인지 알려주는 역할
    // application/json은 유니코드-8로 작성?
    var JSON = MediaType.parse("application/json; charset=utf-8")
    // url 주소
    var url = "https://fcm.googleapis.com/fcm/send"
    // url에 사용되는 서버키
    var serverKey = "AAAAZ2RsRLA:APA91bH8ndpbeblAJcF66Ukz_KaVAaJo9OFS-8o4zfSDCEGBKlE9dh3edoXBxug_qT-r34eVBLyR7-8jemg4hfajgq1vi6QoxI28mycqQoZl2FWp1jMFIf8aA9c6etbKzxfrHyVQOBKh"

    var gson : Gson? = null
    var okHttpClient : OkHttpClient? = null

    companion object{
        var instance = FcmPush()
    }
//
    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }
    fun sendMessage(destinationUid : String, title : String, message : String){
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get().addOnCompleteListener {
                task ->
            if(task.isSuccessful){
                var token = task?.result?.get("pushToken").toString()

                var pushDTO = PushDTO()
                pushDTO.to = token
                pushDTO.notification.title = title
                pushDTO.notification.body = message

                var body = RequestBody.create(JSON,gson?.toJson(pushDTO))
                var request = Request.Builder()
                    .addHeader("Content-Type","application/json")
                    .addHeader("Authorization", "key=$serverKey")
                    .url(url)
                    .post(body)
                    .build()

                okHttpClient?.newCall(request)?.enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {

                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        println(response?.body()?.string())
                    }

                })
            }
        }
    }
}