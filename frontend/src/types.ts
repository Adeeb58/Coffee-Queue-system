// types.ts - TypeScript type definitions for the application

export interface Order {
    id: number;
    orderNumber: string;
    customerName: string;
    drinkName: string;
    quantity: number;
    customizationNotes?: string;
    status: OrderStatus;
    priorityScore: number;
    currentWaitMinutes: number;
    estimatedPrepTime: number;
    emergencyFlag: boolean;
    baristaName?: string;
    baristaId?: number;
    orderTime: string;
    completionTime?: string;
}

export type OrderStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface Barista {
    id: number;
    name: string;
    status: BaristaStatus;
    currentWorkload: number;
    totalOrdersServed: number;
    averagePrepTime?: number;
}

export type BaristaStatus = 'AVAILABLE' | 'BUSY' | 'OFFLINE';

export interface OrderRequest {
    customerName: string;
    drinkId: number;
    quantity: number;
    customerPhone: string;
    customizationNotes?: string;
    emergencyFlag?: boolean;
}

export interface DrinkMenuItem {
    id: number;
    name: string;
    prepTime: number;
    price: number;
}

export interface QueueStats {
    totalPending: number;
    totalInProgress: number;
    totalCompleted: number;
    averageWaitTime: number;
    emergencyOrders: number;
}