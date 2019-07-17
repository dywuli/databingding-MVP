#include "../include/sub.h"
#include <stdio.h>
#include <stdlib.h>
#ifdef __cplusplus
extern "C" {
#endif
namespace test {
int main(int arg, char *agvs[]) {
  Base1 *base = new sub();
  base->notify();
  std::cout << base->_age << std::endl;
  const char **names = new const char *[100];//指针数组
  int *par1[3];
  int *par = new int[3];
  *par = 1;
  *(par+1) = 2;
  *(par+2) = 3;
  par1[0] = par;
  par1[1] = par+1;
  par1[2] = par+2;
  printf("%d,%d,%d\n", *par1[0], *par1[1], *par1[2]);
  const int **ptr = new const int *[3];
  printf(names[0]);
  for (size_t i = 0; i < 5; i++) {
    std::string str_name = "na";
  }
  std::string str_name = "name1";
  char c = 0;
  printf("%s,%c\n","nnnnn:",c);
  names[1] = &c;
  names[0] = str_name.c_str();
  delete[] names;
  std::cout<<"bbbb" <<getNameSS()<<std::endl;
  return 1;
}
} // namespace test
#ifdef __cplusplus
}
#endif
