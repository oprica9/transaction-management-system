/** Formats a numeric amount as USD currency, e.g. 150 -> "$150.00". */
export function formatAmount(amount: number): string {
    return new Intl.NumberFormat('en-US', {
        style: 'currency',
        currency: 'USD',
    }).format(amount);
}

/** Formats an ISO date string (YYYY-MM-DD) for display, e.g. "Mar 1, 2025". */
export function formatDate(isoDate: string): string {
    const [year, month, day] = isoDate.split('-').map(Number);
    if (!year || !month || !day) return isoDate;

    // Construct with local components to avoid UTC off-by-one-day shifts.
    const date = new Date(year, month - 1, day);
    return new Intl.DateTimeFormat('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
    }).format(date);
}
