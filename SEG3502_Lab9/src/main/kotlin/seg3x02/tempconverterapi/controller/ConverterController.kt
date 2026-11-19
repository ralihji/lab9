package seg3x02.tempconverterapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("temperature-converter")
@Tag(name = "Temperature Converter", description = "Conversions Celsius ↔ Fahrenheit (authentification Basic requise)")
class ConverterController {
    @GetMapping("/celsius-fahrenheit/{celsius}")
    @Operation(summary = "Celsius vers Fahrenheit", description = "Exemple : 50 °C donne 122.0 °F")
    fun getFahrenheit(
        @Parameter(description = "Température en degrés Celsius", example = "50")
        @PathVariable celsius: Double
    ) = ((celsius * 9) / 5 + 32)

    @GetMapping("/fahrenheit-celsius/{fahrenheit}")
    @Operation(summary = "Fahrenheit vers Celsius", description = "Exemple : 122 °F donne 50.0 °C")
    fun getCelsius(
        @Parameter(description = "Température en degrés Fahrenheit", example = "122")
        @PathVariable fahrenheit: Double
    ) = ((fahrenheit - 32) * 5) / 9
}
