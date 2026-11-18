package com.spy.backend.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class WebConfigSpec extends Specification {

    @Autowired
    MockMvc mvc

    def "health endpoint is served under /api"() {
        expect:
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.status').value("ok"))
    }

    def "health endpoint is not served without /api prefix"() {
        expect:
        mvc.perform(get("/health")).andExpect(status().isNotFound())
    }

    def "preflight from #origin is allowed"() {
        expect:
        mvc.perform(options("/api/health")
                .header("Origin", origin)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "X-Player-Token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", origin))

        where:
        origin << ["http://localhost:4200", "https://kacperkk2.github.io"]
    }

    def "preflight from unknown origin is rejected"() {
        expect:
        mvc.perform(options("/api/health")
                .header("Origin", "https://evil.example")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
    }

    def "preflight with disallowed method is rejected"() {
        expect:
        mvc.perform(options("/api/health")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "PUT"))
                .andExpect(status().isForbidden())
    }
}
