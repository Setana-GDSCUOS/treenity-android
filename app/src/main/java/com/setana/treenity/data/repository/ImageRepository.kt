package com.setana.treenity.data.repository

import com.setana.treenity.data.api.ImageApiService
import javax.inject.Inject

class ImageRepository
@Inject
constructor(private var api: ImageApiService) {

    suspend fun getAllImages() = api.getAllImages()

}

// 아직 잘 모르겠는 부분..ㅜ
// 깜빡했는데 Repository는 Interface로 선언하고 ~RepositoryImpl 이라는 구현체 클래스를 따로 만들어주세요.
// Module 내용도 바뀌어야겠죠? :)