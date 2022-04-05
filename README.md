# 🌲 Treenity (2022 Google Solution Challenge)<br>

## 📹 Demo Video
[![](http://yt-embed.herokuapp.com/embed?v=EMuaofVMWWk)](https://www.youtube.com/watch?v=EMuaofVMWWk)

This is an application that provides an AR-based social networking/exercising experience.

APK File
<a href="https://drive.google.com/file/d/10QIdxzayWGN6b9ZpV7ODKDwN9LhPcAOu/view?usp=sharing"><img src="https://img.shields.io/badge/download-4285F4?style=flat-square&logo=googledrive&logoColor=white"/></a>

## 📖 Explanation

 We propose a way to reduce your carbon footprint through fun-filled walking.
As users walk while using the app, the app records their steps.
The number of steps recorded is converted into points for buying seeds and buckets of water,
Users can plant seeds and water trees in places they frequently visit through AR technology and build up their efforts toward nature and health.
Not just this! Users can share their hard work with other users and also interact with the world directly from their Android smartphones!

By walking and watering, you will be filled with the power of steady exercise.

👣Let's just plant your walks!👣

## 💎 Main Features

- Realtime step-counting service

- Google ARCore CloudAnchor based spatial information sharing system

- Marking hosted ar trees of all users on Google Maps

- Push Notification of hosted ar trees lists sorted via distance

## 🖥️ Build Environment

This project uses the Gradle build system.
To build this project, please use the `gradlew build` command or use "Import Project" in Android Studio.

This project is built on : `Gradle 7.0.2` with `JDK 11`

- `minSdkVersion` : 29
- `targetSdkVersion` : 31

## 🏛️ Libraries Used

- ### Android Architecture Components

  - [Lifecycles](https://developer.android.com/topic/libraries/architecture/lifecycle) - Allows control and respond lifecycles of events.
  - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - Allows immediate response of view on changes of data to show.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Stores UI-related data that does not volatilize even when the screen is rotated.
  - [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) - Manage Android background jobs and schedule periodic works.

- ### Dependency Injection

  - [Hilt](https://dagger.dev/hilt) provides a standard way to incorporate Dagger dependency injection into an Android application.

- ### Asynchronous Programming

  - [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) are implementation of coroutine in Kotlin language.

- ### Network

  - [Retrofit 2](https://square.github.io/retrofit) is a type-safe HTTP client for Android and Java.
  - [OkHttp 3](https://square.github.io/okhttp) is an efficient HTTP client helps to request/response data with HTTP.

- ### Authentication

  - [Firebase](https://firebase.google.com) is Google's platform for mobile application development.

- ### AR

  - [ARCore API](https://developers.google.com/ar/reference) is Google’s platform for building augmented reality experiences.

- ### Google Maps

  - [Google Maps Platform](https://mapsplatform.google.com) is Google’s platform for map-related tasks.

- ### Third Party
  - [Coil](https://coil-kt.github.io/coil) is image loading library for Android backed by Kotlin Coroutines.
  - [Lottie](https://github.com/airbnb/lottie-android) is library that renders After Effects animations natively on Android.
  - [SceneView 0.2.0](https://github.com/SceneView/sceneview-android) is a 3D/AR Android View with ARCore and Google Filament.
  - [MPAndroidChart 3](https://github.com/PhilJay/MPAndroidChart) is a powerful & easy to use chart library for Android.

## 👷‍♂️ Install

Please download **APK file** <a href="https://drive.google.com/file/d/10QIdxzayWGN6b9ZpV7ODKDwN9LhPcAOu/view?usp=sharing"><img src="https://img.shields.io/badge/download-4285F4?style=flat-square&logo=googledrive&logoColor=white"/></a>

1. Select apk files on file manager<br>
![1_select_apk](https://user-images.githubusercontent.com/37829895/161111997-33edb3a6-6b80-4c04-a820-8552e09b30ce.jpg)

2. Go to settings to allow installing apps from unknown source.<br>
![2_allow_unknow_source](https://user-images.githubusercontent.com/37829895/161112001-9bf1f385-4088-4513-aa9c-9af92f22a847.jpg)

3. Allow your file manager application to install unknown apps<br>
![3_allow_install](https://user-images.githubusercontent.com/37829895/161112003-30d32802-6855-4ed7-8f91-18e309de8fea.jpg)

4. Tap install button<br>
![4_install](https://user-images.githubusercontent.com/37829895/161112004-b4a8e24b-e149-497e-9031-653ab5cf5197.jpg)

5. Select `INSTALL ANYWAY`<br>
![5_install_continue](https://user-images.githubusercontent.com/37829895/161112012-8749926b-343d-453a-b1fc-9b21f6538c61.jpg)


6. Please don't send app for play protect scanning!<br>
![5 5_Dont](https://user-images.githubusercontent.com/37829895/161112005-728c9541-f4ac-4e94-8e9b-3270373055cf.jpg)

7. You're Done!<br>
![6_finish](https://user-images.githubusercontent.com/37829895/161112010-d671fa8d-b8ca-43d5-957c-e025f1b2a229.jpg)

## 📸 Usage

#### 🍀 Space recognization

Application detects plane and **recognizes space** from video
<br>
![plane_detection](https://user-images.githubusercontent.com/37829895/161015860-3302d646-58d4-4fd7-b18e-78927bcada7c.gif)

#### 🍀 Planting tree

Just **select seed** and **touch the screen** to plant trees on the AR space
<br>
![planting](https://user-images.githubusercontent.com/37829895/161019749-4b2a2f99-7dc9-4f97-89f4-63c9971afb3f.gif)

#### 🍀 Enter Information

You can **add descriptions** for your own AR tree
<br>
![enter_information](https://user-images.githubusercontent.com/37829895/161016939-504a43e7-49b5-4780-8b0f-d83ee08a871c.gif)

#### 🍀 List trees

Check your trees on your **My Page** or **My Tree List**
<br>
![my_tree](https://user-images.githubusercontent.com/37829895/161017577-3a3b50fd-f830-45bd-bb31-520b369c0964.gif)

#### 🍀 Find trees of the other users

Use map pages to **find** or **bookmark** trees of other users
<br>
![map_with_bookmark](https://user-images.githubusercontent.com/37829895/161017975-183c0997-eef2-4c30-9edd-92e7051cbc2b.gif)

#### 🍀 Bring shared trees to your space

**Point your phone** to a specific place to bring another user's trees!
<br>
![resolve_shared_tree](https://user-images.githubusercontent.com/37829895/161018816-ec75695f-560b-4cda-91bf-a40490a47c7c.gif)

#### 🍀 Check your "Mypage"

The application will **count your steps and record** them **in real-time**
You can also check your walk records, the total amount of carbon you've reduced in **Mypage**
<br>
![walks](https://user-images.githubusercontent.com/37829895/161020316-02c65522-9cba-4a24-93ba-d079351b25c7.gif)

#### 🍀 Buy items for planting

Recorded steps will be converted into points to buy **water buckets** or **seeds**
<br>
![store](https://user-images.githubusercontent.com/37829895/161021053-32289864-03fb-46d2-a4b6-e820589b3b52.gif)

#### 🍀 Personalize your experience

You can **set your name** or control features like **step counting** and **Push alarm** that automatically detects trees around,
**set number of trees to render** on the **setting page**
<br>
![setting](https://user-images.githubusercontent.com/37829895/161021308-98216ca6-aac9-4e65-852f-76d80719c568.gif)

#### 🍀 Push alarm

 Application will find trees around and make alarm
 <br>
![Push](https://user-images.githubusercontent.com/37829895/161036284-e97b866d-b10d-478f-9e39-c67242254636.jpg)


### 🔭 Future Visions

**<details><summary>Expand the possibility of interaction between users.</summary>** Even though society is one of the main components of our project, related features are currently reduced than initially thought. In the next step, interactions between users will take place with trees in between as they are now. For example, we can add features like users picking fruits from another user’s tree, or cutting another user’s tree. And if the obtained item could configure the tree to grow again from the item, not only the interaction but also the completeness of the app will increase.</details>

**<details><summary>Add motivating elements by creating a rewarding system.</summary>** Currently, points that users could get from the number of steps and trees that could be purchased with points are the only ways to motivate users to walk. Suppose that we have added an achievement system that gives rewards to users when they find new kinds of trees and fruits. Users will try to discover more trees for rewards, and this could be another motivation for them to walk more. The more users walk, the fewer carbon emissions will occur than using transportation. Additionally, the quality of individual health and the overall health of society will be improved.</details>

**<details><summary> Provide users rich experience</summary>** With a variety of types of seeds such as flowers or crops. It could make users’ experience richer when planting plants.</details>

#### Contributors

<a href="https://github.com/rxdcxdrnine"><img src="https://img.shields.io/badge/Changgu Kang-black?style=social-square&logo=github&logoColor=white"/></a>
<a href="https://github.com/SHEELE41"><img src="https://img.shields.io/badge/Jongkyu Seok-black?style=social-square&logo=github&logoColor=white"/></a>
<a href="https://github.com/iju1633"><img src="https://img.shields.io/badge/Jaeuk Im-black?style=social-square&logo=github&logoColor=white"/></a>
<a href="https://github.com/kstew16"><img src="https://img.shields.io/badge/Eunwoo Tae-black?style=social-square&logo=github&logoColor=white"/></a>
