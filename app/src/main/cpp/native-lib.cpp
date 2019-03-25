#include <jni.h>


extern "C" JNIEXPORT jstring JNICALL
Java_cc_mil_cnt_cream_1sauce_1smoked_1chicken_1spaghetti_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    return env->NewStringUTF("");
}