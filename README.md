Adding a Sender as a Module
=====================
1. File - Import module - <path to Sender> 
2. Add: ```classpath 'com.google.gms:google-services:3.0.0'``` to build.gradle file of your project.
3. Project structure - app - Dependencies - «+» - Module dependencies - :sender
4. Open build.gradle file in Senders module:
    1. Change line :
        ```apply plugin: 'com.android.application'```
        to
        ```apply plugin: 'com.android.library'``` 
        in build.gradle file in Senders module.
    2. Delete: ```applicationId "mobi.sender"```
    3. Delete:
        
        ```
        /*Crash analitics*/
	compile('com.crashlytics.sdk.android:crashlytics:2.6.5@aar') {
	        transitive = true;
        }
        ```
    4. Delete all crashlytics files from App.class in module.
5. Transfer all from Sender manifest to manifest your app. Leave only:

        <?xml version="1.0" encoding="utf-8"?>
        <manifest xmlns:android="http://schemas.android.com/apk/res/android"
        package="mobi.sender"
        android:installLocation="preferExternal" />
        
    Don’t forget to remove this:  
    ```
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    ```
    from StartActivity.
6. ProGuard rules:
    1. Copy all rules from Senders file to your rules;
    2. Add:
		
		```
		buildTypes {
		    release {
	            minifyEnabled true
                proguardFiles   getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
            }
            debug {
                minifyEnabled true
                proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
            }
        }
        ```
7. Add: 

    ```
    <string name="provider_str" translatable="false">com.your.package.fileprovider</string> 
    ```
    in strings.xml
8. Extends your application from the Senders, add to OnCreate method:
			
		private void registerEvents() {
			Bus.getInstance().register(this, Code3Event.class.getSimpleName());
			Bus.getInstance().register(this, UndefinedEvent.class.getSimpleName());
			Bus.getInstance().register(this, P24onRegEvent.class.getSimpleName());
			Bus.getInstance().register(this, P24ChangeBtcEvent.class.getSimpleName());
			Bus.getInstance().register(this,P24onBitcoinClickEvent.class.getSimpleName());
			Bus.getInstance().register(this, P24openEvent.class.getSimpleName());
			Bus.getInstance().register(this, P24enableFullVerReq.class.getSimpleName());
		}

	and override onEvent(Bus.Event evt):

		@Override
		public void onEvent(Bus.Event evt) {
		   if (evt instanceof Code3Event) {
		   } else if (evt instanceof UndefinedEvent) {
		   } else if (evt instanceof P24onRegEvent || evt instanceof P24ChangeBtcEvent) {
		   } else if (evt instanceof P24openEvent) {
		   } else if (evt instanceof P24enableFullVerReq) {
		   } else if (evt instanceof P24onBitcoinClickEvent){
		   }
		}
