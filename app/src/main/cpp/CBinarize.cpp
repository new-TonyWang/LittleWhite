#include <cstdint>
#include <cstdlib>
#include "CBinarize.h"
#include "libyuv.h"
#include "opencv2/objdetect.hpp"
using namespace libyuv;

//
//
//
int TEST(int width,int height,uint8_t *RGB){
    cv::Mat img(height,width,CV_8UC4,RGB);
    cv::Mat hsv;
    Mat out;
    cv::cvtColor(img,hsv,cv::COLOR_BGRA2BGR);
    Mat point;
    //cv::QRCodeDetector QRdetecter;
     //QRdetecter.detect(hsv,point);
    cv::cvtColor(hsv,img,cv::COLOR_BGR2HSV);
   //hsvmask(hsv, 0,120,25,255);
   // colorDetection(img,width,height);
    cv::cvtColor(img,out,cv::COLOR_HSV2BGR);
    cv::imwrite("sdcard/test.jpg",out);
    return 1;
}
void getHSVchnnels(int width,int height,uint8_t *RGB,uint8_t* H,uint8_t* S,uint8_t* V){
    cv::Mat img(height,width,CV_8UC4,RGB);
    cv::Mat hsv;
    Mat out;
    cv::cvtColor(img,hsv,cv::COLOR_BGRA2BGR);
   //cv::imwrite("sdcard/test.jpg",hsv);
    cv::cvtColor(hsv,img,cv::COLOR_BGR2HSV);
    std::vector<cv::Mat> channels;
    split(hsv,channels);
    H = channels[0].data;
    S = channels[1].data;
    V = channels[2].data;
}
void hsvmask(cv::Mat hsv,double minH,double maxH,double minS,double maxS){
    std::vector<cv::Mat> channels;
    split(hsv,channels);
    uint8_t* H = NULL;
            H = channels[0].data;
    //channel[0] H
    //channel[1] S
    //channel[2] V
    Mat mask1;
    threshold(channels[0],mask1,maxH,255,THRESH_BINARY_INV);
    Mat mask2;
    threshold(channels[0],mask2,minH,255,THRESH_BINARY);
    Mat huemask;
    if(minH<maxH){
        huemask = mask1&mask2;
    }
    else{
        huemask = mask1 | mask2;
    }
    threshold(channels[1],mask1,maxS,255,THRESH_BINARY_INV);
    threshold(channels[1],mask2,minS,255,THRESH_BINARY);
    Mat sMask;
    sMask = mask1&mask2;
    hsv = huemask&sMask;
}
void colorDetection(Mat hsv,int width,int height){
    std::vector<cv::Mat> channels;
    split(hsv,channels);
    int maxYOffset = height-COLOR_DETECT_BLOCK;
    int maxXOffset = width - COLOR_DETECT_BLOCK;
    int subHeight = height >> COLOR_DETECT_POWER;
    int subWidth = width >> COLOR_DETECT_POWER;
    int yoffset = 0;
    int xoffset = 0;
    int sumH = 0;
    int sumS = 0;
    int sumV = 0;
    int vmin = 0xFF;
    int vmax = 0;
    int V = 0;
    uint8_t avgH =0;
    uint8_t avgS =0;
    uint8_t avgV =0;
    for(int y = 0 ;y<subHeight;y++){
      yoffset = y << COLOR_DETECT_POWER;
        if (yoffset > maxYOffset) {
            yoffset = maxYOffset;
        }
        for(int x = 0; x < subWidth; x++){
            xoffset  = x << COLOR_DETECT_POWER;
            if (xoffset > maxXOffset) {
                xoffset = maxXOffset;
            }
            for (int yy = 0, offset = yoffset * width + xoffset; yy < COLOR_DETECT_BLOCK; yy++, offset +=  width) {
                for (int xx = 0; xx < COLOR_DETECT_BLOCK; xx++) {

                    sumH +=   *(channels[0].data+offset+xx);
                    // still looking for good contrast
                    sumS+=*(channels[1].data+offset+xx);
                    // v = channels[2].data[offset+xx];
                    sumV+=*(channels[2].data+offset+xx);

                }
                // short-circuit min/max tests once dynamic range is met
            }
             //avgH =(uint8_t)(sumH>>COLOR_DETECT_BLOCK);
             //avgS =(uint8_t)(sumS>>COLOR_DETECT_BLOCK);
             //avgV =(uint8_t)(sumV>>COLOR_DETECT_BLOCK);
             avgH =(uint8_t)sumH;
            avgS =(uint8_t)sumS;
            avgV =(uint8_t)sumV;
            sumH = sumS = sumV = 0;


                 if(((avgH>=0&&avgH<=14)||(avgH>=159&&avgH<=180))

                        &&(avgS>=43&&avgS<=255)

                        &&(avgV>=46&&avgV<=255)){

                    for (int yy = 0, offset = yoffset * hsv.step[0] + xoffset*hsv.step[1]; yy < COLOR_DETECT_BLOCK; yy++, offset +=  hsv.step[0]) {
                        for (int xx = 0; xx < COLOR_DETECT_BLOCK; xx++) {
                            *(hsv.data+offset+xx*hsv.step[1]) =(uint8_t)avgH;
                            *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()) =(uint8_t)avgS;
                            *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()*2) =(uint8_t)avgV;
                        }
                        // short-circuit min/max tests once dynamic range is met(
                    }

                   // cout<<"红"<<endl;



                }


                else if((avgH>=15&&avgH<=45)

                        &&(avgS>=43&&avgS<=255)

                        &&(avgV>=46&&avgV<=255)){
                    for (int yy = 0, offset = yoffset * hsv.step[0] + xoffset*hsv.step[1]; yy < COLOR_DETECT_BLOCK; yy++, offset +=  hsv.step[0]) {
                        for (int xx = 0; xx < COLOR_DETECT_BLOCK; xx++) {
                            *(hsv.data+offset+xx*hsv.step[1]) =(uint8_t)avgH;
                            *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()) =(uint8_t)avgS;
                            *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()*2) =(uint8_t)avgV;
                        }
                        // short-circuit min/max tests once dynamic range is met(
                    }


                    //cout<<"黄"<<endl;

                }

                else if((avgH>=46&&avgH<=77)

                        &&(avgS>=43&&avgS<=255)

                        &&(avgV>=46&&avgV<=255)){

                    for (int yy = 0, offset = yoffset * hsv.step[0] + xoffset*hsv.step[1]; yy < COLOR_DETECT_BLOCK; yy++, offset +=  hsv.step[0]) {
                        for (int xx = 0; xx < COLOR_DETECT_BLOCK; xx++) {
                            *(hsv.data+offset+xx*hsv.step[1]) =(uint8_t)0;
                            *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()) =(uint8_t)0;
                            *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()*2) =(uint8_t)255;
                        }
                        // short-circuit min/max tests once dynamic range is met(
                    }

                   // cout<<"绿"<<endl;

                }

                else if((avgH>=78&&avgH<=109)

                        &&(avgS>=43&&avgS<=255)

                        &&(avgV>=46&&avgV<=255)){


                    for (int yy = 0, offset = yoffset * hsv.step[0] + xoffset*hsv.step[1]; yy < COLOR_DETECT_BLOCK; yy++, offset +=  hsv.step[0]) {
                        for (int xx = 0; xx < COLOR_DETECT_BLOCK; xx++) {
                            *(hsv.data+offset+xx*hsv.step[1]) =(uint8_t)0;
                            *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()) =(uint8_t)0;
                            *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()*2) =(uint8_t)255;
                        }
                        // short-circuit min/max tests once dynamic range is met(
                    }
                   // cout<<"青"<<endl;

                }

                else if((avgH>=110&&avgH<=134)

                        &&(avgS>=43&&avgS<=255)

                        &&(avgV>=46&&avgV<=255)){

                    for (int yy = 0, offset = yoffset * hsv.step[0] + xoffset*hsv.step[1]; yy < COLOR_DETECT_BLOCK; yy++, offset +=  hsv.step[0]) {
                        for (int xx = 0; xx < COLOR_DETECT_BLOCK; xx++) {
                            *(hsv.data+offset+xx*hsv.step[1]) =(uint8_t)0;
                            *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()) =(uint8_t)0;
                            *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()*2) =(uint8_t)255;
                        }
                        // short-circuit min/max tests once dynamic range is met(
                    }

                    //cout<<"蓝"<<endl;

                }

                else if((avgH>=135&&avgH<=160)

                        &&(avgS>=43&&avgS<=255)

                        &&(avgV>=46&&avgV<=255)){

                    for (int yy = 0, offset = yoffset * hsv.step[0] + xoffset*hsv.step[1]; yy < COLOR_DETECT_BLOCK; yy++, offset +=  hsv.step[0]) {
                        for (int xx = 0; xx < COLOR_DETECT_BLOCK; xx++) {
                            *(hsv.data+offset+xx*hsv.step[1]) =(uint8_t)avgH;
                            *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()) =(uint8_t)avgS;
                            *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()*2) =(uint8_t)avgV;
                        }
                        // short-circuit min/max tests once dynamic range is met(
                    }

                   // cout<<"品红"<<endl;

                }
             else if((avgH>=0&&avgH<=180)

               &&(avgS>=0&&avgS<=255)

               &&(avgV>=0&&avgV<=149)){
                for (int yy = 0, offset = yoffset * hsv.step[0] + xoffset*hsv.step[1]; yy < COLOR_DETECT_BLOCK; yy++, offset +=  hsv.step[0]) {
                    for (int xx = 0; xx < COLOR_DETECT_BLOCK; xx++) {
                        *(hsv.data+offset+xx*hsv.step[1]) =(uint8_t)avgH;
                        *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()) =(uint8_t)avgS;
                        *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()*2) =(uint8_t)avgV;
                    }
                    // short-circuit min/max tests once dynamic range is met(
                }


                // cout<<"黑"<<endl;

            }

            else if((avgH>=0&&avgH<=180)

                    &&(avgS>=0&&avgS<=40)

                    &&(avgV>=150&&avgV<=255)){
                for (int yy = 0, offset = yoffset * hsv.step[0] + xoffset*hsv.step[1]; yy < COLOR_DETECT_BLOCK; yy++, offset +=  hsv.step[0]) {
                    for (int xx = 0; xx < COLOR_DETECT_BLOCK; xx++) {
                        *(hsv.data+offset+xx*hsv.step[1]) =(uint8_t)0;
                        *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()) =(uint8_t)0;
                        *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()*2) =(uint8_t)255;
                    }
                    // short-circuit min/max tests once dynamic range is met(
                }
                // cout<<"白"<<endl;

            }

                else{

                    for (int yy = 0, offset = yoffset * hsv.step[0] + xoffset*hsv.step[1]; yy < COLOR_DETECT_BLOCK; yy++, offset +=  hsv.step[0]) {
                        for (int xx = 0; xx < COLOR_DETECT_BLOCK; xx++) {
                            *(hsv.data+offset+xx*hsv.step[1]) =(uint8_t)0;
                            *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()) =(uint8_t)0;
                            *(hsv.data+offset+xx*hsv.step[1]+hsv.elemSize1()*2) =(uint8_t)255;
                        }
                        // short-circuit min/max tests once dynamic range is met(
                    }

                   // cout<<"未知"<<endl;

                }
                getchar();
            }

        }

    }





