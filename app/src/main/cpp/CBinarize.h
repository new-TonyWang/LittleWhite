//
// Created by 同同 on 2019/4/23.
//

#ifndef LITTLEWHITE_CBINARIZE_H
#define LITTLEWHITE_CBINARIZE_H

#include <opencv.hpp>
#include <imgproc.hpp>
#include <jni.h>

using namespace cv ;
const int BLOCK_SIZE_POWER = 3;
const int BLOCK_SIZE = 1<<BLOCK_SIZE_POWER; // ...0100...00
const int BLOCK_SIZE_MASK =BLOCK_SIZE-1;   // ...0011...11
const int  MINIMUM_DIMENSION = BLOCK_SIZE*5;
const int MIN_DYNAMIC_RANGE = 24;
const int COLOR_DETECT_POWER = 0;
const int COLOR_DETECT_BLOCK = 1<<COLOR_DETECT_POWER;
 int TEST(int wieht,int height,uint8_t *ABGR);
void hsvmask(cv::Mat hsv,double minH,double maxH,double minS,double maxS);
void colorDetection(Mat hsv,int width,int height);
jobjectArray calculateBlackPoints(JNIEnv *env,
                                  uint8_t *luminances,
                                  int subWidth,
                                  int subHeight,
                                  int width,
                                  int height);
void getHSVchnnels(int width,int height,uint8_t *RGB,uint8_t* H,uint8_t* S,uint8_t* V);
#endif //LITTLEWHITE_CBINARIZE_H
