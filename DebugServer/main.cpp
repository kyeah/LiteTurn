#include <string>
#include <vector>
#include <iostream>
#include <iomanip>
#include <algorithm>

#include "common.h"
#include "bb.h"
#include "websocket/server_ws.hpp"
#include "websocket/client_ws.hpp"

using namespace std;
using namespace SimpleWeb;


/**
 * A hacked-together debugging server for visualizing Android sensory information sent over wifi or LAN.
 */

// window parameters
int window_width = 800, window_height = 600;
float window_aspect = window_width / static_cast<float>(window_height);
float zoom = 1;

float mouse_x, mouse_y;
float arcmouse_x, arcmouse_y, arcmouse_z;

bool right_mouse_button = false;
bool left_mouse_button = false;

GLfloat rot_matrix[16] = {1, 0, 0, 0,
                          0, 1, 0, 0,
                          0, 0, 1, 0,
                          0, 0, 0, 1};

float boxRot[16] = {1, 0, 0, 0,
                    0, 1, 0, 0,
                    0, 0, 1, 0,
                    0, 0, 0, 1};

// lighting
bool scene_lighting;
GLfloat light_ambient[] = { 0.1f, 0.1f, 0.1f, 1.0f };
GLfloat light_diffuse[] = { 0.0f, 1.0f, 1.0f, 1.0f };
GLfloat light_specular[] = { 0, 1.0f, 1.0f, 1.0f };
GLfloat light_position[] = { 0, 30, 20, 0 };

SocketServer<WS> webserver(8080, 4);

void SetLighting() {
  glShadeModel(GL_SMOOTH);
  glEnable(GL_LIGHTING);
  glEnable(GL_LIGHT0);
  glLightfv(GL_LIGHT0, GL_AMBIENT, light_ambient);
  glLightfv(GL_LIGHT0, GL_DIFFUSE, light_diffuse);
  glLightfv(GL_LIGHT0, GL_SPECULAR, light_specular);
  glLightfv(GL_LIGHT0, GL_POSITION, light_position);
}

void Display() {
  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

  glMatrixMode(GL_PROJECTION);
  glLoadIdentity();
  gluPerspective(40.0, window_aspect, 1, 1500);

  glMatrixMode(GL_MODELVIEW);
  glLoadIdentity();

  // Set reasonable view for object
  BoundingBox bbox;
  bbox.min = Vec3f::makeVec(-5, -5, -5);
  bbox.max = Vec3f::makeVec(5, 5, 5);
  //BoundingBox bbox = mesh.bb();
  float maxDist = (bbox.max-bbox.min).max();
  Vec3f eye = Vec3f::makeVec(0.0f*maxDist, 0.25f*maxDist, 1.5f*maxDist);

  if (!scene_lighting) {
    light_position[0] = eye[0]*zoom;
    light_position[1] = eye[1]*zoom;
    light_position[2] = eye[2]*zoom;
    SetLighting();
  }

  gluLookAt(eye[0]*zoom, eye[1]*zoom, eye[2]*zoom,
            0, 0, 0,
            0, 1, 0);

  // Apply rotation matrix
  glMultMatrixf(rot_matrix);

  glDisable(GL_LIGHTING);
  glLineWidth(4);
  DrawAxis();
  DrawAxisSphere();
  DrawBox();

  // Move the origin up
  glTranslatef(0, -maxDist/8, 0);
  if (scene_lighting)
    SetLighting();

  glEnable(GL_RESCALE_NORMAL);
  glEnable(GL_LIGHTING);

  // Draw

  glFlush();
  glutSwapBuffers();
}

void PrintMatrix(GLfloat* m) {
  cout.precision(2);
  int w = 6;
  for (int i = 0; i < 4; ++i) {
    cout << setprecision(2) << setw(w) << m[i] << " "
         << setprecision(2) << setw(w) << m[i+4] << " "
         << setprecision(2) << setw(w) << m[i+8] << " "
         << setprecision(2) << setw(w) << m[i+12] << " "
         << endl;
  }
  cout << endl;
}

