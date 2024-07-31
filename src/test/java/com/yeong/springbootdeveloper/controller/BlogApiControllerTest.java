package com.yeong.springbootdeveloper.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yeong.springbootdeveloper.domain.Article;
import com.yeong.springbootdeveloper.dto.AddArticleRequest;
import com.yeong.springbootdeveloper.dto.UpdateArticleRequest;
import com.yeong.springbootdeveloper.repository.BlogRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BlogApiControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    BlogRepository blogRepository;

    @BeforeEach
    void mockMvcSetUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();

        blogRepository.deleteAll();
    }

    @DisplayName("addArticle : 블로그 글 추가에 성공한다.")
    @Test
    void addArticle() throws Exception {
        // given
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";
        final AddArticleRequest userRequest = new AddArticleRequest(title, content);

        // 객체 JSON으로 직렬화
        final String requestBody = objectMapper.writeValueAsString(userRequest);

        // when
        ResultActions result = mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());

        List<Article> articles = blogRepository.findAll();

        assertThat(articles.size()).isEqualTo(1);
        assertThat(articles.get(0).getTitle()).isEqualTo("title");
        assertThat(articles.get(0).getContent()).isEqualTo("content");
    }

    @DisplayName("findAllArticles : 블로그 글 목록 조회에 성공한다.")
    @Test
    void findAllArticles() throws Exception {
        // given
        final String url = "/api/articles";
        final String title = "title";
        final String content = "content";

        blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        // when
        ResultActions result = mockMvc.perform(get(url)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value(content))
                .andExpect(jsonPath("$[0].title").value(title));
    }

    @DisplayName("findArticle : 블로그 글 조회에 성공한다.")
    @Test
    void findArticle() throws Exception {
        // given
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        // when
        ResultActions result = mockMvc.perform(get(url, savedArticle.getId()));

        // then
        result
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.content").value(content));
    }

    @DisplayName("deleteArticle : 블로그 글 삭제에 성공한다.")
    @Test
    void deleteArticle() throws Exception {
        // given
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        // when
        ResultActions result = mockMvc.perform(delete(url, savedArticle.getId()));

        // then
        result.andExpect(status().isOk());

        List<Article> articles = blogRepository.findAll();

        assertThat(articles).isEmpty();
    }

    @DisplayName("updateArticle :  블로그 글 수정에 성공한다.")
    @Test
    void updateArticle() throws Exception {
        // given
        final String url = "/api/articles/{id}";
        final String title = "title";
        final String content = "content";

        Article savedArticle = blogRepository.save(Article.builder()
                .title(title)
                .content(content)
                .build());

        final String newTitle = "newTitle";
        final String newContent = "newContent";

        UpdateArticleRequest request = new UpdateArticleRequest(newTitle, newContent);

        // when
        ResultActions result = mockMvc.perform(put(url, savedArticle.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result
                .andExpect(status().isOk());

        Article article = blogRepository.findById(savedArticle.getId()).get();

        assertThat(article.getTitle()).isEqualTo(newTitle);
        assertThat(article.getContent()).isEqualTo(newContent);
    }
}
