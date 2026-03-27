import React from 'react'

interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg'
  text?: string
}

/**
 * Reusable Loading Spinner Component
 * Shows loading state with optional text
 */
const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ size = 'md', text }) => {
  const sizeStyles = {
    sm: 'w-6 h-6 border-2',
    md: 'w-10 h-10 border-3',
    lg: 'w-16 h-16 border-4',
  }

  return (
    <div className="flex flex-col items-center justify-center gap-4">
      <div
        className={`${sizeStyles[size]} border-gray-300 border-t-blue-600 rounded-full animate-spin`}
      ></div>
      {text && <p className="text-gray-600 text-sm">{text}</p>}
    </div>
  )
}

export default LoadingSpinner
