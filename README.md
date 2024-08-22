
## Installing Mobilisten (ZohoSalesIQ Android SDK)

#### Requirements
The SDK supports Android 5.0 (API level 21) and above.

###### Step 1:
Add the following maven repository in the **settings.gradle** file or root build.gradle file (**/build.gradle**).

     allprojects {
        repositories {
           maven { url 'https://maven.zohodl.com' }
        }
     }
![enter image description here](https://www.zohowebstatic.com/sites/zweb/images/salesiq/step-4---settings.gradle-android-sdk.png)
###### Step 2:
Add the following dependency in the app's build.gradle file (**app/build.gradle**).

    dependencies {
       implementation 'com.zoho.salesiq:mobilisten:8.0.3'
    }

![enter image description here](https://www.zohowebstatic.com/sites/zweb/images/salesiq/step-5---dependency-android-sdk.png)
###### Step 3:

Then, press **Sync Now** in the bar that appears in the IDE.

![enter image description here](https://www.zohowebstatic.com/sites/zweb/images/salesiq/sync-now---android-sdk.png)
###### Step 4:

Now, initialize the SDK by adding the following line in the **onCreate()** method of your **Application class**. You will have to insert the **app key** and **access key** as mentioned below:

    public class MyApplication extends Application {
        @Override
        public void onCreate() {
            super.onCreate();
            
            InitConfig initConfig = new InitConfig();
            initConfig.setFont(Fonts.REGULAR, <fontpath>);
            
            ZohoSalesIQ.init(this, "your app key", "your access key", initConfig, new InitListener() {
                @Override
                public void onInitSuccess() {
                     ZohoSalesIQ.Launcher.show(ZohoSalesIQ.Launcher.VisibilityMode.ALWAYS); //by default launcher will be hidden and it can be enabled with this line.
                }

                @Override
                public void onInitError(int errorCode, String errorMessage) {
                    //your code
                }
            });
        }
    }

###### Step 5:
Also, in the **AndroidManifest.xml** file, add the application class name in the **application** tag.

    <application  android:name=".MyApplication">
      ...
    </application>​

Help Doc - [Mobilisten (ZohoSalesIQ Android SDK)](https://www.zoho.com/salesiq/help/developer-section/android-mobile-sdk-installation.html)

###### Demo Application:
Download our demo app from the [Play Store](https://play.google.com/store/apps/details?id=com.zoho.salesiq.zylkerhomes) today!
