#include <jni.h>
#include "crypto.h"

static Crypto crypto;

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_weewa_lib_Crypto_encrypt(JNIEnv *env, jobject thiz, jbyteArray data) {
    // java byte array to ByteArray class
    jbyte* b = env->GetByteArrayElements(data, nullptr);
    jint len = env->GetArrayLength(data);
    ByteArray cb(len, 0);
    memcpy(cb.data(), b, len);

    // release
    env->ReleaseByteArrayElements(data, b, JNI_ABORT);

    // encrypt
    jbyteArray ret = nullptr;
    try {
        ByteArray eb = crypto.encrypt(cb);

        // to java byte array
        ret = env->NewByteArray(eb.size());
        env->SetByteArrayRegion(ret, 0, eb.size(), (const jbyte*)eb.data());
    } catch (const std::exception &e) {
        jclass ex = env->FindClass("java/lang/Exception");
        env->ThrowNew(ex, e.what());
    }

    // return
    return ret;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_weewa_lib_Crypto_decrypt(JNIEnv *env, jobject thiz, jbyteArray data) {
    // java byte array to ByteArray class
    jbyte* b = env->GetByteArrayElements(data, nullptr);
    jint len = env->GetArrayLength(data);
    ByteArray cb(len, 0);
    memcpy(cb.data(), b, len);

    // release
    env->ReleaseByteArrayElements(data, b, JNI_ABORT);

    // decrypt
    jbyteArray ret = nullptr;
    try {
        ByteArray eb = crypto.decrypt(cb);

        // to java byte array
        ret = env->NewByteArray(eb.size());
        env->SetByteArrayRegion(ret, 0, eb.size(), (const jbyte*)eb.data());
    } catch (const std::exception &e) {
        jclass ex = env->FindClass("java/lang/Exception");
        env->ThrowNew(ex, e.what());
    }

    // return
    return ret;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_weewa_lib_Crypto_localPublicKey(JNIEnv *env, jobject thiz) {
    ByteArray eb = crypto.localPublicKey();
    jbyteArray ret = env->NewByteArray(eb.size());
    env->SetByteArrayRegion(ret, 0, eb.size(), (const jbyte*)eb.data());
    return ret;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_weewa_lib_Crypto_publicKeySize(JNIEnv *env, jobject thiz) {
    return crypto.publicKeySize();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_weewa_lib_Crypto_setRemotePublicKey(JNIEnv *env, jobject thiz, jbyteArray key) {
    // java byte array to ByteArray class
    jbyte* b = env->GetByteArrayElements(key, nullptr);
    jint len = env->GetArrayLength(key);
    ByteArray cb(len, 0);
    memcpy(cb.data(), b, len);

    // set
    try {
        crypto.setRemotePublicKey(cb);
    } catch (const std::exception &e) {
        jclass ex = env->FindClass("java/lang/Exception");
        env->ThrowNew(ex, e.what());
    }

    // release
    env->ReleaseByteArrayElements(key, b, JNI_ABORT);
}