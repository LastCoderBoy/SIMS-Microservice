import api from '../api/axios';

const PRODUCT_ENDPOINTS = {
    GET_ALL: '/products',
    GET_ALL_LIST: '/products/all',
};

class ProductService {
    /**
     * Get All Products
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