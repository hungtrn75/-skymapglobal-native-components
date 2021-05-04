//
//  NSUserDefaults+Helper.h
//  ecoffice
//
//  Created by Suni on 4/13/21.
//

#import <Foundation/Foundation.h>

@interface NSUserDefaults (Helper)

+ (void)setObjectWithKey:(NSObject *)object key:(NSString *)key;
+ (NSObject *)getObjectWithKey:(NSString *)key;

@end
