/**
 * Confirm Purchase Order Form Logic
 */

// Get token from URL
const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get('token');

// Check if token exists
if (!token) {
    alert('Invalid confirmation link: No token provided');
    window.close();
}

// Set minimum date to today
const today = new Date().toISOString().split('T')[0];
const dateInput = document.getElementById('expectedArrivalDate');
dateInput. setAttribute('min', today);
dateInput.value = today;

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
        document.getElementById('poNumber').textContent = data.poNumber;
        document.getElementById('productName').textContent = data.productName;
        document.getElementById('quantity').textContent = data.orderedQuantity;

    } catch (error) {
        console.error('Error loading PO details:', error);
        document.getElementById('poNumber').textContent = 'Error loading details';
        document.getElementById('productName').textContent = 'Error loading details';
        document.getElementById('quantity').textContent = 'Error loading details';
    }
}

// Handle form submission
document.getElementById('confirmForm').addEventListener('submit', async function(e) {
    e. preventDefault();

    const expectedArrivalDate = dateInput.value;

    if (!expectedArrivalDate) {
        alert('Please select an expected arrival date');
        return;
    }

    // Show loader, hide form
    document.getElementById('loader').classList.add('active');
    document.querySelector('.button-group').style.display = 'none';
    document.getElementById('confirmForm'). style.display = 'none';

    try {
        const response = await fetch(`/api/v1/email/purchase-order/confirm?token=${token}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                expectedArrivalDate: expectedArrivalDate
            })
        });

        const result = await response.json();

        // Hide loader
        document.getElementById('loader').classList.remove('active');

        // Show result
        showResult(result. message, result.success ?  'success' : 'danger');

    } catch (error) {
        document.getElementById('loader').classList.remove('active');
        showResult('Error confirming order: ' + error.message, 'danger');
    }
});

function showResult(message, type) {
    const resultDiv = document.getElementById('result');
    const alertDiv = document.getElementById('alert');
    const messageP = document.getElementById('message');

    alertDiv.className = 'alert alert-' + type;
    messageP.textContent = message;
    resultDiv.style.display = 'block';
}