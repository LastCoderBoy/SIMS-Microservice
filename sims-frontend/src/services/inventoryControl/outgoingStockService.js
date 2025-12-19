import api from '../../api/axios.js';

const OUTGOING_STOCK_ENDPOINTS = {
    METRICS: '/inventory/sales-orders',
    ALL: '/inventory/sales-orders/all',
    URGENT: '/inventory/sales-orders/urgent',
    SEARCH:  '/inventory/sales-orders/search',
    FILTER: '/inventory/sales-orders/filter',
    STOCK_OUT: '/inventory/sales-orders/stocks/out',
    CANCEL:  (orderId) => `/inventory/sales-orders/${orderId}/cancel`,
    ORDER_DETAILS: (orderId) => `/inventory/sales-orders/${orderId}/items`,
};

class OutgoingStockService {
    /**
     * Get Sales Order Metrics
     * @returns {Promise<Object>} SalesOrderSummary
     */
    async getMetrics() {
        try {
            const response = await api.get(OUTGOING_STOCK_ENDPOINTS.METRICS);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get All Waiting Sales Orders
     * @param {Object} params - { page, size, sortBy, sortDir }
     * @returns {Promise<Object>} PaginatedResponse<SummarySalesOrderView>
     */
    async getAllWaitingOrders(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(OUTGOING_STOCK_ENDPOINTS.ALL, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get Urgent Sales Orders (delivery < 2 days)
     * @param {Object} params - { page, size, sortBy, sortDir }
     * @returns {Promise<Object>} PaginatedResponse<SummarySalesOrderView>
     */
    async getUrgentOrders(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(OUTGOING_STOCK_ENDPOINTS.URGENT, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get Order Details with Items
     * @param {number} orderId - Sales Order ID
     * @returns {Promise<Object>} DetailedSalesOrderView
     */
    async getOrderDetails(orderId) {
        try {
            const response = await api.get(OUTGOING_STOCK_ENDPOINTS.ORDER_DETAILS(orderId));
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Search Sales Orders
     * @param {Object} params - { text, page, size, sortBy, sortDir }
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

            const response = await api.get(OUTGOING_STOCK_ENDPOINTS.SEARCH, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this. handleError(error);
        }
    }

    /**
     * Filter Sales Orders
     * @param {Object} params - { status, optionDate, startDate, endDate, page, size, sortBy, sortDirection }
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

            const response = await api.get(OUTGOING_STOCK_ENDPOINTS. FILTER, {
                params:  queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Stock Out (Process/Fulfill Order)
     * @param {Object} data - { orderId, itemQuantities:  { productId:  quantity } }
     * @returns {Promise<Object>} ApiResponse
     */
    async stockOut(data) {
        try {
            const response = await api.put(OUTGOING_STOCK_ENDPOINTS.STOCK_OUT, data);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Cancel Sales Order
     * @param {number} orderId - Sales Order ID
     * @returns {Promise<Object>} ApiResponse
     */
    async cancelOrder(orderId) {
        try {
            const response = await api.put(OUTGOING_STOCK_ENDPOINTS.CANCEL(orderId));
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
                message:  error.response.data.message || 'An error occurred',
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

export default new OutgoingStockService();