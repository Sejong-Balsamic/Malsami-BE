package com.balsamic.sejongmalsami.util.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
    info = @Info(
        title = "📚 세종말싸미 : SEJONG-MALSAMI 📚",
        description = """
            ### 🌐 세종말싸미 웹사이트 🌐 : sejong-malsami.co.kr
            [**웹사이트 바로가기**](https://www.sejong-malsami.co.kr/) 

            ### 💻 **GitHub 저장소**
            - **[백엔드 소스코드](https://github.com/Sejong-Balsamic/Malsami-BE)**  
              백엔드 개발에 관심이 있다면 저장소를 방문해보세요.
            """,
        version = "1.0v"
    ),
    servers = {
        @Server(url = "https://api.sejong-malsami.co.kr", description = "메인 서버"),
        @Server(url = "https://api.test.sejong-malsami.co.kr", description = "테스트 서버"),
        @Server(url = "http://localhost:8080", description = "로컬 서버")
    }
)
@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    SecurityScheme apiKey = new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .in(SecurityScheme.In.HEADER)
        .name("Authorization")
        .scheme("bearer")
        .bearerFormat("JWT");

    SecurityRequirement securityRequirement = new SecurityRequirement()
        .addList("Bearer Token");

    return new OpenAPI()
        .components(new Components().addSecuritySchemes("Bearer Token", apiKey))
        .addSecurityItem(securityRequirement)
        .servers(List.of(
                new io.swagger.v3.oas.models.servers.Server()
                    .url("http://localhost:8080")
                    .description("로컬 서버"),
                new io.swagger.v3.oas.models.servers.Server()
                    .url("https://api.test.sejong-malsami.co.kr")
                    .description("테스트 서버"),
                new io.swagger.v3.oas.models.servers.Server()
                    .url("https://api.sejong-malsami.co.kr")
                    .description("메인 서버")
            )
        );
  }
}
