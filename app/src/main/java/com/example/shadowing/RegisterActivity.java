//패키지명 shadowing
package com.example.shadowing;

// 메서드나 변수에 null을 사용하면 경고를 표시?

import androidx.annotation.NonNull;

// AppCompatActivity는 안드로이드 하위버전을 지원하는 Activity의 일종
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

//※ 인텐트란
// 인텐트는 앱 컴포넌트가 무엇을 할 것인지를 담는 메시지 객체입니다. 메시지는 의사소통을 하기 위해 보내고 받는 것이지요.
// 메시지를 사용하는 가장 큰 목적은 다른 액티비티, 서비스, 브로드캐스트 리시버, 컨텐트 프로바이더 등을 실행하는 것입니다.
// 인텐트는 그들 사이에 데이터를 주고 받기 위한 용도로도 쓰입니다.
import android.content.Intent;

// Bundle은 여러가지의 타입의 값을 저장하는 Map 클래스
// Android에서는 Activity간에 데이터를 주고 받을 때 Bundle 클래스를 사용하여 데이터를 전송한다
import android.os.Bundle;

//Text를 위해 제공된 Util 주로 Text가 들어왔는 지 아닌 지를 체크할 수 있는데, isEmpty라는 메소드를 통해 문자열 null 체크를 할 수 있다.
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//gms -> google mobile service의 약자

//- Task는 어플리케이션에서 실행되는 액티비티를 보관하고 관리하며 Stack형태의 연속된 Activity로 이루어진다
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// HashMap이란 Map인터페이스의 한 종류로써 Key와 Value값으로 데이터를 제장하는 형태를 가지고 있다.
import java.util.HashMap;

//RegisterActivity에 AppCompatActivity 상속
public class RegisterActivity extends AppCompatActivity {

    //객체 생성
    EditText username, email, password;
    Button btn_register;
    FirebaseAuth auth;
    DatabaseReference reference;
    //재정의

    //onCreate(Bundle) -> onStart() -> onResume() -> onPause() -> onStop() -> onDestory()
    @Override
    //savedInstanceState 액티비티가 중단하게 되면 savedInstanceState메서드를 호출하여 데이터를 임시 저장한다.
    //다시 동작하게 되면 저장되었던 데이터를 가지고 다시 액티비티를 생성한다.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setContentView메소드에 원하는 레이아웃의 리소드ID를 전달하면 해당 레이아웃 출력
        setContentView(R.layout.activity_register);

        //findViewById로 객체에 뷰의 식별자(id)를 제공해줌
        username = findViewById(R.id.usernametext);
        email = findViewById(R.id.authEmail);
        password = findViewById(R.id.passwordEmail2);
        btn_register = findViewById(R.id.finalschoolcheckbtn);

        auth = FirebaseAuth.getInstance();

        //finalschoolcheckbtn 클릭시 동작
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //usernametext부분에 작성한 글을 (getText한다) txt_username에 담는다.
                String txt_username = username.getText().toString();
                //authEmail이라는 부분에 작성한 글에  + @kunsan.ac.kr 해서 txt_email에 담는다.
                String txt_email = email.getText().toString() + "@kunsan.ac.kr";
                //passwordEmail2이라는 부분에 작성한 글을 txt_password에 담는다.
                String txt_password = password.getText().toString();

                //txt_username이 공백이거나 txt_email이 공백, txt_password가 공백이면 토스트 알림 실행
                if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(RegisterActivity.this, "All filed are required", Toast.LENGTH_SHORT).show();

                } else if (txt_password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "6이상 써", Toast.LENGTH_SHORT).show();
                } else {
                    //register 매개변수
                    register(txt_username, txt_email, txt_password);
                }
            }
        });
    }

    private void register(String username, String email, String password) {
        //파이어베이스 유저 회원가입 (생성하기. 이메일과 패스워드).성공리스너
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //task에 넣고 성공시
                if (task.isSuccessful()) {
                    //현재 파이업베이스 현재 로그인 유저에게 확인 이메일 전송
                    auth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        //그 후 성공하면 실행
                        public void onComplete(@NonNull Task<Void> task) {
                            //토스트 실행
                            Toast.makeText(RegisterActivity.this,
                                    "회원가입 성공하셨습니다." +
                                            "전송된 메일을 확인해 주세요.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;
                    // userid = 파이어베이스 유저의 UId를 가져와서 넣기
                    String userid = firebaseUser.getUid();

                    //파이어베이스 데이터베이스에 Users라는 이름으로 문서 만들기?
                    //파이어베이스 문서 읽어 본 후에 파악 가능
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                    //hashmap이라는 변수에 데이터 저장
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userid);
                    hashMap.put("username", username);
                    hashMap.put("imageURL", "default");

                    //파이어베이스 데이터베이스에 해쉬데이터 넣기
                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //RegisterActivity에서 LoginActivity로 화면 전환
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                //Flag? 안드로이드 앱에서는 새로운 액티비티가 실행될 때마다 기존에 사용하던 액티비티는 스택에 쌓임
                                // 이를 Flag를 사용하여 제어할 수 있다.
                                //플래그 추가.  FLAG_ACTIVITY_CLEAR_TASK = 현재 태스크를 모두 비우는 플래그. FLAG_ACTIVITY_NEW_TASK= 기존에 플래그가 없다면 새로 만듬
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                //intent 실행
                                startActivity(intent);
                                //RegisterActivity 종료
                                finish();
                            }

                        }
                    });
                    // 파이어베이스 데이터베이스에 데이터 넣기 실패시 작동
                } else {
                    Toast.makeText(RegisterActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}