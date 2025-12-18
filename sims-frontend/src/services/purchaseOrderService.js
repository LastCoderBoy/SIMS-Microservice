import api from '../api/axios';

const PURCHASE_ORDER_ENDPOINTS = {
    // Order Management endpoints
    GET_ALL:  '/order-management/purchase-orders',
    GET_DETAILS: (orderId) => `/order-management/purchase-orders/${orderId}`,
    CREATE: '/order-management/purchase-orders',
    SEARCH: '/order-management/purchase-orders/search',
    FILTER: '/order-management/purchase-orders/filter',

    // Inventory endpoints
    METRICS: '/inventory/purchase-orders',
    CANCEL: (orderId) => `/inventory/purchase-orders/${orderId}/cancel`,
};

class PurchaseOrderService {
    /**
     * Get Purchase Order Metrics
     * @returns {Promise<Object>} PurchaseOrderSummary
     */
    async getMetrics() {
        try {
            const response = await api.get(PURCHASE_ORDER_ENDPOINTS.METRICS);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get All Purchase Orders
     * @param {Object} params - { page, size, sortBy, sortDirection }
     * @returns {Promise<Object>} PaginatedResponse<SummaryPurchaseOrderView>
     */
    async getAllPurchaseOrders(params = {}) {
        try {
            const queryParams = {};
            Object.keys(params).forEach(key => {
                if (params[key] !== undefined && params[key] !== null && params[key] !== '') {
                    queryParams[key] = params[key];
                }
            });

            const response = await api.get(PURCHASE_ORDER_ENDPOINTS.GET_ALL, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get Purchase Order Details
     * @param {number} orderId - Purchase Order ID
     * @returns {Promise<Object>} PurchaseOrderDetailsView
     */
    async getOrderDetails(orderId) {
        try {
            const response = await api.get(PURCHASE_ORDER_ENDPOINTS.GET_DETAILS(orderId));
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Create Purchase Order
     * @param {Object} data - PurchaseOrderRequest
     * @returns {Promise<Object>} ApiResponse
     */
    async createPurchaseOrder(data) {
        try {
            const response = await api.post(PURCHASE_ORDER_ENDPOINTS.CREATE, data);
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

            const response = await api.get(PURCHASE_ORDER_ENDPOINTS.SEARCH, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Filter Purchase Orders
     * @param {Object} params - { category, status, page, size, sortBy, sortDirection }
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

            const response = await api.get(PURCHASE_ORDER_ENDPOINTS.FILTER, {
                params: queryParams,
            });
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
            const response = await api.put(PURCHASE_ORDER_ENDPOINTS.CANCEL(orderId));
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

export default new PurchaseOrderService();