void PrintMatrix(GLint matrix) {
  GLfloat m[16];
  glGetFloatv(matrix, m);
  PrintMatrix(m);
}

void PrintMatrix() {
  PrintMatrix(GL_MODELVIEW_MATRIX);
}

void LoadMatrix(GLfloat* m) {
  // transpose to column-major
  for (int i = 0; i < 4; ++i) {
    for (int j = i; j < 4; ++j) {
      swap(m[i*4+j], m[j*4+i]);
    }
  }
  glLoadMatrixf(m);
}

void MultMatrix(GLfloat* m) {
  // transpose to column-major
  for (int i = 0; i < 4; ++i) {
    for (int j = i; j < 4; ++j) {
      swap(m[i*4+j], m[j*4+i]);
    }
  }
  glMultMatrixf(m);
}

void Init() {
  glEnable(GL_DEPTH_TEST);
  glDepthMask(GL_TRUE);
  glDepthFunc(GL_LEQUAL);
  glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

  glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
  //glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
  glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

  // resize the window
  window_aspect = window_width/static_cast<float>(window_height);

  glMatrixMode(GL_PROJECTION);
  glLoadIdentity();

  gluPerspective(40.0, window_aspect, 1, 1500);
}

void DrawAxis() {
  const Vec3f c = {0, 0, 0};
  const float L = 4;
  const Vec3f X = {L, 0, 0}, Y = {0, L, 0}, Z = {0, 0, L};

  glBegin(GL_LINES);
  glColor3f(1, 0, 0);
  glVertex3fv((c-X).x);
  glVertex3fv((c+X).x);
  glColor3f(0, 1, 0);
  glVertex3fv((c-Y).x);
  glVertex3fv((c+Y).x);
  glColor3f(0, 0, 1);
  glVertex3fv((c-Z).x);
  glVertex3fv((c+Z).x);
  glEnd();
}

void DrawAxisSphere() {
  static const float DEG2RAD = 3.14159/180;
  float radius = 2.25;

  glColor3f(1, 0, 0);
  glBegin(GL_LINE_LOOP);
  for (int i=0; i <= 360; i++) {
    float degInRad = i*DEG2RAD;
    glVertex3f(cos(degInRad)*radius,sin(degInRad)*radius, 0);
  }
  glEnd();

  glColor3f(0, 1, 0);
  glBegin(GL_LINE_LOOP);
  for (int i=0; i <= 360; i++) {
    float degInRad = i*DEG2RAD;
    glVertex3f(0, cos(degInRad)*radius, sin(degInRad)*radius);
  }
  glEnd();

  glColor3f(0, 0, 1);
  glBegin(GL_LINE_LOOP);
  for (int i=0; i <= 360; i++) {
    float degInRad = i*DEG2RAD;
    glVertex3f(cos(degInRad)*radius, 0, sin(degInRad)*radius);
  }
  glEnd();
}

