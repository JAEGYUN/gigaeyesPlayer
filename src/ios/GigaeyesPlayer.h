#import <Cordova/CDVPlugin.h>
#import <Cordova/CDV.h>
#import <Foundation/Foundation.h>
#import "PlayerViewController.h"

@interface GigaeyesPlayer : CDVPlugin

- (void) watch : (CDVInvokedUrlCommand*) command;
- (void) finishOkAndDismiss;

@property (strong,nonatomic) CDVInvokedUrlCommand* lastCommand;
@property (strong,nonatomic) PlayerViewController* overlay;
@property (readwrite, assign) BOOL hasPendingOperation;

@end
