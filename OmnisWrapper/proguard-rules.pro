# Add project specific ProGuard rules here.

# Keep Omnis Interface core classes and members
-keep class net.omnis.omnisinterface.** { *; }
-keepclassmembers class net.omnis.omnisinterface.** { *; }

# Keep Omnis Android app classes and members
-keep class net.omnis.android.** { *; }
-keepclassmembers class net.omnis.android.** { *; }

# Keep JavaScript Interface methods called from WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Preserve WebView client and Chrome client
-keepclassmembers class * extends android.webkit.WebViewClient {
    public *;
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public *;
}

# Keep Firebase messaging & analytics models
-keep class com.google.firebase.** { *; }

# Keep ML Kit Barcode Scanning
-keep class com.google.mlkit.** { *; }

# Preserve line numbers for stack trace debugging
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
