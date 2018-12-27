#include <jni.h>
#include <iostream>
#include <string>
#include "imgui.h"
#include "cn_lambdalib2_vis_editor_ImGui.h"

static JNIEnv* gJNIEnv;
static jclass gJavaImGuiClass;

// From lwjgl Keyboard.java
const int 
KEY_TAB = 0x0F,
KEY_LEFT = 0xCB,
KEY_RIGHT = 0xCD,
KEY_UP = 0xC8,
KEY_DOWN = 0xD0,
KEY_HOME = 0xC7,
KEY_END = 0xCF,
KEY_INSERT = 0xD2,
KEY_DELETE = 0xD3,
KEY_BKSP = 0x0E,
KEY_SPACE = 0x39,
KEY_ENTER = 0x1C,
KEY_ESCAPE = 0x01,
KEY_A = 0x1E,
KEY_C = 0x2E,
KEY_V = 0x2F,
KEY_X = 0x2D,
KEY_Y = 0x15,
KEY_Z = 0x2C;

void SetClipboardText(void*, const char* text) {
	jmethodID mid = gJNIEnv->GetStaticMethodID(gJavaImGuiClass, "setClipboardContent", "(Ljava/lang/String;)V");
	auto str = gJNIEnv->NewStringUTF(text);
	gJNIEnv->CallStaticObjectMethod(gJavaImGuiClass, mid, str);
}

std::string gClipboardBuf = "";

