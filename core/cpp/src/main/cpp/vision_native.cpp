#include <jni.h>
#include <android/log.h>
#include <algorithm>
#include <cstdint>

#define LOG_TAG "vision_native"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {

    inline uint8_t clampToByte(int value) {
        return static_cast<uint8_t>(std::max(0, std::min(255, value)));
    }

    inline uint8_t readPlaneValue(
            const uint8_t* plane,
            int rowStride,
            int pixelStride,
            int x,
            int y
    ) {
        return plane[(y * rowStride) + (x * pixelStride)];
    }

    inline void mapRotatedToSource(
            int rotationDegrees,
            int srcWidth,
            int srcHeight,
            int rotatedX,
            int rotatedY,
            int& outSourceX,
            int& outSourceY
    ) {
        switch (rotationDegrees) {
            case 90:
                outSourceX = rotatedY;
                outSourceY = srcHeight - 1 - rotatedX;
                break;
            case 180:
                outSourceX = srcWidth - 1 - rotatedX;
                outSourceY = srcHeight - 1 - rotatedY;
                break;
            case 270:
                outSourceX = srcWidth - 1 - rotatedY;
                outSourceY = rotatedX;
                break;
            case 0:
            default:
                outSourceX = rotatedX;
                outSourceY = rotatedY;
                break;
        }
    }

    inline void yuvToRgb(
            uint8_t yValue,
            uint8_t uValue,
            uint8_t vValue,
            uint8_t& outR,
            uint8_t& outG,
            uint8_t& outB
    ) {
        const int c = static_cast<int>(yValue) - 16;
        const int d = static_cast<int>(uValue) - 128;
        const int e = static_cast<int>(vValue) - 128;

        const int r = (298 * std::max(c, 0) + 409 * e + 128) >> 8;
        const int g = (298 * std::max(c, 0) - 100 * d - 208 * e + 128) >> 8;
        const int b = (298 * std::max(c, 0) + 516 * d + 128) >> 8;

        outR = clampToByte(r);
        outG = clampToByte(g);
        outB = clampToByte(b);
    }

} // namespace

extern "C"
JNIEXPORT void JNICALL
Java_com_espimsystems_visionlab_core_cpp_NativeImageProcessor_processFrameNative(
        JNIEnv *env,
        jobject /* thiz */,
        jobject y_buffer,
        jint y_row_stride,
        jint y_pixel_stride,
        jobject u_buffer,
        jint u_row_stride,
        jint u_pixel_stride,
        jobject v_buffer,
        jint v_row_stride,
        jint v_pixel_stride,
        jint width,
        jint height,
        jint rotation_degrees,
        jint target_width,
        jint target_height,
        jobject output_buffer
) {
    auto* yPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(y_buffer));
    auto* uPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(u_buffer));
    auto* vPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(v_buffer));
    auto* outPtr = static_cast<uint8_t*>(env->GetDirectBufferAddress(output_buffer));

    if (yPtr == nullptr || uPtr == nullptr || vPtr == nullptr || outPtr == nullptr) {
        LOGE("Falha ao acessar buffers diretos via JNI");
        return;
    }

    const bool swapDimensions = rotation_degrees == 90 || rotation_degrees == 270;
    const int rotatedWidth = swapDimensions ? height : width;
    const int rotatedHeight = swapDimensions ? width : height;

    for (int outY = 0; outY < target_height; ++outY) {
        const int rotatedY = std::min(
                static_cast<int>((static_cast<float>(outY) / target_height) * rotatedHeight),
                rotatedHeight - 1
        );

        for (int outX = 0; outX < target_width; ++outX) {
            const int rotatedX = std::min(
                    static_cast<int>((static_cast<float>(outX) / target_width) * rotatedWidth),
                    rotatedWidth - 1
            );

            int sourceX = 0;
            int sourceY = 0;
            mapRotatedToSource(
                    rotation_degrees,
                    width,
                    height,
                    rotatedX,
                    rotatedY,
                    sourceX,
                    sourceY
            );

            const int chromaX = sourceX / 2;
            const int chromaY = sourceY / 2;

            const uint8_t yValue = readPlaneValue(yPtr, y_row_stride, y_pixel_stride, sourceX, sourceY);
            const uint8_t uValue = readPlaneValue(uPtr, u_row_stride, u_pixel_stride, chromaX, chromaY);
            const uint8_t vValue = readPlaneValue(vPtr, v_row_stride, v_pixel_stride, chromaX, chromaY);

            uint8_t r;
            uint8_t g;
            uint8_t b;
            yuvToRgb(yValue, uValue, vValue, r, g, b);

            const int outIndex = (outY * target_width + outX) * 3;
            outPtr[outIndex] = r;
            outPtr[outIndex + 1] = g;
            outPtr[outIndex + 2] = b;
        }
    }
}