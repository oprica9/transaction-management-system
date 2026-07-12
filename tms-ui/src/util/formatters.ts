const amountFormatter = new Intl.NumberFormat(
    'en-US',
    {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
    },
);

const dateFormatter = new Intl.DateTimeFormat(
    'en-US',
    {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
    },
);

export function formatAmount(
    amount: number,
): string {
    return amountFormatter.format(amount);
}

export function formatDate(
    isoDate: string,
): string {
    const parts = isoDate.split('-');

    if (parts.length !== 3) {
        return isoDate;
    }

    const [year, month, day] =
        parts.map(Number);

    if (
        !Number.isInteger(year)
        || !Number.isInteger(month)
        || !Number.isInteger(day)
    ) {
        return isoDate;
    }

    const date = new Date(
        year,
        month - 1,
        day,
    );

    const isValidDate =
        date.getFullYear() === year
        && date.getMonth() === month - 1
        && date.getDate() === day;

    return isValidDate
        ? dateFormatter.format(date)
        : isoDate;
}
