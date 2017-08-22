#import "GigaeyesPlayer.h"
#import "PlayerViewController.h"
#import <SGPlayer/SGPlayer.h>
#import "UIColor+Extension.h"

@interface PlayerViewController() {
    BOOL isHidden;
    BOOL enableFavorites;
    BOOL enableRecord;
    BOOL enableIot;
    BOOL enableVA;
    BOOL isSchedule;
    UIImage *door;
    UIImage *sound;
    UIImage *fire;
    UIImage *motion;
    UIImage *temperature;
    UIImage *humidity;
    UIActivityIndicatorView *spinner;
    NSOperationQueue *opQueue;
}

//@property (nonatomic, retain) NSTimer *nextFrameTimer;
@property (nonatomic, strong) SGPlayer * player;
@property (nonatomic, strong) UIView * vaView;
@property (nonatomic, strong) CALayer * vaLayer;
@property (nonatomic, strong) CALayer * iotLayer;
@property (nonatomic, assign) CGFloat screenWidth;
@property (nonatomic, assign) CGFloat screenHeight;
@property (weak, nonatomic) IBOutlet UILabel *stateLabel;
@property (weak, nonatomic) IBOutlet UILabel *titleLabel;
@property (weak, nonatomic) IBOutlet UISlider *progressSilder;
@property (weak, nonatomic) IBOutlet UILabel *currentTimeLabel;
@property (weak, nonatomic) IBOutlet UILabel *totalTimeLabel;
@property (weak, nonatomic) IBOutlet UINavigationBar *navigationBar;
@property (weak, nonatomic) IBOutlet UINavigationItem *navigationBarTitle;
@property (weak, nonatomic) IBOutlet UIButton *favoritesButton;
@property (weak, nonatomic) IBOutlet UIButton *recordStateButton;
@property (weak, nonatomic) IBOutlet UIButton *iotButton;
@property (weak, nonatomic) IBOutlet UIButton *vaButton;
@property (nonatomic, assign) CGSize * currentSize;
@property (nonatomic, assign) BOOL progressSilderTouching;
@end

@implementation PlayerViewController

// Load with xib :)
- (id) initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    enableFavorites = NO;
    enableRecord = NO;
    isHidden = NO;
    //landscape 강제..
    [self setShouldRotate:YES];
    return self;
}


