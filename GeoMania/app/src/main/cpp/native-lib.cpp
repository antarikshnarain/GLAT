#include <jni.h>
#include <string>

extern "C"
jstring
Java_com_lasser_play_geomania_UserProfile_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
