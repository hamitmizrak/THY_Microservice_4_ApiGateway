package com.hamitmizrak._4_apigateway.config;

/*
API Gateway, GlobalFilter ve GatewayFilter gibi özel filtreler eklemenize olanak tanır.
Örneğin, gelen isteklerin başlıklarını loglamak için bir GlobalFilter ekleyebiliriz:
Bu kodda:
GlobalFilter: Tüm isteklere uygulanan bir filtre tanımlar. Bu örnekte istek URL'sini logluyoruz.
Mono.fromRunnable: İstek tamamlandıktan sonra bir işlem yürütülmesini sağlar.
 */

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

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
