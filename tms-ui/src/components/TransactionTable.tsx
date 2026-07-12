import type { Transaction } from '../types/transaction';
import { formatAmount, formatDate } from '../util/formatters';
import { StatusBadge } from './StatusBadge';
import './TransactionTable.css';

interface TransactionTableProps {
    transactions: Transaction[];
}

export function TransactionTable({
    transactions,
}: TransactionTableProps) {
    return (
        <div className="table-scroll">
            <table className="transaction-table">
                <caption className="sr-only">
                    Transactions
                </caption>

                <thead>
                    <tr>
                        <th scope="col">
                            Transaction Date
                        </th>
                        <th scope="col">
                            Account Number
                        </th>
                        <th scope="col">
                            Account Holder Name
                        </th>
                        <th
                            scope="col"
                            className="col-amount"
                        >
                            Amount
                        </th>
                        <th scope="col">
                            Status
                        </th>
                    </tr>
                </thead>

                <tbody>
                    {transactions.map(
                        (transaction, index) => (
                            <tr
                                key={getTransactionKey(
                                    transaction,
                                    index,
                                )}
                            >
                                <td>
                                    {formatDate(
                                        transaction.transactionDate,
                                    )}
                                </td>

                                <td className="col-mono">
                                    {
                                        transaction.accountNumber
                                    }
                                </td>

                                <td>
                                    {
                                        transaction.accountHolderName
                                    }
                                </td>

                                <td className="col-amount col-mono">
                                    {formatAmount(
                                        transaction.amount,
                                    )}
                                </td>

                                <td>
                                    <StatusBadge
                                        status={
                                            transaction.status
                                        }
                                    />
                                </td>
                            </tr>
                        ),
                    )}
                </tbody>
            </table>
        </div>
    );
}

function getTransactionKey(
    transaction: Transaction,
    index: number,
): string {
    /*
     * The API currently exposes no persistent transaction ID.
     * The index is used only as a duplicate fallback.
     *
     * A backend-generated UUID would be the proper long-term key.
     */
    return [
        transaction.transactionDate,
        transaction.accountNumber,
        transaction.accountHolderName,
        transaction.amount,
        transaction.status,
        index,
    ].join('|');
}
