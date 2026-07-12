import type {
    TransactionStatus,
} from '../types/transaction';
import './StatusBadge.css';

interface StatusBadgeProps {
    status: TransactionStatus;
}

const STATUS_CLASS_NAMES: Record<
    TransactionStatus,
    string
> = {
    Pending: 'status-badge--pending',
    Settled: 'status-badge--settled',
    Failed: 'status-badge--failed',
};

export function StatusBadge({
    status,
}: StatusBadgeProps) {
    return (
        <span
            className={[
                'status-badge',
                STATUS_CLASS_NAMES[status],
            ].join(' ')}
        >
            {status}
        </span>
    );
}
