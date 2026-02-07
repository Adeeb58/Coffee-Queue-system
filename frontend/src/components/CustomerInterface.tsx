import React, { useState, useEffect } from 'react';
import { Coffee, Clock, AlertCircle, CheckCircle, Loader } from 'lucide-react';
import { Order, DrinkMenuItem } from '../types';
import { createOrder, getPendingOrders, getMenuItems } from '../services/api';

const CustomerInterface: React.FC = () => {
    const [customerPhone, setCustomerPhone] = useState('');
    const [customerName, setCustomerName] = useState('');
    const [selectedDrinkId, setSelectedDrinkId] = useState<number | ''>('');
    const [quantity, setQuantity] = useState(1);
    const [customization, setCustomization] = useState('');
    const [isEmergency, setIsEmergency] = useState(false);
    const [loading, setLoading] = useState(false);
    const [orderPlaced, setOrderPlaced] = useState(false);
    const [currentOrder, setCurrentOrder] = useState<Order | null>(null);
    const [queuePosition, setQueuePosition] = useState<number>(0);
    const [pendingOrders, setPendingOrders] = useState<Order[]>([]);
    const [menuItems, setMenuItems] = useState<DrinkMenuItem[]>([]);

    useEffect(() => {
        loadMenu();
    }, []);

    const loadMenu = async () => {
        try {
            const items = await getMenuItems();
            setMenuItems(items);
        } catch (error) {
            console.error('Error loading menu:', error);
        }
    };

    useEffect(() => {
        if (orderPlaced && currentOrder) {
            const interval = setInterval(async () => {
                try {
                    const orders = await getPendingOrders();
                    setPendingOrders(orders);

                    // Find position of current order
                    const position = orders.findIndex(o => o.id === currentOrder.id);
                    if (position !== -1) {
                        setQueuePosition(position + 1);
                    } else {
                        // Order might be completed or in progress
                        // We strictly don't unset orderPlaced here to keep the success screen visible
                        // until user places new order, but we could update status text.
                    }
                } catch (error) {
                    console.error('Error fetching queue:', error);
                }
            }, 3000); // Update every 3 seconds

            return () => clearInterval(interval);
        }
    }, [orderPlaced, currentOrder]);

    const handleSubmitOrder = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!customerName.trim() || selectedDrinkId === '' || !customerPhone.trim()) {
            alert('Please fill in all required fields');
            return;
        }

        if (quantity < 1 || quantity > 10) {
            alert('Quantity must be between 1 and 10');
            return;
        }

        setLoading(true);

        try {
            const orderRequest = {
                drinkId: Number(selectedDrinkId),
                quantity,
                customerName: customerName.trim(),
                customerPhone: customerPhone.trim(),
                emergencyFlag: isEmergency,
                customizationNotes: customization.trim() || undefined
            };

            console.log('Submitting order:', orderRequest);

            const order = await createOrder(orderRequest);

            setCurrentOrder(order);
            setOrderPlaced(true);
            setQueuePosition(0); // Will be updated by effect

            // alert(`Order placed successfully!\nOrder Number: ${order.orderNumber}`); // Removed alert for smoother UI flow
        } catch (error: any) {
            console.error('Error creating order:', error);

            let message = 'Failed to place order.\n';

            if (error.response?.data?.errors) {
                message += error.response.data.errors
                    .map((e: any) => `${e.field}: ${e.defaultMessage}`)
                    .join('\n');
            } else if (error.message) {
                message += error.message;
            }

            alert(message);
        } finally {
            setLoading(false);
        }
    };

    const handleNewOrder = () => {
        setOrderPlaced(false);
        setCurrentOrder(null);
        setCustomerName('');
        setCustomerPhone('');
        setSelectedDrinkId('');
        setQuantity(1);
        setCustomization('');
        setIsEmergency(false);
        setQueuePosition(0);
    };

    const getEstimatedWaitTime = () => {
        if (!currentOrder) return 0;

        // If we have queue position, try to estimate
        if (queuePosition > 0) {
            // Simple estimation: 3 mins per order ahead + current order prep time
            const ordersAhead = Math.max(0, queuePosition - 1);
            return (ordersAhead * 3) + currentOrder.estimatedPrepTime;
        }

        return currentOrder.currentWaitMinutes || currentOrder.estimatedPrepTime;
    };

    if (orderPlaced && currentOrder) {
        return (
            <div className="min-h-screen bg-gradient-to-br from-amber-50 to-orange-50 p-6">
                <div className="max-w-2xl mx-auto">
                    <div className="bg-white rounded-2xl shadow-xl p-8">
                        {/* Success Header */}
                        <div className="text-center mb-8">
                            <div className="flex justify-center mb-4">
                                <CheckCircle className="w-20 h-20 text-green-500" />
                            </div>
                            <h2 className="text-3xl font-bold text-gray-800 mb-2">
                                Order Placed Successfully!
                            </h2>
                            <p className="text-gray-600">
                                Thank you, {currentOrder.customerName}
                            </p>
                        </div>

                        {/* Order Details Card */}
                        <div className="bg-gradient-to-r from-primary to-secondary text-white rounded-xl p-6 mb-6">
                            <div className="text-center mb-4">
                                <div className="text-sm opacity-90 mb-1">Order Number</div>
                                <div className="text-4xl font-bold tracking-wider">
                                    {currentOrder.orderNumber}
                                </div>
                            </div>

                            <div className="border-t border-white/30 pt-4 space-y-2">
                                <div className="flex justify-between">
                                    <span className="opacity-90">Drink:</span>
                                    <span className="font-semibold">
                                        {currentOrder.drinkName} x{currentOrder.quantity}
                                    </span>
                                </div>
                                {currentOrder.customizationNotes && (
                                    <div className="flex justify-between">
                                        <span className="opacity-90">Notes:</span>
                                        <span className="font-semibold">{currentOrder.customizationNotes}</span>
                                    </div>
                                )}
                                <div className="flex justify-between">
                                    <span className="opacity-90">Status:</span>
                                    <span className="font-semibold uppercase">
                                        {currentOrder.status.replace('_', ' ')}
                                    </span>
                                </div>
                            </div>
                        </div>

                        {/* Queue Information */}
                        <div className="grid grid-cols-2 gap-4 mb-6">
                            <div className="bg-blue-50 rounded-lg p-4 text-center">
                                <div className="text-3xl font-bold text-blue-600 mb-1">
                                    {queuePosition > 0 ? queuePosition : '-'}
                                </div>
                                <div className="text-sm text-gray-600">Position in Queue</div>
                            </div>

                            <div className="bg-orange-50 rounded-lg p-4 text-center">
                                <div className="text-3xl font-bold text-orange-600 mb-1">
                                    ~{getEstimatedWaitTime()}
                                </div>
                                <div className="text-sm text-gray-600">Minutes Wait</div>
                            </div>
                        </div>

                        {/* Emergency Badge */}
                        {currentOrder.emergencyFlag && (
                            <div className="bg-red-50 border-2 border-red-200 rounded-lg p-4 mb-6 flex items-center">
                                <AlertCircle className="w-6 h-6 text-red-500 mr-3" />
                                <div>
                                    <div className="font-semibold text-red-700">Priority Order</div>
                                    <div className="text-sm text-red-600">
                                        Your order has been marked as urgent and will be prioritized
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* Instructions */}
                        <div className="bg-gray-50 rounded-lg p-4 mb-6">
                            <h3 className="font-semibold text-gray-800 mb-2">What's Next?</h3>
                            <ul className="text-sm text-gray-600 space-y-1">
                                <li>• Keep an eye on your order number</li>
                                <li>• We'll call your number when your drink is ready</li>
                                <li>• You can track your position in real-time above</li>
                                <li>• Average preparation time: {currentOrder.estimatedPrepTime} minutes</li>
                            </ul>
                        </div>

                        {/* Action Button */}
                        <button
                            onClick={handleNewOrder}
                            className="w-full bg-primary text-white py-3 rounded-lg hover:bg-secondary transition-colors font-semibold"
                        >
                            Place Another Order
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-amber-50 to-orange-50 p-6">
            <div className="max-w-2xl mx-auto">
                <div className="bg-white rounded-2xl shadow-xl p-8">
                    {/* Header */}
                    <div className="text-center mb-8">
                        <div className="flex justify-center mb-4">
                            <Coffee className="w-16 h-16 text-primary" />
                        </div>
                        <h1 className="text-3xl font-bold text-gray-800 mb-2">
                            Welcome to Bean & Brew
                        </h1>
                        <p className="text-gray-600">
                            Place your order and we'll notify you when it's ready
                        </p>
                    </div>

                    {/* Order Form */}
                    <form onSubmit={handleSubmitOrder} className="space-y-6">
                        {/* Customer Name */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Your Name *
                            </label>
                            <input
                                type="text"
                                value={customerName}
                                onChange={(e) => setCustomerName(e.target.value)}
                                placeholder="Enter your name"
                                className="w-full px-4 py-3 border-2 border-gray-200 rounded-lg focus:border-primary focus:outline-none transition-colors"
                                required
                            />
                        </div>

                        {/* Drink Selection */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Select Your Drink *
                            </label>
                            <select
                                value={selectedDrinkId}
                                onChange={(e) => setSelectedDrinkId(e.target.value ? Number(e.target.value) : '')}
                                className="w-full px-4 py-3 border-2 border-gray-200 rounded-lg focus:border-primary focus:outline-none transition-colors"
                                required
                            >
                                <option value="">Choose a drink...</option>
                                {menuItems.map((drink) => (
                                    <option key={drink.id} value={drink.id}>
                                        {drink.name} - ${drink.price.toFixed(2)}
                                    </option>
                                ))}
                            </select>
                        </div>

                        {/* Quantity */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Quantity
                            </label>
                            <div className="flex items-center space-x-4">
                                <button
                                    type="button"
                                    onClick={() => setQuantity(Math.max(1, quantity - 1))}
                                    className="w-10 h-10 bg-gray-200 rounded-lg hover:bg-gray-300 font-bold"
                                >
                                    -
                                </button>
                                <span className="text-2xl font-bold text-gray-800 w-12 text-center">
                                    {quantity}
                                </span>
                                <button
                                    type="button"
                                    onClick={() => setQuantity(Math.min(10, quantity + 1))}
                                    className="w-10 h-10 bg-gray-200 rounded-lg hover:bg-gray-300 font-bold"
                                >
                                    +
                                </button>
                            </div>
                        </div>

                        {/* Customization */}
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Special Instructions (Optional)
                            </label>
                            <textarea
                                value={customization}
                                onChange={(e) => setCustomization(e.target.value)}
                                placeholder="Extra hot, no sugar, oat milk, etc."
                                rows={3}
                                className="w-full px-4 py-3 border-2 border-gray-200 rounded-lg focus:border-primary focus:outline-none transition-colors resize-none"
                            />
                        </div>
                        <div>
                            <label className="block text-sm font-semibold text-gray-700 mb-2">
                                Customer Phone Number *
                            </label>

                            <input
                                type="tel"
                                value={customerPhone}
                                onChange={(e) => setCustomerPhone(e.target.value)}
                                placeholder="123-456-7890"
                                className="w-full px-4 py-3 border-2 border-gray-200 rounded-lg 
               focus:border-primary focus:outline-none transition-colors"
                                required
                            />
                        </div>

                        {/* Emergency Flag */}
                        <div className="bg-orange-50 border-2 border-orange-200 rounded-lg p-4">
                            <label className="flex items-start space-x-3 cursor-pointer">
                                <input
                                    type="checkbox"
                                    checked={isEmergency}
                                    onChange={(e) => setIsEmergency(e.target.checked)}
                                    className="w-5 h-5 mt-0.5 text-primary focus:ring-primary"
                                />
                                <div className="flex-1">
                                    <div className="font-semibold text-gray-800 flex items-center">
                                        <AlertCircle className="w-5 h-5 mr-2 text-orange-600" />
                                        Mark as Priority Order
                                    </div>
                                    <p className="text-sm text-gray-600 mt-1">
                                        Check this if you're in a hurry. Priority orders are processed first.
                                    </p>
                                </div>
                            </label>
                        </div>

                        {/* Submit Button */}
                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full bg-primary text-white py-4 rounded-lg hover:bg-secondary transition-colors font-bold text-lg flex items-center justify-center disabled:bg-gray-400 disabled:cursor-not-allowed"
                        >
                            {loading ? (
                                <>
                                    <Loader className="w-5 h-5 mr-2 animate-spin" />
                                    Placing Order...
                                </>
                            ) : (
                                <>
                                    <Coffee className="w-5 h-5 mr-2" />
                                    Place Order
                                </>
                            )}
                        </button>
                    </form>

                    {/* Info Footer */}
                    <div className="mt-8 pt-6 border-t border-gray-200">
                        <div className="flex items-start space-x-3 text-sm text-gray-600">
                            <Clock className="w-5 h-5 text-primary flex-shrink-0 mt-0.5" />
                            <div>
                                <p className="font-semibold text-gray-700 mb-1">Estimated Wait Times:</p>
                                <p>Most drinks: 2-5 minutes | Complex drinks: 5-8 minutes</p>
                                <p className="mt-1">We'll display your position in the queue after ordering.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CustomerInterface;