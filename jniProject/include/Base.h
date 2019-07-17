#include <iostream>
namespace test {
class Base1 {
private:
 virtual std::string getName();
public:
  Base1(/* args */);
  int _age = 6;
  void notify();
  ~Base1();
};

} // namespace test
