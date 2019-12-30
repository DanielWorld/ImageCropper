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
-keepclasseswithmembers interface com.danielworld.imagecropper.CropperInterface {
    <methods>;
}
-keepclasseswithmembers interface com.danielworld.imagecropper.listener.OnUndoRedoStateChangeListener {
    <methods>;
}
-keepclasseswithmembers interface com.danielworld.imagecropper.listener.OnThumbnailChangeListener {
    <methods>;
}
-keepclasseswithmembers class com.danielworld.imagecropper.model.CropSetting {
    public <init>(com.danielworld.imagecropper.model.CropSetting$CropBuilder);
}
-keepclasseswithmembers class com.danielworld.imagecropper.model.CropSetting {
    <methods>;
}
-keepclasseswithmembers class com.danielworld.imagecropper.model.CropSetting$CropBuilder {
    <methods>;
}
-keepclasseswithmembers class com.danielworld.imagecropper.CropperImageView {
    public *;
}
-keepclasseswithmembers class com.danielworld.imagecropper.util.CalculationUtil {
    public *;
}
-keepclasseswithmembers enum com.danielworld.imagecropper.CropMode {
    *;
}
-keepclasseswithmembers enum com.danielworld.imagecropper.UtilMode {
    *;
}
-keepclasseswithmembers enum com.danielworld.imagecropper.ControlMode {
    *;
}
-keepclasseswithmembers enum com.danielworld.imagecropper.ShapeMode {
    *;
}
-keepclasseswithmembers enum com.danielworld.imagecropper.CropExtension {
    *;
}


