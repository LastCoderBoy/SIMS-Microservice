import api from '../../api/axios.js';

const SUPPLIER_ENDPOINTS = {
    GET_ALL: '/suppliers',
    GET_BY_ID: (id) => `/suppliers/${id}`,
};

class SupplierService {
    /**
     * Get All Suppliers
     * @returns {Promise<Array>} List<SupplierResponse>
     */
    async getAllSuppliers() {
        try {
            const response = await api.get(SUPPLIER_ENDPOINTS.GET_ALL);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Get Supplier By ID
     * @param {number} id - Supplier ID
     * @returns {Promise<Object>} SupplierResponse
     */
    async getSupplierById(id) {
        try {
            const response = await api.get(SUPPLIER_ENDPOINTS.GET_BY_ID(id));
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
                message: 'No response from server.  Please check your connection.',
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

export default new SupplierService();