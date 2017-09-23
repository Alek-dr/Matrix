package MatrixLib;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by alexander on 22.09.17.
 */
public class FunctionParser {

    static final Pattern justNumb =  Pattern.compile("(-|\\+)?\\d(\\.\\d)?");
    static final Pattern variable = Pattern.compile("[-+]?\\d?(\\.\\d)?[Xx][0-9]*");

    public static double[] getCoefficient(String z){
        double N = 0;
        z = z.replaceAll(",",".");
        Matcher matcher = variable.matcher(z);
        List<String> expr = new ArrayList<>();
        //Находим переменные
        while (matcher.find()){
            String v = matcher.group();
            if(v=="") continue;
            expr.add(v);
            z = z.replace(v, "");
        }
        //Теперь выражение содержит только свободные члены
        matcher = justNumb.matcher(z);
        while (matcher.find()){
            String num = matcher.group();
            if(num=="") continue;
            if(num.charAt(num.length()-1)=='-'|num.charAt(num.length()-1)=='+')
                num = num.substring(0,num.length()-1);
            N+=Double.parseDouble(num);
            z = z.replace(num, "");
        }
        //coef - коэффициенты при X
        //x - порядковый номер переменной
        List<Double> coef = new ArrayList<>();
        List<Integer> x = new ArrayList<>();
        for(String c : expr){
            String [] parts = c.split("X|x");
            if(parts.length==1){
                x.add(Integer.parseInt(parts[1]));
                coef.add(1.0);
            }else{
                if(parts[0].equals(""))
                    coef.add(1.0);
                else coef.add(Double.parseDouble(parts[0]));
                x.add(Integer.parseInt(parts[1]));
            }
        }
        int size = x.stream().max(Integer::compare).get()+1;
        double [] coefficients = new double[size];
        for(int i=0; i<x.size(); i++){
            int c = x.get(i)-1;
            coefficients[c] = coef.get(i);
        }
        coefficients[size-1] = N; //свободный член
        return coefficients;
    }
}
