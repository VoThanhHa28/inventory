import React from 'react'

interface AlertProps {
  type: 'success' | 'error' | 'warning' | 'info'
  message: string
  onClose?: () => void
}

/**
 * Reusable Alert Component
 * For displaying success/error messages
 */
const Alert: React.FC<AlertProps> = ({ type, message, onClose }) => {
  const styles = {
    success: 'bg-green-100 border-green-400 text-green-700',
    error: 'bg-red-100 border-red-400 text-red-700',
    warning: 'bg-yellow-100 border-yellow-400 text-yellow-700',
    info: 'bg-blue-100 border-blue-400 text-blue-700',
  }

  const icons = {
    success: '✓',
    error: '✕',
    warning: '⚠',
    info: 'ℹ',
  }

  return (
    <div className={`p-4 border rounded-lg ${styles[type]} mb-4 flex justify-between items-start`}>
      <div className="flex gap-3">
        <span className="font-bold">{icons[type]}</span>
        <p>{message}</p>
      </div>
      {onClose && (
        <button
          onClick={onClose}
          className="text-xl font-light hover:opacity-70"
        >
          ×
        </button>
      )}
    </div>
  )
}

export default Alert