- (void)viewDidLoad {
    [super viewDidLoad];
    door = [UIImage imageNamed:@"ico_door.png"];
    motion = [UIImage imageNamed:@"ico_theft.png"];
    sound = [UIImage imageNamed:@"ico_sound.png"];
    fire = [UIImage imageNamed:@"ico_fire.png"];
    temperature = [UIImage imageNamed:@"ico_temperature.png"];
    humidity = [UIImage imageNamed:@"ico_humidity.png"];

    // 플레이어 호출 부분
    self.view.backgroundColor = [UIColor blackColor];
    
    // 플레이어 등록
    self.player = [SGPlayer player];
    
    // callback handler 등록
    [self.player registerPlayerNotificationTarget:self
                                      stateAction:@selector(stateAction:)
                                   progressAction:@selector(progressAction:)
                                   playableAction:@selector(playableAction:)
                                      errorAction:@selector(errorAction:)];
    [self.navigationBarTitle.title setValue:self.camName forKey:self.camName];
    // 탭하여 화면 재생
    [self.player setViewTapAction:^(SGPlayer * _Nonnull player, SGPLFView * _Nonnull view) {
        NSLog(@"player display view did click!");
        isHidden = !isHidden;
        [self hiddenBar];
        
        
    }];
    
    
    [self.view insertSubview:self.player.view atIndex:0];
    
    // 캠명...
    self.navigationBarTitle.title = self.camName;
    
    NSLog(@"요청 URL %@", self.videoAddress);
    // URL을 UTF-8로 변환하여 저장(NSString --> NSURL)
    NSURL* urlString =  [NSURL URLWithString:[self.videoAddress stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]]];
    
    NSLog(@"요청 URL Check %@", [urlString absoluteString]);
    
    // 플레이어 디코더 선택...AVPlayer와 FFmepgDecoder 또는 모두 사용가능하나 FFmpeg을 사용하도록 설정
    self.player.decoder =  [SGPlayerDecoder decoderByFFmpeg];
    //    self.player.decoder =  [SGPlayerDecoder decoderByDefault];
    [self.player.decoder setFFmpegCodecContextOptionStringValue :@"tcp" forKey:@"rtsp_transport"];
    [self.player.decoder setFFmpegCodecContextOptionStringValue :@"prefer_tcp" forKey:@"rtsp_flags"];
    [self.player.decoder setFFmpegCodecContextOptionStringValue :@"0" forKey:@"timeout"];
    [self.player.decoder setFFmpegCodecContextOptionStringValue :@"video" forKey:@"allowed_media_types"];
    
    // 하드웨어 가속
    self.player.decoder.hardwareAccelerateEnableForFFmpeg = YES;
    // 자동재생
    // self.player.backgroundMode = SGPlayerBackgroundModeContinue;
    self.player.backgroundMode = SGPlayerBackgroundModeAutoPlayAndPause;
    // 일반 영상 재생
    [self.player replaceVideoWithURL:urlString];
    [self addTapGesture];
    
    opQueue = [[NSOperationQueue alloc] init];
    opQueue.maxConcurrentOperationCount = 1; // set to 1 to force everything to run on one thread;
    
    [self drawFavorites];
    [self drawRecordStatus];
    
    //  VA 처리를 위한 Layer
    self.vaLayer = [CALayer layer];
    // IoT처리를 위한 Layer
    self.iotLayer = [CALayer layer];
    
    self.screenWidth  = UIInterfaceOrientationIsPortrait(self.preferredInterfaceOrientationForPresentation)?
    [[UIScreen mainScreen] bounds].size.height: [[UIScreen mainScreen] bounds].size.width;
    
    self.screenHeight  = (CGFloat)UIInterfaceOrientationIsPortrait(self.preferredInterfaceOrientationForPresentation)?
    [[UIScreen mainScreen] bounds].size.width: [[UIScreen mainScreen] bounds].size.height;
    
    
    self.vaLayer.frame = CGRectMake(0, 0, self.screenWidth ,self.screenHeight);
    self.iotLayer.frame = CGRectMake(0, 0, self.screenWidth ,self.screenHeight);
    
    //    vaLayer.backgroundColor = [UIColor yellowColor].CGColor;
    
    //   뷰에 VA/IoT 레이어를 추가한다.
    [self.view.layer addSublayer:self.vaLayer];
    [self.view.layer addSublayer:self.iotLayer];
    
    //    CAShapeLayer *line = [CAShapeLayer layer];
    //
    //    long viewHeight = 200;
    //
    //    //ROI 샘플코드
    //    UIBezierPath *path = [[UIBezierPath alloc] init];
    //    [path moveToPoint:CGPointMake(20, viewHeight-19.5)];
    //    [path addLineToPoint:CGPointMake(200, viewHeight-19.5)];
    //    [path addLineToPoint:CGPointMake(300, viewHeight-119.5)];
    //    [path addLineToPoint:CGPointMake(120, viewHeight-119.5)];
    //    [path addLineToPoint:CGPointMake(20, viewHeight-19.5)];
    //
    //
    //    [[UIColor colorWithRed:(248/255.0) green:(222/255.0) blue:(173/255.0) alpha:1.0] setFill];
    //    [path fill];
    //    [[UIColor colorWithRed:(170/255.0) green:(138/255.0) blue:(99/255.0) alpha:1.0] setStroke];
    //    [path stroke];
    //
    //    line.path = path.CGPath;
    //
    //    //이미지 샘플코드.
    //    UIImage* il =[UIImage imageNamed:@"ico_door.png"];
    //    CALayer * imageLayer = [CALayer layer];
    //    imageLayer.frame = CGRectMake(100, 300, 100, 100);
    //    imageLayer.opacity = 0.65;
    //    imageLayer.contents = (id)il.CGImage;
    //    [vaLayer addSublayer:imageLayer];
    //
    //
    //    UIImage* il1 =[UIImage imageNamed:@"ico_fire.png"];
    //    UIImage *rotatedImage = [self imageRotatedByDegrees:il1 deg:90];
    //
    //    CALayer * imageLayer1 = [CALayer layer];
    //    imageLayer1.frame = CGRectMake(300, 100, 100, 100);
    //    imageLayer1.opacity = 0.65;
    //    imageLayer1.contents = (id)rotatedImage.CGImage;
    //
    //
    //
    //    [vaLayer addSublayer:imageLayer1];
    //
    //
    //    //  rotate 샘플코드
    //    CAShapeLayer * imageLayer2 = [CAShapeLayer layer];
    //    UIBezierPath *path1 = [[UIBezierPath alloc] init];
    //    [path1 moveToPoint:CGPointMake(0, 50)];
    //    [path1 addLineToPoint:CGPointMake(200, 50)];
    //    imageLayer2.lineWidth = 3;
    //    imageLayer2.path = path1.CGPath;
    //    imageLayer2.strokeColor = [UIColor redColor].CGColor;
    //
    //    UIImage* il2 =[UIImage imageNamed:@"ico_arrow_left.png"];
    //
    //    CALayer * imageLayer3 = [CALayer layer];
    //    imageLayer3.frame = CGRectMake(75, 30, 50, 50);
    //    imageLayer3.opacity = 0.65;
    //    imageLayer3.contents = (id)il2.CGImage;
    //
    //
    //    [imageLayer2 addSublayer:imageLayer3];
    //
    //    CGFloat intVal = 10;
    ////    CGAffineTransformMakeRotation(intVal);
    //    imageLayer2.affineTransform = CGAffineTransformMakeRotation(intVal);
    //    imageLayer2.frame = CGRectMake(200, 200, imageLayer2.bounds.size.width, imageLayer2.bounds.size.height);
    ////    CGAffineTransformRotate(CGAffineTransform t, <#CGFloat angle#>)
    ////    CGAffineTransformMakeRotation(intVal);
    //    [vaLayer addSublayer:line];
    //    [vaLayer addSublayer:imageLayer2];
    //
    ////    self.vaView =[[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width,self.view.bounds.size.height)];
    ////    [self.view addSubview:self.vaView];
    //
    ////    [self.view insertSubview:self.vaView atIndex:5];
    ////    [self.view bringSubviewToFront:self.player.view];
    //
    
    [self parseRoiInfo];
    [self parseSensorInfo];
}


