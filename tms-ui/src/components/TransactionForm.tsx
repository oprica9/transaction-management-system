import { useState } from 'react';
import type { FormEvent } from 'react';
import type { TransactionCreateRequest } from '../types/transaction';
import './TransactionForm.css';

interface TransactionFormProps {
    isSubmitting: boolean;
    onSubmit: (request: TransactionCreateRequest) => Promise<boolean>;
    onSuccess: () => void;
}

interface FormState {
    transactionDate: string;
    accountNumber: string;
    accountHolderName: string;
    amount: string;
}

type FieldErrors = Partial<Record<keyof FormState, string>>;

const EMPTY_FORM: FormState = {
    transactionDate: '',
    accountNumber: '',
    accountHolderName: '',
    amount: '',
};

/** Validates the form locally so obviously-bad input never reaches the API. */
function validate(form: FormState): FieldErrors {
    const errors: FieldErrors = {};

    if (!form.transactionDate) {
        errors.transactionDate = 'Transaction date is required.';
    }

    if (!form.accountNumber.trim()) {
        errors.accountNumber = 'Account number is required.';
    }

    if (!form.accountHolderName.trim()) {
        errors.accountHolderName = 'Account holder name is required.';
    }

    const amount = Number(form.amount);
    if (!form.amount) {
        errors.amount = 'Amount is required.';
    } else if (Number.isNaN(amount) || amount <= 0) {
        errors.amount = 'Amount must be a number greater than zero.';
    }

    return errors;
}

export function TransactionForm({ isSubmitting, onSubmit, onSuccess }: TransactionFormProps) {
    const [form, setForm] = useState<FormState>(EMPTY_FORM);
    const [errors, setErrors] = useState<FieldErrors>({});
    const [submitError, setSubmitError] = useState<string | null>(null);

    function updateField<K extends keyof FormState>(field: K, value: FormState[K]) {
        setForm((current) => ({ ...current, [field]: value }));
    }

    async function handleSubmit(event: FormEvent) {
        event.preventDefault();

        const validationErrors = validate(form);
        setErrors(validationErrors);
        if (Object.keys(validationErrors).length > 0) return;

        setSubmitError(null);
        const succeeded = await onSubmit({
            transactionDate: form.transactionDate,
            accountNumber: form.accountNumber.trim(),
            accountHolderName: form.accountHolderName.trim(),
            amount: Number(form.amount),
        });

        if (succeeded) {
            onSuccess();
        } else {
            setSubmitError('Could not save the transaction. Please try again.');
        }
    }

    return (
        <form className="transaction-form" onSubmit={handleSubmit} noValidate>
            <div className="form-field">
                <label htmlFor="transactionDate">Transaction Date</label>
                <input
                    id="transactionDate"
                    type="date"
                    value={form.transactionDate}
                    onChange={(e) => updateField('transactionDate', e.target.value)}
                    aria-invalid={Boolean(errors.transactionDate)}
                />
                {errors.transactionDate && <p className="field-error">{errors.transactionDate}</p>}
            </div>

            <div className="form-field">
                <label htmlFor="accountNumber">Account Number</label>
                <input
                    id="accountNumber"
                    type="text"
                    placeholder="e.g. 1234-5678-9012"
                    value={form.accountNumber}
                    onChange={(e) => updateField('accountNumber', e.target.value)}
                    aria-invalid={Boolean(errors.accountNumber)}
                />
                {errors.accountNumber && <p className="field-error">{errors.accountNumber}</p>}
            </div>

            <div className="form-field">
                <label htmlFor="accountHolderName">Account Holder Name</label>
                <input
                    id="accountHolderName"
                    type="text"
                    placeholder="e.g. Maria Johnson"
                    value={form.accountHolderName}
                    onChange={(e) => updateField('accountHolderName', e.target.value)}
                    aria-invalid={Boolean(errors.accountHolderName)}
                />
                {errors.accountHolderName && <p className="field-error">{errors.accountHolderName}</p>}
            </div>

            <div className="form-field">
                <label htmlFor="amount">Amount</label>
                <input
                    id="amount"
                    type="number"
                    step="0.01"
                    min="0"
                    placeholder="0.00"
                    value={form.amount}
                    onChange={(e) => updateField('amount', e.target.value)}
                    aria-invalid={Boolean(errors.amount)}
                />
                {errors.amount && <p className="field-error">{errors.amount}</p>}
            </div>

            {submitError && <p className="form-error">{submitError}</p>}

            <button type="submit" className="btn btn--primary btn--full" disabled={isSubmitting}>
                {isSubmitting ? 'Saving…' : 'Add Transaction'}
            </button>
        </form>
    );
}
