import type { Transaction, TransactionCreateRequest } from '../types/transaction';

export interface TransactionService {
    getAll(): Promise<Transaction[]>;
    create(request: TransactionCreateRequest): Promise<Transaction>;
}
