//#- encoding utf-8
package burp;

import burp.indi.augusttheodor.helper.HTTPHelper;

import java.io.PrintWriter;

//main extender class
//stupid name rule kill the biggest pleasure as a programmer while the ruined IDEA coding type kill the others.
public class BurpExtender implements IBurpExtender,IHttpListener {

    private IExtensionHelpers helpers;
    private IBurpExtenderCallbacks call;
    private static final String VERSION = "0.1.0";
    public static PrintWriter so;

    public BurpExtender(){}

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks call) {
        this.call=call;
        this.call.setExtensionName("DebuggerKiller "+BurpExtender.VERSION);
        this.helpers=this.call.getHelpers();
        this.call.registerHttpListener(BurpExtender.this); //register the listener
        BurpExtender.so = new PrintWriter(call.getStdout(), true);
        BurpExtender.so.println("@Author AugustTheodor \n"
                +"@Github https://github.com/AugustineFulgur/ \n"
                +"@Use delete all 'debugger;' in http js response. \n"
                +"@�÷� �˲���Զ���������debugger����װ���֮���������������沢���¼���ҳ�棨��ʹ���޺�ģʽ��ҳ�棩��");
    }

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
        if(toolFlag==4){
            if(!messageIsRequest){ //only delete response in PROXY(flag 4)
                HTTPHelper.deleteResponseDebugger(this.helpers,messageInfo);
            }else{
                HTTPHelper.deleteRequestScriptParam(this.helpers,messageInfo);
            }
        }

    }
}
