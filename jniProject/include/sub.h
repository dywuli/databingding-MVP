#include "Base.h"
namespace test {
class String;
class sub : public Base1 {
private:
  /* data */
  std::string *str;

public:
  sub(/* args */);
  virtual std::string getName();
  void setName(const char *data[]);
  ~sub();
  sub &operator=(const sub &s);
  int _age = 7;
  void setNamess(String str);
};
std::string getNameSS();
template <typename T> void swap(T &a, T &b);
} // namespace test