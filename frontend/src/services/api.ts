// services/api.ts - API service layer for backend communication

import axios from 'axios';
import { Order, Barista, OrderRequest, DrinkMenuItem, QueueStats } from '../types';

const API_BASE_URL = process.env.REACT_APP_API_URL;

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});


// Add response interceptor for error handling
api.interceptors.response.use(
    (response) => response,
    (error) => {
        console.error('API Error:', error.response?.data || error.message);
        throw error;
    }
);

// ==================== ORDER ENDPOINTS ====================

/**
 * Get all orders
 */
export const getAllOrders = async (): Promise<Order[]> => {
    const response = await api.get('/orders');
    return response.data;
};

/**
 * Get pending orders (ordered by priority)
 */
export const getPendingOrders = async (): Promise<Order[]> => {
    const response = await api.get('/orders/pending');
    return response.data;
};

/**
 * Get orders by status
 */
export const getOrdersByStatus = async (status: string): Promise<Order[]> => {
    const response = await api.get(`/orders/status/${status}`);
    return response.data;
};

/**
 * Get a specific order by ID
 */
export const getOrderById = async (orderId: number): Promise<Order> => {
    const response = await api.get(`/orders/${orderId}`);
    return response.data;
};

/**
 * Create a new order
 */
export const createOrder = async (orderRequest: OrderRequest): Promise<Order> => {
    // Clean the payload - remove undefined values and ensure proper formatting
    const cleanedRequest = {
        customerName: orderRequest.customerName.trim(),
        customerPhone: orderRequest.customerPhone.trim(),
        drinkId: Number(orderRequest.drinkId),
        quantity: Number(orderRequest.quantity),
        ...(orderRequest.customizationNotes && orderRequest.customizationNotes.trim()
            ? { customizationNotes: orderRequest.customizationNotes.trim() }
            : {}),
        emergencyFlag: Boolean(orderRequest.emergencyFlag)
    };

    console.log('Sending order request:', cleanedRequest);

    try {
        const response = await api.post('/orders', cleanedRequest);
        return response.data;
    } catch (error: any) {
        console.error('Order creation failed:', error.response?.data);
        throw error;
    }
};

/**
 * Update order status
 */
export const updateOrderStatus = async (orderId: number, status: string): Promise<Order> => {
    const response = await api.put(`/orders/${orderId}/status`, null, {
        params: { status }
    });
    return response.data;
};

/**
 * Complete an order
 */
export const completeOrder = async (orderId: number): Promise<Order> => {
    const response = await api.put(`/orders/${orderId}/complete`);
    return response.data;
};

/**
 * Cancel an order
 */
export const cancelOrder = async (orderId: number): Promise<Order> => {
    const response = await api.put(`/orders/${orderId}/cancel`);
    return response.data;
};

/**
 * Mark order as emergency
 */
export const markOrderAsEmergency = async (orderId: number): Promise<Order> => {
    const response = await api.put(`/orders/${orderId}/emergency`);
    return response.data;
};

// ==================== BARISTA ENDPOINTS ====================

/**
 * Get all baristas
 */
export const getAllBaristas = async (): Promise<Barista[]> => {
    const response = await api.get('/baristas');
    return response.data;
};

/**
 * Get available baristas
 */
export const getAvailableBaristas = async (): Promise<Barista[]> => {
    const response = await api.get('/baristas/available');
    return response.data;
};

/**
 * Get a specific barista by ID
 */
export const getBaristaById = async (baristaId: number): Promise<Barista> => {
    const response = await api.get(`/baristas/${baristaId}`);
    return response.data;
};

/**
 * Create a new barista
 */
export const createBarista = async (name: string): Promise<Barista> => {
    const response = await api.post('/baristas', { name });
    return response.data;
};

/**
 * Update barista status
 */
export const updateBaristaStatus = async (baristaId: number, status: string): Promise<Barista> => {
    const response = await api.put(`/baristas/${baristaId}/status`, null, {
        params: { status }
    });
    return response.data;
};

/**
 * Assign next order to a barista
 */
export const assignNextOrder = async (baristaId: number): Promise<Order> => {
    const response = await api.post(`/baristas/${baristaId}/assign-next`);
    return response.data;
};

/**
 * Get barista's current orders
 */
export const getBaristaOrders = async (baristaId: number): Promise<Order[]> => {
    const response = await api.get(`/baristas/${baristaId}/orders`);
    return response.data;
};

// ==================== QUEUE/ANALYTICS ENDPOINTS ====================

/**
 * Get queue statistics
 */
export const getQueueStats = async (): Promise<QueueStats> => {
    const response = await api.get('/queue/stats');
    return response.data;
};

/**
 * Get next order in queue
 */
export const getNextOrder = async (): Promise<Order | null> => {
    const response = await api.get('/queue/next');
    return response.data;
};

// ==================== MENU ENDPOINTS (if available) ====================

/**
 * Get drink menu items
 */
export const getMenuItems = async (): Promise<DrinkMenuItem[]> => {
    try {
        const response = await api.get('/drinks');
        return response.data;
    } catch (error) {
        // Return default menu if endpoint doesn't exist
        return getDefaultMenu();
    }
};

/**
 * Default menu items if backend doesn't provide menu endpoint
 */
const getDefaultMenu = (): DrinkMenuItem[] => {
    return [
        { id: 1, name: 'Espresso', prepTime: 2, price: 3.50 },
        { id: 2, name: 'Cappuccino', prepTime: 3, price: 4.50 },
        { id: 3, name: 'Latte', prepTime: 3, price: 4.50 },
        { id: 4, name: 'Americano', prepTime: 2, price: 3.50 },
        { id: 5, name: 'Mocha', prepTime: 4, price: 5.00 },
        { id: 6, name: 'Macchiato', prepTime: 3, price: 4.00 },
        { id: 7, name: 'Flat White', prepTime: 3, price: 4.50 },
        { id: 8, name: 'Cold Brew', prepTime: 2, price: 4.00 },
        { id: 9, name: 'Iced Latte', prepTime: 3, price: 4.75 },
        { id: 10, name: 'Frappuccino', prepTime: 5, price: 5.50 },
        { id: 11, name: 'Hot Chocolate', prepTime: 3, price: 4.00 },
        { id: 12, name: 'Chai Latte', prepTime: 3, price: 4.25 },
    ];
};

export default api;