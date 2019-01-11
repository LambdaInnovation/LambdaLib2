#include <jni.h>
#include <iostream>
#include <algorithm>
#include <vector>
#include <string>
#include "imgui.h"
#include "cn_lambdalib2_vis_editor_ImGui.h"

// Globals

static JNIEnv* gJNIEnv;
static jclass gJavaImGuiClass;
static jclass gJavaImBoolRefClass;
static jfieldID gImBoolRef_value;

// Utilities

ImVec4 ParseColor(int col) {
	int r = (col >> 24) & 0xFF;
	int g = (col >> 16) & 0xFF;
	int b = (col >> 8) & 0xFF;
	int a = (col) & 0xFF;
	return ImVec4(r / 255.0f, g / 255.0f, b / 255.0f, a / 255.0f);
}

int Vec4ToColor(const ImVec4& v) {
	unsigned r = (int)(255 * v.x);
	unsigned g = (int)(255 * v.y);
	unsigned b = (int)(255 * v.z);
	unsigned a = (int)(255 * v.w);
	return (r << 24) | (g << 16) | (b << 8) | a;
}

class JNIStr {
	const char* chars;

	JNIEnv* env;
	jstring str;

public:
	JNIStr(JNIEnv* env, jstring s) : env(env), str(s) {
		chars = env->GetStringUTFChars(str, nullptr);
	}
	
	~JNIStr() {
		env->ReleaseStringUTFChars(str, chars);
	}

	const char* c_str() const {
		return chars;
	}

	operator const char*() {
		return c_str();
	}

	// Disallow copy & move
	JNIStr(JNIStr& ref) = delete;
	JNIStr(JNIStr&& rref) = delete;
};

void SetBoolRef(jobject refObj, bool value) {
	gJNIEnv->SetBooleanField(refObj, gImBoolRef_value, value);
}

