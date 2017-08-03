#import <UIKit/UIKit.h>

@class GigaeyesPlayer;

@interface PlayerViewController : UIViewController {
    BOOL getFrame;
    float lastFrameTime;
}

-(void) imageTap;

@property (retain, nonatomic) GigaeyesPlayer* origem;
@property (retain, nonatomic) NSString* videoAddress;
@property (retain, nonatomic) NSString* playType;
@property (retain, nonatomic) NSString* camId;
@property (retain, nonatomic) NSString* camName;
@property (retain, nonatomic) NSString* roiInfo;
@property (retain, nonatomic) NSString* sensorInfo;
@property (retain, nonatomic) NSString* recordStatus;
@property (retain, nonatomic) NSString* isFavorites;
@property (assign, nonatomic) BOOL shouldRotate;

@end
