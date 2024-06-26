/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_gorges_bot_NTgCalls */

#ifndef _Included_com_gorges_bot_NTgCalls
#define _Included_com_gorges_bot_NTgCalls
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_gorges_bot_NTgCalls
 * Method:    init
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_gorges_bot_NTgCalls_init
  (JNIEnv *, jobject);

/*
 * Class:     com_gorges_bot_NTgCalls
 * Method:    getParams
 * Signature: (IJLcom/gorges/bot/NTgCalls/Media;[BI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_gorges_bot_NTgCalls_getParams
  (JNIEnv *, jobject, jint, jlong, jobject, jbyteArray, jint);

/*
 * Class:     com_gorges_bot_NTgCalls
 * Method:    connect
 * Signature: (IJ[B)I
 */
JNIEXPORT jint JNICALL Java_com_gorges_bot_NTgCalls_connect
  (JNIEnv *, jobject, jint, jlong, jbyteArray);

/*
 * Class:     com_gorges_bot_NTgCalls
 * Method:    destroy
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_gorges_bot_NTgCalls_destroy
  (JNIEnv *, jobject, jint);

/*
 * Class:     com_gorges_bot_NTgCalls
 * Method:    time
 * Signature: (IJ)J
 */
JNIEXPORT jlong JNICALL Java_com_gorges_bot_NTgCalls_time
  (JNIEnv *, jobject, jint, jlong);

/*
 * Class:     com_gorges_bot_NTgCalls
 * Method:    pause
 * Signature: (IJ)I
 */
JNIEXPORT jint JNICALL Java_com_gorges_bot_NTgCalls_pause
  (JNIEnv *, jobject, jint, jlong);

/*
 * Class:     com_gorges_bot_NTgCalls
 * Method:    resume
 * Signature: (IJ)I
 */
JNIEXPORT jint JNICALL Java_com_gorges_bot_NTgCalls_resume
  (JNIEnv *, jobject, jint, jlong);

/*
 * Class:     com_gorges_bot_NTgCalls
 * Method:    getVersion
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_gorges_bot_NTgCalls_getVersion
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
