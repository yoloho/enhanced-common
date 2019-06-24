package com.yoloho.enhanced.common.util.base64;

import org.junit.Test;

import com.yoloho.enhanced.common.util.base64.Base64Util;

public class Base64Test {

	@Test
	public void testEncode() {
		String src = "yoloho123";
		String dest = Base64Util.encodeToString(src.getBytes(), Base64Util.DEFAULT);
		System.out.println(src + " <> " + dest);
	}
}
