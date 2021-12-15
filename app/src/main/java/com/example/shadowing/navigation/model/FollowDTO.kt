package com.example.shadowing.navigation.model

// 데이터 클래스란?
// toString(), hashCode(), equals(), copy() 메소드를 자동으로 만들어주는 클래스
// FollowDTO의 클래스 안에 있는 변수를 쉽게 사용할 수 있도록?
data class FollowDTO(
    var followerCount : Int = 0,
    var followers : MutableMap<String,Boolean> = HashMap(),

    var followingCount : Int = 0,
    var followings : MutableMap<String,Boolean> = HashMap()
)