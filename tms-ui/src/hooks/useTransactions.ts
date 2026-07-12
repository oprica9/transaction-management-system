import { useCallback, useEffect, useState } from 'react';
import type { TransactionService } from '../api/TransactionService';
import type { Transaction, TransactionCreateRequest } from '../types/transaction';
import { ApiError } from '../api/ApiError';

interface UseTransactionsResult {
    transactions: Transaction[];
    isLoading: boolean;
    error: string | null;
    isSubmitting: boolean;
    addTransaction: (request: TransactionCreateRequest) => Promise<boolean>;
    refresh: () => void;
}

/**
 * Owns the transaction list's lifecycle: initial load, manual refresh, and
 * adding a new transaction.
 */
export function useTransactions(service: TransactionService): UseTransactionsResult {
    const [transactions, setTransactions] = useState<Transaction[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [refreshToken, setRefreshToken] = useState(0);

    useEffect(() => {
        let cancelled = false;

        setIsLoading(true);
        setError(null);

        service
            .getAll()
            .then((data) => {
                if (!cancelled) setTransactions(data);
            })
            .catch((err: unknown) => {
                if (!cancelled) setError(toMessage(err));
            })
            .finally(() => {
                if (!cancelled) setIsLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [service, refreshToken]);

    const addTransaction = useCallback(
        async (request: TransactionCreateRequest): Promise<boolean> => {
            setIsSubmitting(true);
            setError(null);
            try {
                const created = await service.create(request);
                setTransactions((current) => [created, ...current]);
                return true;
            } catch (err) {
                setError(toMessage(err));
                return false;
            } finally {
                setIsSubmitting(false);
            }
        },
        [service],
    );

    const refresh = useCallback(() => setRefreshToken((token) => token + 1), []);

    return { transactions, isLoading, error, isSubmitting, addTransaction, refresh };
}

function toMessage(err: unknown): string {
    if (err instanceof ApiError) return err.message;
    if (err instanceof Error) return err.message;
    return 'Something went wrong. Please try again.';
}
