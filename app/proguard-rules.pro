# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep only Firestore POJO models that rely on reflection (.toObject) while allowing R8 to obfuscate all other classes
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep @com.google.firebase.firestore.IgnoreExtraProperties class com.example.data.** { *; }
-keepclassmembers @com.google.firebase.firestore.IgnoreExtraProperties class com.example.data.** {
    <fields>;
    <init>(...);
    public <methods>;
}

# Keep Glance AppWidget ActionCallback zero-argument constructors for RunCallbackAction reflection
-keep class * implements androidx.glance.appwidget.action.ActionCallback {
    public <init>();
}
