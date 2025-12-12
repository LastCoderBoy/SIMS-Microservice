import api from '../api/axios';

const DAMAGE_LOSS_ENDPOINTS = {
    DASHBOARD: '/inventory/damage-loss',
    ADD:  '/inventory/damage-loss',
    UPDATE: (id) => `/inventory/damage-loss/${id}`,
    DELETE: (id) => `/inventory/damage-loss/${id}`,
    SEARCH: '/inventory/damage-loss/search',
    FILTER: '/inventory/damage-loss/filter',
    REPORT: '/inventory/damage-loss/report',
};

class DamageLossService {
    /**
     * Get Damage Loss Dashboard Data
     * @param {number} page - Page number (optional)
     * @param {number} size - Page size (optional)
     * @returns {Promise<Object>} DamageLossDashboardResponse
     */
    async getDashboardData(page, size) {
        try {
            const params = {};
            if (page !== undefined) params.page = page;
            if (size !== undefined) params.size = size;

            const response = await api.get(DAMAGE_LOSS_ENDPOINTS.DASHBOARD, {
                params,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Add Damage/Loss Report
     * @param {Object} reportData - { sku, quantityLost, reason, lossDate?  }
     * @returns {Promise<Object>} ApiResponse
     */
    async addReport(reportData) {
        try {
            const response = await api.post(DAMAGE_LOSS_ENDPOINTS.ADD, reportData);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Update Damage/Loss Report
     * @param {number} id - Report ID
     * @param {Object} reportData - { sku, quantityLost, reason, lossDate? }
     * @returns {Promise<Object>} ApiResponse
     */
    async updateReport(id, reportData) {
        try {
            const response = await api.put(DAMAGE_LOSS_ENDPOINTS.UPDATE(id), reportData);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Delete Damage/Loss Report
     * @param {number} id - Report ID
     * @returns {Promise<Object>} ApiResponse
     */
    async deleteReport(id) {
        try {
            const response = await api.delete(DAMAGE_LOSS_ENDPOINTS.DELETE(id));
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Search Damage/Loss Reports
     * @param {string} text - Search text
     * @param {number} page - Page number (optional)
     * @param {number} size - Page size (optional)
     * @returns {Promise<Object>} PaginatedResponse<DamageLossResponse>
     */
    async searchReports(text, page, size) {
        try {
            const params = {};
            if (text) params.text = text;
            if (page !== undefined) params.page = page;
            if (size !== undefined) params.size = size;

            const response = await api.get(DAMAGE_LOSS_ENDPOINTS.SEARCH, {
                params,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Filter Damage/Loss Reports by Reason
     * @param {Object} filters - { reason, sortBy, sortDirection, page, size }
     * @returns {Promise<Object>} PaginatedResponse<DamageLossResponse>
     */
    async filterReports(filters) {
        try {
            const params = {};

            Object.keys(filters).forEach(key => {
                if (filters[key] !== undefined && filters[key] !== null && filters[key] !== '') {
                    params[key] = filters[key];
                }
            });

            const response = await api.get(DAMAGE_LOSS_ENDPOINTS.FILTER, {
                params,
            });
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Download Excel Report
     * @param {string} sortBy - Sort field (optional)
     * @param {string} sortDirection - Sort direction (optional)
     */
    async downloadReport(sortBy, sortDirection) {
        try {
            const params = {};
            if (sortBy) params.sortBy = sortBy;
            if (sortDirection) params.sortDirection = sortDirection;

            const response = await api.get(DAMAGE_LOSS_ENDPOINTS.REPORT, {
                params,
                responseType: 'blob', // Important for file download
            });

            // Create download link
            const url = window.URL.createObjectURL(new Blob([response.data]));
            const link = document.createElement('a');
            link.href = url;
            link.setAttribute('download', 'DamageLossReport.xlsx');
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

export default new DamageLossService();