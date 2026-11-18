package seg3x02.tempconverterapi

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTests @Autowired constructor(private val mvc: MockMvc) {
    private val cToF = "/temperature-converter/celsius-fahrenheit/50"
    private val fToC = "/temperature-converter/fahrenheit-celsius/122"

    @Test
    fun `both endpoints require credentials`() {
        for (path in listOf(cToF, fToC)) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized)
                .andExpect(header().exists("WWW-Authenticate"))
        }
    }

    @Test
    fun `both users can access both conversions`() {
        for ((user, password) in listOf("user1" to "pass1", "user2" to "pass2")) {
            mvc.perform(get(cToF).with(httpBasic(user, password)))
                .andExpect(status().isOk).andExpect(content().string("122.0"))
            mvc.perform(get(fToC).with(httpBasic(user, password)))
                .andExpect(status().isOk).andExpect(content().string("50.0"))
        }
    }

    @Test
    fun `invalid credentials are rejected`() {
        mvc.perform(get(cToF).with(httpBasic("user1", "wrong")))
            .andExpect(status().isUnauthorized)
        mvc.perform(get(fToC).with(httpBasic("unknown", "pass2")))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `authentication does not persist across requests`() {
        mvc.perform(get(cToF).with(httpBasic("user1", "pass1")))
            .andExpect(status().isOk)
        mvc.perform(get(cToF)).andExpect(status().isUnauthorized)
    }
}
