#include "../include/sub.h"
#include "../include/String.h"
#include <memory>
namespace test {
sub::sub() : str(0){}
sub::~sub() {}
// std::string sub::getName() { return "sub"; }
void sub::setName(const char *data[]) {
  while (data) {
    std::cout << *data << std::endl;
    std::auto_ptr<String> pSub(new String());
    std::shared_ptr<String> pstr = std::make_shared<String>("ninnn");
    data++;
  }
}
sub &sub::operator=(const sub &s) {
  /* 方法一
  std::string *strTemp = str;
  str = new std::string(*s.str);
  delete strTemp;*/
  ///方法二
  
  return *this;
}
std::string sub::getName() {
return "sub";
}
template <typename T> void swap(T &a, T &b) {
    T temp(a);
    a = b;
    b = temp;
}
} // namespace test