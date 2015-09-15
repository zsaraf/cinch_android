# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/nadavhollander/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

## Joda Time 2.3

-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

-dontwarn org.apache.commons.**
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**

-dontwarn okio.**

-keep class com.seshtutoring.seshapp.model.** { *; }
#-keep class com.seshtutoring.seshapp.model.AvailableBlock { *; }
#-keep class com.seshtutoring.seshapp.model.AvailableJob { *; }
#-keep class com.seshtutoring.seshapp.model.Card { *; }
#-keep class com.seshtutoring.seshapp.model.Constants { *; }
#-keep class com.seshtutoring.seshapp.model.Course { *; }
#-keep class com.seshtutoring.seshapp.model.Discount { *; }
#-keep class com.seshtutoring.seshapp.model.LearnRequest { *; }
#-keep class com.seshtutoring.seshapp.model.Message { *; }
#-keep class com.seshtutoring.seshapp.model.Notification { *; }
#-keep class com.seshtutoring.seshapp.model.OutstandingCharge { *; }
#-keep class com.seshtutoring.seshapp.model.PastRequest { *; }
#-keep class com.seshtutoring.seshapp.model.School { *; }
#-keep class com.seshtutoring.seshapp.model.Sesh { *; }
#-keep class com.seshtutoring.seshapp.model.Student { *; }
#-keep class com.seshtutoring.seshapp.model.Tutor { *; }
#-keep class com.seshtutoring.seshapp.model.User { *; }
