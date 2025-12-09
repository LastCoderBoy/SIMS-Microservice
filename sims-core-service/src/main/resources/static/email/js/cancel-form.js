/**
 * Cancel Purchase Order Form Logic
 */

// Get token from URL
const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get('token');

// Check if token exists
if (!token) {
    alert('Invalid confirmation link: No token provided');
    window.close();
}

// Load PO details from backend
fetchPODetails(token);

/**
 * Fetch PO details by token
 */
async function fetchPODetails(token) {
    try {
        const response = await fetch(`/api/v1/email/purchase-order/details?token=${token}`);

        if (! response.ok) {
            throw new Error('Failed to load order details');
        }

        const data = await response.json();

        // Update UI with actual data
        document. getElementById('poNumber').textContent = data.poNumber;
        document.getElementById('productName').textContent = data.productName;
        document.getElementById('quantity').textContent = data.orderedQuantity;

    } catch (error) {
        console.error('Error loading PO details:', error);
        document.getElementById('poNumber').textContent = 'Error loading details';
        document.getElementById('productName').textContent = 'Error loading details';
        document. getElementById('quantity').textContent = 'Error loading details';
    }
}

// Handle cancel button click
document.getElementById('cancelBtn').addEventListener('click', async function() {
    if (!confirm('Are you absolutely sure you want to cancel this order? ')) {
        return;
    }

    // Show loader, hide buttons
    document.getElementById('loader'). classList.add('active');
    document.querySelector('.button-group'). style.display = 'none';

    try {
        const response = await fetch(`/api/v1/email/purchase-order/cancel?token=${token}`, {
            method: 'POST'
        });

        const result = await response.json();

        // Hide loader
        document.getElementById('loader').classList.remove('active');

        // Show result
        showResult(result.message, result.success ? 'success' : 'danger');

    } catch (error) {
        document.getElementById('loader').classList. remove('active');
        showResult('Error cancelling order: ' + error.message, 'danger');
    }
});

function showResult(message, type) {
    const resultDiv = document.getElementById('result');
    const alertDiv = document.getElementById('alert');
    const messageP = document.getElementById('message');

    alertDiv.className = 'alert alert-' + type;
    messageP.textContent = message;
    resultDiv.style. display = 'block';
}