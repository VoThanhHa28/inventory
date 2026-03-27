import React, { useEffect, useState } from 'react'

export type ToastType = 'success' | 'error' | 'warning' | 'info'

interface ToastProps {
  id: string
  type: ToastType
  message: string
  duration?: number
  onClose: (id: string) => void
}

/**
 * Individual Toast Component
 * Displays a single toast notification
 */
const Toast: React.FC<ToastProps> = ({ id, type, message, duration = 5000, onClose }) => {
  useEffect(() => {
    const timer = setTimeout(() => onClose(id), duration)
    return () => clearTimeout(timer)
  }, [id, duration, onClose])

  const styles = {
    success: 'bg-green-50 text-green-800 border-green-200',
    error: 'bg-red-50 text-red-800 border-red-200',
    warning: 'bg-yellow-50 text-yellow-800 border-yellow-200',
    info: 'bg-blue-50 text-blue-800 border-blue-200',
  }

  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ',
  }

  return (
    <div
      className={`${styles[type]} border-l-4 p-4 rounded-lg shadow-lg flex items-start gap-3`}
    >
      <span className="text-lg font-bold flex-shrink-0">{icons[type]}</span>
      <p className="flex-1 text-sm">{message}</p>
      <button
        onClick={() => onClose(id)}
        className="text-lg hover:opacity-70 flex-shrink-0"
      >
        ✕
      </button>
    </div>
  )
}

/**
 * Toast Container Component
 * Manages multiple toast notifications
 */
export const useToast = () => {
  const [toasts, setToasts] = useState<
    Array<{ id: string; type: ToastType; message: string }>
  >([])

  const addToast = (type: ToastType, message: string, duration?: number) => {
    const id = Date.now().toString()
    setToasts((prev) => [...prev, { id, type, message }])

    const timer = setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id))
    }, duration || 5000)

    return () => clearTimeout(timer)
  }

  const removeToast = (id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }

  return { toasts, addToast, removeToast }
}

export const ToastContainer: React.FC<{
  toasts: Array<{ id: string; type: ToastType; message: string }>
  onRemove: (id: string) => void
}> = ({ toasts, onRemove }) => {
  return (
    <div className="fixed top-4 right-4 z-50 space-y-2 max-w-sm">
      {toasts.map((toast) => (
        <Toast
          key={toast.id}
          id={toast.id}
          type={toast.type}
          message={toast.message}
          onClose={onRemove}
        />
      ))}
    </div>
  )
}

export default Toast
