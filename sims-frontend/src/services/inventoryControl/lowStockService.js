import api from '../../api/axios.js';

const LOW_STOCK_ENDPOINTS = {
    METRICS: '/inventory/low-stock',
    ALL: '/inventory/low-stock/all',
    SEARCH: '/inventory/low-stock/search',
    FILTER:  '/inventory/low-stock/filter',
    REPORT: '/inventory/low-stock/report',
};

class LowStockService {
    /**
     * Get Low Stock Metrics
     * @returns {Promise<Object>} LowStockMetrics
     */
    async getMetrics() {
        try {
            const response = await api.get(LOW_STOCK_ENDPOINTS.METRICS);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get All Low Stock Products
     * @param {Object} params - { sortBy, sortDirection, page, size }
     * @returns {Promise<Object>} PaginatedResponse<InventoryResponse>
     */
    async getAllLowStockProducts(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(LOW_STOCK_ENDPOINTS.ALL, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Search Low Stock Products
     * @param {Object} params - { text, sortBy, sortDirection, page, size }
     * @returns {Promise<Object>} PaginatedResponse<InventoryResponse>
     */
    async searchLowStockProducts(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(LOW_STOCK_ENDPOINTS.SEARCH, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Filter Low Stock Products by Category
     * @param {Object} params - { category, sortBy, sortDirection, page, size }
     * @returns {Promise<Object>} PaginatedResponse<InventoryResponse>
     */
    async filterLowStockProducts(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(LOW_STOCK_ENDPOINTS.FILTER, {
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

            const response = await api.get(LOW_STOCK_ENDPOINTS.REPORT, {
                params:  queryParams,
                responseType: 'blob',
            });

            // Create download link
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'LowStockReport.xlsx');
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

export default new LowStockService();