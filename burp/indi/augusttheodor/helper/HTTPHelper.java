package burp.indi.augusttheodor.helper;

import burp.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.*;

//deal http response
//in fact it is a static class, but java not allow me to clarify that.
public class HTTPHelper {

    public static void deleteResponseDebugger(IExtensionHelpers helpers, IHttpRequestResponse http){ //function deal js response
        byte[] response=http.getResponse();
        String evalOverride="(function() { 'use strict';var eval1 = window.eval;window.eval = function(x){eval1(x.replace(\"debugger;\",\";\"));};window.eval.toString = eval1.toString;})();"; //神秘代码，用了就进入二次元（重载eval函数）
        IResponseInfo info=helpers.analyzeResponse(response);
        int original_in=0; //content length 长度
        int edited_in=0; //修改后长度
        String url=helpers.analyzeRequest(http).getUrl().getPath();
        if(url.endsWith(".js") || url.contains(".js?")){
            //need to determine if this response is a js response
            byte[] byteHeader= Arrays.copyOfRange(response,0,info.getBodyOffset());
            byte[] byteBody= Arrays.copyOfRange(response,info.getBodyOffset(),response.length); //slice head and body
            String head=new String(byteHeader,StandardCharsets.UTF_8).intern();
            String out=new String(byteBody, StandardCharsets.UTF_8).intern();;
            out=out.replace("debugger;","");
            out=out.replace("debugger","");
            out=evalOverride+out; //增加重载eval的部分
            List<String> headers=helpers.analyzeResponse(response).getHeaders();
            for (String h:headers) {
                if(h.contains("Content-Length")){
                    original_in=Integer.parseInt(h.replace("Content-Length: ",""));
                    break;
                }
            }
            edited_in=original_in+evalOverride.getBytes().length;
            head=head.replace("Content-Length: "+Integer.toString(original_in),"Content-Length: "+Integer.toString(edited_in)); //替换长度
            http.setResponse((head+out).getBytes());
            BurpExtender.so.println("js response found,had execute filter.");
        }
    }

    public static void deleteRequestScriptParam(IExtensionHelpers helpers, IHttpRequestResponse http){ //function deal js request
        byte[] request=http.getRequest();
        IRequestInfo info=helpers.analyzeRequest(request);
        String url=helpers.analyzeRequest(http).getUrl().getPath();
        if(url.contains(".js?") && !info.getParameters().isEmpty() && Objects.equals(info.getMethod(), "GET")) { //删除所有带参数请求的JS的参数
            String body = new String(Arrays.copyOfRange(request, info.getBodyOffset(), request.length), StandardCharsets.UTF_8).intern();
            String header = new String(Arrays.copyOfRange(request, 0, info.getBodyOffset()), StandardCharsets.UTF_8).intern();
            String pattern = "\\.js\\?([^ ]*) HTTP"; //pattern of params
            header = HTTPHelper.RegReplacement(pattern, header, ".js HTTP");
            http.setRequest((header + body).getBytes());
            BurpExtender.so.println("js request found,had execute filter.");
        }
    }

    private static String RegReplacement(String pattern, String str, String replacement){ //真懒得放洋屁了，这是正则表达式的替换函数
        Matcher matcher=Pattern.compile(pattern).matcher(str);
        while(matcher.find()){
            try{
                str=str.replace(matcher.group(1),replacement);
            }catch (Exception e){
                BurpExtender.so.println("replace error. please check up your REGEX sentences.");
            }
        }
        return str;
    }

}
