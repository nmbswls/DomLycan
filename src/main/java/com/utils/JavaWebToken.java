package com.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class JavaWebToken {

    private static Logger log = LoggerFactory.getLogger(JavaWebToken.class);

    //该方法使用HS256算法和Secret:bankgl生成signKey
    private static Key getKeyInstance() {
        //We will sign our JavaWebToken with our ApiKey secret
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary("sonTaoSmelledSb");
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
        return signingKey;
    }

    //使用HS256签名算法和生成的signingKey最终的Token,claims中是有效载荷
    public static String createJavaWebToken(Map<String, Object> claims, Long lifeTime) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT")
                .setIssuer("sender")
                .setAudience("receiver")
                .setIssuedAt(now).
                setClaims(claims).signWith(SignatureAlgorithm.HS256, getKeyInstance());
        if (lifeTime >= 0) {
            long expMillis = nowMillis + lifeTime*1000;
            Date exp = new Date(expMillis);

            builder.setExpiration(exp).setNotBefore(now);
        }

        return builder.compact();
    }


    //
//    nbf - 1518148957
//    exp - 1518152557

    //解析Token，同时也能验证Token，当验证失败返回null
    public static Map<String, Object> parserJavaWebToken(String jwt) {
        try {
            Map<String, Object> jwtClaims =
                    Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(jwt).getBody();

            return jwtClaims;
        } catch (Exception e) {
            log.error("json web token verify failed");
            return null;
        }
    }

    public static Map<String, Object> parserJavaWebToken2(String jwt) {
        try {
            Map<String, Object> jwtClaims =
                    Jwts.parser().setSigningKey(getKeyInstance()).parseClaimsJws(jwt).getBody();
            if(!isTokenValid(jwtClaims)){
                return null;
            }
            return jwtClaims;
        } catch (Exception e) {
            log.error("json web token verify failed");
            return null;
        }
    }




    public static boolean isTokenValid(Map<String,Object> claims){

        Long exp = null;
        Long nbf = null;
        try{
            exp = Long.parseLong(claims.get("exp").toString());
            nbf = Long.parseLong(claims.get("nbf").toString());

        }catch (Exception e){

            return false;
        }

        Long now = System.currentTimeMillis()/1000;

        if(now<nbf){
            return false;

        }
        if(now>exp){
            return false;

        }
        return true;
    }







    //=-------------------------

//    public static void verifyToken(String token,String key) throws Exception{
//
//        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(key))
//                .build();
//        DecodedJWT jwt = verifier.verify(token);
//        Map<String, Claim> claims = jwt.getClaims();
//        System.out.println(claims.get("name").asString());
//    }
//
//    public static String createToken() throws Exception{
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("alg", "HS256");
//        map.put("typ", "JWT");
//        String token = JWT.create()
//                .withHeader(map)//header
//                .withClaim("name", "zwz")//payload
//                .withClaim("age", "18")
//                .sign(Algorithm.HMAC256("secret"));//加密
//        return token;
//    }

}