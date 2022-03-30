# 🌲 Treenity (2022 Google Solution Challenge)

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-FFCC00?style=flat-square&logo=Firebase&logoColor=white&textColor=whilte)

video comming soon!
[![](https://yt-embed.herokuapp.com/embed?v=y9kkXTucnLU)](https://www.youtube.com/watch?v=y9kkXTucnLU)

This is an application provides AR based social-networking/exercising experience.

## 📖 Explanation

 We propose a way for you how to reduce your carbon footprint through fun-filled walking.
As users walk while using the app, the app records their steps.
The number of steps recorded is converted into points for buying seeds and buckets of water,
Users can plant seeds and water trees in places they frequently visit through AR technology and build up their efforts toward nature and health.
Not just this! Users can share their hard work with other users and also see and touch the world directly from their Android smartphones!

By walking and watering, you will be filled with the power of steady exercise.

👣Let's just plant your walks!👣


## 💎 Main Features

- Realtime step-counting service

- Google ARCore CloudAnchor based spatial information sharing system
  
- Marking hosted ar trees of all users on Google Maps
  
- Push Notification of hosted ar trees lists sorted via distance

## 🖥️ Build Environment

This project users the gradle build system.
To build this project, please user the `gradlew build` command or use "Import Project" in Android Studio.

This project is built on : `Gradle 7.0.2` with `JDK 11`

- `minSdkVersion` : 29
- `targetSdkVersion` : 31

## 🏛️ Libraries Used

- ### Architecture
  - [Lifecycles](https://developer.android.com/topic/libraries/architecture/lifecycle) - Allows control and respond lifecycles of events.
  - [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - Allows immediate respond of view on changes of datas to show.
  - [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - Stores UI-related data that does not volatilize even when the screen is rotated. Can be applied to set up asynchronous operations
  - [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) - Manage Android background jobs and schedule periodic works.

- ### Third Party
  - [Retrofit 2](https://square.github.io/retrofit/) is a type-safe HTTP client for Android and Java
  - [OkHttp 3](https://square.github.io/okhttp/) is an efficient HTTP client helps to request/response data with HTTP
  - [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) manages async tasks with background threads
  - [ARCore API](https://developers.google.com/ar/reference) is Google’s platform for building augmented reality experiences
  - [SceneView 0.2.0](https://github.com/SceneView/sceneview-android) is a 3D/AR Android View with ARCore and Google Filament.
  - [Hilt](https://dagger.dev/hilt/) provides a standard way to incorporate Dagger dependency injection into an Android application
  - [MPAndroidChart 3](https://github.com/PhilJay/MPAndroidChart) is a powerful & easy to use chart library for Android

## Usage

- Screenshots comming soon!

### 🔭 Future Visions

**<details><summary>Expand the possibility of interaction between users.</summary>** Even though society is one of the main components of our project, related features are currently reduced than initially thought. In the next step, interactions between users will take place with trees in between as they are now. For example, we can add features like users picking fruits from another user’s tree, or cutting another user’s tree. And if the obtained item could configure the tree to grow again from the item, not only the interaction but also the completeness of the app will increase.</details>

**<details><summary>Add motivating elements by creating a rewarding system.</summary>** Currently, points that users could get from the number of steps and trees that could be purchased with points are the only motivations that make users walk. Suppose that we have added an achievement system that gives rewards to users when they find new kinds of trees and fruits. Users will try to discover more trees for rewards, and this could be another motivation for them to walk more. The more users walk, the fewer carbon emissions will occur than using transportation. Additionally, the quality of individual health and the overall health of society will be improved.</details>

**<details><summary> Provide users rich experience</summary>** With a variety of types of seeds such as flowers or crops. It could make users’ experience richer when planting plants.</details>

#### Contributors

[Changgu Kang](https://github.com/rxdcxdrnine)|[Jongkyu Seok](https://github.com/SHEELE41)|[Jaeuk Im](https://github.com/iju1633)|[Eunwoo Tae](https://github.com/kstew16)
|:---:|:---:|:---:|:---:|
BACKEND|ANDROID|ANDROID|ANDROID|
