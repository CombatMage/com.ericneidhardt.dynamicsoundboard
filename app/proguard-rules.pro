# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\development\AndroidSDK/tools/proguard/proguard-android.txt
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

-keepattributes LineNumberTable,SourceFile,*Annotation*,Signature

#jackson and pojos
-dontwarn com.fasterxml.jackson.databind.**
-keep class com.fasterxml.jackson.** { *; }
-keep class org.neidhardt.dynamicsoundboard.misc.JsonPojo { *; }

#greenDAO
-keep public class org.neidhardt.dynamicsoundboard.dao.** {
	public static <fields>;
}

#greenrobot EventBus
-keepclassmembers class ** {
    public void onEvent*(**);
}
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#ACRA
-keep class org.acra.ACRA {
    *;
}
-keep class org.acra.ReportingInteractionMode {
    *;
}
-keepnames class org.acra.sender.HttpSender$** {
    *;
}
-keepnames class org.acra.ReportField {
    *;
}
-keep public class org.acra.ErrorReporter {
    public void addCustomData(java.lang.String,java.lang.String);
    public void putCustomData(java.lang.String,java.lang.String);
    public void removeCustomData(java.lang.String);
}
-keep public class org.acra.ErrorReporter {
    public void handleSilentException(java.lang.Throwable);
}