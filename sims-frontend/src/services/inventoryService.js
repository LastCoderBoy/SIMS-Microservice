import api from '../api/axios';

const INVENTORY_ENDPOINTS = {
    DASHBOARD: '/inventory',
    SEARCH_PENDING: '/inventory/pending-orders/search',
    FILTER_PENDING: '/inventory/pending-orders/filter',
};

class InventoryService {
    /**
     * Get Inventory Dashboard Data
     * @param {number} page - Page number (default: 0)
     * @param {number} size - Page size (default: 10)
     * @returns {Promise<Object>} InventoryPageResponse
     */
    async getDashboard(page = 0, size = 10) {
        try {
            const response = await api.get(INVENTORY_ENDPOINTS.DASHBOARD, {
                params: { page, size },
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Search Pending Orders
     * @param {string} text - Search text
     * @param {number} page - Page number
     * @param {number} size - Page size
     * @returns {Promise<Object>} PaginatedResponse<PendingOrderResponse>
     */
    async searchPendingOrders(text, page = 0, size = 10) {
        try {
            const response = await api.get(INVENTORY_ENDPOINTS.SEARCH_PENDING, {
                params: { text, page, size },
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Filter Pending Orders
     * @param {Object} filters - Filter parameters
     * @returns {Promise<Object>} PaginatedResponse<PendingOrderResponse>
     */
    async filterPendingOrders(filters) {
        try {
            // Build query params from filters object
            const params = {};

            // Only add parameters that have values
            Object.keys(filters).forEach(key => {
                if (filters[key] !== undefined && filters[key] !== null && filters[key] !== '') {
                    params[key] = filters[key];
                }
            });

            console.log('[INVENTORY-SERVICE] Filter params:', params); // Debug log

            const response = await api.get(INVENTORY_ENDPOINTS.FILTER_PENDING, {
                params,
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

export default new InventoryService();