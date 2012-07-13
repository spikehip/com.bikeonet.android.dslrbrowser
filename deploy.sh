/home/spike/bin/android-sdk-linux_86/platform-tools/adb uninstall com.bikeonet.android.dslrbrowser
mvn install -Dandroid.sdk.path=/home/spike/bin/android-sdk-linux_86/
/home/spike/bin/android-sdk-linux_86/platform-tools/adb install /home/spike/.m2/repository/com/bikeonet/android/dslrBrowser/0.0.2-SNAPSHOT/dslrBrowser-0.0.2-SNAPSHOT.apk