const char* GetClipboardText(void*) {
	jmethodID mid = gJNIEnv->GetStaticMethodID(gJavaImGuiClass, "getClipboardContent", "()Ljava/lang/String;");
	auto strObject = gJNIEnv->CallStaticObjectMethod(gJavaImGuiClass, mid);
	const char* str = gJNIEnv->GetStringUTFChars((jstring) strObject, nullptr);
	gClipboardBuf = str;
	gJNIEnv->ReleaseStringUTFChars((jstring) strObject, str);
	return gClipboardBuf.c_str();
}

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nCreateContext
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nCreateContext
(JNIEnv * env, jclass clz) {
	gJNIEnv = env;
	gJavaImGuiClass = clz;

	ImGui::CreateContext();
	ImGui::StyleColorsDark();

	auto& io = ImGui::GetIO();

	io.BackendRendererName = "imgui_impl_lwjgl_gl3";

	io.BackendFlags |= ImGuiBackendFlags_HasMouseCursors;
	io.BackendFlags |= ImGuiBackendFlags_HasSetMousePos;
	io.BackendPlatformName = "imgui_impl_lwjgl";

    // Keyboard mapping. ImGui will use those indices to peek into the io.KeysDown[] array.
    io.KeyMap[ImGuiKey_Tab] = KEY_TAB;
    io.KeyMap[ImGuiKey_LeftArrow] = KEY_LEFT;
    io.KeyMap[ImGuiKey_RightArrow] = KEY_RIGHT;
    io.KeyMap[ImGuiKey_UpArrow] = KEY_UP;
    io.KeyMap[ImGuiKey_DownArrow] = KEY_DOWN;
    io.KeyMap[ImGuiKey_Home] = KEY_HOME;
    io.KeyMap[ImGuiKey_End] = KEY_END;
    io.KeyMap[ImGuiKey_Insert] = KEY_INSERT;
    io.KeyMap[ImGuiKey_Delete] = KEY_DELETE;
    io.KeyMap[ImGuiKey_Backspace] = KEY_BKSP;
    io.KeyMap[ImGuiKey_Space] = KEY_SPACE;
    io.KeyMap[ImGuiKey_Enter] = KEY_ENTER;
    io.KeyMap[ImGuiKey_Escape] = KEY_ESCAPE;
    io.KeyMap[ImGuiKey_A] = KEY_A;
    io.KeyMap[ImGuiKey_C] = KEY_C;
    io.KeyMap[ImGuiKey_V] = KEY_V;
    io.KeyMap[ImGuiKey_X] = KEY_X;
    io.KeyMap[ImGuiKey_Y] = KEY_Y;
    io.KeyMap[ImGuiKey_Z] = KEY_Z;

	io.GetClipboardTextFn = GetClipboardText;
	io.SetClipboardTextFn = SetClipboardText;

	std::cout << "IMGui: nCreateContext finish" << std::endl;
}

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nShowDemoWindow
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nShowDemoWindow
  (JNIEnv* env, jclass clz, jboolean open)
{
	bool bOpen = open;
	ImGui::ShowDemoWindow(&bOpen);
	return bOpen;
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nFillInput
(JNIEnv* env, jclass clz, 
	jfloat dispWidth, jfloat dispHeight, 
	jfloat deltaTime, 
	jfloat mouseX, jfloat mouseY, 
	jbooleanArray mouseDown, 
	jfloat mouseWheel, 
	jboolean keyCtrl, 
	jboolean keyShift, 
	jboolean keyAlt, 
	jboolean keySuper, 
	jbooleanArray keysDown, 
	jcharArray inputChars)
{
	auto& io = ImGui::GetIO();
	io.DisplaySize = ImVec2(dispWidth, dispHeight);
	io.DeltaTime = deltaTime;
	io.MousePos = ImVec2(mouseX, mouseY);
	{
		auto elements = env->GetBooleanArrayElements(mouseDown, nullptr);
		for (int i = 0; i < 5; ++i)
			io.MouseDown[i] = elements[i];
		env->ReleaseBooleanArrayElements(mouseDown, elements, JNI_ABORT);
	}
	io.MouseWheel = mouseWheel;
	io.KeyCtrl = keyCtrl;
	io.KeyShift = keyShift;
	io.KeyAlt = keyAlt;
	io.KeySuper = keySuper;
	{
		auto elems = env->GetBooleanArrayElements(keysDown, nullptr);
		for (int i = 0; i < 256; ++i)
			io.KeysDown[i] = elems[i];
		env->ReleaseBooleanArrayElements(keysDown, elems, JNI_ABORT);
	}
	io.ClearInputCharacters();
	{
		auto len = env->GetArrayLength(inputChars);
		auto elems = env->GetCharArrayElements(inputChars, nullptr);
		for (int i = 0; i < len; ++i)
			io.AddInputCharacter(elems[i]);
	}
}

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nNewFrame
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nNewFrame
(JNIEnv* env, jclass clz) {
	ImGui::NewFrame();
}


/*
* Class:     cn_lambdalib2_vis_editor_ImGui
* Method:    nRender
* Signature: ()Lcn/lambdalib2/vis/editor/ImDrawData;
*/
JNIEXPORT jobject JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nRender
(JNIEnv* env, jclass clz) {
	ImGui::Render();
	auto drawData = ImGui::GetDrawData();

	// Fill ImDrawData
	auto clzImDrawData = env->FindClass("cn/lambdalib2/vis/editor/ImDrawData");

	auto clzImDrawList = env->FindClass("cn/lambdalib2/vis/editor/ImDrawList");
	auto clzImDrawVert = env->FindClass("cn/lambdalib2/vis/editor/ImDrawVert");
	auto clzImDrawCmd = env->FindClass("cn/lambdalib2/vis/editor/ImDrawCmd");

	auto cmdLists = env->NewObjectArray(drawData->CmdListsCount, clzImDrawList, nullptr);

	auto ctorDrawList = env->GetMethodID(clzImDrawList, "<init>", "");
	for (int i = 0; i < drawData->CmdListsCount; ++i) {
		auto list = drawData->CmdLists[i];

		auto arrCmdBuffer = env->NewObjectArray(list->CmdBuffer.Size, clzImDrawCmd, nullptr);
		auto arrIdxBuffer = env->NewIntArray(list->IdxBuffer.Size);
		auto arrVtxBuffer = env->NewObjectArray(list->VtxBuffer.Size, clzImDrawVert, nullptr);

		auto jlist = env->NewObject(clzImDrawData, ctorDrawList, arrCmdBuffer, arrIdxBuffer, arrVtxBuffer, list->Flags);
		// TODO: Add item
	}

	jobject ret;
	{
		auto ctor = env->GetMethodID(clzImDrawData, "<init>", "([Lcn/lambdalib2/vis/editor/ImDrawList;FFFF)V");
		ret = env->NewObject(clzImDrawData, ctor, cmdLists, 
			drawData->DisplayPos.x, drawData->DisplayPos.y,
			drawData->DisplaySize.x, drawData->DisplaySize.y);
	}
	return ret;
}


/*
* Class:     cn_lambdalib2_vis_editor_ImGui
* Method:    nGetFontTexARGB32
* Signature: ()Lcn/lambdalib2/vis/editor/ImFontTex;
*/
JNIEXPORT jobject JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nGetFontTexARGB32
(JNIEnv* env, jclass clz) {
	auto clzImFontTex = env->FindClass("cn/lambdalib2/vis/editor/ImFontTex");
	auto ctor = env->GetMethodID(clzImFontTex, "<init>", "([BII)V");
	auto& io = ImGui::GetIO();

	unsigned char* pixels;
	int width, height;
	io.Fonts->GetTexDataAsRGBA32(&pixels, &width, &height);

	auto arrLen = width * height * 4;
	jbyte* buf = new jbyte[arrLen];
	for (int i = 0; i < arrLen; ++i)
		buf[i] = pixels[i];
	ImGui::MemFree(pixels); // ! pixels lifetime ends here

	auto jByteArr = env->NewByteArray(arrLen);
	env->SetByteArrayRegion(jByteArr, 0, arrLen, buf);
	delete buf; // ! buf lifetime ends here

	// new ImFontTex(...)
	return env->NewObject(clzImFontTex, ctor, jByteArr, width, height);
}

/*
* Class:     cn_lambdalib2_vis_editor_ImGui
* Method:    nSetFontTexID
* Signature: (I)V
*/
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSetFontTexID
(JNIEnv* env, jclass clz, jint texID) {
	auto& io = ImGui::GetIO();
	io.Fonts->TexID = (ImTextureID)(intptr_t)texID;
}
