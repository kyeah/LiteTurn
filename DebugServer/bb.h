#ifndef _GEOM_H_
#define _GEOM_H_

#include<cmath>
#include<algorithm>
#include<iostream>
#include<vector>
#include<map>
#include <limits>
#include "vec.h"

using namespace std;

struct BoundingBox {
  Vec3f min;
  Vec3f max;
  Vec3f center() const {
    return Vec3f::makeVec((min[0]+max[0])/2,
                          (min[1]+max[1])/2,
                          (min[2]+max[2])/2);
  }
  float dim(int i) const { return max[i]-min[i]; }
  float xdim() const { return dim(0); }
  float ydim() const { return dim(1); }
  float zdim() const { return dim(2); }
  void operator()(const Vec3f& v) {
    min = Vec3f::min(min, v);
    max = Vec3f::max(max, v);
  }
  friend ostream & operator<<(ostream & out, const BoundingBox & b);
};

inline ostream & operator<<(ostream & out, const BoundingBox & b)
{
  out << "[box min=" << b.min
      << " max=" << b.max 
      << " center=" << b.center() << "]";
  return out;
}
	
#endif
