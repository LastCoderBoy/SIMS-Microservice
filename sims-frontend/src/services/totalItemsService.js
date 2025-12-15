import api from '../api/axios';

const TOTAL_ITEMS_ENDPOINTS = {
    ALL:  '/inventory/total',
    UPDATE: (sku) => `/inventory/total/${sku}/update`,
    DELETE: (sku) => `/inventory/total/${sku}`,
    SEARCH: '/inventory/total/search',
    FILTER: '/inventory/total/filter',
    REPORT: '/inventory/total/report',
};

class TotalItemsService {
    /**
     * Get All Inventory Products
     * @param {Object} params - { sortBy, sortDirection, page, size }
     * @returns {Promise<Object>} PaginatedResponse<InventoryResponse>
     */
    async getAllProducts(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(TOTAL_ITEMS_ENDPOINTS.ALL, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Update Inventory Stock Levels
     * @param {string} sku - Stock Keeping Unit
     * @param {Object} data - { currentStock, minLevel }
     * @returns {Promise<Object>} ApiResponse
     */
    async updateProduct(sku, data) {
        try {
            const response = await api.put(TOTAL_ITEMS_ENDPOINTS.UPDATE(sku), data);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Delete Inventory Product
     * @param {string} sku - Stock Keeping Unit
     * @returns {Promise<Object>} ApiResponse
     */
    async deleteProduct(sku) {
        try {
            const response = await api.delete(TOTAL_ITEMS_ENDPOINTS.DELETE(sku));
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Search Inventory Products
     * @param {Object} params - { text, sortBy, sortDirection, page, size }
     * @returns {Promise<Object>} PaginatedResponse<InventoryResponse>
     */
    async searchProducts(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(TOTAL_ITEMS_ENDPOINTS.SEARCH, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Filter Inventory Products
     * @param {Object} params - { filterBy, sortBy, sortDirection, page, size }
     * @returns {Promise<Object>} PaginatedResponse<InventoryResponse>
     */
    async filterProducts(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(TOTAL_ITEMS_ENDPOINTS.FILTER, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Download Excel Report
     * @param {Object} params - { sortBy, sortDirection }
     */
    async downloadReport(params = {}) {
        try {
            const queryParams = {};
            if (params.sortBy) queryParams.sortBy = params.sortBy;
            if (params.sortDirection) queryParams.sortDirection = params.sortDirection;

            const response = await api.get(TOTAL_ITEMS_ENDPOINTS.REPORT, {
                params: queryParams,
                responseType: 'blob',
            });

            // Create download link
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'TotalItemsReport.xlsx');
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);

            return { success: true, message: 'Report downloaded successfully' };
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
                message:  'No response from server. Please check your connection.',
                status: null,
                success: false,
            };
        } else {
            return {
                message: error.message || 'An unexpected error occurred',
                status: null,
                success: false,
            };
        }
    }
}

export default new TotalItemsService();