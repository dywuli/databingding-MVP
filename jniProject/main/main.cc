#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include "../include/String.h"
#ifdef __cplusplus
extern "C"
{
#endif
     namespace test
     {
     int main(int arg, char *agvs[])
     {
          String name, the("the\n"), it("it\n");
          std::cout << the.c_str();
          name = it;
          std::cout << it[1];
          std::cout << name.c_str();
          std::cout << name.size();
          return 1;
     }
     } // namespace test
#ifdef __cplusplus
}
#endif