package seg3x02.tempconverterapi

import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Tests complémentaires : valeurs de conversion, entrées invalides,
 * mots de passe croisés et documentation OpenAPI protégée.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiBehaviourTests @Autowired constructor(private val mvc: MockMvc) {
    private val base = "/temperature-converter"

    @Test
    fun `conversions return the expected values`() {
        val cases = mapOf(
            "$base/celsius-fahrenheit/0" to "32.0",
            "$base/celsius-fahrenheit/-40" to "-40.0",
            "$base/celsius-fahrenheit/37.5" to "99.5",
            "$base/fahrenheit-celsius/212" to "100.0",
            "$base/fahrenheit-celsius/32" to "0.0"
        )
        for ((path, expected) in cases) {
            mvc.perform(get(path).with(httpBasic("user2", "pass2")))
                .andExpect(status().isOk)
                .andExpect(content().string(expected))
        }
    }

    @Test
    fun `invalid temperature returns 400 once authenticated`() {
        mvc.perform(get("$base/celsius-fahrenheit/abc").with(httpBasic("user1", "pass1")))
            .andExpect(status().isBadRequest)
            .andExpect(content().string("Unable to process request"))
    }

    @Test
    fun `credentials are checked before the request is processed`() {
        // Sans identifiants, même une requête invalide reçoit 401 et non 400
        mvc.perform(get("$base/celsius-fahrenheit/abc"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `a user cannot sign in with the other user's password`() {
        mvc.perform(get("$base/celsius-fahrenheit/50").with(httpBasic("user1", "pass2")))
            .andExpect(status().isUnauthorized)
        mvc.perform(get("$base/celsius-fahrenheit/50").with(httpBasic("user2", "pass1")))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `passwords are case sensitive`() {
        mvc.perform(get("$base/celsius-fahrenheit/50").with(httpBasic("user1", "PASS1")))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `OpenAPI documentation and Swagger UI require authentication`() {
        for (path in listOf("/v3/api-docs", "/swagger-ui/index.html")) {
            mvc.perform(get(path))
                .andExpect(status().isUnauthorized)
                .andExpect(header().string("WWW-Authenticate", containsString("Basic")))
        }
    }

    @Test
    fun `OpenAPI documentation declares Basic authentication`() {
        mvc.perform(get("/v3/api-docs").with(httpBasic("user1", "pass1")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.info.title").value("Temperature Converter API"))
            .andExpect(jsonPath("$.components.securitySchemes.basicAuth.type").value("http"))
            .andExpect(jsonPath("$.components.securitySchemes.basicAuth.scheme").value("basic"))
            .andExpect(jsonPath("$.security[0].basicAuth").isArray)
            .andExpect(jsonPath("$.paths['$base/celsius-fahrenheit/{celsius}'].get.responses['401']").exists())
            .andExpect(jsonPath("$.paths['$base/fahrenheit-celsius/{fahrenheit}'].get.responses['401']").exists())
    }
}
