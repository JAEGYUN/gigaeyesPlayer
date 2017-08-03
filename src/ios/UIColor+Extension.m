//
//  UIColor+Extension.m
//  VsaasMobile
//
//  Created by civan on 2017. 8. 3..
//
//

#import "UIColor+Extension.h"

@implementation UIColor (Extension)
+(UIColor *)colorWithRGBHex:(NSUInteger)RGBHex{
    CGFloat red = ((CGFloat)((RGBHex & 0xFF0000)>>16))/255.0f;
    CGFloat green = ((CGFloat)((RGBHex & 0xFF00)>>16))/255.0f;
    CGFloat blue = ((CGFloat)((RGBHex & 0xFF)>>16))/255.0f;
    
    return [UIColor colorWithRed:red green:green blue:blue alpha:1.0f];
}

+(UIColor *)colorWithRGBHex:(NSUInteger)RGBHex alpha:(CGFloat)alpha{
    CGFloat red = ((CGFloat)((RGBHex & 0xFF0000)>>16))/255.0f;
    CGFloat green = ((CGFloat)((RGBHex & 0xFF00)>>16))/255.0f;
    CGFloat blue = ((CGFloat)((RGBHex & 0xFF)>>16))/255.0f;
    
    return [UIColor colorWithRed:red green:green blue:blue alpha:alpha];
}

@end
