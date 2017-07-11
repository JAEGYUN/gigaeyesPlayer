

### Gigaeyes RTSP Player 

Gigaeyes 프로젝트에서 사용되는 Cordova RTSP Player 플러그인 입니다.

(현재 안드로이드용만 제공합니다. 추후 IOS 용 추가 예정.)

## install
ionic cordova plugin add https://github.com/JAEGYUN/gigaeyesPlayer.git

## android build (Android Studio)

add code in build.gradle(Module:android)

```
buildscript {
    repositories{
        ...
        maven {
            url 'https://maven.google.com'
        }
    }
    ...
}
...
dependencies {
    ...
    compile 'com.android.support.constraint:constraint-layout:1.0.2'
}     
```

## Using

``` javascript

cordova.plugins.gigaeyesplayer.play("rtsp://10.0.0.100:554/video", 'cam_01', '2층 복도', 'Y', callbackSucces, callbackError);


```

## param
* url 
* camId : 카메라 ID
* title : 카메라 명
* recordSattus : 카메라 녹화상태. Y/N


