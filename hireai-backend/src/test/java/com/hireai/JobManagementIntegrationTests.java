package com.hireai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireai.auth.dto.AuthResponse;
import com.hireai.auth.dto.RegisterRequest;
import com.hireai.auth.dto.VerifyPinRequest;
import com.hireai.auth.entity.EmailVerification;
import com.hireai.auth.entity.User;
import com.hireai.auth.repository.EmailVerificationRepository;
import com.hireai.auth.repository.UserRepository;
import com.hireai.auth.service.PinService;
import com.hireai.job.dto.CreateJobRequest;
import com.hireai.job.dto.JobFocusAreaDto;
import com.hireai.job.dto.SampleQuestionDto;
import com.hireai.job.dto.SkillDto;
import com.hireai.job.enums.DifficultyLevel;
import com.hireai.job.enums.JobMode;
import com.hireai.job.enums.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class JobManagementIntegrationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private PinService pinService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testEndToEndAuthAndJobCreationWorkflow() throws Exception {
        String testEmail = "recruiter." + System.currentTimeMillis() + "@acme-corp.com";

        // 1. Register organization and recruiter
        RegisterRequest registerRequest = RegisterRequest.builder()
                .organizationName("Acme Global Corp")
                .fullName("Alex Morgan")
                .workEmail(testEmail)
                .password("SecureP@ssw0rd2026!")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        User user = userRepository.findByWorkEmailWithDetails(testEmail).orElseThrow();
        EmailVerification verification = emailVerificationRepository.findLatestActiveByUser(user).orElseThrow();

        // 2. Simulate 6-digit PIN verification using generated PIN hash
        String knownPin = "741258";
        verification.setPinHash(pinService.hashPin(knownPin));
        emailVerificationRepository.save(verification);

        VerifyPinRequest verifyRequest = VerifyPinRequest.builder()
                .workEmail(testEmail)
                .pin(knownPin)
                .build();

        MvcResult verifyResult = mockMvc.perform(post("/api/v1/auth/verify-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        String tokenJson = verifyResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(tokenJson).path("data").path("accessToken").asText();

        // 3. Create a Job in Smart Mode (with skills & interview configurations)
        CreateJobRequest smartJob = CreateJobRequest.builder()
                .title("Full Stack Developer")
                .description("Looking for a skilled Spring Boot and React engineer to lead recruitment platform modules.")
                .mode(JobMode.SMART)
                .location("Bangalore, India")
                .experienceMin(3)
                .experienceMax(6)
                .interviewType("Technical Evaluation")
                .interviewDurationMinutes(45)
                .skills(List.of(
                        SkillDto.builder().name("Java").category("Backend").required(true).weightage(30).build(),
                        SkillDto.builder().name("Spring Boot").category("Backend").required(true).weightage(30).build(),
                        SkillDto.builder().name("React").category("Frontend").required(true).weightage(20).build(),
                        SkillDto.builder().name("PostgreSQL").category("Database").required(false).weightage(20).build()
                ))
                .sampleQuestions(List.of(
                        SampleQuestionDto.builder().category("Backend").questionText("How do you manage database migrations with Flyway?").difficulty("MEDIUM").build(),
                        SampleQuestionDto.builder().category("Frontend").questionText("Explain state management patterns in modern React.").difficulty("MEDIUM").build()
                ))
                .build();

        MvcResult createJobResult = mockMvc.perform(post("/api/v1/jobs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(smartJob)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Full Stack Developer"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.skills.length()").value(4))
                .andReturn();

        String jobId = objectMapper.readTree(createJobResult.getResponse().getContentAsString()).path("data").path("id").asText();

        // 4. Create a Job in Custom Mode (with focus areas, difficulty levels, and auto-distributed weightage)
        CreateJobRequest customJob = CreateJobRequest.builder()
                .title("HR Manager")
                .description("Lead talent acquisition strategy and operational HR functions.")
                .mode(JobMode.CUSTOM)
                .location("Dubai, United Arab Emirates")
                .experienceMin(5)
                .experienceMax(8)
                .focusAreas(List.of(
                        JobFocusAreaDto.builder().name("Recruitment Strategy Development").weightage(25).difficultyLevel(DifficultyLevel.MEDIUM).topics("Sourcing, Employer Branding").build(),
                        JobFocusAreaDto.builder().name("Performance Management Facilitation").weightage(25).difficultyLevel(DifficultyLevel.MEDIUM).topics("KPIs, OKRs, Review Cycles").build(),
                        JobFocusAreaDto.builder().name("Data-Driven HR Decision Making").weightage(25).difficultyLevel(DifficultyLevel.HARD).topics("Attrition Analysis, Metrics").build(),
                        JobFocusAreaDto.builder().name("Onboarding Process Ownership").weightage(25).difficultyLevel(DifficultyLevel.EASY).topics("Orientation, Compliance").build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/jobs")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customJob)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("HR Manager"))
                .andExpect(jsonPath("$.data.focusAreas.length()").value(4));

        // 5. Fetch dashboard statistics (matching Open Job Roles & On Hold counts)
        mockMvc.perform(get("/api/v1/jobs/dashboard-stats")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.openJobRoles").value(2))
                .andExpect(jsonPath("$.data.totalJobRoles").value(2));

        // 6. Update job status to ON_HOLD
        mockMvc.perform(patch("/api/v1/jobs/" + jobId + "/status")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ON_HOLD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ON_HOLD"));

        // 7. Verify updated dashboard statistics
        mockMvc.perform(get("/api/v1/jobs/dashboard-stats")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.openJobRoles").value(1))
                .andExpect(jsonPath("$.data.onHoldJobRoles").value(1));

        // 8. Search skills
        mockMvc.perform(get("/api/v1/jobs/skills/search?query=jav")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Java"));
    }
}
