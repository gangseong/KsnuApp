package com.example.shadowing

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.shadowing.databinding.ActivitySplashBinding
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // animatin1에 AnimationUtils.loadAnimation() 함수를 사용하여 anim_text XML파일 객체화?
        val animation1 = AnimationUtils.loadAnimation(this, R.anim.anim_test)

        val animation2 = AnimationUtils.loadAnimation(this, R.anim.anim_test2)

        val animation3 = AnimationUtils.loadAnimation(this, R.anim.anim_test3)


        //splash_test에 animation1 실행
        splash_text.startAnimation(animation1)

        splash_text2.startAnimation(animation2)

        splash_text3.startAnimation(animation3)

        // Android에서 UI 작업은 별도의 스레드가 아닌 메인 스레드에서 작업해야 한다.
        //로직상, 다른 스레드에서 UI 처리를 해야 한다면 해당 스레드와 메인 스레드를 연결해주는 Handler를 사용해야한다
        // delay를 주는 코드
        // 핸들러는 루퍼를 통해
        //메인 쓰레드의 메인 루퍼를 통해 딜레이 실행
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, DURATION)

    }

    /* 클래스 인스턴스 없이 어떤 클래스 내부에 접근하고 싶다면, 클래스 내부에 객체를 선언할 때
    companion 식별자를 붙인 object를 선언하면 된다.
    다른 클래스에서는 안되는 듯?
    비슷하나 즉, 객체를 소프트웨어에 실체화 하면 그것을 ‘인스턴스’라고 부른다
    */
    companion object {
        private var DURATION: Long = 2000
    }

    // LoginActivity 화면에서 뒤로 가기 해도 SplashActivity로는 못 가게 처리.
    override fun onBackPressed() {
        super.onBackPressed()
    }
}