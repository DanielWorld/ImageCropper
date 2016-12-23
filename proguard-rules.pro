#http://proguard.sourceforge.net/manual/examples.html
#http://proguard.sourceforge.net/manual/usage.html

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

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
-keepclasseswithmembers class com.danielpark.imagecropper.util.log.ImageCropperLogger {
     <methods>;
}
-keepclasseswithmembers interface com.danielpark.imagecropper.CropperInterface {
    <methods>;
}
-keepclasseswithmembers interface com.danielpark.imagecropper.listener.OnUndoRedoStateChangeListener {
    <methods>;
}
-keepclasseswithmembers interface com.danielpark.imagecropper.listener.OnThumbnailChangeListener {
    <methods>;
}
-keepclasseswithmembers class com.danielpark.imagecropper.CropperImageView {
    public *;
}
-keepclasseswithmembers class com.danielpark.imagecropper.util.CalculationUtil {
    public *;
}
-keepclasseswithmembers enum com.danielpark.imagecropper.CropMode {
    *;
}
-keepclasseswithmembers enum com.danielpark.imagecropper.UtilMode {
    *;
}
-keepclasseswithmembers enum com.danielpark.imagecropper.ControlMode {
    *;
}
-keepclasseswithmembers enum com.danielpark.imagecropper.ShapeMode {
    *;
}

