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
            **세종대학교 학생들을 위한 최고의 지식 공유 플랫폼, 세종말싸미에 오신 것을 환영합니다!**

            세종말싸미는 세종대학교 학생들이 자신이 공부한 자료를 자유롭게 공유하고 열람할 수 있는 웹 서비스입니다. 포인트 시스템을 통해 자료 업로드, 다운로드, 질문 및 답변 등 다양한 활동에 참여하여 학습 효율을 극대화하세요!

            ## 🌐 세종말싸미 웹사이트 🌐 : sejong-malsami.co.kr
            [**웹사이트 바로가기**](https://www.sejong-malsami.co.kr/) 

            ### 📝 **서비스 소개**

            - ``**자료 공유 및 다운로드**``  
              학생들이 직접 업로드한 다양한 학습 자료를 손쉽게 찾아보고 다운로드할 수 있습니다.

            - ``**질문 & 답변 커뮤니티**``  
              학업 관련 궁금한 점을 질문하고, 다른 학생들의 답변을 통해 함께 성장하세요.

            - ``**포인트 시스템**``  
              활동에 따라 포인트를 적립하고, 더 많은 자료와 기능을 활용해보세요.

            ### 💡 **'세종말싸미'의 의미**

            '세종말싸미'는 세종대왕님의 '나랏말싸미 듕귁에 달아...'에서 영감을 받아, 세종대학교 학생들의 지식과 언어를 함께 나누는 공간을 의미합니다. 
            우리의 지식 공유 플랫폼을 통해 모두가 함께 성장할 수 있기를 바랍니다.

            ### 💻 **GitHub 저장소**

            - **[백엔드 소스코드](https://github.com/Sejong-Balsamic/Malsami-BE)**  
              백엔드 개발에 관심이 있다면 저장소를 방문해보세요.

            - **[프론트엔드 소스코드](https://github.com/Sejong-Balsamic/Malsami-FE)**  
              프론트엔드 디자인과 기능 구현에 대해 알아보세요.

            ### ⚠️ **Swagger 이용 시 주의사항**

            - **Send empty value 체크박스 확인**  
              요청 시 문제가 발생한다면, Swagger UI에서 `Send empty value` 체크박스가 선택되어 있지 않은지 확인해주세요. 체크박스를 **해제**한 후 다시 시도하시기 바랍니다.

            - **MultipartFile, Object 관련 오류 해결**  
              `Can't Parse JSON` 오류가 발생하면 해당 부분의 체크박스를 해제하고 다시 시도해주세요.

            ![Swagger 오류 해결 가이드](http://220.85.169.165/sul-game/images/swagger_error_resolve.gif)
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
