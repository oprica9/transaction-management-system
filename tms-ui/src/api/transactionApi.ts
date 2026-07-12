import { ApiError } from './ApiError';
import type {
    Transaction,
    TransactionCreateRequest,
} from '../types/transaction';

const API_BASE_URL = (
    import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
).replace(/\/+$/, '');

export interface TransactionApi {
    getAll(signal?: AbortSignal): Promise<Transaction[]>;

    create(
        request: TransactionCreateRequest,
        signal?: AbortSignal,
    ): Promise<Transaction>;
}

export const transactionApi: TransactionApi = {
    getAll(signal) {
        return request<Transaction[]>('/transactions', {
            method: 'GET',
            signal,
        });
    },

    create(payload, signal) {
        return request<Transaction>('/transactions', {
            method: 'POST',
            signal,
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });
    },
};

async function request<T>(
    path: string,
    init: RequestInit,
): Promise<T> {
    let response: Response;

    try {
        response = await fetch(`${API_BASE_URL}${path}`, init);
    } catch (error) {
        if (error instanceof Error && error.name === 'AbortError') {
            throw error;
        }

        throw new ApiError(
            `Could not reach the API at ${API_BASE_URL}.`,
        );
    }

    if (!response.ok) {
        throw new ApiError(
            await describeError(response),
            response.status,
        );
    }

    return response.json() as Promise<T>;
}

async function describeError(response: Response): Promise<string> {
    try {
        const body: unknown = await response.json();

        if (!isRecord(body)) {
            return fallbackError(response.status);
        }

        if (typeof body.detail === 'string') {
            return body.detail;
        }

        if (typeof body.message === 'string') {
            return body.message;
        }

        if (isRecord(body.errors)) {
            const messages = Object.values(body.errors)
                .flatMap(extractErrorMessages);

            if (messages.length > 0) {
                return messages.join(', ');
            }
        }
    } catch {
        // The response body was empty or not JSON.
    }

    return fallbackError(response.status);
}

function extractErrorMessages(value: unknown): string[] {
    if (typeof value === 'string') {
        return [value];
    }

    if (Array.isArray(value)) {
        return value.filter(
            (item): item is string => typeof item === 'string',
        );
    }

    return [];
}

function fallbackError(status: number): string {
    return `Request failed with status ${status}.`;
}

function isRecord(
    value: unknown,
): value is Record<string, unknown> {
    return typeof value === 'object' && value !== null;
}
