#import "GigaeyesPlayer.h"
#import "PlayerViewController.h"
#import <SGPlayer/SGPlayer.h>

@interface PlayerViewController (){
    BOOL isHidden;
    UIActivityIndicatorView *spinner;
    NSOperationQueue *opQueue;
}

//@property (nonatomic, retain) NSTimer *nextFrameTimer;
@property (nonatomic, strong) SGPlayer * player;

@property (weak, nonatomic) IBOutlet UILabel *stateLabel;
@property (weak, nonatomic) IBOutlet UILabel *titleLabel;
@property (weak, nonatomic) IBOutlet UISlider *progressSilder;
@property (weak, nonatomic) IBOutlet UILabel *currentTimeLabel;
@property (weak, nonatomic) IBOutlet UILabel *totalTimeLabel;
@property (weak, nonatomic) IBOutlet UINavigationItem *navigationBarTitle;

@property (nonatomic, assign) BOOL progressSilderTouching;
@end

@implementation PlayerViewController

// Load with xib :)
- (id) initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    isHidden = NO;
    
    return self;
}


- (void)viewDidLoad {
    [super viewDidLoad];
    
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
    }];
    [self.view insertSubview:self.player.view atIndex:0];

    // 캠명...   
    self.navigationBarTitle.title = self.camName;
    
    NSLog(@"요청 URL %@", self.videoAddress);
    // URL을 UTF-8로 변환하여 저장(NSString --> NSURL)
    NSURL* urlString =  [NSURL URLWithString:[self.videoAddress stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]]];

    NSLog(@"요청 URL Check %@", [urlString absoluteString]);
    
    // 플레이어 디코더 선택...AVPlayer와 FFmepgDecoder 또는 모두 사용가능하나 FFmpeg을 사용하도록 설정
    // self.player.decoder =  [SGPlayerDecoder decoderByFFmpeg];
    self.player.decoder =  [SGPlayerDecoder decoderByDefault];

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

@end

