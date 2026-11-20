# Zoho SalesIQ Android SDK (Mobilisten) sample

This sample app shows how to add the Zoho SalesIQ Android SDK (Mobilisten) to an Android app. With the SDK, the people who use your app can chat with your team, get answers from a bot, read your help content, and talk to you in a voice call.

For the complete guide, see the [Mobilisten Android documentation](https://www.zoho.com/salesiq/help/developer-guides/mobilisten/android/).

## Requirements

- **Minimum Android version:** Android 5.0 (Lollipop), API level 21
- **Compile SDK version:** API level 35 (Android 15) or above
- **Java version:** Java 11 or above
- **Gradle version:** Gradle 8.7 or above, with Android Gradle Plugin 8.6 or above
- A Zoho SalesIQ account with your brand set up

## Get started

### 1. Get your App Key and Access Key

In the SalesIQ console, open **Settings**, select **Brands**, choose your brand, and select **Mobile SDK**. On the **Installation** page, select the **Android** tab. Enter your app's package name, select **Generate**, and copy the App Key and Access Key.

### 2. Add the Maven repository

Add the Zoho Maven repository to your `settings.gradle` file:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.zohodl.com") }
    }
}
```

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://maven.zohodl.com' }
    }
}
```

### 3. Add the dependency

Add the SDK to your app-level `build.gradle` file, and then click **Sync Now**. Add the Calls module only if you want in-app voice calls.

```kotlin
// app/build.gradle.kts
dependencies {
    implementation("com.zoho.salesiq:mobilisten:9.0.4")
    implementation("com.zoho.salesiq:mobilisten-calls:0.1.6") // Optional: voice calls
}
```

The SDK adds the permissions that it needs, such as `INTERNET`, to your app automatically.

### 4. Initialize the SDK

Initialize the SDK in the `onCreate()` method of your `Application` class. Call `MobilistenActivityLifecycleCallbacks.register(this)` first, before `super.onCreate()`. Replace `APP_KEY` and `ACCESS_KEY` with your own keys.

```kotlin
// MyApplication.kt
class MyApplication : Application() {
    override fun onCreate() {
        MobilistenActivityLifecycleCallbacks.register(this)
        super.onCreate()

        val configuration = SalesIQConfiguration.Builder("APP_KEY", "ACCESS_KEY").build()

        ZohoSalesIQ.initialize(this, configuration) { result ->
            if (result.isSuccess) {
                // Show the chat launcher. It is hidden by default.
                ZohoSalesIQ.Launcher.show(ZohoSalesIQ.Launcher.VisibilityMode.ALWAYS)
            } else {
                // Handle the failure.
            }
        }
    }
}
```

```java
// MyApplication.java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        MobilistenActivityLifecycleCallbacks.register(this);
        super.onCreate();

        SalesIQConfiguration configuration = new SalesIQConfiguration.Builder("APP_KEY", "ACCESS_KEY").build();

        ZohoSalesIQ.initialize(this, configuration, result -> {
            if (result.isSuccess()) {
                ZohoSalesIQ.Launcher.show(ZohoSalesIQ.Launcher.VisibilityMode.ALWAYS);
            } else {
                // Handle the failure.
            }
        });
    }
}
```

Then register the class in the `<application>` tag of your `AndroidManifest.xml` file:

```xml
<application
    android:name=".MyApplication"
    ... >
```

### 5. Add the ProGuard rule

If you use ProGuard or R8 (`minifyEnabled true`), add this rule to your `proguard-rules.pro` file:

```text
-dontwarn kotlinx.parcelize.Parcelize
```

### 6. Check your integration

Run your app. The chat button appears on the screen. Tap it and start a conversation, and the visitor appears on your SalesIQ dashboard.

## Run this sample

1. Clone this repository, and open it in Android Studio.
2. Replace `APP_KEY` and `ACCESS_KEY` with the keys from step 1.
3. Run the app on a device or an emulator.

## Learn more

- [Mobilisten Android documentation](https://www.zoho.com/salesiq/help/developer-guides/mobilisten/android/): push notifications, customization, the API reference, and voice calls.
- [Demo app on Google Play](https://play.google.com/store/apps/details?id=com.zoho.salesiq.zylkerhomes)
