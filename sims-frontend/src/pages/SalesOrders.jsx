import { useState, useEffect } from 'react';
import salesOrderService from '../services/salesOrderService';
import productService from '../services/productService';
import qrCodeService from '../services/qrCodeService';
import authService from '../services/authService';
import Toast from '../components/common/Toast';
import './SalesOrders.css';

const SalesOrders = () => {
    const currentUser = authService.getCurrentUser();
    const isAdminOrManager = currentUser?.role === 'ROLE_ADMIN' || currentUser?.role === 'ROLE_MANAGER';

    const [metrics, setMetrics] = useState(null);
    const [orders, setOrders] = useState(null);
    const [isLoadingMetrics, setIsLoadingMetrics] = useState(true);
    const [isLoadingOrders, setIsLoadingOrders] = useState(true);
    const [error, setError] = useState(null);

    // View mode:   'all' or 'urgent'
    const [viewMode, setViewMode] = useState('all');

    // Pagination
    const [currentPage, setCurrentPage] = useState(0);

    // Search
    const [searchText, setSearchText] = useState('');
    const [searchQuery, setSearchQuery] = useState('');

    // Filter
    const [showFilters, setShowFilters] = useState(false);
    const [filterValue, setFilterValue] = useState('');
    const [activeFilter, setActiveFilter] = useState({ type: '', value: '' });

    // Modals
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [showManageItemsModal, setShowManageItemsModal] = useState(false);
    const [showDetailsModal, setShowDetailsModal] = useState(false);
    const [showQrModal, setShowQrModal] = useState(false);
    const [showRemoveItemModal, setShowRemoveItemModal] = useState(false);
    const [selectedOrder, setSelectedOrder] = useState(null);
    const [orderDetails, setOrderDetails] = useState(null);
    const [isLoadingDetails, setIsLoadingDetails] = useState(false);
    const [selectedItemToRemove, setSelectedItemToRemove] = useState(null);

    // Products data
    const [products, setProducts] = useState([]);
    const [isLoadingProducts, setIsLoadingProducts] = useState(false);

    // QR Code data
    const [qrCodeData, setQrCodeData] = useState(null);
    const [isLoadingQr, setIsLoadingQr] = useState(false);
    const [qrError, setQrError] = useState(null);

    // Toast notification
    const [toast, setToast] = useState(null);

    // Form data for create/edit
    const [formData, setFormData] = useState({
        customerName: '',
        destination: '',
        orderItems: [{ productId: '', quantity: '' }],
    });
    const [formErrors, setFormErrors] = useState({});

    // Form data for manage items (add items only)
    const [manageItemsData, setManageItemsData] = useState({
        orderItems: [{ productId: '', quantity: '' }],
    });
    const [manageItemsErrors, setManageItemsErrors] = useState({});

    // Sales Order Statuses
    const orderStatuses = [
        { value: 'PENDING', label:   'Pending' },
        { value: 'PARTIALLY_APPROVED', label: 'Partially Approved' },
        { value:  'PARTIALLY_DELIVERED', label: 'Partially Delivered' },
        { value: 'APPROVED', label: 'Approved' },
        { value: 'DELIVERY_IN_PROCESS', label:  'Delivery In Process' },
        { value: 'DELIVERED', label: 'Delivered' },
        { value: 'COMPLETED', label: 'Completed' },
        { value: 'CANCELLED', label: 'Cancelled' },
    ];

    // Status colors
    const statusColors = {
        PENDING: '#FF9800',
        PARTIALLY_APPROVED: '#9C27B0',
        PARTIALLY_DELIVERED: '#673AB7',
        APPROVED:   '#4CAF50',
        DELIVERY_IN_PROCESS: '#2196F3',
        DELIVERED: '#00BCD4',
        CANCELLED: '#F44336',
        COMPLETED: '#4CAF50',
    };

    // Category colors
    const categoryColors = {
        EDUCATION: '#4CAF50',
        ELECTRONIC: '#2196F3',
        ACTION_FIGURES: '#FF5722',
        DOLLS: '#E91E63',
        MUSICAL_TOY: '#9C27B0',
        OUTDOOR_TOY: '#FF9800',
    };

    // Show toast notification
    const showToast = (message, type = 'success') => {
        setToast({ message, type });
    };

    // Close toast
    const closeToast = () => {
        setToast(null);
    };

    // Fetch metrics
    const fetchMetrics = async () => {
        setIsLoadingMetrics(true);
        try {
            const data = await salesOrderService.getMetrics();
            setMetrics(data.data);
        } catch (err) {
            console.error('Error fetching metrics:', err);
        } finally {
            setIsLoadingMetrics(false);
        }
    };

    // Fetch all orders
    const fetchAllOrders = async () => {
        setIsLoadingOrders(true);
        setError(null);

        try {
            const params = {};
            if (currentPage > 0) params.page = currentPage;

            const data = await salesOrderService.getAllSalesOrders(params);
            setOrders(data);
        } catch (err) {
            console.error('Error fetching orders:', err);
            setError(err.message || 'Failed to load orders');
        } finally {
            setIsLoadingOrders(false);
        }
    };

    // Fetch urgent orders (filter on frontend)
    const fetchUrgentOrders = async () => {
        setIsLoadingOrders(true);
        setError(null);

        try {
            const params = {};
            if (currentPage > 0) params.page = currentPage;

            const data = await salesOrderService.getAllSalesOrders(params);

            // Filter urgent orders (delivery within 2 days)
            const today = new Date();
            today.setHours(0, 0, 0, 0);

            const urgentContent = data.content.filter(order => {
                if (! order.estimatedDeliveryDate) return false;
                const eta = new Date(order.estimatedDeliveryDate);
                eta.setHours(0, 0, 0, 0);
                const diffTime = eta - today;
                const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
                return diffDays <= 2 &&
                    diffDays >= 0 &&
                    order.status !== 'DELIVERED' &&
                    order.status !== 'COMPLETED' &&
                    order.status !== 'CANCELLED';
            });

            setOrders({
                ...data,
                content: urgentContent,
                totalElements: urgentContent.length,
            });
        } catch (err) {
            console.error('Error fetching urgent orders:', err);
            setError(err.message || 'Failed to load urgent orders');
        } finally {
            setIsLoadingOrders(false);
        }
    };

    // Search orders
    const searchOrders = async () => {
        setIsLoadingOrders(true);
        setError(null);

        try {
            const params = { text: searchQuery };
            if (currentPage > 0) params.page = currentPage;

            const data = await salesOrderService.searchOrders(params);
            setOrders(data);
        } catch (err) {
            console.error('Error searching orders:', err);
            setError(err.message || 'Failed to search orders');
        } finally {
            setIsLoadingOrders(false);
        }
    };

    // Filter orders
    const filterOrders = async () => {
        setIsLoadingOrders(true);
        setError(null);

        try {
            const params = { status: activeFilter.value };
            if (currentPage > 0) params.page = currentPage;

            const data = await salesOrderService.filterOrders(params);
            setOrders(data);
        } catch (err) {
            console.error('Error filtering orders:', err);
            setError(err.message || 'Failed to filter orders');
        } finally {
            setIsLoadingOrders(false);
        }
    };

    // Initial load
    useEffect(() => {
        fetchMetrics();
    }, []);

    // Fetch orders based on view mode, search, filter, pagination
    useEffect(() => {
        if (searchQuery) {
            searchOrders();
        } else if (activeFilter.value) {
            filterOrders();
        } else if (viewMode === 'urgent') {
            fetchUrgentOrders();
        } else {
            fetchAllOrders();
        }
    }, [currentPage, searchQuery, activeFilter, viewMode]);

    // Handle search
    const handleSearch = (e) => {
        e.preventDefault();
        if (searchText.trim()) {
            setCurrentPage(0);
            setActiveFilter({ type: '', value: '' });
            setFilterValue('');
            setViewMode('all');
            setSearchQuery(searchText);
        } else {
            handleClearSearch();
        }
    };

    // Clear search
    const handleClearSearch = () => {
        setSearchText('');
        setCurrentPage(0);
        setSearchQuery('');
    };

    // Apply filter
    const handleApplyFilter = () => {
        if (! filterValue) {
            showToast('Please select a status', 'warning');
            return;
        }

        setCurrentPage(0);
        setSearchQuery('');
        setSearchText('');
        setViewMode('all');
        setShowFilters(false);
        setActiveFilter({ type: 'status', value: filterValue });
    };

    // Clear filter
    const handleClearFilter = () => {
        setFilterValue('');
        setCurrentPage(0);
        setShowFilters(false);
        setActiveFilter({ type: '', value:  '' });
    };

    // Toggle view mode
    const handleToggleViewMode = (mode) => {
        setViewMode(mode);
        setCurrentPage(0);
        setSearchQuery('');
        setSearchText('');
        setActiveFilter({ type: '', value: '' });
        setFilterValue('');
    };

    // Handle page change
    const handlePageChange = (newPage) => {
        setCurrentPage(newPage);
    };

    // Check if order is urgent
    const isUrgent = (estimatedDate) => {
        if (!estimatedDate) return false;
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const eta = new Date(estimatedDate);
        eta.setHours(0, 0, 0, 0);
        const diffTime = eta - today;
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays <= 2 && diffDays >= 0;
    };

    // Get status color
    const getStatusColor = (status) => {
        return statusColors[status] || '#757575';
    };

    // Get category color
    const getCategoryColor = (category) => {
        return categoryColors[category] || '#757575';
    };

    // Format date
    const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
        });
    };

    // Calculate total amount for order items
    const calculateTotalAmount = (items) => {
        return items.reduce((total, item) => {
            if (! item.productId || !item.quantity) return total;
            const product = products.find(p => p.productId === item.productId);
            if (!product) return total;
            return total + (product.price * parseInt(item.quantity || 0));
        }, 0);
    };

    // Generate page numbers
    const generatePageNumbers = () => {
        const totalPages = orders?.totalPages || 0;
        const pages = [];
        const maxPagesToShow = 5;

        let startPage = Math.max(0, currentPage - Math.floor(maxPagesToShow / 2));
        let endPage = Math.min(totalPages - 1, startPage + maxPagesToShow - 1);

        if (endPage - startPage < maxPagesToShow - 1) {
            startPage = Math.max(0, endPage - maxPagesToShow + 1);
        }

        for (let i = startPage; i <= endPage; i++) {
            pages.push(i);
        }

        return pages;
    };

    // Open Create Modal
    const handleOpenCreateModal = async () => {
        setShowCreateModal(true);
        setFormData({
            customerName: '',
            destination: '',
            orderItems: [{ productId: '', quantity: '' }],
        });
        setFormErrors({});

        // Fetch products
        setIsLoadingProducts(true);
        try {
            const productsData = await productService.getAllProductsList();
            setProducts(productsData || []);
        } catch (err) {
            console.error('Error fetching products:', err);
            setFormErrors({ submit: 'Failed to load products' });
        } finally {
            setIsLoadingProducts(false);
        }
    };

    // Close Create Modal
    const handleCloseCreateModal = () => {
        setShowCreateModal(false);
        setFormData({
            customerName: '',
            destination: '',
            orderItems: [{ productId: '', quantity: '' }],
        });
        setFormErrors({});
        setProducts([]);
    };

    // Open Edit Modal
    const handleOpenEditModal = async (order) => {
        setSelectedOrder(order);
        setShowEditModal(true);
        setIsLoadingDetails(true);
        setFormErrors({});

        try {
            const [detailsResponse, productsData] = await Promise.all([
                salesOrderService.getOrderDetails(order.id),
                productService.getAllProductsList(),
            ]);

            const details = detailsResponse.data;
            setOrderDetails(details);
            setProducts(productsData || []);

            // Pre-fill form with existing data
            setFormData({
                customerName: details.customerName,
                destination: details.destination,
                orderItems: details.items.map(item => ({
                    productId: item.productId,
                    quantity: item.quantity.toString(),
                })),
            });
        } catch (err) {
            console.error('Error fetching order details:', err);
            setFormErrors({ submit: err.message || 'Failed to load order details' });
        } finally {
            setIsLoadingDetails(false);
        }
    };

    // Close Edit Modal
    const handleCloseEditModal = () => {
        setShowEditModal(false);
        setFormData({
            customerName: '',
            destination: '',
            orderItems: [{ productId: '', quantity: '' }],
        });
        setFormErrors({});
        setOrderDetails(null);
        setSelectedOrder(null);
        setProducts([]);
    };

    // Open Manage Items Modal
    const handleOpenManageItemsModal = async (order) => {
        setSelectedOrder(order);
        setShowManageItemsModal(true);
        setIsLoadingDetails(true);
        setManageItemsErrors({});

        try {
            const [detailsResponse, productsData] = await Promise.all([
                salesOrderService.getOrderDetails(order.id),
                productService.getAllProductsList(),
            ]);

            const details = detailsResponse.data;
            setOrderDetails(details);
            setProducts(productsData || []);

            // Initialize with one empty item
            setManageItemsData({
                orderItems: [{ productId: '', quantity: '' }],
            });
        } catch (err) {
            console.error('Error fetching order details:', err);
            setManageItemsErrors({ submit:  err.message || 'Failed to load order details' });
        } finally {
            setIsLoadingDetails(false);
        }
    };

    // Close Manage Items Modal
    const handleCloseManageItemsModal = () => {
        setShowManageItemsModal(false);
        setManageItemsData({
            orderItems: [{ productId:  '', quantity: '' }],
        });
        setManageItemsErrors({});
        setOrderDetails(null);
        setSelectedOrder(null);
        setProducts([]);
    };

    // Open Details Modal
    const handleOpenDetailsModal = async (order) => {
        setSelectedOrder(order);
        setShowDetailsModal(true);
        setIsLoadingDetails(true);

        try {
            const details = await salesOrderService.getOrderDetails(order.id);
            setOrderDetails(details.data);
        } catch (err) {
            console.error('Error fetching order details:', err);
            setError(err.message || 'Failed to load order details');
        } finally {
            setIsLoadingDetails(false);
        }
    };

    // Close Details Modal
    const handleCloseDetailsModal = () => {
        setShowDetailsModal(false);
        setOrderDetails(null);
        setSelectedOrder(null);
    };

    // Open QR Code Modal
    const handleOpenQrModal = async (order) => {
        setSelectedOrder(order);
        setShowQrModal(true);
        setIsLoadingQr(true);
        setQrError(null);

        try {
            const response = await qrCodeService.getQrCodeUrl(order.id);
            setQrCodeData(response.data);
        } catch (err) {
            console.error('Error fetching QR code:', err);
            setQrError(err.message || 'Failed to load QR code');
        } finally {
            setIsLoadingQr(false);
        }
    };

    // Close QR Code Modal
    const handleCloseQrModal = () => {
        setShowQrModal(false);
        setQrCodeData(null);
        setQrError(null);
        setSelectedOrder(null);
    };

    // Open Remove Item Confirmation Modal
    const handleOpenRemoveItemModal = (item) => {
        setSelectedItemToRemove(item);
        setShowRemoveItemModal(true);
    };

    // Close Remove Item Modal
    const handleCloseRemoveItemModal = () => {
        setShowRemoveItemModal(false);
        setSelectedItemToRemove(null);
    };

    // Download QR Code
    const handleDownloadQr = () => {
        if (qrCodeData?.qrCodeUrl) {
            const link = document.createElement('a');
            link.href = qrCodeData.qrCodeUrl;
            link.download = `${qrCodeData.orderReference}-QR.png`;
            link.target = '_blank';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }
    };

    // Print QR Code
    const handlePrintQr = () => {
        if (qrCodeData?.qrCodeUrl) {
            const printWindow = window.open('', '_blank');
            printWindow.document.write(`
                <html>
                    <head>
                        <title>Print QR Code - ${qrCodeData.orderReference}</title>
                        <style>
                            body {
                                display: flex;
                                flex-direction: column;
                                align-items: center;
                                justify-content: center;
                                min-height: 100vh;
                                margin: 0;
                                font-family: Arial, sans-serif;
                            }
                            .qr-print-container {
                                text-align: center;
                                padding: 20px;
                            }
                            h2 {
                                margin-bottom: 10px;
                                color: #333;
                            }
                            p {
                                margin: 5px 0;
                                color: #666;
                            }
                            img {
                                max-width:   400px;
                                margin: 20px 0;
                                border: 2px solid #ddd;
                                border-radius: 8px;
                            }
                            @media print {
                                body {
                                    padding: 0;
                                }
                            }
                        </style>
                    </head>
                    <body>
                        <div class="qr-print-container">
                            <h2>Sales Order QR Code</h2>
                            <p><strong>Order Reference:</strong> ${qrCodeData.orderReference}</p>
                            <p><strong>Customer:</strong> ${selectedOrder?.customerName}</p>
                            <img src="${qrCodeData.qrCodeUrl}" alt="QR Code" />
                            <p><small>Scan to verify order details</small></p>
                        </div>
                    </body>
                </html>
            `);
            printWindow.document.close();
            printWindow.focus();
            setTimeout(() => {
                printWindow.print();
            }, 250);
        }
    };

    // Copy QR Token
    const handleCopyToken = () => {
        if (qrCodeData?.qrToken) {
            navigator.clipboard.writeText(qrCodeData.qrToken)
                .then(() => {
                    showToast('QR Token copied to clipboard! ', 'success');
                })
                .catch((err) => {
                    console.error('Failed to copy:', err);
                    showToast('Failed to copy token', 'error');
                });
        }
    };

    // Add item to form
    const handleAddItem = () => {
        setFormData(prev => ({
            ...prev,
            orderItems: [...prev.orderItems, { productId: '', quantity: '' }]
        }));
    };

    // Remove item from form
    const handleRemoveItemFromForm = (index) => {
        setFormData(prev => ({
            ...prev,
            orderItems: prev.orderItems.filter((_, i) => i !== index)
        }));
    };

    // Add item to manage items form
    const handleAddItemToManage = () => {
        setManageItemsData(prev => ({
            ...prev,
            orderItems: [...prev.orderItems, { productId: '', quantity:  '' }]
        }));
    };

    // Remove item from manage items form
    const handleRemoveItemFromManage = (index) => {
        setManageItemsData(prev => ({
            ...prev,
            orderItems: prev.orderItems.filter((_, i) => i !== index)
        }));
    };

    // Handle form input change
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        if (formErrors[name]) {
            setFormErrors(prev => ({
                ...prev,
                [name]:  ''
            }));
        }
    };

    // Handle order item change
    const handleOrderItemChange = (index, field, value) => {
        setFormData(prev => {
            const newItems = [...prev.orderItems];
            newItems[index][field] = value;
            return {
                ...prev,
                orderItems: newItems
            };
        });

        if (formErrors[`item_${index}_${field}`]) {
            setFormErrors(prev => ({
                ...prev,
                [`item_${index}_${field}`]: ''
            }));
        }
    };

    // Handle manage items change
    const handleManageItemChange = (index, field, value) => {
        setManageItemsData(prev => {
            const newItems = [...prev.orderItems];
            newItems[index][field] = value;
            return {
                ...prev,
                orderItems: newItems
            };
        });

        if (manageItemsErrors[`item_${index}_${field}`]) {
            setManageItemsErrors(prev => ({
                ...prev,
                [`item_${index}_${field}`]: ''
            }));
        }
    };

    // Validate create/edit form
    const validateForm = () => {
        const errors = {};

        if (!formData.customerName.trim()) {
            errors.customerName = 'Customer name is required';
        }

        if (!formData.destination.trim()) {
            errors.destination = 'Destination is required';
        }

        if (formData.orderItems.length === 0) {
            errors.submit = 'At least one item is required';
        }

        // Check for duplicate products
        const productIds = formData.orderItems.map(item => item.productId).filter(Boolean);
        const duplicates = productIds.filter((item, index) => productIds.indexOf(item) !== index);
        if (duplicates.length > 0) {
            errors.submit = 'Cannot add the same product multiple times. Please increase quantity instead.';
        }

        // Validate each item
        formData.orderItems.forEach((item, index) => {
            if (!item.productId) {
                errors[`item_${index}_productId`] = 'Product is required';
            }

            if (!item.quantity || item.quantity < 1) {
                errors[`item_${index}_quantity`] = 'Quantity must be at least 1';
            }
        });

        setFormErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // Validate manage items form
    const validateManageItemsForm = () => {
        const errors = {};

        if (manageItemsData.orderItems.length === 0) {
            errors.submit = 'At least one item is required';
        }

        // Check for duplicate products
        const productIds = manageItemsData.orderItems.map(item => item.productId).filter(Boolean);
        const duplicates = productIds.filter((item, index) => productIds.indexOf(item) !== index);
        if (duplicates.length > 0) {
            errors.submit = 'Cannot add the same product multiple times.Please increase quantity instead.';
        }

        // Check if product already exists in order
        manageItemsData.orderItems.forEach((item, index) => {
            if (item.productId) {
                const existsInOrder = orderDetails?.items?.some(existingItem =>
                    existingItem.productId === item.productId
                );
                if (existsInOrder) {
                    errors[`item_${index}_productId`] = 'Product already exists in order';
                }
            }
        });

        // Validate each item
        manageItemsData.orderItems.forEach((item, index) => {
            if (!item.productId) {
                errors[`item_${index}_productId`] = 'Product is required';
            }

            if (!item.quantity || item.quantity < 1) {
                errors[`item_${index}_quantity`] = 'Quantity must be at least 1';
            }
        });

        setManageItemsErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // Handle Create Sales Order
    const handleCreateSalesOrder = async (e) => {
        e.preventDefault();

        if (!validateForm()) return;

        try {
            const createData = {
                customerName: formData.customerName.trim(),
                destination: formData.destination.trim(),
                orderItems: formData.orderItems.map(item => ({
                    productId: item.productId,
                    quantity: parseInt(item.quantity),
                })),
            };

            await salesOrderService.createSalesOrder(createData);

            showToast('Sales order created successfully!   🎉', 'success');

            handleCloseCreateModal();
            fetchMetrics();
            if (viewMode === 'urgent') {
                fetchUrgentOrders();
            } else {
                fetchAllOrders();
            }
        } catch (err) {
            console.error('Error creating sales order:', err);
            setFormErrors({ submit: err.message || 'Failed to create sales order' });
            showToast(err.message || 'Failed to create sales order', 'error');
        }
    };

    // Handle Update Sales Order
    const handleUpdateSalesOrder = async (e) => {
        e.preventDefault();

        if (!validateForm()) return;

        try {
            const updateData = {
                customerName: formData.customerName.trim(),
                destination: formData.destination.trim(),
                orderItems: formData.orderItems.map(item => ({
                    productId: item.productId,
                    quantity: parseInt(item.quantity),
                })),
            };

            await salesOrderService.updateSalesOrder(selectedOrder.id, updateData);

            showToast('Sales order updated successfully!  ✅', 'success');

            handleCloseEditModal();
            fetchMetrics();
            if (viewMode === 'urgent') {
                fetchUrgentOrders();
            } else {
                fetchAllOrders();
            }
        } catch (err) {
            console.error('Error updating sales order:', err);
            setFormErrors({ submit: err.message || 'Failed to update sales order' });
            showToast(err.message || 'Failed to update sales order', 'error');
        }
    };

    // Handle Add Items to Order
    const handleAddItemsToOrder = async (e) => {
        e.preventDefault();

        if (!validateManageItemsForm()) return;

        try {
            const addItemsData = {
                orderItems:  manageItemsData.orderItems.map(item => ({
                    productId: item.productId,
                    quantity: parseInt(item.quantity),
                })),
            };

            console.log('Sending request:', addItemsData); // Debug log

            await salesOrderService.addItems(selectedOrder.id, addItemsData);

            showToast('Items added successfully!  ✅', 'success');

            handleCloseManageItemsModal();
            fetchMetrics();
            if (viewMode === 'urgent') {
                fetchUrgentOrders();
            } else {
                fetchAllOrders();
            }
        } catch (err) {
            console.error('Error adding items:', err);
            setManageItemsErrors({ submit: err.message || 'Failed to add items' });
            showToast(err.message || 'Failed to add items', 'error');
        }
    };

    // Handle Remove Item from Order
    const handleRemoveItem = async () => {
        if (!selectedItemToRemove) return;

        try {
            await salesOrderService.removeItem(
                orderDetails.id,
                selectedItemToRemove.id
            );

            showToast('Item removed successfully!  🗑️', 'success');

            handleCloseRemoveItemModal();

            // Refresh order details if manage items modal is open
            if (showManageItemsModal) {
                const detailsResponse = await salesOrderService.getOrderDetails(orderDetails.id);
                setOrderDetails(detailsResponse.data);
            }

            fetchMetrics();
            if (viewMode === 'urgent') {
                fetchUrgentOrders();
            } else {
                fetchAllOrders();
            }
        } catch (err) {
            console.error('Error removing item:', err);
            showToast(err.message || 'Failed to remove item', 'error');
        }
    };

    // Loading and error states
    if (isLoadingMetrics || (isLoadingOrders && !orders)) {
        return (
            <div className="loading-container">
                <div className="spinner-large"></div>
                <p>Loading sales orders...</p>
            </div>
        );
    }

    if (error && !orders) {
        return (
            <div className="error-container">
                <svg className="error-icon-large" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                    <path d="M12 8V12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                </svg>
                <h2>Error Loading Data</h2>
                <p>{error}</p>
                <button className="retry-btn" onClick={fetchAllOrders}>
                    Try Again
                </button>
            </div>
        );
    }

    return (
        <div className="sales-orders-page">
            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">Sales Orders</h1>
                    <p className="page-subtitle">Manage and track all sales orders</p>
                </div>
            </div>

            {/* Metrics Cards */}
            <div className="metrics-grid-so">
                <div className="metric-card-so metric-total">
                    <div className="metric-icon-wrapper-so">
                        <svg className="metric-icon-so" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 2H15M9 2V6M15 2V6M9 2C7.89543 2 7 2.89543 7 4V6H17V4C17 2.89543 16.1046 2 15 2Z" stroke="currentColor" strokeWidth="2"/>
                            <rect x="3" y="8" width="18" height="13" rx="2" stroke="currentColor" strokeWidth="2"/>
                        </svg>
                    </div>
                    <div className="metric-content-so">
                        <p className="metric-label-so">Total Orders</p>
                        <h2 className="metric-value-so">{metrics?.totalOrders || 0}</h2>
                        <p className="metric-sublabel-so">All sales orders</p>
                    </div>
                </div>

                <div className="metric-card-so metric-pending">
                    <div className="metric-icon-wrapper-so">
                        <svg className="metric-icon-so" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                            <path d="M12 6V12L16 14" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-so">
                        <p className="metric-label-so">Pending</p>
                        <h2 className="metric-value-so">{metrics?.totalPending || 0}</h2>
                        <p className="metric-sublabel-so">Awaiting confirmation</p>
                    </div>
                </div>

                <div className="metric-card-so metric-progress">
                    <div className="metric-icon-wrapper-so">
                        <svg className="metric-icon-so" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="1" y="3" width="15" height="13" rx="2" stroke="currentColor" strokeWidth="2"/>
                            <path d="M16 8H20L23 11V16H16V8Z" stroke="currentColor" strokeWidth="2" strokeLinejoin="round"/>
                            <circle cx="5.5" cy="18.5" r="2.5" stroke="currentColor" strokeWidth="2"/>
                            <circle cx="18.5" cy="18.5" r="2.5" stroke="currentColor" strokeWidth="2"/>
                        </svg>
                    </div>
                    <div className="metric-content-so">
                        <p className="metric-label-so">In Progress</p>
                        <h2 className="metric-value-so">{metrics?.totalInProgress || 0}</h2>
                        <p className="metric-sublabel-so">Being processed</p>
                    </div>
                </div>

                <div className="metric-card-so metric-delivered">
                    <div className="metric-icon-wrapper-so">
                        <svg className="metric-icon-so" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 11L12 14L22 4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-so">
                        <p className="metric-label-so">Delivered</p>
                        <h2 className="metric-value-so">{metrics?.totalDelivered || 0}</h2>
                        <p className="metric-sublabel-so">Completed</p>
                    </div>
                </div>
            </div>


            {/* Orders Table Section */}
            <div className="orders-section">
                {/* Section Header */}
                <div className="section-header-so">
                    {/* Left:   View Mode Tabs */}
                    <div className="view-mode-tabs-so">
                        <button
                            className={`tab-btn-so ${viewMode === 'all' ? 'active' : ''}`}
                            onClick={() => handleToggleViewMode('all')}
                        >
                            <span className="tab-label">All Sales Orders</span>
                            <span className="tab-count">{viewMode === 'all' ? orders?.totalElements || 0 : metrics?.totalOrders || 0}</span>
                        </button>
                        <button
                            className={`tab-btn-so ${viewMode === 'urgent' ?    'active' : ''}`}
                            onClick={() => handleToggleViewMode('urgent')}
                        >
                            <span className="tab-label">🔥 Urgent Orders</span>
                            <span className="tab-count">{viewMode === 'urgent' ?   orders?.totalElements || 0 :  0}</span>
                        </button>
                    </div>

                    {/* Right: Actions */}
                    <div className="header-actions-so">
                        {isAdminOrManager && (
                            <button className="create-so-btn" onClick={handleOpenCreateModal}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M12 5V19M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                                Create New Order
                            </button>
                        )}

                        {/* Search Bar */}
                        <form className="search-form-so" onSubmit={handleSearch}>
                            <div className="search-input-wrapper-so">
                                <svg className="search-icon-so" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <circle cx="11" cy="11" r="8" stroke="currentColor" strokeWidth="2"/>
                                    <path d="M21 21L16.65 16.65" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                                <input
                                    type="text"
                                    className="search-input-so"
                                    placeholder="Search by order ref, customer..."
                                    value={searchText}
                                    onChange={(e) => setSearchText(e.target.value)}
                                />
                                {searchText && (
                                    <button type="button" className="clear-search-btn-so" onClick={handleClearSearch}>
                                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                            <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                        </svg>
                                    </button>
                                )}
                            </div>
                            <button type="submit" className="search-btn-so">
                                Search
                            </button>
                        </form>

                        {/* Filter Button */}
                        <button
                            className={`filter-btn-so ${activeFilter.value ?    'active' : ''}`}
                            onClick={() => setShowFilters(!showFilters)}
                        >
                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M22 3H2L10 12.46V19L14 21V12.46L22 3Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                            Filter
                            {activeFilter.value && <span className="filter-count-so">1</span>}
                        </button>
                    </div>
                </div>

                {/* Filter Panel */}
                {showFilters && (
                    <div className="filter-panel-so">
                        <div className="filter-grid-single-so">
                            <div className="filter-group-so">
                                <label className="filter-label-so">Filter By Status</label>
                                <select
                                    className="filter-select-so"
                                    value={filterValue}
                                    onChange={(e) => setFilterValue(e.target.value)}
                                >
                                    <option value="">Select status</option>
                                    {orderStatuses.map(status => (
                                        <option key={status.value} value={status.value}>
                                            {status.label}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <div className="filter-actions-so">
                            <button className="clear-filters-btn-so" onClick={handleClearFilter}>
                                Clear
                            </button>
                            <button className="apply-filters-btn-so" onClick={handleApplyFilter}>
                                Apply Filter
                            </button>
                        </div>
                    </div>
                )}

                {/* Loading indicator */}
                {isLoadingOrders && orders && (
                    <div className="table-loading">
                        <div className="spinner-small"></div>
                        <span>Loading...</span>
                    </div>
                )}

                {/* Orders Table */}
                {!  isLoadingOrders && orders?.content?.length > 0 ?     (
                    <>
                        <div className="table-container">
                            <table className="orders-table">
                                <thead>
                                <tr>
                                    <th>Order Ref</th>
                                    <th>Customer</th>
                                    <th>Destination</th>
                                    <th>Total Items</th>
                                    <th>Shipped</th>
                                    <th>Total Amount</th>
                                    <th>Order Date</th>
                                    <th>ETA</th>
                                    <th>Status</th>
                                    <th>QR Code</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {orders.content.map((order) => {
                                    const urgent = isUrgent(order.estimatedDeliveryDate);

                                    return (
                                        <tr key={order.id} className={urgent ? 'row-urgent' : ''}>
                                            <td className="order-ref-cell">{order.orderReference}</td>
                                            <td className="customer-name-so">{order.customerName}</td>
                                            <td>{order.destination}</td>
                                            <td className="items-cell">{order.totalOrderedQuantity}</td>
                                            <td>
                                                <div className="progress-wrapper">
                                                    <div className="progress-bar-so">
                                                        <div
                                                            className="progress-fill-so"
                                                            style={{
                                                                width: `${Math.min(100, ((order.totalApprovedQuantity || 0) / order.totalOrderedQuantity) * 100)}%`,
                                                                backgroundColor: ((order.totalApprovedQuantity || 0) / order.totalOrderedQuantity) * 100 === 100 ? '#4CAF50' : '#2196F3'
                                                            }}
                                                        ></div>
                                                    </div>
                                                    <div className="progress-details">
                                                        <span className="progress-text">
                                                            {order.totalApprovedQuantity || 0}
                                                            {(order.totalApprovedQuantity || 0) < order.totalOrderedQuantity && (
                                                                <span className="pending-hint">
                                                                    &nbsp;({order.totalOrderedQuantity - (order.totalApprovedQuantity || 0)} pending)
                                                                </span>
                                                            )}
                                                        </span>
                                                    </div>
                                                </div>
                                            </td>
                                            <td className="amount-cell">${order.totalAmount?.toFixed(2)}</td>
                                            <td>{formatDate(order.orderDate)}</td>
                                            <td>
                                                <span className={`eta-badge ${urgent ? 'eta-urgent' : ''}`}>
                                                    {urgent && '🔥 '}
                                                    {formatDate(order.estimatedDeliveryDate)}
                                                </span>
                                            </td>
                                            <td>
                                                <span
                                                    className="status-badge-so"
                                                    style={{
                                                        backgroundColor:   `${getStatusColor(order.status)}20`,
                                                        color: getStatusColor(order.status),
                                                        border: `1px solid ${getStatusColor(order.status)}50`
                                                    }}
                                                >
                                                    {order.status.replace(/_/g, ' ')}
                                                </span>
                                            </td>
                                            <td className="qr-cell">
                                                <button
                                                    className="qr-btn"
                                                    onClick={() => handleOpenQrModal(order)}
                                                    title="View/Print QR Code"
                                                >
                                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                        <rect x="3" y="3" width="8" height="8" rx="1" stroke="currentColor" strokeWidth="2"/>
                                                        <rect x="13" y="3" width="8" height="8" rx="1" stroke="currentColor" strokeWidth="2"/>
                                                        <rect x="3" y="13" width="8" height="8" rx="1" stroke="currentColor" strokeWidth="2"/>
                                                        <rect x="16" y="16" width="2" height="2" fill="currentColor"/>
                                                        <rect x="19" y="16" width="2" height="2" fill="currentColor"/>
                                                        <rect x="16" y="19" width="2" height="2" fill="currentColor"/>
                                                        <rect x="19" y="19" width="2" height="2" fill="currentColor"/>
                                                    </svg>
                                                </button>
                                            </td>
                                            <td>
                                                <div className="action-buttons-so">
                                                    <button
                                                        className="action-btn-so view-btn"
                                                        onClick={() => handleOpenDetailsModal(order)}
                                                        title="View Details"
                                                    >
                                                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                            <path d="M1 12C1 12 5 4 12 4C19 4 23 12 23 12C23 12 19 20 12 20C5 20 1 12 1 12Z" stroke="currentColor" strokeWidth="2"/>
                                                            <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="2"/>
                                                        </svg>
                                                    </button>
                                                    {isAdminOrManager && (
                                                        <>
                                                            <button
                                                                className="action-btn-so edit-btn"
                                                                onClick={() => handleOpenEditModal(order)}
                                                                title="Edit Order"
                                                            >
                                                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                    <path d="M11 4H4C3.46957 4 2.96086 4.21071 2.58579 4.58579C2.21071 4.96086 2 5.46957 2 6V20C2 20.5304 2.21071 21.0391 2.58579 21.4142C2.96086 21.7893 3.46957 22 4 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V13" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                    <path d="M18.5 2.50001C18.8978 2.10219 19.4374 1.87869 20 1.87869C20.5626 1.87869 21.1022 2.10219 21.5 2.50001C21.8978 2.89784 22.1213 3.4374 22.1213 4.00001C22.1213 4.56262 21.8978 5.10219 21.5 5.50001L12 15L8 16L9 12L18.5 2.50001Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                </svg>
                                                            </button>
                                                            <button
                                                                className="action-btn-so manage-btn"
                                                                onClick={() => handleOpenManageItemsModal(order)}
                                                                title="Manage Items"
                                                            >
                                                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                    <rect x="3" y="3" width="7" height="7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                    <rect x="14" y="3" width="7" height="7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                    <rect x="14" y="14" width="7" height="7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                    <rect x="3" y="14" width="7" height="7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                </svg>
                                                            </button>
                                                        </>
                                                    )}
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })}
                                </tbody>
                            </table>
                        </div>

                        {/* Pagination */}
                        {orders.totalPages > 1 && (
                            <div className="pagination">
                                <button
                                    className="pagination-btn pagination-arrow"
                                    onClick={() => handlePageChange(currentPage - 1)}
                                    disabled={currentPage === 0}
                                >
                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M15 18L9 12L15 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                </button>

                                {currentPage > 2 && (
                                    <>
                                        <button className="pagination-btn pagination-number" onClick={() => handlePageChange(0)}>
                                            1
                                        </button>
                                        {currentPage > 3 && <span className="pagination-ellipsis">...</span>}
                                    </>
                                )}

                                {generatePageNumbers().map((pageNum) => (
                                    <button
                                        key={pageNum}
                                        className={`pagination-btn pagination-number ${pageNum === currentPage ? 'active' : ''}`}
                                        onClick={() => handlePageChange(pageNum)}
                                    >
                                        {pageNum + 1}
                                    </button>
                                ))}

                                {currentPage < orders.totalPages - 3 && (
                                    <>
                                        {currentPage < orders.totalPages - 4 && (
                                            <span className="pagination-ellipsis">...</span>
                                        )}
                                        <button
                                            className="pagination-btn pagination-number"
                                            onClick={() => handlePageChange(orders.totalPages - 1)}
                                        >
                                            {orders.totalPages}
                                        </button>
                                    </>
                                )}

                                <button
                                    className="pagination-btn pagination-arrow"
                                    onClick={() => handlePageChange(currentPage + 1)}
                                    disabled={currentPage === orders.totalPages - 1}
                                >
                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M9 18L15 12L9 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                </button>

                                <div className="pagination-info">
                                    Page {currentPage + 1} of {orders.totalPages}
                                </div>
                            </div>
                        )}
                    </>
                ) : ! isLoadingOrders && (
                    <div className="empty-state">
                        <svg className="empty-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" strokeWidth="2"/>
                            <path d="M9 9H15M9 13H15M9 17H12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        </svg>
                        <h3>No Orders Found</h3>
                        <p>There are currently no {viewMode === 'urgent' ? 'urgent' : ''} sales orders.</p>
                    </div>
                )}
            </div>

            {/* Create Sales Order Modal */}
            {showCreateModal && (
                <div className="modal-overlay" onClick={handleCloseCreateModal}>
                    <div className="modal-content modal-xlarge" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>📦 Create New Sales Order</h2>
                            <button className="modal-close-btn" onClick={handleCloseCreateModal}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <form onSubmit={handleCreateSalesOrder}>
                            <div className="modal-body">
                                {formErrors.submit && (
                                    <div className="error-alert">
                                        {formErrors.submit}
                                    </div>
                                )}

                                {/* Customer Name */}
                                <div className="form-group">
                                    <label className="form-label">Customer Name *</label>
                                    <input
                                        type="text"
                                        name="customerName"
                                        className={`form-input ${formErrors.customerName ? 'input-error' : ''}`}
                                        placeholder="Enter customer name"
                                        value={formData.customerName}
                                        onChange={handleInputChange}
                                    />
                                    {formErrors.customerName && <span className="error-text">{formErrors.customerName}</span>}
                                </div>

                                {/* Destination */}
                                <div className="form-group">
                                    <label className="form-label">Destination *</label>
                                    <input
                                        type="text"
                                        name="destination"
                                        className={`form-input ${formErrors.destination ? 'input-error' : ''}`}
                                        placeholder="Enter destination address"
                                        value={formData.destination}
                                        onChange={handleInputChange}
                                    />
                                    {formErrors.destination && <span className="error-text">{formErrors.destination}</span>}
                                </div>

                                {/* Order Items */}
                                <div className="form-group">
                                    <label className="form-label">Order Items *</label>
                                    <div className="items-container">
                                        {formData.orderItems.map((item, index) => (
                                            <div key={index} className="item-row">
                                                <div className="item-row-header">
                                                    <span className="item-number">📦 Item {index + 1}</span>
                                                    {formData.orderItems.length > 1 && (
                                                        <button
                                                            type="button"
                                                            className="remove-item-btn"
                                                            onClick={() => handleRemoveItemFromForm(index)}
                                                        >
                                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                                            </svg>
                                                        </button>
                                                    )}
                                                </div>

                                                <div className="item-row-content">
                                                    {/* Product Selection */}
                                                    <div className="item-field">
                                                        <label className="item-label">Product *</label>
                                                        {isLoadingProducts ? (
                                                            <div className="loading-dropdown">
                                                                <div className="spinner-small"></div>
                                                                <span>Loading...</span>
                                                            </div>
                                                        ) : (
                                                            <select
                                                                className={`form-input ${formErrors[`item_${index}_productId`] ? 'input-error' : ''}`}
                                                                value={item.productId}
                                                                onChange={(e) => handleOrderItemChange(index, 'productId', e.target.value)}
                                                            >
                                                                <option value="">Select a product</option>
                                                                {products.map(product => (
                                                                    <option key={product.productId} value={product.productId}>
                                                                        {product.name} ({product.productId}) - ${product.price?.toFixed(2)}
                                                                    </option>
                                                                ))}
                                                            </select>
                                                        )}
                                                        {formErrors[`item_${index}_productId`] && (
                                                            <span className="error-text">{formErrors[`item_${index}_productId`]}</span>
                                                        )}
                                                    </div>

                                                    {/* Quantity */}
                                                    <div className="item-field">
                                                        <label className="item-label">Quantity *</label>
                                                        <input
                                                            type="number"
                                                            min="1"
                                                            className={`form-input ${formErrors[`item_${index}_quantity`] ? 'input-error' : ''}`}
                                                            placeholder="Qty"
                                                            value={item.quantity}
                                                            onChange={(e) => handleOrderItemChange(index, 'quantity', e.target.value)}
                                                        />
                                                        {formErrors[`item_${index}_quantity`] && (
                                                            <span className="error-text">{formErrors[`item_${index}_quantity`]}</span>
                                                        )}
                                                    </div>

                                                    {/* Item Total */}
                                                    {item.productId && item.quantity && (
                                                        <div className="item-total">
                                                            <span className="item-total-label">Item Total:</span>
                                                            <span className="item-total-value">
                                                                ${(products.find(p => p.productId === item.productId)?.price * parseInt(item.quantity || 0)).toFixed(2)}
                                                            </span>
                                                        </div>
                                                    )}
                                                </div>
                                            </div>
                                        ))}

                                        <button
                                            type="button"
                                            className="add-item-btn"
                                            onClick={handleAddItem}
                                        >
                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M12 5V19M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                            </svg>
                                            Add Another Item
                                        </button>
                                    </div>
                                </div>

                                {/* Total Amount */}
                                {formData.orderItems.some(item => item.productId && item.quantity) && (
                                    <div className="total-amount-box">
                                        <span className="total-label">Total Order Amount:</span>
                                        <span className="total-value">${calculateTotalAmount(formData.orderItems).toFixed(2)}</span>
                                    </div>
                                )}
                            </div>

                            <div className="modal-footer">
                                <button type="button" className="btn-secondary" onClick={handleCloseCreateModal}>
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="btn-primary"
                                    disabled={isLoadingProducts}
                                >
                                    Create Sales Order
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Edit Sales Order Modal */}
            {showEditModal && (
                <div className="modal-overlay" onClick={handleCloseEditModal}>
                    <div className="modal-content modal-xlarge" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>✏️ Edit Sales Order</h2>
                            <button className="modal-close-btn" onClick={handleCloseEditModal}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <form onSubmit={handleUpdateSalesOrder}>
                            <div className="modal-body">
                                {formErrors.submit && (
                                    <div className="error-alert">
                                        {formErrors.submit}
                                    </div>
                                )}

                                {isLoadingDetails ? (
                                    <div className="loading-details">
                                        <div className="spinner-small"></div>
                                        <p>Loading order details...</p>
                                    </div>
                                ) : (
                                    <>
                                        {/* Order Reference (Read-only) */}
                                        <div className="form-group">
                                            <label className="form-label">Order Reference</label>
                                            <input
                                                type="text"
                                                className="form-input input-readonly"
                                                value={orderDetails?.orderReference || ''}
                                                readOnly
                                            />
                                        </div>

                                        {/* Customer Name */}
                                        <div className="form-group">
                                            <label className="form-label">Customer Name *</label>
                                            <input
                                                type="text"
                                                name="customerName"
                                                className={`form-input ${formErrors.customerName ? 'input-error' : ''}`}
                                                placeholder="Enter customer name"
                                                value={formData.customerName}
                                                onChange={handleInputChange}
                                            />
                                            {formErrors.customerName && <span className="error-text">{formErrors.customerName}</span>}
                                        </div>

                                        {/* Destination */}
                                        <div className="form-group">
                                            <label className="form-label">Destination *</label>
                                            <input
                                                type="text"
                                                name="destination"
                                                className={`form-input ${formErrors.destination ? 'input-error' : ''}`}
                                                placeholder="Enter destination address"
                                                value={formData.destination}
                                                onChange={handleInputChange}
                                            />
                                            {formErrors.destination && <span className="error-text">{formErrors.destination}</span>}
                                        </div>

                                        {/* Order Items */}
                                        <div className="form-group">
                                            <label className="form-label">Order Items *</label>
                                            <div className="items-container">
                                                {formData.orderItems.map((item, index) => (
                                                    <div key={index} className="item-row">
                                                        <div className="item-row-header">
                                                            <span className="item-number">📦 Item {index + 1}</span>
                                                            {formData.orderItems.length > 1 && (
                                                                <button
                                                                    type="button"
                                                                    className="remove-item-btn"
                                                                    onClick={() => handleRemoveItemFromForm(index)}
                                                                >
                                                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                        <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                                                    </svg>
                                                                </button>
                                                            )}
                                                        </div>

                                                        <div className="item-row-content">
                                                            {/* Product Selection */}
                                                            <div className="item-field">
                                                                <label className="item-label">Product *</label>
                                                                {isLoadingProducts ? (
                                                                    <div className="loading-dropdown">
                                                                        <div className="spinner-small"></div>
                                                                        <span>Loading...</span>
                                                                    </div>
                                                                ) : (
                                                                    <select
                                                                        className={`form-input ${formErrors[`item_${index}_productId`] ? 'input-error' : ''}`}
                                                                        value={item.productId}
                                                                        onChange={(e) => handleOrderItemChange(index, 'productId', e.target.value)}
                                                                    >
                                                                        <option value="">Select a product</option>
                                                                        {products.map(product => (
                                                                            <option key={product.productId} value={product.productId}>
                                                                                {product.name} ({product.productId}) - ${product.price?.toFixed(2)}
                                                                            </option>
                                                                        ))}
                                                                    </select>
                                                                )}
                                                                {formErrors[`item_${index}_productId`] && (
                                                                    <span className="error-text">{formErrors[`item_${index}_productId`]}</span>
                                                                )}
                                                            </div>

                                                            {/* Quantity */}
                                                            <div className="item-field">
                                                                <label className="item-label">Quantity *</label>
                                                                <input
                                                                    type="number"
                                                                    min="1"
                                                                    className={`form-input ${formErrors[`item_${index}_quantity`] ? 'input-error' : ''}`}
                                                                    placeholder="Qty"
                                                                    value={item.quantity}
                                                                    onChange={(e) => handleOrderItemChange(index, 'quantity', e.target.value)}
                                                                />
                                                                {formErrors[`item_${index}_quantity`] && (
                                                                    <span className="error-text">{formErrors[`item_${index}_quantity`]}</span>
                                                                )}
                                                            </div>

                                                            {/* Item Total */}
                                                            {item.productId && item.quantity && (
                                                                <div className="item-total">
                                                                    <span className="item-total-label">Item Total:</span>
                                                                    <span className="item-total-value">
                                                                        ${(products.find(p => p.productId === item.productId)?.price * parseInt(item.quantity || 0)).toFixed(2)}
                                                                    </span>
                                                                </div>
                                                            )}
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>

                                        {/* Total Amount */}
                                        {formData.orderItems.some(item => item.productId && item.quantity) && (
                                            <div className="total-amount-box">
                                                <span className="total-label">Total Order Amount:</span>
                                                <span className="total-value">${calculateTotalAmount(formData.orderItems).toFixed(2)}</span>
                                            </div>
                                        )}
                                    </>
                                )}
                            </div>

                            <div className="modal-footer">
                                <button type="button" className="btn-secondary" onClick={handleCloseEditModal}>
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="btn-primary"
                                    disabled={isLoadingDetails || isLoadingProducts}
                                >
                                    Confirm Update
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* Manage Items Modal (Add/Remove Items) */}
            {showManageItemsModal && (
                <div className="modal-overlay" onClick={handleCloseManageItemsModal}>
                    <div className="modal-content modal-xlarge" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>🔧 Manage Order Items</h2>
                            <button className="modal-close-btn" onClick={handleCloseManageItemsModal}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <div className="modal-body">
                            {isLoadingDetails ? (
                                <div className="loading-details">
                                    <div className="spinner-small"></div>
                                    <p>Loading order details...</p>
                                </div>
                            ) : orderDetails ?  (
                                <>
                                    {/* Order Info */}
                                    <div className="order-details-box">
                                        <h3>Order Details</h3>
                                        <div className="detail-row">
                                            <span className="detail-label">Order Reference:</span>
                                            <span className="detail-value">{orderDetails.orderReference}</span>
                                        </div>
                                        <div className="detail-row">
                                            <span className="detail-label">Customer: </span>
                                            <span className="detail-value">{orderDetails.customerName}</span>
                                        </div>
                                        <div className="detail-row">
                                            <span className="detail-label">Destination:</span>
                                            <span className="detail-value">{orderDetails.destination}</span>
                                        </div>
                                    </div>

                                    {/* Current Items */}
                                    <div className="current-items-section">
                                        <h3>Current Items</h3>
                                        {orderDetails.items && orderDetails.items.length > 0 ? (
                                            <div className="current-items-list">
                                                {orderDetails.items.map((item) => (
                                                    <div key={item.id} className="current-item-card">
                                                        <div className="current-item-info">
                                                            <div className="current-item-details">
                                                                <h4>{item.productName}</h4>
                                                                <span className="current-item-id">ID: {item.productId}</span>
                                                                <span
                                                                    className="item-category"
                                                                    style={{
                                                                        backgroundColor: `${getCategoryColor(item.productCategory)}20`,
                                                                        color: getCategoryColor(item.productCategory),
                                                                    }}
                                                                >
                                                                    {item.productCategory?.replace(/_/g, ' ')}
                                                                </span>
                                                            </div>
                                                            <div className="current-item-quantities">
                                                                <div className="current-item-qty">
                                                                    <span className="qty-label">Quantity:</span>
                                                                    <span className="qty-value">{item.quantity}</span>
                                                                </div>
                                                                <div className="current-item-qty">
                                                                    <span className="qty-label">Shipped:</span>
                                                                    <span className="qty-value shipped">{item.approvedQuantity}</span>
                                                                </div>
                                                                <div className="current-item-qty">
                                                                    <span className="qty-label">Price:</span>
                                                                    <span className="qty-value">${item.unitPrice?.toFixed(2)}</span>
                                                                </div>
                                                            </div>
                                                        </div>
                                                        <button
                                                            type="button"
                                                            className="remove-current-item-btn"
                                                            onClick={() => handleOpenRemoveItemModal(item)}
                                                            title="Remove Item"
                                                        >
                                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                <path d="M3 6H5H21" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                                <path d="M8 6V4C8 3.46957 8.21071 2.96086 8.58579 2.58579C8.96086 2.21071 9.46957 2 10 2H14C14.5304 2 15.0391 2.21071 15.4142 2.58579C15.7893 2.96086 16 3.46957 16 4V6M19 6V20C19 20.5304 18.7893 21.0391 18.4142 21.4142C18.0391 21.7893 17.5304 22 17 22H7C6.46957 22 5.96086 21.7893 5.58579 21.4142C5.21071 21.0391 5 20.5304 5 20V6H19Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                            </svg>
                                                        </button>
                                                    </div>
                                                ))}
                                            </div>
                                        ) : (
                                            <p className="no-items-text">No items in this order</p>
                                        )}
                                    </div>

                                    {/* Add New Items */}
                                    <form onSubmit={handleAddItemsToOrder}>
                                        <div className="add-items-section">
                                            <h3>Add New Items</h3>

                                            {manageItemsErrors.submit && (
                                                <div className="error-alert">
                                                    {manageItemsErrors.submit}
                                                </div>
                                            )}

                                            <div className="items-container">
                                                {manageItemsData.orderItems.map((item, index) => (
                                                    <div key={index} className="item-row">
                                                        <div className="item-row-header">
                                                            <span className="item-number">➕ New Item {index + 1}</span>
                                                            {manageItemsData.orderItems.length > 1 && (
                                                                <button
                                                                    type="button"
                                                                    className="remove-item-btn"
                                                                    onClick={() => handleRemoveItemFromManage(index)}
                                                                >
                                                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                                        <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                                                    </svg>
                                                                </button>
                                                            )}
                                                        </div>

                                                        <div className="item-row-content">
                                                            {/* Product Selection */}
                                                            <div className="item-field">
                                                                <label className="item-label">Product *</label>
                                                                {isLoadingProducts ? (
                                                                    <div className="loading-dropdown">
                                                                        <div className="spinner-small"></div>
                                                                        <span>Loading...</span>
                                                                    </div>
                                                                ) : (
                                                                    <select
                                                                        className={`form-input ${manageItemsErrors[`item_${index}_productId`] ? 'input-error' :  ''}`}
                                                                        value={item.productId}
                                                                        onChange={(e) => handleManageItemChange(index, 'productId', e.target.value)}
                                                                    >
                                                                        <option value="">Select a product</option>
                                                                        {products.map(product => (
                                                                            <option key={product.productId} value={product.productId}>
                                                                                {product.name} ({product.productId}) - ${product.price?.toFixed(2)}
                                                                            </option>
                                                                        ))}
                                                                    </select>
                                                                )}
                                                                {manageItemsErrors[`item_${index}_productId`] && (
                                                                    <span className="error-text">{manageItemsErrors[`item_${index}_productId`]}</span>
                                                                )}
                                                            </div>

                                                            {/* Quantity */}
                                                            <div className="item-field">
                                                                <label className="item-label">Quantity *</label>
                                                                <input
                                                                    type="number"
                                                                    min="1"
                                                                    className={`form-input ${manageItemsErrors[`item_${index}_quantity`] ? 'input-error' : ''}`}
                                                                    placeholder="Qty"
                                                                    value={item.quantity}
                                                                    onChange={(e) => handleManageItemChange(index, 'quantity', e.target.value)}
                                                                />
                                                                {manageItemsErrors[`item_${index}_quantity`] && (
                                                                    <span className="error-text">{manageItemsErrors[`item_${index}_quantity`]}</span>
                                                                )}
                                                            </div>

                                                            {/* Item Total */}
                                                            {item.productId && item.quantity && (
                                                                <div className="item-total">
                                                                    <span className="item-total-label">Item Total: </span>
                                                                    <span className="item-total-value">
                                                                        ${(products.find(p => p.productId === item.productId)?.price * parseInt(item.quantity || 0)).toFixed(2)}
                                                                    </span>
                                                                </div>
                                                            )}
                                                        </div>
                                                    </div>
                                                ))}

                                                <button
                                                    type="button"
                                                    className="add-item-btn"
                                                    onClick={handleAddItemToManage}
                                                >
                                                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                        <path d="M12 5V19M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                                    </svg>
                                                    Add Another Item
                                                </button>
                                            </div>
                                        </div>

                                        <div className="modal-footer">
                                            <button type="button" className="btn-secondary" onClick={handleCloseManageItemsModal}>
                                                Cancel
                                            </button>
                                            <button
                                                type="submit"
                                                className="btn-primary"
                                                disabled={isLoadingProducts}
                                            >
                                                Add Items to Order
                                            </button>
                                        </div>
                                    </form>
                                </>
                            ) : (
                                <p>Failed to load order details</p>
                            )}
                        </div>
                    </div>
                </div>
            )}

            {/* View Details Modal */}
            {showDetailsModal && (
                <div className="modal-overlay" onClick={handleCloseDetailsModal}>
                    <div className="modal-content modal-large" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>📋 Sales Order Details</h2>
                            <button className="modal-close-btn" onClick={handleCloseDetailsModal}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <div className="modal-body">
                            {isLoadingDetails ? (
                                <div className="loading-details">
                                    <div className="spinner-small"></div>
                                    <p>Loading order details...</p>
                                </div>
                            ) : orderDetails ? (
                                <>
                                    {/* Order Information */}
                                    <div className="details-section">
                                        <h3 className="details-title">Order Information</h3>
                                        <div className="details-grid">
                                            <div className="detail-item">
                                                <span className="detail-label">Order Reference: </span>
                                                <span className="detail-value po-number">{orderDetails.orderReference}</span>
                                            </div>
                                            <div className="detail-item">
                                                <span className="detail-label">Status:</span>
                                                <span
                                                    className="status-badge-so"
                                                    style={{
                                                        backgroundColor: `${getStatusColor(orderDetails.status)}20`,
                                                        color: getStatusColor(orderDetails.status),
                                                        border: `1px solid ${getStatusColor(orderDetails.status)}50`
                                                    }}
                                                >
                                                    {orderDetails.status.replace(/_/g, ' ')}
                                                </span>
                                            </div>
                                            <div className="detail-item">
                                                <span className="detail-label">Order Date:</span>
                                                <span className="detail-value">{formatDate(orderDetails.orderDate)}</span>
                                            </div>
                                            <div className="detail-item">
                                                <span className="detail-label">Estimated Delivery:</span>
                                                <span className="detail-value">{formatDate(orderDetails.estimatedDeliveryDate)}</span>
                                            </div>
                                            {orderDetails.deliveryDate && (
                                                <div className="detail-item">
                                                    <span className="detail-label">Actual Delivery:</span>
                                                    <span className="detail-value">{formatDate(orderDetails.deliveryDate)}</span>
                                                </div>
                                            )}
                                        </div>
                                    </div>

                                    {/* Customer Information */}
                                    <div className="details-section">
                                        <h3 className="details-title">Customer Information</h3>
                                        <div className="details-grid">
                                            <div className="detail-item">
                                                <span className="detail-label">Customer Name:</span>
                                                <span className="detail-value">{orderDetails.customerName}</span>
                                            </div>
                                            <div className="detail-item">
                                                <span className="detail-label">Destination:</span>
                                                <span className="detail-value">{orderDetails.destination}</span>
                                            </div>
                                        </div>
                                    </div>

                                    {/* Order Items */}
                                    <div className="details-section">
                                        <h3 className="details-title">Order Items</h3>
                                        <div className="details-items-table">
                                            <table className="items-detail-table">
                                                <thead>
                                                <tr>
                                                    <th>Product</th>
                                                    <th>Category</th>
                                                    <th>Ordered</th>
                                                    <th>Shipped</th>
                                                    <th>Unit Price</th>
                                                    <th>Total</th>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                {orderDetails.items?.map((item) => (
                                                    <tr key={item.id}>
                                                        <td>
                                                            <div className="item-product-info">
                                                                <span className="item-product-name">{item.productName}</span>
                                                                <span className="item-product-id">{item.productId}</span>
                                                            </div>
                                                        </td>
                                                        <td>
                                                            <span
                                                                className="category-badge-so"
                                                                style={{
                                                                    backgroundColor: `${getCategoryColor(item.productCategory)}20`,
                                                                    color: getCategoryColor(item.productCategory),
                                                                }}
                                                            >
                                                                {item.productCategory?.replace(/_/g, ' ')}
                                                            </span>
                                                        </td>
                                                        <td className="qty-cell">{item.quantity}</td>
                                                        <td className="qty-cell shipped">{item.approvedQuantity}</td>
                                                        <td className="price-cell">${item.unitPrice?.toFixed(2)}</td>
                                                        <td className="price-cell total">${item.totalPrice?.toFixed(2)}</td>
                                                    </tr>
                                                ))}
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>

                                    {/* Summary */}
                                    <div className="details-section">
                                        <h3 className="details-title">Order Summary</h3>
                                        <div className="details-grid">
                                            <div className="detail-item">
                                                <span className="detail-label">Total Items:</span>
                                                <span className="detail-value quantity">{orderDetails.totalItems} units</span>
                                            </div>
                                            <div className="detail-item">
                                                <span className="detail-label">Total Shipped:</span>
                                                <span className="detail-value quantity">{orderDetails.totalApprovedQuantity || 0} units</span>
                                            </div>
                                            <div className="detail-item">
                                                <span className="detail-label">Total Amount:</span>
                                                <span className="detail-value price">${orderDetails.totalAmount?.toFixed(2)}</span>
                                            </div>
                                        </div>

                                        {/* Progress Bar */}
                                        <div className="progress-section">
                                            <div className="progress-header">
                                                <span>Fulfillment Progress</span>
                                                <span>{Math.round(((orderDetails.totalApprovedQuantity || 0) / orderDetails.totalItems) * 100)}%</span>
                                            </div>
                                            <div className="progress-bar-so">
                                                <div
                                                    className="progress-fill-so"
                                                    style={{
                                                        width:  `${((orderDetails.totalApprovedQuantity || 0) / orderDetails.totalItems) * 100}%`,
                                                        backgroundColor: orderDetails.totalApprovedQuantity === orderDetails.totalItems ? '#4CAF50' : '#2196F3'
                                                    }}
                                                ></div>
                                            </div>
                                        </div>
                                    </div>

                                    {/* User Information */}
                                    <div className="details-section">
                                        <h3 className="details-title">Additional Information</h3>
                                        <div className="details-grid">
                                            <div className="detail-item">
                                                <span className="detail-label">Confirmed By:</span>
                                                <span className="detail-value">{orderDetails.confirmedBy || 'N/A'}</span>
                                            </div>
                                            {orderDetails.lastUpdate && (
                                                <div className="detail-item">
                                                    <span className="detail-label">Last Update:</span>
                                                    <span className="detail-value">{formatDate(orderDetails.lastUpdate)}</span>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </>
                            ) : (
                                <p>Failed to load order details</p>
                            )}
                        </div>

                        <div className="modal-footer">
                            <button type="button" className="btn-secondary" onClick={handleCloseDetailsModal}>
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* QR Code Modal */}
            {showQrModal && (
                <div className="modal-overlay" onClick={handleCloseQrModal}>
                    <div className="modal-content modal-qr" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>📱 Order QR Code</h2>
                            <button className="modal-close-btn" onClick={handleCloseQrModal}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <div className="modal-body">
                            {isLoadingQr ? (
                                <div className="loading-qr">
                                    <div className="spinner-small"></div>
                                    <p>Generating QR code...</p>
                                </div>
                            ) : qrError ? (
                                <div className="error-alert">
                                    {qrError}
                                </div>
                            ) : qrCodeData ? (
                                <>
                                    {/* Order Info */}
                                    <div className="qr-order-info">
                                        <h3>Order Details</h3>
                                        <div className="qr-info-row">
                                            <span className="qr-label">Order Reference:</span>
                                            <span className="qr-value">{qrCodeData.orderReference}</span>
                                        </div>
                                        <div className="qr-info-row">
                                            <span className="qr-label">Customer:</span>
                                            <span className="qr-value">{selectedOrder?.customerName}</span>
                                        </div>
                                        <div className="qr-info-row">
                                            <span className="qr-label">Destination:</span>
                                            <span className="qr-value">{selectedOrder?.destination}</span>
                                        </div>
                                    </div>

                                    {/* QR Code Image */}
                                    <div className="qr-image-container">
                                        <img
                                            src={qrCodeData.qrCodeUrl}
                                            alt="Order QR Code"
                                            className="qr-image"
                                        />
                                        <p className="qr-scan-hint">Scan this QR code to verify order details</p>
                                    </div>

                                    {/* QR Token */}
                                    <div className="qr-token-section">
                                        <label className="qr-token-label">QR Token:</label>
                                        <div className="qr-token-container">
                                            <input
                                                type="text"
                                                className="qr-token-input"
                                                value={qrCodeData.qrToken}
                                                readOnly
                                            />
                                            <button
                                                className="copy-token-btn"
                                                onClick={handleCopyToken}
                                                title="Copy Token"
                                            >
                                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <rect x="9" y="9" width="13" height="13" rx="2" stroke="currentColor" strokeWidth="2"/>
                                                    <path d="M5 15H4C2.89543 15 2 14.1046 2 13V4C2 2.89543 2.89543 2 4 2H13C14.1046 2 15 2.89543 15 4V5" stroke="currentColor" strokeWidth="2"/>
                                                </svg>
                                            </button>
                                        </div>
                                        <small className="qr-expires-hint">
                                            QR code URL expires in {qrCodeData.expiresIn} minutes
                                        </small>
                                    </div>

                                    {/* Action Buttons */}
                                    <div className="qr-actions">
                                        <button className="qr-action-btn download-btn" onClick={handleDownloadQr}>
                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                <path d="M7 10L12 15L17 10" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                <path d="M12 15V3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                            </svg>
                                            Download QR
                                        </button>
                                        <button className="qr-action-btn print-btn" onClick={handlePrintQr}>
                                            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M6 9V2H18V9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                <path d="M6 18H4C3.46957 18 2.96086 17.7893 2.58579 17.4142C2.21071 17.0391 2 16.5304 2 16V11C2 10.4696 2.21071 9.96086 2.58579 9.58579C2.96086 9.21071 3.46957 9 4 9H20C20.5304 9 21.0391 9.21071 21.4142 9.58579C21.7893 9.96086 22 10.4696 22 11V16C22 16.5304 21.7893 17.0391 21.4142 17.4142C21.0391 17.7893 20.5304 18 20 18H18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                                <rect x="6" y="14" width="12" height="8" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                            </svg>
                                            Print QR
                                        </button>
                                    </div>
                                </>
                            ) : null}
                        </div>

                        <div className="modal-footer">
                            <button type="button" className="btn-secondary" onClick={handleCloseQrModal}>
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Remove Item Confirmation Modal */}
            {showRemoveItemModal && (
                <div className="modal-overlay" onClick={handleCloseRemoveItemModal}>
                    <div className="modal-content modal-small" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>⚠️ Remove Item</h2>
                            <button className="modal-close-btn" onClick={handleCloseRemoveItemModal}>
                                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 6L6 18M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                                </svg>
                            </button>
                        </div>

                        <div className="modal-body">
                            <p>Are you sure you want to remove this item from the order?</p>

                            <div className="order-details-box">
                                <div className="detail-row">
                                    <span className="detail-label">Product:</span>
                                    <span className="detail-value">{selectedItemToRemove?.productName}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Product ID:</span>
                                    <span className="detail-value">{selectedItemToRemove?.productId}</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Quantity:</span>
                                    <span className="detail-value">{selectedItemToRemove?.quantity} units</span>
                                </div>
                                <div className="detail-row">
                                    <span className="detail-label">Unit Price:</span>
                                    <span className="detail-value">${selectedItemToRemove?.unitPrice?.toFixed(2)}</span>
                                </div>
                            </div>

                            <p className="warning-text">
                                ⚠️ This action cannot be undone. The item will be removed from the order.
                            </p>
                        </div>

                        <div className="modal-footer">
                            <button type="button" className="btn-secondary" onClick={handleCloseRemoveItemModal}>
                                Cancel
                            </button>
                            <button type="button" className="btn-danger" onClick={handleRemoveItem}>
                                Remove Item
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Toast Notification */}
            {toast && (
                <Toast
                    message={toast.message}
                    type={toast.type}
                    onClose={closeToast}
                    duration={3000}
                />
            )}
        </div>
    );
};

export default SalesOrders;



