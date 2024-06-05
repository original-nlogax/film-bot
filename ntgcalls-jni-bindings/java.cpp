#include "ntgcalls.h"
#include "com_gorges_bot_NTgCalls.h"

#include <iostream>

// todo implement callbacks 
// https://stackoverflow.com/questions/9630134/jni-how-to-callback-from-c-or-c-to-java
// https://pytgcalls.github.io/NTgCalls/Callback%20Registration

JNIEXPORT jint JNICALL Java_com_gorges_bot_NTgCalls_init
  (JNIEnv *, jobject) {
	int uid = (int)ntg_init();
	return uid;
}

JNIEXPORT jint JNICALL Java_com_gorges_bot_NTgCalls_destroy
  (JNIEnv *, jobject, jint uid) {
	int result = ntg_destroy((int)uid);
	return result;
}

JNIEXPORT jbyteArray JNICALL Java_com_gorges_bot_NTgCalls_getParams
  (JNIEnv * env, jobject, jint uid, jlong chatID, 
  jobject media_desc_object, jbyteArray buffer_array, jint size) {
	// retrieve Media object class
	jclass media_desc_cls = env->GetObjectClass(media_desc_object);


	// get Audio object from Media object
	jfieldID fid_audio_class = env->GetFieldID(
		media_desc_cls, "audio", "Lcom/gorges/bot/NTgCalls$Audio;");
    jobject audio_object = env->GetObjectField(media_desc_object, fid_audio_class);
	jclass cls_audio = env->GetObjectClass(audio_object);

	// get data from Audio object...
	// int inputMode
    jfieldID fid_audio_input_mode = env->GetFieldID(cls_audio, "inputMode", "I");
    jint audio_input_mode = env->GetIntField(audio_object, fid_audio_input_mode);

	// byte[] input
	jfieldID fid_audio_input = env->GetFieldID(cls_audio, "input", "[B");
	jbyteArray audio_input_array = reinterpret_cast<jbyteArray>(env->GetObjectField(audio_object, fid_audio_input));
	jboolean isCopyAudio; // ?
	jbyte* audio_input = env->GetByteArrayElements(audio_input_array, &isCopyAudio);

	// int sampleRate
    jfieldID fid_audio_sample_rate = env->GetFieldID(cls_audio, "sampleRate", "I");
    jint audio_sample_rate = env->GetIntField(audio_object, fid_audio_sample_rate);

	// byte bitsPerSample
    jfieldID fid_audio_bits_per_sample = env->GetFieldID(cls_audio, "bitsPerSample", "B");
    jbyte audio_bits_per_sample = env->GetByteField(audio_object, fid_audio_bits_per_sample);

	// byte channelCount
    jfieldID fid_audio_channel_count = env->GetFieldID(cls_audio, "channelCount", "B");
    jbyte audio_channel_count = env->GetByteField(audio_object, fid_audio_channel_count);

	// create audio struct with data from Audio object
	ntg_audio_description_struct *audio_desc_struct = new ntg_audio_description_struct();
	audio_desc_struct->inputMode = (ntg_input_mode_enum)audio_input_mode;
	audio_desc_struct->input = (char*)audio_input;
	audio_desc_struct->sampleRate = (int)audio_sample_rate;
	audio_desc_struct->bitsPerSample = (uint8_t)audio_bits_per_sample;
	audio_desc_struct->channelCount = (uint8_t)audio_channel_count;


	// get Video object from Media object
	jfieldID fid_video_class = env->GetFieldID(
		media_desc_cls, "video", "Lcom/gorges/bot/NTgCalls$Video;");
    jobject video_object = env->GetObjectField(media_desc_object, fid_video_class);
	jclass cls_video = env->GetObjectClass(video_object);

	// get data from Video object...
	// int inputMode
    jfieldID fid_video_input_mode = env->GetFieldID(cls_video, "inputMode", "I");
    jint video_input_mode = env->GetIntField(video_object, fid_video_input_mode);

	// byte[] input
	jfieldID fid_video_input = env->GetFieldID(cls_video, "input", "[B");
	jbyteArray video_input_array = reinterpret_cast<jbyteArray>(env->GetObjectField(video_object, fid_video_input));
	jboolean isCopyVideo; // ?
	jbyte* video_input = env->GetByteArrayElements(video_input_array, &isCopyVideo);

	// terminate input with null byte (C strings are defined as some bytes, ending with a zero byte)
	
	char* video_input_string = (char*)video_input; // todo remove

	std::cout << "video_input:" << std::endl;
	std::cout << video_input_string << std::endl;

	// int width
    jfieldID fid_video_width = env->GetFieldID(cls_video, "width", "I");
    jint video_width = env->GetIntField(video_object, fid_video_width);
	
	// int height
    jfieldID fid_video_height  = env->GetFieldID(cls_video, "height", "I");
    jint video_height = env->GetIntField(video_object, fid_video_height);

	// byte fps
    jfieldID fid_video_fps = env->GetFieldID(cls_video, "fps", "B");
    jbyte video_fps = env->GetByteField(video_object, fid_video_fps);

	// create video struct with data from Video object
	ntg_video_description_struct *video_desc_struct = new ntg_video_description_struct();
	video_desc_struct->inputMode = (ntg_input_mode_enum)video_input_mode;
	video_desc_struct->input = (char*)video_input;
	video_desc_struct->width = (int)video_width;
	video_desc_struct->height = (int)video_height;
	video_desc_struct->fps = (uint8_t)video_fps;


	ntg_media_description_struct *media_desc_struct = new ntg_media_description_struct();
	media_desc_struct->audio = audio_desc_struct;
	media_desc_struct->video = video_desc_struct;


	jboolean isCopyBuffer; // ?
	int buffer_size = (int)(env->GetArrayLength(buffer_array));
	jbyte *buffer = env->GetByteArrayElements(buffer_array, &isCopyBuffer);

	int result = ntg_get_params((int)uid, (long)chatID, *media_desc_struct, (char*)buffer, buffer_size);

	std::cout << "result:" << std::endl;
	std::cout << result << std::endl;

	// return params
	jbyteArray params = env->NewByteArray(buffer_size);
	env->SetByteArrayRegion(params, 0, buffer_size, buffer);

	return params;
}

JNIEXPORT jint JNICALL Java_com_gorges_bot_NTgCalls_connect
  (JNIEnv * env, jobject, jint uid, jlong chatID, jbyteArray params) {
	//const char *nativeString = env->GetStringUTFChars(params, 0);
	jboolean isCopy; // ?
	int result = ntg_connect((uint32_t)uid, (int64_t)chatID, 
		(char*)(env->GetByteArrayElements(params, &isCopy)));
	return result;
}

JNIEXPORT jlong JNICALL Java_com_gorges_bot_NTgCalls_time
  (JNIEnv * env, jobject, jint uid, jlong chatID) {
    int result = ntg_time((int)uid, (long)chatID);
    return result;
}

JNIEXPORT jint JNICALL Java_com_gorges_bot_NTgCalls_pause
  (JNIEnv *, jobject, jint uid, jlong chatID) {
    int result = ntg_pause((int)uid, (long)chatID);
    return result;
}

JNIEXPORT jint JNICALL Java_com_gorges_bot_NTgCalls_resume
  (JNIEnv *, jobject, jint uid, jlong chatID) {
    int result = ntg_resume((int)uid, (long)chatID);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_gorges_bot_NTgCalls_getVersion
  (JNIEnv * env, jobject) {
	char version[8];
	ntg_get_version(version, sizeof(version));

	return (env)->NewStringUTF((char*) version);
}