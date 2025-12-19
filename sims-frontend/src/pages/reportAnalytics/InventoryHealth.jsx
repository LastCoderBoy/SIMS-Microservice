import { useState, useEffect } from 'react';
import { Chart as ChartJS, ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title } from 'chart.js';
import { Doughnut, Bar } from 'react-chartjs-2';
import analyticsService from '../../services/reportAnalytics/analyticsService';
import authService from '../../services/authService';
import './InventoryHealth.css';

// Register Chart.js components
ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title);

const InventoryHealth = () => {
    const currentUser = authService.getCurrentUser();
    const isAdminOrManager = currentUser?.role === 'ROLE_ADMIN' || currentUser?.role === 'ROLE_MANAGER';

    const [metrics, setMetrics] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // Chart colors
    const chartColors = {
        primary: '#1976d2',
        success: '#4caf50',
        warning: '#ff9800',
        error: '#f44336',
        info: '#2196f3',
        purple: '#9c27b0',
        teal: '#00bcd4',
        grey: '#757575',
    };

    // Fetch inventory health metrics
    const fetchMetrics = async () => {
        setIsLoading(true);
        setError(null);

        try {
            const data = await analyticsService.getInventoryHealth();
            setMetrics(data.data);
        } catch (err) {
            console.error('Error fetching inventory health:', err);
            setError(err.message || 'Failed to load inventory health metrics');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        if (isAdminOrManager) {
            fetchMetrics();
        } else {
            setError('Access Denied: Admin or Manager role required');
            setIsLoading(false);
        }
    }, []);

    // Format currency
    const formatCurrency = (value) => {
        return new Intl.NumberFormat('en-US', {
            style:  'currency',
            currency:  'USD',
            minimumFractionDigits: 2,
        }).format(value || 0);
    };

    // Get health status color
    const getHealthStatusColor = (status) => {
        switch (status) {
            case 'EXCELLENT':
                return chartColors.success;
            case 'GOOD':
                return chartColors.info;
            case 'FAIR':
                return chartColors.warning;
            case 'POOR':
                return chartColors.error;
            case 'CRITICAL':
                return '#d32f2f';
            default:
                return chartColors.grey;
        }
    };

    // Get health status icon
    const getHealthStatusIcon = (status) => {
        switch (status) {
            case 'EXCELLENT':
                return '🌟';
            case 'GOOD':
                return '✅';
            case 'FAIR':
                return '⚠️';
            case 'POOR':
                return '❌';
            case 'CRITICAL':
                return '🚨';
            default:
                return '❓';
        }
    };

    // Stock Health Breakdown Chart (Horizontal Bar)
    const stockHealthChartData = {
        labels:  ['In Stock', 'Low Stock', 'Out of Stock'],
        datasets: [
            {
                label: 'Number of Items',
                data: [
                    metrics?.inStockItems || 0,
                    metrics?.lowStockItems || 0,
                    metrics?.outOfStockItems || 0,
                ],
                backgroundColor: [
                    chartColors.success,
                    chartColors.warning,
                    chartColors.error,
                ],
                borderRadius: 8,
                barThickness: 40,
            },
        ],
    };

    const stockHealthChartOptions = {
        indexAxis: 'y',
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                display: false,
            },
            tooltip:  {
                callbacks: {
                    label: function (context) {
                        return `${context.label}: ${context.parsed.x} items`;
                    },
                },
            },
        },
        scales: {
            x: {
                beginAtZero: true,
                ticks: {
                    stepSize: 1,
                },
                grid: {
                    display:  true,
                    color: 'rgba(0, 0, 0, 0.05)',
                },
            },
            y: {
                grid: {
                    display: false,
                },
            },
        },
    };

    // Stock Utilization Chart (Doughnut)
    const utilizationChartData = {
        labels:  ['Reserved Stock', 'Available Stock'],
        datasets: [
            {
                data: [
                    metrics?.totalReservedStock || 0,
                    metrics?.availableStock || 0,
                ],
                backgroundColor: [chartColors.purple, chartColors.grey],
                borderColor: ['#fff', '#fff'],
                borderWidth: 3,
            },
        ],
    };

    const utilizationChartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '70%',
        plugins: {
            legend: {
                position: 'bottom',
                labels: {
                    padding: 15,
                    font: {
                        size: 12,
                        weight: '600',
                    },
                },
            },
            tooltip:  {
                callbacks: {
                    label: function (context) {
                        const label = context.label || '';
                        const value = context.parsed || 0;
                        const total = context.dataset.data.reduce((a, b) => a + b, 0);
                        const percentage = ((value / total) * 100).toFixed(1);
                        return `${label}: ${value} (${percentage}%)`;
                    },
                },
            },
        },
    };

    // Health Score Chart (Doughnut - Gauge style)
    const healthScoreChartData = {
        labels:  ['Health Score', 'Remaining'],
        datasets: [
            {
                data: [
                    metrics?.healthScore || 0,
                    100 - (metrics?.healthScore || 0),
                ],
                backgroundColor: [
                    getHealthStatusColor(metrics?.healthStatus),
                    'rgba(0, 0, 0, 0.05)',
                ],
                borderWidth: 0,
            },
        ],
    };

    const healthScoreChartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '75%',
        rotation: -90,
        circumference: 180,
        plugins: {
            legend: {
                display: false,
            },
            tooltip: {
                enabled: false,
            },
        },
    };

    // Loading State
    if (isLoading) {
        return (
            <div className="loading-container">
                <div className="spinner-large"></div>
                <p>Loading inventory health...</p>
            </div>
        );
    }

    // Error State
    if (error) {
        return (
            <div className="error-container">
                <svg className="error-icon-large" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                    <path d="M12 8V12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                    <circle cx="12" cy="16" r="1" fill="currentColor"/>
                </svg>
                <h2>Error Loading Inventory Health</h2>
                <p>{error}</p>
                {isAdminOrManager && (
                    <button className="retry-btn" onClick={fetchMetrics}>
                        Try Again
                    </button>
                )}
            </div>
        );
    }

    return (
        <div className="inventory-health-page">
            {/* Page Header */}
            <div className="page-header">
                <div>
                    <h1 className="page-title">💚 Inventory Health</h1>
                    <p className="page-subtitle">Monitor stock levels, utilization, and overall inventory health</p>
                </div>
                <button className="refresh-btn" onClick={fetchMetrics}>
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M21.5 2V8M21.5 8H15.5M21.5 8L18 4.5C16.8 3.3 15.3 2.4 13.6 2C9.9 1 6.1 2.7 4.1 6C2.1 9.3 2.3 13.5 4.8 16.5C7.3 19.5 11.3 20.6 14.9 19.3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                    </svg>
                    Refresh
                </button>
            </div>

            {/* Health Status Card */}
            <div className="health-status-card">
                <div className="health-status-content">
                    <div className="health-status-icon" style={{ color: getHealthStatusColor(metrics?.healthStatus) }}>
                        {getHealthStatusIcon(metrics?.healthStatus)}
                    </div>
                    <div className="health-status-text">
                        <h2>Overall Health Status</h2>
                        <div
                            className="health-status-badge"
                            style={{
                                backgroundColor: `${getHealthStatusColor(metrics?.healthStatus)}20`,
                                color: getHealthStatusColor(metrics?.healthStatus),
                                border: `2px solid ${getHealthStatusColor(metrics?.healthStatus)}`,
                            }}
                        >
                            {metrics?.healthStatus}
                        </div>
                    </div>
                </div>
                <div className="health-score-display">
                    <span className="health-score-value" style={{ color: getHealthStatusColor(metrics?.healthStatus) }}>
                        {metrics?.healthScore?.toFixed(1)}%
                    </span>
                    <span className="health-score-label">Health Score</span>
                </div>
            </div>

            {/* Metrics Cards Grid */}
            <div className="metrics-grid-health">
                {/* Total Stock Value */}
                <div className="metric-card-health metric-primary">
                    <div className="metric-icon-wrapper-health">
                        <svg className="metric-icon-health" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 2V22M17 5H9.5C8.57174 5 7.6815 5.36875 7.02513 6.02513C6.36875 6.6815 6 7.57174 6 8.5C6 9.42826 6.36875 10.3185 7.02513 10.9749C7.6815 11.6313 8.57174 12 9.5 12H14.5C15.4283 12 16.3185 12.3687 16.9749 13.0251C17.6313 13.6815 18 14.5717 18 15.5C18 16.4283 17.6313 17.3185 16.9749 17.9749C16.3185 18.6313 15.4283 19 14.5 19H6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-health">
                        <p className="metric-label-health">Total Stock Value</p>
                        <h2 className="metric-value-health">{formatCurrency(metrics?.totalStockValueAtRetail)}</h2>
                        <p className="metric-sublabel-health">At retail price</p>
                    </div>
                </div>

                {/* Total Stock Quantity */}
                <div className="metric-card-health metric-info">
                    <div className="metric-icon-wrapper-health">
                        <svg className="metric-icon-health" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="3" y="3" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                            <rect x="14" y="3" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                            <rect x="14" y="14" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                            <rect x="3" y="14" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                        </svg>
                    </div>
                    <div className="metric-content-health">
                        <p className="metric-label-health">Total Stock</p>
                        <h2 className="metric-value-health">{metrics?.totalStockQuantity || 0}</h2>
                        <p className="metric-sublabel-health">Units in inventory</p>
                    </div>
                </div>

                {/* Reserved Stock */}
                <div className="metric-card-health metric-purple">
                    <div className="metric-icon-wrapper-health">
                        <svg className="metric-icon-health" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="3" y="11" width="18" height="11" rx="2" stroke="currentColor" strokeWidth="2"/>
                            <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" strokeWidth="2"/>
                            <circle cx="12" cy="16" r="1" fill="currentColor"/>
                        </svg>
                    </div>
                    <div className="metric-content-health">
                        <p className="metric-label-health">Reserved Stock</p>
                        <h2 className="metric-value-health">{metrics?.totalReservedStock || 0}</h2>
                        <p className="metric-sublabel-health">Units reserved</p>
                    </div>
                </div>

                {/* Available Stock */}
                <div className="metric-card-health metric-success">
                    <div className="metric-icon-wrapper-health">
                        <svg className="metric-icon-health" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 11L12 14L22 4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </div>
                    <div className="metric-content-health">
                        <p className="metric-label-health">Available Stock</p>
                        <h2 className="metric-value-health">{metrics?.availableStock || 0}</h2>
                        <p className="metric-sublabel-health">Units available</p>
                    </div>
                </div>
            </div>

            {/* Charts Section */}
            <div className="charts-section-health">
                {/* Stock Health Breakdown */}
                <div className="chart-card-health chart-large">
                    <div className="chart-header">
                        <h3 className="chart-title">📊 Stock Health Breakdown</h3>
                        <span className="chart-subtitle">Distribution by stock level</span>
                    </div>
                    <div className="chart-wrapper-health">
                        <Bar data={stockHealthChartData} options={stockHealthChartOptions} />
                    </div>
                    <div className="chart-legend-custom">
                        <div className="legend-item">
                            <span className="legend-color" style={{ backgroundColor: chartColors.success }}></span>
                            <span className="legend-label">In Stock</span>
                            <span className="legend-value">{metrics?.inStockItems || 0} items</span>
                        </div>
                        <div className="legend-item">
                            <span className="legend-color" style={{ backgroundColor: chartColors.warning }}></span>
                            <span className="legend-label">Low Stock</span>
                            <span className="legend-value">{metrics?.lowStockItems || 0} items</span>
                        </div>
                        <div className="legend-item">
                            <span className="legend-color" style={{ backgroundColor: chartColors.error }}></span>
                            <span className="legend-label">Out of Stock</span>
                            <span className="legend-value">{metrics?.outOfStockItems || 0} items</span>
                        </div>
                    </div>
                </div>

                {/* Stock Utilization */}
                <div className="chart-card-health">
                    <div className="chart-header">
                        <h3 className="chart-title">🔄 Stock Utilization</h3>
                        <span className="chart-subtitle">Reserved vs Available</span>
                    </div>
                    <div className="chart-wrapper-health">
                        <Doughnut data={utilizationChartData} options={utilizationChartOptions} />
                    </div>
                    <div className="utilization-percentage">
                        <span className="utilization-value">{metrics?.stockUtilization?.toFixed(1)}%</span>
                        <span className="utilization-label">Utilization Rate</span>
                    </div>
                </div>

                {/* Health Score Gauge */}
                <div className="chart-card-health">
                    <div className="chart-header">
                        <h3 className="chart-title">🎯 Health Score</h3>
                        <span className="chart-subtitle">Overall inventory health</span>
                    </div>
                    <div className="gauge-wrapper">
                        <Doughnut data={healthScoreChartData} options={healthScoreChartOptions} />
                        <div className="gauge-center">
                            <span className="gauge-value" style={{ color: getHealthStatusColor(metrics?.healthStatus) }}>
                                {metrics?.healthScore?.toFixed(0)}
                            </span>
                            <span className="gauge-label">/ 100</span>
                        </div>
                    </div>
                    <div className="gauge-status">
                        <span
                            className="gauge-status-badge"
                            style={{
                                backgroundColor: `${getHealthStatusColor(metrics?.healthStatus)}20`,
                                color: getHealthStatusColor(metrics?.healthStatus),
                                border: `2px solid ${getHealthStatusColor(metrics?.healthStatus)}`,
                            }}
                        >
                            {getHealthStatusIcon(metrics?.healthStatus)} {metrics?.healthStatus}
                        </span>
                    </div>
                </div>
            </div>

            {/* Summary Stats */}
            <div className="summary-section">
                <h2 className="section-title">📋 Inventory Summary</h2>

                <div className="summary-grid-health">
                    <div className="summary-card">
                        <div className="summary-header">
                            <span className="summary-icon">📦</span>
                            <h4 className="summary-title">Stock Distribution</h4>
                        </div>
                        <div className="summary-stats">
                            <div className="summary-stat">
                                <span className="stat-label">In Stock: </span>
                                <span className="stat-value success">{metrics?.inStockItems || 0} items</span>
                            </div>
                            <div className="summary-stat">
                                <span className="stat-label">Low Stock:</span>
                                <span className="stat-value warning">{metrics?.lowStockItems || 0} items</span>
                            </div>
                            <div className="summary-stat">
                                <span className="stat-label">Out of Stock: </span>
                                <span className="stat-value error">{metrics?.outOfStockItems || 0} items</span>
                            </div>
                        </div>
                    </div>

                    <div className="summary-card">
                        <div className="summary-header">
                            <span className="summary-icon">📊</span>
                            <h4 className="summary-title">Stock Metrics</h4>
                        </div>
                        <div className="summary-stats">
                            <div className="summary-stat">
                                <span className="stat-label">Total Quantity:</span>
                                <span className="stat-value">{metrics?.totalStockQuantity || 0} units</span>
                            </div>
                            <div className="summary-stat">
                                <span className="stat-label">Reserved:</span>
                                <span className="stat-value">{metrics?.totalReservedStock || 0} units</span>
                            </div>
                            <div className="summary-stat">
                                <span className="stat-label">Available:</span>
                                <span className="stat-value">{metrics?.availableStock || 0} units</span>
                            </div>
                        </div>
                    </div>

                    <div className="summary-card">
                        <div className="summary-header">
                            <span className="summary-icon">💰</span>
                            <h4 className="summary-title">Financial Overview</h4>
                        </div>
                        <div className="summary-stats">
                            <div className="summary-stat">
                                <span className="stat-label">Total Value:</span>
                                <span className="stat-value primary">{formatCurrency(metrics?.totalStockValueAtRetail)}</span>
                            </div>
                            <div className="summary-stat">
                                <span className="stat-label">Utilization:</span>
                                <span className="stat-value">{metrics?.stockUtilization?.toFixed(1)}%</span>
                            </div>
                            <div className="summary-stat">
                                <span className="stat-label">Health Score:</span>
                                <span className="stat-value" style={{ color: getHealthStatusColor(metrics?.healthStatus) }}>
                                    {metrics?.healthScore?.toFixed(1)}%
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default InventoryHealth;