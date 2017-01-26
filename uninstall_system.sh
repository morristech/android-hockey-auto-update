#!/bin/bash

dir_app_name=$1
if [ -z "$dir_app_name" ]
    then
        echo -n "Enter the App dir_name: "
        read dir_app_name
fi

ADB_SH="adb shell su -c" # see `Caveats` if using `adb su`

path_sysapp="/system/priv-app" # assuming the app is priviledged
apk_target_dir="$path_sysapp/$dir_app_name"

set -x

$ADB_SH "mount -o rw,remount /system"
$ADB_SH "rm -rf $apk_target_dir"
$ADB_SH "mount -o remount,ro /"
$ADB_SH "reboot"