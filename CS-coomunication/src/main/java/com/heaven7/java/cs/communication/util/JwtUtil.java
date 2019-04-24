package com.heaven7.java.cs.communication.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class JwtUtil {

    public static final String SERCETKEY = "#cs_communication#";
    public static final long KEEPTIME = 3600 * 1000 * 24 * 7;

    public static String generateToken(String id, String subject) {
        return generateToken(id, subject, KEEPTIME);
    }
    public static String generateToken(String id, String subject, long keepTimeInMills) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("id", id);

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                //for secure
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setSubject(subject)
                .signWith(signatureAlgorithm, SERCETKEY);
        if (keepTimeInMills >= 0) {
            long expMillis = nowMillis + keepTimeInMills;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }
        return builder.compact();
    }

    public static String updateToken(String token){
        Claims claims = parseJWT(token);
        String subject = claims.getSubject();
        String id = claims.get("id").toString();
        return generateToken(id, subject, KEEPTIME);
    }

    public static Claims parseJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SERCETKEY)
                .parseClaimsJws(token).getBody();
        return claims;
    }


    public static Boolean verify(String token, String id) {
        Claims claims = Jwts.parser()
                .setSigningKey(SERCETKEY)
                .parseClaimsJws(token).getBody();

        if (claims.get("id").equals(id)) {
            return true;
        }
        return false;
    }

}