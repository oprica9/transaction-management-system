import { useState } from 'react';
import { Modal } from './Modal';
import { TransactionForm } from './TransactionForm';
import type { TransactionCreateRequest } from '../types/transaction';

interface AddTransactionButtonProps {
    isSubmitting: boolean;
    onAdd: (request: TransactionCreateRequest) => Promise<boolean>;
}

export function AddTransactionButton({ isSubmitting, onAdd }: AddTransactionButtonProps) {
    const [isOpen, setIsOpen] = useState(false);

    return (
        <>
            <button type="button" className="btn btn--primary" onClick={() => setIsOpen(true)}>
                + Add Transaction
            </button>

            {isOpen && (
                <Modal title="Add Transaction" onClose={() => setIsOpen(false)}>
                    <TransactionForm
                        isSubmitting={isSubmitting}
                        onSubmit={onAdd}
                        onSuccess={() => setIsOpen(false)}
                    />
                </Modal>
            )}
        </>
    );
}
