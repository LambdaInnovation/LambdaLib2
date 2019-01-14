/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class cn_lambdalib2_vis_editor_ImGui */

#ifndef _Included_cn_lambdalib2_vis_editor_ImGui
#define _Included_cn_lambdalib2_vis_editor_ImGui
#ifdef __cplusplus
extern "C" {
#endif
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_None
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_None 0L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoTitleBar
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoTitleBar 1L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoResize
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoResize 2L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoMove
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoMove 4L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoScrollbar
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoScrollbar 8L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoScrollWithMouse
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoScrollWithMouse 16L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoCollapse
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoCollapse 32L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_AlwaysAutoResize
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_AlwaysAutoResize 64L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoBackground
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoBackground 128L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoSavedSettings
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoSavedSettings 256L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoMouseInputs
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoMouseInputs 512L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_MenuBar
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_MenuBar 1024L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_HorizontalScrollbar
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_HorizontalScrollbar 2048L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoFocusOnAppearing
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoFocusOnAppearing 4096L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoBringToFrontOnFocus
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoBringToFrontOnFocus 8192L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_AlwaysVerticalScrollbar
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_AlwaysVerticalScrollbar 16384L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_AlwaysHorizontalScrollbar
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_AlwaysHorizontalScrollbar 32768L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_AlwaysUseWindowPadding
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_AlwaysUseWindowPadding 65536L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoNavInputs
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoNavInputs 262144L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoNavFocus
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoNavFocus 524288L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_UnsavedDocument
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_UnsavedDocument 1048576L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoNav
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoNav 786432L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoDecoration
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoDecoration 43L
#undef cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoInputs
#define cn_lambdalib2_vis_editor_ImGui_WindowFlags_NoInputs 786944L
/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nBegin2
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBegin2
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nBegin
 * Signature: (Ljava/lang/String;ZI)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBegin
  (JNIEnv *, jclass, jstring, jboolean, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nEnd
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nEnd
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nBeginChild
 * Signature: (Ljava/lang/String;FFZI)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBeginChild
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jboolean, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nEndChild
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nEndChild
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nSeparator
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSeparator
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nSameLine
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSameLine
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nNewLine
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nNewLine
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nSpacing
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSpacing
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nBeginGroup
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBeginGroup
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nEndGroup
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nEndGroup
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nPushItemWidth
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nPushItemWidth
  (JNIEnv *, jclass, jfloat);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nPopItemWidth
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nPopItemWidth
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nSetCursorPosX
 * Signature: (F)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSetCursorPosX
  (JNIEnv *, jclass, jfloat);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nGetCursorPos
 * Signature: ()[F
 */
JNIEXPORT jfloatArray JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nGetCursorPos
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nPushID
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nPushID
  (JNIEnv *, jclass, jstring);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nPushID2
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nPushID2
  (JNIEnv *, jclass, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nPopID
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nPopID
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nText
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nText
  (JNIEnv *, jclass, jstring);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nTextColored
 * Signature: (ILjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nTextColored
  (JNIEnv *, jclass, jint, jstring);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nTextWrapped
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nTextWrapped
  (JNIEnv *, jclass, jstring);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nLabelText
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nLabelText
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nBulletText
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBulletText
  (JNIEnv *, jclass, jstring);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nButton
 * Signature: (Ljava/lang/String;FF)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nButton
  (JNIEnv *, jclass, jstring, jfloat, jfloat);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nArrowButton
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nArrowButton
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nImage
 * Signature: (IFFFFFFII)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nImage
  (JNIEnv *, jclass, jint, jfloat, jfloat, jfloat, jfloat, jfloat, jfloat, jint, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nImageButton
 * Signature: (IFFFFFFIII)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nImageButton
  (JNIEnv *, jclass, jint, jfloat, jfloat, jfloat, jfloat, jfloat, jfloat, jint, jint, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nCheckbox
 * Signature: (Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nCheckbox
  (JNIEnv *, jclass, jstring, jboolean);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nRadioButton
 * Signature: (Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nRadioButton
  (JNIEnv *, jclass, jstring, jboolean);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nBullet
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBullet
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nBeginCombo
 * Signature: (Ljava/lang/String;Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBeginCombo
  (JNIEnv *, jclass, jstring, jstring, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nEndCombo
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nEndCombo
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nCombo
 * Signature: (Ljava/lang/String;I[Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nCombo
  (JNIEnv *, jclass, jstring, jint, jobjectArray);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nSliderFloat
 * Signature: (Ljava/lang/String;FFFLjava/lang/String;F)F
 */
JNIEXPORT jfloat JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSliderFloat
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jfloat, jstring, jfloat);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nSliderFloat2
 * Signature: (Ljava/lang/String;[FFFLjava/lang/String;F)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSliderFloat2
  (JNIEnv *, jclass, jstring, jfloatArray, jfloat, jfloat, jstring, jfloat);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nSliderFloat3
 * Signature: (Ljava/lang/String;[FFFLjava/lang/String;F)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSliderFloat3
  (JNIEnv *, jclass, jstring, jfloatArray, jfloat, jfloat, jstring, jfloat);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nSliderFloat4
 * Signature: (Ljava/lang/String;[FFFLjava/lang/String;F)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSliderFloat4
  (JNIEnv *, jclass, jstring, jfloatArray, jfloat, jfloat, jstring, jfloat);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nSliderAngle
 * Signature: (Ljava/lang/String;FFFLjava/lang/String;)F
 */
JNIEXPORT jfloat JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSliderAngle
  (JNIEnv *, jclass, jstring, jfloat, jfloat, jfloat, jstring);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nSliderInt
 * Signature: (Ljava/lang/String;III)I
 */
JNIEXPORT jint JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSliderInt
  (JNIEnv *, jclass, jstring, jint, jint, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputText
 * Signature: (Ljava/lang/String;Ljava/lang/String;I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputText
  (JNIEnv *, jclass, jstring, jstring, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputTextMultiline
 * Signature: (Ljava/lang/String;Ljava/lang/String;FFI)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputTextMultiline
  (JNIEnv *, jclass, jstring, jstring, jfloat, jfloat, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputFloat
 * Signature: (Ljava/lang/String;FLjava/lang/String;I)F
 */
JNIEXPORT jfloat JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputFloat
  (JNIEnv *, jclass, jstring, jfloat, jstring, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputFloat2
 * Signature: (Ljava/lang/String;[FLjava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputFloat2
  (JNIEnv *, jclass, jstring, jfloatArray, jstring, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputFloat3
 * Signature: (Ljava/lang/String;[FLjava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputFloat3
  (JNIEnv *, jclass, jstring, jfloatArray, jstring, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputFloat4
 * Signature: (Ljava/lang/String;[FLjava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputFloat4
  (JNIEnv *, jclass, jstring, jfloatArray, jstring, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputInt
 * Signature: (Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputInt
  (JNIEnv *, jclass, jstring, jint, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputInt2
 * Signature: (Ljava/lang/String;[II)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputInt2
  (JNIEnv *, jclass, jstring, jintArray, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputInt3
 * Signature: (Ljava/lang/String;[II)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputInt3
  (JNIEnv *, jclass, jstring, jintArray, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputInt4
 * Signature: (Ljava/lang/String;[II)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputInt4
  (JNIEnv *, jclass, jstring, jintArray, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputDouble
 * Signature: (Ljava/lang/String;DI)D
 */
JNIEXPORT jdouble JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputDouble
  (JNIEnv *, jclass, jstring, jdouble, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nColorEdit4
 * Signature: (Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nColorEdit4
  (JNIEnv *, jclass, jstring, jint, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nColorButton
 * Signature: (Ljava/lang/String;IIFF)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nColorButton
  (JNIEnv *, jclass, jstring, jint, jint, jfloat, jfloat);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nTreeNode
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nTreeNode
  (JNIEnv *, jclass, jstring);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nTreeNodeEx
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nTreeNodeEx
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nTreePop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nTreePop
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nCollapsingHeader
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nCollapsingHeader
  (JNIEnv *, jclass, jstring, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nBeginMainMenuBar
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBeginMainMenuBar
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nEndMainMenuBar
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nEndMainMenuBar
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nBeginMenuBar
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBeginMenuBar
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nBeginMenu
 * Signature: (Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBeginMenu
  (JNIEnv *, jclass, jstring, jboolean);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nEndMenu
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nEndMenu
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nEndMenuBar
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nEndMenuBar
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nMenuItem
 * Signature: (Ljava/lang/String;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nMenuItem
  (JNIEnv *, jclass, jstring, jboolean);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nMenuItem2
 * Signature: (Ljava/lang/String;Lcn/lambdalib2/vis/editor/ImBoolRef;Z)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nMenuItem2
  (JNIEnv *, jclass, jstring, jobject, jboolean);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nGetWindowRect
 * Signature: ()[F
 */
JNIEXPORT jfloatArray JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nGetWindowRect
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nIsItemClicked
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nIsItemClicked
  (JNIEnv *, jclass, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nAddUserCallback
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nAddUserCallback
  (JNIEnv *, jclass, jint);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nCreateContext
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nCreateContext
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nShowDemoWindow
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nShowDemoWindow
  (JNIEnv *, jclass, jboolean);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nFillInput
 * Signature: (FFFFF[ZFZZZZ[Z[C)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nFillInput
  (JNIEnv *, jclass, jfloat, jfloat, jfloat, jfloat, jfloat, jbooleanArray, jfloat, jboolean, jboolean, jboolean, jboolean, jbooleanArray, jcharArray);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nNewFrame
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nNewFrame
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nRender
 * Signature: ()Lcn/lambdalib2/vis/editor/ImDrawData;
 */
JNIEXPORT jobject JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nRender
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nGetFontTexARGB32
 * Signature: ()Lcn/lambdalib2/vis/editor/ImFontTex;
 */
JNIEXPORT jobject JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nGetFontTexARGB32
  (JNIEnv *, jclass);

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nSetFontTexID
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSetFontTexID
  (JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif
