package com.hamitmizrak._4_apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

// @SpringBootApplication= @Configuration (Bean) +@EnableAutoConfiguration+ @ComponentScan
// NOT: ApiGateway Eureka Server'a kendisini kayıt yaptırarak işe başlar
@SpringBootApplication
@EnableDiscoveryClient // Yenisi
//@EnableEurekaClient // Eskisi
//@EnableEurekaClient  // Eureka Client olarak çalışmasını sağlar
/*
Yeni Spring Cloud sürümlerinde, @EnableEurekaClient anotasyonuna gerek kalmamıştır çünkü
Spring Boot otomatik yapılandırma özelliği sayesinde Eureka Client’ı otomatik olarak algılar.
Eğer Spring Cloud Netflix Eureka bağımlılığını eklediyseniz ve application.yml
dosyanızda gerekli Eureka yapılandırmalarını yaptıysanız,
Spring Boot otomatik olarak uygulamayı bir Eureka Client olarak çalıştıracaktır.
 */
public class Application {
	// PSVM
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	// Bean
	// Rest API

	// Dinamik Route Tanımlama
    /*
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/user/**")
                        .uri("lb://USER-SERVICE"))
                .route("product-service", r -> r.path("/product/**")
                        .uri("lb://PRODUCT-SERVICE"))
                .route("order-service", r -> r.path("/order/**")
                        .uri("lb://ORDER-SERVICE"))
                .build();
    }
     */

} //end Application
