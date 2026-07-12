import type { TransactionStatus } from '../types/transaction';
import './StatusBadge.css';

interface StatusBadgeProps {
    status: TransactionStatus;
}

/**
 * Renders a transaction's status as a colored pill.
 * Pending -> yellow, Settled -> green, Failed -> red
 */
export function StatusBadge({ status }: StatusBadgeProps) {
    return <span className={`status-badge status-badge--${status.toLowerCase()}`}>{status}</span>;
}
