package com.teamsoft.teamadmin.exception;

/**
 * 校验异常
 * @author zhangcc
 * @version 2017/11/14
 */
public class VerifyException extends RuntimeException {
	public VerifyException(String message) {
		super(message);
	}
}