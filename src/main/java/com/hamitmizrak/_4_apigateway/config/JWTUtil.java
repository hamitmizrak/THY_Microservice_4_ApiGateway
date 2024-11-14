package com.hamitmizrak._4_apigateway.config;


/*
API Gateway, JWT token doğrulaması yaparak örneğin address-service güvenli erişim sağlayabilir.
Bunun için Spring Security kullanarak JWT doğrulaması yapılabilir.
Öncelikle, JWTUtil sınıfını ekleyerek token doğrulama işlemlerini gerçekleştirin:
 */

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

// Bu sınıf, JWT token oluşturma ve doğrulama işlemleri için yardımcı bir sınıf sağlar.
//@Component
public class JWTUtil {

    // Token imzalamak için kullanılan gizli anahtar.
    private final String SECRET_KEY = "secret";

    // Belirli bir kullanıcı adı için JWT token oluşturur.
    public String generateToken(String username) {
        // JJWT kütüphanesi ile token oluşturma işlemi başlatılıyor.
        return Jwts.builder()
                .setSubject(username) // Token için konu (subject) olarak kullanıcı adı belirleniyor.
                .setIssuedAt(new Date(System.currentTimeMillis())) // Token oluşturulma tarihi ekleniyor.
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Token 10 saat geçerli olacak şekilde son kullanma tarihi ayarlanıyor.
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // Token, HS256 algoritması ve SECRET_KEY kullanılarak imzalanıyor.
                .compact(); // Token oluşturulup string formatında döndürülüyor.
    }

    // Bir JWT token'ın geçerliliğini kontrol eder.
    public boolean validateToken(String token) {
        try {
            // Token, SECRET_KEY kullanılarak çözülüyor ve doğruluğu kontrol ediliyor.
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true; // Token geçerliyse true döndürülüyor.
        } catch (Exception e) {
            // Herhangi bir hata durumunda (örn. token süresi dolmuş veya geçersiz), false döndürülüyor.
            return false;
        }
    } //end validateToken

} //end JWTUtil
