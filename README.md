

### Gigaeyes RTSP Player 

Gigaeyes 프로젝트에서 사용되는 Cordova RTSP Player 플러그인 입니다.

(현재 안드로이드용만 제공합니다. 추후 IOS 용 추가 예정.)

## install
ionic cordova plugin add https://github.com/JAEGYUN/gigaeyesPlayer.git

## libVLC 지원 (only Android)

## Using 

``` javascript


var roi_info = [{
            "roi_id": "ROI-LineCount-INOUT-00001",
            "roi_type": "11",
            "order_no":1,
            "roi_coord": [{
              "x": "20",
              "y": "10"
            }, {"x": "80",
              "y": "10"
            }]
          }, {"roi_id": "ROI-LineCrossing-IN-00002",
            "roi_type": "14",
            "order_no":2,
            "roi_coord": [{
              "x": "29.583",
              "y": "43.796"
            }, {"x": "25.938",
              "y": "50.463"
            }]
          }, {"roi_id": "ROI-AreaCount-00003",
            "roi_type": "21",
            "order_no":3,
            "roi_coord": [{   
              "x": "61.823",
              "y": "29.537"
            }, {"x": "69.740",
              "y": "29.537"
            }, {"x": "69.740",
              "y": "54.815"
            }, {"x": "61.823",
              "y": "54.815"
            }]
          }, {"roi_id": "ROI-AreaAtack-IN-00004",
            "roi_type": "22",
            "order_no":4,
            "roi_coord": [{   
              "x": "1.927",
              "y": "31.389"
            }, {"x": "17.188",
              "y": "31.389"
            }, {"x": "17.188",
              "y": "47.037"
            }, {"x": "1.927",
              "y": "47.037"
            }]
          }];
     
      var sensor_info = [{
              "sensor_id" : "SENSOR-00001",
              "sensor_type" : "10001", // 도난 센서
              "order_no":1,
              "location_x" : "50.000",
              "location_y" : "50.000"
            }, {
              "sensor_id" : "SENSOR-00002",
              "sensor_type" : "10002", // 문열림 센서
              "order_no":2,
              "location_x" : "70.000",
              "location_y" : "90.000"
            }
          ];

cordova.plugins.gigaeyesplayer.play("rtsp://10.0.0.100:554/video", 'cam_01', '2층 복도', roi_info , sensor_info ,'Y', 'Y' callbackSucces, callbackError);


```

## param
* url 
* camId : cam ID
* title : cam Name
* roiInfo : ROI Objects
* sensorInfo : Sensor Objects
* recordSattus : recording status. Y/N
* is_favorites : favorites Y/N

## return
```
 {
     type : 'favorites',
     camId : '2층 복도',
     action : 'Y''
 }
```

* type : favorites // 즐겨찾기
* action : Y (register) , N (release)

## 플러그인 종료시 이벤트
* type : result // 플러그인 종료
* action : ok


