## Installing Mobilisten (ZohoSalesIQ Android SDK)

###### Step 1:
   Add the following maven repository in the root build.gradle file (**/build.gradle**).
   
     allprojects {
        repositories {
           maven { url 'https://maven.zohodl.com' }
        }
     }

###### Step 2:
   Add the following dependency in the app's build.gradle file (**app/build.gradle**).
   
    dependencies {
       implementation 'com.zoho.salesiq:mobilisten:3.1'
    }
    
###### Step 3:

 Then, press **Sync Now** in the bar that appears in the IDE.
 
###### Step 4:
 
  Now, initialize the SDK by adding the following line in the **onCreate()** method of your **Application class**. You will have to insert the **app key** and **access key** as mentioned below:
  
    public class MyApplication extends Application {
        @Override
        public void onCreate() {
             super.onCreate();
             ZohoSalesIQ.init(this, "your app key", "your access key");
             
             //by default launcher will be hidden can be enabled with following line.
             ZohoSalesIQ.showLauncher(true);
        }
    }
    
 Help Doc - [Mobilisten (ZohoSalesIQ Android SDK)](https://www.zoho.com/salesiq/help/developer-section/android-mobile-sdk-installation.html)

###### Demo Application:
 Download our demo app from the [Play Store](https://play.google.com/store/apps/details?id=com.zoho.salesiq.zylkerhomes) today!
