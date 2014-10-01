#ifndef _VEC_H_
#define _VEC_H_
#include <cmath>
#include <cstring>

#include <iostream>

using namespace std;

typedef unsigned short ushort;

template <class NumType, ushort NumDims>
struct Vec
{
  NumType x[NumDims];
  static Vec<NumType, NumDims> makeVec(NumType * values)
  {
    Vec<NumType,NumDims> v;
    memcpy(v.x,values,sizeof(NumType)*NumDims);
    return v;
  }
  static Vec<NumType, NumDims> makeVec()
  {
    Vec<NumType,NumDims> v;
    memset(v.x,0,sizeof(NumType)*NumDims);
    return v;
  }
  static Vec<NumType, NumDims> makeVec(NumType a)
  {
    Vec<NumType,NumDims> v;
    if(NumDims>=1)
    {
      v.x[0]=a;
    }
    return v;
  }
  static Vec<NumType, NumDims> makeVec(NumType a, NumType b)
  {
    Vec<NumType,NumDims> v;
    if(NumDims>=2)
    {
      v.x[0]=a;v.x[1]=b;
    }
    return v;
  }
  static Vec<NumType, NumDims> makeVec(NumType a, NumType b, NumType c)
  {
    Vec<NumType,NumDims> v;
    if(NumDims>=3)
    {
      v.x[0]=a;v.x[1]=b;v.x[2]=c;
    }
    return v;
  }
  static Vec<NumType, NumDims> makeVec(NumType a, NumType b, NumType c, NumType d)
  {
    Vec<NumType,NumDims> v;
    if(NumDims>=4)
    {
      v.x[0]=a;v.x[1]=b;v.x[2]=c,v.x[3]=d;
    }
    return v;
  }
  static Vec<NumType,NumDims> zero()
  {
    Vec<NumType,NumDims> z;
    memset(z.x,0,sizeof(NumType)*NumDims);
    return z;
  }
  void copy(const NumType  * y) 
  {			
    memcpy(x,y,sizeof(NumType)*NumDims);
  }
  void copy(const Vec<NumType,NumDims> & v) 
  {
    memcpy(x,v.x,sizeof(NumType)*NumDims);
  }
  NumType norm2() const
  {
    return (*this)*(*this);
  }
  NumType norm() const
  {
    return sqrt(norm2());
  }
  Vec<NumType,NumDims> unit() const
  {
    return (*this)/norm();
  }
  bool operator==(NumType a) const
  {
    bool eq = true;
    for(int i=0;i<NumDims && (eq &=(x[i]==a));i++);
    return eq;
  }
  bool operator==(const Vec<NumType,NumDims> & rhs) const
  {
    bool eq = true;
    for(int i=0;i<NumDims && (eq &= (x[i]==rhs.x[i]));i++);
    return eq;
  }

  NumType operator*(const Vec<NumType,NumDims> & rhs) const
  {
    NumType result=NumType(0);
    for(int i=0;i<NumDims;i++) result += x[i]*rhs.x[i];
    return result;
  }
  Vec<NumType,NumDims> operator/(NumType a) const
  {
    Vec<NumType,NumDims> result;
    const NumType inv = 1.0 / a;
    for(int i=0;i<NumDims;i++) result.x[i] = x[i] * inv;
    return result;
  }
  Vec<NumType,NumDims> operator*(NumType a) const
  {
    Vec<NumType,NumDims> result;
    for(int i=0;i<NumDims;i++) result.x[i] = x[i]*a;
    return result;
  }
  Vec<NumType,NumDims> operator+(const Vec<NumType,NumDims> & rhs) const
  {
    Vec<NumType,NumDims> result;
    for(int i=0;i<NumDims;i++) result.x[i] = x[i]+rhs.x[i];
    return result;
  }
  Vec<NumType,NumDims> operator-(const Vec<NumType,NumDims> & rhs) const
  {
    Vec<NumType,NumDims> result;
    for(int i=0;i<NumDims;i++) result.x[i] = x[i]-rhs.x[i];
    return result;
  }
  Vec<NumType,NumDims> operator-() const
  {
    Vec<NumType,NumDims> result;
    for(int i=0;i<NumDims;i++) result.x[i] = -x[i];
    return result;
  }
  Vec<NumType,NumDims> & operator+=(const Vec<NumType,NumDims> & rhs) 
  {
    return *this=(*this)+rhs;
  }
  Vec<NumType,NumDims> & operator-=(const Vec<NumType,NumDims> & rhs)
  {
    return *this=(*this)-rhs;
  }
  Vec<NumType,NumDims> & operator*=(NumType a)
  {
    return *this=(*this)*a;
  }
  Vec<NumType,NumDims> & operator/=(NumType a)
  {
    return *this=(*this)/a;
  }

