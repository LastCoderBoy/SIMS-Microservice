import api from '../api/axios';

const PURCHASE_ORDERS_ENDPOINTS = {
    METRICS: '/inventory/purchase-orders',
    ALL: '/inventory/purchase-orders/all',
    OVERDUE:  '/inventory/purchase-orders/overdue',
    SEARCH: '/inventory/purchase-orders/search',
    FILTER: '/inventory/purchase-orders/filter',
    RECEIVE: (orderId) => `/inventory/purchase-orders/${orderId}/receive`,
    CANCEL: (orderId) => `/inventory/purchase-orders/${orderId}/cancel`,
};

class PurchaseOrdersService {
    /**
     * Get Purchase Order Metrics
     * @returns {Promise<Object>} PurchaseOrderSummary
     */
    async getMetrics() {
        try {
            const response = await api.get(PURCHASE_ORDERS_ENDPOINTS.METRICS);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get All Pending Purchase Orders
     * @param {Object} params - { page, size, sortBy, sortDirection }
     * @returns {Promise<Object>} PaginatedResponse<SummaryPurchaseOrderView>
     */
    async getAllPendingOrders(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(PURCHASE_ORDERS_ENDPOINTS.ALL, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get Overdue Purchase Orders
     * @param {Object} params - { page, size }
     * @returns {Promise<Object>} PaginatedResponse<SummaryPurchaseOrderView>
     */
    async getOverdueOrders(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(PURCHASE_ORDERS_ENDPOINTS.OVERDUE, {
                params:  queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Search Purchase Orders
     * @param {Object} params - { text, page, size, sortBy, sortDirection }
     * @returns {Promise<Object>} PaginatedResponse<SummaryPurchaseOrderView>
     */
    async searchOrders(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(PURCHASE_ORDERS_ENDPOINTS.SEARCH, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Filter Purchase Orders
     * @param {Object} params - { status, category, page, size, sortBy, sortDirection }
     * @returns {Promise<Object>} PaginatedResponse<SummaryPurchaseOrderView>
     */
    async filterOrders(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(PURCHASE_ORDERS_ENDPOINTS.FILTER, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Receive Stock (Stock In)
     * @param {number} orderId - Purchase Order ID
     * @param {Object} data - { receivedQuantity, actualArrivalDate?  }
     * @returns {Promise<Object>} ApiResponse
     */
    async receiveStock(orderId, data) {
        try {
            const response = await api.put(PURCHASE_ORDERS_ENDPOINTS.RECEIVE(orderId), data);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Cancel Purchase Order
     * @param {number} orderId - Purchase Order ID
     * @returns {Promise<Object>} ApiResponse
     */
    async cancelOrder(orderId) {
        try {
            const response = await api.put(PURCHASE_ORDERS_ENDPOINTS.CANCEL(orderId));
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

export default new PurchaseOrdersService();