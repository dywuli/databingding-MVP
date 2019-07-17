#include "../include/Base.h"
namespace test {
Base1::Base1() {}
Base1::~Base1() {}
std::string Base1::getName() {
    return "base";
}
std::string getNameSS() {
  return "Base";
}
void Base1::notify() {
  std::cout<<"22222222222"<<getName();
}
} // namespace test