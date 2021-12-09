package util;

public class MathUtil {
    public static int maxInList(int[] list){
        int prev = Integer.MIN_VALUE;
        for(int i=0;i<list.length;i++){
            if(list[i]>prev)
                prev = list[i];
        }
        return prev;
    }
}
