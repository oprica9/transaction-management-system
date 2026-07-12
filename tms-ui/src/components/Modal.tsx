import { useEffect, useRef } from 'react';
import type { ReactNode } from 'react';
import './Modal.css';

interface ModalProps {
    title: string;
    onClose: () => void;
    children: ReactNode;
}

export function Modal({ title, onClose, children }: ModalProps) {
    const mouseDownOnBackdrop = useRef(false);

    useEffect(() => {
        function handleKeyDown(event: KeyboardEvent) {
            if (event.key === 'Escape') onClose();
        }
        document.addEventListener('keydown', handleKeyDown);
        return () => document.removeEventListener('keydown', handleKeyDown);
    }, [onClose]);

    function handleBackdropMouseDown(event: React.MouseEvent) {
        mouseDownOnBackdrop.current = event.target === event.currentTarget;
    }

    function handleBackdropClick(event: React.MouseEvent) {
        if (mouseDownOnBackdrop.current && event.target === event.currentTarget) {
            onClose();
        }
    }

    return (
        <div
            className="modal-backdrop"
            onMouseDown={handleBackdropMouseDown}
            onClick={handleBackdropClick}
        >
            <div
                className="modal"
                role="dialog"
                aria-modal="true"
                aria-labelledby="modal-title"
            >
                <div className="modal__header">
                    <h2 id="modal-title">{title}</h2>
                    <button
                        type="button"
                        className="modal__close"
                        onClick={onClose}
                        aria-label="Close dialog"
                    >
                        &times;
                    </button>
                </div>
                <div className="modal__body">{children}</div>
            </div>
        </div>
    );
}