// 파라미터의 ROI json 객체로 Polygon Layer를 생성한다.
- (void)drawPolygon:(NSDictionary*) jsonObject{
    
    NSLog(@"drawPolygon : inputDta :%@" , jsonObject );
    CAShapeLayer * imageLayer = [CAShapeLayer layer];
    UIBezierPath *path = [[UIBezierPath alloc] init];
    
    NSString *roiType = [jsonObject objectForKey:@"roi_type"];
    NSLog(@"roi_type....%@",roiType );
    
    NSMutableArray * arry =[jsonObject objectForKey:@"roi_coord"];
    NSLog(@"draw polygon....%@",arry );
    
    
    NSArray *array = [jsonObject objectForKey:@"roi_coord"];
    NSLog(@"draw polygon....%@",array );
    
    
    for(int n = 0; n <[array count]; n++){
        
        NSDictionary *pt = [array objectAtIndex:n];
        
        NSLog(@">>>>>%@", pt );
        
        NSString *x = [pt objectForKey:@"x"];
        NSString *y = [pt objectForKey:@"y"];
        NSLog(@"data : x :%@, y :%@" , x, y );
        if(n == 0){
            [path moveToPoint:CGPointMake([x floatValue]/self.screenWidth/100000, [y floatValue]/self.screenHeight/100000)];
        }else{
            [path addLineToPoint:CGPointMake([x floatValue]/self.screenWidth/100000, [y floatValue]/self.screenHeight/100000)];
        }
        
    }
    
    if([roiType isEqualToString:@"21" ] ){
        [[UIColor colorWithRGBHex:(0x36B255)] setFill];
    }else if([roiType isEqualToString:@"22"] || [roiType isEqualToString:@"24"]){
        [[UIColor colorWithRGBHex:(0xFF962E)] setFill];
    }else if([roiType isEqualToString:@"23" ]){
        [[UIColor colorWithRGBHex:(0xA75BCB)] setFill];
    }else if([roiType isEqualToString:@"25" ]){
        [[UIColor colorWithRGBHex:(0x5AAAC7)] setFill];
    }else if([roiType isEqualToString:@"26" ]){
        [[UIColor colorWithRGBHex:(0x969696)] setFill];
    }else{
        [[UIColor colorWithRGBHex:(0x36B255)] setFill];
    }
    
    NSLog(@"색 변경" );
    
    //  ROI 색상 설정
    //    [[UIColor colorWithRed:(248/255.0) green:(222/255.0) blue:(173/255.0) alpha:1.0] setFill];
    [path fill];
    NSLog(@"라인 색 변경" );
    
    [[UIColor redColor] setStroke];
    [path stroke];
    
    
    imageLayer.lineWidth = 3;
    imageLayer.path = path.CGPath;
    imageLayer.strokeColor = [UIColor redColor].CGColor;
    NSLog(@"vaLayer 에 추가" );
    
    [self.vaLayer addSublayer:imageLayer];
    NSLog(@"완료" );
    
}

