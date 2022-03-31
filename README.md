# 🌲 Treenity (2022 Google Solution Challenge)

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCC00?style=flat-square&logo=Firebase&logoColor=white&textCol...)

video coming soon!
[![](https://yt-embed.herokuapp.com/embed?v=y9kkXTucnLU)](https://www.youtube.com/watch?v=y9kkXTucnLU)

This is an application that provides an AR-based social networking/exercising experience.

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

- ### Architecture
  - [Lifecycles](https://developer.android.com/topic/libraries/architecture/lifecycle) - Allows control and respond lifecycles of events.
  - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - Allows immediate response of view on changes of data to show.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Stores UI-related data that does not volatilize even when the screen is rotated. Can be applied to set up asynchronous operations
  - [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) - Manage Android background jobs and schedule periodic works.

- ### Third Party
  - [Retrofit 2](https://square.github.io/retrofit/) is a type-safe HTTP client for Android and Java
  - [OkHttp 3](https://square.github.io/okhttp/) is an efficient HTTP client helps to request/response data with HTTP
  - [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) manages async tasks with background threads
  - [ARCore API](https://developers.google.com/ar/reference) is Google’s platform for building augmented reality experiences
  - [Google Maps Platform](https://developers.google.com/ar/reference) is Google’s platform for implement map-related tasks
  - [SceneView 0.2.0](https://github.com/SceneView/sceneview-android) is a 3D/AR Android View with ARCore and Google Filament.
  - [Hilt](https://dagger.dev/hilt/) provides a standard way to incorporate Dagger dependency injection into an Android application
  - [MPAndroidChart 3](https://github.com/PhilJay/MPAndroidChart) is a powerful & easy to use chart library for Android

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
