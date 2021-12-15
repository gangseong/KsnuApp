package com.example.shadowing

import android.content.Context
import android.content.Intent

//SharedPreferences 란?
// 앱의 데이터를 저장할 때, 간단한 데이터를 서버나 DB에 저장하기 부담스러울 때, SharedPreferences 사용
// 주로 자동 로그인, Application에 파일 형태로 데이터를 저장, Application이 삭제되기 까지 데이터 보존, Key-value 방식
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shadowing.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*

class LoginActivity : AppCompatActivity() {

    //gradle(app 부분에) buildFeatures{ viewBinding = true} 해야 동작
    // 뷰 바인딩은 레이아웃xml 파일에 선언한 뷰 객체를 코드에서 쉽게 이용하는 방법
    // findViewById() 함수를 일일이 가져오지 않아도 사용가능
    // 레이아웃 XML 파일에 등록된 뷰 객체를 포함하는 클래스가 자동으로 생성
    lateinit var binding: ActivityLoginBinding

    // lateinit 늦은 초기화 var 키워드를 사용해야됨, primitive type에 적용 불가 primitive type은 int, Boolean,double등
    lateinit var auth: FirebaseAuth

    lateinit var email: String
    lateinit var password: String

    lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 자동으로 만들어진 클래스의 inflate() 함수를 호출하면 바인딩 객체를 얻을 수 있다.
        /*인자로 layoutInflater를 전달, 바인딩 객체의 root 프로퍼티에는 XML의 루트 태그 객체가 자동으로 등록되므로
        액티비티 화면 출력은 setContentView() 함수에 binding.root를 전달하면 됩니다.
         */
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // sharedPrefs로  이메일과 비밀번호 데이터 값 저장하기
        // MODE_PRIVATE : 생성한 Application에서만 사용 가능하다.
        // MODE_WORLD_READABLE : 외부 App에서 사용 가능, But 읽기만 가능
        // MODE_WORLD_WRITEABLE : 외부 App에서 사용 가능, 읽기/쓰기 가능
        sharedPrefs = getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

//      val editor = sharedPrefs.edit()

        // 변수 data1에 sharedPrefs에서 id 값 가져오기 , 없으면 Value에 null
        val data1 = sharedPrefs.getString("id", null)
        // 변수 data1에 sharedPrefs에서 pwd 값 가져오기
        val data2 = sharedPrefs.getString("pwd", null)

        //data1 이 널 값이 아니거나 data2가 널 값이 아닐 때
        if (data1 != null && data2 != null) {

            //email에 data1 값 집어넣기,password에 data2 값 집어넣기
            email = data1
            password = data2

            //로그인 로직
            // 파이어베이스 signInWithEmailAndPassword함수에 매개변수로 email, password 값 집어넣어서 로그인 하기
            auth.signInWithEmailAndPassword(email, password)

                // 성공시 태스크에 가서
                .addOnCompleteListener(this) { task ->
                    //emailtext ,passwordtext 정리
                    binding.emailtext.text.clear()
                    binding.passwordtext.text.clear()

                    // 태스크 동작 성공 시 화면 이동
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        //finish() 넣어야 할까?
                    }
                }
        }

        // 로그인 버튼 클릭시 동작
        binding.mainloginbtn.setOnClickListener {
            //emailtext에 값을 email 넣기, passwordtext값을 password 변수에 넣기
            email = binding.emailtext.text.toString() + "@kunsan.ac.kr"
            password = binding.passwordtext.text.toString()
            //로그인
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.emailtext.text.clear()
                    binding.passwordtext.text.clear()
                    if (task.isSuccessful) {
                        //이메일 전송 확인 함수
                        if (checkAuth()) {
                            // 자동 로그인 체크 상태 확인 true -> 실행
                            if (binding.radioButton.isChecked) {
                                //save 함수 실행
                                save()
                            }
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            //발송된 메일로 인증 확인을 안한경우...........
                            Toast.makeText(
                                baseContext, "전송된 메일로 이메일 인증이 되지 않았습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        //로그인 실패 시
                    } else {
                        Toast.makeText(
                            baseContext, "로그인 실패",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        //앗, 처음이신가요 text 클릭시 동작
        binding.text22.setOnClickListener {

            // AuthActivity로 이동
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            //finish() 넣어야 할까?
        }

    }
    // 자동 로그인을 위한 함수
    private fun save() {

        //sharedPrefs에 email, password 값 저장해두기
        sharedPrefs.edit().run {
            putString("id", email)
            putString("pwd", password)
            //커밋!
            commit()
            Toast.makeText(this@LoginActivity, "저장 완료", Toast.LENGTH_SHORT).show()
        }
    }
    // 현재 로그인 유저가 이메일 전송 체크했는지 확인하는 함수
    //checkAuth() : Boolean?
    fun checkAuth(): Boolean {
        // currentUser = 파이어베이스 현재 로그인 유저
        val currentUser = auth.currentUser
        // return 값 현재 유저 맞는지?
        return currentUser?.let {
            //맞으면 이메일 확인 했는지
            if (currentUser.isEmailVerified) {
                true
            } else {
                false
            }
        } ?: let {
            false
        }
    }
}