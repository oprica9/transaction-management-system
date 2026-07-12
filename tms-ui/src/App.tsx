import { useMemo } from 'react';
import { HttpTransactionService } from './api/HttpTransactionService';
import { useTransactions } from './hooks/useTransactions';
import { TransactionTable } from './components/TransactionTable';
import { AddTransactionButton } from './components/AddTransactionButton';
import './App.css';

function App() {
  const service = useMemo(() => new HttpTransactionService(), []);
  const { transactions, isLoading, error, isSubmitting, addTransaction } =
    useTransactions(service);

  return (
    <div className="page">
      <header className="page-header">
        <h1>Transactions</h1>
        <AddTransactionButton isSubmitting={isSubmitting} onAdd={addTransaction} />
      </header>

      {error && <p style={{ color: 'red' }}>{error}</p>}

      {isLoading ? (
        <p>Loading transactions…</p>
      ) : (
        <TransactionTable transactions={transactions} />
      )}
    </div>
  );
}

export default App;
