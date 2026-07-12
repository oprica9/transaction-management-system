import {
    useCallback,
    useEffect,
    useState,
} from 'react';
import { ApiError } from '../api/ApiError';
import {
    transactionApi,
    type TransactionApi,
} from '../api/transactionApi';
import type {
    Transaction,
    TransactionCreateRequest,
} from '../types/transaction';

interface UseTransactionsResult {
    transactions: Transaction[];
    isLoading: boolean;
    loadError: string | null;
    isSubmitting: boolean;

    addTransaction(
        request: TransactionCreateRequest,
    ): Promise<void>;

    refresh(): void;
}

export function useTransactions(
    api: TransactionApi = transactionApi,
): UseTransactionsResult {
    const [transactions, setTransactions] = useState<Transaction[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState<string | null>(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [refreshToken, setRefreshToken] = useState(0);

    useEffect(() => {
        const controller = new AbortController();

        async function loadTransactions() {
            setIsLoading(true);
            setLoadError(null);

            try {
                const loadedTransactions = await api.getAll(
                    controller.signal,
                );

                setTransactions(loadedTransactions);
            } catch (error) {
                if (!controller.signal.aborted) {
                    setLoadError(toErrorMessage(error));
                }
            } finally {
                if (!controller.signal.aborted) {
                    setIsLoading(false);
                }
            }
        }

        void loadTransactions();

        return () => {
            controller.abort();
        };
    }, [api, refreshToken]);

    const addTransaction = useCallback(
        async (
            request: TransactionCreateRequest,
        ): Promise<void> => {
            setIsSubmitting(true);

            try {
                const createdTransaction = await api.create(request);

                // The backend appends to the CSV, so the frontend appends too.
                setTransactions((current) => [
                    ...current,
                    createdTransaction,
                ]);
            } finally {
                setIsSubmitting(false);
            }
        },
        [api],
    );

    const refresh = useCallback(() => {
        setRefreshToken((current) => current + 1);
    }, []);

    return {
        transactions,
        isLoading,
        loadError,
        isSubmitting,
        addTransaction,
        refresh,
    };
}

function toErrorMessage(error: unknown): string {
    if (error instanceof ApiError) {
        return error.message;
    }

    if (error instanceof Error) {
        return error.message;
    }

    return 'Something went wrong. Please try again.';
}
