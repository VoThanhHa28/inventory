import React, { ReactNode } from 'react'
import { createPortal } from 'react-dom'

interface ModalProps {
  isOpen: boolean
  title: string
  children: ReactNode
  onClose: () => void
  footer?: ReactNode
  size?: 'sm' | 'md' | 'lg'
}

/**
 * Modal Component
 * Displays content in a modal dialog
 */
const Modal: React.FC<ModalProps> = ({
  isOpen,
  title,
  children,
  onClose,
  footer,
  size = 'md',
}) => {
  if (!isOpen) return null

  const sizeStyles = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
  }

  return createPortal(
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className={`${sizeStyles[size]} bg-white rounded-lg shadow-xl`}>
        {/* Header */}
        <div className="flex items-center justify-between border-b px-6 py-4">
          <h2 className="text-lg font-semibold text-gray-900">{title}</h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 text-2xl leading-none"
          >
            ✕
          </button>
        </div>

        {/* Content */}
        <div className="px-6 py-4 max-h-[60vh] overflow-y-auto">{children}</div>

        {/* Footer */}
        {footer && (
          <div className="border-t px-6 py-4 flex gap-3 justify-end">{footer}</div>
        )}
      </div>
    </div>,
    document.body,
  )
}

export default Modal
