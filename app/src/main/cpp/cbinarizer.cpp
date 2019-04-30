#include <jni.h>
#include <libyuv.h>
#include "CBinarize.h"

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_com_google_zxing_common_CBinarizer_calculateBlackPointsfromC(JNIEnv *env, jobject instance,
                                                                  jbyteArray luminances_,
                                                                  jint subWidth, jint subHeight,
                                                                  jint width, jint height) {
    uint8_t *luminances = (uint8_t *) env->GetByteArrayElements( luminances_, NULL);
    jobjectArray blackpoints = calculateBlackPoints(env,luminances,subWidth,subHeight,width, height);


    env->ReleaseByteArrayElements( luminances_, (jbyte *) luminances, 0);
    return blackpoints;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_google_zxing_common_CBinarizer_convertToRGB(JNIEnv *env, jobject instance,
                                                     jbyteArray luminances_, jbyteArray uv_,
                                                     jbyteArray ARGB_, jint width, jint height) {

    uint8_t *ARGB = (uint8_t *) env->GetByteArrayElements( ARGB_, NULL);
    uint8_t *luminances = (uint8_t *) env->GetByteArrayElements( luminances_, NULL);
    uint8_t *uv = (uint8_t *) env->GetByteArrayElements( uv_, NULL);
    //uint8_t *ARGB = (uint8_t*)malloc(sizeof(uint8_t*)*width*height);
    //NV12ToABGR(luminances,width,uv,width,ARGB,width*4,width,height);
    TEST(width,height, ARGB);
    // TODO

    env->ReleaseByteArrayElements( luminances_, (jbyte *) luminances, 0);
    env->ReleaseByteArrayElements( uv_, (jbyte *) uv, 0);

    env->ReleaseByteArrayElements( ARGB_, (jbyte *) ARGB, 0);
    return  1;
}
jobjectArray calculateBlackPoints(JNIEnv *env,
        uint8_t *luminances,
                           int subWidth,
                           int subHeight,
                           int width,
                           int height){
    int maxYOffset = height - BLOCK_SIZE;
    int maxXOffset = width - BLOCK_SIZE;
    jobjectArray blackpoints;
    jclass intArrCls = env->FindClass("[I");
    blackpoints = env->NewObjectArray(subHeight,intArrCls,NULL);//行
    int **blackPoints = (int**)malloc(sizeof(int*)*subHeight); //开辟行
    for (int i = 0; i < subHeight; i++)
    {
        *(blackPoints + i) = (int*)malloc(sizeof(int)*subWidth);//开辟列
    }
    for (int y = 0; y < subHeight; y++) {
        int yoffset = y << BLOCK_SIZE_POWER;
        if (yoffset > maxYOffset) {
            yoffset = maxYOffset;
        }
        for (int x = 0; x < subWidth; x++) {
            int xoffset = x << BLOCK_SIZE_POWER;
            if (xoffset > maxXOffset) {
                xoffset = maxXOffset;
            }
            int sum = 0;
            int min = 0xFF;
            int max = 0;
            int averageNeighborBlackPoint = 0;
            int pixel = 0;
            int average = 0;
            for (int yy = 0, offset = yoffset * width + xoffset; yy < BLOCK_SIZE; yy++, offset += width) {
                for (int xx = 0; xx < BLOCK_SIZE; xx++) {
                    pixel = luminances[offset + xx] & 0xFF;
                    sum += pixel;
                    // still looking for good contrast
                    if (pixel < min) {
                        min = pixel;
                    }
                    if (pixel > max) {
                        max = pixel;
                    }
                }
                // short-circuit min/max tests once dynamic range is met
                if (max - min > MIN_DYNAMIC_RANGE) {
                    // finish the rest of the rows quickly
                    for (yy++, offset += width; yy < BLOCK_SIZE; yy++, offset += width) {
                        for (int xx = 0; xx < BLOCK_SIZE; xx++) {
                            sum += luminances[offset + xx] & 0xFF;
                        }
                    }
                }
            }

            // The default estimate is the average of the values in the block.
            average = sum >> (BLOCK_SIZE_POWER * 2);
            if (max - min <= MIN_DYNAMIC_RANGE) {
                // If variation within the block is low, assume this is a block with only light or only
                // dark pixels. In that case we do not want to use the average, as it would divide this
                // low contrast area into black and white pixels, essentially creating data out of noise.
                //
                // The default assumption is that the block is light/background. Since no estimate for
                // the level of dark pixels exists locally, use half the min for the block.
                average = min / 2;

                if (y > 0 && x > 0) {
                    // Correct the "white background" assumption for blocks that have neighbors by comparing
                    // the pixels in this block to the previously calculated black points. This is based on
                    // the fact that dark barcode symbology is always surrounded by some amount of light
                    // background for which reasonable black point estimates were made. The bp estimated at
                    // the boundaries is used for the interior.

                    // The (min < bp) is arbitrary but works better than other heuristics that were tried.
                    averageNeighborBlackPoint =
                            (blackPoints[y - 1][x] + (2 * blackPoints[y][x - 1]) + blackPoints[y - 1][x - 1]) / 4;
                    if (min < averageNeighborBlackPoint) {
                        average = averageNeighborBlackPoint;
                    }
                }
            }
            blackPoints[y][x] = average;
        }
    }
    jint tmp[subWidth];
    for(int y = 0;y<subHeight;y++){
        jintArray iarr = env->NewIntArray(subWidth);
        for(int x = 0;x<subWidth;x++){
            tmp[x] = blackPoints[y][x];
        }
        env->SetIntArrayRegion(iarr,0,subWidth,tmp);
        env->SetObjectArrayElement(blackpoints,y,iarr);
        env->DeleteLocalRef(iarr);
        delete(*(blackPoints + y));
        *(blackPoints + y) =   NULL;
    }
       delete(blackPoints);
    blackPoints = NULL;
    return blackpoints;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_google_zxing_common_CBinarizer_convertToHSV(JNIEnv *env, jobject instance,
                                                     jbyteArray luminances_, jbyteArray uv_,
                                                     jbyteArray H_, jbyteArray S_, jbyteArray V_,
                                                     jint width, jint height) {
    uint8_t *luminances =(uint8_t *) env->GetByteArrayElements(luminances_, NULL);
    uint8_t *uv = (uint8_t *)env->GetByteArrayElements(uv_, NULL);
   // uint8_t *H = (uint8_t *)env->GetByteArrayElements(H_, NULL);
   // uint8_t *S = (uint8_t *)env->GetByteArrayElements(S_, NULL);
   // uint8_t *V = (uint8_t *)env->GetByteArrayElements(V_, NULL);
    uint8_t *ARGB = new uint8_t[width*height*4];
    libyuv::NV12ToABGR(luminances,width,uv,width,ARGB,width*4,width,height);
  //  getHSVchnnels(width,height,ARGB,H,S,V);
    // TODO
    cv::Mat img(height,width,CV_8UC4,ARGB);
    cv::Mat hsv;
    Mat out;
    cv::cvtColor(img,hsv,cv::COLOR_BGRA2BGR);
    //cv::imwrite("sdcard/test.jpg",hsv);
    cv::cvtColor(hsv,img,cv::COLOR_BGR2HSV);
    std::vector<cv::Mat> channels;
    split(img,channels);
    //H = channels[0].data;
    //S = channels[1].data;
    //V = channels[2].data;
    jsize cwidth = (jsize)channels[0].cols;
    jsize cheght = (jsize)channels[0].rows;
    jsize length = cwidth*cheght;
    //cv::imwrite("sdcard/test.jpg",hsv);
    free(ARGB);
    env->ReleaseByteArrayElements(luminances_, (jbyte *)luminances, 0);
    env->ReleaseByteArrayElements(uv_,(jbyte *) uv, 0);
    env->SetByteArrayRegion(H_, 0, length, (jbyte *)channels[0].data);
    env->SetByteArrayRegion(S_,0, length,(jbyte *)channels[1].data);
    env->SetByteArrayRegion(V_,0, length,(jbyte *)channels[2].data);
   // cv::imwrite("sdcard/test.jpg",img);
    //env->ReleaseByteArrayElements(H_,(jbyte *) H, 0);
   // env->ReleaseByteArrayElements(S_,(jbyte *) S, 0);
  //  env->ReleaseByteArrayElements(V_,(jbyte *) V, 0);
    return 1;
}