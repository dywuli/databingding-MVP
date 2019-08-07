#include <iostream>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <regex>
#ifdef __cplusplus
extern "C"
{
#endif
  namespace test
  {
  std::string Utf8SubStr(const std::string &name, size_t start,
                         size_t end)
  {
    size_t i = 0; // Initialized the params
    size_t j = 0;
    size_t k = 0;
    size_t l = 0;
    while (i < start && j < name.length())
    {
      unsigned char c = (unsigned char)name[j++];
      i += ((c & 0xc0) != 0x80);
    }
    while (j < name.length())
    {
      unsigned char c = (unsigned char)name[j];
      if ((c & 0xc0) == 0x80)
      {
        j++; // J saved chinese utf8 end position
        std::cout << "Utf8SubStr j 0000... " << j << std::endl;
      }
      else
      {
        break;
      }
    }
    k = j;
    while (i < start + end && j < name.length())
    {
      unsigned char c = (unsigned char)name[j++];
      i += ((c & 0xc0) != 0x80);
      std::cout << "Utf8SubStr i ..." << i << std::endl;
      std::cout << "Utf8SubStr j ... " << j << std::endl;
    }
    while (j < name.length())
    {
      unsigned char c = (unsigned char)name[j];
      if ((c & 0xc0) == 0x80)
      {
        j++; // J saved chinese utf8 end position
        std::cout << "Utf8SubStr j 0000... " << j << std::endl;
      }
      else
      {
        break;
      }
    }
    // i = 0;
    // j = 0;
    // while (i < start+end && j < name.length())
    // {
    //   unsigned char c = (unsigned char)name[j++];
    //   i += ((c & 0xc0) != 0x80);
    //   while (j < name.length())
    //   {
    //     unsigned char c = (unsigned char)name[j];
    //     if ((c & 0xc0) == 0x80)
    //     {
    //       j++; // J saved chinese utf8 end position
    //     }
    //     else
    //     {
    //       l = j;
    //       break;
    //     }
    //   }
    // }
    std::cout << "Utf8SubStr k " << k << std::endl;
    std::cout << "Utf8SubStr l " << l << std::endl;
    std::cout << "Utf8SubStr j " << j << std::endl;
    std::cout << "Utf8SubStr i " << i << std::endl;
    return name.substr(k, j - k); // Acuqired the chinese name sub_str
  }
  std::string Utf8SubStrA(const std::string &name, size_t need)
  {
    size_t i = 0;
    size_t j = 0;
    while (i < need && j < name.length())
    {
      unsigned char c = (unsigned char)name[j++];

      i += ((c & 0xc0) != 0x80);
    }

    while (j < name.length())
    {
      unsigned char c = (unsigned char)name[j];
      if ((c & 0xc0) == 0x80)
      {
        j++;
      }
      else
      {
        break;
      }
    }
    return name.substr(0, j);
  }
  int main(int arg, char *agvs[])
  {
    const char **names = new const char *[100]; //指针数组
    int *par1[3];
    int *par = new int[3];
    *par = 1;
    *(par + 1) = 2;
    *(par + 2) = 3;
    par1[0] = par;
    par1[1] = par + 1;
    par1[2] = par + 2;
    printf("%d,%d,%d\n", *par1[0], *par1[1], *par1[2]);
    const int **ptr = new const int *[3];
    for (size_t i = 0; i < 5; i++)
    {
      std::string str_name = "na";
    }
    std::string str_name = "name1";
    char c = 0;
    printf("%s,%c\n", "nnnnn:", c);
    names[1] = &c;
    names[0] = str_name.c_str();
    delete[] names;
    std::string name = "阿里";
    std::cout << "动h力给个哥" << name.size() << std::endl;
    std::string subName = Utf8SubStr(name, 0, 2);
    std::cout << "bbbb33" << subName << std::endl;
    int i = 10;
    std::cout << "     " << i << std::endl;
    std::cout << "bbbb33 size " << subName.size() << std::endl;
    // std::cout << "bbbb" << Utf8SubStrA(name, 4) << std::endl;
    int *p;
    
    std::cout << "     " << i << std::endl;
    p = &i;
    std::cout << "     " << *p << std::endl;
    memset(p,2,sizeof(int));
    std::cout << "     " << p << std::endl;

  }
  } // namespace test
#ifdef __cplusplus
}
#endif
