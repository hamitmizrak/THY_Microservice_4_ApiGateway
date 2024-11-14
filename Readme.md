Merkezi yönlendirme ve yük dengeleme.
JWT ile kimlik doğrulama.
İstek filtreleme ve loglama.
Servis keşfi (Eureka) ile dinamik servis bulma.

Test için: http://localhost:8080/address/getAddress/1
Eğer username password istiyorsa herşeyi doğru yaptık demektir.
username: hamit
password: 123


Bir `address-service` projesinde API Gateway konfigürasyonu yapmak, istemcilerden gelen istekleri merkezi bir noktadan adres servisine yönlendirmek için ideal bir çözümdür. Bu çözüm, güvenlik, yük dengeleme, önbellekleme ve izleme gibi ek özellikler sağlar. Şimdi detaylıca API Gateway’in `address-service` ile nasıl entegre edileceğini ve kod örneklerini inceleyelim.

### 1. API Gateway Nedir ve Neden Kullanılır?
API Gateway, istemci uygulamaların mikroservislerle doğrudan iletişim kurmak yerine tek bir giriş noktası üzerinden bu servislere ulaşmasını sağlar. `address-service` gibi bir servis, API Gateway ile aşağıdaki avantajları sağlar:
- **Güvenlik**: Kimlik doğrulama ve yetkilendirme işlemlerini merkezi bir noktadan yönetir.
- **Yük Dengeleme**: İsteklerin birden fazla `address-service` örneğine dağıtılmasını sağlar.
- **Önbellekleme**: Sık kullanılan verilerin önbelleğe alınarak performansın artırılmasını sağlar.
- **Versiyon Kontrolü ve İzleme**: Servislerin farklı versiyonlarının kontrolü ve tüm isteklerin merkezi loglamasını kolaylaştırır.

### 2. Spring Boot ve Spring Cloud Gateway ile API Gateway Kurulumu
Spring Cloud Gateway, API Gateway işlevini sağlamak için kullanılan güçlü bir yapıdır. Projeyi yapılandırmak için şu adımları izleyebilirsiniz:

#### Adım 1: Projeyi Oluşturma
Spring Initializr kullanarak bir API Gateway projesi oluşturun ve şu bağımlılıkları ekleyin:
- Spring Cloud Gateway
- Eureka Discovery Client (isteğe bağlı olarak servis keşfi için)

