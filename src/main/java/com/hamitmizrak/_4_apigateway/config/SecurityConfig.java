package com.hamitmizrak._4_apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

// Öncelikle, JWTUtil sınıfını ekleyerek token doğrulama işlemlerini gerçekleştirin: (JWTUtil )
// Daha sonra, API Gateway’e gelen her istekte JWT doğrulaması yapmak için bir güvenlik yapılandırması ekleyin: (SecurityConfig)

// Bu sınıf, Spring Security için güvenlik yapılandırmalarını tanımlayan bir konfigürasyon sınıfıdır.
@Configuration
public class SecurityConfig {

    // Bu yapılandırmada oauth2Login() kaldırılarak temel bir güvenlik yapılandırması oluşturulur.
    // Eğer OAuth2 girişine ihtiyaç yoksa, bu kod sorununuzu çözmelidir.

    // Basit bir güvenlik yapılandırması oluşturan SecurityWebFilterChain bean’i tanımlıyoruz.
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        // CSRF korumasını devre dışı bırakıyoruz, çünkü burada basit bir güvenlik yapılandırması yapıyoruz.
        http
                .csrf().disable()
                // Gelen istekler üzerinde yetkilendirme işlemlerini başlatır.
                .authorizeExchange()
                // /address/** rotasına erişim için USER rolü gereklidir.
                .pathMatchers("/address/**").hasRole("USER")
                // Diğer tüm yollar serbest bırakılır ve herkes erişebilir.
                .anyExchange().permitAll()
                .and()
                // Basit HTTP kimlik doğrulaması sağlar, kullanıcı adı ve şifre kullanarak doğrulama yapılır.
                .httpBasic();
        // Aşağıdaki satırda belirtilen herhangi bir özel kimlik doğrulama kullanılmıyor.
        /*.anyExchange().permitAll();*/
        //.and()
        //.oauth2Login();  // OAuth2 veya JWT doğrulaması için ek yapılandırmalar yapılabilir
        return http.build(); // Güvenlik yapılandırmasını oluşturur.
    }


    // Şifreleri şifrelemek için PasswordEncoder bean’i tanımlıyoruz.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt algoritması kullanılarak şifreleme yapılır.
    }


    // Bellek içinde kullanıcı bilgilerini yöneten bir MapReactiveUserDetailsService tanımlıyoruz.
    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        // "hamit" kullanıcı adında bir kullanıcı tanımlıyoruz.
        UserDetails user = User.withUsername("hamit")
                // Şifreyi passwordEncoder ile şifreliyoruz.
                .password(passwordEncoder().encode("123"))
                // Kullanıcı rolü olarak "USER" atanıyor.
                .roles("USER")
                .build();

        // "admin" kullanıcı adında bir admin tanımlıyoruz.
        UserDetails admin = User.withUsername("admin")
                // Admin kullanıcı şifresi de passwordEncoder ile şifreleniyor.
                .password(passwordEncoder().encode("adminpassword"))
                // Admin rolü olarak "ADMIN" atanıyor.
                .roles("ADMIN")
                .build();

        // Bellek içindeki kullanıcıları yöneten MapReactiveUserDetailsService döndürülür.
        return new MapReactiveUserDetailsService(user, admin);
    }

}