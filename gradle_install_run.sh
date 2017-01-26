#!/bin/bash

#
# script from this http://stackoverflow.com/a/31703543
# this is to be added as "Run/Debug COnfiguration" while developing
#

ANDROID_SDK=`./parser.sh sdk.dir local.properties`
PATH=$PATH:"$ANDROID_SDK/platform-tools"

echo $PATH

# CHANGE THESE
app_package=$1 #"com.sensorberg.iot_autoupdate"
dir_app_name=$2 #"iot_update"
MAIN_ACTIVITY=$3 #"MainActivity"

ADB_SH="adb shell su -c" # see `Caveats` if using `adb su`

path_sysapp="/system/priv-app" # assuming the app is priviledged
apk_host="./app/build/outputs/apk/app-debug.apk"
apk_name=$dir_app_name".apk"
apk_target_dir="$path_sysapp/$dir_app_name"
apk_target_sys="$apk_target_dir/$apk_name"

# Delete previous APK
rm -f $apk_host

# Compile the APK: you can adapt this for production build, flavors, etc.
./gradlew assembleDebug || exit -1 # exit on failure

# Install APK: using adb su
$ADB_SH "mount -o rw,remount /system"
$ADB_SH "chmod 777 /system/lib/"
$ADB_SH "mkdir -p /sdcard/tmp" 2> /dev/null
$ADB_SH "mkdir -p $apk_target_dir" 2> /dev/null
adb push $apk_host /sdcard/tmp/$apk_name 2> /dev/null
$ADB_SH "mv /sdcard/tmp/$apk_name $apk_target_sys"
$ADB_SH "rmdir /sdcard/tmp" 2> /dev/null

# Give permissions
$ADB_SH "chmod 755 $apk_target_dir"
$ADB_SH "chmod 644 $apk_target_sys"

#Unmount system
$ADB_SH "mount -o remount,ro /"

# Stop the app
adb shell "am force-stop $app_package"

# Re execute the app
adb shell "am start -n \"$app_package/$app_package.$MAIN_ACTIVITY\" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER"