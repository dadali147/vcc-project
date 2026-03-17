package com.vcc.admin.controller;

import com.vcc.card.domain.Card;
import com.vcc.card.service.ICardService;
import com.vcc.common.TestUtils;
import com.vcc.framework.web.service.PermissionService;
import com.vcc.web.controller.admin.AdminCardController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = AdminCardControllerTest.TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "file:../src/test/resources/application-test.yml")
class AdminCardControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ICardService cardService;

    @Test
    @DisplayName("testListCards_需要admin角色")
    void testListCards_需要admin角色() throws Exception
    {
        // given: admin 用户访问卡片列表
        Card card = TestUtils.buildCard(200L, 100L, Card.STATUS_ACTIVE);
        when(cardService.selectCardList(any(Card.class))).thenReturn(List.of(card));

        // when / then: admin 角色可正常访问列表接口
        mockMvc.perform(get("/admin/card/list")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .with(authentication(TestUtils.buildAuthentication(1L, "admin", "admin"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.rows[0].id").value(200));
    }

    @Test
    @DisplayName("testListCards_merchant角色被拒绝403")
    void testListCards_merchant角色被拒绝403() throws Exception
    {
        // given: merchant 用户访问仅 admin 可见的接口

        // when / then: 应被方法级权限拦截为 403
        mockMvc.perform(get("/admin/card/list")
                        .with(authentication(TestUtils.buildAuthentication(200L, "merchant", "merchant"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("testChangeCardStatus_冻结成功")
    void testChangeCardStatus_冻结成功() throws Exception
    {
        // given: admin 修改卡片状态为冻结
        when(cardService.freezeCard(200L, 1L)).thenReturn(1);

        // when / then: 应调用冻结业务并返回成功
        mockMvc.perform(put("/admin/card/200/status")
                        .with(authentication(TestUtils.buildAuthentication(1L, "admin", "admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(cardService).freezeCard(200L, 1L);
    }

    @Test
    @DisplayName("testChangeCardStatus_注销成功")
    void testChangeCardStatus_注销成功() throws Exception
    {
        // given: admin 修改卡片状态为注销
        when(cardService.cancelCard(200L, 1L)).thenReturn(1);

        // when / then: 应调用注销业务并返回成功
        mockMvc.perform(put("/admin/card/200/status")
                        .with(authentication(TestUtils.buildAuthentication(1L, "admin", "admin")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(cardService).cancelCard(200L, 1L);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({AdminCardController.class, TestSecurityConfig.class})
    static class TestApplication
    {
        @Bean
        ICardService cardService()
        {
            return Mockito.mock(ICardService.class);
        }
    }

    @Configuration
    @EnableMethodSecurity(prePostEnabled = true)
    static class TestSecurityConfig
    {
        @Bean("ss")
        PermissionService permissionService()
        {
            return new PermissionService();
        }

        @Bean
        SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception
        {
            return httpSecurity
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(requests -> requests.anyRequest().authenticated())
                    .build();
        }
    }
}
