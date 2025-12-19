import api from '../../api/axios.js';

const SALES_ORDER_ENDPOINTS = {
    // Order Management endpoints
    GET_ALL:  '/order-management/sales-orders',
    GET_DETAILS: (orderId) => `/order-management/sales-orders/${orderId}`,
    CREATE: '/order-management/sales-orders',
    UPDATE: (orderId) => `/order-management/sales-orders/${orderId}`,
    SEARCH: '/order-management/sales-orders/search',
    FILTER: '/order-management/sales-orders/filter',
    ADD_ITEMS: (orderId) => `/order-management/sales-orders/${orderId}/items`,
    REMOVE_ITEM: (orderId, itemId) => `/order-management/sales-orders/${orderId}/items/${itemId}`,

    // Inventory endpoints
    METRICS: '/inventory/sales-orders',
};

class SalesOrderService {
    /**
     * Get Sales Order Metrics
     * @returns {Promise<Object>} SalesOrderSummary
     */
    async getMetrics() {
        try {
            const response = await api.get(SALES_ORDER_ENDPOINTS.METRICS);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get All Sales Orders
     * @param {Object} params - { page, size, sortBy, sortDirection }
     * @returns {Promise<Object>} PaginatedResponse<SummarySalesOrderView>
     */
    async getAllSalesOrders(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(SALES_ORDER_ENDPOINTS.GET_ALL, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get Sales Order Details
     * @param {number} orderId - Sales Order ID
     * @returns {Promise<Object>} DetailedSalesOrderView
     */
    async getOrderDetails(orderId) {
        try {
            const response = await api.get(SALES_ORDER_ENDPOINTS.GET_DETAILS(orderId));
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Create Sales Order
     * @param {Object} data - SalesOrderRequest
     * @returns {Promise<Object>} ApiResponse
     */
    async createSalesOrder(data) {
        try {
            const response = await api.post(SALES_ORDER_ENDPOINTS.CREATE, data);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Update Sales Order
     * @param {number} orderId - Sales Order ID
     * @param {Object} data - SalesOrderRequest
     * @returns {Promise<Object>} ApiResponse
     */
    async updateSalesOrder(orderId, data) {
        try {
            const response = await api.put(SALES_ORDER_ENDPOINTS.UPDATE(orderId), data);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Search Sales Orders
     * @param {Object} params - { text, page, size, sortBy, sortDirection }
     * @returns {Promise<Object>} PaginatedResponse<SummarySalesOrderView>
     */
    async searchOrders(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(SALES_ORDER_ENDPOINTS.SEARCH, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Filter Sales Orders
     * @param {Object} params - { status, page, size, sortBy, sortDirection }
     * @returns {Promise<Object>} PaginatedResponse<SummarySalesOrderView>
     */
    async filterOrders(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(SALES_ORDER_ENDPOINTS.FILTER, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Add Items to Sales Order
     * @param {number} orderId - Sales Order ID
     * @param {Object} data - BulkOrderItemsRequestDto
     * @returns {Promise<Object>} ApiResponse
     */
    async addItems(orderId, data) {
        try {
            const response = await api.patch(SALES_ORDER_ENDPOINTS.ADD_ITEMS(orderId), data);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Remove Item from Sales Order
     * @param {number} orderId - Sales Order ID
     * @param {number} itemId - Order Item ID
     * @returns {Promise<Object>} ApiResponse
     */
    async removeItem(orderId, itemId) {
        try {
            const response = await api.delete(SALES_ORDER_ENDPOINTS.REMOVE_ITEM(orderId, itemId));
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

export default new SalesOrderService();