import java.lang.*;
import java.util.Arrays;
public class Test {
    public static void main(String[] agrs) {
        int[] array = {120, 2, 9, 78, 90, 98};
        getMax(array, 3);

    }
    private static void getMax(int[] array, int maxLength) {
        if(null != array) {
            int size = array.length;
            if (size == 0) return;
            String[] strings = new String[size];
            int count = 0;
            for(int i : array) {
                strings[count] = "" + i;
                int ii = maxLength - strings[count].length();
                while(ii > 0) {
                    strings[count] +="o";
                    ii--;
                }
                System.out.println(strings[count]);
                count++;
            }
            
            Arrays.sort(strings);
            for(String str : strings) {
                System.out.println(str);
            }
            for(int i= strings.length -1;i>=0;i--) {
                char[] myChars = strings[i].toCharArray();
                for(char c : myChars) {
                    if(c != 'o') {
                        System.out.print(c);
                    }
                }
            }
        }
    }
}