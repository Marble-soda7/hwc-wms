package com.hwc.wms.common.result;

import lombok.Data;

/**
 * 统一返回结果
 */
@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    private Result() {
    }

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ========== 成功 ==========

    public static <T> Result<T> ok() {
        return new Result<>(ResultCode.SUCCESS, "success", null);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS, "success", data);
    }

    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(ResultCode.SUCCESS, message, data);
    }

    // ========== 失败 ==========

    public static <T> Result<T> fail() {
        return new Result<>(ResultCode.ERROR, "操作失败", null);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultCode.ERROR, message, null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }

    // ========== 参数错误 ==========

    public static <T> Result<T> invalidParam(String message) {
        return new Result<>(ResultCode.INVALID_PARAM, message, null);
    }

    // ========== 未登录/无权限 ==========

    public static <T> Result<T> unauthorized() {
        return new Result<>(ResultCode.UNAUTHORIZED, "请先登录", null);
    }

    public static <T> Result<T> forbidden() {
        return new Result<>(ResultCode.FORBIDDEN, "无权限访问", null);
    }

    /**
     * 状态码常量
     */
    public static class ResultCode {
        public static final int SUCCESS = 200;
        public static final int ERROR = 500;
        public static final int INVALID_PARAM = 400;
        public static final int UNAUTHORIZED = 401;
        public static final int FORBIDDEN = 403;
    }
}
