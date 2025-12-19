import './ConfirmModal.css';

const ConfirmModal = ({
                          isOpen,
                          title,
                          message,
                          confirmText = 'Confirm',
                          cancelText = 'Cancel',
                          onConfirm,
                          onCancel,
                          isLoading = false,
                          type = 'danger', // 'danger', 'warning', 'info'
                      }) => {
    if (!isOpen) return null;

    const getTypeClass = () => {
        switch (type) {
            case 'danger':
                return 'confirm-danger';
            case 'warning':
                return 'confirm-warning';
            case 'info':
                return 'confirm-info';
            default:
                return 'confirm-danger';
        }
    };

    const getIcon = () => {
        switch (type) {
            case 'danger':
                return (
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M10.29 3.86L1.82 18C1.64537 18.3024 1.55296 18.6453 1.55199 18.9945C1.55101 19.3437 1.64151 19.6871 1.81445 19.9905C1.98738 20.2939 2.23675 20.5467 2.53773 20.7239C2.83871 20.901 3.18082 20.9962 3.53 21H20.47C20.8192 20.9962 21.1613 20.901 21.4623 20.7239C21.7633 20.5467 22.0126 20.2939 22.1856 19.9905C22.3585 19.6871 22.449 19.3437 22.448 18.9945C22.447 18.6453 22.3546 18.3024 22.18 18L13.71 3.86C13.5317 3.56611 13.2807 3.32312 12.9812 3.15448C12.6817 2.98585 12.3437 2.89725 12 2.89725C11.6563 2.89725 11.3183 2.98585 11.0188 3.15448C10.7193 3.32312 10.4683 3.56611 10.29 3.86Z" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                        <path d="M12 9V13" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        <circle cx="12" cy="17" r="1" fill="currentColor"/>
                    </svg>
                );
            case 'warning':
                return (
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                        <path d="M12 8V12" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                        <circle cx="12" cy="16" r="1" fill="currentColor"/>
                    </svg>
                );
            case 'info':
                return (
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <circle cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="2"/>
                        <path d="M12 16V12M12 8H12.01" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
                    </svg>
                );
            default:
                return null;
        }
    };

    return (
        <div className="confirm-modal-overlay" onClick={onCancel}>
            <div className={`confirm-modal-content ${getTypeClass()}`} onClick={(e) => e.stopPropagation()}>
                <div className="confirm-modal-icon">
                    {getIcon()}
                </div>

                <div className="confirm-modal-body">
                    <h2 className="confirm-modal-title">{title}</h2>
                    <div className="confirm-modal-message">{message}</div>
                </div>

                <div className="confirm-modal-actions">
                    <button
                        className="confirm-btn-cancel"
                        onClick={onCancel}
                        disabled={isLoading}
                    >
                        {cancelText}
                    </button>
                    <button
                        className="confirm-btn-confirm"
                        onClick={onConfirm}
                        disabled={isLoading}
                    >
                        {isLoading ? (
                            <>
                                <div className="confirm-spinner"></div>
                                Loading...
                            </>
                        ) : (
                            confirmText
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmModal;