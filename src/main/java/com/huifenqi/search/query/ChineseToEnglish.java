package com.huifenqi.search.query;

import net.sourceforge.pinyin4j.PinyinHelper;  
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;  
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;  
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;  
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;  
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;  
  
public class ChineseToEnglish {  
    // 将汉字转换为全拼  
    public static String getPingYin(String src) {  
  
        char[] t1 = null;  
        t1 = src.toCharArray();  
        String[] t2 = new String[t1.length];  
        HanyuPinyinOutputFormat t3 = new HanyuPinyinOutputFormat();  
          
        t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);  
        t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);  
        t3.setVCharType(HanyuPinyinVCharType.WITH_V);  
        String t4 = "";  
        int t0 = t1.length;  
        try {  
            for (int i = 0; i < t0; i++) {  
                // 判断是否为汉字字符  
                if (java.lang.Character.toString(t1[i]).matches(  
                        "[\\u4E00-\\u9FA5]+")) {  
                    t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3); 
                    if(t2!=null)
                    	t4 += t2[0];  
                } else if (java.lang.Character.toString(t1[i]).contentEquals("1"))
                {
                	t4+= "yi";
                	
                }else if (java.lang.Character.toString(t1[i]).contentEquals("2"))
                {
                	t4+= "er";
                	
                }
                else if (java.lang.Character.toString(t1[i]).contentEquals("3"))
                {
                	t4+= "san";
                	
                }
                else if (java.lang.Character.toString(t1[i]).contentEquals("4"))
                {
                	t4+= "si";
                	
                }
                else if (java.lang.Character.toString(t1[i]).contentEquals("5"))
                {
                	t4+= "wu";
                	
                }
                else if (java.lang.Character.toString(t1[i]).contentEquals("6"))
                {
                	t4+= "liu";
                	
                }
                else if (java.lang.Character.toString(t1[i]).contentEquals("7"))
                {
                	t4+= "qi";
                	
                }
                else if (java.lang.Character.toString(t1[i]).contentEquals("8"))
                {
                	t4+= "ba";
                	
                }
                else if (java.lang.Character.toString(t1[i]).contentEquals("9"))
                {
                	t4+= "jiu";
                	
                }else
                    t4 += java.lang.Character.toString(t1[i]);  
            }  
            // System.out.println(t4);  
            return t4;  
        } catch (BadHanyuPinyinOutputFormatCombination e1) {  
            e1.printStackTrace();  
        }  
        return t4;  
    }  
  
    // 返回中文的首字母  
    public static String getPinYinHeadChar(String str) {  
  
        String convert = "";  
        for (int j = 0; j < str.length(); j++) {  
            char word = str.charAt(j);  
            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);  
            if (pinyinArray != null) {  
                convert += pinyinArray[0].charAt(0);  
            } else {  
                convert += word;  
            }  
        }  
        return convert;  
    }  
  
    // 将字符串转移为ASCII码  
    public static String getCnASCII(String cnStr) {  
        StringBuffer strBuf = new StringBuffer();  
        byte[] bGBK = cnStr.getBytes();  
        for (int i = 0; i < bGBK.length; i++) {  
            strBuf.append(Integer.toHexString(bGBK[i] & 0xff));  
        }  
        return strBuf.toString();  
    }  
  
//    public static void main(String[] args) {  
//        System.out.println(getPingYin("綦江qq县"));  
//        System.out.println(getPinYinHeadChar("綦江县"));  
//        System.out.println(getCnASCII("綦江县"));  
//    }  
}  



