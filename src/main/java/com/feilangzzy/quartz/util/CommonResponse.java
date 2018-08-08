package com.feilangzzy.quartz.util;

import java.io.Serializable;

public class CommonResponse implements Serializable {
    private boolean success;
    private String msg;
    private Object data;
    private String code;

    public CommonResponse() {

    }

    public static CommonResponse returnCommonResponse(boolean isOK,String msg,Object data){
        CommonResponse commonResponse=new CommonResponse();
        commonResponse.setSuccess(isOK);
        commonResponse.setMsg(msg);
        commonResponse.setData(data);
        return commonResponse;
    }

    public CommonResponse(boolean success) {
        this.success = success;
    }

    public CommonResponse(boolean success, String msg){
        this.success = success;
        this.msg = msg;
    }

    public CommonResponse(boolean success, String successMsg, String failMsg){
        this.success = success;
        this.msg = this.success? successMsg : failMsg;
    }

    public CommonResponse(boolean success, Object data, String msg){
        this.success = success;
        this.data = data;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