void DrawBox() {
  glPushMatrix();
  glMultMatrixf(boxRot);  // rotate
  //glTranslatef(0, 3, 0);

  const float w = 1;
  const float l = 0.25;
  const float h = 2;

  const float w2 = w / 2;
  const float l2 = l / 2;
  const float h2 = h / 2;

  //Multi-colored side - FRONT
  glBegin(GL_POLYGON);
  glColor3f( 1.0, 0.0, 0.0 ); glVertex3f( w2, -h2, -l2 );      // P1 is red
  glColor3f( 0.0, 1.0, 0.0 ); glVertex3f( w2,  h2, -l2 );      // P2 is green
  glColor3f( 0.0, 0.0, 1.0 ); glVertex3f( -w2,  h2, -l2 );      // P3 is blue
  glColor3f( 1.0, 0.0, 1.0 ); glVertex3f( -w2, -h2, -l2 );      // P4 is purple
  glEnd();

  // White side - BACK
  glBegin(GL_POLYGON);
  glColor3f(   1.0,  1.0, 1.0 );
  glVertex3f(  w2, -h2, l2 );
  glVertex3f(  w2,  h2, l2 );
  glVertex3f( -w2,  h2, l2 );
  glVertex3f( -w2, -h2, l2 );
  glEnd();

  // Purple side - RIGHT
  glBegin(GL_POLYGON);
  glColor3f(  1.0,  0.0,  1.0 );
  glVertex3f( w2, -h2, -l2 );
  glVertex3f( w2,  h2, -l2 );
  glVertex3f( w2,  h2,  l2 );
  glVertex3f( w2, -h2,  l2 );
  glEnd();

  // Green side - LEFT
  glBegin(GL_POLYGON);
  glColor3f(   0.0,  1.0,  0.0 );
  glVertex3f( -w2, -h2,  l2 );
  glVertex3f( -w2,  h2,  l2 );
  glVertex3f( -w2,  h2, -l2 );
  glVertex3f( -w2, -h2, -l2 );
  glEnd();

  // Blue side - TOP
  glBegin(GL_POLYGON);
  glColor3f(   0.0,  0.0,  1.0 );
  glVertex3f(  w2,  h2,  l2 );
  glVertex3f(  w2,  h2, -l2 );
  glVertex3f( -w2,  h2, -l2 );
  glVertex3f( -w2,  h2,  l2 );
  glEnd();

  // Red side - BOTTOM
  glBegin(GL_POLYGON);
  glColor3f(   1.0,  0.0,  0.0 );
  glVertex3f(  w2, -h2, -l2 );
  glVertex3f(  w2, -h2,  l2 );
  glVertex3f( -w2, -h2,  l2 );
  glVertex3f( -w2, -h2, -l2 );
  glEnd();

  glPopMatrix();
}

// Finds the x and y coordinates on arc ball
Vec3f arcSnap(float x, float y) {
  x = (2.0*x / window_width) - 1;
  y = (2.0*y / window_height) - 1;

  float mag2 = x * x + y * y;
  float mag = sqrt(mag2);

  if (mag > 1) {
    x = x*0.999 / mag;  // mult by .999 to account for edge cases of rounding up
    y = y*0.999 / mag;
  }

  float z = sqrt(1.0 - (x*x + y*y));
  return Vec3f::makeVec(x, y, z);
}

void MouseButton(int button, int state, int x, int y) {
  y = window_height - y;

  if (button == GLUT_LEFT_BUTTON) {
    Vec3f arc_coords = arcSnap(x, y);
    arcmouse_x = arc_coords[0];
    arcmouse_y = arc_coords[1];
    arcmouse_z = arc_coords[2];

    left_mouse_button = !state;  // state==0 if down
  }
  if (button == GLUT_RIGHT_BUTTON) {
    right_mouse_button = !state;  // state==0 if down
  }

  mouse_x = x, mouse_y = y;
  glutPostRedisplay();
}

void MouseMotion(int x, int y) {
  y = window_height - y;

  if (left_mouse_button) {
    // Rotation
    Vec3f arc_coords = arcSnap(x, y);
    float fx = arc_coords[0];
    float fy = arc_coords[1];
    float fz = arc_coords[2];

    // Find rotational axis
    float normal_x = arcmouse_y*fz - arcmouse_z*fy;
    float normal_y = arcmouse_z*fx - arcmouse_x*fz;
    float normal_z = arcmouse_x*fy - arcmouse_y*fx;

    // Find rotational angle
    float ax = sqrt(normal_x*normal_x +
                    normal_y*normal_y +
                    normal_z*normal_z);

    float ay = arcmouse_x*fx + arcmouse_y*fy + arcmouse_z*fz;
    float angle = atan2(ax, ay)*180/3.14159;

    // Modify and save rotation matrix
    glLoadIdentity();
    glRotatef(angle, normal_x, normal_y, normal_z);
    glMultMatrixf(rot_matrix);
    glGetFloatv(GL_MODELVIEW_MATRIX, rot_matrix);

    arcmouse_x = fx, arcmouse_y = fy, arcmouse_z = fz;
  } else if (right_mouse_button && y && mouse_y) {
    // Zoom: Multiplies current zoom by ratio between initial and current y
    float smy = mouse_y+window_height;
    float sy = y+window_height;
    float dy;

    if (sy < 0 && smy < 0) {
      dy = abs(smy/sy);
    } else {
      dy = abs(sy/smy);
    }

    zoom *= dy;
  }

  mouse_x = x, mouse_y = y;
  glutPostRedisplay();
}

