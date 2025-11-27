package com.sims.simscoreservice.product.services;


import com.sims.common.exceptions.ValidationException;
import com.sims.common.models.PaginatedResponse;
import com.sims.simscoreservice.product.dto.BatchProductRequest;
import com.sims.simscoreservice.product.dto.BatchProductResponse;
import com.sims.simscoreservice.product.dto.ProductRequest;
import com.sims.simscoreservice.product.dto.ProductResponse;
import com.sims.simscoreservice.product.entity.Product;
import com.sims.simscoreservice.product.enums.ProductStatus;
import com.sims.simscoreservice.product.mapper.ProductMapper;
import com.sims.simscoreservice.product.repository.ProductRepository;
import com.sims.simscoreservice.product.helper.ProductHelper;
import com.sims.simscoreservice.product.services.impl.ProductServiceImpl;
import com.sims.simscoreservice.product.services.queryService.ProductQueryService;
import com.sims.simscoreservice.product.services.searchService.ProductSearchService;
import com.sims.simscoreservice.product.util.ProductTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for ProductServiceImpl
 * Tests methods WITHOUT inventory dependencies
 *
 * @author LastCoderBoy
 * @since 2025-01-23
 */
@ExtendWith(MockitoExtension. class)
@DisplayName("Product Service Unit Tests")
public class ProductServiceImplTest {
    private final int PAGE_SIZE = 10;
    private final int PAGE_NUMBER = 0;
    private Product productA;
    private Product productB;
    private Product productC;
    private ProductRequest validRequest;
    private ProductRequest planningRequest;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductHelper productHelper;

    @Mock
    private ProductQueryService productQueryService;

    @Mock
    private ProductSearchService productSearchService;

    @InjectMocks
    private ProductServiceImpl serviceUnderTest;

    @BeforeEach
    void setUp() {
        productA = ProductTestUtils.createProductA();
        productB = ProductTestUtils.createProductB();
        productC = ProductTestUtils.createProductC();
        validRequest = ProductTestUtils.createValidProductRequest();
        planningRequest = ProductTestUtils.createPlanningProductRequest();
    }

    @Test
    @DisplayName("Should return paginated products successfully")
    void testGetAllProducts_Success() {
        // Arrange
        List<Product> products = List.of(productA, productB, productC);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(PAGE_NUMBER, PAGE_SIZE), products.size());
        List<ProductResponse> productContent = List.of(
                ProductTestUtils.createProductResponse(productA),
                ProductTestUtils.createProductResponse(productB),
                ProductTestUtils.createProductResponse(productC)
        );
        PaginatedResponse<ProductResponse> expectedResponse = new PaginatedResponse<>();
        expectedResponse.setContent(productContent);
        expectedResponse.setTotalPages(1);
        expectedResponse.setTotalElements(products.size());
        expectedResponse.setCurrentPage(PAGE_NUMBER);
        expectedResponse.setPageSize(PAGE_SIZE);

        // Stubbing
        when(productQueryService.getAllProducts(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(productPage);
        when(productHelper.toPaginatedResponse(productPage)).thenReturn(expectedResponse);


        // Act
        PaginatedResponse<ProductResponse> actualResponse =
                serviceUnderTest.getAllProducts("productId", "desc", PAGE_NUMBER, PAGE_SIZE);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getContent()).isEqualTo(expectedResponse.getContent());
        assertThat(actualResponse.getTotalPages()).isEqualTo(1);
        assertThat(actualResponse.getContent()).hasSize(3);

        verify(productQueryService, times(1))
                .getAllProducts("productId", "desc", 0, 10);
        verify(productHelper, times(1))
                . toPaginatedResponse(productPage);
    }

