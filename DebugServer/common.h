#ifndef __COMMON_H__
#define __COMMON_H__

#ifdef _WIN32
#include <windows.h>
#endif
#ifdef __MAC__
#include <OpenGL/gl.h>
#include <GLUT/glut.h>
#else
#include <GL/gl.h>
#include <GL/glut.h>
#endif

#include "vec.h"

void Init();
void InitSocket();
void InitWebSocket();
void IdleWebSocket();
void Display();
void Keyboard(unsigned char key, int x, int y);
void DisableLighting();
void EnableLighting();
void DrawAxis();
void DrawAxisSphere();
void DrawBox();
void PrintMatrix(GLint matrix);
void PrintMatrix();
void LoadMatrix(GLfloat* m);
void MultMatrix(GLfloat* m);

void setRotFromQuat(Vec4f& q);

#endif