  Vec< NumType, NumDims > operator/( const Vec< NumType, NumDims > rhs ) const
  {
    Vec<NumType,NumDims> result;
    for ( int i = 0; i < NumDims; ++i )
      result[i] = x[i] / rhs.x[i];
    return result;
  }

  Vec<NumType,NumDims> operator^(const Vec<NumType,NumDims> & rhs) const
  {
    Vec<NumType,NumDims> result;
    if(NumDims>=3)
    {
      result.x[0]=x[1]*rhs.x[2]-x[2]*rhs.x[1];
      result.x[1]=x[2]*rhs.x[0]-x[0]*rhs.x[2];
      result.x[2]=x[0]*rhs.x[1]-x[1]*rhs.x[0];
    }
    return result;
  }

  Vec<NumType,NumDims> & operator^=(const Vec<NumType,NumDims> & rhs)
  {
    return *this=(*this)^rhs;
  }

  bool operator<(NumType rhs) const
  {
    bool lt=true;
    for(int i=0;i<NumDims && (lt=(x[i]<rhs));i++);
    return lt;
  }

  inline NumType &operator[]( int _n )
  {
    return x[_n];
  }

  inline NumType operator[]( int _n ) const
  {
    return x[_n];
  }

  static Vec<NumType,NumDims> min(const Vec<NumType,NumDims> & a, const Vec<NumType,NumDims> & b)
  {
    Vec<NumType,NumDims> result;
    for(int i=0;i<NumDims;i++)
    {
      result.x[i]=(a.x[i]<b.x[i]?a.x[i]:b.x[i]);
    }
    return result;
  }

  static Vec<NumType,NumDims> max(const Vec<NumType,NumDims> & a, const Vec<NumType,NumDims> & b)
  {
    Vec<NumType,NumDims> result;
    for(int i=0;i<NumDims;i++)
    {
      result.x[i]=(a.x[i]>b.x[i]?a.x[i]:b.x[i]);
    }
    return result;
  }

  Vec<NumType,NumDims> abs() const
  {
    Vec<NumType,NumDims> result;
    for(int i=0;i<NumDims;i++)
    {
      result.x[i]=(x[i]>0?x[i]:-x[i]);
    }
    return result;
  }

  NumType min() const
  {
    NumType result=x[0];
    for(int i=0;i<NumDims;i++)
    {
      result=(result<x[i]?result:x[i]);
    }
    return result;
  }


  NumType max() const
  { 
    NumType result=x[0];
    for(int i=0;i<NumDims;i++)
    {
      result=(result>x[i]?result:x[i]);
    }
    return result;
  }

  template <class NType, ushort NDims>
  friend ostream & operator<<(ostream & out, const Vec<NType,NDims> & v) ;
  template <class NType, ushort NDims>
  friend Vec<NType,NDims> operator*(NType a, const Vec<NType,NDims> & rhs);
};

template<class NType,ushort NDims>
Vec<NType,NDims> operator*(NType a, const Vec<NType,NDims> & rhs) 
{
  return rhs*a;
}
template<class NType,ushort NDims>
ostream & operator<<(ostream & out, const Vec<NType,NDims> & v) 
{
  out << "("; 
  for(int i=0;i<NDims;i++)
  {	
    out << v.x[i];
    if(i<NDims-1) out << ",";
  }
  out << ")";
  return out;
}

typedef Vec<float, 3 >  Vec3f;
typedef Vec<double, 3 > Vec3d;
typedef Vec<float, 4 > Vec4f;

#endif