static CGFloat DegreesToRadians(CGFloat degrees) {return degrees * M_PI / 180;}
static CGFloat RadiansToDegrees(CGFloat radians) {return radians * 180/M_PI;}

- (UIImage *)imageRotatedByDegrees:(UIImage*)oldImage deg:(CGFloat)degrees{
    //Calculate the size of the rotated view's containing box for our drawing space
    UIView *rotatedViewBox = [[UIView alloc] initWithFrame:CGRectMake(0,0,oldImage.size.width, oldImage.size.height)];
    CGAffineTransform t = CGAffineTransformMakeRotation(degrees * M_PI / 180);
    rotatedViewBox.transform = t;
    CGSize rotatedSize = rotatedViewBox.frame.size;
    
    //Create the bitmap context
    UIGraphicsBeginImageContext(rotatedSize);
    CGContextRef bitmap = UIGraphicsGetCurrentContext();
    
    //Move the origin to the middle of the image so we will rotate and scale around the center.
    CGContextTranslateCTM(bitmap, rotatedSize.width/2, rotatedSize.height/2);
    
    //Rotate the image context
    CGContextRotateCTM(bitmap, (degrees * M_PI / 180));
    
    //Now, draw the rotated/scaled image into the context
    CGContextScaleCTM(bitmap, 1.0, -1.0);
    CGContextDrawImage(bitmap, CGRectMake(-oldImage.size.width / 2, -oldImage.size.height / 2, oldImage.size.width, oldImage.size.height), [oldImage CGImage]);
    
    UIImage *newImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return newImage;
}


