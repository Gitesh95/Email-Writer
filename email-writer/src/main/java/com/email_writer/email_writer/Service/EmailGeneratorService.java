package com.email_writer.email_writer.Service;
import com.email_writer.email_writer.Repository.EmailReplyRepository;
import com.email_writer.email_writer.Model.EmailReply;
import com.email_writer.email_writer.DTO.EmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.time.Duration;
import java.util.Map;
import java.util.List;

@Service
public class EmailGeneratorService {
    private final WebClient webClient;
    private final EmailReplyRepository emailReplyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder,
                                 EmailReplyRepository emailReplyRepository) {
        this.webClient = webClientBuilder.baseUrl("https://generativelanguage.googleapis.com/v1beta").build();
        this.emailReplyRepository = emailReplyRepository;
    }

    public String generateEmailReply(EmailRequest emailRequest) {
        String prompt = buildPrompt(emailRequest);

        // Validate prompt is not empty
        if (prompt == null || prompt.trim().isEmpty()) {
            return "Error: Email content cannot be empty.";
        }

        // Minimal request body - removing optional fields that might cause issues
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        String generatedReply = "No reply generated. Please try again.";

        try {
            // Debug: Log the request being sent
            System.out.println("Sending request to Gemini API: " + objectMapper.writeValueAsString(requestBody));

            JsonNode response = webClient.post()
                    .uri("/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response != null &&
                    response.path("candidates").isArray() &&
                    response.path("candidates").size() > 0) {

                JsonNode candidate = response.path("candidates").get(0);

                // Check if content was blocked by safety filters
                if (candidate.path("finishReason").asText().equals("SAFETY")) {
                    generatedReply = "Content was blocked by safety filters. Please try rephrasing your email.";
                } else {
                    generatedReply = candidate
                            .path("content")
                            .path("parts")
                            .get(0)
                            .path("text")
                            .asText("No reply generated.");
                }
            }

        } catch (WebClientResponseException e) {
            // Log the exact error response for debugging
            System.err.println("API Error Status: " + e.getStatusCode());
            System.err.println("API Error Response: " + e.getResponseBodyAsString());
            System.err.println("Request that failed: " + requestBody);
            generatedReply = "Error generating reply: " + e.getMessage();
        } catch (Exception e) {
            System.err.println("General error: " + e.getMessage());
            e.printStackTrace();
            generatedReply = "An error occurred while generating the reply.";
        }

        // Save in DB
        EmailReply emailReply = new EmailReply();
        emailReply.setOriginalEmail(emailRequest.getEmailContent());
        emailReply.setTone(emailRequest.getTone());
        emailReply.setGeneratedReply(generatedReply);
        emailReplyRepository.save(emailReply);

        return generatedReply;
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following email content. ");
        prompt.append("Do not generate a subject line. Only reply body. ");

        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone. ");
        }

        prompt.append("\nOriginal email:\n").append(emailRequest.getEmailContent());

        return prompt.toString();
    }
}