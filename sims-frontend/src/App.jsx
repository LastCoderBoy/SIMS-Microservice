import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import MainLayout from './components/layout/MainLayout';
import InventoryDashboard from './pages/InventoryDashboard';
import DamageLoss from './pages/DamageLoss';
import ProtectedRoute from './components/auth/ProtectedRoute';
import './App.css';

function App() {
    return (
        <Router>
            <Routes>
                {/* Public Routes */}
                <Route path="/login" element={<Login />} />

                {/* Protected Routes with Layout */}
                <Route
                    path="/"
                    element={
                        <ProtectedRoute>
                            <MainLayout />
                        </ProtectedRoute>
                    }
                >
                    {/* Dashboard - Default Route */}
                    <Route index element={<Navigate to="/dashboard" replace />} />
                    <Route path="dashboard" element={<InventoryDashboard />} />

                    {/* Inventory Control Sub-routes */}
                    <Route path="inventory/total-items" element={<div className="coming-soon">Total Items Page (Coming Soon)</div>} />
                    <Route path="inventory/low-stock" element={<div className="coming-soon">Low Stock Page (Coming Soon)</div>} />
                    <Route path="inventory/damage-loss" element={<DamageLoss />} />
                    <Route path="inventory/incoming-stock" element={<div className="coming-soon">Incoming Stock Page (Coming Soon)</div>} />
                    <Route path="inventory/outgoing-stock" element={<div className="coming-soon">Outgoing Stock Page (Coming Soon)</div>} />

                    {/* Order Management Sub-routes */}
                    <Route path="orders/purchase-orders" element={<div className="coming-soon">Purchase Orders Page (Coming Soon)</div>} />
                    <Route path="orders/sales-orders" element={<div className="coming-soon">Sales Orders Page (Coming Soon)</div>} />

                    {/* Reports & Analytics Sub-routes */}
                    <Route path="analytics/inventory-health" element={<div className="coming-soon">Inventory Health Page (Coming Soon)</div>} />
                    <Route path="analytics/financial-overview" element={<div className="coming-soon">Financial Overview Page (Coming Soon)</div>} />
                    <Route path="analytics/orders-summary" element={<div className="coming-soon">Orders Summary Page (Coming Soon)</div>} />

                    {/* User Profile */}
                    <Route path="profile" element={<div className="coming-soon">User Profile Page (Coming Soon)</div>} />
                </Route>

                {/* Catch all - Redirect to Dashboard */}
                <Route path="*" element={<Navigate to="/dashboard" replace />} />
            </Routes>
        </Router>
    );
}

export default App;