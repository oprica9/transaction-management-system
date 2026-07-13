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

const MAX_AMOUNT = '999999999999999.99';

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

    const today = toLocalDateString(new Date());

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

    function updateAmount(value: string) {
        if (/^\d{0,15}(?:\.\d{0,2})?$/.test(value)) {
            updateField('amount', value);
        }
    }

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault();

        const validationErrors = validate(form, today);

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
                    max={today}
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
                    max={MAX_AMOUNT}
                    inputMode="decimal"
                    placeholder="0.00"
                    value={form.amount}
                    disabled={isSubmitting}
                    onChange={(event) =>
                        updateAmount(event.target.value)
                    }
                    aria-invalid={Boolean(errors.amount)}
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

function validate(
    form: FormState,
    today: string,
): FieldErrors {
    const errors: FieldErrors = {};

    if (!form.transactionDate) {
        errors.transactionDate =
            'Transaction date is required.';
    } else if (form.transactionDate > today) {
        errors.transactionDate =
            'Transaction date cannot be in the future.';
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
        !/^\d+(?:\.\d{1,2})?$/.test(amountText)
    ) {
        errors.amount =
            'Amount must have at most two decimal places.';
    } else if (
        !Number.isFinite(amount)
        || amount <= 0
    ) {
        errors.amount =
            'Amount must be greater than zero.';
    } else if (amount > Number(MAX_AMOUNT)) {
        errors.amount =
            'Amount must not exceed 999999999999999.99.';
    }

    return errors;
}

function toLocalDateString(date: Date): string {
    const year = date.getFullYear();
    const month = String(
        date.getMonth() + 1,
    ).padStart(2, '0');
    const day = String(
        date.getDate(),
    ).padStart(2, '0');

    return `${year}-${month}-${day}`;
}