-(void) parseRoiInfo{
    
    NSLog(@"data : sendDta :%@" , self.roiInfo );
    
    //    NSData *jsonData = [self.roiInfo dataUsingEncoding:NSUTF8StringEncoding];
    NSMutableArray *jsonData = self.roiInfo;
    //    NSError * error;
    
    //    id jsonObject = [NSJSONSerialization JSONObjectWithData:jsonData options:0 error:&error];
    //    if(error){
    //        NSLog(@"Error parsing JSON : %@",jsonData);
    //    }else{
    //        if([jsonObject isKindOfClass:[NSArray class]]){
    //            NSLog(@"type : array!");
    //            NSArray * jsonArray =(NSArray *)jsonObject;
    NSArray * jsonArray =(NSArray *)jsonData;
    NSLog(@"jsonArray - %@", self.roiInfo);
    
    for(id rois in jsonArray){
        NSString * roiType = [rois objectForKey:@"roi_type"];
        if([roiType isEqualToString:@"11" ] || [roiType isEqualToString:@"12"] || [roiType isEqualToString:@"13"]||
           [roiType isEqualToString:@"14" ] || [roiType isEqualToString:@"15" ] || [roiType isEqualToString:@"16" ] ||
           [roiType isEqualToString:@"18" ] || [roiType isEqualToString:@"19" ] || [roiType isEqualToString:@"102" ]){
            //         Line그리기
            
        }else{
            //        영역그리기
            [self drawPolygon : rois];
        }
        
        
        //                if()
        
        //                CAShapeLayer * imageLayer2 = [CAShapeLayer layer];
        //                UIBezierPath *path1 = [[UIBezierPath alloc] init];
        //                [path1 moveToPoint:CGPointMake(0, 50)];
        //                [path1 addLineToPoint:CGPointMake(200, 50)];
        //                imageLayer2.lineWidth = 3;
        //                imageLayer2.path = path1.CGPath;
        //                imageLayer2.strokeColor = [UIColor redColor].CGColor;
        //
        //                NSMutableDictionary *map = [NSMutableDictionary dictionary];
        ////                map[@1]
    }
    //        }else{
    //            NSLog(@"type : dictionary!");
    //            NSDictionary * jsonDictionary =(NSDictionary *)jsonObject;
    //            NSLog(@"jsonArray - %@", jsonDictionary);
    //        }
    //    }
}

-(void) parseSensorInfo{
    BOOL enableShow = NO;
    if(![self isObjectnull : self.sensorInfo]){
        NSString * sampleStr = [self.sensorInfo componentsJoinedByString:@" "];
        NSLog(@"Parising %@", sampleStr);
        
       NSArray * jsonArray =self.sensorInfo;;
        NSLog(@"jsonArray - %@", self.sensorInfo);
    
    
    
    for(id iots in jsonArray){
        NSString * sensorTagCd = [iots objectForKey:@"sensor_tag_cd"];
        NSLog(@"parsing tagCd - %@", sensorTagCd);
        UIImage* il;
        if([sensorTagCd isEqualToString:@"10001" ]){
            il = motion;
        }else if([sensorTagCd isEqualToString:@"10002" ]){
            il = door;
        }else if([sensorTagCd isEqualToString:@"10003" ]){
            il = sound;
        }else if([sensorTagCd isEqualToString:@"10004" ]){
            il = humidity;
        }else if([sensorTagCd isEqualToString:@"10005" ]){
            il = temperature;
        }else if([sensorTagCd isEqualToString:@"10006" ]){
            il = fire;
        }
        CGFloat location_x = [[iots objectForKey:@"location_x"] floatValue] ;
        CGFloat location_y = [[iots objectForKey:@"location_y"] floatValue] ;
         NSLog(@"parsing x : %f, y : %f",  location_x, location_y);
        CALayer * imageLayer = [CALayer layer];
        imageLayer.frame = CGRectMake(location_x, location_y, 100, 100);
        imageLayer.opacity = 0.65;
        imageLayer.contents = (id)il.CGImage;
        NSLog(@"parsing add image to iotLayer");
        
        [self.iotLayer addSublayer:imageLayer];
        enableShow = YES;
    }
    }
    NSLog(@"parsing show iot button >>>>%d", enableShow);
     self.iotButton.hidden = enableShow;
}


-(UIInterfaceOrientationMask)application:(UIApplication *)application supportedInterfaceOrientationsForWindow:(UIWindow *)window{
    if (self.shouldRotate)
        return UIInterfaceOrientationMaskAllButUpsideDown;
    else
        return UIInterfaceOrientationMaskPortrait;
}

- (void) hiddenBar{
    [self.navigationBar setHidden:isHidden];
    
}

