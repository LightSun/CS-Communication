package com.heaven7.java.cs.communication.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author heaven7
 */
public class JwtUtilTest {

    @Test
    public void test1() {
        String token = JwtUtil.generateToken("heaven7", "12345");
        Assert.assertTrue(JwtUtil.verify(token, "heaven7"));
        String newToken = JwtUtil.updateToken(token);
        Assert.assertTrue(JwtUtil.verify(newToken, "heaven7"));

        System.out.println(token);
        System.out.println(newToken);
    }
}