    @Test
    @DisplayName("Should return empty page when no products exist")
    void testGetAllProducts_EmptyResult() {
        // Arrange
        Page<Product> emptyPage = new PageImpl<>(List.of());
        PaginatedResponse<ProductResponse> emptyResponse = new PaginatedResponse<>();
        emptyResponse.setContent(List. of());
        emptyResponse. setTotalElements(0L);

        when(productQueryService.getAllProducts(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(emptyPage);
        when(productHelper. toPaginatedResponse(emptyPage))
                .thenReturn(emptyResponse);

        // Act
        PaginatedResponse<ProductResponse> result =
                serviceUnderTest.getAllProducts("productId", "asc", 0, 10);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result. getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // ========================================
    // Test: addProduct() - PLANNING status (no inventory)
    // ========================================

    @Test
    @DisplayName("Should add product with PLANNING status successfully (no inventory call)")
    void testAddProduct_PlanningStatus_Success() {
        // Arrange
        Product savedProduct = ProductTestUtils.createProductB();
        savedProduct.setProductId("PRD001");

        ProductResponse expectedResponse = ProductTestUtils.createProductResponse(savedProduct);

        // Stubbing
        doNothing().when(productHelper).validateProduct(planningRequest);
        when(productMapper.toEntity(planningRequest)).thenReturn(savedProduct);
        when(productRepository.findLastProductId()).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productMapper.toResponse(savedProduct)).thenReturn(expectedResponse);

        // Act
        ProductResponse result = serviceUnderTest.addProduct(planningRequest, "admin");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo("PRD001");
        assertThat(result.getStatus()).isEqualTo(ProductStatus.PLANNING);

        verify(productHelper, times(1)).validateProduct(planningRequest);
        verify(productRepository, times(1)).save(any(Product.class));
        // Note: No inventory service call because status is PLANNING
    }

    @Test
    @DisplayName("Should throw ValidationException when validation fails")
    void testAddProduct_ValidationFails() {
        // Arrange
        doThrow(new ValidationException("Invalid product data"))
                .when(productHelper).validateProduct(any());

        // Act & Assert
        assertThatThrownBy(() -> serviceUnderTest.addProduct(validRequest, "admin"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Invalid product data");

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when duplicate product name")
    void testAddProduct_DuplicateName() {
        // Arrange
        doNothing().when(productHelper).validateProduct(validRequest);
        when(productMapper.toEntity(validRequest)).thenReturn(productA);
        when(productRepository.findLastProductId()).thenReturn(Optional.of("PRD005"));
        when(productRepository.save(any(Product.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        // Act & Assert
        assertThatThrownBy(() -> serviceUnderTest.addProduct(validRequest, "admin"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Product with this name already exists");
    }

    @Test
    @DisplayName("Should handle partial success in batch")
    void testAddProductsBatch_PartialSuccess() {
        // Arrange
        List<ProductRequest> requests = Arrays.asList(
                ProductTestUtils.createPlanningProductRequest(),
                ProductTestUtils.createInvalidProductRequest(), // This will fail
                ProductTestUtils.createPlanningProductRequest()
        );
        BatchProductRequest batchRequest = new BatchProductRequest();
        batchRequest.setProducts(requests);

        Product savedProduct = ProductTestUtils.createProductB();
        savedProduct.setProductId("PRD001");

        // Stubbing
        when(productMapper.toEntity(any())).thenReturn(savedProduct);
        when(productRepository.findLastProductId()). thenReturn(Optional.empty());
        when(productRepository. save(any(Product.class))).thenReturn(savedProduct);

        // Act
        BatchProductResponse result = serviceUnderTest.addProductsBatch(batchRequest, "admin");

        // Assert
        assertThat(result.getTotalRequested()).isEqualTo(3);
        assertThat(result.getSuccessCount()).isEqualTo(2);
        assertThat(result.getFailureCount()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getIndex()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should search products successfully")
    void testSearchProducts_Success() {
        String searchText = "LEGO";
        List<Product> products = List.of(productB);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(PAGE_NUMBER, PAGE_SIZE), products.size());

        PaginatedResponse<ProductResponse> expectedResponse = new PaginatedResponse<>();
        expectedResponse.setContent(List.of(ProductTestUtils.createProductResponse(productB)));
        expectedResponse.setTotalPages(1);
        expectedResponse.setTotalElements(products.size());
        expectedResponse.setCurrentPage(PAGE_NUMBER);
        expectedResponse.setPageSize(PAGE_SIZE);

        // Stubbing
        when(productSearchService
                .searchProduct(searchText, "productId", "desc", PAGE_NUMBER, PAGE_SIZE))
                .thenReturn(productPage);
        when(productHelper.toPaginatedResponse(productPage)).thenReturn(expectedResponse);

        // Act
        PaginatedResponse<ProductResponse> actualResponse =
                serviceUnderTest.searchProducts(searchText, "productId", "desc", PAGE_NUMBER, PAGE_SIZE);

        // Assert
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getContent()).hasSize(1);
        assertThat(actualResponse.getContent().get(0).getName()).isEqualTo("LEGO Education Set");

        verify(productSearchService, times(1))
                .searchProduct(searchText, "productId", "desc", 0, 10);
    }

    @Test
    @DisplayName("Should filter products by category successfully")
    void testFilterProducts_Success() {
        // Arrange
        String filter = "category:ELECTRONIC";
        List<Product> filterResults = List.of(productA);
        Page<Product> filterPage = new PageImpl<>(filterResults);

        PaginatedResponse<ProductResponse> expectedResponse = new PaginatedResponse<>();
        expectedResponse.setContent(List.of(ProductTestUtils.createProductResponse(productA)));

        when(productSearchService.filterProducts(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(filterPage);
        when(productHelper.toPaginatedResponse(filterPage))
                .thenReturn(expectedResponse);

        // Act
        PaginatedResponse<ProductResponse> result =
                serviceUnderTest.filterProducts(filter, "productId", "desc", 0, 10);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(productSearchService, times(1))
                . filterProducts(filter, "productId", "desc", 0, 10);
    }

    // ========================================
    // Test: generateProductId()
    // ========================================

    @Test
    @DisplayName("Should generate PRD001 when no products exist")
    void testGenerateProductId_FirstProduct() {
        // Arrange
        when(productRepository.findLastProductId()).thenReturn(Optional.empty());

        // Act
        String result = serviceUnderTest.generateProductId();

        // Assert
        assertThat(result).isEqualTo("PRD001");
    }

    @Test
    @DisplayName("Should generate next sequential product ID")
    void testGenerateProductId_Sequential() {
        // Arrange
        when(productRepository.findLastProductId()).thenReturn(Optional.of("PRD005"));

        // Act
        String result = serviceUnderTest.generateProductId();

        // Assert
        assertThat(result).isEqualTo("PRD006");
    }

    @Test
    @DisplayName("Should handle large product ID numbers")
    void testGenerateProductId_LargeNumber() {
        // Arrange
        when(productRepository.findLastProductId()).thenReturn(Optional.of("PRD999"));

        // Act
        String result = serviceUnderTest.generateProductId();

        // Assert
        assertThat(result).isEqualTo("PRD1000");
    }
}
