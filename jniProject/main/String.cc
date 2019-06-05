#include <cstring>
#include "../include/String.h"
namespace test
{
String::String()
{
     _size = 0;
     _String = 0;
}
String::String(const char *str)
{
     if (!str)
     {
          _size = 0;
          _String = 0;
     }
     else
     {
          _size = strlen(str);
          _String = new char(_size + 1);
          strcpy(_String, str);
     }
}
String::String(const String &rhs)
{
     _size = rhs._size;
     if (!rhs._String)
     {
          _String = 0;
     }
     else
     {
          _String = new char(_size + 1);
          _String = strcpy(_String, rhs._String);
     }
}
bool String::operator==(const char *str)
{
     return strcmp(_String, str) ? false : true;
}
bool String::operator==(const String &str)
{
     if (_size != str._size)
     {
          return false;
     }
     return strcmp(_String, str._String) ? false : true;
}
String &String::operator=(const char *str)
{
     if (!str)
     {
          _size = 0;
          delete[] _String;
          _String = 0;
     }
     else
     {
          _size = strlen(str);
          delete[] _String;
          _String = new char[_size + 1];
          strcpy(_String, str);
     }
     return *this;
}
String &String::operator=(const String &str)
{
     if (&str != this)
     {
          delete[] _String;
          _size = str._size;
          if (!str._String) {
               _String = 0;
          } else
          {
               _String = new char[_size + 1];
               strcpy(_String, str._String);
          }
          
     }
     return *this;
}
char &String::operator[](int i)
{
     if (i >= 0 && i < _size)
     {
          return _String[i];
     }
}
int String::size()
{
     return _size;
}
char *String::c_str()
{
     return _String;
}
String::~String()
{
     delete[] _String;
}

} // namespace test