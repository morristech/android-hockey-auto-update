#!/bin/bash

app_package=$1
if [ -z "$app_package" ]
    then
        echo -n "Enter the App packageName: "
        read app_package
fi

dir_app_name=$2
if [ -z "$dir_app_name" ]
    then
        echo -n "Enter the App dir_name: "
        read dir_app_name
fi

apk_host=$3
if [ -z "$apk_host" ]
    then
        echo -n "Enter the App .apk path: "
        read apk_host
fi

ADB_SH="adb shell su -c" # see `Caveats` if using `adb su`

path_sysapp="/system/priv-app" # assuming the app is priviledged
apk_name=$dir_app_name".apk"
apk_target_dir="$path_sysapp/$dir_app_name"
apk_target_sys="$apk_target_dir/$apk_name"

set -x

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
adb shell reboot