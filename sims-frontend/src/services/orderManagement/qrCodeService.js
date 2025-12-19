import api from '../../api/axios.js';

const QR_CODE_ENDPOINTS = {
    VIEW_QR:  (salesOrderId) => `/sales-orders/qrcode/${salesOrderId}/view`,
    VERIFY_QR: (qrToken) => `/sales-orders/qrcode/${qrToken}/verify`,
    UPDATE_STATUS: (qrToken) => `/sales-orders/qrcode/${qrToken}`,
};

class QrCodeService {
    /**
     * Get presigned URL for QR code image
     * @param {number} salesOrderId - Sales Order ID
     * @returns {Promise<Object>} QrCodeUrlResponse
     */
    async getQrCodeUrl(salesOrderId) {
        try {
            const response = await api.get(QR_CODE_ENDPOINTS.VIEW_QR(salesOrderId));
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Verify QR code by scanning
     * @param {string} qrToken - QR Token
     * @param {string} userId - User ID (optional, defaults to 'GUEST')
     * @returns {Promise<Object>} DetailedSalesOrderView
     */
    async verifyQrCode(qrToken, userId = 'GUEST') {
        try {
            const config = {};
            if (userId && userId !== 'GUEST') {
                config.headers = {
                    'X-User-ID': userId,
                };
            }

            const response = await api.get(QR_CODE_ENDPOINTS.VERIFY_QR(qrToken), config);
            return response.data;
        } catch (error) {
            throw this.handleError(error);
        }
    }

    /**
     * Update order status via QR code
     * @param {string} qrToken - QR Token
     * @param {string} status - New SalesOrderStatus
     * @param {string} userId - User ID
     * @returns {Promise<Object>} ApiResponse
     */
    async updateOrderStatus(qrToken, status, userId) {
        try {
            const response = await api.patch(
                QR_CODE_ENDPOINTS.UPDATE_STATUS(qrToken),
                null,
                {
                    params: { status },
                    headers: {
                        'X-User-ID': userId,
                    },
                }
            );
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

export default new QrCodeService();