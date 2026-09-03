package com.acme.accountservice.auth;

import com.acme.accountservice.common.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void signupReturnsUserWithoutPassword() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "John",
                                  "lastname": "Doe",
                                  "email": "johndoe@acme.com",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "name": "John",
                          "lastname": "Doe",
                          "email": "johndoe@acme.com"
                        }
                        """))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        UserService userService() {
            return new UserService(null, null) {
                @Override
                public AccountUser register(SignupRequest request) {
                    return new AccountUser(1L, request.name(), request.lastname(), request.email(), "encoded");
                }
            };
        }
    }

    @Test
    void signupRejectsMissingRequiredField() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lastname": "Doe",
                                  "email": "johndoe@acme.com",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/auth/signup"))
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void signupRejectsNonCorporateEmail() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "John",
                                  "lastname": "Doe",
                                  "email": "johndoe@google.com",
                                  "password": "secret"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
