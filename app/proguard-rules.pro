# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Developer\android-sdk-windows/tools/proguard/proguard-android.txt
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
#-dontwarn
#-optimizationpasses 5
#-dontusemixedcaseclassnames
#-dontskipnonpubliclibraryclasses
#-dontpreverify
#-verbose
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgentHelper
#-keep public class * extends android.preference.Preference
#-keep public class com.android.vending.licensing.ILicensingService
#
#-keepclasseswithmembernames class * {
#    native <methods>;
#}
#
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet);
#}
#
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}
#
#-keepclassmembers class * extends android.app.Activity {
#   public void *(android.view.View);
#}
#
#-keepclassmembers enum * {
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}
#
#-keep class * implements android.os.Parcelable {
#  public static final android.os.Parcelable$Creator *;
#}

# okhttp
-dontwarn okio.**

# retrofit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses

-dontwarn androidsvg.**
#-keep class com.caverock:androidsvg.** {*;}
-keepclassmembers class com.caverock.androidsvg.SVGImageView {*;}

# VideoPlayer
-dontwarn sinavideo.**
-keepclassmembers class com.sina.** { *; }
-keep class com.sina.sinavideo.coreplayer.**{*;}
-keep class com.sina.sinavideo.coreplayer.splayer.**{*;}
-keep class com.sina.sinavideo.dlna.**{*;}

# rx
-dontwarn rx.**
-keepclassmembers class rx.** { *; }

# support library
-keep class android.support.v4.app.NotificationCompatJellybean { *; }
-keep class android.support.v7.widget.CardView {*; }
-keep class android.support.v7.widget.LinearLayoutManager { *; }
-keep class android.support.v7.widget.StaggeredGridLayoutManager { *; }
-keep class android.support.design.widget.AppBarLayout$ScrollingViewBehavior { *; }

# data binding
-keep class ooo.oxo.mr.databinding.** { *; }

# glide
-keep public class com.bumptech.glide.integration.okhttp.OkHttpGlideModule { *; }
-keep public class com.bumptech.glide.module.GlideModule { *; }
-keep public class * extends com.bumptech.glide.module.GlideModule { *; }
-keep public class * extends com.bumptech.glide.integration.okhttp.OkHttpGlideModule { *; }

# all in all
-keepnames class * { *; }

# tengxun ad
-dontwarn com.qq.**
-keep class com.qq.e.** {
    public protected *;
}
-keep class android.support.v4.app.NotificationCompat**{
    public *;
}

# Umeng
-keepclassmembers class * { public <init>(org.json.JSONObject); }
-keepclassmembers enum com.umeng.analytics.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}