package com.leetcoder.infrastructure.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class LeetCodeClient {

  private final RestClient restClient;

  public LeetCodeClient(RestClient.Builder builder) {
    this.restClient = builder
        .baseUrl("https://leetcode.com")
        .build();
  }

  /**
   * Fetches the last 20 Accepted submissions.
   * Includes 'lang' just in case you want to filter by language later.
   */
  @CircuitBreaker(name = "leetcode", fallbackMethod = "submissionFallback")
  @RateLimiter(name = "leetcode")
  public List<SubmissionDto> getRecentSubmissions(String username) {
    String query = """
        query recentAcSubmissions($username: String!, $limit: Int!) {
          recentAcSubmissionList(username: $username, limit: $limit) {
            id
            title
            titleSlug
            timestamp
            lang
          }
        }
        """;

    var body = Map.of(
        "query", query,
        "variables", Map.of("username", username, "limit", 20));

    @SuppressWarnings("null")
    var response = restClient.post()
        .uri("/graphql")
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(SubmissionResponseWrapper.class);

    if (response != null && response.data() != null && response.data().recentAcSubmissionList() != null) {
      return response.data().recentAcSubmissionList();
    }
    return List.of();
  }

  /**
   * Lazy loads a single question's details.
   */
  @CircuitBreaker(name = "leetcode", fallbackMethod = "questionFallback")
  @RateLimiter(name = "leetcode")
  public QuestionDto getQuestionDetails(String titleSlug) {
    String query = """
        query questionData($titleSlug: String!) {
          question(titleSlug: $titleSlug) {
            questionId
            title
            titleSlug
            difficulty
          }
        }
        """;

    var body = Map.of(
        "query", query,
        "variables", Map.of("titleSlug", titleSlug));

    @SuppressWarnings("null")
    var response = restClient.post()
        .uri("/graphql")
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .body(QuestionResponseWrapper.class);

    if (response != null && response.data() != null) {
      return response.data().question();
    }
    return null;
  }

  // --- Fallbacks ---

  public List<SubmissionDto> submissionFallback(String username, Throwable t) {
    // Log the error here with Slf4j if desired
    return List.of();
  }

  public QuestionDto questionFallback(String titleSlug, Throwable t) {
    return null;
  }

  // --- DTO Records ---

  // Wrapper: { "data": { "recentAcSubmissionList": [ ... ] } }
  record SubmissionResponseWrapper(RecentAcSubmissionListResponse data) {
  }

  record RecentAcSubmissionListResponse(List<SubmissionDto> recentAcSubmissionList) {
  }

  // Added 'lang' here
  public record SubmissionDto(String id, String title, String titleSlug, String timestamp, String lang) {
  }

  // Wrapper: { "data": { "question": { ... } } }
  record QuestionResponseWrapper(QuestionDataResponse data) {
  }

  record QuestionDataResponse(QuestionDto question) {
  }

  public record QuestionDto(String questionId, String title, String titleSlug, String difficulty) {
  }
}