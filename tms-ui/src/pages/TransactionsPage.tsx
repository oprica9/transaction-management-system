
import { AddTransactionDialog } from '../components/AddTransactionDialog';
import { ErrorBanner } from '../components/ErrorBanner';
import { TransactionTable } from '../components/TransactionTable';
import { useTransactions } from '../hooks/useTransactions';
import './TransactionsPage.css';

export function TransactionsPage() {
    const {
        transactions,
        isLoading,
        loadError,
        isSubmitting,
        addTransaction,
        refresh,
    } = useTransactions();

    const canAddTransaction = !isLoading && loadError === null;

    return (
        <main className="page">
            <header className="page-header">
                <div className="page-header__copy">
                    <p className="eyebrow">
                        Transaction Management System
                    </p>

                    <h1 className="page-title">
                        Transactions
                    </h1>

                    <p className="page-subtitle">
                        View and create transaction records stored by the
                        CSV-backed API.
                    </p>
                </div>

                <AddTransactionDialog
                    disabled={!canAddTransaction}
                    isSubmitting={isSubmitting}
                    onAdd={addTransaction}
                />
            </header>

            <section
                className="transactions-panel"
                aria-label="Transactions"
                aria-busy={isLoading}
            >
                {isLoading ? (
                    <p
                        className="state-message"
                        role="status"
                        aria-live="polite"
                    >
                        Loading transactions…
                    </p>
                ) : loadError ? (
                    <ErrorBanner
                        message={loadError}
                        onRetry={refresh}
                    />
                ) : transactions.length === 0 ? (
                    <div className="empty-state">
                        <p>No transactions yet.</p>
                        <p className="empty-state__hint">
                            Add one to see it appear here.
                        </p>
                    </div>
                ) : (
                    <TransactionTable
                        transactions={transactions}
                    />
                )}
            </section>
        </main>
    );
}
