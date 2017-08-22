#import "GigaeyesPlayer.h"

@implementation GigaeyesPlayer

-(void) play:(CDVInvokedUrlCommand*) command{
    NSString * playType = @"normal";
    
    //확인 예제 코드 : 웹뷰에서 전달한 URL이 전달되었는지를 확인한다.
    //현재 UIAlertController 가 아닌 deprecated된 UIAlertView를 사용한 것은 interface 내에서 view가 정의되어 있지 않아,
    //UIAlertController 이후에서나 호출가능...테스트 로그용으로만 사용.이후 주석
    //    [[[UIAlertView alloc]initWithTitle:@"ios알림" message: message delegate: nil cancelButtonTitle:@"취소" otherButtonTitles:@"확인", nil] show];
    
    // 메모리로부터 웹뷰가 비워지는 것을 방지하기 위한 옵션.
    self.hasPendingOperation = YES;
    
    // 플러그인(플레이어) 종료시 전달받음.
    self.lastCommand = command;
    
    // 뷰 호출
    self.overlay = [[PlayerViewController alloc] initWithNibName:@"PlayerViewController" bundle:nil];
    
    // 뷰컨트럴러에서 참조할 내용 생성(URL).
    self.overlay.origem = self;
    self.overlay.videoAddress = [command argumentAtIndex:0];
    NSLog(@"Param1 %@",[command argumentAtIndex:0]);
    self.overlay.camId = [command argumentAtIndex:1];
    NSLog(@"Param2 %@",[command argumentAtIndex:1]);
    self.overlay.camName = [command argumentAtIndex:2];
    NSLog(@"Param3 %@",[command argumentAtIndex:2]);
    
    NSArray * roiData = [command argumentAtIndex:3] ;
 
    if(![self isObjectnull : roiData]){
        NSString * sampleStr = [roiData componentsJoinedByString:@" "];
        NSLog(@"Param4 %@", sampleStr);
        self.overlay.roiInfo = roiData;
    }
    
    NSArray * sensorData = [command argumentAtIndex:4] ;
    
    if(![self isObjectnull : sensorData]){
        NSString * sampleStr = [sensorData componentsJoinedByString:@" "];
        NSLog(@"Param5 %@", sampleStr);
        self.overlay.sensorInfo = sensorData;
    }
    
    
    self.overlay.recordStatus = [command argumentAtIndex:5];
    NSLog(@"Param6 %@",[command argumentAtIndex:5]);
    self.overlay.isFavorites = [command argumentAtIndex:6];
    self.overlay.playType = playType;
    
    NSLog(@"Param7 %@",[command argumentAtIndex:6]);
    
    
    //  현재 뷰를 자신으로 활성화
    [self.viewController presentViewController:self.overlay animated:YES completion:nil];
    
}

-(BOOL) isObjectnull:(id )value{
    if([value isEqual:[NSNull null] ] || !value){
        return YES;
    }
    return NO;
}

-(void) finishOkAndDismiss {
    // 실행종료.
    [self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_OK]
                                callbackId:self.lastCommand.callbackId];
    
    // dismiss view from stack
    [self.viewController dismissViewControllerAnimated:YES completion:nil];
    
    // 메모리 반환.
    self.hasPendingOperation = NO;
}


-(void)pluginInitialize {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onPause) name:UIApplicationDidEnterBackgroundNotification object:nil];
}

- (void) onPause {
    NSLog(@"pausou..");
    //    [self.overlay buttonDismissPressed:nil];
}


@end
