#http://proguard.sourceforge.net/manual/examples.html
#http://proguard.sourceforge.net/manual/usage.html

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keepclasseswithmembers class * {
    native <methods>;
#    public *;
}
-keepclasseswithmembers class * {
	native <methods>;
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers interface com.danielpark.imagecropper.CropperInterface {
    <methods>;
}
-keepclasseswithmembers class com.danielpark.imagecropper.CropperImageView {
    public *;
}
-keepclasseswithmembers enum com.danielpark.imagecropper.CropMode {
    *;
}
