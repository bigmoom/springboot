package com.cwh.springboot.springsecurity.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Date;

/**
 * @author cwh
 * @date 2021/6/15 9:13
 */
@Slf4j
@Component
public class JWTManager {

    @Value("${security.jwt.securityKey}")
    private String securityKey;


//  设置过期时间
    private Duration expiration = Duration.ofDays(1);
    /**
     * 通过用户名加密
     * @param name
     * @return
     */
    public String generate(String name){
        Date expiraDate =new Date(System.currentTimeMillis() + expiration.toMillis());

        //构建token
        return Jwts.builder()
                .setExpiration(expiraDate)
                .setSubject(name)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512,securityKey)
                .compact();
    }

    /**
     * 解密，成功返回claims对象，失败返回null
     * claims为对象存储token有效信息的载荷
     * @param token
     * @return
     */
    public Claims parse(String token){
//      空字符串直接返回null
//      代表当前没有携带token
        if(!StringUtils.hasLength(token)){
            return null;
        }

//      非空字符串则解析
        Claims claims = null;

        log.info("token:"+token);
        try{
            claims = Jwts.parser()
                    .setSigningKey(securityKey)
                    .parseClaimsJws(token)
                    .getBody();

        }
        catch(JwtException e){
            log.error("解析失败",e.toString());
        }
        return claims;
    }
}
