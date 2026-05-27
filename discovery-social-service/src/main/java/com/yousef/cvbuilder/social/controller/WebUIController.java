package com.yousef.cvbuilder.social.controller;

import com.yousef.cvbuilder.social.entity.PublicCv;
import com.yousef.cvbuilder.social.service.SocialFeedService;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/ui")
@RequiredArgsConstructor
public class WebUIController {

    private final SocialFeedService socialFeedService;
    private final RestTemplate restTemplate;

    @Value("${app.gateway-base-url:http://localhost:8080}")
    private String gatewayBaseUrl;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session,
                              Model model) {
        try {
            String loginUrl = UriComponentsBuilder.fromHttpUrl(gatewayBaseUrl)
                    .path("/api/auth/login")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<String> loginResponse = restTemplate.exchange(
                    loginUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(Map.of("email", username, "password", password), headers),
                    String.class
            );
            String userId = extractUserId(Objects.requireNonNull(loginResponse.getBody()));
            session.setAttribute("userId", userId);
            session.setAttribute("userEmail", username);
            return "redirect:/ui/dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Login failed: " + e.getMessage());
            return "login";
        }
    }

    // Renders the main administration dashboard and global public project feed.
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        List<PublicCv> cvs = socialFeedService.getAllPublicFeeds();
        model.addAttribute("cvList", cvs);
        model.addAttribute("systemStatus", "ACTIVE");
        return "dashboard"; // Resolves to templates/dashboard.html
    }

    @GetMapping("/cv/new")
    public String showCreateCvForm(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/ui/login";
        }
        return "create-cv";
    }

    @PostMapping("/cv/save")
    public String saveCv(@RequestParam String fullName,
                         @RequestParam String title,
                         @RequestParam String summary,
                         @RequestParam String skills,
                         @RequestParam(required = false) List<String> experienceCompanyName,
                         @RequestParam(required = false) List<String> experienceJobTitle,
                         @RequestParam(required = false) List<String> experienceStartDate,
                         @RequestParam(required = false) List<String> experienceEndDate,
                         @RequestParam(required = false) List<String> experienceResponsibilities,
                         @RequestParam(required = false) List<String> educationInstitution,
                         @RequestParam(required = false) List<String> educationFieldOfStudy,
                         @RequestParam(required = false) List<String> educationDegreeLevel,
                         @RequestParam(required = false) List<String> educationGraduationYear,
                         @RequestParam(required = false) List<String> projectName,
                         @RequestParam(required = false) List<String> projectTechnicalStack,
                         @RequestParam(required = false) List<String> projectUrl,
                         @RequestParam(required = false) List<String> projectArchitecturalSummary,
                         @RequestParam(required = false) List<String> languageName,
                         @RequestParam(required = false) List<String> languageFluencyTier,
                         HttpSession session,
                         Model model) {
        Object userId = session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/ui/login";
        }

        try {
            Map<String, Object> requestData = Map.of(
                    "userId", userId.toString(),
                    "title", title,
                    "fullName", fullName,
                    "summary", summary,
                    "skills", parseSkills(skills),
                    "experience", buildExperience(experienceCompanyName, experienceJobTitle,
                            experienceStartDate, experienceEndDate, experienceResponsibilities),
                    "education", buildEducation(educationInstitution, educationFieldOfStudy,
                            educationDegreeLevel, educationGraduationYear),
                    "projects", buildProjects(projectName, projectTechnicalStack,
                            projectUrl, projectArchitecturalSummary),
                    "languages", buildLanguages(languageName, languageFluencyTier)
            );

            ResponseEntity<Map<String, Object>> saveResponse = restTemplate.exchange(
                    gatewayBaseUrl + "/api/cv/save",
                    HttpMethod.POST,
                    new HttpEntity<>(requestData),
                    new ParameterizedTypeReference<>() {
                    }
            );

            Object cvId = Objects.requireNonNull(saveResponse.getBody()).get("id");
            restTemplate.put(gatewayBaseUrl + "/api/cv/" + cvId + "/publish", null);

            return "redirect:/ui/dashboard";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "CV creation failed: " + e.getMessage());
            return "create-cv";
        }
    }

    // Displays details for a specific CV and triggers synchronous gRPC verification under the hood.
    @GetMapping("/cv/{id}")
    public String showCvDetails(@PathVariable String id, Model model) {
        try {
            PublicCv cv = socialFeedService.getPublicCvDetails(id);
            model.addAttribute("cv", cv);
            model.addAttribute("grpcVerification", "SUCCESS: Account is ACTIVE via gRPC");
            return "cv-details";
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("grpcVerification", "FAILED: Identity Service Blocked Access via gRPC");
            return "error-page";
        }
    }

    private String extractUserId(String loginResponse) {
        if (loginResponse == null || !loginResponse.contains("User ID:")) {
            throw new IllegalStateException("Identity service did not return a user id");
        }
        return loginResponse.substring(loginResponse.indexOf("User ID:") + "User ID:".length()).trim();
    }

    private List<String> parseSkills(String skills) {
        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .filter(skill -> !skill.isBlank())
                .toList();
    }

    private List<Map<String, String>> buildExperience(List<String> companyNames,
                                                       List<String> jobTitles,
                                                       List<String> startDates,
                                                       List<String> endDates,
                                                       List<String> responsibilities) {
        int size = maxSize(companyNames, jobTitles, startDates, endDates, responsibilities);
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(index -> Map.of(
                        "companyName", valueAt(companyNames, index),
                        "jobTitle", valueAt(jobTitles, index),
                        "startDate", valueAt(startDates, index),
                        "endDate", valueAt(endDates, index),
                        "responsibilities", valueAt(responsibilities, index)
                ))
                .filter(this::hasAnyValue)
                .toList();
    }

    private List<Map<String, String>> buildEducation(List<String> institutions,
                                                     List<String> fieldsOfStudy,
                                                     List<String> degreeLevels,
                                                     List<String> graduationYears) {
        int size = maxSize(institutions, fieldsOfStudy, degreeLevels, graduationYears);
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(index -> Map.of(
                        "institution", valueAt(institutions, index),
                        "fieldOfStudy", valueAt(fieldsOfStudy, index),
                        "degreeLevel", valueAt(degreeLevels, index),
                        "graduationYear", valueAt(graduationYears, index)
                ))
                .filter(this::hasAnyValue)
                .toList();
    }

    private List<Map<String, String>> buildProjects(List<String> names,
                                                    List<String> technicalStacks,
                                                    List<String> urls,
                                                    List<String> summaries) {
        int size = maxSize(names, technicalStacks, urls, summaries);
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(index -> Map.of(
                        "projectName", valueAt(names, index),
                        "technicalStack", valueAt(technicalStacks, index),
                        "projectUrl", valueAt(urls, index),
                        "architecturalSummary", valueAt(summaries, index)
                ))
                .filter(this::hasAnyValue)
                .toList();
    }

    private List<Map<String, String>> buildLanguages(List<String> names, List<String> fluencyTiers) {
        int size = maxSize(names, fluencyTiers);
        return java.util.stream.IntStream.range(0, size)
                .mapToObj(index -> Map.of(
                        "languageName", valueAt(names, index),
                        "fluencyTier", valueAt(fluencyTiers, index)
                ))
                .filter(this::hasAnyValue)
                .toList();
    }

    @SafeVarargs
    private int maxSize(List<String>... lists) {
        return Arrays.stream(lists)
                .filter(Objects::nonNull)
                .mapToInt(List::size)
                .max()
                .orElse(0);
    }

    private String valueAt(List<String> values, int index) {
        if (values == null || index >= values.size() || values.get(index) == null) {
            return "";
        }
        return values.get(index).trim();
    }

    private boolean hasAnyValue(Map<String, String> values) {
        return values.values().stream().anyMatch(value -> !value.isBlank());
    }
}