// 즐겨찾기 상태값 변경
- (void)changeFavorites{
    NSString * diff = @"Y";
    NSLog(@"즐겨찾기 상태값 변경(AsIs) : %@", self.isFavorites);
    
    if([diff isEqualToString:self.isFavorites]){
        self.isFavorites = @"N";
    }else{
        self.isFavorites = @"Y";
    }
    NSLog(@"즐겨찾기 상태값 변경(ToBe) : %@", self.isFavorites);
    [self drawFavorites];
}


//  즐겨찾기 버튼 초기값 설정
- (void)drawFavorites{
    [self.favoritesButton setImage:[UIImage imageNamed:@"ico_star_off.png"] forState:UIControlStateNormal];
    [self.favoritesButton setImage:[UIImage imageNamed:@"ico_star.png"] forState:UIControlStateSelected];
    
    NSString * diff = @"Y";
    
    if([diff isEqualToString:self.isFavorites]){
        enableFavorites = YES;
    }
    
    self.favoritesButton.selected = enableFavorites;
}

//VA버튼 클릭
- (IBAction)toggleVa:(id)sender
{
    [self.vaButton setImage:[UIImage imageNamed:@"ico_va_off.png"] forState:UIControlStateNormal];
    [self.vaButton setImage:[UIImage imageNamed:@"ico_va.png"] forState:UIControlStateSelected];
    
    enableVA = !enableVA;
    
    self.vaButton.selected = enableVA;
    
    if(enableVA){
        [self.view.layer addSublayer:self.vaLayer];
    }else{
        [self.vaLayer removeFromSuperlayer];
    }
}

//IOT 버틀 클릭
- (IBAction)toggleIoT:(id)sender
{
    NSLog(@"즐겨찾기 상태값 변경(AsIs) : %@", self.isFavorites);
    [self.iotButton setImage:[UIImage imageNamed:@"ico_widget6_off.png"] forState:UIControlStateNormal];
    [self.iotButton setImage:[UIImage imageNamed:@"ico_widget6.png"] forState:UIControlStateSelected];
    
    enableIot = !enableIot;
    
    self.iotButton.selected = enableIot;
    
    if(enableIot){
        [self.view.layer addSublayer:self.iotLayer];
    }else{
        [self.iotLayer removeFromSuperlayer];
    }
}



// 저장상태 여부 표시
- (void)drawRecordStatus{
    [self.recordStateButton setImage:[UIImage imageNamed:@"ico_cameraON.png"] forState:UIControlStateNormal];
    [self.recordStateButton setImage:[UIImage imageNamed:@"ico_cameraon.png"] forState:UIControlStateSelected];
    [self.recordStateButton setImage:[UIImage imageNamed:@"ico_cameraOff.png"] forState:UIControlStateDisabled];
    
    
    NSString * diff = @"Y";
    NSString * err = @"Z";

    
    if([diff isEqualToString:self.recordStatus]){
        self.recordStateButton.selected = true;
    }else if([err isEqualToString:self.recordStatus]){
        self.recordStateButton.enabled = false;
    }else{
        self.recordStateButton.selected = false;
    }

}


- (void)viewDidLayoutSubviews
{
    [super viewDidLayoutSubviews];
    self.player.view.frame = self.view.bounds;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

-(void) viewDidAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [opQueue addOperationWithBlock:^{
        
    }];
}

- (BOOL)prefersStatusBarHidden {
    return YES;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
    // enable both landscape modes
    return (toInterfaceOrientation == UIInterfaceOrientationLandscapeRight || toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft);
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskLandscape;
}

-(void) addTapGesture {
    UITapGestureRecognizer *singleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(imageTap)];
    
    UIImageView *get = (UIImageView*)[self.view viewWithTag:100];
    
    [get setUserInteractionEnabled:YES];
    [get addGestureRecognizer:singleTap];
}

-(void) imageTap {
    
    isHidden = !isHidden;
    int direction;
    
    if(isHidden) {
        direction = -1;
    } else {
        direction = 1;
    }
    
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:0.5f];
    
    
    [UIView commitAnimations];
}

- (IBAction)buttonDismissPressed:(id)sender {
}

