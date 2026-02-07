import React, { useState, useEffect } from 'react';
import { Clock, CheckCircle, AlertTriangle, User } from 'lucide-react';
import { Order, Barista } from '../types';
import { getAllBaristas, getPendingOrders, assignNextOrder, completeOrder } from '../services/api';

const BaristaDashboard: React.FC = () => {
    const [baristas, setBaristas] = useState<Barista[]>([]);
    const [pendingOrders, setPendingOrders] = useState<Order[]>([]);
    const [selectedBarista, setSelectedBarista] = useState<number | null>(null);
    const [autoRefresh, setAutoRefresh] = useState(true);

    useEffect(() => {
        loadData();
        if (autoRefresh) {
            const interval = setInterval(loadData, 5000); // Refresh every 5 seconds
            return () => clearInterval(interval);
        }
    }, [autoRefresh]);

    const loadData = async () => {
        try {
            const [baristasData, ordersData] = await Promise.all([
                getAllBaristas(),
                getPendingOrders(),
            ]);
            setBaristas(baristasData);
            setPendingOrders(ordersData);
        } catch (error) {
            console.error('Error loading data:', error);
        }
    };

    const handleAssignOrder = async (baristaId: number) => {
        try {
            await assignNextOrder(baristaId);
            await loadData();
        } catch (error) {
            console.error('Error assigning order:', error);
        }
    };

    const handleCompleteOrder = async (orderId: number) => {
        try {
            await completeOrder(orderId);
            await loadData();
        } catch (error) {
            console.error('Error completing order:', error);
        }
    };

    const getBaristasOrders = (baristaId: number) => {
        return pendingOrders.filter(
            (order) => order.baristaName === baristas.find((b) => b.id === baristaId)?.name
        );
    };

    return (
        <div className="min-h-screen bg-gray-100 p-6">
            <div className="max-w-7xl mx-auto">
                {/* Header */}
                <div className="flex justify-between items-center mb-6">
                    <h1 className="text-3xl font-bold text-gray-800">Barista Dashboard</h1>
                    <div className="flex items-center space-x-4">
                        <label className="flex items-center space-x-2">
                            <input
                                type="checkbox"
                                checked={autoRefresh}
                                onChange={(e) => setAutoRefresh(e.target.checked)}
                                className="w-4 h-4"
                            />
                            <span className="text-sm">Auto Refresh</span>
                        </label>
                        <button
                            onClick={loadData}
                            className="bg-primary text-white px-4 py-2 rounded-lg hover:bg-secondary"
                        >
                            Refresh Now
                        </button>
                    </div>
                </div>

                {/* Queue Stats */}
                <div className="grid grid-cols-4 gap-4 mb-6">
                    <div className="bg-white p-4 rounded-lg shadow">
                        <div className="text-2xl font-bold text-gray-800">
                            {pendingOrders.length}
                        </div>
                        <div className="text-sm text-gray-600">Pending Orders</div>
                    </div>
                    <div className="bg-white p-4 rounded-lg shadow">
                        <div className="text-2xl font-bold text-orange-600">
                            {pendingOrders.filter((o) => o.emergencyFlag).length}
                        </div>
                        <div className="text-sm text-gray-600">Emergency</div>
                    </div>
                    <div className="bg-white p-4 rounded-lg shadow">
                        <div className="text-2xl font-bold text-blue-600">
                            {Math.round(
                                pendingOrders.reduce((sum, o) => sum + o.currentWaitMinutes, 0) /
                                Math.max(pendingOrders.length, 1)
                            )}
                            min
                        </div>
                        <div className="text-sm text-gray-600">Avg Wait Time</div>
                    </div>
                    <div className="bg-white p-4 rounded-lg shadow">
                        <div className="text-2xl font-bold text-green-600">
                            {baristas.filter((b) => b.status === 'AVAILABLE').length}
                        </div>
                        <div className="text-sm text-gray-600">Available Baristas</div>
                    </div>
                </div>

                {/* Baristas Grid */}
                <div className="grid grid-cols-3 gap-6">
                    {baristas.map((barista) => {
                        const baristaOrders = getBaristasOrders(barista.id);
                        const currentOrder = baristaOrders.find((o) => o.status === 'IN_PROGRESS');

                        return (
                            <div key={barista.id} className="bg-white rounded-lg shadow-lg p-6">
                                {/* Barista Header */}
                                <div className="flex items-center justify-between mb-4">
                                    <div className="flex items-center space-x-3">
                                        <User className="w-8 h-8 text-primary" />
                                        <div>
                                            <h3 className="font-bold text-lg">{barista.name}</h3>
                                            <span
                                                className={`text-xs px-2 py-1 rounded-full ${barista.status === 'AVAILABLE'
                                                    ? 'bg-green-100 text-green-700'
                                                    : barista.status === 'BUSY'
                                                        ? 'bg-yellow-100 text-yellow-700'
                                                        : 'bg-gray-100 text-gray-700'
                                                    }`}
                                            >
                                                {barista.status}
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                {/* Workload */}
                                <div className="mb-4">
                                    <div className="flex justify-between text-sm mb-1">
                                        <span>Workload</span>
                                        <span>{barista.currentWorkload} min</span>
                                    </div>
                                    <div className="w-full bg-gray-200 rounded-full h-2">
                                        <div
                                            className="bg-primary rounded-full h-2 transition-all"
                                            style={{
                                                width: `${Math.min((barista.currentWorkload / 20) * 100, 100)}%`,
                                            }}
                                        />
                                    </div>
                                </div>

                                {/* Current Order */}
                                {currentOrder && (
                                    <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3 mb-3">
                                        <div className="flex items-center justify-between mb-2">
                                            <span className="font-bold text-sm">Current Order</span>
                                            <span className="text-xs text-gray-600">
                                                {currentOrder.orderNumber}
                                            </span>
                                        </div>
                                        <div className="text-sm mb-2">
                                            {currentOrder.drinkName} x{currentOrder.quantity}
                                        </div>
                                        <button
                                            onClick={() => handleCompleteOrder(currentOrder.id)}
                                            className="w-full bg-green-500 text-white py-2 rounded hover:bg-green-600 flex items-center justify-center space-x-2"
                                        >
                                            <CheckCircle className="w-4 h-4" />
                                            <span>Complete Order</span>
                                        </button>
                                    </div>
                                )}

                                {/* Assign Next Order Button */}
                                {!currentOrder && (
                                    <button
                                        onClick={() => handleAssignOrder(barista.id)}
                                        disabled={pendingOrders.length === 0}
                                        className="w-full bg-primary text-white py-2 rounded hover:bg-secondary disabled:bg-gray-400 flex items-center justify-center space-x-2"
                                    >
                                        <Clock className="w-4 h-4" />
                                        <span>Assign Next Order</span>
                                    </button>
                                )}

                                {/* Orders Served */}
                                <div className="mt-4 text-center text-sm text-gray-600">
                                    {barista.totalOrdersServed} orders served today
                                </div>
                            </div>
                        );
                    })}
                </div>

                {/* Pending Queue */}
                <div className="mt-8 bg-white rounded-lg shadow-lg p-6">
                    <h2 className="text-2xl font-bold mb-4">Pending Queue (Priority Order)</h2>
                    <div className="space-y-3">
                        {pendingOrders
                            .filter((o) => o.status === 'PENDING')
                            .map((order, index) => (
                                <div
                                    key={order.id}
                                    className={`flex items-center justify-between p-4 rounded-lg border-2 ${order.emergencyFlag
                                        ? 'border-red-500 bg-red-50'
                                        : 'border-gray-200 bg-gray-50'
                                        }`}
                                >
                                    <div className="flex items-center space-x-4">
                                        <div className="text-2xl font-bold text-gray-400">#{index + 1}</div>
                                        <div>
                                            <div className="font-bold">{order.orderNumber}</div>
                                            <div className="text-sm text-gray-600">
                                                {order.drinkName} x{order.quantity}
                                            </div>
                                        </div>
                                    </div>
                                    <div className="flex items-center space-x-6">
                                        <div className="text-center">
                                            <div className="text-sm text-gray-600">Priority</div>
                                            <div className="font-bold">{order.priorityScore}</div>
                                        </div>
                                        <div className="text-center">
                                            <div className="text-sm text-gray-600">Wait Time</div>
                                            <div className="font-bold">{order.currentWaitMinutes} min</div>
                                        </div>
                                        <div className="text-center">
                                            <div className="text-sm text-gray-600">Prep Time</div>
                                            <div className="font-bold">{order.estimatedPrepTime} min</div>
                                        </div>
                                        {order.emergencyFlag && (
                                            <AlertTriangle className="w-6 h-6 text-red-500" />
                                        )}
                                    </div>
                                </div>
                            ))}
                        {pendingOrders.filter((o) => o.status === 'PENDING').length === 0 && (
                            <div className="text-center text-gray-500 py-8">
                                No pending orders in queue
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default BaristaDashboard;