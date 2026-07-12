import type { Transaction } from '../types/transaction';
import { StatusBadge } from './StatusBadge';
import { formatAmount, formatDate } from './formatters';
import './TransactionTable.css';

interface TransactionTableProps {
    transactions: Transaction[];
}

export function TransactionTable({ transactions }: TransactionTableProps) {
    if (transactions.length === 0) {
        return (
            <div className="empty-state">
                <p>No transactions yet.</p>
                <p className="empty-state__hint">Add one to see it appear here.</p>
            </div>
        );
    }

    return (
        <div className="table-wrapper">
            <table className="transaction-table">
                <thead>
                    <tr>
                        <th>Transaction Date</th>
                        <th>Account Number</th>
                        <th>Account Holder Name</th>
                        <th className="col-amount">Amount</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    {transactions.map((transaction, index) => (
                        <tr key={`${transaction.transactionDate}-${transaction.accountNumber}-${index}`}>
                            <td>{formatDate(transaction.transactionDate)}</td>
                            <td className="col-mono">{transaction.accountNumber}</td>
                            <td>{transaction.accountHolderName}</td>
                            <td className="col-amount col-mono">{formatAmount(transaction.amount)}</td>
                            <td>
                                <StatusBadge status={transaction.status} />
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}
