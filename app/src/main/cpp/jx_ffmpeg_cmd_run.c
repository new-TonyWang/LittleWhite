#include <jni.h>
#include "ffmpeg.h"
#include "android_log.h"
 //ANDROID

JNIEXPORT jint JNICALL
Java_com_littlewhite_SendFile_FFMPEGHandler_ffmpegRun(JNIEnv *env, jobject instance,
                                                        jobjectArray cmd) {
    int argc = (*env)->GetArrayLength(env, cmd);//长度
    char *chars = NULL;
    char *argv[argc];
    int i;
    for (i = 0; i < argc; i++) {
        jstring js = (jstring) (*env)->GetObjectArrayElement(env, cmd, i);
        argv[i] = (char *) (*env)->GetStringUTFChars(env, js, 0);
    }
    LOGD("----------begin---------");

    return jxRun(argc, argv);

    // TODO

}

