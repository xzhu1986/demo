package au.com.isell.rlm.module.jbe.util;

public class ConvertPass{

    private static final String KEYSTRING = "Jqo3y2951CU9Td86[7<r2<w,Xn^/Uf";
    private static final Integer MAX_LOOP = 30;

    public ConvertPass(){}

    /**
     *
     * @param strUserName
     * @param strPassword
     * @return
     */
    private static final synchronized String convert(String strUserName,String strPassword) {
    	if (strPassword == null){strPassword = "";}
//    	if (strUserName == null){strUserName = "";}
//
//    	strUserName = strUserName.trim().toLowerCase();
    	strPassword = strPassword.trim().toLowerCase();

    	if ("".equals(strPassword)){return "";}

    	StringBuffer strNewPassword=new StringBuffer();
    	String fromWord="";
    	String numberString="";
    	int asciiTotal = 0;
    	int count=0;
    	int len=0;

    	// Step 1
    	fromWord = strPassword+KEYSTRING;
    	for(count=0;count<MAX_LOOP;count++){
    		asciiTotal += (int)(fromWord.charAt(count));
    	}

    	// Step 2
    	for(count=0;count<MAX_LOOP;count++){
    		numberString += (int)(fromWord.charAt(count))*(int)(KEYSTRING.charAt(count))*asciiTotal;
    	}

    	// Step 3
    	numberString = numberString.substring(strPassword.length(),numberString.length());
    	numberString = numberString.substring(0,numberString.length()-strPassword.length());

		len = numberString.length();
		if (len%2>0){len--;}
    	for (count=0;count<len;count+=2){
    		int rangeValue = (int)(Integer.valueOf(numberString.substring(count,count+2)) * 0.92) + 33;
    		strNewPassword.append((char)rangeValue);
    	}
    	return strNewPassword.toString();
    }

    /**
     *
     * @param strUserName
     * @param strPassword
     * @return
     */
    public static String getEncryptString(String strUserName,String strPassword){
    	return convert(strUserName,strPassword);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
    	String strPassword = convert("Fany","peace");
//    	String strPassword = convert("paulp","abc");
        System.out.println(strPassword);
   }
}