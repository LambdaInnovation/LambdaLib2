#include <jni.h>
#include <iostream>
#include "cn_lambdalib2_vis_editor_ImGui.h"

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    Begin
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_Begin
  (JNIEnv * env, jclass, jstring str, jint) {
	auto s = env->GetStringUTFChars(str, nullptr);
	std::cout << "ImGui::Begin: " << s << std::endl;
	env->ReleaseStringUTFChars(str, s);
	return true;
}

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    End
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_End
(JNIEnv *env, jclass) {
	std::cout << "ImGui::End" << std::endl;
}

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    Text
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_Text
(JNIEnv *env, jclass, jstring str) {
	auto s = env->GetStringUTFChars(str, nullptr);
	std::cout << "ImGui::Text: " << s << std::endl;
	env->ReleaseStringUTFChars(str, s);
}