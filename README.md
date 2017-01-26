# Auto update apps you push to hockey

Needs root (of course). Perfect for IoT use case.

## Installation

to install as a system app execute the script `install_system.sh`. Example:

```
./install_system.sh com.myapp.wow myapp <path_to_apk>
```

## Development

setup `gradle_install_run.sh` to be executed on run as explained http://stackoverflow.com/a/31703543

## Uninstall

to remove this from system app execute the `uninstall_system.sh`. Example:

```
./uninstall_system.sh myapp
```

## Extras

- Provide `WIFI_SSID` and `WIFI_PASS` to auto-setup WiFi
- set `TURN_RADIOS_ON` to true (build.gradle) to monitor WiFi/Bluetooth state changes and keep them on
- set `SET_CLOCK` to true (build.gradle) to enable automatic date & time
- set `DISABLE_SCREEN_TIMEOUT` to true (build.gradle) to disable screen timeout