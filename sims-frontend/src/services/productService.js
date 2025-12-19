import api from '../api/axios';

const PRODUCT_ENDPOINTS = {
    GET_ALL: '/products',
    GET_ALL_LIST: '/products/all',
    ADD_PRODUCT: '/products',
    UPDATE_PRODUCT: '/products',
    DELETE_PRODUCT: '/products',
    SEARCH:  '/products/search',
    FILTER: '/products/filter',
    GENERATE_REPORT: '/products/report',
};

class ProductService {
    /**
     * Get All Products with Pagination
     * @param {Object} params - { page, size, sortBy, sortDirection }
     * @returns {Promise<Object>} PaginatedResponse<ProductResponse>
     */
    async getAllProducts(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(PRODUCT_ENDPOINTS.GET_ALL, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get All Products List (No Pagination)
     * @returns {Promise<Array>} List<ProductResponse>
     */
    async getAllProductsList() {
        try {
            const response = await api.get(PRODUCT_ENDPOINTS.GET_ALL_LIST);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Add Single Product
     * @param {Object} productData - { name, location, category, price, status }
     * @returns {Promise<Object>} ProductResponse
     */
    async addProduct(productData) {
        try {
            const response = await api.post(PRODUCT_ENDPOINTS.ADD_PRODUCT, productData);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Update Product
     * @param {string} productId - Product ID
     * @param {Object} productData - { name, location, category, price, status }
     * @returns {Promise<Object>} ApiResponse
     */
    async updateProduct(productId, productData) {
        try {
            const response = await api.put(`${PRODUCT_ENDPOINTS.UPDATE_PRODUCT}/${productId}`, productData);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Delete Product
     * @param {string} productId - Product ID
     * @returns {Promise<Object>} ApiResponse
     */
    async deleteProduct(productId) {
        try {
            const response = await api.delete(`${PRODUCT_ENDPOINTS.DELETE_PRODUCT}/${productId}`);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Search Products
     * @param {Object} params - { text, page, size, sortBy, sortDirection }
     * @returns {Promise<Object>} PaginatedResponse<ProductResponse>
     */
    async searchProducts(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(PRODUCT_ENDPOINTS.SEARCH, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Filter Products
     * @param {Object} params - { filter, page, size, sortBy, direction }
     * @returns {Promise<Object>} PaginatedResponse<ProductResponse>
     */
    async filterProducts(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(PRODUCT_ENDPOINTS.FILTER, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Generate Product Report (Excel)
     * @returns {Promise<Blob>} Excel file
     */
    async generateReport() {
        try {
            const response = await api.get(PRODUCT_ENDPOINTS.GENERATE_REPORT, {
                responseType: 'blob',
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Error handler
     */
    handleError(error) {
        if (error.response) {
            return {
                message: error.response.data.message || 'An error occurred',
                status: error.response.status,
                success: false,
            };
        } else if (error.request) {
            return {
                message: 'No response from server. Please check your connection.',
                status: null,
                success: false,
            };
        } else {
            return {
                message: error.message || 'An unexpected error occurred',
                status:  null,
                success: false,
            };
        }
    }
}

export default new ProductService();