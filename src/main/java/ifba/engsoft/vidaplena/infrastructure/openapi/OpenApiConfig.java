package ifba.engsoft.vidaplena.infrastructure.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("!prod")
public class OpenApiConfig {

	@Bean
	public OpenAPI customOpenAPI() {
		final String securitySchemeName = "bearerAuth";

		return new OpenAPI()
				.info(new Info()
						.title("Vida Plena API - Gestão de Saúde e Prontuários")
						.version("1.0.0")
						.description("""
								## Especificação Oficial da API Vida Plena
								
								A **Vida Plena** é uma plataforma integrada de gestão preventiva de saúde, acompanhamento familiar e prontuário eletrônico do paciente (PEP).
								
								### Autenticação & Segurança
								A API utiliza tokens **JWT (JSON Web Token)** transmitidos via cabeçalho `Authorization` no padrão Bearer:
								```http
								Authorization: Bearer <seu_token_jwt>
								```
								Para rotas autenticadas, utilize o botão **Authorize** no topo do Swagger UI informando o token obtido no endpoint `/api/v1/auth/login`.
								
								### Perfis de Acesso (Roles)
								* `ADMINISTRADOR`: Acesso irrestrito a configurações, auditoria e gestão de usuários/organizações.
								* `MEDICO`, `NUTRICIONISTA`, `PERSONAL_TRAINER`, `CUIDADOR`: Acesso clínico a prontuários, evoluções e agendamentos.
								* `FUNCIONARIO_ADMINISTRATIVO`: Gestão operacional de clínicas e agendamentos.
								* `PACIENTE`: Acesso a seu próprio histórico de saúde, agendamentos e dados cadastrais.
								* `RESPONSAVEL`: Gestão de dependentes vinculados ao núcleo familiar.
								* `REPRESENTANTE_EMPRESA`: Gestão corporativa de convênios empresariais.
								
								### Padrão de Erros HTTP
								Todas as exceções e erros de validação retornam o objeto `ApiErrorResponse` padronizado contendo:
								* `timestamp`: Momento UTC da ocorrência
								* `status`: Código de status HTTP numérico (ex: 400, 401, 403, 404, 422, 500)
								* `error`: Descrição textual do status HTTP
								* `message`: Causa simplificada do erro
								* `path`: URI requisitada
								* `details`: Lista de mensagens detalhadas (erros de campos ou regras violadas)
								""")
						.contact(new Contact()
								.name("Equipe de Engenharia de Software Vida Plena")
								.email("suporte@vidaplena.com.br")
								.url("https://vidaplena.com.br"))
						.license(new License()
								.name("MIT License")
								.url("https://opensource.org/licenses/MIT")))
				.servers(List.of(
						new Server().url("http://localhost:8081").description("Ambiente Local de Desenvolvimento"),
						new Server().url("https://api-staging.vidaplena.com.br").description("Ambiente de Homologação (Staging)"),
						new Server().url("https://api.vidaplena.com.br").description("Ambiente de Produção")))
				.addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
				.components(new Components()
						.addSecuritySchemes(securitySchemeName,
								new SecurityScheme()
										.name(securitySchemeName)
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")
										.description("Insira o token JWT retornado pelo endpoint de login. Exemplo: `eyJhbGciOiJIUz...`")));
	}
}
