import { useMemo } from 'react';
import { HttpTransactionService } from './api/HttpTransactionService';
import { useTransactions } from './hooks/useTransactions';
import { TransactionTable } from './components/TransactionTable';
import './App.css';

function App() {
  const service = useMemo(() => new HttpTransactionService(), []);
  const { transactions, isLoading, error } = useTransactions(service);

  return (
    <div className="page">
      <header className="page-header">
        <h1>Transactions</h1>
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
