//
// Created by  kkim on 07/04/2020.
//
#include <jni.h>
#include <opencv2/opencv.hpp>

extern "C"
JNIEXPORT void JNICALL
Java_org_gaval_tutorial_HelloOpenCvActivity_ConvertRGBtoGray(JNIEnv *env, jobject thiz,
                                                             jlong mat_addr_input,
                                                             jlong mat_addr_result) {
    // TODO: implement ConvertRGBtoGray()
    cv::Mat &matInput = *(cv::Mat *) mat_addr_input;
    cv::Mat &matResult = *(cv::Mat *) mat_addr_result;

    cv:cvtColor(matInput, matResult, cv::COLOR_RGBA2GRAY);
}