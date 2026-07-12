import { useCallback, useState } from 'react';
import type { TransactionCreateRequest } from '../types/transaction';
import { Modal } from './Modal';
import { TransactionForm } from './TransactionForm';

interface AddTransactionDialogProps {
    disabled?: boolean;
    isSubmitting: boolean;

    onAdd(
        request: TransactionCreateRequest,
    ): Promise<void>;
}

export function AddTransactionDialog({
    disabled = false,
    isSubmitting,
    onAdd,
}: AddTransactionDialogProps) {
    const [isOpen, setIsOpen] = useState(false);

    const openDialog = useCallback(() => {
        setIsOpen(true);
    }, []);

    const closeDialog = useCallback(() => {
        setIsOpen(false);
    }, []);

    return (
        <>
            <button
                type="button"
                className="btn btn--primary"
                disabled={disabled || isSubmitting}
                onClick={openDialog}
                aria-haspopup="dialog"
            >
                + Add Transaction
            </button>

            {isOpen && (
                <Modal
                    title="Add Transaction"
                    onClose={closeDialog}
                >
                    <TransactionForm
                        isSubmitting={isSubmitting}
                        onSubmit={onAdd}
                        onSuccess={closeDialog}
                    />
                </Modal>
            )}
        </>
    );
}