bool GetBoolRef(jobject refObj) {
	return gJNIEnv->GetBooleanField(refObj, gImBoolRef_value);
}

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

	gJavaImBoolRefClass = env->FindClass("cn/lambdalib2/vis/editor/ImBoolRef");
	gImBoolRef_value = env->GetFieldID(gJavaImBoolRefClass, "value", "Z");

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
		env->ReleaseCharArrayElements(inputChars, elems, JNI_ABORT);
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

	auto ctorDrawList = env->GetMethodID(clzImDrawList, "<init>", "([Lcn/lambdalib2/vis/editor/ImDrawCmd;[I[Lcn/lambdalib2/vis/editor/ImDrawVert;I)V");
	for (int i = 0; i < drawData->CmdListsCount; ++i) {
		auto list = drawData->CmdLists[i];

		auto arrCmdBuffer = env->NewObjectArray(list->CmdBuffer.Size, clzImDrawCmd, nullptr);
		auto arrIdxBuffer = env->NewIntArray(list->IdxBuffer.Size);
		auto arrVtxBuffer = env->NewObjectArray(list->VtxBuffer.Size, clzImDrawVert, nullptr);

		// Fill cmd buf
		auto ctorDrawCmd = env->GetMethodID(clzImDrawCmd, "<init>", "(IFFFFI)V");
		for (int ncmd = 0; ncmd < list->CmdBuffer.Size; ++ncmd) {
			auto& c = list->CmdBuffer[ncmd];
			auto jcmd = env->NewObject(clzImDrawCmd, ctorDrawCmd, c.ElemCount, 
				c.ClipRect.x, c.ClipRect.y, c.ClipRect.z, c.ClipRect.w, (int) c.TextureId);

			if (c.UserCallback != nullptr)
				std::cout << "WARN: This DrawCmd Has UserCallback!" << std::endl;
			env->SetObjectArrayElement(arrCmdBuffer, ncmd, jcmd);
		}

		// Fill idx buf
		{
			auto buf = new jint[list->IdxBuffer.Size];
			for (int j = 0; j < list->IdxBuffer.Size; ++j)
				buf[j] = list->IdxBuffer[j];
			env->SetIntArrayRegion(arrIdxBuffer, 0, list->IdxBuffer.Size, buf);
			delete[] buf;
		}

		// Fill vtx buf
		auto ctorDrawVtx = env->GetMethodID(clzImDrawVert, "<init>", "(FFFFI)V");
		for (int nvtx = 0; nvtx < list->VtxBuffer.Size; ++nvtx) {
			auto& v = list->VtxBuffer[nvtx];
			auto jvtx = env->NewObject(clzImDrawVert, ctorDrawVtx, 
				v.pos.x, v.pos.y, v.uv.x, v.uv.y, v.col);
			env->SetObjectArrayElement(arrVtxBuffer, nvtx, jvtx);
		}

		auto jlist = env->NewObject(clzImDrawList, ctorDrawList, arrCmdBuffer, arrIdxBuffer, arrVtxBuffer, list->Flags);
		env->SetObjectArrayElement(cmdLists, i, jlist);
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
	delete[] buf; // ! buf lifetime ends here

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

JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBegin
(JNIEnv* env, jclass clz, jstring name, jboolean open, jint flags) {
	JNIStr cname(env, name);
	bool copen = open;
	ImGui::Begin(cname, &copen, flags);
	return copen;
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nEnd
(JNIEnv *, jclass) {
	ImGui::End();
}

JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBeginChild
(JNIEnv* env, jclass clz, jstring name, jfloat sx, jfloat sy, jboolean border, jint flags) {
	JNIStr cname(env, name);
	auto ret = ImGui::BeginChild(cname, ImVec2(sx, sy), border, flags);
	return ret;
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nEndChild
(JNIEnv *, jclass) {
	ImGui::EndChild();
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSeparator
(JNIEnv *, jclass) {
	ImGui::Separator();
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nNewLine
(JNIEnv *, jclass) {
	ImGui::NewLine();
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSpacing
(JNIEnv *, jclass) {
	ImGui::Spacing();
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBeginGroup
(JNIEnv *, jclass) {
	ImGui::BeginGroup();
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nEndGroup
(JNIEnv *, jclass) {
	ImGui::EndGroup();
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nPushID
(JNIEnv * env, jclass, jstring id) {
	JNIStr cid(env, id);
	ImGui::PushID(cid);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nPushID2
(JNIEnv *, jclass, jint id) {
	ImGui::PushID(id);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nPopID
(JNIEnv *, jclass) {
	ImGui::PopID();
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nText
(JNIEnv* env, jclass, jstring s) {
	JNIStr cs(env, s);
	ImGui::Text(cs);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nTextColored
(JNIEnv* env, jclass, jint col, jstring s) {
	JNIStr cs(env, s);
	ImGui::TextColored(ParseColor(col), cs);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nTextWrapped
(JNIEnv* env, jclass, jstring str) {
	JNIStr cs(env, str);
	ImGui::TextWrapped(cs);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nLabelText
(JNIEnv* env, jclass, jstring label, jstring text) {
	JNIStr clabel(env, label), ctext(env, text);
	ImGui::LabelText(clabel, ctext);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBulletText
(JNIEnv* env, jclass clz, jstring str) {
	JNIStr cstr(env, str);
	ImGui::BulletText(cstr);
}

JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nButton
(JNIEnv* env, jclass, jstring str, jfloat sizeX, jfloat sizeY) {
	JNIStr cstr(env, str);
	return ImGui::Button(cstr, ImVec2(sizeX, sizeY));
}

JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nArrowButton
(JNIEnv* env, jclass clz, jstring str, jint dir) {
	JNIStr cstr(env, str);
	return ImGui::ArrowButton(cstr, dir);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nImage
(JNIEnv* env, jclass, jint texID, jfloat sizeX, jfloat sizeY, 
	jfloat u0, jfloat v0, jfloat u1, jfloat v1, jint tintColor, jint borderColor) {
	ImGui::Image(reinterpret_cast<ImTextureID>(texID), ImVec2(sizeX, sizeY), ImVec2(u0, v0), ImVec2(u1, v1), 
		ParseColor(tintColor), ParseColor(borderColor));
}

JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nImageButton
(JNIEnv* env, jclass, jint texID, jfloat sx, jfloat sy, jfloat u0, jfloat v0, 
	jfloat u1, jfloat v1, jint framePadding, jint bgCol, jint tintCol) {
	return ImGui::ImageButton(reinterpret_cast<ImTextureID>(texID), ImVec2(sx, sy), ImVec2(u0, v0),
		ImVec2(u1, v1), framePadding, ParseColor(bgCol), ParseColor(tintCol));
}

JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nCheckbox
(JNIEnv* env, jclass, jstring text, jboolean active) {
	JNIStr ctext(env, text);
	bool cactive = active;
	ImGui::Checkbox(ctext, &cactive);
	return cactive;
}

JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nRadioButton
(JNIEnv* env, jclass, jstring label, jboolean active) {
	JNIStr clabel(env, label);
	return ImGui::RadioButton(clabel, active);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBullet
(JNIEnv *, jclass) {
	ImGui::Bullet();
}

JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBeginCombo
(JNIEnv* env, jclass, jstring label, jstring text, jint flags) {
	JNIStr clabel(env, label), ctext(env, text);
	return ImGui::BeginCombo(clabel, ctext, flags);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nEndCombo
(JNIEnv *, jclass) {
	ImGui::EndCombo();
}

JNIEXPORT jint JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nCombo
(JNIEnv* env, jclass, jstring label, jint ix, jobjectArray arr) {
	int cix = ix;
	int len = env->GetArrayLength(arr);
	std::vector<const char*> cs;
	for (int i = 0; i < len; ++i) {
		auto str = (jstring) env->GetObjectArrayElement(arr, i);
		cs.push_back(env->GetStringUTFChars(str, nullptr));
	}
	
	JNIStr clabel(env, label);
	ImGui::Combo(clabel, &cix, &cs.at(0), len);

	for (int i = 0; i < len; ++i) {
		auto str = (jstring) env->GetObjectArrayElement(arr, i);
		env->ReleaseStringUTFChars(str, cs[i]);
	}
	return cix;
}

JNIEXPORT jfloat JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSliderFloat
(JNIEnv* env, jclass, jstring label, jfloat v, jfloat vmin, jfloat vmax,
	jstring format, jfloat pwr) {
	float cf = v;
	JNIStr clabel(env, label);
	JNIStr cformat(env, format);
	ImGui::SliderFloat(clabel, &cf, vmin, vmax, cformat, pwr);
	return cf;
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSliderFloat2
(JNIEnv* env, jclass, jstring label, jfloatArray val,
	jfloat vmin, jfloat vmax, jstring fmt, jfloat pwr) {
	JNIStr clabel(env, label), cfmt(env, fmt);
	auto cval = env->GetFloatArrayElements(val, nullptr);
	ImGui::SliderFloat2(clabel, cval, vmin, vmax, cfmt, pwr);
	env->ReleaseFloatArrayElements(val, cval, 0);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSliderFloat3
(JNIEnv* env, jclass, jstring label, jfloatArray val,
	jfloat vmin, jfloat vmax, jstring fmt, jfloat pwr) {
	JNIStr clabel(env, label), cfmt(env, fmt);
	auto cval = env->GetFloatArrayElements(val, nullptr);
	ImGui::SliderFloat3(clabel, cval, vmin, vmax, cfmt, pwr);
	env->ReleaseFloatArrayElements(val, cval, 0);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSliderFloat4
(JNIEnv* env, jclass, jstring label, jfloatArray val,
	jfloat vmin, jfloat vmax, jstring fmt, jfloat pwr) {
	JNIStr clabel(env, label), cfmt(env, fmt);
	auto cval = env->GetFloatArrayElements(val, nullptr);
	ImGui::SliderFloat4(clabel, cval, vmin, vmax, cfmt, pwr);
	env->ReleaseFloatArrayElements(val, cval, 0);
}

JNIEXPORT jfloat JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSliderAngle
(JNIEnv* env, jclass, jstring label, jfloat v, jfloat vmin, jfloat vmax, jstring fmt) {
	JNIStr clabel(env, label), cfmt(env, fmt);
	ImGui::SliderAngle(clabel, &v, vmin, vmax, cfmt);
	return v;
}

JNIEXPORT jint JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSliderInt
(JNIEnv* env, jclass, jstring label, jint v, jint vmin, jint vmax) {
	JNIStr clabel(env, label);
	int cv = v;
	ImGui::SliderInt(clabel, &cv, vmin, vmax);
	return cv;
}

char textBuf[65535];

JNIEXPORT jstring JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputText
(JNIEnv* env, jclass, jstring label, jstring text, jint flags) {
	JNIStr clabel(env, label);
	JNIStr ctext(env, text);
	strcpy(textBuf, ctext);

	ImGui::InputText(clabel, textBuf, 65535, flags);

	return strcmp(textBuf, ctext) == 0 ? text : env->NewStringUTF(textBuf);
}

JNIEXPORT jstring JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputTextMultiline
(JNIEnv* env, jclass, jstring label, jstring text, jfloat sx, jfloat sy, jint flags) {
	JNIStr clabel(env, label);
	JNIStr ctext(env, text);
	strcpy(textBuf, ctext);

	ImGui::InputTextMultiline(clabel, textBuf, 65535, ImVec2(sx, sy), flags);

	return strcmp(textBuf, ctext) == 0 ? text : env->NewStringUTF(textBuf);
}

JNIEXPORT jfloat JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputFloat
(JNIEnv* env, jclass, jstring label, jfloat v, jstring fmt, jint flags) {
	JNIStr clabel(env, label);
	JNIStr cfmt(env, fmt);
	ImGui::InputFloat(clabel, &v, 0.0f, 0.0f, cfmt, flags);
	return v;
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputFloat2
(JNIEnv* env, jclass, jstring label, jfloatArray v, jstring fmt, jint flags) {
	JNIStr clabel(env, label), cfmt(env, fmt);
	auto cv = env->GetFloatArrayElements(v, nullptr);
	ImGui::InputFloat2(clabel, cv, cfmt, flags);
	env->ReleaseFloatArrayElements(v, cv, 0);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputFloat3
(JNIEnv* env, jclass, jstring label, jfloatArray v, jstring fmt, jint flags) {
	JNIStr clabel(env, label), cfmt(env, fmt);
	auto cv = env->GetFloatArrayElements(v, nullptr);
	ImGui::InputFloat3(clabel, cv, cfmt, flags);
	env->ReleaseFloatArrayElements(v, cv, 0);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputFloat4
(JNIEnv* env, jclass, jstring label, jfloatArray v, jstring fmt, jint flags) {
	JNIStr clabel(env, label), cfmt(env, fmt);
	auto cv = env->GetFloatArrayElements(v, nullptr);
	ImGui::InputFloat4(clabel, cv, cfmt, flags);
	env->ReleaseFloatArrayElements(v, cv, 0);
}

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputInt
 * Signature: (Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputInt
(JNIEnv* env, jclass, jstring label, jint v, jint flags) {
	int cv = v;
	JNIStr clabel(env, label);
	ImGui::InputInt(clabel, &cv, 1, 100, flags);
	return cv;
}

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputInt2
 * Signature: (Ljava/lang/String;[II)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputInt2
(JNIEnv* env, jclass, jstring label, jintArray v, jint flags) {
	JNIStr clabel(env, label);

	int len = env->GetArrayLength(v);
	int* cv = new int[len]; // ! begin cv
	jint* carr = env->GetIntArrayElements(v, nullptr); // ! begin carr
	std::copy(carr, carr + len, cv);

	ImGui::InputInt2(clabel, &cv[0], flags);

	std::copy(cv, cv + len, carr);
	env->ReleaseIntArrayElements(v, carr, 0); // ! end carr
	delete[] cv; // ! end cv
}

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputInt3
 * Signature: (Ljava/lang/String;[II)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputInt3
(JNIEnv * env, jclass, jstring label, jintArray v, jint flags) {
	JNIStr clabel(env, label);

	int len = env->GetArrayLength(v);
	int* cv = new int[len]; // ! begin cv
	jint* carr = env->GetIntArrayElements(v, nullptr); // ! begin carr
	std::copy(carr, carr + len, cv);

	ImGui::InputInt3(clabel, &cv[0], flags);

	std::copy(cv, cv + len, carr);
	env->ReleaseIntArrayElements(v, carr, 0); // ! end carr
	delete[] cv; // ! end cv
}

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputInt4
 * Signature: (Ljava/lang/String;[II)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputInt4
(JNIEnv * env, jclass, jstring label, jintArray v, jint flags) {
	JNIStr clabel(env, label);

	int len = env->GetArrayLength(v);
	int* cv = new int[len]; // ! begin cv
	jint* carr = env->GetIntArrayElements(v, nullptr); // ! begin carr
	std::copy(carr, carr + len, cv);

	ImGui::InputInt4(clabel, &cv[0], flags);

	std::copy(cv, cv + len, carr);
	env->ReleaseIntArrayElements(v, carr, 0); // ! end carr
	delete[] cv; // ! end cv
}

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nInputDouble
 * Signature: (Ljava/lang/String;DI)D
 */
JNIEXPORT jdouble JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nInputDouble
(JNIEnv * env, jclass, jstring label, jdouble v, jint flags) {
	JNIStr clabel(env, label);
	ImGui::InputDouble(clabel, &v, 0, 0, "%.6f", flags);
	return v;
}

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nColorEdit4
 * Signature: (Ljava/lang/String;II)I
 */
JNIEXPORT jint JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nColorEdit4
(JNIEnv* env, jclass, jstring label, jint col, jint flags) {
	JNIStr clabel(env, label);
	ImVec4 c = ParseColor(col);
	float arr[4] = { c.x, c.y, c.z, c.w };
	ImGui::ColorEdit4(clabel, arr, flags);
	c.x = arr[0]; c.y = arr[1]; c.z = arr[2]; c.w = arr[3];
	return Vec4ToColor(c);
}

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nColorButton
 * Signature: (Ljava/lang/String;IIFF)V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nColorButton
(JNIEnv* env, jclass, jstring descID, jint col, jint flags, jfloat sx, jfloat sy) {
	JNIStr cDescID(env, descID);
	ImVec4 c = ParseColor(col);
	ImGui::ColorButton(cDescID, c, flags, ImVec2(sx, sy));
}

JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nTreeNode
(JNIEnv* env, jclass, jstring label) {
	JNIStr clabel(env, label);
	return ImGui::TreeNode(clabel);
}

JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nTreeNodeEx
(JNIEnv* env, jclass, jstring label, jint flags) {
	JNIStr clabel(env, label);
	return ImGui::TreeNodeEx(clabel, flags);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nTreePop
(JNIEnv *, jclass) {
	ImGui::TreePop();
}


JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nCollapsingHeader
(JNIEnv* env, jclass, jstring label, jint flags) {
	JNIStr clabel(env, label);
	return ImGui::CollapsingHeader(clabel, nullptr, flags);
}

JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBeginMainMenuBar
(JNIEnv *, jclass) {
	return ImGui::BeginMainMenuBar();
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nEndMainMenuBar
(JNIEnv *, jclass) {
	ImGui::EndMainMenuBar();
}

JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBeginMenuBar
(JNIEnv *, jclass) {
	return ImGui::BeginMenuBar();
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nEndMenuBar
(JNIEnv *, jclass) {
	ImGui::EndMenuBar();
}

JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nMenuItem
(JNIEnv* env, jclass clz, jstring text, jboolean enabled) {
	JNIStr ctext(env, text);
	return ImGui::MenuItem(ctext, 0, false, enabled);
}

JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nMenuItem2
(JNIEnv* env, jclass, jstring label, jobject selected, jboolean enabled) {
	JNIStr clabel(env, label);
	bool vs = GetBoolRef(selected);
	auto ret = ImGui::MenuItem(clabel, nullptr, &vs, enabled);
	SetBoolRef(selected, vs);
	return ret;
}

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nBeginMenu
 * Signature: (Ljava/lang/String;Z)V
 */
JNIEXPORT jboolean JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBeginMenu
(JNIEnv* env, jclass, jstring label, jboolean enabled) {
	JNIStr clabel(env, label);
	return ImGui::BeginMenu(clabel, enabled);
}

/*
 * Class:     cn_lambdalib2_vis_editor_ImGui
 * Method:    nEndMenu
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nEndMenu
(JNIEnv *, jclass) {
	ImGui::EndMenu();
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSameLine
(JNIEnv *, jclass) {
	ImGui::SameLine();
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nPushItemWidth
(JNIEnv*, jclass, jfloat w) {
	ImGui::PushItemWidth(w);
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nPopItemWidth
(JNIEnv* env, jclass) {
	ImGui::PopItemWidth();
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nSetCursorPosX
(JNIEnv *, jclass, jfloat v) {
	ImGui::SetCursorPosX(v);
}

JNIEXPORT jfloatArray JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nGetCursorPos
(JNIEnv* env, jclass) {
	auto p = ImGui::GetCursorPos();
	float arr[] = { p.x, p.y };
	auto jarr = env->NewFloatArray(2);
	env->SetFloatArrayRegion(jarr, 0, 2, arr);
	return jarr;
}

JNIEXPORT jfloatArray JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nGetWindowRect
(JNIEnv* env, jclass) {
	auto sz = ImGui::GetWindowSize();
	auto pos = ImGui::GetWindowPos();

	float arr[] = { pos.x, pos.y, sz.x, sz.y };
	auto jarr = env->NewFloatArray(4);
	env->SetFloatArrayRegion(jarr, 0, 4, arr);
	return jarr;
}

JNIEXPORT void JNICALL Java_cn_lambdalib2_vis_editor_ImGui_nBegin2
(JNIEnv * env, jclass, jstring name, jint flags) {
	JNIStr cname(env, name);
	ImGui::Begin(cname, nullptr, flags);
}