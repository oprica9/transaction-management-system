import { API_BASE_URL } from './config';
import { ApiError } from './ApiError';
import type { Transaction, TransactionCreateRequest } from '../types/transaction';
import type { TransactionService } from './TransactionService';

export class HttpTransactionService implements TransactionService {
    private readonly baseUrl: string;

    constructor(baseUrl: string = API_BASE_URL) {
        this.baseUrl = baseUrl;
    }

    async getAll(): Promise<Transaction[]> {
        const response = await this.request('/transactions', { method: 'GET' });
        return response.json() as Promise<Transaction[]>;
    }

    async create(payload: TransactionCreateRequest): Promise<Transaction> {
        const response = await this.request('/transactions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload),
        });
        return response.json() as Promise<Transaction>;
    }

    private async request(path: string, init: RequestInit): Promise<Response> {
        let response: Response;
        try {
            response = await fetch(`${this.baseUrl}${path}`, init);
        } catch {
            throw new ApiError(
                `Could not reach the API at ${this.baseUrl}`,
            );
        }

        if (!response.ok) {
            throw new ApiError(await this.describeError(response), response.status);
        }

        return response;
    }

    /** Tries to surface a useful message from a Spring `ProblemDetail` / validation error body. */
    private async describeError(response: Response): Promise<string> {
        try {
            const body = await response.json();
            if (typeof body?.detail === 'string') return body.detail;
            if (typeof body?.message === 'string') return body.message;
            if (body?.errors && typeof body.errors === 'object') {
                return Object.values(body.errors).join(', ');
            }
        } catch {
            // response body wasn't JSON fall through to the generic message
        }
        return `Request failed with status ${response.status}`;
    }
}
