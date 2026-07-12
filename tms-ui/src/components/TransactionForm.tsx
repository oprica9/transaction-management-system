import { useState } from 'react';
import type { FormEvent } from 'react';
import type { TransactionCreateRequest } from '../types/transaction';
import './TransactionForm.css';

interface TransactionFormProps {
    isSubmitting: boolean;

    onSubmit(
        request: TransactionCreateRequest,
    ): Promise<void>;

    onSuccess(): void;
}

interface FormState {
    transactionDate: string;
    accountNumber: string;
    accountHolderName: string;
    amount: string;
}

type FieldErrors = Partial<
    Record<keyof FormState, string>
>;

const EMPTY_FORM: FormState = {
    transactionDate: '',
    accountNumber: '',
    accountHolderName: '',
    amount: '',
};

export function TransactionForm({
    isSubmitting,
    onSubmit,
    onSuccess,
}: TransactionFormProps) {
    const [form, setForm] =
        useState<FormState>(EMPTY_FORM);

    const [errors, setErrors] =
        useState<FieldErrors>({});

    const [submitError, setSubmitError] =
        useState<string | null>(null);

    function updateField<K extends keyof FormState>(
        field: K,
        value: FormState[K],
    ) {
        setForm((current) => ({
            ...current,
            [field]: value,
        }));

        setErrors((current) => {
            if (!current[field]) {
                return current;
            }

            const next = { ...current };
            delete next[field];
            return next;
        });

        setSubmitError(null);
    }

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault();

        const validationErrors = validate(form);

        setErrors(validationErrors);

        if (
            Object.keys(validationErrors).length > 0
        ) {
            return;
        }

        setSubmitError(null);

        try {
            await onSubmit({
                transactionDate: form.transactionDate,
                accountNumber:
                    form.accountNumber.trim(),
                accountHolderName:
                    form.accountHolderName.trim(),
                amount: Number(form.amount),
            });

            onSuccess();
        } catch (error) {
            setSubmitError(
                error instanceof Error
                    ? error.message
                    : 'Could not save the transaction. Please try again.',
            );
        }
    }

    return (
        <form
            className="transaction-form"
            onSubmit={handleSubmit}
            noValidate
        >
            <div className="form-field">
                <label htmlFor="transactionDate">
                    Transaction Date
                </label>

                <input
                    id="transactionDate"
                    name="transactionDate"
                    type="date"
                    value={form.transactionDate}
                    disabled={isSubmitting}
                    data-autofocus
                    onChange={(event) =>
                        updateField(
                            'transactionDate',
                            event.target.value,
                        )
                    }
                    aria-invalid={
                        Boolean(errors.transactionDate)
                    }
                    aria-describedby={
                        errors.transactionDate
                            ? 'transactionDate-error'
                            : undefined
                    }
                />

                {errors.transactionDate && (
                    <p
                        id="transactionDate-error"
                        className="field-error"
                    >
                        {errors.transactionDate}
                    </p>
                )}
            </div>

            <div className="form-field">
                <label htmlFor="accountNumber">
                    Account Number
                </label>

                <input
                    id="accountNumber"
                    name="accountNumber"
                    type="text"
                    placeholder="e.g. 1234-5678-9012"
                    value={form.accountNumber}
                    disabled={isSubmitting}
                    autoComplete="off"
                    onChange={(event) =>
                        updateField(
                            'accountNumber',
                            event.target.value,
                        )
                    }
                    aria-invalid={
                        Boolean(errors.accountNumber)
                    }
                    aria-describedby={
                        errors.accountNumber
                            ? 'accountNumber-error'
                            : undefined
                    }
                />

                {errors.accountNumber && (
                    <p
                        id="accountNumber-error"
                        className="field-error"
                    >
                        {errors.accountNumber}
                    </p>
                )}
            </div>

            <div className="form-field">
                <label htmlFor="accountHolderName">
                    Account Holder Name
                </label>

                <input
                    id="accountHolderName"
                    name="accountHolderName"
                    type="text"
                    placeholder="e.g. Maria Johnson"
                    value={form.accountHolderName}
                    disabled={isSubmitting}
                    autoComplete="name"
                    onChange={(event) =>
                        updateField(
                            'accountHolderName',
                            event.target.value,
                        )
                    }
                    aria-invalid={
                        Boolean(
                            errors.accountHolderName,
                        )
                    }
                    aria-describedby={
                        errors.accountHolderName
                            ? 'accountHolderName-error'
                            : undefined
                    }
                />

                {errors.accountHolderName && (
                    <p
                        id="accountHolderName-error"
                        className="field-error"
                    >
                        {errors.accountHolderName}
                    </p>
                )}
            </div>

            <div className="form-field">
                <label htmlFor="amount">
                    Amount
                </label>

                <input
                    id="amount"
                    name="amount"
                    type="number"
                    step="0.01"
                    min="0.01"
                    inputMode="decimal"
                    placeholder="0.00"
                    value={form.amount}
                    disabled={isSubmitting}
                    onChange={(event) =>
                        updateField(
                            'amount',
                            event.target.value,
                        )
                    }
                    aria-invalid={
                        Boolean(errors.amount)
                    }
                    aria-describedby={
                        errors.amount
                            ? 'amount-error'
                            : undefined
                    }
                />

                {errors.amount && (
                    <p
                        id="amount-error"
                        className="field-error"
                    >
                        {errors.amount}
                    </p>
                )}
            </div>

            {submitError && (
                <p
                    className="form-error"
                    role="alert"
                >
                    {submitError}
                </p>
            )}

            <button
                type="submit"
                className="btn btn--primary btn--full"
                disabled={isSubmitting}
            >
                {isSubmitting
                    ? 'Saving…'
                    : 'Add Transaction'}
            </button>
        </form>
    );
}

function validate(form: FormState): FieldErrors {
    const errors: FieldErrors = {};

    if (!form.transactionDate) {
        errors.transactionDate =
            'Transaction date is required.';
    }

    if (!form.accountNumber.trim()) {
        errors.accountNumber =
            'Account number is required.';
    }

    if (!form.accountHolderName.trim()) {
        errors.accountHolderName =
            'Account holder name is required.';
    }

    const amountText = form.amount.trim();
    const amount = Number(amountText);

    if (!amountText) {
        errors.amount = 'Amount is required.';
    } else if (
        !Number.isFinite(amount)
        || amount <= 0
    ) {
        errors.amount =
            'Amount must be greater than zero.';
    }

    return errors;
}