void Keyboard(unsigned char key, int x, int y) {
  switch (key) {
  case 'q':
  case 27:  // esc
    exit(0);
    break;
  }
}

void InitWebSocket() {
  auto& echo=webserver.endpoint["^/echo/?$"];

  //C++14, lambda parameters declared with auto
  //For C++11 use: (shared_ptr<Server<WS>::Connection> connection, shared_ptr<Server<WS>::Message> message)
  echo.onmessage=[&webserver](auto connection, auto message) {
    stringstream data_ss;
    message->data >> data_ss.rdbuf();

    float x, y, z, w;
    data_ss >> x;
    data_ss >> y;
    data_ss >> z;
    data_ss >> w;
    Vec4f v = Vec4f::makeVec(x, y, z, w);
    setRotFromQuat(v);
    cout << "Server: Message received: \"" << data_ss.str() << "\" from " << (size_t)connection.get() << endl;
    cout << "Server: Sending message \"" << data_ss.str() <<  "\" to " << (size_t)connection.get() << endl;

    glutPostRedisplay();
  };

  echo.onopen=[](auto connection) {
    cout << "Server: Opened connection " << (size_t)connection.get() << endl;
  };

  //See RFC 6455 7.4.1. for status codes
  echo.onclose=[](auto connection, int status, const string& reason) {
    cout << "Server: Closed connection " << (size_t)connection.get() << " with status code " << status << endl;
  };

  //See http://www.boost.org/doc/libs/1_55_0/doc/html/boost_asio/reference.html, Error Codes for error code meanings
  echo.onerror=[](auto connection, const boost::system::error_code& ec) {
    cout << "Server: Error in connection " << (size_t)connection.get() << ". " <<
    "Error: " << ec << ", error message: " << ec.message() << endl;
  };
}

void setRotFromQuat( Vec4f& q ) {
  float x2 = q[0] + q[0];
  float y2 = q[1] + q[1];
  float z2 = q[2] + q[2];
  float w2 = q[3] + q[3];

  float yy2 = q[1] * y2;
  float xy2 = q[0] * y2;
  float xz2 = q[0] * z2;
  float yz2 = q[1] * z2;

  float zz2 = q[2] * z2;
  float wz2 = q[3] * z2;
  float wy2 = q[3] * y2;
  float wx2 = q[3] * x2;

  float xx2 = q[0] * x2;

  boxRot[0*4+0] = - yy2 - zz2 + 1.0f;
  boxRot[0*4+1] = xy2 + wz2;
  boxRot[0*4+2] = xz2 - wy2;
  boxRot[0*4+3] = q[4];

  boxRot[1*4+0] = xy2 - wz2;
  boxRot[1*4+1] = - xx2 - zz2 + 1.0f;
  boxRot[1*4+2] = yz2 + wx2;
  boxRot[1*4+3] = q[5];

  boxRot[2*4+0] = xz2 + wy2;
  boxRot[2*4+1] = yz2 - wx2;
  boxRot[2*4+2] = - xx2 - yy2 + 1.0f;
  boxRot[2*4+3] = q[6];
}

int main(int argc, char *argv[]) {

  // Initialize GLUT
  glutInit(&argc, argv);
  glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGBA | GLUT_DEPTH);
  glutInitWindowSize(window_width, window_height);
  glutInitWindowPosition(100, 100);
  glutCreateWindow("Object viewer");
  glutMouseFunc(MouseButton);
  glutMotionFunc(MouseMotion);
  glutKeyboardFunc(Keyboard);
  glutDisplayFunc(Display);

  Init();
  InitWebSocket();

  thread server_thread([&webserver](){
      webserver.start();
    });
  
  glutMainLoop();
  server_thread.join();
  return 0;
}
