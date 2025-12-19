import api from '../../api/axios.js';

const ANALYTICS_ENDPOINTS = {
    DASHBOARD:  '/analytics/dashboard',
    INVENTORY_HEALTH: '/analytics/inventory-health',
    FINANCIAL_OVERVIEW: '/analytics/financial-overview',
    ORDER_SUMMARY: '/analytics/order-summary',
};

class AnalyticsService {
    /**
     * Get Dashboard Metrics
     * @returns {Promise<Object>} DashboardMetrics
     */
    async getDashboardMetrics() {
        try {
            const response = await api.get(ANALYTICS_ENDPOINTS.DASHBOARD);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get Inventory Health Metrics
     * @returns {Promise<Object>} InventoryReportMetrics
     */
    async getInventoryHealth() {
        try {
            const response = await api.get(ANALYTICS_ENDPOINTS.INVENTORY_HEALTH);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get Financial Overview Metrics
     * @param {Object} params - { range, startDate, endDate }
     * @returns {Promise<Object>} FinancialOverviewMetrics
     */
    async getFinancialOverview(params = {}) {
        try {
            const queryParams = {};

            if (params.range) {
                queryParams.range = params.range;
            }

            if (params.startDate) {
                queryParams.startDate = params.startDate;
            }

            if (params.endDate) {
                queryParams.endDate = params.endDate;
            }

            const response = await api.get(ANALYTICS_ENDPOINTS.FINANCIAL_OVERVIEW, {
                params: queryParams,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get Order Summary Metrics
     * @returns {Promise<Object>} OrderSummaryMetrics
     */
    async getOrderSummary() {
        try {
            const response = await api.get(ANALYTICS_ENDPOINTS.ORDER_SUMMARY);
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
                status: null,
                success: false,
            };
        }
    }
}

export default new AnalyticsService();