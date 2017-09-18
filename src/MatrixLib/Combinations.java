package MatrixLib;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by alexander on 18.09.17.
 */
public class Combinations {

    static int numCombinations = 0;
    static int counter = 0;
    static List<Integer[]> comb = new ArrayList<>();

    static void getCombinations(List<Integer> nums, Integer prev [], int k){
        ++counter;
        if(nums.size() < k) return;
        else if (k==0){
            ++numCombinations;
            comb.add(prev);
        }else{
            Integer first = nums.get(0);
            getCombinations(getSubArray(nums), addElem(prev,first), k-1);
            getCombinations(getSubArray(nums), prev, k);
        }
    }

    private static List<Integer> getSubArray(List<Integer> nums){
        int n = nums.size();
        List<Integer> subArray = new ArrayList<>();
        for(int i=1; i<n;i++)
            subArray.add(nums.get(i));
        return subArray;
    }

    private static Integer[] addElem(Integer prev [],int elem){
        int n = prev.length;
        Integer sum [] = new Integer[n+1];
        sum[n] = elem;
        for(int i=0; i<n; i++)
            sum[i] = prev[i];
        return sum;
    }

    protected static int findCombIndex(Integer[] currComb){
        boolean find;
        for(int i=0; i<comb.size(); i++){
            find = true;
            Integer [] c = comb.get(i);
            for(int j=0; j<c.length; j++){
                if(c[j]!=currComb[j]){
                    find = false;
                    break;
                }
            }
            if (find) return i;
        }
        return -1;
    }
}
