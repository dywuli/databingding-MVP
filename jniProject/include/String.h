#include <iostream>
namespace test
{
class String
{
   private:
     int _size;
     char *_String;

   public:
     String();
     String(const char *);
     String(const String &);
     String &operator=(const String &);
     String &operator=(const char *);
     bool operator==(const String &);
     bool operator==(const char *);
     char &operator[](int);
     int size();
     char *c_str();
     ~String();
};
} // namespace test