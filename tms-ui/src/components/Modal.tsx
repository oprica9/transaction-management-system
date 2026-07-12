import {
    useEffect,
    useId,
    useRef,
} from 'react';
import type {
    MouseEvent as ReactMouseEvent,
    ReactNode,
} from 'react';
import { createPortal } from 'react-dom';
import './Modal.css';

interface ModalProps {
    title: string;
    onClose(): void;
    children: ReactNode;
}

const FOCUSABLE_SELECTOR = [
    'a[href]',
    'button:not([disabled])',
    'input:not([disabled])',
    'select:not([disabled])',
    'textarea:not([disabled])',
    '[tabindex]:not([tabindex="-1"])',
].join(',');

export function Modal({
    title,
    onClose,
    children,
}: ModalProps) {
    const titleId = useId();
    const dialogRef = useRef<HTMLDivElement>(null);
    const mouseDownOnBackdrop = useRef(false);

    useEffect(() => {
        const dialog = dialogRef.current;
        const previouslyFocusedElement =
            document.activeElement instanceof HTMLElement
                ? document.activeElement
                : null;

        const previousBodyOverflow =
            document.body.style.overflow;

        document.body.style.overflow = 'hidden';

        const preferredFocusTarget =
            dialog?.querySelector<HTMLElement>(
                '[data-autofocus]',
            );

        const firstFocusableElement =
            dialog?.querySelector<HTMLElement>(
                FOCUSABLE_SELECTOR,
            );

        (
            preferredFocusTarget
            ?? firstFocusableElement
            ?? dialog
        )?.focus();

        function handleKeyDown(event: KeyboardEvent) {
            if (event.key === 'Escape') {
                event.preventDefault();
                onClose();
                return;
            }

            if (event.key !== 'Tab' || !dialog) {
                return;
            }

            const focusableElements = Array.from(
                dialog.querySelectorAll<HTMLElement>(
                    FOCUSABLE_SELECTOR,
                ),
            );

            if (focusableElements.length === 0) {
                event.preventDefault();
                dialog.focus();
                return;
            }

            const firstElement = focusableElements[0];
            const lastElement =
                focusableElements[
                focusableElements.length - 1
                ];

            const activeElement = document.activeElement;

            if (
                event.shiftKey
                && (
                    activeElement === firstElement
                    || !dialog.contains(activeElement)
                )
            ) {
                event.preventDefault();
                lastElement.focus();
                return;
            }

            if (
                !event.shiftKey
                && activeElement === lastElement
            ) {
                event.preventDefault();
                firstElement.focus();
            }
        }

        document.addEventListener(
            'keydown',
            handleKeyDown,
        );

        return () => {
            document.removeEventListener(
                'keydown',
                handleKeyDown,
            );

            document.body.style.overflow =
                previousBodyOverflow;

            previouslyFocusedElement?.focus();
        };
    }, [onClose]);

    function handleBackdropMouseDown(
        event: ReactMouseEvent<HTMLDivElement>,
    ) {
        mouseDownOnBackdrop.current =
            event.target === event.currentTarget;
    }

    function handleBackdropClick(
        event: ReactMouseEvent<HTMLDivElement>,
    ) {
        const clickedBackdrop =
            event.target === event.currentTarget;

        if (
            mouseDownOnBackdrop.current
            && clickedBackdrop
        ) {
            onClose();
        }

        mouseDownOnBackdrop.current = false;
    }

    return createPortal(
        <div
            className="modal-backdrop"
            onMouseDown={handleBackdropMouseDown}
            onClick={handleBackdropClick}
        >
            <div
                ref={dialogRef}
                className="modal"
                role="dialog"
                aria-modal="true"
                aria-labelledby={titleId}
                tabIndex={-1}
            >
                <div className="modal__header">
                    <h2 id={titleId}>{title}</h2>

                    <button
                        type="button"
                        className="modal__close"
                        onClick={onClose}
                        aria-label="Close dialog"
                    >
                        ×
                    </button>
                </div>

                <div className="modal__body">
                    {children}
                </div>
            </div>
        </div>,
        document.body,
    );
}
