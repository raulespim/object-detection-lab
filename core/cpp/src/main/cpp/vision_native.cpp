#include <jni.h>
#include <android/log.h>
#include <android/bitmap.h>

// Tag para vermos o log do C++ no Logcat do Android
#define LOG_TAG "VisionNative"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT void JNICALL
Java_com_espimsystems_visionlab_core_cpp_NativeImageProcessor_processFrameNative(
        JNIEnv *env,
jobject thiz,
        jobject y_buffer,
jint width,
        jint height
) {
// 1. Aqui o C++ recebe o buffer de memória do Kotlin
// 2. Por enquanto, apenas logamos para confirmar que a ponte funcionou
LOGD("C++: Recebi um frame de %dx%d via NDK!", width, height);

// No futuro, faremos a conversão YUV -> RGB ultra rápida aqui
}

}
