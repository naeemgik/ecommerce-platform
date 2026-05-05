package com.ecommerce.product;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Product Controller Integration Tests")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private KafkaTemplate<String, ?> kafkaTemplate;

    private Long testProductId;

    @BeforeEach
    void setUp() {
        Product product = Product.builder()
                .sku("INT-TEST-001")
                .name("Integration Test Product")
                .description("Created for integration testing")
                .price(new BigDecimal("149.99"))
                .stockQuantity(50)
                .category("Electronics")
                .brand("TestBrand")
                .active(true)
                .build();
        Product saved = productRepository.save(product);
        testProductId = saved.getId();
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - should return product when found")
    void getProductById_Found_Returns200() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/products/" + testProductId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testProductId))
                .andExpect(jsonPath("$.data.sku").value("INT-TEST-001"))
                .andExpect(jsonPath("$.data.name").value("Integration Test Product"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("INT-TEST-001");
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - should return 404 when not found")
    void getProductById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/products/999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /api/v1/products - should create product successfully")
    void createProduct_ValidRequest_Returns201() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .sku("NEW-INT-001")
                .name("New Integration Product")
                .price(new BigDecimal("59.99"))
                .stockQuantity(100)
                .category("Books")
                .brand("TestPublisher")
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value("NEW-INT-001"))
                .andExpect(jsonPath("$.message").value("Product created successfully"));
    }

    @Test
    @DisplayName("POST /api/v1/products - should return 400 for invalid request")
    void createProduct_InvalidRequest_Returns400() throws Exception {
        ProductRequest request = ProductRequest.builder()
                .sku("") // Invalid: empty SKU
                .name("A")  // Invalid: too short
                .price(new BigDecimal("-10.00")) // Invalid: negative price
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - should soft delete product")
    void deleteProduct_Exists_Returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/products/" + testProductId))
                .andExpect(status().isNoContent());

        // Verify product is soft deleted
        mockMvc.perform(get("/api/v1/products/" + testProductId))
                .andExpect(status().isNotFound());
    }
}
