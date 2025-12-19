import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import authService from '../../services/userManagement/authService';
import './Sidebar.css';

const Sidebar = ({ isOpen, toggleSidebar }) => {
    const navigate = useNavigate();
    const currentUser = authService.getCurrentUser();
    const isAdminOrManager = currentUser?.role === 'ROLE_ADMIN' || currentUser?.role === 'ROLE_MANAGER';

    // State for managing submenu expansion
    const [expandedMenus, setExpandedMenus] = useState({
        inventoryControl:  true,
        orderManagement: false,
        reportsAnalytics: false,
    });

    // Toggle submenu
    const toggleSubmenu = (menuKey) => {
        setExpandedMenus(prev => ({
            ...prev,
            [menuKey]: !prev[menuKey]
        }));
    };

    // Handle Reports & Analytics click
    const handleReportsAnalyticsClick = () => {
        navigate('/analytics/dashboard');
        setExpandedMenus(prev => ({ ...prev, reportsAnalytics: true }));
    };

    // Handle user info click - Navigate to profile
    const handleUserInfoClick = () => {
        navigate('/profile');
    };

    const handleLogout = async () => {
        try {
            await authService.logout();
            navigate('/login');
        } catch (error) {
            console.error('Logout error:', error);
        }
    };

    return (
        <>
            {/* Overlay for mobile */}
            {isOpen && (
                <div className="sidebar-overlay" onClick={toggleSidebar}></div>
            )}

            {/* Sidebar */}
            <aside className={`sidebar ${isOpen ? 'open' : 'closed'}`}>
                {/* Logo Section with Toggle Button */}
                <div className="sidebar-header">
                    {isOpen && (
                        <div className="sidebar-logo">
                            <svg
                                className="logo-icon"
                                viewBox="0 0 24 24"
                                fill="none"
                                xmlns="http://www.w3.org/2000/svg"
                            >
                                <path
                                    d="M20 7H4C2.9 7 2 7.9 2 9V19C2 20.1 2.9 21 4 21H20C21.1 21 22 20.1 22 19V9C22 7.9 21.1 7 20 7Z"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                />
                                <path
                                    d="M16 7V5C16 4.46957 15.7893 3.96086 15.4142 3.58579C15.0391 3.21071 14.5304 3 14 3H10C9.46957 3 8.96086 3.21071 8.58579 3.58579C8.21071 3.96086 8 4.46957 8 5V7"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                />
                                <path
                                    d="M12 12V16"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                />
                                <path
                                    d="M9 14H15"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                />
                            </svg>
                            <span className="logo-text">SIMS</span>
                        </div>
                    )}

                    {/* Toggle Button */}
                    <button
                        className="sidebar-toggle-btn"
                        onClick={toggleSidebar}
                        aria-label="Toggle Sidebar"
                    >
                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            {isOpen ? (
                                <>
                                    <path d="M11 19L4 12L11 5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    <path d="M19 19L12 12L19 5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                </>
                            ) : (
                                <>
                                    <path d="M13 5L20 12L13 19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    <path d="M5 5L12 12L5 19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                </>
                            )}
                        </svg>
                    </button>
                </div>

                {/* Navigation Menu */}
                <nav className="sidebar-nav">
                    {/* Dashboard */}
                    <NavLink
                        to="/dashboard"
                        className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                        title="Dashboard"
                    >
                        <svg className="nav-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="3" y="3" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                            <rect x="14" y="3" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                            <rect x="14" y="14" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                            <rect x="3" y="14" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                        </svg>
                        {isOpen && <span className="nav-text">Dashboard</span>}
                    </NavLink>

                    {/* Inventory Control (with submenu) */}
                    <div className="nav-item-group">
                        <button
                            className={`nav-item ${expandedMenus.inventoryControl ? 'expanded' : ''}`}
                            onClick={() => toggleSubmenu('inventoryControl')}
                            title="Inventory Control"
                        >
                            <div className="nav-item-content">
                                <svg className="nav-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M20 7H4C2.9 7 2 7.9 2 9V19C2 20.1 2.9 21 4 21H20C21.1 21 22 20.1 22 19V9C22 7.9 21.1 7 20 7Z" stroke="currentColor" strokeWidth="2"/>
                                    <path d="M16 7V5C16 3.9 15.1 3 14 3H10C8.9 3 8 3.9 8 5V7" stroke="currentColor" strokeWidth="2"/>
                                </svg>
                                {isOpen && <span className="nav-text">Inventory Control</span>}
                            </div>
                            {isOpen && (
                                <svg
                                    className={`expand-icon ${expandedMenus.inventoryControl ? 'rotated' : ''}`}
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <path d="M6 9L12 15L18 9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                </svg>
                            )}
                        </button>

                        {/* Inventory Submenu */}
                        {isOpen && expandedMenus.inventoryControl && (
                            <div className="submenu">
                                <NavLink to="/inventory/total-items" className={({ isActive }) => `submenu-item ${isActive ? 'active' : ''}`}>
                                    <span className="submenu-dot"></span>
                                    <span className="submenu-text">Total Items</span>
                                </NavLink>
                                <NavLink to="/inventory/low-stock" className={({ isActive }) => `submenu-item ${isActive ? 'active' : ''}`}>
                                    <span className="submenu-dot"></span>
                                    <span className="submenu-text">Low Stock</span>
                                </NavLink>
                                <NavLink to="/inventory/damage-loss" className={({ isActive }) => `submenu-item ${isActive ? 'active' : ''}`}>
                                    <span className="submenu-dot"></span>
                                    <span className="submenu-text">Damage & Loss</span>
                                </NavLink>
                                <NavLink to="/inventory/incoming-stock" className={({ isActive }) => `submenu-item ${isActive ? 'active' : ''}`}>
                                    <span className="submenu-dot"></span>
                                    <span className="submenu-text">Incoming Stock</span>
                                </NavLink>
                                <NavLink to="/inventory/outgoing-stock" className={({ isActive }) => `submenu-item ${isActive ? 'active' : ''}`}>
                                    <span className="submenu-dot"></span>
                                    <span className="submenu-text">Outgoing Stock</span>
                                </NavLink>
                            </div>
                        )}
                    </div>

                    {/* Order Management (with submenu) */}
                    <div className="nav-item-group">
                        <button
                            className={`nav-item ${expandedMenus.orderManagement ? 'expanded' :  ''}`}
                            onClick={() => toggleSubmenu('orderManagement')}
                            title="Order Management"
                        >
                            <div className="nav-item-content">
                                <svg className="nav-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M9 11L12 14L22 4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                </svg>
                                {isOpen && <span className="nav-text">Order Management</span>}
                            </div>
                            {isOpen && (
                                <svg
                                    className={`expand-icon ${expandedMenus.orderManagement ? 'rotated' : ''}`}
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    xmlns="http://www.w3.org/2000/svg"
                                >
                                    <path d="M6 9L12 15L18 9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                </svg>
                            )}
                        </button>

                        {/* Order Management Submenu */}
                        {isOpen && expandedMenus.orderManagement && (
                            <div className="submenu">
                                <NavLink to="/orders/purchase-orders" className={({ isActive }) => `submenu-item ${isActive ? 'active' : ''}`}>
                                    <span className="submenu-dot"></span>
                                    <span className="submenu-text">Purchase Orders</span>
                                </NavLink>
                                <NavLink to="/orders/sales-orders" className={({ isActive }) => `submenu-item ${isActive ? 'active' : ''}`}>
                                    <span className="submenu-dot"></span>
                                    <span className="submenu-text">Sales Orders</span>
                                </NavLink>
                                <NavLink to="/orders/qr-tracker" className={({ isActive }) => `submenu-item ${isActive ? 'active' : ''}`}>
                                    <span className="submenu-dot"></span>
                                    <span className="submenu-text">QR Order Tracker</span>
                                </NavLink>
                            </div>
                        )}
                    </div>

                    {/* Product Management (with submenu) - NEW */}
                    <NavLink
                        to="/products"
                        className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
                        title="Product Management"
                    >
                        <svg className="nav-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <rect x="3" y="3" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                            <rect x="14" y="3" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                            <rect x="14" y="14" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                            <rect x="3" y="14" width="7" height="7" rx="1" stroke="currentColor" strokeWidth="2"/>
                        </svg>
                        {isOpen && <span className="nav-text">Product Management</span>}
                    </NavLink>

                    {/* Reports & Analytics (CLICKABLE - Navigates to /analytics/dashboard) */}
                    <div className="nav-item-group">
                        <button
                            className={`nav-item ${expandedMenus.reportsAnalytics ? 'expanded' : ''}`}
                            onClick={handleReportsAnalyticsClick}
                            title="Reports & Analytics"
                        >
                            <div className="nav-item-content">
                                <svg className="nav-icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M18 20V10" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    <path d="M12 20V4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    <path d="M6 20V14" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                </svg>
                                {isOpen && <span className="nav-text">Reports & Analytics</span>}
                            </div>
                            {isOpen && (
                                <svg
                                    className={`expand-icon ${expandedMenus.reportsAnalytics ? 'rotated' : ''}`}
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    xmlns="http://www.w3.org/2000/svg"
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        toggleSubmenu('reportsAnalytics');
                                    }}
                                >
                                    <path d="M6 9L12 15L18 9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                </svg>
                            )}
                        </button>

                        {/* Reports & Analytics Submenu */}
                        {isOpen && expandedMenus.reportsAnalytics && (
                            <div className="submenu">
                                <NavLink to="/analytics/dashboard" className={({ isActive }) => `submenu-item ${isActive ? 'active' : ''}`}>
                                    <span className="submenu-dot"></span>
                                    <span className="submenu-text">Dashboard</span>
                                </NavLink>
                                {isAdminOrManager && (
                                    <>
                                        <NavLink to="/analytics/inventory-health" className={({ isActive }) => `submenu-item ${isActive ? 'active' : ''}`}>
                                            <span className="submenu-dot"></span>
                                            <span className="submenu-text">Inventory Health</span>
                                        </NavLink>
                                        <NavLink to="/analytics/financial-overview" className={({ isActive }) => `submenu-item ${isActive ?  'active' : ''}`}>
                                            <span className="submenu-dot"></span>
                                            <span className="submenu-text">Financial Overview</span>
                                        </NavLink>
                                        <NavLink to="/analytics/orders-summary" className={({ isActive }) => `submenu-item ${isActive ? 'active' : ''}`}>
                                            <span className="submenu-dot"></span>
                                            <span className="submenu-text">Orders Summary</span>
                                        </NavLink>
                                    </>
                                )}
                            </div>
                        )}
                    </div>
                </nav>

                {/* User Info & Logout */}
                <div className="sidebar-footer">
                    {isOpen && (
                        <div
                            className="user-info user-info-clickable"
                            onClick={handleUserInfoClick}
                            title="Click to view profile"
                        >
                            <div className="user-avatar">
                                {currentUser?.username?.charAt(0).toUpperCase() || 'U'}
                            </div>
                            <div className="user-details">
                                <p className="user-name">{currentUser?.username || 'User'}</p>
                                <p className="user-role">{currentUser?.role || 'Role'}</p>
                            </div>
                            <svg className="profile-arrow" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M9 18L15 12L9 6" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            </svg>
                        </div>
                    )}

                    {/* Clickable User Avatar (when sidebar is closed) */}
                    {! isOpen && (
                        <button
                            className="user-avatar-btn"
                            onClick={handleUserInfoClick}
                            title="View Profile"
                        >
                            {currentUser?.username?.charAt(0).toUpperCase() || 'U'}
                        </button>
                    )}

                    {/* Logout Button */}
                    <button
                        className="logout-btn"
                        onClick={handleLogout}
                        title="Logout"
                    >
                        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M9 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M16 17L21 12L16 7" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M21 12H9" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                        {isOpen && <span>Logout</span>}
                    </button>
                </div>
            </aside>
        </>
    );
};

export default Sidebar;