**Maven** kullanıyorsanız `pom.xml` dosyanıza aşağıdaki bağımlılıkları ekleyin:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <!-- Diğer bağımlılıklar burada yer alabilir -->
</dependencies>
```

#### Adım 2: application.yml Dosyasını Yapılandırma
API Gateway yapılandırmasını `application.yml` dosyasında tanımlayarak `address-service`e yönlendirme işlemlerini gerçekleştireceğiz. Eğer `address-service` bir Eureka sunucusunda kayıtlıysa, aşağıdaki gibi yük dengeleme (load balancing) de yapılabilir:

```yaml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: address-service
          uri: lb://ADDRESS-SERVICE
          predicates:
            - Path=/address/**
          filters:
            - AddRequestHeader=X-Request-Source, Gateway
```

Bu yapılandırmada:
- **`id`**: Rota için tanımlayıcıdır. Burada `address-service` olarak belirlenmiştir.
- **`uri`**: İsteklerin yönlendirileceği mikroservisin URI’sidir. `lb://ADDRESS-SERVICE` ifadesi, yük dengeleme yaparak `address-service`e yönlendirme yapacaktır.
- **`predicates`**: İsteklerin hangi yollar için geçerli olacağını belirler. Burada `/address/**` ile başlayan tüm istekler `address-service`e yönlendirilir.
- **`filters`**: İsteklere özel başlık eklemek veya loglamak gibi işlemler için filtre kullanılır.

#### Adım 3: Eureka Servis Keşfi
API Gateway, `address-service`in IP adresini veya URL’sini manuel olarak belirtmek zorunda kalmadan servis keşif (service discovery) aracı olan Eureka ile mikroservisleri bulabilir. `application.yml` dosyanıza Eureka yapılandırmasını ekleyin:

```yaml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8761/eureka/
    fetchRegistry: true
    registerWithEureka: true
```

Bu konfigürasyon ile API Gateway, `address-service`in yerini otomatik olarak Eureka üzerinden bulabilir. Eureka sunucusu `localhost:8761` adresinde çalışmaktadır, isteğe bağlı olarak bu adres değiştirilebilir.

### 3. API Gateway Üzerinde Filtreler
API Gateway, `GlobalFilter` ve `GatewayFilter` gibi özel filtreler eklemenize olanak tanır. Örneğin, gelen isteklerin başlıklarını loglamak için bir `GlobalFilter` ekleyebiliriz:

```java
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    public GlobalFilter customFilter() {
        return (exchange, chain) -> {
            System.out.println("İstek URL’si: " + exchange.getRequest().getURI());
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                System.out.println("Yanıt gönderildi.");
            }));
        };
    }
}
```

Bu kodda:
- **`GlobalFilter`**: Tüm isteklere uygulanan bir filtre tanımlar. Bu örnekte istek URL'sini logluyoruz.
- **`Mono.fromRunnable`**: İstek tamamlandıktan sonra bir işlem yürütülmesini sağlar.

### 4. Güvenlik Ekleme (JWT Kullanımı ile)
API Gateway, JWT token doğrulaması yaparak `address-service`e güvenli erişim sağlayabilir. Bunun için Spring Security kullanarak JWT doğrulaması yapılabilir.

Öncelikle, `JWTUtil` sınıfını ekleyerek token doğrulama işlemlerini gerçekleştirin:

```java
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JWTUtil {

    private final String SECRET_KEY = "secret";

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))  // 10 saat geçerli
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

Daha sonra, API Gateway’e gelen her istekte JWT doğrulaması yapmak için bir güvenlik yapılandırması ekleyin:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf().disable()
            .authorizeExchange()
            .pathMatchers("/address/**").authenticated()
            .anyExchange().permitAll()
            .and()
            .oauth2Login();  // veya JWT doğrulaması için ek ayarlar
        return http.build();
    }
}
```

Bu güvenlik yapılandırması, `/address/**` yolu için gelen isteklerin kimlik doğrulaması gerektirdiğini belirtir. Böylece yalnızca geçerli JWT token’ı olan kullanıcılar `address-service`e erişebilir.

### 5. Test ve Çalıştırma
Yukarıdaki adımları tamamladıktan sonra `address-service` ve API Gateway projelerini başlatın. API Gateway üzerinden `/address/**` yoluna erişim sağladığınızda, istekler doğrudan `address-service`e yönlendirilecektir.

Örneğin:
- `http://localhost:8080/address/getAddress/1` isteği, API Gateway üzerinden `address-service`in ilgili endpoint’ine yönlendirilir.
- Eğer güvenlik yapılandırması aktifse JWT token olmadan erişim sağlanamaz.

### Özet

API Gateway, `address-service` gibi mikroservislerin güvenli, düzenli ve performanslı bir şekilde yönetilmesini sağlayan önemli bir bileşendir. Spring Cloud Gateway ile aşağıdaki yapılandırmaları gerçekleştirdik:
- Merkezi yönlendirme ve yük dengeleme.
- JWT ile kimlik doğrulama.
- İstek filtreleme ve loglama.
- Servis keşfi (Eureka) ile dinamik servis bulma.

Bu yapı, `address-service` gibi mikroservislerin ölçeklenebilir ve güvenli bir şekilde kullanılmasına olanak tanır.

// Bu sınıf, Spring Cloud Gateway için global bir filtre tanımlayan bir konfigürasyon sınıfıdır.
@Configuration
public class GatewayConfig {

    // Spring konteynırında kullanılacak olan özel bir GlobalFilter bean’i tanımlıyoruz.
    @Bean
    public GlobalFilter customFilter() {
        // Lambda ifadesi ile bir GlobalFilter döndürülür; exchange ve chain parametrelerini alır.
        return (exchange, chain) -> {
            // İstek URL'sini konsola yazdırır, böylece her isteğin hangi URL'ye yapıldığını loglayabiliriz.
            System.out.println("İstek URL’si: " + exchange.getRequest().getURI());

            // Filtre zincirini çağırır ve işlem tamamlandığında yanıtın gönderildiğini loglar.
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                // Yanıt gönderildikten sonra konsola "Yanıt gönderildi." mesajını yazdırır.
                System.out.println("Yanıt gönderildi.");
            }));
        };
    }
}


// Bu sınıf, JWT token oluşturma ve doğrulama işlemleri için yardımcı bir sınıf sağlar.
@Component
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
    }
}


// Bu sınıf, Spring Security için güvenlik yapılandırmalarını tanımlayan bir konfigürasyon sınıfıdır.
@Configuration
public class SecurityConfig {

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


