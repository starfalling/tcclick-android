ANDROID_JAR=/software/eclipse/android/android-sdk-mac_x86/platforms/android-8/android.jar

rm -rf bin/classes
mkdir bin/classes
javac -bootclasspath $ANDROID_JAR -d bin/classes -sourcepath src src/com/truecolor/tcclick/TCClick.java
cp -r src/com/truecolor/tcclick/ bin/classes/com/truecolor/tcclick/
rm bin/classes/com/truecolor/tcclick/TCClickActivity.java
jar cf tcclick.jar -C bin/classes/ .
rm -rf bin/classes
