package seg3x02.tempconverterapi.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Documentation OpenAPI de l'API sécurisée.
 *
 * Déclare le schéma d'authentification HTTP Basic et l'applique à toutes les routes :
 * Swagger UI affiche alors le bouton « Authorize » et envoie l'en-tête Authorization
 * avec chaque requête « Try it out ».
 */
@Configuration
class OpenApiConfig {

    @Bean
    fun temperatureConverterOpenApi(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Temperature Converter API")
                .version("1.0")
                .description(
                    "API REST de conversion de température (SEG3502, laboratoire 9). " +
                        "Toutes les requêtes exigent une authentification HTTP Basic " +
                        "avec un des comptes en mémoire (user1 ou user2)."
                )
        )
        .components(
            Components().addSecuritySchemes(
                BASIC_AUTH,
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("basic")
                    .description("Nom d'utilisateur et mot de passe d'un compte en mémoire")
            )
        )
        .addSecurityItem(SecurityRequirement().addList(BASIC_AUTH))

    /** Ajoute la réponse 401 à chaque opération documentée. */
    @Bean
    fun unauthorizedResponseCustomizer(): OpenApiCustomizer = OpenApiCustomizer { openApi ->
        openApi.paths?.values?.forEach { pathItem ->
            pathItem.readOperations().forEach { operation ->
                operation.responses.addApiResponse(
                    "401",
                    ApiResponse()
                        .description("Identifiants absents ou invalides (en-tête WWW-Authenticate: Basic)")
                        .content(Content().addMediaType("*/*", MediaType().schema(StringSchema())))
                )
            }
        }
    }

    companion object {
        const val BASIC_AUTH = "basicAuth"
    }
}