- (IBAction)back:(id)sender
{
    NSLog(@"뒤로가기 요청 : %@", sender);
    
    [self.presentingViewController dismissViewControllerAnimated:YES completion:nil ];
}

- (IBAction)play:(id)sender
{
    [self.player play];
}

- (IBAction)pause:(id)sender
{
    [self.player pause];
}

- (IBAction)progressTouchDown:(id)sender
{
    self.progressSilderTouching = YES;
}

- (IBAction)progressTouchUp:(id)sender
{
    self.progressSilderTouching = NO;
    [self.player seekToTime:self.player.duration * self.progressSilder.value];
}

- (void)stateAction:(NSNotification *)notification
{
    SGState * state = [SGState stateFromUserInfo:notification.userInfo];
    
    NSString * text;
    switch (state.current) {
        case SGPlayerStateNone:
            text = @"None";
            break;
        case SGPlayerStateBuffering:
            text = @"Buffering...";
            break;
        case SGPlayerStateReadyToPlay:
            text = @"Prepare";
            self.totalTimeLabel.text = [self timeStringFromSeconds:self.player.duration];
            [self.player play];
            break;
        case SGPlayerStatePlaying:
            text = @"Playing";
            break;
        case SGPlayerStateSuspend:
            text = @"Suspend";
            break;
        case SGPlayerStateFinished:
            text = @"Finished";
            break;
        case SGPlayerStateFailed:
            text = @"Error";
            break;
    }
    self.stateLabel.text = text;
}

// 프로그레스 바 액션
- (void)progressAction:(NSNotification *)notification
{
    SGProgress * progress = [SGProgress progressFromUserInfo:notification.userInfo];
    if (!self.progressSilderTouching) {
        self.progressSilder.value = progress.percent;
    }
    self.currentTimeLabel.text = [self timeStringFromSeconds:progress.current];
}


- (void)playableAction:(NSNotification *)notification
{
    SGPlayable * playable = [SGPlayable playableFromUserInfo:notification.userInfo];
    NSLog(@"playable time : %f", playable.current);
}

// 에러 액션
- (void)errorAction:(NSNotification *)notification
{
    SGError * error = [SGError errorFromUserInfo:notification.userInfo];
    NSLog(@"player did error : %@", error.error);
}

//재생시간 표시
- (NSString *)timeStringFromSeconds:(CGFloat)seconds
{
    return [NSString stringWithFormat:@"%ld:%.2ld", (long)seconds / 60, (long)seconds % 60];
}

- (void)dealloc
{
    [self.player removePlayerNotificationTarget:self];
}


//VA 그리기
-(void) getVAOverlayBitmap{
    
}

//즐겨찾기
- (IBAction)setFavorites:(id)sender
{
    NSLog(@"즐겨찾기 상태값 변경(AsIs) : %@", self.isFavorites);
    
    self.favoritesButton.selected = !self.favoritesButton.selected;
    if(self.favoritesButton.selected){
        self.isFavorites = @"Y";
    }else{
        self.isFavorites = @"N";
    }
    
    NSLog(@"즐겨찾기 상태값 변경(ToBe) : %@", self.isFavorites);
    
    [self.origem.commandDelegate runInBackground:^{
        NSDictionary *jsonInfo = @{@"type":@"favorites",@"camId":self.camId,@"acton":self.isFavorites};
        CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary: jsonInfo];
        pluginResult.keepCallback = [NSNumber numberWithBool:YES];
        [self.origem.commandDelegate sendPluginResult:pluginResult callbackId:self.origem.lastCommand.callbackId];
    }];
    
}

//SnapShot 썸네일 저장하기
- (IBAction)getBitmap:(id)sender{
    SGPLFImage *snapshot = self.player.snapshot;
    UIImageWriteToSavedPhotosAlbum(snapshot,nil, nil, nil);
    NSLog(@"The image: %@", snapshot);
    
}

-(BOOL) isObjectnull:(id )value{
    if([value isEqual:[NSNull null] ] || !value){
        return YES;
    }
    return NO;
}

@end

