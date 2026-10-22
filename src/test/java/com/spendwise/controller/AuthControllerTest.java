package com.spendwise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendwise.dto.LoginRequestDto;
import com.spendwise.dto.RegisterRequestDto;
import com.spendwise.model.User;
import com.spendwise.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.spendwise.repository.ExpenseRepository expenseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        expenseRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testRegisterNewUser() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("john_doe", "John Doe", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("john_doe")))
                .andExpect(jsonPath("$.fullName", is("John Doe")));
    }

    @Test
    void testRegisterDuplicateUsername() throws Exception {
        userRepository.save(new User("john_doe", "John Doe", passwordEncoder.encode("secret"), "ROLE_USER"));

        RegisterRequestDto dto = new RegisterRequestDto("john_doe", "Another John", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Username 'john_doe' is already taken")));
    }

    @Test
    void testLoginSuccessAndGetMe() throws Exception {
        userRepository.save(new User("alice", "Alice Smith", passwordEncoder.encode("mypassword"), "ROLE_USER"));

        LoginRequestDto loginDto = new LoginRequestDto("alice", "mypassword");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("alice")))
                .andExpect(jsonPath("$.fullName", is("Alice Smith")))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        // Access /api/auth/me using the authenticated session
        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("alice")));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        userRepository.save(new User("alice", "Alice Smith", passwordEncoder.encode("mypassword"), "ROLE_USER"));

        LoginRequestDto invalidDto = new LoginRequestDto("alice", "wrongpass");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Invalid username or password")));
    }

    @Test
    void testUnauthenticatedApiAccessReturns401() throws Exception {
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isUnauthorized());
    }
}
